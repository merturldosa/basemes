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
 * Sales Order Service
 * 판매 주문 관리 서비스
 *
 * 핵심 기능:
 * - 판매 주문 생성 및 관리
 * - 주문 확정 워크플로우
 * - 배송 추적 및 상태 관리
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;

    /**
     * Find all sales orders by tenant
     */
    public List<SalesOrderEntity> findByTenant(String tenantId) {
        return salesOrderRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find sales order by ID
     */
    public Optional<SalesOrderEntity> findById(Long salesOrderId) {
        return salesOrderRepository.findByIdWithAllRelations(salesOrderId);
    }

    /**
     * Find sales orders by status
     */
    public List<SalesOrderEntity> findByStatus(String tenantId, String status) {
        return salesOrderRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Find sales orders by customer
     */
    public List<SalesOrderEntity> findByCustomer(String tenantId, Long customerId) {
        return salesOrderRepository.findByTenantIdAndCustomerId(tenantId, customerId);
    }

    /**
     * Find sales orders by date range
     */
    public List<SalesOrderEntity> findByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return salesOrderRepository.findByTenantIdAndOrderDateBetween(tenantId, startDate, endDate);
    }

    /**
     * Create new sales order
     *
     * 워크플로우:
     * 1. 주문 번호 자동 생성 (SO-YYYYMMDD-0001)
     * 2. 주문 헤더 생성 (DRAFT 상태)
     * 3. 주문 항목 검증 및 금액 계산
     * 4. 저장
     */
    @Transactional
    public SalesOrderEntity createSalesOrder(String tenantId, SalesOrderEntity salesOrder) {
        log.info("Creating sales order for tenant: {}, customer: {}",
            tenantId,
            salesOrder.getCustomer().getCustomerId());

        // 1. Resolve tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND, "Tenant not found: " + tenantId));
        salesOrder.setTenant(tenant);

        // 2. Generate order number if not provided
        if (salesOrder.getOrderNo() == null || salesOrder.getOrderNo().isEmpty()) {
            salesOrder.setOrderNo(generateOrderNo(tenantId));
        }

        // Check duplicate
        if (salesOrderRepository.existsByTenant_TenantIdAndOrderNo(tenantId, salesOrder.getOrderNo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Order number already exists: " + salesOrder.getOrderNo());
        }

        // 3. Resolve customer
        CustomerEntity customer = customerRepository.findById(salesOrder.getCustomer().getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Customer not found: " + salesOrder.getCustomer().getCustomerId()));
        salesOrder.setCustomer(customer);

        // 4. Resolve sales user
        UserEntity salesUser = userRepository.findById(salesOrder.getSalesUser().getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "Sales user not found: " + salesOrder.getSalesUser().getUserId()));
        salesOrder.setSalesUser(salesUser);

        // 5. Set initial status
        if (salesOrder.getStatus() == null || salesOrder.getStatus().isEmpty()) {
            salesOrder.setStatus("DRAFT");
        }

        // 6. Process items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrderItemEntity item : salesOrder.getItems()) {
            item.setSalesOrder(salesOrder);

            // Resolve product or material
            if (item.getProduct() != null) {
                ProductEntity product = productRepository.findById(item.getProduct().getProductId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + item.getProduct().getProductId()));
                item.setProduct(product);
                // Product code and name will be retrieved from product entity
            } else if (item.getMaterial() != null) {
                MaterialEntity material = materialRepository.findById(item.getMaterial().getMaterialId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND, "Material not found: " + item.getMaterial().getMaterialId()));
                item.setMaterial(material);
                // Material code and name will be retrieved from material entity
            }

            // Calculate line amount
            if (item.getUnitPrice() != null && item.getOrderedQuantity() != null) {
                item.setAmount(item.getUnitPrice().multiply(item.getOrderedQuantity()));
            }

            // Initialize delivered quantity
            if (item.getDeliveredQuantity() == null) {
                item.setDeliveredQuantity(BigDecimal.ZERO);
            }

            // Add to total
            if (item.getAmount() != null) {
                totalAmount = totalAmount.add(item.getAmount());
            }
        }

        salesOrder.setTotalAmount(totalAmount);

        // 7. Save sales order
        SalesOrderEntity saved = salesOrderRepository.save(salesOrder);

        log.info("Created sales order: {} with {} items, total: {}",
            saved.getOrderNo(), saved.getItems().size(), saved.getTotalAmount());

        return salesOrderRepository.findByIdWithAllRelations(saved.getSalesOrderId())
                .orElse(saved);
    }

    /**
     * Update sales order
     *
     * Only DRAFT orders can be updated
     */
    @Transactional
    public SalesOrderEntity updateSalesOrder(Long salesOrderId, SalesOrderEntity updates) {
        log.info("Updating sales order: {}", salesOrderId);

        SalesOrderEntity existing = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ORDER_NOT_FOUND, "Sales order not found: " + salesOrderId));

        // Only DRAFT orders can be updated
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Only DRAFT orders can be updated: " + salesOrderId);
        }

        // Update fields
        if (updates.getOrderDate() != null) {
            existing.setOrderDate(updates.getOrderDate());
        }
        if (updates.getCustomer() != null) {
            CustomerEntity customer = customerRepository.findById(updates.getCustomer().getCustomerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
            existing.setCustomer(customer);
        }
        if (updates.getRequestedDeliveryDate() != null) {
            existing.setRequestedDeliveryDate(updates.getRequestedDeliveryDate());
        }
        if (updates.getDeliveryAddress() != null) {
            existing.setDeliveryAddress(updates.getDeliveryAddress());
        }
        if (updates.getPaymentTerms() != null) {
            existing.setPaymentTerms(updates.getPaymentTerms());
        }
        if (updates.getCurrency() != null) {
            existing.setCurrency(updates.getCurrency());
        }
        if (updates.getRemarks() != null) {
            existing.setRemarks(updates.getRemarks());
        }

        // Recalculate total if items updated
        if (updates.getItems() != null && !updates.getItems().isEmpty()) {
            existing.getItems().clear();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (SalesOrderItemEntity item : updates.getItems()) {
                item.setSalesOrder(existing);

                // Resolve product or material
                if (item.getProduct() != null) {
                    ProductEntity product = productRepository.findById(item.getProduct().getProductId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found"));
                    item.setProduct(product);
                    // Product code and name will be retrieved from product entity
                }

                // Calculate line amount
                if (item.getUnitPrice() != null && item.getOrderedQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getOrderedQuantity()));
                }

                if (item.getAmount() != null) {
                    totalAmount = totalAmount.add(item.getAmount());
                }

                existing.getItems().add(item);
            }

            existing.setTotalAmount(totalAmount);
        }

        SalesOrderEntity saved = salesOrderRepository.save(existing);

        log.info("Updated sales order: {}", saved.getOrderNo());

        return salesOrderRepository.findByIdWithAllRelations(saved.getSalesOrderId())
                .orElse(saved);
    }

    /**
     * Confirm sales order
     *
     * 워크플로우:
     * 1. 상태 검증 (DRAFT만 확정 가능)
     * 2. 상태 → CONFIRMED
     */
    @Transactional
    public SalesOrderEntity confirmSalesOrder(Long salesOrderId) {
        log.info("Confirming sales order: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ORDER_NOT_FOUND, "Sales order not found: " + salesOrderId));

        // Only DRAFT orders can be confirmed
        if (!"DRAFT".equals(salesOrder.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Only DRAFT orders can be confirmed: " + salesOrderId);
        }

        salesOrder.setStatus("CONFIRMED");

        SalesOrderEntity saved = salesOrderRepository.save(salesOrder);

        log.info("Confirmed sales order: {}", saved.getOrderNo());

        return salesOrderRepository.findByIdWithAllRelations(saved.getSalesOrderId())
                .orElse(saved);
    }

    /**
     * Cancel sales order
     *
     * Only DRAFT or CONFIRMED orders can be cancelled
     */
    @Transactional
    public SalesOrderEntity cancelSalesOrder(Long salesOrderId, String cancellationReason) {
        log.info("Cancelling sales order: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ORDER_NOT_FOUND, "Sales order not found: " + salesOrderId));

        // Only DRAFT or CONFIRMED orders can be cancelled
        if (!("DRAFT".equals(salesOrder.getStatus()) || "CONFIRMED".equals(salesOrder.getStatus()))) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Only DRAFT or CONFIRMED orders can be cancelled: " + salesOrderId);
        }

        salesOrder.setStatus("CANCELLED");
        if (cancellationReason != null) {
            salesOrder.setRemarks((salesOrder.getRemarks() != null ? salesOrder.getRemarks() + "\n" : "")
                    + "CANCELLED: " + cancellationReason);
        }

        SalesOrderEntity saved = salesOrderRepository.save(salesOrder);

        log.info("Cancelled sales order: {}", saved.getOrderNo());

        return salesOrderRepository.findByIdWithAllRelations(saved.getSalesOrderId())
                .orElse(saved);
    }

    /**
     * Update delivery status based on shipping
     *
     * Called by ShippingService after processing shipping
     */
    @Transactional
    public void updateDeliveryStatus(Long salesOrderId) {
        log.info("Updating delivery status for sales order: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findByIdWithAllRelations(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ORDER_NOT_FOUND, "Sales order not found: " + salesOrderId));

        // Check if all items are fully delivered
        boolean fullyDelivered = true;
        boolean partiallyDelivered = false;

        for (SalesOrderItemEntity item : salesOrder.getItems()) {
            if (item.getDeliveredQuantity().compareTo(BigDecimal.ZERO) > 0) {
                partiallyDelivered = true;
            }
            if (item.getDeliveredQuantity().compareTo(item.getOrderedQuantity()) < 0) {
                fullyDelivered = false;
            }
        }

        // Update status
        if (fullyDelivered) {
            salesOrder.setStatus("DELIVERED");
            log.info("Sales order {} fully delivered", salesOrder.getOrderNo());
        } else if (partiallyDelivered) {
            salesOrder.setStatus("PARTIALLY_DELIVERED");
            log.info("Sales order {} partially delivered", salesOrder.getOrderNo());
        }

        salesOrderRepository.save(salesOrder);
    }

    /**
     * Delete sales order
     *
     * Only DRAFT or CANCELLED orders can be deleted
     */
    @Transactional
    public void deleteSalesOrder(Long salesOrderId) {
        log.info("Deleting sales order: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ORDER_NOT_FOUND, "Sales order not found: " + salesOrderId));

        // Only DRAFT or CANCELLED orders can be deleted
        if (!("DRAFT".equals(salesOrder.getStatus()) || "CANCELLED".equals(salesOrder.getStatus()))) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Only DRAFT or CANCELLED orders can be deleted: " + salesOrderId);
        }

        salesOrderRepository.delete(salesOrder);

        log.info("Deleted sales order: {}", salesOrder.getOrderNo());
    }

    /**
     * Generate sales order number: SO-YYYYMMDD-0001
     */
    private String generateOrderNo(String tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SO-" + dateStr + "-";

        // Find last order number for today
        List<SalesOrderEntity> todayOrders = salesOrderRepository.findByTenantIdWithAllRelations(tenantId);

        int maxSeq = todayOrders.stream()
                .map(SalesOrderEntity::getOrderNo)
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
