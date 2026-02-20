package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.wms.MaterialHandoverResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.MaterialHandoverEntity;
import kr.co.softice.mes.domain.service.MaterialHandoverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Material Handover Controller
 * 자재 인수인계 관리 API
 *
 * 엔드포인트:
 * - GET /api/material-handovers - 인수인계 목록
 * - GET /api/material-handovers/{id} - 인수인계 상세
 * - GET /api/material-handovers/my-pending - 내 대기 인수인계 (로그인 사용자)
 * - POST /api/material-handovers/{id}/confirm - 인수 확인
 * - POST /api/material-handovers/{id}/reject - 인수 거부
 *
 * 워크플로우: PENDING → CONFIRMED/REJECTED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/material-handovers")
@RequiredArgsConstructor
@Tag(name = "Material Handover Management", description = "자재 인수인계 관리 API")
public class MaterialHandoverController {

    private final MaterialHandoverService materialHandoverService;

    /**
     * 인수인계 목록 조회
     * GET /api/material-handovers
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "인수인계 목록 조회", description = "테넌트의 모든 자재 인수인계 조회")
    public ResponseEntity<ApiResponse<List<MaterialHandoverResponse>>> getMaterialHandovers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long materialRequestId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting material handovers for tenant: {}, status: {}, requestId: {}",
            tenantId, status, materialRequestId);

        List<MaterialHandoverEntity> handovers;

        if (status != null) {
            handovers = materialHandoverService.findByStatus(tenantId, status);
        } else if (materialRequestId != null) {
            handovers = materialHandoverService.findByMaterialRequest(materialRequestId);
        } else {
            handovers = materialHandoverService.findByTenant(tenantId);
        }

        List<MaterialHandoverResponse> responses = handovers.stream()
                .map(this::toMaterialHandoverResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("인수인계 목록 조회 성공", responses));
    }

    /**
     * 인수인계 상세 조회
     * GET /api/material-handovers/{id}
     */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "인수인계 상세 조회", description = "인수인계 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<MaterialHandoverResponse>> getMaterialHandover(@PathVariable Long id) {
        log.info("Getting material handover: {}", id);

        MaterialHandoverEntity handover = materialHandoverService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATERIAL_HANDOVER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("인수인계 조회 성공", toMaterialHandoverResponse(handover)));
    }

    /**
     * 내 대기 인수인계 조회 (로그인 사용자)
     * GET /api/material-handovers/my-pending
     */
    @Transactional(readOnly = true)
    @GetMapping("/my-pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 대기 인수인계", description = "로그인 사용자가 인수해야 할 대기 중인 인수인계 조회")
    public ResponseEntity<ApiResponse<List<MaterialHandoverResponse>>> getMyPendingHandovers(
            @RequestParam Long receiverId) {

        log.info("Getting pending handovers for receiver: {}", receiverId);

        List<MaterialHandoverEntity> handovers = materialHandoverService.findPendingByReceiver(receiverId);
        List<MaterialHandoverResponse> responses = handovers.stream()
                .map(this::toMaterialHandoverResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("대기 인수인계 조회 성공", responses));
    }

    /**
     * 인수 확인
     * POST /api/material-handovers/{id}/confirm
     *
     * 워크플로우:
     * - 상태 검증 (PENDING만 확인 가능)
     * - 인수자 검증 (assigned receiver만 확인 가능)
     * - 인수 정보 업데이트
     * - 상태 → CONFIRMED
     * - 모든 인수인계 확인 시 불출 신청 자동 완료
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'PRODUCTION_WORKER')")
    @Operation(summary = "인수 확인", description = "자재 인수 확인 (생산 담당자)")
    public ResponseEntity<ApiResponse<MaterialHandoverResponse>> confirmHandover(
            @PathVariable Long id,
            @RequestParam Long receiverId,
            @RequestParam(required = false) String remarks) {

        log.info("Confirming handover: {} by receiver: {}", id, receiverId);

        MaterialHandoverEntity confirmed = materialHandoverService.confirmHandover(
            id, receiverId, remarks != null ? remarks : "");

        return ResponseEntity.ok(ApiResponse.success("인수 확인 성공", toMaterialHandoverResponse(confirmed)));
    }

    /**
     * 인수 거부
     * POST /api/material-handovers/{id}/reject
     *
     * 워크플로우:
     * - 상태 검증 (PENDING만 거부 가능)
     * - 인수자 검증
     * - 거부 사유 저장
     * - 상태 → REJECTED
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'PRODUCTION_WORKER')")
    @Operation(summary = "인수 거부", description = "자재 인수 거부 (생산 담당자)")
    public ResponseEntity<ApiResponse<MaterialHandoverResponse>> rejectHandover(
            @PathVariable Long id,
            @RequestParam Long receiverId,
            @RequestParam String reason) {

        log.info("Rejecting handover: {} by receiver: {}, reason: {}", id, receiverId, reason);

        MaterialHandoverEntity rejected = materialHandoverService.rejectHandover(id, receiverId, reason);

        return ResponseEntity.ok(ApiResponse.success("인수 거부 성공", toMaterialHandoverResponse(rejected)));
    }

    // ================== Private Helper Methods ==================

    /**
     * Convert entity to response DTO
     */
    private MaterialHandoverResponse toMaterialHandoverResponse(MaterialHandoverEntity entity) {
        return MaterialHandoverResponse.builder()
                .materialHandoverId(entity.getMaterialHandoverId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .handoverNo(entity.getHandoverNo())
                .handoverDate(entity.getHandoverDate())
                .handoverStatus(entity.getHandoverStatus())
                // Material Request Reference
                .materialRequestId(entity.getMaterialRequest().getMaterialRequestId())
                .materialRequestNo(entity.getMaterialRequest().getRequestNo())
                .materialRequestItemId(entity.getMaterialRequestItem().getMaterialRequestItemId())
                // Inventory Transaction Reference
                .inventoryTransactionId(entity.getInventoryTransaction().getTransactionId())
                .transactionNo(entity.getInventoryTransaction().getTransactionNo())
                // Product and LOT
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProduct().getProductCode())
                .productName(entity.getProduct().getProductName())
                .lotId(entity.getLot() != null ? entity.getLot().getLotId() : null)
                .lotNo(entity.getLotNo())
                .lotQualityStatus(entity.getLot() != null ? entity.getLot().getQualityStatus() : null)
                .quantity(entity.getQuantity())
                .unit(entity.getUnit())
                // Issuer
                .issuerUserId(entity.getIssuer().getUserId())
                .issuerUserName(entity.getIssuer().getUsername())
                .issuerName(entity.getIssuerName())
                .issueLocation(entity.getIssueLocation())
                // Receiver
                .receiverUserId(entity.getReceiver().getUserId())
                .receiverUserName(entity.getReceiver().getUsername())
                .receiverName(entity.getReceiverName())
                .receiveLocation(entity.getReceiveLocation())
                .receivedDate(entity.getReceivedDate())
                // Confirmation
                .confirmationRemarks(entity.getConfirmationRemarks())
                // Additional
                .remarks(entity.getRemarks())
                // Audit
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
