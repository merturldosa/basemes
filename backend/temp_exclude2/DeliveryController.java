package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.sales.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Delivery Controller
 * 출하 컨트롤러
 *
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deliveries", description = "출하 관리 API")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER', 'SHIPPING_USER')")
    @Operation(summary = "Get all deliveries", description = "모든 출하 조회")
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/deliveries - tenant: {}", tenantId);

        List<DeliveryEntity> deliveries = deliveryService.getAllDeliveriesByTenant(tenantId);
        List<DeliveryResponse> responses = deliveries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER', 'SHIPPING_USER')")
    @Operation(summary = "Get delivery by ID", description = "ID로 출하 조회")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long id) {
        log.info("GET /api/deliveries/{}", id);

        DeliveryEntity delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(toResponse(delivery));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER', 'SHIPPING_USER')")
    @Operation(summary = "Get deliveries by status", description = "상태별 출하 조회")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/deliveries/status/{} - tenant: {}", status, tenantId);

        List<DeliveryEntity> deliveries = deliveryService.getDeliveriesByStatus(tenantId, status);
        List<DeliveryResponse> responses = deliveries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/sales-order/{salesOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER', 'SHIPPING_USER')")
    @Operation(summary = "Get deliveries by sales order", description = "판매 주문별 출하 조회")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesBySalesOrder(@PathVariable Long salesOrderId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/deliveries/sales-order/{} - tenant: {}", salesOrderId, tenantId);

        List<DeliveryEntity> deliveries = deliveryService.getDeliveriesBySalesOrder(tenantId, salesOrderId);
        List<DeliveryResponse> responses = deliveries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/quality-check-status/{qualityCheckStatus}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER', 'QUALITY_MANAGER')")
    @Operation(summary = "Get deliveries by quality check status", description = "품질 검사 상태별 출하 조회")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByQualityCheckStatus(@PathVariable String qualityCheckStatus) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/deliveries/quality-check-status/{} - tenant: {}", qualityCheckStatus, tenantId);

        List<DeliveryEntity> deliveries = deliveryService.getDeliveriesByQualityCheckStatus(tenantId, qualityCheckStatus);
        List<DeliveryResponse> responses = deliveries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER', 'SHIPPING_USER')")
    @Operation(summary = "Get deliveries by date range", description = "기간별 출하 조회")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/deliveries/date-range - tenant: {}, startDate: {}, endDate: {}", tenantId, startDate, endDate);

        List<DeliveryEntity> deliveries = deliveryService.getDeliveriesByDateRange(tenantId, startDate, endDate);
        List<DeliveryResponse> responses = deliveries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER', 'SHIPPING_USER')")
    @Operation(summary = "Create delivery", description = "출하 생성")
    public ResponseEntity<DeliveryResponse> createDelivery(@Valid @RequestBody DeliveryCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/deliveries - tenant: {}, deliveryNo: {}", tenantId, request.getDeliveryNo());

        DeliveryEntity delivery = toEntity(request, tenantId);
        DeliveryEntity created = deliveryService.createDelivery(delivery);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER', 'SHIPPING_USER')")
    @Operation(summary = "Update delivery", description = "출하 수정")
    public ResponseEntity<DeliveryResponse> updateDelivery(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryUpdateRequest request) {
        log.info("PUT /api/deliveries/{}", id);

        String tenantId = TenantContext.getCurrentTenant();
        DeliveryEntity delivery = toEntityForUpdate(request, tenantId);
        DeliveryEntity updated = deliveryService.updateDelivery(id, delivery);

        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Delete delivery", description = "출하 삭제")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        log.info("DELETE /api/deliveries/{}", id);

        deliveryService.deleteDelivery(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Complete delivery", description = "출하 완료")
    public ResponseEntity<DeliveryResponse> completeDelivery(@PathVariable Long id) {
        log.info("POST /api/deliveries/{}/complete", id);

        DeliveryEntity completed = deliveryService.completeDelivery(id);
        return ResponseEntity.ok(toResponse(completed));
    }

    @PostMapping("/{id}/quality-check")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "Update quality check status", description = "품질 검사 상태 업데이트")
    public ResponseEntity<DeliveryResponse> updateQualityCheckStatus(
            @PathVariable Long id,
            @RequestParam String qualityCheckStatus,
            @RequestParam Long inspectorUserId) {
        log.info("POST /api/deliveries/{}/quality-check - status: {}, inspector: {}", id, qualityCheckStatus, inspectorUserId);

        DeliveryEntity updated = deliveryService.updateQualityCheckStatus(id, qualityCheckStatus, inspectorUserId);
        return ResponseEntity.ok(toResponse(updated));
    }

    // Helper methods

    private DeliveryResponse toResponse(DeliveryEntity entity) {
        List<DeliveryItemResponse> itemResponses = entity.getItems() != null ?
                entity.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()) :
                List.of();

        return DeliveryResponse.builder()
                .deliveryId(entity.getDeliveryId())
                .tenantId(entity.getTenant().getTenantId())
                .deliveryNo(entity.getDeliveryNo())
                .deliveryDate(entity.getDeliveryDate())
                .salesOrderId(entity.getSalesOrder().getSalesOrderId())
                .salesOrderNo(entity.getSalesOrder().getOrderNo())
                .customerId(entity.getSalesOrder().getCustomer().getCustomerId())
                .customerName(entity.getSalesOrder().getCustomer().getCustomerName())
                .warehouseId(entity.getWarehouse().getWarehouseId())
                .warehouseCode(entity.getWarehouse().getWarehouseCode())
                .warehouseName(entity.getWarehouse().getWarehouseName())
                .qualityCheckStatus(entity.getQualityCheckStatus())
                .inspectorUserId(entity.getInspector() != null ? entity.getInspector().getUserId() : null)
                .inspectorName(entity.getInspector() != null ? entity.getInspector().getUsername() : null)
                .inspectionDate(entity.getInspectionDate())
                .shippingMethod(entity.getShippingMethod())
                .trackingNo(entity.getTrackingNo())
                .carrier(entity.getCarrier())
                .status(entity.getStatus())
                .shipperUserId(entity.getShipper().getUserId())
                .shipperName(entity.getShipper().getUsername())
                .remarks(entity.getRemarks())
                .items(itemResponses)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private DeliveryItemResponse toItemResponse(DeliveryItemEntity entity) {
        return DeliveryItemResponse.builder()
                .deliveryItemId(entity.getDeliveryItemId())
                .lineNo(entity.getLineNo())
                .salesOrderItemId(entity.getSalesOrderItem().getSalesOrderItemId())
                .productId(entity.getProduct() != null ? entity.getProduct().getProductId() : null)
                .productCode(entity.getProduct() != null ? entity.getProduct().getProductCode() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getProductName() : null)
                .materialId(entity.getMaterial() != null ? entity.getMaterial().getMaterialId() : null)
                .materialCode(entity.getMaterial() != null ? entity.getMaterial().getMaterialCode() : null)
                .materialName(entity.getMaterial() != null ? entity.getMaterial().getMaterialName() : null)
                .deliveredQuantity(entity.getDeliveredQuantity())
                .unit(entity.getUnit())
                .lotId(entity.getLot() != null ? entity.getLot().getLotId() : null)
                .lotNo(entity.getLot() != null ? entity.getLot().getLotNo() : null)
                .location(entity.getLocation())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private DeliveryEntity toEntity(DeliveryCreateRequest request, String tenantId) {
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(tenantId);

        SalesOrderEntity salesOrder = new SalesOrderEntity();
        salesOrder.setSalesOrderId(request.getSalesOrderId());

        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setWarehouseId(request.getWarehouseId());

        UserEntity shipper = new UserEntity();
        shipper.setUserId(request.getShipperUserId());

        UserEntity inspector = null;
        if (request.getInspectorUserId() != null) {
            inspector = new UserEntity();
            inspector.setUserId(request.getInspectorUserId());
        }

        DeliveryEntity entity = DeliveryEntity.builder()
                .tenant(tenant)
                .deliveryNo(request.getDeliveryNo())
                .deliveryDate(request.getDeliveryDate())
                .salesOrder(salesOrder)
                .warehouse(warehouse)
                .shipper(shipper)
                .inspector(inspector)
                .shippingMethod(request.getShippingMethod())
                .trackingNo(request.getTrackingNo())
                .carrier(request.getCarrier())
                .remarks(request.getRemarks())
                .build();

        // Add items
        if (request.getItems() != null) {
            for (DeliveryItemRequest itemRequest : request.getItems()) {
                DeliveryItemEntity item = toItemEntity(itemRequest);
                entity.addItem(item);
            }
        }

        return entity;
    }

    private DeliveryEntity toEntityForUpdate(DeliveryUpdateRequest request, String tenantId) {
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(tenantId);

        UserEntity inspector = null;
        if (request.getInspectorUserId() != null) {
            inspector = new UserEntity();
            inspector.setUserId(request.getInspectorUserId());
        }

        return DeliveryEntity.builder()
                .tenant(tenant)
                .deliveryDate(request.getDeliveryDate())
                .qualityCheckStatus(request.getQualityCheckStatus())
                .inspector(inspector)
                .inspectionDate(request.getInspectionDate())
                .shippingMethod(request.getShippingMethod())
                .trackingNo(request.getTrackingNo())
                .carrier(request.getCarrier())
                .remarks(request.getRemarks())
                .build();
    }

    private DeliveryItemEntity toItemEntity(DeliveryItemRequest request) {
        SalesOrderItemEntity salesOrderItem = new SalesOrderItemEntity();
        salesOrderItem.setSalesOrderItemId(request.getSalesOrderItemId());

        ProductEntity product = null;
        if (request.getProductId() != null) {
            product = new ProductEntity();
            product.setProductId(request.getProductId());
        }

        MaterialEntity material = null;
        if (request.getMaterialId() != null) {
            material = new MaterialEntity();
            material.setMaterialId(request.getMaterialId());
        }

        LotEntity lot = null;
        if (request.getLotId() != null) {
            lot = new LotEntity();
            lot.setLotId(request.getLotId());
        }

        return DeliveryItemEntity.builder()
                .lineNo(request.getLineNo())
                .salesOrderItem(salesOrderItem)
                .product(product)
                .material(material)
                .deliveredQuantity(request.getDeliveredQuantity())
                .unit(request.getUnit())
                .lot(lot)
                .location(request.getLocation())
                .remarks(request.getRemarks())
                .build();
    }
}
