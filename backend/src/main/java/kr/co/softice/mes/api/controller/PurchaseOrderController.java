package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.purchase.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Purchase Order Controller
 * 구매 주문 컨트롤러
 *
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Purchase Order Management", description = "구매 주문 관리 API")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    /**
     * 모든 구매 주문 조회
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모든 구매 주문 조회", description = "테넌트의 모든 구매 주문을 조회합니다")
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/purchase-orders - tenant: {}", tenantId);

        List<PurchaseOrderEntity> orders = purchaseOrderService.getAllPurchaseOrders(tenantId);
        List<PurchaseOrderResponse> responses = orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 상태별 구매 주문 조회
     */
    @Transactional(readOnly = true)
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 구매 주문 조회", description = "특정 상태의 구매 주문을 조회합니다")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrdersByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/purchase-orders/status/{} - tenant: {}", status, tenantId);

        List<PurchaseOrderEntity> orders = purchaseOrderService.getPurchaseOrdersByStatus(tenantId, status);
        List<PurchaseOrderResponse> responses = orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 공급업체별 구매 주문 조회
     */
    @Transactional(readOnly = true)
    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "공급업체별 구매 주문 조회", description = "특정 공급업체의 구매 주문을 조회합니다")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrdersBySupplier(@PathVariable Long supplierId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/purchase-orders/supplier/{} - tenant: {}", supplierId, tenantId);

        List<PurchaseOrderEntity> orders = purchaseOrderService.getPurchaseOrdersBySupplier(tenantId, supplierId);
        List<PurchaseOrderResponse> responses = orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 구매 주문 ID로 조회
     */
    @Transactional(readOnly = true)
    @GetMapping("/{purchaseOrderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "구매 주문 조회", description = "구매 주문 ID로 구매 주문을 조회합니다")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long purchaseOrderId) {
        log.info("GET /api/purchase-orders/{}", purchaseOrderId);

        PurchaseOrderEntity order = purchaseOrderService.getPurchaseOrderById(purchaseOrderId);
        return ResponseEntity.ok(toResponse(order));
    }

    /**
     * 구매 주문 생성
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    @Operation(summary = "구매 주문 생성", description = "새로운 구매 주문을 생성합니다")
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/purchase-orders - tenant: {}, orderNo: {}", tenantId, request.getOrderNo());

        PurchaseOrderEntity purchaseOrder = toEntity(request);
        PurchaseOrderEntity created = purchaseOrderService.createPurchaseOrder(tenantId, purchaseOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    /**
     * 구매 주문 수정
     */
    @PutMapping("/{purchaseOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    @Operation(summary = "구매 주문 수정", description = "기존 구매 주문을 수정합니다")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @Valid @RequestBody PurchaseOrderUpdateRequest request) {
        log.info("PUT /api/purchase-orders/{}", purchaseOrderId);

        PurchaseOrderEntity updatedOrder = toUpdateEntity(request);
        PurchaseOrderEntity updated = purchaseOrderService.updatePurchaseOrder(purchaseOrderId, updatedOrder);

        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * 구매 주문 확정
     */
    @PostMapping("/{purchaseOrderId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    @Operation(summary = "구매 주문 확정", description = "구매 주문을 확정합니다")
    public ResponseEntity<PurchaseOrderResponse> confirmPurchaseOrder(@PathVariable Long purchaseOrderId) {
        log.info("POST /api/purchase-orders/{}/confirm", purchaseOrderId);

        PurchaseOrderEntity confirmed = purchaseOrderService.confirmPurchaseOrder(purchaseOrderId);
        return ResponseEntity.ok(toResponse(confirmed));
    }

    /**
     * 구매 주문 취소
     */
    @PostMapping("/{purchaseOrderId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    @Operation(summary = "구매 주문 취소", description = "구매 주문을 취소합니다")
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(@PathVariable Long purchaseOrderId) {
        log.info("POST /api/purchase-orders/{}/cancel", purchaseOrderId);

        PurchaseOrderEntity cancelled = purchaseOrderService.cancelPurchaseOrder(purchaseOrderId);
        return ResponseEntity.ok(toResponse(cancelled));
    }

    /**
     * 구매 주문 삭제
     */
    @DeleteMapping("/{purchaseOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    @Operation(summary = "구매 주문 삭제", description = "구매 주문을 삭제합니다")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long purchaseOrderId) {
        log.info("DELETE /api/purchase-orders/{}", purchaseOrderId);

        purchaseOrderService.deletePurchaseOrder(purchaseOrderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private PurchaseOrderResponse toResponse(PurchaseOrderEntity entity) {
        List<PurchaseOrderItemResponse> itemResponses = entity.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return PurchaseOrderResponse.builder()
                .purchaseOrderId(entity.getPurchaseOrderId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .orderNo(entity.getOrderNo())
                .orderDate(entity.getOrderDate())
                .supplierId(entity.getSupplier().getSupplierId())
                .supplierCode(entity.getSupplier().getSupplierCode())
                .supplierName(entity.getSupplier().getSupplierName())
                .expectedDeliveryDate(entity.getExpectedDeliveryDate())
                .deliveryAddress(entity.getDeliveryAddress())
                .paymentTerms(entity.getPaymentTerms())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .buyerUserId(entity.getBuyer().getUserId())
                .buyerUsername(entity.getBuyer().getUsername())
                .buyerFullName(entity.getBuyer().getFullName())
                .remarks(entity.getRemarks())
                .items(itemResponses)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Item Entity를 Response DTO로 변환
     */
    private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItemEntity entity) {
        return PurchaseOrderItemResponse.builder()
                .purchaseOrderItemId(entity.getPurchaseOrderItemId())
                .lineNo(entity.getLineNo())
                .materialId(entity.getMaterial().getMaterialId())
                .materialCode(entity.getMaterial().getMaterialCode())
                .materialName(entity.getMaterial().getMaterialName())
                .orderedQuantity(entity.getOrderedQuantity())
                .receivedQuantity(entity.getReceivedQuantity())
                .unit(entity.getUnit())
                .unitPrice(entity.getUnitPrice())
                .amount(entity.getAmount())
                .requiredDate(entity.getRequiredDate())
                .purchaseRequestId(entity.getPurchaseRequest() != null ? entity.getPurchaseRequest().getPurchaseRequestId() : null)
                .purchaseRequestNo(entity.getPurchaseRequest() != null ? entity.getPurchaseRequest().getRequestNo() : null)
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * CreateRequest를 Entity로 변환
     */
    private PurchaseOrderEntity toEntity(PurchaseOrderCreateRequest request) {
        PurchaseOrderEntity.PurchaseOrderEntityBuilder builder = PurchaseOrderEntity.builder()
                .orderNo(request.getOrderNo())
                .supplier(SupplierEntity.builder().supplierId(request.getSupplierId()).build())
                .buyer(UserEntity.builder().userId(request.getBuyerUserId()).build())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .deliveryAddress(request.getDeliveryAddress())
                .paymentTerms(request.getPaymentTerms())
                .currency(request.getCurrency())
                .remarks(request.getRemarks());

        // Add items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<PurchaseOrderItemEntity> items = new ArrayList<>();
            for (PurchaseOrderItemRequest itemReq : request.getItems()) {
                PurchaseOrderItemEntity item = PurchaseOrderItemEntity.builder()
                        .lineNo(itemReq.getLineNo())
                        .material(MaterialEntity.builder().materialId(itemReq.getMaterialId()).build())
                        .orderedQuantity(itemReq.getOrderedQuantity())
                        .unit(itemReq.getUnit())
                        .unitPrice(itemReq.getUnitPrice())
                        .requiredDate(itemReq.getRequiredDate())
                        .remarks(itemReq.getRemarks())
                        .build();

                if (itemReq.getPurchaseRequestId() != null) {
                    item.setPurchaseRequest(PurchaseRequestEntity.builder()
                            .purchaseRequestId(itemReq.getPurchaseRequestId()).build());
                }

                items.add(item);
            }
            builder.items(items);
        }

        return builder.build();
    }

    /**
     * UpdateRequest를 Entity로 변환
     */
    private PurchaseOrderEntity toUpdateEntity(PurchaseOrderUpdateRequest request) {
        PurchaseOrderEntity.PurchaseOrderEntityBuilder builder = PurchaseOrderEntity.builder()
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .deliveryAddress(request.getDeliveryAddress())
                .paymentTerms(request.getPaymentTerms())
                .currency(request.getCurrency())
                .remarks(request.getRemarks());

        // Add items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<PurchaseOrderItemEntity> items = new ArrayList<>();
            for (PurchaseOrderItemRequest itemReq : request.getItems()) {
                PurchaseOrderItemEntity item = PurchaseOrderItemEntity.builder()
                        .lineNo(itemReq.getLineNo())
                        .material(MaterialEntity.builder().materialId(itemReq.getMaterialId()).build())
                        .orderedQuantity(itemReq.getOrderedQuantity())
                        .unit(itemReq.getUnit())
                        .unitPrice(itemReq.getUnitPrice())
                        .requiredDate(itemReq.getRequiredDate())
                        .remarks(itemReq.getRemarks())
                        .build();

                items.add(item);
            }
            builder.items(items);
        }

        return builder.build();
    }
}
