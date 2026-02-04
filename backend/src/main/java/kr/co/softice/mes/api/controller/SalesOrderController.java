package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.sales.*;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sales Order Controller
 * 판매 주문 관리 API
 *
 * 엔드포인트:
 * - GET /api/sales-orders - 판매 주문 목록
 * - GET /api/sales-orders/{id} - 판매 주문 상세
 * - GET /api/sales-orders/status/{status} - 상태별 조회
 * - GET /api/sales-orders/customer/{customerId} - 고객별 조회
 * - POST /api/sales-orders - 판매 주문 생성
 * - PUT /api/sales-orders/{id} - 판매 주문 수정
 * - POST /api/sales-orders/{id}/confirm - 주문 확정
 * - POST /api/sales-orders/{id}/cancel - 주문 취소
 * - DELETE /api/sales-orders/{id} - 판매 주문 삭제
 *
 * 워크플로우: DRAFT → CONFIRMED → PARTIALLY_DELIVERED → DELIVERED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@Tag(name = "Sales Order Management", description = "판매 주문 관리 API")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;

    /**
     * 판매 주문 목록 조회
     * GET /api/sales-orders
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "판매 주문 목록 조회", description = "테넌트의 모든 판매 주문 조회")
    public ResponseEntity<ApiResponse<List<SalesOrderResponse>>> getSalesOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting sales orders for tenant: {}, status: {}, customer: {}",
            tenantId, status, customerId);

        List<SalesOrderEntity> orders;

        if (status != null) {
            orders = salesOrderService.findByStatus(tenantId, status);
        } else if (customerId != null) {
            orders = salesOrderService.findByCustomer(tenantId, customerId);
        } else {
            orders = salesOrderService.findByTenant(tenantId);
        }

        List<SalesOrderResponse> responses = orders.stream()
                .map(this::toSalesOrderResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("판매 주문 목록 조회 성공", responses));
    }

    /**
     * 판매 주문 상세 조회 (항목 포함)
     * GET /api/sales-orders/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "판매 주문 상세 조회", description = "판매 주문 ID로 상세 정보 조회 (항목 포함)")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> getSalesOrder(@PathVariable Long id) {
        log.info("Getting sales order: {}", id);

        SalesOrderEntity order = salesOrderService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SALES_ORDER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("판매 주문 조회 성공", toSalesOrderResponse(order)));
    }

    /**
     * 상태별 판매 주문 조회
     * GET /api/sales-orders/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 조회", description = "특정 상태의 판매 주문 조회")
    public ResponseEntity<ApiResponse<List<SalesOrderResponse>>> getSalesOrdersByStatus(
            @PathVariable String status) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting sales orders by status: {} for tenant: {}", status, tenantId);

        List<SalesOrderEntity> orders = salesOrderService.findByStatus(tenantId, status);
        List<SalesOrderResponse> responses = orders.stream()
                .map(this::toSalesOrderResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("상태별 판매 주문 조회 성공", responses));
    }

    /**
     * 고객별 판매 주문 조회
     * GET /api/sales-orders/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "고객별 조회", description = "특정 고객의 판매 주문 조회")
    public ResponseEntity<ApiResponse<List<SalesOrderResponse>>> getSalesOrdersByCustomer(
            @PathVariable Long customerId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting sales orders by customer: {} for tenant: {}", customerId, tenantId);

        List<SalesOrderEntity> orders = salesOrderService.findByCustomer(tenantId, customerId);
        List<SalesOrderResponse> responses = orders.stream()
                .map(this::toSalesOrderResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("고객별 판매 주문 조회 성공", responses));
    }

    /**
     * 판매 주문 생성
     * POST /api/sales-orders
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "판매 주문 생성", description = "새로운 판매 주문 생성")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> createSalesOrder(
            @Valid @RequestBody SalesOrderCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating sales order for tenant: {}, customer: {}",
            tenantId, request.getCustomerId());

        // Resolve entities
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));

        UserEntity salesUser = userRepository.findById(request.getSalesUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        // Build sales order entity
        SalesOrderEntity salesOrder = SalesOrderEntity.builder()
                .tenant(tenant)
                .orderNo(request.getOrderNo())
                .orderDate(request.getOrderDate())
                .customer(customer)
                .salesUser(salesUser)
                .requestedDeliveryDate(request.getRequestedDeliveryDate())
                .deliveryAddress(request.getDeliveryAddress())
                .paymentTerms(request.getPaymentTerms())
                .currency(request.getCurrency())
                .remarks(request.getRemarks())
                .build();

        // Add items
        if (request.getItems() != null) {
            for (SalesOrderItemRequest itemReq : request.getItems()) {
                ProductEntity product = null;
                MaterialEntity material = null;

                if (itemReq.getProductId() != null) {
                    product = productRepository.findById(itemReq.getProductId())
                            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
                } else if (itemReq.getMaterialId() != null) {
                    material = materialRepository.findById(itemReq.getMaterialId())
                            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATERIAL_NOT_FOUND));
                }

                SalesOrderItemEntity item = SalesOrderItemEntity.builder()
                        .product(product)
                        .material(material)
                        .orderedQuantity(itemReq.getOrderedQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .remarks(itemReq.getRemarks())
                        .build();

                salesOrder.addItem(item);
            }
        }

        // Create sales order
        SalesOrderEntity created = salesOrderService.createSalesOrder(tenantId, salesOrder);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("판매 주문 생성 성공", toSalesOrderResponse(created)));
    }

    /**
     * 판매 주문 수정
     * PUT /api/sales-orders/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "판매 주문 수정", description = "판매 주문 수정 (DRAFT 상태만 가능)")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> updateSalesOrder(
            @PathVariable Long id,
            @Valid @RequestBody SalesOrderUpdateRequest request) {

        log.info("Updating sales order: {}", id);

        // Build updates
        SalesOrderEntity updates = SalesOrderEntity.builder()
                .orderDate(request.getOrderDate())
                .requestedDeliveryDate(request.getRequestedDeliveryDate())
                .deliveryAddress(request.getDeliveryAddress())
                .paymentTerms(request.getPaymentTerms())
                .currency(request.getCurrency())
                .remarks(request.getRemarks())
                .build();

        // Resolve optional entities
        if (request.getCustomerId() != null) {
            CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));
            updates.setCustomer(customer);
        }

        // Add items if provided
        if (request.getItems() != null) {
            for (SalesOrderItemRequest itemReq : request.getItems()) {
                ProductEntity product = null;
                MaterialEntity material = null;

                if (itemReq.getProductId() != null) {
                    product = productRepository.findById(itemReq.getProductId())
                            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
                } else if (itemReq.getMaterialId() != null) {
                    material = materialRepository.findById(itemReq.getMaterialId())
                            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATERIAL_NOT_FOUND));
                }

                SalesOrderItemEntity item = SalesOrderItemEntity.builder()
                        .product(product)
                        .material(material)
                        .orderedQuantity(itemReq.getOrderedQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .remarks(itemReq.getRemarks())
                        .build();

                updates.addItem(item);
            }
        }

        SalesOrderEntity updated = salesOrderService.updateSalesOrder(id, updates);

        return ResponseEntity.ok(ApiResponse.success("판매 주문 수정 성공", toSalesOrderResponse(updated)));
    }

    /**
     * 판매 주문 확정
     * POST /api/sales-orders/{id}/confirm
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "주문 확정", description = "판매 주문 확정 (DRAFT → CONFIRMED)")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> confirmSalesOrder(@PathVariable Long id) {
        log.info("Confirming sales order: {}", id);

        SalesOrderEntity confirmed = salesOrderService.confirmSalesOrder(id);

        return ResponseEntity.ok(ApiResponse.success("판매 주문 확정 성공", toSalesOrderResponse(confirmed)));
    }

    /**
     * 판매 주문 취소
     * POST /api/sales-orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "주문 취소", description = "판매 주문 취소")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> cancelSalesOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        log.info("Cancelling sales order: {}", id);

        SalesOrderEntity cancelled = salesOrderService.cancelSalesOrder(id, reason);

        return ResponseEntity.ok(ApiResponse.success("판매 주문 취소 성공", toSalesOrderResponse(cancelled)));
    }

    /**
     * 판매 주문 삭제
     * DELETE /api/sales-orders/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "판매 주문 삭제", description = "판매 주문 삭제 (DRAFT 또는 CANCELLED만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteSalesOrder(@PathVariable Long id) {
        log.info("Deleting sales order: {}", id);

        salesOrderService.deleteSalesOrder(id);

        return ResponseEntity.ok(ApiResponse.success("판매 주문 삭제 성공", null));
    }

    // === Helper Methods ===

    /**
     * Convert SalesOrderEntity to SalesOrderResponse
     */
    private SalesOrderResponse toSalesOrderResponse(SalesOrderEntity order) {
        return SalesOrderResponse.builder()
                .salesOrderId(order.getSalesOrderId())
                .tenantId(order.getTenant().getTenantId())
                .orderNo(order.getOrderNo())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .customerId(order.getCustomer().getCustomerId())
                .customerCode(order.getCustomer().getCustomerCode())
                .customerName(order.getCustomer().getCustomerName())
                .salesUserId(order.getSalesUser().getUserId())
                .salesUserName(order.getSalesUser().getUsername())
                .requestedDeliveryDate(order.getRequestedDeliveryDate())
                .deliveryAddress(order.getDeliveryAddress())
                .paymentTerms(order.getPaymentTerms())
                .currency(order.getCurrency())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(this::toSalesOrderItemResponse)
                        .collect(Collectors.toList()))
                .remarks(order.getRemarks())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * Convert SalesOrderItemEntity to SalesOrderItemResponse
     */
    private SalesOrderItemResponse toSalesOrderItemResponse(SalesOrderItemEntity item) {
        return SalesOrderItemResponse.builder()
                .salesOrderItemId(item.getSalesOrderItemId())
                .productId(item.getProduct() != null ? item.getProduct().getProductId() : null)
                .productCode(item.getProduct() != null ? item.getProduct().getProductCode() : null)
                .productName(item.getProduct() != null ? item.getProduct().getProductName() : null)
                .materialId(item.getMaterial() != null ? item.getMaterial().getMaterialId() : null)
                .orderedQuantity(item.getOrderedQuantity())
                .deliveredQuantity(item.getDeliveredQuantity() != null ? item.getDeliveredQuantity() : BigDecimal.ZERO)
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .requestedDate(item.getRequestedDate())
                .remarks(item.getRemarks())
                .build();
    }
}
