package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.wms.ReturnCreateRequest;
import kr.co.softice.mes.common.dto.wms.ReturnItemRequest;
import kr.co.softice.mes.common.dto.wms.ReturnItemResponse;
import kr.co.softice.mes.common.dto.wms.ReturnResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.ReturnService;
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
 * Return Controller
 * 반품 관리 API
 *
 * 엔드포인트:
 * - GET /api/returns - 반품 목록
 * - GET /api/returns/{id} - 반품 상세
 * - POST /api/returns - 반품 생성
 * - POST /api/returns/{id}/approve - 승인
 * - POST /api/returns/{id}/reject - 거부
 * - POST /api/returns/{id}/receive - 입고
 * - POST /api/returns/{id}/complete - 완료
 * - POST /api/returns/{id}/cancel - 취소
 *
 * 워크플로우: PENDING → APPROVED → RECEIVED → INSPECTING → COMPLETED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@Tag(name = "Return Management", description = "반품 관리 API")
public class ReturnController {

    private final ReturnService returnService;
    private final TenantRepository tenantRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final MaterialRequestRepository materialRequestRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * 반품 목록 조회
     * GET /api/returns
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "반품 목록 조회", description = "테넌트의 모든 반품 조회")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> getReturns(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long materialRequestId,
            @RequestParam(required = false) Long workOrderId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting returns for tenant: {}, status: {}, type: {}, warehouse: {}, MR: {}, WO: {}",
            tenantId, status, type, warehouseId, materialRequestId, workOrderId);

        List<ReturnEntity> returns;

        if (status != null) {
            returns = returnService.findByStatus(tenantId, status);
        } else if (type != null) {
            returns = returnService.findByType(tenantId, type);
        } else if (warehouseId != null) {
            returns = returnService.findByWarehouseId(tenantId, warehouseId);
        } else if (materialRequestId != null) {
            returns = returnService.findByMaterialRequestId(tenantId, materialRequestId);
        } else if (workOrderId != null) {
            returns = returnService.findByWorkOrderId(tenantId, workOrderId);
        } else {
            returns = returnService.findByTenant(tenantId);
        }

        List<ReturnResponse> responses = returns.stream()
                .map(this::toReturnResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("반품 목록 조회 성공", responses));
    }

    /**
     * 반품 상세 조회 (항목 포함)
     * GET /api/returns/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "반품 상세 조회", description = "반품 ID로 상세 정보 조회 (항목 포함)")
    public ResponseEntity<ApiResponse<ReturnResponse>> getReturn(@PathVariable Long id) {
        log.info("Getting return: {}", id);

        ReturnEntity returnEntity = returnService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("반품 조회 성공", toReturnResponse(returnEntity)));
    }

    /**
     * 창고별 대기 반품 조회
     * GET /api/returns/warehouse/{warehouseId}/pending
     */
    @GetMapping("/warehouse/{warehouseId}/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "창고별 대기 반품", description = "특정 창고의 대기/승인/입고 상태 반품 조회")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> getPendingReturnsByWarehouse(
            @PathVariable Long warehouseId) {

        log.info("Getting pending returns for warehouse: {}", warehouseId);

        List<ReturnEntity> returns = returnService.findPendingByWarehouse(warehouseId);
        List<ReturnResponse> responses = returns.stream()
                .map(this::toReturnResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("창고별 대기 반품 조회 성공", responses));
    }

    /**
     * 검사 필요 반품 조회
     * GET /api/returns/requiring-inspection
     */
    @GetMapping("/requiring-inspection")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "검사 필요 반품", description = "입고/검사 중 상태의 반품 조회")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> getReturnsRequiringInspection() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting returns requiring inspection for tenant: {}", tenantId);

        List<ReturnEntity> returns = returnService.findRequiringInspection(tenantId);
        List<ReturnResponse> responses = returns.stream()
                .map(this::toReturnResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("검사 필요 반품 조회 성공", responses));
    }

    /**
     * 반품 생성
     * POST /api/returns
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'PRODUCTION_WORKER', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 생성", description = "신규 반품 등록")
    public ResponseEntity<ApiResponse<ReturnResponse>> createReturn(
            @Valid @RequestBody ReturnCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating return for tenant: {}, returnNo: {}", tenantId, request.getReturnNo());

        // Validate and fetch entities
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        UserEntity requester = userRepository.findById(request.getRequesterUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        WarehouseEntity warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        MaterialRequestEntity materialRequest = null;
        if (request.getMaterialRequestId() != null) {
            materialRequest = materialRequestRepository.findById(request.getMaterialRequestId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATERIAL_REQUEST_NOT_FOUND));
        }

        WorkOrderEntity workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));
        }

        // Build return entity
        ReturnEntity returnEntity = ReturnEntity.builder()
                .tenant(tenant)
                .returnNo(request.getReturnNo())
                .returnDate(request.getReturnDate())
                .returnType(request.getReturnType())
                .materialRequest(materialRequest)
                .workOrder(workOrder)
                .requester(requester)
                .warehouse(warehouse)
                .returnStatus("PENDING")
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        // Build items
        for (ReturnItemRequest itemReq : request.getItems()) {
            ProductEntity product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

            ReturnItemEntity item = ReturnItemEntity.builder()
                    .returnEntity(returnEntity)
                    .product(product)
                    .returnQuantity(itemReq.getReturnQuantity())
                    .originalLotNo(itemReq.getOriginalLotNo())
                    .returnReason(itemReq.getReturnReason())
                    .remarks(itemReq.getRemarks())
                    .build();

            returnEntity.addItem(item);
        }

        // Create return
        ReturnEntity created = returnService.createReturn(returnEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("반품 생성 성공", toReturnResponse(created)));
    }

    /**
     * 반품 승인
     * POST /api/returns/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 승인", description = "대기 중인 반품 승인")
    public ResponseEntity<ApiResponse<ReturnResponse>> approveReturn(
            @PathVariable Long id,
            @RequestParam Long approverUserId) {

        log.info("Approving return: {} by user: {}", id, approverUserId);

        ReturnEntity approved = returnService.approveReturn(id, approverUserId);

        return ResponseEntity.ok(ApiResponse.success("반품 승인 성공", toReturnResponse(approved)));
    }

    /**
     * 반품 거부
     * POST /api/returns/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 거부", description = "대기 중인 반품 거부")
    public ResponseEntity<ApiResponse<ReturnResponse>> rejectReturn(
            @PathVariable Long id,
            @RequestParam Long approverUserId,
            @RequestParam String reason) {

        log.info("Rejecting return: {} by user: {}, reason: {}", id, approverUserId, reason);

        ReturnEntity rejected = returnService.rejectReturn(id, approverUserId, reason);

        return ResponseEntity.ok(ApiResponse.success("반품 거부 성공", toReturnResponse(rejected)));
    }

    /**
     * 반품 입고
     * POST /api/returns/{id}/receive
     *
     * 워크플로우:
     * - 재고 트랜잭션 생성 (IN_RETURN)
     * - 검사 필요 시 품질 검사 요청 생성
     * - 상태 → RECEIVED (검사 필요 시 INSPECTING)
     */
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_CLERK')")
    @Operation(summary = "반품 입고", description = "승인된 반품 입고 처리 (재고 트랜잭션 생성, 검사 요청)")
    public ResponseEntity<ApiResponse<ReturnResponse>> receiveReturn(
            @PathVariable Long id,
            @RequestParam Long receiverUserId) {

        log.info("Receiving return: {} by user: {}", id, receiverUserId);

        ReturnEntity received = returnService.receiveReturn(id, receiverUserId);

        return ResponseEntity.ok(ApiResponse.success("반품 입고 성공", toReturnResponse(received)));
    }

    /**
     * 반품 완료 (재고 복원)
     * POST /api/returns/{id}/complete
     *
     * 워크플로우:
     * - 합격품: 원래 창고에 재입고
     * - 불합격품: 격리 창고로 이동
     * - 상태 → COMPLETED
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 완료", description = "반품 프로세스 완료 (재고 복원)")
    public ResponseEntity<ApiResponse<ReturnResponse>> completeReturn(@PathVariable Long id) {
        log.info("Completing return: {}", id);

        ReturnEntity completed = returnService.completeReturn(id);

        return ResponseEntity.ok(ApiResponse.success("반품 완료 성공", toReturnResponse(completed)));
    }

    /**
     * 반품 취소
     * POST /api/returns/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "반품 취소", description = "반품 취소 (PENDING/APPROVED 상태만 가능)")
    public ResponseEntity<ApiResponse<ReturnResponse>> cancelReturn(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        log.info("Cancelling return: {}, reason: {}", id, reason);

        ReturnEntity cancelled = returnService.cancelReturn(id,
            reason != null ? reason : "Cancelled by user");

        return ResponseEntity.ok(ApiResponse.success("반품 취소 성공", toReturnResponse(cancelled)));
    }

    // ================== Private Helper Methods ==================

    /**
     * Convert entity to response DTO
     */
    private ReturnResponse toReturnResponse(ReturnEntity entity) {
        return ReturnResponse.builder()
                .returnId(entity.getReturnId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .returnNo(entity.getReturnNo())
                .returnDate(entity.getReturnDate())
                .returnType(entity.getReturnType())
                .returnStatus(entity.getReturnStatus())
                // References
                .materialRequestId(entity.getMaterialRequest() != null ?
                    entity.getMaterialRequest().getMaterialRequestId() : null)
                .materialRequestNo(entity.getMaterialRequest() != null ?
                    entity.getMaterialRequest().getRequestNo() : null)
                .workOrderId(entity.getWorkOrder() != null ?
                    entity.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(entity.getWorkOrder() != null ?
                    entity.getWorkOrder().getWorkOrderNo() : null)
                // Requester
                .requesterUserId(entity.getRequester().getUserId())
                .requesterUserName(entity.getRequester().getUsername())
                .requesterName(entity.getRequesterName())
                // Warehouse
                .warehouseId(entity.getWarehouse().getWarehouseId())
                .warehouseCode(entity.getWarehouse().getWarehouseCode())
                .warehouseName(entity.getWarehouse().getWarehouseName())
                // Approver
                .approverUserId(entity.getApprover() != null ? entity.getApprover().getUserId() : null)
                .approverUserName(entity.getApprover() != null ? entity.getApprover().getUsername() : null)
                .approverName(entity.getApproverName())
                .approvedDate(entity.getApprovedDate())
                // Dates
                .receivedDate(entity.getReceivedDate())
                .completedDate(entity.getCompletedDate())
                // Totals
                .totalReturnQuantity(entity.getTotalReturnQuantity())
                .totalReceivedQuantity(entity.getTotalReceivedQuantity())
                .totalPassedQuantity(entity.getTotalPassedQuantity())
                .totalFailedQuantity(entity.getTotalFailedQuantity())
                // Items
                .items(entity.getItems().stream()
                    .map(this::toReturnItemResponse)
                    .collect(Collectors.toList()))
                // Additional
                .remarks(entity.getRemarks())
                .rejectionReason(entity.getRejectionReason())
                .cancellationReason(entity.getCancellationReason())
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
    private ReturnItemResponse toReturnItemResponse(ReturnItemEntity entity) {
        return ReturnItemResponse.builder()
                .returnItemId(entity.getReturnItemId())
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .productType(entity.getProduct().getProductType())
                .unit(entity.getProduct().getUnit())
                .originalLotNo(entity.getOriginalLotNo())
                .newLotNo(entity.getNewLotNo())
                .returnQuantity(entity.getReturnQuantity())
                .receivedQuantity(entity.getReceivedQuantity())
                .passedQuantity(entity.getPassedQuantity())
                .failedQuantity(entity.getFailedQuantity())
                .inspectionStatus(entity.getInspectionStatus())
                .qualityInspectionId(entity.getQualityInspection() != null ?
                    entity.getQualityInspection().getQualityInspectionId() : null)
                .receiveTransactionId(entity.getReceiveTransaction() != null ?
                    entity.getReceiveTransaction().getInventoryTransactionId() : null)
                .passTransactionId(entity.getPassTransaction() != null ?
                    entity.getPassTransaction().getInventoryTransactionId() : null)
                .failTransactionId(entity.getFailTransaction() != null ?
                    entity.getFailTransaction().getInventoryTransactionId() : null)
                .returnReason(entity.getReturnReason())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
