package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.sales.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.SalesOrderService;
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
 * Sales Order Controller
 * 판매 주문 컨트롤러
 *
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Orders", description = "판매 주문 관리 API")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_USER')")
    @Operation(summary = "Get all sales orders", description = "모든 판매 주문 조회")
    public ResponseEntity<List<SalesOrderResponse>> getAllSalesOrders() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/sales-orders - tenant: {}", tenantId);

        List<SalesOrderEntity> salesOrders = salesOrderService.getAllSalesOrdersByTenant(tenantId);
        List<SalesOrderResponse> responses = salesOrders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_USER')")
    @Operation(summary = "Get sales order by ID", description = "ID로 판매 주문 조회")
    public ResponseEntity<SalesOrderResponse> getSalesOrderById(@PathVariable Long id) {
        log.info("GET /api/sales-orders/{}", id);

        SalesOrderEntity salesOrder = salesOrderService.getSalesOrderById(id);
        return ResponseEntity.ok(toResponse(salesOrder));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_USER')")
    @Operation(summary = "Get sales orders by status", description = "상태별 판매 주문 조회")
    public ResponseEntity<List<SalesOrderResponse>> getSalesOrdersByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/sales-orders/status/{} - tenant: {}", status, tenantId);

        List<SalesOrderEntity> salesOrders = salesOrderService.getSalesOrdersByStatus(tenantId, status);
        List<SalesOrderResponse> responses = salesOrders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_USER')")
    @Operation(summary = "Get sales orders by customer", description = "고객별 판매 주문 조회")
    public ResponseEntity<List<SalesOrderResponse>> getSalesOrdersByCustomer(@PathVariable Long customerId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/sales-orders/customer/{} - tenant: {}", customerId, tenantId);

        List<SalesOrderEntity> salesOrders = salesOrderService.getSalesOrdersByCustomer(tenantId, customerId);
        List<SalesOrderResponse> responses = salesOrders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_USER')")
    @Operation(summary = "Get sales orders by date range", description = "기간별 판매 주문 조회")
    public ResponseEntity<List<SalesOrderResponse>> getSalesOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/sales-orders/date-range - tenant: {}, startDate: {}, endDate: {}", tenantId, startDate, endDate);

        List<SalesOrderEntity> salesOrders = salesOrderService.getSalesOrdersByDateRange(tenantId, startDate, endDate);
        List<SalesOrderResponse> responses = salesOrders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_USER')")
    @Operation(summary = "Create sales order", description = "판매 주문 생성")
    public ResponseEntity<SalesOrderResponse> createSalesOrder(@Valid @RequestBody SalesOrderCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/sales-orders - tenant: {}, orderNo: {}", tenantId, request.getOrderNo());

        SalesOrderEntity salesOrder = toEntity(request, tenantId);
        SalesOrderEntity created = salesOrderService.createSalesOrder(salesOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_USER')")
    @Operation(summary = "Update sales order", description = "판매 주문 수정")
    public ResponseEntity<SalesOrderResponse> updateSalesOrder(
            @PathVariable Long id,
            @Valid @RequestBody SalesOrderUpdateRequest request) {
        log.info("PUT /api/sales-orders/{}", id);

        String tenantId = TenantContext.getCurrentTenant();
        SalesOrderEntity salesOrder = toEntityForUpdate(request, tenantId);
        SalesOrderEntity updated = salesOrderService.updateSalesOrder(id, salesOrder);

        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    @Operation(summary = "Delete sales order", description = "판매 주문 삭제")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable Long id) {
        log.info("DELETE /api/sales-orders/{}", id);

        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    @Operation(summary = "Confirm sales order", description = "판매 주문 확정")
    public ResponseEntity<SalesOrderResponse> confirmSalesOrder(@PathVariable Long id) {
        log.info("POST /api/sales-orders/{}/confirm", id);

        SalesOrderEntity confirmed = salesOrderService.confirmSalesOrder(id);
        return ResponseEntity.ok(toResponse(confirmed));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    @Operation(summary = "Cancel sales order", description = "판매 주문 취소")
    public ResponseEntity<SalesOrderResponse> cancelSalesOrder(@PathVariable Long id) {
        log.info("POST /api/sales-orders/{}/cancel", id);

        SalesOrderEntity cancelled = salesOrderService.cancelSalesOrder(id);
        return ResponseEntity.ok(toResponse(cancelled));
    }

    // Helper methods

    private SalesOrderResponse toResponse(SalesOrderEntity entity) {
        List<SalesOrderItemResponse> itemResponses = entity.getItems() != null ?
                entity.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()) :
                List.of();

        return SalesOrderResponse.builder()
                .salesOrderId(entity.getSalesOrderId())
                .tenantId(entity.getTenant().getTenantId())
                .orderNo(entity.getOrderNo())
                .orderDate(entity.getOrderDate())
                .customerId(entity.getCustomer().getCustomerId())
                .customerCode(entity.getCustomer().getCustomerCode())
                .customerName(entity.getCustomer().getCustomerName())
                .requestedDeliveryDate(entity.getRequestedDeliveryDate())
                .deliveryAddress(entity.getDeliveryAddress())
                .paymentTerms(entity.getPaymentTerms())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .salesUserId(entity.getSalesUser().getUserId())
                .salesUserName(entity.getSalesUser().getUsername())
                .remarks(entity.getRemarks())
                .items(itemResponses)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SalesOrderItemResponse toItemResponse(SalesOrderItemEntity entity) {
        return SalesOrderItemResponse.builder()
                .salesOrderItemId(entity.getSalesOrderItemId())
                .lineNo(entity.getLineNo())
                .productId(entity.getProduct() != null ? entity.getProduct().getProductId() : null)
                .productCode(entity.getProduct() != null ? entity.getProduct().getProductCode() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getProductName() : null)
                .materialId(entity.getMaterial() != null ? entity.getMaterial().getMaterialId() : null)
                .materialCode(entity.getMaterial() != null ? entity.getMaterial().getMaterialCode() : null)
                .materialName(entity.getMaterial() != null ? entity.getMaterial().getMaterialName() : null)
                .orderedQuantity(entity.getOrderedQuantity())
                .deliveredQuantity(entity.getDeliveredQuantity())
                .unit(entity.getUnit())
                .unitPrice(entity.getUnitPrice())
                .amount(entity.getAmount())
                .requestedDate(entity.getRequestedDate())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SalesOrderEntity toEntity(SalesOrderCreateRequest request, String tenantId) {
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(tenantId);

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(request.getCustomerId());

        UserEntity salesUser = new UserEntity();
        salesUser.setUserId(request.getSalesUserId());

        SalesOrderEntity entity = SalesOrderEntity.builder()
                .tenant(tenant)
                .orderNo(request.getOrderNo())
                .orderDate(request.getOrderDate())
                .customer(customer)
                .requestedDeliveryDate(request.getRequestedDeliveryDate())
                .deliveryAddress(request.getDeliveryAddress())
                .paymentTerms(request.getPaymentTerms())
                .currency(request.getCurrency())
                .salesUser(salesUser)
                .remarks(request.getRemarks())
                .build();

        // Add items
        if (request.getItems() != null) {
            for (SalesOrderItemRequest itemRequest : request.getItems()) {
                SalesOrderItemEntity item = toItemEntity(itemRequest);
                entity.addItem(item);
            }
        }

        return entity;
    }

    private SalesOrderEntity toEntityForUpdate(SalesOrderUpdateRequest request, String tenantId) {
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(tenantId);

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(request.getCustomerId());

        SalesOrderEntity entity = SalesOrderEntity.builder()
                .tenant(tenant)
                .orderDate(request.getOrderDate())
                .customer(customer)
                .requestedDeliveryDate(request.getRequestedDeliveryDate())
                .deliveryAddress(request.getDeliveryAddress())
                .paymentTerms(request.getPaymentTerms())
                .currency(request.getCurrency())
                .remarks(request.getRemarks())
                .build();

        // Add items
        if (request.getItems() != null) {
            for (SalesOrderItemRequest itemRequest : request.getItems()) {
                SalesOrderItemEntity item = toItemEntity(itemRequest);
                entity.addItem(item);
            }
        }

        return entity;
    }

    private SalesOrderItemEntity toItemEntity(SalesOrderItemRequest request) {
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

        return SalesOrderItemEntity.builder()
                .lineNo(request.getLineNo())
                .product(product)
                .material(material)
                .orderedQuantity(request.getOrderedQuantity())
                .unit(request.getUnit())
                .unitPrice(request.getUnitPrice())
                .requestedDate(request.getRequestedDate())
                .remarks(request.getRemarks())
                .build();
    }
}
