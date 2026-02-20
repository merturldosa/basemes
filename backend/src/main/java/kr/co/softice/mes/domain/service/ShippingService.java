package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
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
import java.util.List;
import java.util.Optional;

/**
 * Shipping Service
 * 출하 관리 서비스
 *
 * 핵심 기능:
 * - 출하 생성 및 관리
 * - 재고 차감 처리
 * - 판매 주문 배송 상태 업데이트
 * - OQC 검사 통합
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final TenantRepository tenantRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderService salesOrderService;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final LotRepository lotRepository;

    /**
     * Find all shippings by tenant
     */
    public List<ShippingEntity> findByTenant(String tenantId) {
        return shippingRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find shipping by ID
     */
    public Optional<ShippingEntity> findById(Long shippingId) {
        return shippingRepository.findByIdWithAllRelations(shippingId);
    }

    /**
     * Find shippings by status
     */
    public List<ShippingEntity> findByStatus(String tenantId, String status) {
        return shippingRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Find shippings by sales order
     */
    public List<ShippingEntity> findBySalesOrder(String tenantId, Long salesOrderId) {
        return shippingRepository.findByTenantIdAndSalesOrderId(tenantId, salesOrderId);
    }

    /**
     * Find shippings by warehouse
     */
    public List<ShippingEntity> findByWarehouse(String tenantId, Long warehouseId) {
        return shippingRepository.findByTenantIdAndWarehouseId(tenantId, warehouseId);
    }

    /**
     * Create new shipping
     *
     * 워크플로우:
     * 1. 출하 번호 자동 생성 (SH-YYYYMMDD-0001)
     * 2. 판매 주문 연결 (선택)
     * 3. 재고 가용성 확인
     * 4. 출하 생성 (PENDING 상태)
     */
    @Transactional
    public ShippingEntity createShipping(String tenantId, ShippingEntity shipping) {
        log.info("Creating shipping for tenant: {}, warehouse: {}",
            tenantId,
            shipping.getWarehouse().getWarehouseId());

        // 1. Resolve tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND, "Tenant not found: " + tenantId));
        shipping.setTenant(tenant);

        // 2. Generate shipping number if not provided
        if (shipping.getShippingNo() == null || shipping.getShippingNo().isEmpty()) {
            shipping.setShippingNo(generateShippingNo(tenantId));
        }

        // Check duplicate
        if (shippingRepository.existsByTenant_TenantIdAndShippingNo(tenantId, shipping.getShippingNo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Shipping number already exists: " + shipping.getShippingNo());
        }

        // 3. Resolve warehouse
        WarehouseEntity warehouse = warehouseRepository.findById(shipping.getWarehouse().getWarehouseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND, "Warehouse not found"));
        shipping.setWarehouse(warehouse);

        // 4. Resolve optional entities
        if (shipping.getSalesOrder() != null) {
            SalesOrderEntity salesOrder = salesOrderRepository.findById(shipping.getSalesOrder().getSalesOrderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ORDER_NOT_FOUND, "Sales order not found"));
            shipping.setSalesOrder(salesOrder);

            // Use sales order customer if not provided
            if (shipping.getCustomer() == null) {
                shipping.setCustomer(salesOrder.getCustomer());
            }
        }

        if (shipping.getCustomer() != null && shipping.getCustomer().getCustomerId() != null) {
            CustomerEntity customer = customerRepository.findById(shipping.getCustomer().getCustomerId())
                    .orElse(null);
            shipping.setCustomer(customer);
        }

        if (shipping.getShipper() != null && shipping.getShipper().getUserId() != null) {
            UserEntity shipper = userRepository.findById(shipping.getShipper().getUserId())
                    .orElse(null);
            shipping.setShipper(shipper);
            if (shipper != null) {
                shipping.setShipperName(shipper.getUsername());
            }
        }

        // 5. Set initial status
        if (shipping.getShippingStatus() == null || shipping.getShippingStatus().isEmpty()) {
            shipping.setShippingStatus("PENDING");
        }

        if (shipping.getIsActive() == null) {
            shipping.setIsActive(true);
        }

        // 6. Process items
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShippingItemEntity item : shipping.getItems()) {
            item.setShipping(shipping);

            // Resolve product
            ProductEntity product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found"));
            item.setProduct(product);
            item.setProductCode(product.getProductCode());
            item.setProductName(product.getProductName());

            // Check inventory availability
            checkInventoryAvailability(warehouse, product, item.getShippedQuantity());

            // Calculate line amount
            if (item.getUnitPrice() != null && item.getShippedQuantity() != null) {
                item.setLineAmount(item.getUnitPrice().multiply(item.getShippedQuantity()));
            }

            // Set default inspection status
            if (item.getInspectionStatus() == null) {
                item.setInspectionStatus("NOT_REQUIRED");
            }

            // Add to totals
            totalQuantity = totalQuantity.add(item.getShippedQuantity());
            if (item.getLineAmount() != null) {
                totalAmount = totalAmount.add(item.getLineAmount());
            }
        }

        shipping.setTotalQuantity(totalQuantity);
        shipping.setTotalAmount(totalAmount);

        // 7. Save shipping
        ShippingEntity saved = shippingRepository.save(shipping);

        log.info("Created shipping: {} with {} items, total quantity: {}",
            saved.getShippingNo(), saved.getItems().size(), saved.getTotalQuantity());

        return shippingRepository.findByIdWithAllRelations(saved.getShippingId())
                .orElse(saved);
    }

    /**
     * Update shipping
     *
     * Only PENDING shippings can be updated
     */
    @Transactional
    public ShippingEntity updateShipping(Long shippingId, ShippingEntity updates) {
        log.info("Updating shipping: {}", shippingId);

        ShippingEntity existing = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPING_NOT_FOUND, "Shipping not found: " + shippingId));

        // Only PENDING shippings can be updated
        if (!"PENDING".equals(existing.getShippingStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Only PENDING shippings can be updated: " + shippingId);
        }

        // Update fields
        if (updates.getShippingDate() != null) {
            existing.setShippingDate(updates.getShippingDate());
        }
        if (updates.getDeliveryAddress() != null) {
            existing.setDeliveryAddress(updates.getDeliveryAddress());
        }
        if (updates.getTrackingNumber() != null) {
            existing.setTrackingNumber(updates.getTrackingNumber());
        }
        if (updates.getCarrierName() != null) {
            existing.setCarrierName(updates.getCarrierName());
        }
        if (updates.getRemarks() != null) {
            existing.setRemarks(updates.getRemarks());
        }

        ShippingEntity saved = shippingRepository.save(existing);

        log.info("Updated shipping: {}", saved.getShippingNo());

        return shippingRepository.findByIdWithAllRelations(saved.getShippingId())
                .orElse(saved);
    }

    /**
     * Process shipping (execute shipping with inventory deduction)
     *
     * 워크플로우:
     * 1. 상태 검증 (PENDING만 처리 가능)
     * 2. OQC 검사 확인 (필요 시)
     * 3. 재고 차감 (OUT_SHIPPING 거래)
     * 4. 판매 주문 배송 수량 업데이트
     * 5. 상태 → SHIPPED
     */
    @Transactional
    public ShippingEntity processShipping(Long shippingId) {
        log.info("Processing shipping: {}", shippingId);

        ShippingEntity shipping = shippingRepository.findByIdWithAllRelations(shippingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPING_NOT_FOUND, "Shipping not found: " + shippingId));

        // Only PENDING shippings can be processed
        if (!"PENDING".equals(shipping.getShippingStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Only PENDING shippings can be processed: " + shippingId);
        }

        // Process each item
        for (ShippingItemEntity item : shipping.getItems()) {
            // Check OQC if required
            if ("PENDING".equals(item.getInspectionStatus())) {
                throw new BusinessException(ErrorCode.INSPECTION_NOT_COMPLETED, "Cannot ship item with pending OQC inspection: " + item.getProductCode());
            }
            if ("FAIL".equals(item.getInspectionStatus())) {
                throw new BusinessException(ErrorCode.INSPECTION_FAILED, "Cannot ship item with failed OQC inspection: " + item.getProductCode());
            }

            // Deduct inventory
            deductInventory(shipping, item);

            // Update sales order item delivered quantity
            if (item.getSalesOrderItem() != null) {
                SalesOrderItemEntity soItem = item.getSalesOrderItem();
                BigDecimal currentDelivered = soItem.getDeliveredQuantity() != null
                        ? soItem.getDeliveredQuantity()
                        : BigDecimal.ZERO;
                soItem.setDeliveredQuantity(currentDelivered.add(item.getShippedQuantity()));
            }
        }

        // Update status
        shipping.setShippingStatus("SHIPPED");

        ShippingEntity saved = shippingRepository.save(shipping);

        // Update sales order delivery status
        if (shipping.getSalesOrder() != null) {
            salesOrderService.updateDeliveryStatus(shipping.getSalesOrder().getSalesOrderId());
        }

        log.info("Processed shipping: {}", saved.getShippingNo());

        return shippingRepository.findByIdWithAllRelations(saved.getShippingId())
                .orElse(saved);
    }

    /**
     * Cancel shipping
     *
     * Only PENDING or INSPECTING shippings can be cancelled
     */
    @Transactional
    public ShippingEntity cancelShipping(Long shippingId, String cancellationReason) {
        log.info("Cancelling shipping: {}", shippingId);

        ShippingEntity shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPING_NOT_FOUND, "Shipping not found: " + shippingId));

        // Only PENDING or INSPECTING shippings can be cancelled
        if (!("PENDING".equals(shipping.getShippingStatus()) || "INSPECTING".equals(shipping.getShippingStatus()))) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Only PENDING or INSPECTING shippings can be cancelled: " + shippingId);
        }

        shipping.setShippingStatus("CANCELLED");
        if (cancellationReason != null) {
            shipping.setRemarks((shipping.getRemarks() != null ? shipping.getRemarks() + "\n" : "")
                    + "CANCELLED: " + cancellationReason);
        }

        ShippingEntity saved = shippingRepository.save(shipping);

        log.info("Cancelled shipping: {}", saved.getShippingNo());

        return shippingRepository.findByIdWithAllRelations(saved.getShippingId())
                .orElse(saved);
    }

    /**
     * Delete shipping
     *
     * Only PENDING or CANCELLED shippings can be deleted
     */
    @Transactional
    public void deleteShipping(Long shippingId) {
        log.info("Deleting shipping: {}", shippingId);

        ShippingEntity shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPING_NOT_FOUND, "Shipping not found: " + shippingId));

        // Only PENDING or CANCELLED shippings can be deleted
        if (!("PENDING".equals(shipping.getShippingStatus()) || "CANCELLED".equals(shipping.getShippingStatus()))) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Only PENDING or CANCELLED shippings can be deleted: " + shippingId);
        }

        shippingRepository.delete(shipping);

        log.info("Deleted shipping: {}", shipping.getShippingNo());
    }

    /**
     * Check inventory availability
     */
    private void checkInventoryAvailability(WarehouseEntity warehouse, ProductEntity product, BigDecimal quantity) {
        List<InventoryEntity> inventories = inventoryRepository
                .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
                        warehouse.getTenant().getTenantId(),
                        warehouse.getWarehouseId(),
                        product.getProductId()
                );

        BigDecimal totalAvailable = inventories.stream()
                .map(InventoryEntity::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(quantity) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY, String.format(
                    "Insufficient inventory for product: %s, requested: %s, available: %s",
                    product.getProductCode(), quantity, totalAvailable));
        }
    }

    /**
     * Deduct inventory for shipped item
     */
    private void deductInventory(ShippingEntity shipping, ShippingItemEntity item) {
        log.info("Deducting inventory for product: {}, quantity: {}",
                item.getProductCode(), item.getShippedQuantity());

        // Select LOT (FIFO)
        LotEntity lot = selectLotForShipping(shipping, item);

        if (lot == null) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY, "No available LOT for product: " + item.getProductCode());
        }

        // Create inventory transaction
        String transactionNo = String.format("OUT-%s-%03d",
                shipping.getShippingNo(),
                shipping.getItems().indexOf(item) + 1);

        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
                .tenant(shipping.getTenant())
                .transactionNo(transactionNo)
                .transactionDate(LocalDateTime.now())
                .transactionType("OUT_SHIPPING")
                .warehouse(shipping.getWarehouse())
                .product(item.getProduct())
                .lot(lot)
                .quantity(item.getShippedQuantity())
                .referenceNo(shipping.getShippingNo())
                .transactionUser(shipping.getShipper())
                .remarks("Shipping to customer: " + (shipping.getCustomer() != null ? shipping.getCustomer().getCustomerName() : "N/A"))
                .build();

        inventoryTransactionRepository.save(transaction);

        // Update inventory balance
        inventoryService.updateInventoryBalance(
                shipping.getWarehouse(),
                item.getProduct(),
                lot,
                item.getShippedQuantity(),
                "OUT_SHIPPING"
        );

        // Update lot current quantity
        lot.setCurrentQuantity(lot.getCurrentQuantity().subtract(item.getShippedQuantity()));
        lotRepository.save(lot);

        // Update item lot info
        item.setLotNo(lot.getLotNo());
        item.setExpiryDate(lot.getExpiryDate());
    }

    /**
     * Select LOT for shipping using FIFO strategy
     */
    private LotEntity selectLotForShipping(ShippingEntity shipping, ShippingItemEntity item) {
        // If specific LOT requested, use that
        if (item.getLotNo() != null && !item.getLotNo().isEmpty()) {
            return lotRepository.findByTenant_TenantIdAndLotNo(
                    shipping.getTenant().getTenantId(),
                    item.getLotNo()
            ).orElse(null);
        }

        // Otherwise, use FIFO - find oldest LOT with available quantity
        List<LotEntity> lots = lotRepository.findByTenant_TenantIdAndProduct_ProductIdOrderByCreatedAtAsc(
                shipping.getTenant().getTenantId(),
                item.getProduct().getProductId()
        );

        for (LotEntity lot : lots) {
            if (lot.getCurrentQuantity().compareTo(item.getShippedQuantity()) >= 0 &&
                    "PASSED".equals(lot.getQualityStatus()) &&
                    Boolean.TRUE.equals(lot.getIsActive())) {
                return lot;
            }
        }

        throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY,
                "No suitable LOT found for the requested quantity of product: " + item.getProductCode());
    }

    /**
     * Generate shipping number: SH-YYYYMMDD-0001
     */
    private String generateShippingNo(String tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SH-" + dateStr + "-";

        // Find last shipping number for today
        List<ShippingEntity> todayShippings = shippingRepository.findByTenantIdWithAllRelations(tenantId);

        int maxSeq = todayShippings.stream()
                .map(ShippingEntity::getShippingNo)
                .filter(no -> no.startsWith(prefix))
                .map(no -> no.substring(prefix.length()))
                .mapToInt(seq -> {
                    try {
                        return Integer.parseInt(seq);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return String.format("%s%04d", prefix, maxSeq + 1);
    }
}
