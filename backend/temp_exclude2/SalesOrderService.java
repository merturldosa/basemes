package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sales Order Service
 * 판매 주문 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final ShippingRepository shippingRepository;

    /**
     * Get all sales orders by tenant
     */
    public List<SalesOrderEntity> getAllSalesOrdersByTenant(String tenantId) {
        log.info("Getting all sales orders for tenant: {}", tenantId);
        return salesOrderRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get sales order by ID
     */
    public SalesOrderEntity getSalesOrderById(Long salesOrderId) {
        log.info("Getting sales order by ID: {}", salesOrderId);
        return salesOrderRepository.findByIdWithAllRelations(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found with ID: " + salesOrderId));
    }

    /**
     * Get sales orders by status
     */
    public List<SalesOrderEntity> getSalesOrdersByStatus(String tenantId, String status) {
        log.info("Getting sales orders by status: {} for tenant: {}", status, tenantId);
        return salesOrderRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get sales orders by customer
     */
    public List<SalesOrderEntity> getSalesOrdersByCustomer(String tenantId, Long customerId) {
        log.info("Getting sales orders by customer: {} for tenant: {}", customerId, tenantId);
        return salesOrderRepository.findByTenantIdAndCustomerId(tenantId, customerId);
    }

    /**
     * Get sales orders by date range
     */
    public List<SalesOrderEntity> getSalesOrdersByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting sales orders by date range: {} to {} for tenant: {}", startDate, endDate, tenantId);
        return salesOrderRepository.findByTenantIdAndOrderDateBetween(tenantId, startDate, endDate);
    }

    /**
     * Create sales order
     */
    @Transactional
    public SalesOrderEntity createSalesOrder(SalesOrderEntity salesOrder) {
        log.info("Creating sales order: {}", salesOrder.getOrderNo());

        // Check duplicate
        if (salesOrderRepository.existsByTenant_TenantIdAndOrderNo(
                salesOrder.getTenant().getTenantId(), salesOrder.getOrderNo())) {
            throw new IllegalArgumentException("Sales order already exists: " + salesOrder.getOrderNo());
        }

        // Validate tenant
        TenantEntity tenant = tenantRepository.findById(salesOrder.getTenant().getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + salesOrder.getTenant().getTenantId()));
        salesOrder.setTenant(tenant);

        // Validate customer
        CustomerEntity customer = customerRepository.findById(salesOrder.getCustomer().getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + salesOrder.getCustomer().getCustomerId()));
        salesOrder.setCustomer(customer);

        // Validate sales user
        UserEntity salesUser = userRepository.findById(salesOrder.getSalesUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Sales user not found: " + salesOrder.getSalesUser().getUserId()));
        salesOrder.setSalesUser(salesUser);

        // Set default status
        if (salesOrder.getStatus() == null) {
            salesOrder.setStatus("DRAFT");
        }

        // Process items
        if (salesOrder.getItems() != null && !salesOrder.getItems().isEmpty()) {
            for (SalesOrderItemEntity item : salesOrder.getItems()) {
                // Validate product or material
                if (item.getProduct() != null) {
                    ProductEntity product = productRepository.findById(item.getProduct().getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getProductId()));
                    item.setProduct(product);
                } else if (item.getMaterial() != null) {
                    MaterialEntity material = materialRepository.findById(item.getMaterial().getMaterialId())
                            .orElseThrow(() -> new IllegalArgumentException("Material not found: " + item.getMaterial().getMaterialId()));
                    item.setMaterial(material);
                }

                // Calculate amount
                if (item.getUnitPrice() != null && item.getOrderedQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getOrderedQuantity()));
                }

                // Initialize delivered quantity
                if (item.getDeliveredQuantity() == null) {
                    item.setDeliveredQuantity(BigDecimal.ZERO);
                }

                item.setSalesOrder(salesOrder);
            }
        }

        // Calculate total amount
        calculateTotalAmount(salesOrder);

        SalesOrderEntity savedOrder = salesOrderRepository.save(salesOrder);
        log.info("Sales order created successfully: {}", savedOrder.getOrderNo());

        return getSalesOrderById(savedOrder.getSalesOrderId());
    }

    /**
     * Update sales order
     */
    @Transactional
    public SalesOrderEntity updateSalesOrder(Long salesOrderId, SalesOrderEntity updatedSalesOrder) {
        log.info("Updating sales order ID: {}", salesOrderId);

        SalesOrderEntity existingOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found with ID: " + salesOrderId));

        // Cannot update if already delivered
        if ("DELIVERED".equals(existingOrder.getStatus())) {
            throw new IllegalArgumentException("Cannot update delivered sales order");
        }

        // Update fields
        existingOrder.setOrderDate(updatedSalesOrder.getOrderDate());
        existingOrder.setRequestedDeliveryDate(updatedSalesOrder.getRequestedDeliveryDate());
        existingOrder.setDeliveryAddress(updatedSalesOrder.getDeliveryAddress());
        existingOrder.setPaymentTerms(updatedSalesOrder.getPaymentTerms());
        existingOrder.setCurrency(updatedSalesOrder.getCurrency());
        existingOrder.setRemarks(updatedSalesOrder.getRemarks());

        // Update customer if changed
        if (updatedSalesOrder.getCustomer() != null &&
            !existingOrder.getCustomer().getCustomerId().equals(updatedSalesOrder.getCustomer().getCustomerId())) {
            CustomerEntity customer = customerRepository.findById(updatedSalesOrder.getCustomer().getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            existingOrder.setCustomer(customer);
        }

        // Update items if provided
        if (updatedSalesOrder.getItems() != null) {
            // Clear existing items
            existingOrder.getItems().clear();

            // Add updated items
            for (SalesOrderItemEntity item : updatedSalesOrder.getItems()) {
                // Validate product or material
                if (item.getProduct() != null) {
                    ProductEntity product = productRepository.findById(item.getProduct().getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                    item.setProduct(product);
                } else if (item.getMaterial() != null) {
                    MaterialEntity material = materialRepository.findById(item.getMaterial().getMaterialId())
                            .orElseThrow(() -> new IllegalArgumentException("Material not found"));
                    item.setMaterial(material);
                }

                // Calculate amount
                if (item.getUnitPrice() != null && item.getOrderedQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getOrderedQuantity()));
                }

                // Initialize delivered quantity if null
                if (item.getDeliveredQuantity() == null) {
                    item.setDeliveredQuantity(BigDecimal.ZERO);
                }

                existingOrder.addItem(item);
            }
        }

        // Recalculate total amount
        calculateTotalAmount(existingOrder);

        salesOrderRepository.save(existingOrder);
        log.info("Sales order updated successfully: {}", existingOrder.getOrderNo());

        return getSalesOrderById(salesOrderId);
    }

    /**
     * Delete sales order
     */
    @Transactional
    public void deleteSalesOrder(Long salesOrderId) {
        log.info("Deleting sales order ID: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found with ID: " + salesOrderId));

        // Cannot delete if already confirmed or delivered
        if (!"DRAFT".equals(salesOrder.getStatus())) {
            throw new IllegalArgumentException("Cannot delete sales order with status: " + salesOrder.getStatus());
        }

        salesOrderRepository.delete(salesOrder);
        log.info("Sales order deleted successfully: {}", salesOrder.getOrderNo());
    }

    /**
     * Confirm sales order
     */
    @Transactional
    public SalesOrderEntity confirmSalesOrder(Long salesOrderId) {
        log.info("Confirming sales order ID: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found with ID: " + salesOrderId));

        if (!"DRAFT".equals(salesOrder.getStatus())) {
            throw new IllegalArgumentException("Only DRAFT sales orders can be confirmed");
        }

        salesOrder.setStatus("CONFIRMED");
        salesOrderRepository.save(salesOrder);

        log.info("Sales order confirmed successfully: {}", salesOrder.getOrderNo());
        return getSalesOrderById(salesOrderId);
    }

    /**
     * Cancel sales order
     */
    @Transactional
    public SalesOrderEntity cancelSalesOrder(Long salesOrderId) {
        log.info("Cancelling sales order ID: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found with ID: " + salesOrderId));

        if ("DELIVERED".equals(salesOrder.getStatus()) || "CANCELLED".equals(salesOrder.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel sales order with status: " + salesOrder.getStatus());
        }

        salesOrder.setStatus("CANCELLED");
        salesOrderRepository.save(salesOrder);

        log.info("Sales order cancelled successfully: {}", salesOrder.getOrderNo());
        return getSalesOrderById(salesOrderId);
    }

    /**
     * Update sales order status based on delivery progress (called by DeliveryService)
     */
    @Transactional
    public void updateSalesOrderStatus(Long salesOrderId) {
        log.info("Updating sales order status based on delivery progress: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found with ID: " + salesOrderId));

        // Check if all items are fully delivered
        boolean allFullyDelivered = true;
        boolean anyPartiallyDelivered = false;

        for (SalesOrderItemEntity item : salesOrder.getItems()) {
            BigDecimal ordered = item.getOrderedQuantity();
            BigDecimal delivered = item.getDeliveredQuantity() != null ? item.getDeliveredQuantity() : BigDecimal.ZERO;

            if (delivered.compareTo(ordered) < 0) {
                allFullyDelivered = false;
            }
            if (delivered.compareTo(BigDecimal.ZERO) > 0) {
                anyPartiallyDelivered = true;
            }
        }

        // Update status
        String newStatus;
        if (allFullyDelivered) {
            newStatus = "DELIVERED";
        } else if (anyPartiallyDelivered) {
            newStatus = "PARTIALLY_DELIVERED";
        } else {
            newStatus = "CONFIRMED";
        }

        if (!newStatus.equals(salesOrder.getStatus())) {
            salesOrder.setStatus(newStatus);
            salesOrderRepository.save(salesOrder);
            log.info("Sales order status updated to: {}", newStatus);
        }
    }

    /**
     * Calculate total amount
     */
    private void calculateTotalAmount(SalesOrderEntity salesOrder) {
        if (salesOrder.getItems() == null || salesOrder.getItems().isEmpty()) {
            salesOrder.setTotalAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal totalAmount = salesOrder.getItems().stream()
                .map(SalesOrderItemEntity::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        salesOrder.setTotalAmount(totalAmount);
    }

    // ================== Inventory Integration ==================

    /**
     * Check inventory availability for sales order
     * 판매 주문에 대한 재고 가용성 확인
     *
     * @param salesOrderId Sales order ID
     * @param warehouseId Warehouse ID to check
     * @return Map of product ID to availability info
     */
    public Map<Long, InventoryAvailability> checkInventoryAvailability(
            Long salesOrderId,
            Long warehouseId) {

        SalesOrderEntity salesOrder = getSalesOrderById(salesOrderId);
        Map<Long, InventoryAvailability> availabilityMap = new HashMap<>();

        for (SalesOrderItemEntity item : salesOrder.getItems()) {
            if (item.getProduct() == null) {
                continue; // Skip non-product items
            }

            Long productId = item.getProduct().getProductId();
            BigDecimal orderedQuantity = item.getOrderedQuantity();

            // Get current inventory
            List<InventoryEntity> inventories = inventoryRepository
                .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
                    salesOrder.getTenant().getTenantId(),
                    warehouseId,
                    productId
                );

            BigDecimal totalAvailable = inventories.stream()
                .map(InventoryEntity::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            InventoryAvailability availability = InventoryAvailability.builder()
                .product(item.getProduct())
                .orderedQuantity(orderedQuantity)
                .availableQuantity(totalAvailable)
                .isAvailable(totalAvailable.compareTo(orderedQuantity) >= 0)
                .shortfall(totalAvailable.compareTo(orderedQuantity) < 0 ?
                    orderedQuantity.subtract(totalAvailable) : BigDecimal.ZERO)
                .build();

            availabilityMap.put(productId, availability);

            log.info("Product {}: ordered={}, available={}, isAvailable={}",
                item.getProduct().getProductCode(),
                orderedQuantity,
                totalAvailable,
                availability.isAvailable());
        }

        return availabilityMap;
    }

    /**
     * Reserve inventory for confirmed sales order
     * 확정된 판매 주문에 대한 재고 예약
     *
     * @param salesOrderId Sales order ID
     * @param warehouseId Warehouse ID
     * @return List of reserved inventory IDs
     */
    @Transactional
    public List<Long> reserveInventoryForOrder(Long salesOrderId, Long warehouseId) {
        SalesOrderEntity salesOrder = getSalesOrderById(salesOrderId);

        if (!"CONFIRMED".equals(salesOrder.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // Check availability first
        Map<Long, InventoryAvailability> availability =
            checkInventoryAvailability(salesOrderId, warehouseId);

        // Verify all items are available
        List<String> unavailableProducts = availability.values().stream()
            .filter(a -> !a.isAvailable())
            .map(a -> a.getProduct().getProductCode())
            .collect(Collectors.toList());

        if (!unavailableProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
        }

        // Reserve inventory for each item
        List<Long> reservedIds = new ArrayList<>();

        for (SalesOrderItemEntity item : salesOrder.getItems()) {
            if (item.getProduct() == null) {
                continue;
            }

            try {
                InventoryEntity reserved = inventoryService.reserveInventory(
                    salesOrder.getTenant().getTenantId(),
                    warehouseId,
                    item.getProduct().getProductId(),
                    null, // LOT ID (FIFO will be applied)
                    item.getOrderedQuantity()
                );

                reservedIds.add(reserved.getInventoryId());

                log.info("Reserved {} of {} for sales order {}",
                    item.getOrderedQuantity(),
                    item.getProduct().getProductCode(),
                    salesOrder.getOrderNo());

            } catch (Exception e) {
                log.error("Failed to reserve inventory for sales order {}: {}",
                    salesOrder.getOrderNo(), e.getMessage());
                throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
            }
        }

        log.info("Reserved all inventory for sales order {}: {} items",
            salesOrder.getOrderNo(), reservedIds.size());

        return reservedIds;
    }

    /**
     * Release reserved inventory (cancel order)
     * 예약된 재고 해제 (주문 취소 시)
     *
     * @param salesOrderId Sales order ID
     * @param warehouseId Warehouse ID
     */
    @Transactional
    public void releaseReservedInventory(Long salesOrderId, Long warehouseId) {
        SalesOrderEntity salesOrder = getSalesOrderById(salesOrderId);

        for (SalesOrderItemEntity item : salesOrder.getItems()) {
            if (item.getProduct() == null) {
                continue;
            }

            try {
                inventoryService.releaseReservedInventory(
                    salesOrder.getTenant().getTenantId(),
                    warehouseId,
                    item.getProduct().getProductId(),
                    null, // LOT ID
                    item.getOrderedQuantity()
                );

                log.info("Released {} of {} for cancelled sales order {}",
                    item.getOrderedQuantity(),
                    item.getProduct().getProductCode(),
                    salesOrder.getOrderNo());

            } catch (Exception e) {
                log.error("Failed to release inventory: {}", e.getMessage());
                // Continue with other items
            }
        }
    }

    /**
     * Create shipping from sales order
     * 판매 주문으로부터 출하 자동 생성
     *
     * @param salesOrderId Sales order ID
     * @param warehouseId Source warehouse ID
     * @param shipperUserId Shipper user ID
     * @return Created shipping entity
     */
    @Transactional
    public ShippingEntity createShippingFromOrder(
            Long salesOrderId,
            Long warehouseId,
            Long shipperUserId) {

        SalesOrderEntity salesOrder = getSalesOrderById(salesOrderId);

        if (!"CONFIRMED".equals(salesOrder.getStatus()) &&
            !"PARTIALLY_DELIVERED".equals(salesOrder.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        WarehouseEntity warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        UserEntity shipper = userRepository.findById(shipperUserId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        // Generate shipping number
        String shippingNo = generateShippingNo(salesOrder.getTenant().getTenantId());

        // Create shipping header
        ShippingEntity shipping = ShippingEntity.builder()
            .tenant(salesOrder.getTenant())
            .shippingNo(shippingNo)
            .shippingDate(LocalDateTime.now())
            .salesOrder(salesOrder)
            .customer(salesOrder.getCustomer())
            .warehouse(warehouse)
            .shipper(shipper)
            .shippingAddress(salesOrder.getDeliveryAddress())
            .shippingStatus("PENDING")
            .isActive(true)
            .build();

        // Create shipping items from sales order items
        for (SalesOrderItemEntity orderItem : salesOrder.getItems()) {
            // Skip if already fully delivered
            BigDecimal remaining = orderItem.getOrderedQuantity()
                .subtract(orderItem.getDeliveredQuantity() != null ?
                    orderItem.getDeliveredQuantity() : BigDecimal.ZERO);

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            ShippingItemEntity shippingItem = ShippingItemEntity.builder()
                .shipping(shipping)
                .salesOrderItem(orderItem)
                .product(orderItem.getProduct())
                .productCode(orderItem.getProduct() != null ?
                    orderItem.getProduct().getProductCode() : null)
                .productName(orderItem.getProduct() != null ?
                    orderItem.getProduct().getProductName() : null)
                .shippedQuantity(remaining)
                .unit(orderItem.getProduct() != null ?
                    orderItem.getProduct().getUnit() : null)
                .inspectionStatus("PENDING")
                .build();

            shipping.addItem(shippingItem);
        }

        ShippingEntity created = shippingRepository.save(shipping);

        log.info("Created shipping {} from sales order {} with {} items",
            created.getShippingNo(),
            salesOrder.getOrderNo(),
            created.getItems().size());

        return created;
    }

    /**
     * Confirm sales order with inventory check and reservation
     * 재고 확인 및 예약과 함께 판매 주문 확정
     *
     * @param salesOrderId Sales order ID
     * @param warehouseId Warehouse ID for inventory
     * @return Confirmed sales order
     */
    @Transactional
    public SalesOrderEntity confirmOrderWithInventory(Long salesOrderId, Long warehouseId) {
        log.info("Confirming sales order {} with inventory check", salesOrderId);

        // 1. Check inventory availability
        Map<Long, InventoryAvailability> availability =
            checkInventoryAvailability(salesOrderId, warehouseId);

        // 2. Verify all items are available
        List<String> unavailableProducts = availability.values().stream()
            .filter(a -> !a.isAvailable())
            .map(a -> String.format("%s (short: %s)",
                a.getProduct().getProductCode(),
                a.getShortfall()))
            .collect(Collectors.toList());

        if (!unavailableProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY,
                "Unavailable products: " + String.join(", ", unavailableProducts));
        }

        // 3. Confirm order
        SalesOrderEntity confirmed = confirmSalesOrder(salesOrderId);

        // 4. Reserve inventory
        reserveInventoryForOrder(salesOrderId, warehouseId);

        log.info("Sales order {} confirmed with inventory reserved", confirmed.getOrderNo());
        return confirmed;
    }

    /**
     * Cancel sales order with inventory release
     * 재고 해제와 함께 판매 주문 취소
     *
     * @param salesOrderId Sales order ID
     * @param warehouseId Warehouse ID
     * @return Cancelled sales order
     */
    @Transactional
    public SalesOrderEntity cancelOrderWithInventory(Long salesOrderId, Long warehouseId) {
        log.info("Cancelling sales order {} with inventory release", salesOrderId);

        // 1. Release reserved inventory
        releaseReservedInventory(salesOrderId, warehouseId);

        // 2. Cancel order
        SalesOrderEntity cancelled = cancelSalesOrder(salesOrderId);

        log.info("Sales order {} cancelled with inventory released", cancelled.getOrderNo());
        return cancelled;
    }

    /**
     * Complete sales order
     * 판매 주문 완료
     *
     * @param salesOrderId Sales order ID
     * @return Completed sales order
     */
    @Transactional
    public SalesOrderEntity completeOrder(Long salesOrderId) {
        SalesOrderEntity salesOrder = getSalesOrderById(salesOrderId);

        // Verify all items are delivered
        for (SalesOrderItemEntity item : salesOrder.getItems()) {
            BigDecimal ordered = item.getOrderedQuantity();
            BigDecimal delivered = item.getDeliveredQuantity() != null ?
                item.getDeliveredQuantity() : BigDecimal.ZERO;

            if (delivered.compareTo(ordered) < 0) {
                throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cannot complete order: not all items delivered");
            }
        }

        salesOrder.setStatus("COMPLETED");
        salesOrderRepository.save(salesOrder);

        log.info("Sales order {} completed", salesOrder.getOrderNo());
        return salesOrder;
    }

    /**
     * Generate shipping number: SH-YYYYMMDD-0001
     */
    private String generateShippingNo(String tenantId) {
        String datePrefix = "SH-" + LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Simplified - actual implementation should query existing shippings
        return datePrefix + "-" + String.format("%04d", (int)(Math.random() * 9999) + 1);
    }

    // ================== Inner Classes ==================

    /**
     * Inventory Availability DTO
     * 재고 가용성 정보
     */
    @lombok.Data
    @lombok.Builder
    public static class InventoryAvailability {
        private ProductEntity product;
        private BigDecimal orderedQuantity;
        private BigDecimal availableQuantity;
        private boolean isAvailable;
        private BigDecimal shortfall;
    }
}
