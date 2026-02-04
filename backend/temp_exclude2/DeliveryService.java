package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Delivery Service
 * 출하 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final TenantRepository tenantRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final LotRepository lotRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final SalesOrderService salesOrderService;

    /**
     * Get all deliveries by tenant
     */
    public List<DeliveryEntity> getAllDeliveriesByTenant(String tenantId) {
        log.info("Getting all deliveries for tenant: {}", tenantId);
        return deliveryRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get delivery by ID
     */
    public DeliveryEntity getDeliveryById(Long deliveryId) {
        log.info("Getting delivery by ID: {}", deliveryId);
        return deliveryRepository.findByIdWithAllRelations(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));
    }

    /**
     * Get deliveries by status
     */
    public List<DeliveryEntity> getDeliveriesByStatus(String tenantId, String status) {
        log.info("Getting deliveries by status: {} for tenant: {}", status, tenantId);
        return deliveryRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get deliveries by sales order
     */
    public List<DeliveryEntity> getDeliveriesBySalesOrder(String tenantId, Long salesOrderId) {
        log.info("Getting deliveries by sales order: {} for tenant: {}", salesOrderId, tenantId);
        return deliveryRepository.findByTenantIdAndSalesOrderId(tenantId, salesOrderId);
    }

    /**
     * Get deliveries by quality check status
     */
    public List<DeliveryEntity> getDeliveriesByQualityCheckStatus(String tenantId, String qualityCheckStatus) {
        log.info("Getting deliveries by quality check status: {} for tenant: {}", qualityCheckStatus, tenantId);
        return deliveryRepository.findByTenantIdAndQualityCheckStatus(tenantId, qualityCheckStatus);
    }

    /**
     * Get deliveries by date range
     */
    public List<DeliveryEntity> getDeliveriesByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting deliveries by date range: {} to {} for tenant: {}", startDate, endDate, tenantId);
        return deliveryRepository.findByTenantIdAndDeliveryDateBetween(tenantId, startDate, endDate);
    }

    /**
     * Create delivery
     */
    @Transactional
    public DeliveryEntity createDelivery(DeliveryEntity delivery) {
        log.info("Creating delivery: {}", delivery.getDeliveryNo());

        // Check duplicate
        if (deliveryRepository.existsByTenant_TenantIdAndDeliveryNo(
                delivery.getTenant().getTenantId(), delivery.getDeliveryNo())) {
            throw new IllegalArgumentException("Delivery already exists: " + delivery.getDeliveryNo());
        }

        // Validate tenant
        TenantEntity tenant = tenantRepository.findById(delivery.getTenant().getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + delivery.getTenant().getTenantId()));
        delivery.setTenant(tenant);

        // Validate sales order
        SalesOrderEntity salesOrder = salesOrderRepository.findById(delivery.getSalesOrder().getSalesOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + delivery.getSalesOrder().getSalesOrderId()));
        delivery.setSalesOrder(salesOrder);

        // Sales order must be confirmed
        if (!"CONFIRMED".equals(salesOrder.getStatus()) &&
            !"PARTIALLY_DELIVERED".equals(salesOrder.getStatus())) {
            throw new IllegalArgumentException("Sales order must be CONFIRMED or PARTIALLY_DELIVERED to create delivery");
        }

        // Validate warehouse
        WarehouseEntity warehouse = warehouseRepository.findById(delivery.getWarehouse().getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + delivery.getWarehouse().getWarehouseId()));
        delivery.setWarehouse(warehouse);

        // Validate shipper
        UserEntity shipper = userRepository.findById(delivery.getShipper().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Shipper user not found: " + delivery.getShipper().getUserId()));
        delivery.setShipper(shipper);

        // Validate inspector if provided
        if (delivery.getInspector() != null && delivery.getInspector().getUserId() != null) {
            UserEntity inspector = userRepository.findById(delivery.getInspector().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Inspector user not found: " + delivery.getInspector().getUserId()));
            delivery.setInspector(inspector);
        }

        // Set default statuses
        if (delivery.getStatus() == null) {
            delivery.setStatus("PENDING");
        }
        if (delivery.getQualityCheckStatus() == null) {
            delivery.setQualityCheckStatus("PENDING");
        }

        // Process items
        if (delivery.getItems() != null && !delivery.getItems().isEmpty()) {
            for (DeliveryItemEntity item : delivery.getItems()) {
                // Validate sales order item
                SalesOrderItemEntity salesOrderItem = salesOrderItemRepository.findById(item.getSalesOrderItem().getSalesOrderItemId())
                        .orElseThrow(() -> new IllegalArgumentException("Sales order item not found: " + item.getSalesOrderItem().getSalesOrderItemId()));
                item.setSalesOrderItem(salesOrderItem);

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

                // Validate LOT if provided
                if (item.getLot() != null && item.getLot().getLotId() != null) {
                    LotEntity lot = lotRepository.findById(item.getLot().getLotId())
                            .orElseThrow(() -> new IllegalArgumentException("LOT not found: " + item.getLot().getLotId()));
                    item.setLot(lot);
                }

                item.setDelivery(delivery);
            }
        }

        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);
        log.info("Delivery created successfully: {}", savedDelivery.getDeliveryNo());

        // Update sales order item delivered quantities
        updateSalesOrderItemQuantities(savedDelivery.getSalesOrder().getSalesOrderId());

        // Update sales order status
        salesOrderService.updateSalesOrderStatus(savedDelivery.getSalesOrder().getSalesOrderId());

        return getDeliveryById(savedDelivery.getDeliveryId());
    }

    /**
     * Update delivery
     */
    @Transactional
    public DeliveryEntity updateDelivery(Long deliveryId, DeliveryEntity updatedDelivery) {
        log.info("Updating delivery ID: {}", deliveryId);

        DeliveryEntity existingDelivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));

        // Cannot update if already completed
        if ("COMPLETED".equals(existingDelivery.getStatus())) {
            throw new IllegalArgumentException("Cannot update completed delivery");
        }

        // Update fields
        existingDelivery.setDeliveryDate(updatedDelivery.getDeliveryDate());
        existingDelivery.setShippingMethod(updatedDelivery.getShippingMethod());
        existingDelivery.setTrackingNo(updatedDelivery.getTrackingNo());
        existingDelivery.setCarrier(updatedDelivery.getCarrier());
        existingDelivery.setRemarks(updatedDelivery.getRemarks());

        // Update quality check status if provided
        if (updatedDelivery.getQualityCheckStatus() != null) {
            existingDelivery.setQualityCheckStatus(updatedDelivery.getQualityCheckStatus());
        }

        // Update inspector if provided
        if (updatedDelivery.getInspector() != null && updatedDelivery.getInspector().getUserId() != null) {
            UserEntity inspector = userRepository.findById(updatedDelivery.getInspector().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Inspector user not found"));
            existingDelivery.setInspector(inspector);
        }

        // Update inspection date if provided
        if (updatedDelivery.getInspectionDate() != null) {
            existingDelivery.setInspectionDate(updatedDelivery.getInspectionDate());
        }

        deliveryRepository.save(existingDelivery);
        log.info("Delivery updated successfully: {}", existingDelivery.getDeliveryNo());

        return getDeliveryById(deliveryId);
    }

    /**
     * Delete delivery
     */
    @Transactional
    public void deleteDelivery(Long deliveryId) {
        log.info("Deleting delivery ID: {}", deliveryId);

        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));

        // Cannot delete if already completed
        if ("COMPLETED".equals(delivery.getStatus())) {
            throw new IllegalArgumentException("Cannot delete completed delivery");
        }

        Long salesOrderId = delivery.getSalesOrder().getSalesOrderId();

        deliveryRepository.delete(delivery);
        log.info("Delivery deleted successfully: {}", delivery.getDeliveryNo());

        // Update sales order item delivered quantities
        updateSalesOrderItemQuantities(salesOrderId);

        // Update sales order status
        salesOrderService.updateSalesOrderStatus(salesOrderId);
    }

    /**
     * Complete delivery
     */
    @Transactional
    public DeliveryEntity completeDelivery(Long deliveryId) {
        log.info("Completing delivery ID: {}", deliveryId);

        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));

        if (!"PENDING".equals(delivery.getStatus())) {
            throw new IllegalArgumentException("Only PENDING deliveries can be completed");
        }

        delivery.setStatus("COMPLETED");
        deliveryRepository.save(delivery);

        log.info("Delivery completed successfully: {}", delivery.getDeliveryNo());
        return getDeliveryById(deliveryId);
    }

    /**
     * Update quality check status
     */
    @Transactional
    public DeliveryEntity updateQualityCheckStatus(Long deliveryId, String qualityCheckStatus, Long inspectorUserId) {
        log.info("Updating quality check status for delivery ID: {} to {}", deliveryId, qualityCheckStatus);

        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));

        // Validate inspector
        UserEntity inspector = userRepository.findById(inspectorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Inspector user not found: " + inspectorUserId));

        delivery.setQualityCheckStatus(qualityCheckStatus);
        delivery.setInspector(inspector);
        delivery.setInspectionDate(LocalDateTime.now());

        deliveryRepository.save(delivery);
        log.info("Quality check status updated to: {}", qualityCheckStatus);

        return getDeliveryById(deliveryId);
    }

    /**
     * Update sales order item delivered quantities
     */
    private void updateSalesOrderItemQuantities(Long salesOrderId) {
        log.info("Updating sales order item delivered quantities for order ID: {}", salesOrderId);

        SalesOrderEntity salesOrder = salesOrderRepository.findByIdWithAllRelations(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found"));

        // Get all deliveries for this sales order
        List<DeliveryEntity> deliveries = deliveryRepository.findByTenantIdAndSalesOrderId(
                salesOrder.getTenant().getTenantId(), salesOrderId);

        // Reset all item delivered quantities to zero
        for (SalesOrderItemEntity orderItem : salesOrder.getItems()) {
            orderItem.setDeliveredQuantity(BigDecimal.ZERO);
        }

        // Sum up delivered quantities from all deliveries
        for (DeliveryEntity delivery : deliveries) {
            for (DeliveryItemEntity deliveryItem : delivery.getItems()) {
                Long salesOrderItemId = deliveryItem.getSalesOrderItem().getSalesOrderItemId();

                // Find corresponding sales order item
                SalesOrderItemEntity orderItem = salesOrder.getItems().stream()
                        .filter(item -> item.getSalesOrderItemId().equals(salesOrderItemId))
                        .findFirst()
                        .orElse(null);

                if (orderItem != null) {
                    BigDecimal currentDelivered = orderItem.getDeliveredQuantity() != null ?
                            orderItem.getDeliveredQuantity() : BigDecimal.ZERO;
                    BigDecimal newDelivered = currentDelivered.add(deliveryItem.getDeliveredQuantity());
                    orderItem.setDeliveredQuantity(newDelivered);
                }
            }
        }

        salesOrderRepository.save(salesOrder);
        log.info("Sales order item quantities updated successfully");
    }
}
