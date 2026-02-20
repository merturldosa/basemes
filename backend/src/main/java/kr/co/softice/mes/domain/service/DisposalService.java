package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.DisposalEntity;
import kr.co.softice.mes.domain.repository.DisposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Disposal Service
 * 폐기 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisposalService {

    private final DisposalRepository disposalRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Find all disposals by tenant
     */
    @Transactional(readOnly = true)
    public List<DisposalEntity> findAllByTenant(String tenantId) {
        return disposalRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find disposal by ID with all relations
     */
    @Transactional(readOnly = true)
    public DisposalEntity findById(Long disposalId) {
        return disposalRepository.findByIdWithAllRelations(disposalId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DISPOSAL_NOT_FOUND));
    }

    /**
     * Find disposals by status
     */
    @Transactional(readOnly = true)
    public List<DisposalEntity> findByStatus(String tenantId, String status) {
        return disposalRepository.findByTenantIdAndStatusWithRelations(tenantId, status);
    }

    /**
     * Find disposals by type
     */
    @Transactional(readOnly = true)
    public List<DisposalEntity> findByType(String tenantId, String type) {
        return disposalRepository.findByTenantIdAndTypeWithRelations(tenantId, type);
    }

    /**
     * Find approved disposals requiring processing
     */
    @Transactional(readOnly = true)
    public List<DisposalEntity> findApprovedDisposals(String tenantId) {
        return disposalRepository.findApprovedDisposals(tenantId);
    }

    /**
     * Create disposal
     */
    @Transactional
    public DisposalEntity createDisposal(DisposalEntity disposal) {
        String tenantId = disposal.getTenant().getTenantId();
        log.info("Creating disposal for tenant: {}", tenantId);

        // Generate disposal number
        String disposalNo = generateDisposalNo(tenantId);
        disposal.setDisposalNo(disposalNo);
        disposal.setDisposalStatus("PENDING");
        disposal.setDisposalDate(LocalDateTime.now());

        // Calculate totals
        disposal.calculateTotals();

        DisposalEntity saved = disposalRepository.save(disposal);
        log.info("Disposal created: {}", disposalNo);
        return disposalRepository.findByIdWithAllRelations(saved.getDisposalId()).orElse(saved);
    }

    /**
     * Approve disposal
     */
    @Transactional
    public DisposalEntity approve(Long disposalId, Long approverId, String approverName) {
        DisposalEntity disposal = disposalRepository.findById(disposalId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DISPOSAL_NOT_FOUND));

        validateStatusTransition(disposal.getDisposalStatus(), "APPROVED");

        disposal.setDisposalStatus("APPROVED");
        disposal.setApprovedDate(LocalDateTime.now());
        disposal.setApproverName(approverName);

        log.info("Disposal approved: {}", disposal.getDisposalNo());
        return disposalRepository.save(disposal);
    }

    /**
     * Process disposal
     */
    @Transactional
    public DisposalEntity process(Long disposalId, Long processorId, String processorName,
                                   String disposalMethod, String disposalLocation) {
        DisposalEntity disposal = disposalRepository.findById(disposalId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DISPOSAL_NOT_FOUND));

        validateStatusTransition(disposal.getDisposalStatus(), "PROCESSED");

        disposal.setDisposalStatus("PROCESSED");
        disposal.setProcessedDate(LocalDateTime.now());
        disposal.setProcessorName(processorName);
        disposal.setDisposalMethod(disposalMethod);
        disposal.setDisposalLocation(disposalLocation);

        log.info("Disposal processed: {}", disposal.getDisposalNo());
        return disposalRepository.save(disposal);
    }

    /**
     * Complete disposal
     */
    @Transactional
    public DisposalEntity complete(Long disposalId) {
        DisposalEntity disposal = disposalRepository.findByIdWithAllRelations(disposalId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DISPOSAL_NOT_FOUND));

        validateStatusTransition(disposal.getDisposalStatus(), "COMPLETED");

        disposal.setDisposalStatus("COMPLETED");
        disposal.setCompletedDate(LocalDateTime.now());
        disposal.calculateTotals();

        log.info("Disposal completed: {}", disposal.getDisposalNo());
        return disposalRepository.save(disposal);
    }

    /**
     * Reject disposal
     */
    @Transactional
    public DisposalEntity reject(Long disposalId, String reason) {
        DisposalEntity disposal = disposalRepository.findById(disposalId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DISPOSAL_NOT_FOUND));

        if (!"PENDING".equals(disposal.getDisposalStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "폐기가 PENDING 상태가 아닙니다.");
        }

        disposal.setDisposalStatus("REJECTED");
        disposal.setRejectionReason(reason);

        log.info("Disposal rejected: {}", disposal.getDisposalNo());
        return disposalRepository.save(disposal);
    }

    /**
     * Cancel disposal
     */
    @Transactional
    public DisposalEntity cancel(Long disposalId, String reason) {
        DisposalEntity disposal = disposalRepository.findById(disposalId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DISPOSAL_NOT_FOUND));

        if ("COMPLETED".equals(disposal.getDisposalStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "완료된 폐기는 취소할 수 없습니다.");
        }

        disposal.setDisposalStatus("CANCELLED");
        disposal.setCancellationReason(reason);

        log.info("Disposal cancelled: {}", disposal.getDisposalNo());
        return disposalRepository.save(disposal);
    }

    /**
     * Delete disposal
     */
    @Transactional
    public void deleteDisposal(Long disposalId) {
        DisposalEntity disposal = disposalRepository.findById(disposalId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DISPOSAL_NOT_FOUND));

        if (!"PENDING".equals(disposal.getDisposalStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "PENDING 상태의 폐기만 삭제할 수 있습니다.");
        }

        disposalRepository.delete(disposal);
        log.info("Disposal deleted: {}", disposal.getDisposalNo());
    }

    /**
     * Generate disposal number (DIS-YYYYMMDD-0001)
     */
    private String generateDisposalNo(String tenantId) {
        String dateStr = LocalDateTime.now().format(DATE_FORMAT);
        String prefix = "DIS-" + dateStr + "-";

        List<DisposalEntity> allDisposals = disposalRepository.findByTenant_TenantId(tenantId);
        long count = allDisposals.stream()
                .filter(d -> d.getDisposalNo() != null && d.getDisposalNo().startsWith(prefix))
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
            case "PROCESSED":
                valid = "APPROVED".equals(currentStatus);
                break;
            case "COMPLETED":
                valid = "PROCESSED".equals(currentStatus);
                break;
        }

        if (!valid) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("폐기 상태를 %s에서 %s로 변경할 수 없습니다.", currentStatus, newStatus));
        }
    }
}
