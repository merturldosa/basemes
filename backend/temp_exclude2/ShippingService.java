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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shipping Service
 * 출하 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final TenantRepository tenantRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final QualityStandardRepository qualityStandardRepository;
    private final LotRepository lotRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final SalesOrderService salesOrderService;

    /**
     * Get all shippings by tenant
     */
    public List<ShippingEntity> getAllShippings(String tenantId) {
        log.info("Getting all shippings for tenant: {}", tenantId);
        return shippingRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get shipping by ID
     */
    public ShippingEntity getShippingById(Long shippingId) {
        log.info("Getting shipping by ID: {}", shippingId);
        return shippingRepository.findByIdWithAllRelations(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found: " + shippingId));
    }

    /**
     * Get shippings by status
     */
    public List<ShippingEntity> getShippingsByStatus(String tenantId, String status) {
        log.info("Getting shippings by status {} for tenant: {}", status, tenantId);
        return shippingRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get shippings by sales order
     */
    public List<ShippingEntity> getShippingsBySalesOrder(String tenantId, Long salesOrderId) {
        log.info("Getting shippings by sales order {} for tenant: {}", salesOrderId, tenantId);
        return shippingRepository.findByTenantIdAndSalesOrderId(tenantId, salesOrderId);
    }

    /**
     * Get shippings by warehouse
     */
    public List<ShippingEntity> getShippingsByWarehouse(String tenantId, Long warehouseId) {
        log.info("Getting shippings by warehouse {} for tenant: {}", warehouseId, tenantId);
        return shippingRepository.findByTenantIdAndWarehouseId(tenantId, warehouseId);
    }

    /**
     * Get shippings by date range
     */
    public List<ShippingEntity> getShippingsByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting shippings by date range: {} to {} for tenant: {}", startDate, endDate, tenantId);
        return shippingRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Create shipping
     */
    @Transactional
    public ShippingEntity createShipping(ShippingEntity shipping) {
        log.info("Creating shipping: {}", shipping.getShippingNo());

        // Check duplicate
        if (shippingRepository.existsByTenant_TenantIdAndShippingNo(
                shipping.getTenant().getTenantId(), shipping.getShippingNo())) {
            throw new IllegalArgumentException("Shipping already exists: " + shipping.getShippingNo());
        }

        // Validate tenant
        TenantEntity tenant = tenantRepository.findById(shipping.getTenant().getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + shipping.getTenant().getTenantId()));
        shipping.setTenant(tenant);

        // Validate sales order
        if (shipping.getSalesOrder() != null && shipping.getSalesOrder().getSalesOrderId() != null) {
            SalesOrderEntity salesOrder = salesOrderRepository.findById(shipping.getSalesOrder().getSalesOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + shipping.getSalesOrder().getSalesOrderId()));
            shipping.setSalesOrder(salesOrder);
        }

        // Validate customer
        if (shipping.getCustomer() != null && shipping.getCustomer().getCustomerId() != null) {
            CustomerEntity customer = customerRepository.findById(shipping.getCustomer().getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + shipping.getCustomer().getCustomerId()));
            shipping.setCustomer(customer);
        }

        // Validate warehouse
        WarehouseEntity warehouse = warehouseRepository.findById(shipping.getWarehouse().getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + shipping.getWarehouse().getWarehouseId()));
        shipping.setWarehouse(warehouse);

        // Validate shipper
        if (shipping.getShipper() != null && shipping.getShipper().getUserId() != null) {
            UserEntity shipper = userRepository.findById(shipping.getShipper().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Shipper not found: " + shipping.getShipper().getUserId()));
            shipping.setShipper(shipper);
        }

        // Set default values
        if (shipping.getShippingStatus() == null) {
            shipping.setShippingStatus("PENDING");
        }
        if (shipping.getIsActive() == null) {
            shipping.setIsActive(true);
        }

        // Process items
        if (shipping.getItems() != null) {
            for (ShippingItemEntity item : shipping.getItems()) {
                // Set product
                if (item.getProduct() != null && item.getProduct().getProductId() != null) {
                    ProductEntity product = productRepository.findById(item.getProduct().getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getProductId()));
                    item.setProduct(product);
                    item.setProductCode(product.getProductCode());
                    item.setProductName(product.getProductName());
                }

                // Set default inspection status
                if (item.getInspectionStatus() == null) {
                    item.setInspectionStatus("NOT_REQUIRED");
                }

                // Create OQC request if inspection is required
                if ("PENDING".equals(item.getInspectionStatus())) {
                    shipping.setShippingStatus("INSPECTING");
                }
            }
        }

        ShippingEntity saved = shippingRepository.save(shipping);

        // Process OQC requests for items requiring inspection
        if (shipping.getItems() != null) {
            for (ShippingItemEntity item : shipping.getItems()) {
                if ("PENDING".equals(item.getInspectionStatus())) {
                    createOQCRequest(saved, item);
                }
            }
        }

        log.info("Shipping created successfully: {}", saved.getShippingId());

        return shippingRepository.findByIdWithAllRelations(saved.getShippingId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve created shipping"));
    }

    /**
     * Update shipping
     */
    @Transactional
    public ShippingEntity updateShipping(Long shippingId, ShippingEntity shipping) {
        log.info("Updating shipping: {}", shippingId);

        ShippingEntity existing = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found: " + shippingId));

        // Update fields
        existing.setShippingDate(shipping.getShippingDate());
        existing.setShippingType(shipping.getShippingType());
        existing.setShippingStatus(shipping.getShippingStatus());
        existing.setTotalQuantity(shipping.getTotalQuantity());
        existing.setTotalAmount(shipping.getTotalAmount());
        existing.setShipperName(shipping.getShipperName());
        existing.setDeliveryAddress(shipping.getDeliveryAddress());
        existing.setTrackingNumber(shipping.getTrackingNumber());
        existing.setCarrierName(shipping.getCarrierName());
        existing.setRemarks(shipping.getRemarks());

        ShippingEntity saved = shippingRepository.save(existing);
        log.info("Shipping updated successfully: {}", saved.getShippingId());

        return shippingRepository.findByIdWithAllRelations(saved.getShippingId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve updated shipping"));
    }

    /**
     * Delete shipping
     */
    @Transactional
    public void deleteShipping(Long shippingId) {
        log.info("Deleting shipping: {}", shippingId);

        ShippingEntity shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found: " + shippingId));

        if ("SHIPPED".equals(shipping.getShippingStatus())) {
            throw new IllegalArgumentException("Cannot delete shipped shipping");
        }

        shippingRepository.deleteById(shippingId);
        log.info("Shipping deleted successfully: {}", shippingId);
    }

    /**
     * Complete shipping (mark as shipped)
     */
    @Transactional
    public ShippingEntity completeShipping(Long shippingId) {
        log.info("Completing shipping: {}", shippingId);

        ShippingEntity shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found: " + shippingId));

        if ("SHIPPED".equals(shipping.getShippingStatus())) {
            throw new IllegalArgumentException("Shipping already completed");
        }

        shipping.setShippingStatus("SHIPPED");

        ShippingEntity saved = shippingRepository.save(shipping);
        log.info("Shipping completed successfully: {}", saved.getShippingId());

        return shippingRepository.findByIdWithAllRelations(saved.getShippingId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve completed shipping"));
    }

    // ================== Inventory Integration Methods ==================

    /**
     * Process shipping with inventory deduction and LOT selection
     * 출하 처리 및 재고 차감
     *
     * @param shippingId Shipping ID
     * @return Processed shipping
     */
    @Transactional
    public ShippingEntity processShipping(Long shippingId) {
        log.info("Processing shipping: {}", shippingId);

        ShippingEntity shipping = shippingRepository.findByIdWithAllRelations(shippingId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHIPPING_NOT_FOUND));

        if (!"PENDING".equals(shipping.getShippingStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // Process each item
        for (ShippingItemEntity item : shipping.getItems()) {
            // Select LOTs using FIFO
            List<LotAllocation> allocations = selectLotsFIFO(
                shipping.getTenant().getTenantId(),
                shipping.getWarehouse().getWarehouseId(),
                item.getProduct().getProductId(),
                item.getShippedQuantity()
            );

            if (allocations.isEmpty()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
            }

            // Create inventory transactions for each LOT allocation
            for (LotAllocation allocation : allocations) {
                createShippingTransaction(shipping, item, allocation);

                // Deduct inventory
                deductInventory(
                    shipping.getTenant().getTenantId(),
                    shipping.getWarehouse().getWarehouseId(),
                    item.getProduct().getProductId(),
                    allocation.getLot().getLotId(),
                    allocation.getAllocatedQuantity()
                );
            }

            // Set primary LOT for item (first allocation)
            if (!allocations.isEmpty()) {
                item.setLot(allocations.get(0).getLot());
                item.setLotNo(allocations.get(0).getLot().getLotNo());
            }

            log.info("Processed shipping item: product={}, quantity={}, lots={}",
                item.getProductCode(),
                item.getShippedQuantity(),
                allocations.size());
        }

        // Update shipping status
        shipping.setShippingStatus("PROCESSING");

        ShippingEntity saved = shippingRepository.save(shipping);
        log.info("Shipping processed successfully: {}", shipping.getShippingNo());

        return saved;
    }

    /**
     * Select LOTs using FIFO (First-In-First-Out) algorithm
     * FIFO LOT 선택 알고리즘
     *
     * @param tenantId Tenant ID
     * @param warehouseId Warehouse ID
     * @param productId Product ID
     * @param requiredQuantity Required quantity
     * @return List of LOT allocations
     */
    private List<LotAllocation> selectLotsFIFO(
            String tenantId,
            Long warehouseId,
            Long productId,
            BigDecimal requiredQuantity) {

        log.info("Selecting LOTs via FIFO: product={}, warehouse={}, qty={}",
            productId, warehouseId, requiredQuantity);

        // Find all available inventory for this product in warehouse
        List<InventoryEntity> inventories = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
                tenantId, warehouseId, productId);

        // Filter: only LOTs with PASSED quality status and available quantity > 0
        List<InventoryEntity> availableInventories = inventories.stream()
            .filter(inv -> inv.getLot() != null)
            .filter(inv -> "PASSED".equals(inv.getLot().getQualityStatus()))
            .filter(inv -> inv.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(inv -> inv.getLot().getProductionDate())) // FIFO: oldest first
            .collect(Collectors.toList());

        if (availableInventories.isEmpty()) {
            log.error("No available inventory found for product: {} in warehouse: {}",
                productId, warehouseId);
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
        }

        // Allocate from oldest LOTs first
        List<LotAllocation> allocations = new ArrayList<>();
        BigDecimal remaining = requiredQuantity;

        for (InventoryEntity inventory : availableInventories) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal availableQty = inventory.getAvailableQuantity();
            BigDecimal allocatedQty = remaining.min(availableQty);

            LotAllocation allocation = LotAllocation.builder()
                .lot(inventory.getLot())
                .inventory(inventory)
                .allocatedQuantity(allocatedQty)
                .build();

            allocations.add(allocation);
            remaining = remaining.subtract(allocatedQty);

            log.info("Allocated LOT: {}, quantity: {}", inventory.getLot().getLotNo(), allocatedQty);
        }

        // Check if full quantity could be allocated
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            log.error("Insufficient inventory: required={}, available={}",
                requiredQuantity, requiredQuantity.subtract(remaining));
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
        }

        log.info("FIFO selection complete: {} lots allocated for total quantity {}",
            allocations.size(), requiredQuantity);

        return allocations;
    }

    /**
     * Confirm shipping and update sales order
     * 출하 확정 및 판매 주문 업데이트
     *
     * @param shippingId Shipping ID
     * @return Confirmed shipping
     */
    @Transactional
    public ShippingEntity confirmShipping(Long shippingId) {
        log.info("Confirming shipping: {}", shippingId);

        ShippingEntity shipping = shippingRepository.findByIdWithAllRelations(shippingId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHIPPING_NOT_FOUND));

        if (!"PROCESSING".equals(shipping.getShippingStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // Check OQC inspection results (if required)
        boolean allInspectionsPassed = true;
        for (ShippingItemEntity item : shipping.getItems()) {
            if ("PENDING".equals(item.getInspectionStatus()) ||
                "INSPECTING".equals(item.getInspectionStatus())) {
                throw new BusinessException(ErrorCode.INSPECTION_NOT_COMPLETED);
            }

            if ("FAIL".equals(item.getInspectionStatus())) {
                allInspectionsPassed = false;
            }
        }

        if (!allInspectionsPassed) {
            throw new BusinessException(ErrorCode.INSPECTION_FAILED);
        }

        // Update shipping status
        shipping.setShippingStatus("SHIPPED");
        shipping.setActualShippingDate(LocalDateTime.now());

        ShippingEntity saved = shippingRepository.save(shipping);

        // Update sales order shipped quantities
        if (shipping.getSalesOrder() != null) {
            updateSalesOrderShippedQuantity(shipping);
        }

        log.info("Shipping confirmed successfully: {}", shipping.getShippingNo());

        return saved;
    }

    /**
     * Cancel shipping and restore inventory
     * 출하 취소 및 재고 복원
     *
     * @param shippingId Shipping ID
     * @param reason Cancellation reason
     * @return Cancelled shipping
     */
    @Transactional
    public ShippingEntity cancelShipping(Long shippingId, String reason) {
        log.info("Cancelling shipping: {}, reason: {}", shippingId, reason);

        ShippingEntity shipping = shippingRepository.findByIdWithAllRelations(shippingId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHIPPING_NOT_FOUND));

        if ("SHIPPED".equals(shipping.getShippingStatus())) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_SHIPPED);
        }

        // Restore inventory if it was already deducted
        if ("PROCESSING".equals(shipping.getShippingStatus())) {
            restoreInventory(shipping);
        }

        // Update shipping status
        shipping.setShippingStatus("CANCELLED");
        shipping.setRemarks((shipping.getRemarks() != null ? shipping.getRemarks() + " | " : "") +
            "Cancelled: " + reason);

        ShippingEntity saved = shippingRepository.save(shipping);

        // Release reserved inventory from sales order if applicable
        if (shipping.getSalesOrder() != null) {
            salesOrderService.releaseReservedInventory(
                shipping.getSalesOrder().getSalesOrderId(),
                shipping.getWarehouse().getWarehouseId()
            );
        }

        log.info("Shipping cancelled successfully: {}", shipping.getShippingNo());

        return saved;
    }

    // ================== Private Helper Methods ==================

    /**
     * Create inventory transaction for shipping
     * 출하 재고 트랜잭션 생성
     */
    private void createShippingTransaction(
            ShippingEntity shipping,
            ShippingItemEntity item,
            LotAllocation allocation) {

        String transactionNo = String.format("SHIP-%s-%s-%03d",
            shipping.getShippingNo(),
            allocation.getLot().getLotNo(),
            item.getShippingItemId() != null ? item.getShippingItemId() : 0);

        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
            .tenant(shipping.getTenant())
            .transactionNo(transactionNo)
            .transactionDate(LocalDateTime.now())
            .transactionType("OUT_SHIPPING")
            .warehouse(shipping.getWarehouse())
            .product(item.getProduct())
            .lot(allocation.getLot())
            .quantity(allocation.getAllocatedQuantity().negate()) // Negative for outbound
            .unit(item.getProduct().getUnit())
            .transactionUser(shipping.getShipper())
            .approvalStatus("APPROVED")
            .referenceNo(shipping.getShippingNo())
            .remarks("Shipping to customer: " + shipping.getCustomer().getCustomerName())
            .build();

        inventoryTransactionRepository.save(transaction);

        log.info("Created shipping transaction: {}", transactionNo);
    }

    /**
     * Deduct inventory quantity
     * 재고 차감
     */
    private void deductInventory(
            String tenantId,
            Long warehouseId,
            Long productId,
            Long lotId,
            BigDecimal quantity) {

        InventoryEntity inventory = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.INVENTORY_NOT_FOUND));

        if (inventory.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
        }

        // Deduct from available quantity
        inventory.setAvailableQuantity(
            inventory.getAvailableQuantity().subtract(quantity)
        );

        // Update last transaction info
        inventory.setLastTransactionDate(LocalDateTime.now());
        inventory.setLastTransactionType("OUT_SHIPPING");

        inventoryRepository.save(inventory);

        log.info("Deducted inventory: product={}, lot={}, quantity={}",
            productId, lotId, quantity);
    }

    /**
     * Update sales order shipped quantities
     * 판매 주문 출하 수량 업데이트
     */
    private void updateSalesOrderShippedQuantity(ShippingEntity shipping) {
        SalesOrderEntity salesOrder = shipping.getSalesOrder();

        if (salesOrder == null || salesOrder.getItems() == null) {
            return;
        }

        // Update shipped quantity for each order item
        for (ShippingItemEntity shippingItem : shipping.getItems()) {
            // Find matching sales order item
            salesOrder.getItems().stream()
                .filter(orderItem -> orderItem.getProduct().getProductId()
                    .equals(shippingItem.getProduct().getProductId()))
                .findFirst()
                .ifPresent(orderItem -> {
                    BigDecimal currentShipped = orderItem.getShippedQuantity() != null ?
                        orderItem.getShippedQuantity() : BigDecimal.ZERO;

                    orderItem.setShippedQuantity(
                        currentShipped.add(shippingItem.getShippedQuantity())
                    );

                    log.info("Updated sales order item shipped quantity: product={}, shipped={}",
                        orderItem.getProductCode(), orderItem.getShippedQuantity());
                });
        }

        // Update sales order status based on shipped quantities
        boolean fullyShipped = salesOrder.getItems().stream()
            .allMatch(item -> item.getShippedQuantity() != null &&
                item.getShippedQuantity().compareTo(item.getQuantity()) >= 0);

        boolean partiallyShipped = salesOrder.getItems().stream()
            .anyMatch(item -> item.getShippedQuantity() != null &&
                item.getShippedQuantity().compareTo(BigDecimal.ZERO) > 0);

        if (fullyShipped) {
            salesOrder.setOrderStatus("DELIVERED");
        } else if (partiallyShipped) {
            salesOrder.setOrderStatus("PARTIALLY_DELIVERED");
        }

        salesOrderRepository.save(salesOrder);

        log.info("Updated sales order: {}, status: {}",
            salesOrder.getOrderNo(), salesOrder.getOrderStatus());
    }

    /**
     * Restore inventory after shipping cancellation
     * 출하 취소 후 재고 복원
     */
    private void restoreInventory(ShippingEntity shipping) {
        log.info("Restoring inventory for cancelled shipping: {}", shipping.getShippingNo());

        // Find all OUT_SHIPPING transactions for this shipping
        List<InventoryTransactionEntity> transactions = inventoryTransactionRepository
            .findByTenant_TenantIdAndReferenceNo(
                shipping.getTenant().getTenantId(),
                shipping.getShippingNo()
            ).stream()
            .filter(t -> "OUT_SHIPPING".equals(t.getTransactionType()))
            .collect(Collectors.toList());

        // Create reversal transactions and restore inventory
        for (InventoryTransactionEntity originalTx : transactions) {
            // Create reversal transaction
            String reversalTxNo = "REV-" + originalTx.getTransactionNo();

            InventoryTransactionEntity reversalTx = InventoryTransactionEntity.builder()
                .tenant(originalTx.getTenant())
                .transactionNo(reversalTxNo)
                .transactionDate(LocalDateTime.now())
                .transactionType("IN_RETURN")
                .warehouse(originalTx.getWarehouse())
                .product(originalTx.getProduct())
                .lot(originalTx.getLot())
                .quantity(originalTx.getQuantity().negate()) // Reverse the negative quantity
                .unit(originalTx.getUnit())
                .transactionUser(shipping.getShipper())
                .approvalStatus("APPROVED")
                .referenceNo(shipping.getShippingNo())
                .remarks("Reversal for cancelled shipping")
                .build();

            inventoryTransactionRepository.save(reversalTx);

            // Restore inventory quantity
            InventoryEntity inventory = inventoryRepository
                .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                    originalTx.getTenant().getTenantId(),
                    originalTx.getWarehouse().getWarehouseId(),
                    originalTx.getProduct().getProductId(),
                    originalTx.getLot().getLotId()
                )
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.INVENTORY_NOT_FOUND));

            // Add back the quantity (originalTx.quantity is negative, so negate it)
            inventory.setAvailableQuantity(
                inventory.getAvailableQuantity().add(originalTx.getQuantity().negate())
            );

            inventory.setLastTransactionDate(LocalDateTime.now());
            inventory.setLastTransactionType("IN_RETURN");

            inventoryRepository.save(inventory);

            log.info("Restored inventory: product={}, lot={}, quantity={}",
                originalTx.getProduct().getProductId(),
                originalTx.getLot().getLotNo(),
                originalTx.getQuantity().negate());
        }

        log.info("Inventory restoration complete for shipping: {}", shipping.getShippingNo());
    }

    /**
     * Create OQC (Outgoing Quality Control) inspection request
     *
     * 출하 품질 검사 의뢰 생성:
     * - inspection_type: OUTGOING
     * - 검사 기준서 조회 (제품별)
     * - 검사 수량 = 출하 수량
     * - 검사자는 품질팀 기본 사용자로 설정
     */
    private void createOQCRequest(ShippingEntity shipping, ShippingItemEntity item) {
        log.info("Creating OQC request for product: {}, LOT: {}", item.getProductCode(), item.getLotNo());

        // Find quality standard for this product (OUTGOING type)
        List<QualityStandardEntity> allStandards = qualityStandardRepository
            .findByTenantAndProduct(shipping.getTenant(), item.getProduct());

        // Filter by inspection type and active status
        List<QualityStandardEntity> standards = allStandards.stream()
            .filter(s -> "OUTGOING".equals(s.getInspectionType()))
            .filter(QualityStandardEntity::getIsActive)
            .collect(java.util.stream.Collectors.toList());

        if (standards.isEmpty()) {
            log.warn("No OQC quality standard found for product: {}, skipping OQC request", item.getProductCode());
            return;
        }

        QualityStandardEntity standard = standards.get(0); // Use first active standard

        // Find quality inspector (default: shipper or system user)
        UserEntity inspector = shipping.getShipper() != null ?
            shipping.getShipper() :
            userRepository.findById(1L).orElseThrow(() ->
                new IllegalArgumentException("No inspector user found"));

        // Generate inspection number
        String inspectionNo = generateInspectionNo(shipping.getTenant().getTenantId(), "OQC");

        // Create quality inspection record
        QualityInspectionEntity inspection = QualityInspectionEntity.builder()
            .tenant(shipping.getTenant())
            .qualityStandard(standard)
            .product(item.getProduct())
            .inspectionNo(inspectionNo)
            .inspectionDate(LocalDateTime.now())
            .inspectionType("OUTGOING")
            .inspector(inspector)
            .inspectedQuantity(item.getShippedQuantity())
            .passedQuantity(BigDecimal.ZERO) // Will be updated after inspection
            .failedQuantity(BigDecimal.ZERO)
            .inspectionResult("CONDITIONAL") // Pending inspection
            .remarks("OQC request from shipping: " + shipping.getShippingNo())
            .build();

        QualityInspectionEntity savedInspection = qualityInspectionRepository.save(inspection);

        // Link inspection to shipping item
        item.setQualityInspection(savedInspection);

        log.info("Created OQC request: {} for shipping item: {}", inspectionNo, item.getProductCode());
    }

    /**
     * Generate sequential inspection number: IQC-YYYYMMDD-0001 or OQC-YYYYMMDD-0001
     */
    private String generateInspectionNo(String tenantId, String prefix) {
        String datePrefix = prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Find existing inspections with the same date prefix
        long count = qualityInspectionRepository.findByTenant_TenantId(tenantId).stream()
            .filter(qi -> qi.getInspectionNo() != null && qi.getInspectionNo().startsWith(datePrefix))
            .count();

        return String.format("%s-%04d", datePrefix, count + 1);
    }

    // ================== Inner Classes ==================

    /**
     * LOT Allocation DTO
     * LOT 할당 정보
     */
    @lombok.Data
    @lombok.Builder
    public static class LotAllocation {
        private LotEntity lot;
        private InventoryEntity inventory;
        private BigDecimal allocatedQuantity;
    }
}
