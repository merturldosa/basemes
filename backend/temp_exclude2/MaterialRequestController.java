package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.wms.MaterialRequestCreateRequest;
import kr.co.softice.mes.common.dto.wms.MaterialRequestItemRequest;
import kr.co.softice.mes.common.dto.wms.MaterialRequestItemResponse;
import kr.co.softice.mes.common.dto.wms.MaterialRequestResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.MaterialRequestService;
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
 * Material Request Controller
 * 불출 신청 관리 API
 *
 * 엔드포인트:
 * - GET /api/material-requests - 불출 신청 목록
 * - GET /api/material-requests/{id} - 불출 신청 상세
 * - POST /api/material-requests - 불출 신청 생성
 * - POST /api/material-requests/{id}/approve - 승인
 * - POST /api/material-requests/{id}/reject - 거부
 * - POST /api/material-requests/{id}/issue - 불출 지시
 * - POST /api/material-requests/{id}/complete - 완료
 * - POST /api/material-requests/{id}/cancel - 취소
 *
 * 워크플로우: PENDING → APPROVED → ISSUED → COMPLETED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/material-requests")
@RequiredArgsConstructor
@Tag(name = "Material Request Management", description = "불출 신청 관리 API")
public class MaterialRequestController {

    private final MaterialRequestService materialRequestService;
    private final TenantRepository tenantRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * 불출 신청 목록 조회
     * GET /api/material-requests
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "불출 신청 목록 조회", description = "테넌트의 모든 불출 신청 조회")
    public ResponseEntity<ApiResponse<List<MaterialRequestResponse>>> getMaterialRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long workOrderId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long requesterId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting material requests for tenant: {}, status: {}, WO: {}, warehouse: {}, requester: {}",
            tenantId, status, workOrderId, warehouseId, requesterId);

        List<MaterialRequestEntity> requests;

        if (status != null) {
            requests = materialRequestService.findByStatus(tenantId, status);
        } else if (workOrderId != null) {
            requests = materialRequestService.findByWorkOrderId(tenantId, workOrderId);
        } else if (warehouseId != null) {
            requests = materialRequestService.findByWarehouseId(tenantId, warehouseId);
        } else if (requesterId != null) {
            requests = materialRequestService.findByRequesterId(tenantId, requesterId);
        } else {
            requests = materialRequestService.findByTenant(tenantId);
        }

        List<MaterialRequestResponse> responses = requests.stream()
                .map(this::toMaterialRequestResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("불출 신청 목록 조회 성공", responses));
    }

    /**
     * 불출 신청 상세 조회 (항목 포함)
     * GET /api/material-requests/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "불출 신청 상세 조회", description = "불출 신청 ID로 상세 정보 조회 (항목 포함)")
    public ResponseEntity<ApiResponse<MaterialRequestResponse>> getMaterialRequest(@PathVariable Long id) {
        log.info("Getting material request: {}", id);

        MaterialRequestEntity request = materialRequestService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATERIAL_REQUEST_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("불출 신청 조회 성공", toMaterialRequestResponse(request)));
    }

    /**
     * 긴급 불출 신청 조회
     * GET /api/material-requests/urgent
     */
    @GetMapping("/urgent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "긴급 불출 신청 조회", description = "긴급(URGENT) 우선순위의 대기 중인 불출 신청 조회")
    public ResponseEntity<ApiResponse<List<MaterialRequestResponse>>> getUrgentRequests() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting urgent material requests for tenant: {}", tenantId);

        List<MaterialRequestEntity> requests = materialRequestService.findUrgentRequests(tenantId);
        List<MaterialRequestResponse> responses = requests.stream()
                .map(this::toMaterialRequestResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("긴급 불출 신청 조회 성공", responses));
    }

    /**
     * 창고별 대기 불출 신청 조회
     * GET /api/material-requests/warehouse/{warehouseId}/pending
     */
    @GetMapping("/warehouse/{warehouseId}/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "창고별 대기 불출 신청", description = "특정 창고의 대기/승인 상태 불출 신청 조회")
    public ResponseEntity<ApiResponse<List<MaterialRequestResponse>>> getPendingRequestsByWarehouse(
            @PathVariable Long warehouseId) {

        log.info("Getting pending material requests for warehouse: {}", warehouseId);

        List<MaterialRequestEntity> requests = materialRequestService.findPendingByWarehouse(warehouseId);
        List<MaterialRequestResponse> responses = requests.stream()
                .map(this::toMaterialRequestResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("창고별 대기 불출 신청 조회 성공", responses));
    }

    /**
     * 불출 신청 생성
     * POST /api/material-requests
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'PRODUCTION_WORKER')")
    @Operation(summary = "불출 신청 생성", description = "신규 불출 신청 등록")
    public ResponseEntity<ApiResponse<MaterialRequestResponse>> createMaterialRequest(
            @Valid @RequestBody MaterialRequestCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating material request for tenant: {}, requestNo: {}", tenantId, request.getRequestNo());

        // Validate and fetch entities
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        UserEntity requester = userRepository.findById(request.getRequesterUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        WarehouseEntity warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        WorkOrderEntity workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));
        }

        // Build material request entity
        MaterialRequestEntity materialRequest = MaterialRequestEntity.builder()
                .tenant(tenant)
                .requestNo(request.getRequestNo())
                .requestDate(request.getRequestDate())
                .workOrder(workOrder)
                .requester(requester)
                .warehouse(warehouse)
                .requiredDate(request.getRequiredDate())
                .requestStatus("PENDING")
                .priority(request.getPriority())
                .purpose(request.getPurpose())
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        // Build items
        for (MaterialRequestItemRequest itemReq : request.getItems()) {
            ProductEntity product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

            MaterialRequestItemEntity item = MaterialRequestItemEntity.builder()
                    .materialRequest(materialRequest)
                    .product(product)
                    .productCode(product.getProductCode())
                    .productName(product.getProductName())
                    .requestedQuantity(itemReq.getRequestedQuantity())
                    .issueStatus("PENDING")
                    .requestedLotNo(itemReq.getRequestedLotNo())
                    .remarks(itemReq.getRemarks())
                    .build();

            materialRequest.addItem(item);
        }

        // Create material request
        MaterialRequestEntity created = materialRequestService.createMaterialRequest(materialRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("불출 신청 생성 성공", toMaterialRequestResponse(created)));
    }

    /**
     * 불출 신청 승인
     * POST /api/material-requests/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_CLERK')")
    @Operation(summary = "불출 신청 승인", description = "대기 중인 불출 신청 승인 (재고 가용성 검증)")
    public ResponseEntity<ApiResponse<MaterialRequestResponse>> approveMaterialRequest(
            @PathVariable Long id,
            @RequestParam Long approverUserId) {

        log.info("Approving material request: {} by user: {}", id, approverUserId);

        MaterialRequestEntity approved = materialRequestService.approveMaterialRequest(id, approverUserId);

        return ResponseEntity.ok(ApiResponse.success("불출 신청 승인 성공", toMaterialRequestResponse(approved)));
    }

    /**
     * 불출 신청 거부
     * POST /api/material-requests/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "불출 신청 거부", description = "대기 중인 불출 신청 거부")
    public ResponseEntity<ApiResponse<MaterialRequestResponse>> rejectMaterialRequest(
            @PathVariable Long id,
            @RequestParam Long approverUserId,
            @RequestParam String reason) {

        log.info("Rejecting material request: {} by user: {}, reason: {}", id, approverUserId, reason);

        MaterialRequestEntity rejected = materialRequestService.rejectMaterialRequest(id, approverUserId, reason);

        return ResponseEntity.ok(ApiResponse.success("불출 신청 거부 성공", toMaterialRequestResponse(rejected)));
    }

    /**
     * 불출 지시 (자재 불출 실행)
     * POST /api/material-requests/{id}/issue
     *
     * 워크플로우:
     * - LOT 선택 (FIFO 또는 특정 LOT)
     * - 재고 트랜잭션 생성 (OUT_ISSUE)
     * - 재고 차감
     * - 인수인계 레코드 생성
     */
    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_CLERK')")
    @Operation(summary = "불출 지시", description = "승인된 불출 신청 실행 (LOT 선택, 재고 차감, 인수인계 생성)")
    public ResponseEntity<ApiResponse<MaterialRequestResponse>> issueMaterials(
            @PathVariable Long id,
            @RequestParam Long issuerUserId) {

        log.info("Issuing materials for request: {} by user: {}", id, issuerUserId);

        MaterialRequestEntity issued = materialRequestService.issueMaterials(id, issuerUserId);

        return ResponseEntity.ok(ApiResponse.success("불출 지시 성공", toMaterialRequestResponse(issued)));
    }

    /**
     * 불출 신청 완료
     * POST /api/material-requests/{id}/complete
     *
     * 조건: 모든 인수인계가 CONFIRMED 상태
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "불출 신청 완료", description = "불출 프로세스 완료 (모든 인수인계 확인 후)")
    public ResponseEntity<ApiResponse<MaterialRequestResponse>> completeMaterialRequest(@PathVariable Long id) {
        log.info("Completing material request: {}", id);

        MaterialRequestEntity completed = materialRequestService.completeMaterialRequest(id);

        return ResponseEntity.ok(ApiResponse.success("불출 신청 완료 성공", toMaterialRequestResponse(completed)));
    }

    /**
     * 불출 신청 취소
     * POST /api/material-requests/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "불출 신청 취소", description = "불출 신청 취소 (PENDING/APPROVED 상태만 가능)")
    public ResponseEntity<ApiResponse<MaterialRequestResponse>> cancelMaterialRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        log.info("Cancelling material request: {}, reason: {}", id, reason);

        MaterialRequestEntity cancelled = materialRequestService.cancelMaterialRequest(id,
            reason != null ? reason : "Cancelled by user");

        return ResponseEntity.ok(ApiResponse.success("불출 신청 취소 성공", toMaterialRequestResponse(cancelled)));
    }

    // ================== Private Helper Methods ==================

    /**
     * Convert entity to response DTO
     */
    private MaterialRequestResponse toMaterialRequestResponse(MaterialRequestEntity entity) {
        return MaterialRequestResponse.builder()
                .materialRequestId(entity.getMaterialRequestId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .requestNo(entity.getRequestNo())
                .requestDate(entity.getRequestDate())
                .requestStatus(entity.getRequestStatus())
                .priority(entity.getPriority())
                .purpose(entity.getPurpose())
                // Work Order
                .workOrderId(entity.getWorkOrder() != null ?
                    entity.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(entity.getWorkOrder() != null ?
                    entity.getWorkOrder().getWorkOrderNo() : null)
                // Requester
                .requesterUserId(entity.getRequester().getUserId())
                .requesterUserName(entity.getRequester().getUsername())
                .requesterName(entity.getRequester().getFullName())
                // Warehouse
                .warehouseId(entity.getWarehouse().getWarehouseId())
                .warehouseCode(entity.getWarehouse().getWarehouseCode())
                .warehouseName(entity.getWarehouse().getWarehouseName())
                // Approver
                .approverUserId(entity.getApprover() != null ? entity.getApprover().getUserId() : null)
                .approverUserName(entity.getApprover() != null ? entity.getApprover().getUsername() : null)
                .approverName(entity.getApprover() != null ? entity.getApprover().getFullName() : null)
                .approvedDate(entity.getApprovedDate())
                // Dates
                .requiredDate(entity.getRequiredDate())
                .issuedDate(entity.getIssuedDate())
                .completedDate(entity.getCompletedDate())
                // Totals
                .totalRequestedQuantity(calculateTotalRequestedQuantity(entity))
                .totalApprovedQuantity(calculateTotalApprovedQuantity(entity))
                .totalIssuedQuantity(calculateTotalIssuedQuantity(entity))
                // Items
                .items(entity.getItems().stream()
                    .map(this::toMaterialRequestItemResponse)
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
    private MaterialRequestItemResponse toMaterialRequestItemResponse(MaterialRequestItemEntity entity) {
        return MaterialRequestItemResponse.builder()
                .materialRequestItemId(entity.getMaterialRequestItemId())
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .productType(entity.getProduct().getProductType())
                .unit(entity.getProduct().getUnit())
                .requestedQuantity(entity.getRequestedQuantity())
                .approvedQuantity(entity.getApprovedQuantity())
                .issuedQuantity(entity.getIssuedQuantity())
                .issueStatus(entity.getIssueStatus())
                .requestedLotNo(entity.getRequestedLotNo())
                .issuedLotNo(entity.getIssuedLotNo())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Calculate total requested quantity
     */
    private BigDecimal calculateTotalRequestedQuantity(MaterialRequestEntity entity) {
        return entity.getItems().stream()
                .map(MaterialRequestItemEntity::getRequestedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total approved quantity
     */
    private BigDecimal calculateTotalApprovedQuantity(MaterialRequestEntity entity) {
        return entity.getItems().stream()
                .map(item -> item.getApprovedQuantity() != null ? item.getApprovedQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total issued quantity
     */
    private BigDecimal calculateTotalIssuedQuantity(MaterialRequestEntity entity) {
        return entity.getItems().stream()
                .map(item -> item.getIssuedQuantity() != null ? item.getIssuedQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
