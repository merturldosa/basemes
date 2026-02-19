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

/**
 * Deviation Service
 * 이탈 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DeviationService {

    private final DeviationRepository deviationRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    /**
     * Get all deviations for tenant
     */
    public List<DeviationEntity> getAllDeviations(String tenantId) {
        log.info("Getting all deviations for tenant: {}", tenantId);
        return deviationRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get deviation by ID
     */
    public DeviationEntity getDeviationById(Long deviationId) {
        log.info("Getting deviation by ID: {}", deviationId);
        return deviationRepository.findByIdWithAllRelations(deviationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEVIATION_NOT_FOUND));
    }

    /**
     * Get deviations by status for tenant
     */
    public List<DeviationEntity> getByStatus(String tenantId, String status) {
        log.info("Getting deviations for tenant: {} with status: {}", tenantId, status);
        return deviationRepository.findByStatus(tenantId, status);
    }

    /**
     * Get deviations by equipment ID
     */
    public List<DeviationEntity> getByEquipment(Long equipmentId) {
        log.info("Getting deviations for equipment ID: {}", equipmentId);
        return deviationRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Create deviation
     */
    @Transactional
    public DeviationEntity createDeviation(String tenantId, DeviationEntity deviation,
                                           Long equipmentId, Long detectedByUserId) {
        log.info("Creating deviation for tenant: {} equipment: {}", tenantId, equipmentId);

        // Check duplicate
        if (deviationRepository.existsByTenant_TenantIdAndDeviationNo(tenantId, deviation.getDeviationNo())) {
            throw new BusinessException(ErrorCode.DEVIATION_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        deviation.setTenant(tenant);

        // Set equipment (required)
        EquipmentEntity equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
        deviation.setEquipment(equipment);

        // Set detected by user (optional)
        if (detectedByUserId != null) {
            UserEntity detectedByUser = userRepository.findByIdWithAllRelations(detectedByUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            deviation.setDetectedByUser(detectedByUser);
        }

        // Set default status
        if (deviation.getStatus() == null) {
            deviation.setStatus("OPEN");
        }

        DeviationEntity saved = deviationRepository.save(deviation);
        log.info("Deviation created successfully: {}", saved.getDeviationId());
        return saved;
    }

    /**
     * Update deviation
     */
    @Transactional
    public DeviationEntity updateDeviation(Long deviationId, DeviationEntity updateData, Long resolvedByUserId) {
        log.info("Updating deviation ID: {}", deviationId);

        DeviationEntity existing = deviationRepository.findByIdWithAllRelations(deviationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEVIATION_NOT_FOUND));

        // Update non-null fields
        if (updateData.getParameterName() != null) {
            existing.setParameterName(updateData.getParameterName());
        }
        if (updateData.getStandardValue() != null) {
            existing.setStandardValue(updateData.getStandardValue());
        }
        if (updateData.getActualValue() != null) {
            existing.setActualValue(updateData.getActualValue());
        }
        if (updateData.getDeviationValue() != null) {
            existing.setDeviationValue(updateData.getDeviationValue());
        }
        if (updateData.getSeverity() != null) {
            existing.setSeverity(updateData.getSeverity());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getRootCause() != null) {
            existing.setRootCause(updateData.getRootCause());
        }
        if (updateData.getCorrectiveAction() != null) {
            existing.setCorrectiveAction(updateData.getCorrectiveAction());
        }
        if (updateData.getPreventiveAction() != null) {
            existing.setPreventiveAction(updateData.getPreventiveAction());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Update resolved by user if provided
        if (resolvedByUserId != null) {
            UserEntity resolvedByUser = userRepository.findByIdWithAllRelations(resolvedByUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            existing.setResolvedByUser(resolvedByUser);
        }

        DeviationEntity updated = deviationRepository.save(existing);
        log.info("Deviation updated successfully: {}", updated.getDeviationId());
        return updated;
    }

    /**
     * Change deviation status
     * Workflow: OPEN -> INVESTIGATING -> RESOLVED -> CLOSED
     */
    @Transactional
    public DeviationEntity changeStatus(Long deviationId, String newStatus) {
        log.info("Changing deviation ID: {} status to: {}", deviationId, newStatus);

        DeviationEntity existing = deviationRepository.findByIdWithAllRelations(deviationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEVIATION_NOT_FOUND));

        validateStatusTransition(existing.getStatus(), newStatus);
        existing.setStatus(newStatus);

        // When RESOLVED: set resolvedAt
        if ("RESOLVED".equals(newStatus)) {
            existing.setResolvedAt(LocalDateTime.now());
        }

        DeviationEntity updated = deviationRepository.save(existing);
        log.info("Deviation status changed successfully: {} -> {}", deviationId, newStatus);
        return updated;
    }

    /**
     * Delete deviation
     */
    @Transactional
    public void deleteDeviation(Long deviationId) {
        log.info("Deleting deviation ID: {}", deviationId);

        DeviationEntity deviation = deviationRepository.findById(deviationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEVIATION_NOT_FOUND));

        deviationRepository.delete(deviation);
        log.info("Deviation deleted successfully: {}", deviation.getDeviationId());
    }

    /**
     * Validate status transition: OPEN -> INVESTIGATING -> RESOLVED -> CLOSED
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return;
        }

        boolean valid = false;
        switch (currentStatus) {
            case "OPEN":
                valid = "INVESTIGATING".equals(newStatus);
                break;
            case "INVESTIGATING":
                valid = "RESOLVED".equals(newStatus);
                break;
            case "RESOLVED":
                valid = "CLOSED".equals(newStatus);
                break;
            case "CLOSED":
                // No further transitions allowed
                break;
        }

        if (!valid) {
            log.warn("Invalid status transition: {} -> {}", currentStatus, newStatus);
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}
