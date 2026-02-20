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
import kr.co.softice.mes.domain.service.ShippingService;
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
 * Shipping Controller
 * 출하 관리 API
 *
 * 엔드포인트:
 * - GET /api/shippings - 출하 목록
 * - GET /api/shippings/{id} - 출하 상세
 * - GET /api/shippings/status/{status} - 상태별 조회
 * - GET /api/shippings/sales-order/{id} - 판매 주문별 조회
 * - POST /api/shippings - 출하 생성
 * - PUT /api/shippings/{id} - 출하 수정
 * - POST /api/shippings/{id}/process - 출하 처리 (재고 차감)
 * - POST /api/shippings/{id}/cancel - 출하 취소
 * - DELETE /api/shippings/{id} - 출하 삭제
 *
 * 워크플로우: PENDING → INSPECTING (optional) → SHIPPED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/shippings")
@RequiredArgsConstructor
@Tag(name = "Shipping Management", description = "출하 관리 API")
public class ShippingController {

    private final ShippingService shippingService;
    private final TenantRepository tenantRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * 출하 목록 조회
     * GET /api/shippings
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "출하 목록 조회", description = "테넌트의 모든 출하 조회")
    public ResponseEntity<ApiResponse<List<ShippingResponse>>> getShippings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long salesOrderId,
            @RequestParam(required = false) Long warehouseId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting shippings for tenant: {}, status: {}, SO: {}, warehouse: {}",
            tenantId, status, salesOrderId, warehouseId);

        List<ShippingEntity> shippings;

        if (status != null) {
            shippings = shippingService.findByStatus(tenantId, status);
        } else if (salesOrderId != null) {
            shippings = shippingService.findBySalesOrder(tenantId, salesOrderId);
        } else if (warehouseId != null) {
            shippings = shippingService.findByWarehouse(tenantId, warehouseId);
        } else {
            shippings = shippingService.findByTenant(tenantId);
        }

        List<ShippingResponse> responses = shippings.stream()
                .map(this::toShippingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("출하 목록 조회 성공", responses));
    }

    /**
     * 출하 상세 조회 (항목 포함)
     * GET /api/shippings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "출하 상세 조회", description = "출하 ID로 상세 정보 조회 (항목 포함)")
    public ResponseEntity<ApiResponse<ShippingResponse>> getShipping(@PathVariable Long id) {
        log.info("Getting shipping: {}", id);

        ShippingEntity shipping = shippingService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHIPPING_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("출하 조회 성공", toShippingResponse(shipping)));
    }

    /**
     * 상태별 출하 조회
     * GET /api/shippings/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 조회", description = "특정 상태의 출하 조회")
    public ResponseEntity<ApiResponse<List<ShippingResponse>>> getShippingsByStatus(
            @PathVariable String status) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting shippings by status: {} for tenant: {}", status, tenantId);

        List<ShippingEntity> shippings = shippingService.findByStatus(tenantId, status);
        List<ShippingResponse> responses = shippings.stream()
                .map(this::toShippingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("상태별 출하 조회 성공", responses));
    }

    /**
     * 판매 주문별 출하 조회
     * GET /api/shippings/sales-order/{id}
     */
    @GetMapping("/sales-order/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "판매 주문별 조회", description = "특정 판매 주문의 출하 조회")
    public ResponseEntity<ApiResponse<List<ShippingResponse>>> getShippingsBySalesOrder(
            @PathVariable Long id) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting shippings by sales order: {} for tenant: {}", id, tenantId);

        List<ShippingEntity> shippings = shippingService.findBySalesOrder(tenantId, id);
        List<ShippingResponse> responses = shippings.stream()
                .map(this::toShippingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("판매 주문별 출하 조회 성공", responses));
    }

    /**
     * 출하 생성
     * POST /api/shippings
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "출하 생성", description = "새로운 출하 생성")
    public ResponseEntity<ApiResponse<ShippingResponse>> createShipping(
            @Valid @RequestBody ShippingCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating shipping for tenant: {}, warehouse: {}",
            tenantId, request.getWarehouseId());

        // Resolve entities
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        WarehouseEntity warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        // Optional entities
        SalesOrderEntity salesOrder = null;
        if (request.getSalesOrderId() != null) {
            salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SALES_ORDER_NOT_FOUND));
        }

        CustomerEntity customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElse(null);
        }

        UserEntity shipper = null;
        if (request.getShipperUserId() != null) {
            shipper = userRepository.findById(request.getShipperUserId())
                    .orElse(null);
        }

        // Build shipping entity
        ShippingEntity shipping = ShippingEntity.builder()
                .tenant(tenant)
                .shippingNo(request.getShippingNo())
                .shippingDate(request.getShippingDate())
                .salesOrder(salesOrder)
                .customer(customer)
                .warehouse(warehouse)
                .shippingType(request.getShippingType())
                .shipper(shipper)
                .deliveryAddress(request.getDeliveryAddress())
                .trackingNumber(request.getTrackingNumber())
                .carrierName(request.getCarrierName())
                .remarks(request.getRemarks())
                .build();

        // Add items
        if (request.getItems() != null) {
            for (ShippingItemRequest itemReq : request.getItems()) {
                ProductEntity product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

                SalesOrderItemEntity salesOrderItem = null;
                if (itemReq.getSalesOrderItemId() != null) {
                    salesOrderItem = salesOrderItemRepository.findById(itemReq.getSalesOrderItemId())
                            .orElse(null);
                }

                ShippingItemEntity item = ShippingItemEntity.builder()
                        .salesOrderItem(salesOrderItem)
                        .product(product)
                        .orderedQuantity(itemReq.getOrderedQuantity())
                        .shippedQuantity(itemReq.getShippedQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .lotNo(itemReq.getLotNo())
                        .expiryDate(itemReq.getExpiryDate())
                        .inspectionStatus(itemReq.getInspectionStatus())
                        .remarks(itemReq.getRemarks())
                        .build();

                shipping.addItem(item);
            }
        }

        // Create shipping
        ShippingEntity created = shippingService.createShipping(tenantId, shipping);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("출하 생성 성공", toShippingResponse(created)));
    }

    /**
     * 출하 수정
     * PUT /api/shippings/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "출하 수정", description = "출하 수정 (PENDING 상태만 가능)")
    public ResponseEntity<ApiResponse<ShippingResponse>> updateShipping(
            @PathVariable Long id,
            @Valid @RequestBody ShippingUpdateRequest request) {

        log.info("Updating shipping: {}", id);

        // Build updates
        ShippingEntity updates = ShippingEntity.builder()
                .shippingDate(request.getShippingDate())
                .deliveryAddress(request.getDeliveryAddress())
                .trackingNumber(request.getTrackingNumber())
                .carrierName(request.getCarrierName())
                .remarks(request.getRemarks())
                .build();

        ShippingEntity updated = shippingService.updateShipping(id, updates);

        return ResponseEntity.ok(ApiResponse.success("출하 수정 성공", toShippingResponse(updated)));
    }

    /**
     * 출하 처리 (재고 차감)
     * POST /api/shippings/{id}/process
     */
    @PostMapping("/{id}/process")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "출하 처리", description = "출하 처리 및 재고 차감 (PENDING → SHIPPED)")
    public ResponseEntity<ApiResponse<ShippingResponse>> processShipping(@PathVariable Long id) {
        log.info("Processing shipping: {}", id);

        ShippingEntity processed = shippingService.processShipping(id);

        return ResponseEntity.ok(ApiResponse.success("출하 처리 성공", toShippingResponse(processed)));
    }

    /**
     * 출하 취소
     * POST /api/shippings/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "출하 취소", description = "출하 취소")
    public ResponseEntity<ApiResponse<ShippingResponse>> cancelShipping(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        log.info("Cancelling shipping: {}", id);

        ShippingEntity cancelled = shippingService.cancelShipping(id, reason);

        return ResponseEntity.ok(ApiResponse.success("출하 취소 성공", toShippingResponse(cancelled)));
    }

    /**
     * 출하 삭제
     * DELETE /api/shippings/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "출하 삭제", description = "출하 삭제 (PENDING 또는 CANCELLED만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteShipping(@PathVariable Long id) {
        log.info("Deleting shipping: {}", id);

        shippingService.deleteShipping(id);

        return ResponseEntity.ok(ApiResponse.success("출하 삭제 성공", null));
    }

    // === Helper Methods ===

    /**
     * Convert ShippingEntity to ShippingResponse
     */
    private ShippingResponse toShippingResponse(ShippingEntity shipping) {
        return ShippingResponse.builder()
                .shippingId(shipping.getShippingId())
                .tenantId(shipping.getTenant().getTenantId())
                .tenantName(shipping.getTenant().getTenantName())
                .shippingNo(shipping.getShippingNo())
                .shippingDate(shipping.getShippingDate())
                .shippingType(shipping.getShippingType())
                .shippingStatus(shipping.getShippingStatus())
                .salesOrderId(shipping.getSalesOrder() != null ? shipping.getSalesOrder().getSalesOrderId() : null)
                .salesOrderNo(shipping.getSalesOrder() != null ? shipping.getSalesOrder().getOrderNo() : null)
                .customerId(shipping.getCustomer() != null ? shipping.getCustomer().getCustomerId() : null)
                .customerCode(shipping.getCustomer() != null ? shipping.getCustomer().getCustomerCode() : null)
                .customerName(shipping.getCustomer() != null ? shipping.getCustomer().getCustomerName() : null)
                .warehouseId(shipping.getWarehouse().getWarehouseId())
                .warehouseCode(shipping.getWarehouse().getWarehouseCode())
                .warehouseName(shipping.getWarehouse().getWarehouseName())
                .shipperUserId(shipping.getShipper() != null ? shipping.getShipper().getUserId() : null)
                .shipperUserName(shipping.getShipper() != null ? shipping.getShipper().getUsername() : null)
                .shipperName(shipping.getShipperName())
                .deliveryAddress(shipping.getDeliveryAddress())
                .trackingNumber(shipping.getTrackingNumber())
                .carrierName(shipping.getCarrierName())
                .totalQuantity(shipping.getTotalQuantity())
                .totalAmount(shipping.getTotalAmount())
                .items(shipping.getItems().stream()
                        .map(this::toShippingItemResponse)
                        .collect(Collectors.toList()))
                .remarks(shipping.getRemarks())
                .isActive(shipping.getIsActive())
                .createdAt(shipping.getCreatedAt())
                .createdBy(shipping.getCreatedBy())
                .updatedAt(shipping.getUpdatedAt())
                .updatedBy(shipping.getUpdatedBy())
                .build();
    }

    /**
     * Convert ShippingItemEntity to ShippingItemResponse
     */
    private ShippingItemResponse toShippingItemResponse(ShippingItemEntity item) {
        return ShippingItemResponse.builder()
                .shippingItemId(item.getShippingItemId())
                .salesOrderItemId(item.getSalesOrderItem() != null ? item.getSalesOrderItem().getSalesOrderItemId() : null)
                .productId(item.getProduct().getProductId())
                .productCode(item.getProductCode())
                .productName(item.getProductName())
                .orderedQuantity(item.getOrderedQuantity())
                .shippedQuantity(item.getShippedQuantity())
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .lotNo(item.getLotNo())
                .expiryDate(item.getExpiryDate())
                .inspectionStatus(item.getInspectionStatus())
                .qualityInspectionId(item.getQualityInspection() != null ? item.getQualityInspection().getQualityInspectionId() : null)
                .inspectionResult(item.getQualityInspection() != null ? item.getQualityInspection().getInspectionResult() : null)
                .remarks(item.getRemarks())
                .build();
    }
}
