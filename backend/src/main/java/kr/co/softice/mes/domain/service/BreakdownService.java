package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Breakdown Service
 * 고장 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BreakdownService {

    private final BreakdownRepository breakdownRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;
    private final DowntimeRepository downtimeRepository;
    private final UserRepository userRepository;

    // Valid status transitions: REPORTED -> ASSIGNED -> IN_PROGRESS -> COMPLETED -> CLOSED
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "REPORTED", Set.of("ASSIGNED"),
            "ASSIGNED", Set.of("IN_PROGRESS"),
            "IN_PROGRESS", Set.of("COMPLETED"),
            "COMPLETED", Set.of("CLOSED")
    );

    /**
     * Get all breakdowns for tenant
     */
    public List<BreakdownEntity> getAllBreakdowns(String tenantId) {
        log.info("Getting all breakdowns for tenant: {}", tenantId);
        return breakdownRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get breakdown by ID
     */
    public BreakdownEntity getBreakdownById(Long breakdownId) {
        log.info("Getting breakdown by ID: {}", breakdownId);
        return breakdownRepository.findByIdWithAllRelations(breakdownId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BREAKDOWN_NOT_FOUND));
    }

    /**
     * Get breakdowns by status
     */
    public List<BreakdownEntity> getByStatus(String tenantId, String status) {
        log.info("Getting breakdowns for tenant: {} with status: {}", tenantId, status);
        return breakdownRepository.findByStatus(tenantId, status);
    }

    /**
     * Get breakdowns by equipment
     */
    public List<BreakdownEntity> getByEquipment(Long equipmentId) {
        log.info("Getting breakdowns for equipment ID: {}", equipmentId);
        return breakdownRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Create breakdown
     */
    @Transactional
    public BreakdownEntity createBreakdown(String tenantId, BreakdownEntity breakdown,
                                           Long equipmentId, Long downtimeId, Long reportedByUserId) {
        log.info("Creating breakdown: {} for tenant: {}", breakdown.getBreakdownNo(), tenantId);

        // Check duplicate
        if (breakdownRepository.existsByTenant_TenantIdAndBreakdownNo(tenantId, breakdown.getBreakdownNo())) {
            throw new BusinessException(ErrorCode.BREAKDOWN_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        breakdown.setTenant(tenant);

        // Get equipment
        EquipmentEntity equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
        breakdown.setEquipment(equipment);

        // Set optional downtime relation
        if (downtimeId != null) {
            DowntimeEntity downtime = downtimeRepository.findById(downtimeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOWNTIME_NOT_FOUND));
            breakdown.setDowntime(downtime);
        }

        // Set optional reported by user
        if (reportedByUserId != null) {
            UserEntity reportedByUser = userRepository.findById(reportedByUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            breakdown.setReportedByUser(reportedByUser);
        }

        // Set default status
        breakdown.setStatus("REPORTED");

        BreakdownEntity saved = breakdownRepository.save(breakdown);
        log.info("Breakdown created successfully: {}", saved.getBreakdownNo());
        return saved;
    }

    /**
     * Update breakdown
     */
    @Transactional
    public BreakdownEntity updateBreakdown(Long breakdownId, BreakdownEntity updateData, Long assignedUserId) {
        log.info("Updating breakdown ID: {}", breakdownId);

        BreakdownEntity existing = breakdownRepository.findByIdWithAllRelations(breakdownId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BREAKDOWN_NOT_FOUND));

        // Update fields
        if (updateData.getFailureType() != null) {
            existing.setFailureType(updateData.getFailureType());
        }
        if (updateData.getSeverity() != null) {
            existing.setSeverity(updateData.getSeverity());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getRepairDescription() != null) {
            existing.setRepairDescription(updateData.getRepairDescription());
        }
        if (updateData.getPartsUsed() != null) {
            existing.setPartsUsed(updateData.getPartsUsed());
        }
        if (updateData.getRepairCost() != null) {
            existing.setRepairCost(updateData.getRepairCost());
        }
        if (updateData.getRootCause() != null) {
            existing.setRootCause(updateData.getRootCause());
        }
        if (updateData.getPreventiveAction() != null) {
            existing.setPreventiveAction(updateData.getPreventiveAction());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Set optional assigned user
        if (assignedUserId != null) {
            UserEntity assignedUser = userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            existing.setAssignedUser(assignedUser);
        }

        BreakdownEntity updated = breakdownRepository.save(existing);
        log.info("Breakdown updated successfully: {}", updated.getBreakdownNo());
        return updated;
    }

    /**
     * Change breakdown status with validation
     * Valid transitions: REPORTED -> ASSIGNED -> IN_PROGRESS -> COMPLETED -> CLOSED
     */
    @Transactional
    public BreakdownEntity changeStatus(Long breakdownId, String newStatus) {
        log.info("Changing breakdown ID: {} status to: {}", breakdownId, newStatus);

        BreakdownEntity breakdown = breakdownRepository.findByIdWithAllRelations(breakdownId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BREAKDOWN_NOT_FOUND));

        String currentStatus = breakdown.getStatus();

        // Validate status transition
        Set<String> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new BusinessException(ErrorCode.BREAKDOWN_INVALID_STATUS_TRANSITION,
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }

        // Set status and timestamps based on new status
        breakdown.setStatus(newStatus);

        switch (newStatus) {
            case "ASSIGNED":
                breakdown.setAssignedAt(LocalDateTime.now());
                break;
            case "IN_PROGRESS":
                breakdown.setRepairStartedAt(LocalDateTime.now());
                break;
            case "COMPLETED":
                breakdown.setRepairCompletedAt(LocalDateTime.now());
                // Calculate repair duration if repair started
                if (breakdown.getRepairStartedAt() != null) {
                    long minutes = java.time.Duration.between(
                            breakdown.getRepairStartedAt(),
                            breakdown.getRepairCompletedAt()
                    ).toMinutes();
                    breakdown.setRepairDurationMinutes((int) minutes);
                }
                break;
            case "CLOSED":
                breakdown.setClosedAt(LocalDateTime.now());
                break;
            default:
                break;
        }

        BreakdownEntity updated = breakdownRepository.save(breakdown);
        log.info("Breakdown status changed successfully: {} -> {}", currentStatus, newStatus);
        return updated;
    }

    /**
     * Delete breakdown
     */
    @Transactional
    public void deleteBreakdown(Long breakdownId) {
        log.info("Deleting breakdown ID: {}", breakdownId);

        BreakdownEntity breakdown = breakdownRepository.findById(breakdownId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BREAKDOWN_NOT_FOUND));

        breakdownRepository.delete(breakdown);
        log.info("Breakdown deleted successfully: {}", breakdown.getBreakdownNo());
    }
}
