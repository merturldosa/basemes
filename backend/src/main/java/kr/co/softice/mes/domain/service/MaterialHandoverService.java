package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Material Handover Service
 * 자재 인수인계 관리 서비스
 *
 * 핵심 기능:
 * - 인수인계 목록 조회
 * - 인수 확인
 * - 인수 거부
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialHandoverService {

    private final MaterialHandoverRepository materialHandoverRepository;
    private final MaterialRequestRepository materialRequestRepository;
    private final UserRepository userRepository;

    /**
     * Find all material handovers by tenant
     */
    public List<MaterialHandoverEntity> findByTenant(String tenantId) {
        return materialHandoverRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find material handover by ID
     */
    public Optional<MaterialHandoverEntity> findById(Long materialHandoverId) {
        return materialHandoverRepository.findByIdWithAllRelations(materialHandoverId);
    }

    /**
     * Find material handovers by material request
     */
    public List<MaterialHandoverEntity> findByMaterialRequest(Long materialRequestId) {
        return materialHandoverRepository.findByMaterialRequestIdWithRelations(materialRequestId);
    }

    /**
     * Find material handovers by status
     */
    public List<MaterialHandoverEntity> findByStatus(String tenantId, String status) {
        return materialHandoverRepository.findByTenantIdAndStatusWithRelations(tenantId, status);
    }

    /**
     * Find pending handovers by receiver
     */
    public List<MaterialHandoverEntity> findPendingByReceiver(Long receiverId) {
        return materialHandoverRepository.findPendingHandoversByReceiver(receiverId);
    }

    /**
     * Confirm handover (인수 확인)
     *
     * 워크플로우:
     * 1. 상태 검증 (PENDING만 확인 가능)
     * 2. 인수 정보 업데이트
     * 3. 상태 → CONFIRMED
     * 4. 관련 불출 신청 항목 업데이트
     */
    @Transactional
    public MaterialHandoverEntity confirmHandover(Long materialHandoverId, Long receiverId, String remarks) {
        log.info("Confirming handover: {} by user: {}", materialHandoverId, receiverId);

        MaterialHandoverEntity handover = materialHandoverRepository.findByIdWithAllRelations(materialHandoverId)
            .orElseThrow(() -> new IllegalArgumentException("Material handover not found: " + materialHandoverId));

        // Validate status
        if (!"PENDING".equals(handover.getHandoverStatus())) {
            throw new IllegalStateException("Cannot confirm handover in status: " + handover.getHandoverStatus());
        }

        // Validate receiver
        if (!receiverId.equals(handover.getReceiver().getUserId())) {
            throw new IllegalStateException("Only assigned receiver can confirm handover");
        }

        // Update handover
        handover.setReceivedDate(LocalDateTime.now());
        handover.setConfirmationRemarks(remarks);
        handover.setHandoverStatus("CONFIRMED");

        MaterialHandoverEntity confirmed = materialHandoverRepository.save(handover);

        // Check if all handovers for the request are confirmed
        checkAndCompleteRequest(handover.getMaterialRequest().getMaterialRequestId());

        log.info("Confirmed handover: {}", confirmed.getHandoverNo());

        return materialHandoverRepository.findByIdWithAllRelations(confirmed.getMaterialHandoverId())
            .orElse(confirmed);
    }

    /**
     * Reject handover (인수 거부)
     */
    @Transactional
    public MaterialHandoverEntity rejectHandover(Long materialHandoverId, Long receiverId, String reason) {
        log.info("Rejecting handover: {} by user: {}", materialHandoverId, receiverId);

        MaterialHandoverEntity handover = materialHandoverRepository.findByIdWithAllRelations(materialHandoverId)
            .orElseThrow(() -> new IllegalArgumentException("Material handover not found: " + materialHandoverId));

        // Validate status
        if (!"PENDING".equals(handover.getHandoverStatus())) {
            throw new IllegalStateException("Cannot reject handover in status: " + handover.getHandoverStatus());
        }

        // Validate receiver
        if (!receiverId.equals(handover.getReceiver().getUserId())) {
            throw new IllegalStateException("Only assigned receiver can reject handover");
        }

        // Update handover
        handover.setReceivedDate(LocalDateTime.now());
        handover.setConfirmationRemarks(reason);
        handover.setHandoverStatus("REJECTED");

        MaterialHandoverEntity rejected = materialHandoverRepository.save(handover);
        log.info("Rejected handover: {}", rejected.getHandoverNo());

        return materialHandoverRepository.findByIdWithAllRelations(rejected.getMaterialHandoverId())
            .orElse(rejected);
    }

    // ================== Private Helper Methods ==================

    /**
     * Check if all handovers for a material request are confirmed
     * If so, complete the request
     */
    private void checkAndCompleteRequest(Long materialRequestId) {
        List<MaterialHandoverEntity> handovers = materialHandoverRepository
            .findByMaterialRequestIdWithRelations(materialRequestId);

        boolean allConfirmed = handovers.stream()
            .allMatch(h -> "CONFIRMED".equals(h.getHandoverStatus()));

        if (allConfirmed && !handovers.isEmpty()) {
            MaterialRequestEntity request = materialRequestRepository.findByIdWithAllRelations(materialRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Material request not found: " + materialRequestId));

            if ("ISSUED".equals(request.getRequestStatus())) {
                request.setRequestStatus("COMPLETED");
                materialRequestRepository.save(request);
                log.info("Auto-completed material request: {} (all handovers confirmed)", request.getRequestNo());
            }
        }
    }
}
