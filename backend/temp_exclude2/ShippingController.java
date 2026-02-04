package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.warehouse.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Shipping Controller
 * 출하 컨트롤러
 *
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/shippings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shippings", description = "출하 관리 API")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_USER')")
    @Operation(summary = "Get all shippings", description = "모든 출하 조회")
    public ResponseEntity<List<ShippingResponse>> getAllShippings() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/shippings - tenant: {}", tenantId);

        List<ShippingEntity> shippings = shippingService.getAllShippings(tenantId);
        List<ShippingResponse> responses = shippings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_USER')")
    @Operation(summary = "Get shipping by ID", description = "ID로 출하 조회")
    public ResponseEntity<ShippingResponse> getShippingById(@PathVariable Long id) {
        log.info("GET /api/shippings/{}", id);

        ShippingEntity shipping = shippingService.getShippingById(id);
        return ResponseEntity.ok(toResponse(shipping));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_USER')")
    @Operation(summary = "Get shippings by status", description = "상태별 출하 조회")
    public ResponseEntity<List<ShippingResponse>> getShippingsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/shippings/status/{} - tenant: {}", status, tenantId);

        List<ShippingEntity> shippings = shippingService.getShippingsByStatus(tenantId, status);
        List<ShippingResponse> responses = shippings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Create shipping", description = "출하 생성")
    public ResponseEntity<ShippingResponse> createShipping(
            @Valid @RequestBody ShippingCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/shippings - tenant: {}, shippingNo: {}", tenantId, request.getShippingNo());

        ShippingEntity shipping = toEntity(request, tenantId);
        ShippingEntity created = shippingService.createShipping(shipping);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete shipping", description = "출하 삭제")
    public ResponseEntity<Void> deleteShipping(@PathVariable Long id) {
        log.info("DELETE /api/shippings/{}", id);

        shippingService.deleteShipping(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Complete shipping", description = "출하 완료 처리")
    public ResponseEntity<ShippingResponse> completeShipping(@PathVariable Long id) {
        log.info("POST /api/shippings/{}/complete", id);

        ShippingEntity completed = shippingService.completeShipping(id);
        return ResponseEntity.ok(toResponse(completed));
    }

    // Helper methods
    private ShippingResponse toResponse(ShippingEntity entity) {
        return ShippingResponse.builder()
                .shippingId(entity.getShippingId())
                .tenantId(entity.getTenant().getTenantId())
                .shippingNo(entity.getShippingNo())
                .shippingDate(entity.getShippingDate())
                .salesOrderId(entity.getSalesOrder() != null ? entity.getSalesOrder().getSalesOrderId() : null)
                .salesOrderNo(entity.getSalesOrder() != null ? entity.getSalesOrder().getOrderNo() : null)
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getCustomerId() : null)
                .customerCode(entity.getCustomer() != null ? entity.getCustomer().getCustomerCode() : null)
                .customerName(entity.getCustomer() != null ? entity.getCustomer().getCustomerName() : null)
                .warehouseId(entity.getWarehouse() != null ? entity.getWarehouse().getWarehouseId() : null)
                .warehouseCode(entity.getWarehouse() != null ? entity.getWarehouse().getWarehouseCode() : null)
                .warehouseName(entity.getWarehouse() != null ? entity.getWarehouse().getWarehouseName() : null)
                .shippingType(entity.getShippingType())
                .shippingStatus(entity.getShippingStatus())
                .totalQuantity(entity.getTotalQuantity())
                .totalAmount(entity.getTotalAmount())
                .shipperUserId(entity.getShipper() != null ? entity.getShipper().getUserId() : null)
                .shipperName(entity.getShipperName())
                .deliveryAddress(entity.getDeliveryAddress())
                .trackingNumber(entity.getTrackingNumber())
                .carrierName(entity.getCarrierName())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .items(entity.getItems() != null ? entity.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    private ShippingItemResponse toItemResponse(ShippingItemEntity entity) {
        return ShippingItemResponse.builder()
                .shippingItemId(entity.getShippingItemId())
                .shippingId(entity.getShipping().getShippingId())
                .salesOrderItemId(entity.getSalesOrderItem() != null ? entity.getSalesOrderItem().getSalesOrderItemId() : null)
                .productId(entity.getProduct() != null ? entity.getProduct().getProductId() : null)
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .orderedQuantity(entity.getOrderedQuantity())
                .shippedQuantity(entity.getShippedQuantity())
                .unitPrice(entity.getUnitPrice())
                .lineAmount(entity.getLineAmount())
                .lotNo(entity.getLotNo())
                .expiryDate(entity.getExpiryDate())
                .inspectionStatus(entity.getInspectionStatus())
                .qualityInspectionId(entity.getQualityInspection() != null ? entity.getQualityInspection().getQualityInspectionId() : null)
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ShippingEntity toEntity(ShippingCreateRequest request, String tenantId) {
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(tenantId);

        SalesOrderEntity salesOrder = null;
        if (request.getSalesOrderId() != null) {
            salesOrder = new SalesOrderEntity();
            salesOrder.setSalesOrderId(request.getSalesOrderId());
        }

        CustomerEntity customer = null;
        if (request.getCustomerId() != null) {
            customer = new CustomerEntity();
            customer.setCustomerId(request.getCustomerId());
        }

        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setWarehouseId(request.getWarehouseId());

        UserEntity shipper = null;
        if (request.getShipperUserId() != null) {
            shipper = new UserEntity();
            shipper.setUserId(request.getShipperUserId());
        }

        ShippingEntity entity = ShippingEntity.builder()
                .tenant(tenant)
                .salesOrder(salesOrder)
                .customer(customer)
                .warehouse(warehouse)
                .shippingNo(request.getShippingNo())
                .shippingDate(request.getShippingDate())
                .shippingType(request.getShippingType())
                .shippingStatus(request.getShippingStatus())
                .totalQuantity(request.getTotalQuantity())
                .totalAmount(request.getTotalAmount())
                .shipper(shipper)
                .shipperName(request.getShipperName())
                .deliveryAddress(request.getDeliveryAddress())
                .trackingNumber(request.getTrackingNumber())
                .carrierName(request.getCarrierName())
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        // Add items
        if (request.getItems() != null) {
            for (ShippingItemRequest itemRequest : request.getItems()) {
                ShippingItemEntity item = toItemEntity(itemRequest);
                entity.addItem(item);
            }
        }

        return entity;
    }

    private ShippingItemEntity toItemEntity(ShippingItemRequest request) {
        ProductEntity product = new ProductEntity();
        product.setProductId(request.getProductId());

        SalesOrderItemEntity salesOrderItem = null;
        if (request.getSalesOrderItemId() != null) {
            salesOrderItem = new SalesOrderItemEntity();
            salesOrderItem.setSalesOrderItemId(request.getSalesOrderItemId());
        }

        return ShippingItemEntity.builder()
                .salesOrderItem(salesOrderItem)
                .product(product)
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .orderedQuantity(request.getOrderedQuantity())
                .shippedQuantity(request.getShippedQuantity())
                .unitPrice(request.getUnitPrice())
                .lineAmount(request.getLineAmount())
                .lotNo(request.getLotNo())
                .expiryDate(request.getExpiryDate())
                .inspectionStatus(request.getInspectionStatus())
                .remarks(request.getRemarks())
                .build();
    }
}
