package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.wms.GoodsReceiptCreateRequest;
import kr.co.softice.mes.common.dto.wms.GoodsReceiptItemRequest;
import kr.co.softice.mes.common.dto.wms.GoodsReceiptItemResponse;
import kr.co.softice.mes.common.dto.wms.GoodsReceiptResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.GoodsReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Goods Receipt Controller
 * 입하 관리 API
 *
 * 엔드포인트:
 * - GET /api/goods-receipts - 입하 목록
 * - GET /api/goods-receipts/{id} - 입하 상세 (항목 포함)
 * - POST /api/goods-receipts - 입하 생성
 * - PUT /api/goods-receipts/{id} - 입하 수정
 * - POST /api/goods-receipts/{id}/complete - 입하 완료
 * - POST /api/goods-receipts/{id}/cancel - 입하 취소
 *
 * 권한: WAREHOUSE_MANAGER, INVENTORY_CLERK
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/goods-receipts")
@RequiredArgsConstructor
@Tag(name = "Goods Receipt Management", description = "입하 관리 API")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;
    private final TenantRepository tenantRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    /**
     * 입하 목록 조회
     * GET /api/goods-receipts
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "입하 목록 조회", description = "테넌트의 모든 입하 조회")
    public ResponseEntity<ApiResponse<List<GoodsReceiptResponse>>> getGoodsReceipts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long purchaseOrderId,
            @RequestParam(required = false) Long warehouseId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting goods receipts for tenant: {}, status: {}, PO: {}, warehouse: {}",
            tenantId, status, purchaseOrderId, warehouseId);

        List<GoodsReceiptEntity> receipts;

        if (status != null) {
            receipts = goodsReceiptService.findByStatus(tenantId, status);
        } else if (purchaseOrderId != null) {
            receipts = goodsReceiptService.findByPurchaseOrderId(tenantId, purchaseOrderId);
        } else if (warehouseId != null) {
            receipts = goodsReceiptService.findByWarehouseId(tenantId, warehouseId);
        } else {
            receipts = goodsReceiptService.findByTenant(tenantId);
        }

        List<GoodsReceiptResponse> responses = receipts.stream()
                .map(this::toGoodsReceiptResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("입하 목록 조회 성공", responses));
    }

    /**
     * 입하 상세 조회 (항목 포함)
     * GET /api/goods-receipts/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "입하 상세 조회", description = "입하 ID로 상세 정보 조회 (항목 포함)")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> getGoodsReceipt(@PathVariable Long id) {
        log.info("Getting goods receipt: {}", id);

        GoodsReceiptEntity receipt = goodsReceiptService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.GOODS_RECEIPT_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("입하 조회 성공", toGoodsReceiptResponse(receipt)));
    }

    /**
     * 날짜 범위별 입하 조회
     * GET /api/goods-receipts/date-range
     */
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "날짜 범위별 입하 조회", description = "시작일과 종료일 범위 내의 입하 조회")
    public ResponseEntity<ApiResponse<List<GoodsReceiptResponse>>> getGoodsReceiptsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting goods receipts for tenant: {} between {} and {}",
            tenantId, startDate, endDate);

        List<GoodsReceiptEntity> receipts = goodsReceiptService.findByDateRange(tenantId, startDate, endDate);
        List<GoodsReceiptResponse> responses = receipts.stream()
                .map(this::toGoodsReceiptResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("날짜별 입하 조회 성공", responses));
    }

    /**
     * 입하 생성
     * POST /api/goods-receipts
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_CLERK')")
    @Operation(summary = "입하 생성", description = "신규 입하 등록 (WAREHOUSE_MANAGER 권한 필요)")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> createGoodsReceipt(
            @Valid @RequestBody GoodsReceiptCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating goods receipt for tenant: {}, receiptNo: {}", tenantId, request.getReceiptNo());

        // Validate and fetch entities
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        WarehouseEntity warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        PurchaseOrderEntity purchaseOrder = null;
        if (request.getPurchaseOrderId() != null) {
            purchaseOrder = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PURCHASE_ORDER_NOT_FOUND));
        }

        SupplierEntity supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SUPPLIER_NOT_FOUND));
        } else if (purchaseOrder != null) {
            supplier = purchaseOrder.getSupplier();
        }

        UserEntity receiver = null;
        if (request.getReceiverUserId() != null) {
            receiver = userRepository.findById(request.getReceiverUserId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        }

        // Build goods receipt entity
        GoodsReceiptEntity goodsReceipt = GoodsReceiptEntity.builder()
                .tenant(tenant)
                .receiptNo(request.getReceiptNo())
                .receiptDate(request.getReceiptDate())
                .purchaseOrder(purchaseOrder)
                .supplier(supplier)
                .warehouse(warehouse)
                .receiptType(request.getReceiptType())
                .receiptStatus("PENDING")
                .receiver(receiver)
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        // Build items
        for (GoodsReceiptItemRequest itemReq : request.getItems()) {
            ProductEntity product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

            PurchaseOrderItemEntity purchaseOrderItem = null;
            BigDecimal unitPrice = null;
            BigDecimal orderedQuantity = null;

            if (itemReq.getPurchaseOrderItemId() != null) {
                purchaseOrderItem = purchaseOrderItemRepository.findById(itemReq.getPurchaseOrderItemId())
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PURCHASE_ORDER_ITEM_NOT_FOUND));
                unitPrice = purchaseOrderItem.getUnitPrice();
                orderedQuantity = purchaseOrderItem.getOrderedQuantity();
            }

            GoodsReceiptItemEntity item = GoodsReceiptItemEntity.builder()
                    .goodsReceipt(goodsReceipt)
                    .purchaseOrderItem(purchaseOrderItem)
                    .product(product)
                    .productCode(product.getProductCode())
                    .productName(product.getProductName())
                    .orderedQuantity(orderedQuantity)
                    .receivedQuantity(itemReq.getReceivedQuantity())
                    .unitPrice(unitPrice)
                    .lineAmount(unitPrice != null ? unitPrice.multiply(itemReq.getReceivedQuantity()) : null)
                    .lotNo(itemReq.getLotNo())
                    .expiryDate(itemReq.getExpiryDate())
                    .inspectionStatus(itemReq.getInspectionStatus())
                    .remarks(itemReq.getRemarks())
                    .build();

            goodsReceipt.addItem(item);
        }

        // Create goods receipt
        GoodsReceiptEntity created = goodsReceiptService.createGoodsReceipt(goodsReceipt);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("입하 생성 성공", toGoodsReceiptResponse(created)));
    }

    /**
     * 입하 수정
     * PUT /api/goods-receipts/{id}
     *
     * Note: PENDING 상태의 입하만 수정 가능
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_CLERK')")
    @Operation(summary = "입하 수정", description = "기존 입하 정보 수정 (PENDING 상태만 가능)")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> updateGoodsReceipt(
            @PathVariable Long id,
            @Valid @RequestBody GoodsReceiptCreateRequest request) {

        log.info("Updating goods receipt: {}", id);

        GoodsReceiptEntity existing = goodsReceiptService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.GOODS_RECEIPT_NOT_FOUND));

        // Only PENDING receipts can be updated
        if (!"PENDING".equals(existing.getReceiptStatus())) {
            throw new IllegalStateException("Cannot update goods receipt in status: " + existing.getReceiptStatus());
        }

        // Update basic fields
        existing.setReceiptDate(request.getReceiptDate());
        existing.setReceiptType(request.getReceiptType());
        existing.setRemarks(request.getRemarks());

        // Update warehouse if changed
        if (request.getWarehouseId() != null &&
            !request.getWarehouseId().equals(existing.getWarehouse().getWarehouseId())) {
            WarehouseEntity warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));
            existing.setWarehouse(warehouse);
        }

        // Note: For simplicity, not updating items here
        // In production, you would need to handle item updates/deletes

        GoodsReceiptEntity updated = goodsReceiptService.updateGoodsReceipt(existing);

        return ResponseEntity.ok(ApiResponse.success("입하 수정 성공", toGoodsReceiptResponse(updated)));
    }

    /**
     * 입하 완료
     * POST /api/goods-receipts/{id}/complete
     *
     * 워크플로우:
     * - 품질 검사 결과 확인
     * - 합격품: 가용 재고로 추가
     * - 불합격품: 격리 창고로 이동
     * - 상태 → COMPLETED
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "입하 완료", description = "입하 프로세스 완료 (품질 검사 후)")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> completeGoodsReceipt(@PathVariable Long id) {
        log.info("Completing goods receipt: {}", id);

        GoodsReceiptEntity completed = goodsReceiptService.completeGoodsReceipt(id, null);

        return ResponseEntity.ok(ApiResponse.success("입하 완료 성공", toGoodsReceiptResponse(completed)));
    }

    /**
     * 입하 취소
     * POST /api/goods-receipts/{id}/cancel
     *
     * 워크플로우:
     * - 재고 이동 역처리
     * - LOT 비활성화
     * - 상태 → CANCELLED
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "입하 취소", description = "입하 취소 (재고 역처리)")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> cancelGoodsReceipt(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        log.info("Cancelling goods receipt: {}, reason: {}", id, reason);

        GoodsReceiptEntity cancelled = goodsReceiptService.cancelGoodsReceipt(id,
            reason != null ? reason : "Cancelled by user");

        return ResponseEntity.ok(ApiResponse.success("입하 취소 성공", toGoodsReceiptResponse(cancelled)));
    }

    // ================== Private Helper Methods ==================

    /**
     * Convert entity to response DTO
     */
    private GoodsReceiptResponse toGoodsReceiptResponse(GoodsReceiptEntity entity) {
        return GoodsReceiptResponse.builder()
                .goodsReceiptId(entity.getGoodsReceiptId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .receiptNo(entity.getReceiptNo())
                .receiptDate(entity.getReceiptDate())
                .receiptType(entity.getReceiptType())
                .receiptStatus(entity.getReceiptStatus())
                // Purchase Order
                .purchaseOrderId(entity.getPurchaseOrder() != null ?
                    entity.getPurchaseOrder().getPurchaseOrderId() : null)
                .purchaseOrderNo(entity.getPurchaseOrder() != null ?
                    entity.getPurchaseOrder().getOrderNo() : null)
                // Supplier
                .supplierId(entity.getSupplier() != null ? entity.getSupplier().getSupplierId() : null)
                .supplierCode(entity.getSupplier() != null ? entity.getSupplier().getSupplierCode() : null)
                .supplierName(entity.getSupplier() != null ? entity.getSupplier().getSupplierName() : null)
                // Warehouse
                .warehouseId(entity.getWarehouse().getWarehouseId())
                .warehouseCode(entity.getWarehouse().getWarehouseCode())
                .warehouseName(entity.getWarehouse().getWarehouseName())
                // Receiver
                .receiverUserId(entity.getReceiver() != null ? entity.getReceiver().getUserId() : null)
                .receiverUserName(entity.getReceiver() != null ? entity.getReceiver().getUsername() : null)
                .receiverName(entity.getReceiverName())
                // Totals
                .totalQuantity(entity.getTotalQuantity())
                .totalAmount(entity.getTotalAmount())
                // Items
                .items(entity.getItems().stream()
                    .map(this::toGoodsReceiptItemResponse)
                    .collect(Collectors.toList()))
                // Additional
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                // Audit
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    /**
     * Convert item entity to response DTO
     */
    private GoodsReceiptItemResponse toGoodsReceiptItemResponse(GoodsReceiptItemEntity entity) {
        return GoodsReceiptItemResponse.builder()
                .goodsReceiptItemId(entity.getGoodsReceiptItemId())
                .purchaseOrderItemId(entity.getPurchaseOrderItem() != null ?
                    entity.getPurchaseOrderItem().getPurchaseOrderItemId() : null)
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .orderedQuantity(entity.getOrderedQuantity())
                .receivedQuantity(entity.getReceivedQuantity())
                .unitPrice(entity.getUnitPrice())
                .lineAmount(entity.getLineAmount())
                .lotNo(entity.getLotNo())
                .expiryDate(entity.getExpiryDate())
                .inspectionStatus(entity.getInspectionStatus())
                .qualityInspectionId(entity.getQualityInspection() != null ?
                    entity.getQualityInspection().getQualityInspectionId() : null)
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
