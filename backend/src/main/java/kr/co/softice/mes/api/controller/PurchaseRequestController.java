package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.purchase.PurchaseRequestCreateRequest;
import kr.co.softice.mes.common.dto.purchase.PurchaseRequestResponse;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.MaterialEntity;
import kr.co.softice.mes.domain.entity.PurchaseRequestEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.service.PurchaseRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Purchase Request Controller
 * 구매 요청 컨트롤러
 *
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/purchase-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Purchase Request Management", description = "구매 요청 관리 API")
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    /**
     * 모든 구매 요청 조회
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모든 구매 요청 조회", description = "테넌트의 모든 구매 요청을 조회합니다")
    public ResponseEntity<List<PurchaseRequestResponse>> getAllPurchaseRequests() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/purchase-requests - tenant: {}", tenantId);

        List<PurchaseRequestEntity> requests = purchaseRequestService.getAllPurchaseRequests(tenantId);
        List<PurchaseRequestResponse> responses = requests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 상태별 구매 요청 조회
     */
    @Transactional(readOnly = true)
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 구매 요청 조회", description = "특정 상태의 구매 요청을 조회합니다")
    public ResponseEntity<List<PurchaseRequestResponse>> getPurchaseRequestsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/purchase-requests/status/{} - tenant: {}", status, tenantId);

        List<PurchaseRequestEntity> requests = purchaseRequestService.getPurchaseRequestsByStatus(tenantId, status);
        List<PurchaseRequestResponse> responses = requests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 구매 요청 ID로 조회
     */
    @Transactional(readOnly = true)
    @GetMapping("/{purchaseRequestId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "구매 요청 조회", description = "구매 요청 ID로 구매 요청을 조회합니다")
    public ResponseEntity<PurchaseRequestResponse> getPurchaseRequestById(@PathVariable Long purchaseRequestId) {
        log.info("GET /api/purchase-requests/{}", purchaseRequestId);

        PurchaseRequestEntity request = purchaseRequestService.getPurchaseRequestById(purchaseRequestId);
        return ResponseEntity.ok(toResponse(request));
    }

    /**
     * 구매 요청 생성
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "구매 요청 생성", description = "새로운 구매 요청을 생성합니다")
    public ResponseEntity<PurchaseRequestResponse> createPurchaseRequest(@Valid @RequestBody PurchaseRequestCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/purchase-requests - tenant: {}, requestNo: {}", tenantId, request.getRequestNo());

        PurchaseRequestEntity purchaseRequest = toEntity(request);
        PurchaseRequestEntity created = purchaseRequestService.createPurchaseRequest(tenantId, purchaseRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    /**
     * 구매 요청 승인
     */
    @PostMapping("/{purchaseRequestId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    @Operation(summary = "구매 요청 승인", description = "구매 요청을 승인합니다")
    public ResponseEntity<PurchaseRequestResponse> approvePurchaseRequest(
            @PathVariable Long purchaseRequestId,
            @RequestParam Long approverUserId,
            @RequestParam(required = false) String approvalComment) {
        log.info("POST /api/purchase-requests/{}/approve", purchaseRequestId);

        PurchaseRequestEntity approved = purchaseRequestService.approvePurchaseRequest(
                purchaseRequestId, approverUserId, approvalComment);
        return ResponseEntity.ok(toResponse(approved));
    }

    /**
     * 구매 요청 거절
     */
    @PostMapping("/{purchaseRequestId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    @Operation(summary = "구매 요청 거절", description = "구매 요청을 거절합니다")
    public ResponseEntity<PurchaseRequestResponse> rejectPurchaseRequest(
            @PathVariable Long purchaseRequestId,
            @RequestParam Long approverUserId,
            @RequestParam(required = false) String approvalComment) {
        log.info("POST /api/purchase-requests/{}/reject", purchaseRequestId);

        PurchaseRequestEntity rejected = purchaseRequestService.rejectPurchaseRequest(
                purchaseRequestId, approverUserId, approvalComment);
        return ResponseEntity.ok(toResponse(rejected));
    }

    /**
     * 구매 요청 삭제
     */
    @DeleteMapping("/{purchaseRequestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    @Operation(summary = "구매 요청 삭제", description = "구매 요청을 삭제합니다")
    public ResponseEntity<Void> deletePurchaseRequest(@PathVariable Long purchaseRequestId) {
        log.info("DELETE /api/purchase-requests/{}", purchaseRequestId);

        purchaseRequestService.deletePurchaseRequest(purchaseRequestId);
        return ResponseEntity.ok().build();
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private PurchaseRequestResponse toResponse(PurchaseRequestEntity entity) {
        return PurchaseRequestResponse.builder()
                .purchaseRequestId(entity.getPurchaseRequestId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .requestNo(entity.getRequestNo())
                .requestDate(entity.getRequestDate())
                .requesterUserId(entity.getRequester().getUserId())
                .requesterUsername(entity.getRequester().getUsername())
                .requesterFullName(entity.getRequester().getFullName())
                .department(entity.getDepartment())
                .materialId(entity.getMaterial().getMaterialId())
                .materialCode(entity.getMaterial().getMaterialCode())
                .materialName(entity.getMaterial().getMaterialName())
                .requestedQuantity(entity.getRequestedQuantity())
                .unit(entity.getMaterial().getUnit())
                .requiredDate(entity.getRequiredDate())
                .purpose(entity.getPurpose())
                .status(entity.getStatus())
                .approverUserId(entity.getApprover() != null ? entity.getApprover().getUserId() : null)
                .approverUsername(entity.getApprover() != null ? entity.getApprover().getUsername() : null)
                .approverFullName(entity.getApprover() != null ? entity.getApprover().getFullName() : null)
                .approvalDate(entity.getApprovalDate())
                .approvalComment(entity.getApprovalComment())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * CreateRequest를 Entity로 변환
     */
    private PurchaseRequestEntity toEntity(PurchaseRequestCreateRequest request) {
        return PurchaseRequestEntity.builder()
                .requestNo(request.getRequestNo())
                .requester(UserEntity.builder().userId(request.getRequesterUserId()).build())
                .department(request.getDepartment())
                .material(MaterialEntity.builder().materialId(request.getMaterialId()).build())
                .requestedQuantity(request.getRequestedQuantity())
                .requiredDate(request.getRequiredDate())
                .purpose(request.getPurpose())
                .remarks(request.getRemarks())
                .build();
    }
}
