package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.ReturnEntity;
import kr.co.softice.mes.domain.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Return Service
 * 반품 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRepository returnRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Find all returns by tenant
     */
    @Transactional(readOnly = true)
    public List<ReturnEntity> findAllByTenant(String tenantId) {
        return returnRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find return by ID with all relations
     */
    @Transactional(readOnly = true)
    public ReturnEntity findById(Long returnId) {
        return returnRepository.findByIdWithAllRelations(returnId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));
    }

    /**
     * Find returns by status
     */
    @Transactional(readOnly = true)
    public List<ReturnEntity> findByStatus(String tenantId, String status) {
        return returnRepository.findByTenantIdAndStatusWithRelations(tenantId, status);
    }

    /**
     * Find returns by type
     */
    @Transactional(readOnly = true)
    public List<ReturnEntity> findByType(String tenantId, String type) {
        return returnRepository.findByTenantIdAndTypeWithRelations(tenantId, type);
    }

    /**
     * Find returns requiring inspection
     */
    @Transactional(readOnly = true)
    public List<ReturnEntity> findRequiringInspection(String tenantId) {
        return returnRepository.findReturnsRequiringInspection(tenantId);
    }

    /**
     * Create return
     */
    @Transactional
    public ReturnEntity createReturn(ReturnEntity returnEntity) {
        String tenantId = returnEntity.getTenant().getTenantId();
        log.info("Creating return for tenant: {}", tenantId);

        // Generate return number
        String returnNo = generateReturnNo(tenantId);
        returnEntity.setReturnNo(returnNo);
        returnEntity.setReturnStatus("PENDING");
        returnEntity.setReturnDate(LocalDateTime.now());

        // Calculate totals
        returnEntity.calculateTotals();

        ReturnEntity saved = returnRepository.save(returnEntity);
        log.info("Return created: {}", returnNo);
        return returnRepository.findByIdWithAllRelations(saved.getReturnId()).orElse(saved);
    }

    /**
     * Approve return
     */
    @Transactional
    public ReturnEntity approve(Long returnId, Long approverId, String approverName) {
        ReturnEntity returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));

        validateStatusTransition(returnEntity.getReturnStatus(), "APPROVED");

        returnEntity.setReturnStatus("APPROVED");
        returnEntity.setApprovedDate(LocalDateTime.now());
        returnEntity.setApproverName(approverName);

        log.info("Return approved: {}", returnEntity.getReturnNo());
        return returnRepository.save(returnEntity);
    }

    /**
     * Receive return
     */
    @Transactional
    public ReturnEntity receive(Long returnId) {
        ReturnEntity returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));

        validateStatusTransition(returnEntity.getReturnStatus(), "RECEIVED");

        returnEntity.setReturnStatus("RECEIVED");
        returnEntity.setReceivedDate(LocalDateTime.now());

        log.info("Return received: {}", returnEntity.getReturnNo());
        return returnRepository.save(returnEntity);
    }

    /**
     * Start inspection
     */
    @Transactional
    public ReturnEntity startInspection(Long returnId) {
        ReturnEntity returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));

        validateStatusTransition(returnEntity.getReturnStatus(), "INSPECTING");

        returnEntity.setReturnStatus("INSPECTING");

        log.info("Return inspection started: {}", returnEntity.getReturnNo());
        return returnRepository.save(returnEntity);
    }

    /**
     * Complete return
     */
    @Transactional
    public ReturnEntity complete(Long returnId) {
        ReturnEntity returnEntity = returnRepository.findByIdWithAllRelations(returnId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));

        validateStatusTransition(returnEntity.getReturnStatus(), "COMPLETED");

        returnEntity.setReturnStatus("COMPLETED");
        returnEntity.setCompletedDate(LocalDateTime.now());
        returnEntity.calculateTotals();

        log.info("Return completed: {}", returnEntity.getReturnNo());
        return returnRepository.save(returnEntity);
    }

    /**
     * Reject return
     */
    @Transactional
    public ReturnEntity reject(Long returnId, String reason) {
        ReturnEntity returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));

        if (!"PENDING".equals(returnEntity.getReturnStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "반품이 PENDING 상태가 아닙니다.");
        }

        returnEntity.setReturnStatus("REJECTED");
        returnEntity.setRejectionReason(reason);

        log.info("Return rejected: {}", returnEntity.getReturnNo());
        return returnRepository.save(returnEntity);
    }

    /**
     * Cancel return
     */
    @Transactional
    public ReturnEntity cancel(Long returnId, String reason) {
        ReturnEntity returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));

        if ("COMPLETED".equals(returnEntity.getReturnStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "완료된 반품은 취소할 수 없습니다.");
        }

        returnEntity.setReturnStatus("CANCELLED");
        returnEntity.setCancellationReason(reason);

        log.info("Return cancelled: {}", returnEntity.getReturnNo());
        return returnRepository.save(returnEntity);
    }

    /**
     * Delete return
     */
    @Transactional
    public void deleteReturn(Long returnId) {
        ReturnEntity returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RETURN_NOT_FOUND));

        if (!"PENDING".equals(returnEntity.getReturnStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "PENDING 상태의 반품만 삭제할 수 있습니다.");
        }

        returnRepository.delete(returnEntity);
        log.info("Return deleted: {}", returnEntity.getReturnNo());
    }

    /**
     * Generate return number (RT-YYYYMMDD-0001)
     */
    private String generateReturnNo(String tenantId) {
        String dateStr = LocalDateTime.now().format(DATE_FORMAT);
        String prefix = "RT-" + dateStr + "-";

        List<ReturnEntity> todayReturns = returnRepository.findByTenant_TenantId(tenantId);
        long count = todayReturns.stream()
                .filter(r -> r.getReturnNo() != null && r.getReturnNo().startsWith(prefix))
                .count();

        return prefix + String.format("%04d", count + 1);
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        boolean valid = false;
        switch (newStatus) {
            case "APPROVED":
                valid = "PENDING".equals(currentStatus);
                break;
            case "RECEIVED":
                valid = "APPROVED".equals(currentStatus);
                break;
            case "INSPECTING":
                valid = "RECEIVED".equals(currentStatus);
                break;
            case "COMPLETED":
                valid = "INSPECTING".equals(currentStatus) || "RECEIVED".equals(currentStatus);
                break;
        }

        if (!valid) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("반품 상태를 %s에서 %s로 변경할 수 없습니다.", currentStatus, newStatus));
        }
    }
}
