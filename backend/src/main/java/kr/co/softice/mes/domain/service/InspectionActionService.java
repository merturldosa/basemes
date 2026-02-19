package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inspection Action Service
 * 점검 조치 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InspectionActionService {

    private final InspectionActionRepository inspectionActionRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentInspectionRepository equipmentInspectionRepository;
    private final UserRepository userRepository;

    /**
     * Get all inspection actions for tenant
     */
    public List<InspectionActionEntity> getAllActions(String tenantId) {
        log.info("Getting all inspection actions for tenant: {}", tenantId);
        return inspectionActionRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get inspection action by ID
     */
    public InspectionActionEntity getActionById(Long actionId) {
        log.info("Getting inspection action by ID: {}", actionId);
        return inspectionActionRepository.findByIdWithAllRelations(actionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_ACTION_NOT_FOUND));
    }

    /**
     * Get inspection actions by inspection ID
     */
    public List<InspectionActionEntity> getActionsByInspection(Long inspectionId) {
        log.info("Getting inspection actions for inspection ID: {}", inspectionId);
        return inspectionActionRepository.findByInspectionId(inspectionId);
    }

    /**
     * Get inspection actions by status for tenant
     */
    public List<InspectionActionEntity> getActionsByStatus(String tenantId, String status) {
        log.info("Getting inspection actions for tenant: {} with status: {}", tenantId, status);
        return inspectionActionRepository.findByStatus(tenantId, status);
    }

    /**
     * Create inspection action
     */
    @Transactional
    public InspectionActionEntity createAction(String tenantId, InspectionActionEntity action,
                                               Long inspectionId, Long assignedUserId) {
        log.info("Creating inspection action for tenant: {} inspection: {}", tenantId, inspectionId);

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        action.setTenant(tenant);

        // Set inspection (required)
        EquipmentInspectionEntity inspection = equipmentInspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_INSPECTION_NOT_FOUND));
        action.setInspection(inspection);

        // Set assigned user (optional)
        if (assignedUserId != null) {
            UserEntity assignedUser = userRepository.findByIdWithAllRelations(assignedUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            action.setAssignedUser(assignedUser);
        }

        // Set default status
        if (action.getStatus() == null) {
            action.setStatus("OPEN");
        }

        InspectionActionEntity saved = inspectionActionRepository.save(action);
        log.info("Inspection action created successfully: {}", saved.getActionId());
        return saved;
    }

    /**
     * Update inspection action
     */
    @Transactional
    public InspectionActionEntity updateAction(Long actionId, InspectionActionEntity updateData,
                                               Long assignedUserId) {
        log.info("Updating inspection action ID: {}", actionId);

        InspectionActionEntity existing = inspectionActionRepository.findByIdWithAllRelations(actionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_ACTION_NOT_FOUND));

        // Update non-null fields
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getDueDate() != null) {
            existing.setDueDate(updateData.getDueDate());
        }
        if (updateData.getCompletedDate() != null) {
            existing.setCompletedDate(updateData.getCompletedDate());
        }
        if (updateData.getResult() != null) {
            existing.setResult(updateData.getResult());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Status workflow validation: OPEN -> IN_PROGRESS -> COMPLETED
        if (updateData.getStatus() != null) {
            validateStatusTransition(existing.getStatus(), updateData.getStatus());
            existing.setStatus(updateData.getStatus());
        }

        // Update assigned user if provided
        if (assignedUserId != null) {
            UserEntity assignedUser = userRepository.findByIdWithAllRelations(assignedUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            existing.setAssignedUser(assignedUser);
        }

        InspectionActionEntity updated = inspectionActionRepository.save(existing);
        log.info("Inspection action updated successfully: {}", updated.getActionId());
        return updated;
    }

    /**
     * Delete inspection action
     */
    @Transactional
    public void deleteAction(Long actionId) {
        log.info("Deleting inspection action ID: {}", actionId);

        InspectionActionEntity action = inspectionActionRepository.findById(actionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_ACTION_NOT_FOUND));

        inspectionActionRepository.delete(action);
        log.info("Inspection action deleted successfully: {}", action.getActionId());
    }

    /**
     * Validate status transition: OPEN -> IN_PROGRESS -> COMPLETED
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return;
        }

        boolean valid = false;
        switch (currentStatus) {
            case "OPEN":
                valid = "IN_PROGRESS".equals(newStatus);
                break;
            case "IN_PROGRESS":
                valid = "COMPLETED".equals(newStatus);
                break;
            case "COMPLETED":
                // No further transitions allowed
                break;
        }

        if (!valid) {
            log.warn("Invalid status transition: {} -> {}", currentStatus, newStatus);
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}
