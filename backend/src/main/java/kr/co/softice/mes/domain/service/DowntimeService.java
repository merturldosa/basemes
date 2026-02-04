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
 * Downtime Service
 * 비가동 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DowntimeService {

    private final DowntimeRepository downtimeRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final EquipmentOperationRepository operationRepository;
    private final UserRepository userRepository;

    /**
     * Get all downtimes for tenant
     */
    public List<DowntimeEntity> getAllDowntimes(String tenantId) {
        log.info("Getting all downtimes for tenant: {}", tenantId);
        return downtimeRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get downtime by ID
     */
    public DowntimeEntity getDowntimeById(Long downtimeId) {
        log.info("Getting downtime by ID: {}", downtimeId);
        return downtimeRepository.findByIdWithAllRelations(downtimeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOWNTIME_NOT_FOUND));
    }

    /**
     * Get downtimes by equipment
     */
    public List<DowntimeEntity> getDowntimesByEquipment(Long equipmentId) {
        log.info("Getting downtimes for equipment ID: {}", equipmentId);
        return downtimeRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Get downtimes by type
     */
    public List<DowntimeEntity> getDowntimesByType(String tenantId, String downtimeType) {
        log.info("Getting downtimes for tenant: {} with type: {}", tenantId, downtimeType);
        return downtimeRepository.findByTenantIdAndDowntimeType(tenantId, downtimeType);
    }

    /**
     * Get downtimes by date range
     */
    public List<DowntimeEntity> getDowntimesByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting downtimes for tenant: {} from {} to {}", tenantId, startDate, endDate);
        return downtimeRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Get unresolved downtimes
     */
    public List<DowntimeEntity> getUnresolvedDowntimes(String tenantId) {
        log.info("Getting unresolved downtimes for tenant: {}", tenantId);
        return downtimeRepository.findUnresolvedByTenantId(tenantId);
    }

    /**
     * Get ongoing downtimes
     */
    public List<DowntimeEntity> getOngoingDowntimes(String tenantId) {
        log.info("Getting ongoing downtimes for tenant: {}", tenantId);
        return downtimeRepository.findOngoingByTenantId(tenantId);
    }

    /**
     * Create downtime
     */
    @Transactional
    public DowntimeEntity createDowntime(String tenantId, DowntimeEntity downtime) {
        log.info("Creating downtime: {} for tenant: {}", downtime.getDowntimeCode(), tenantId);

        // Check duplicate
        if (downtimeRepository.existsByTenant_TenantIdAndDowntimeCode(tenantId, downtime.getDowntimeCode())) {
            throw new BusinessException(ErrorCode.DOWNTIME_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        downtime.setTenant(tenant);

        // Get equipment
        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(downtime.getEquipment().getEquipmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
        downtime.setEquipment(equipment);

        // Set optional relations
        if (downtime.getWorkOrder() != null && downtime.getWorkOrder().getWorkOrderId() != null) {
            WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(downtime.getWorkOrder().getWorkOrderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));
            downtime.setWorkOrder(workOrder);
        }

        if (downtime.getOperation() != null && downtime.getOperation().getOperationId() != null) {
            EquipmentOperationEntity operation = operationRepository.findByIdWithAllRelations(downtime.getOperation().getOperationId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_OPERATION_NOT_FOUND));
            downtime.setOperation(operation);
        }

        if (downtime.getResponsibleUser() != null && downtime.getResponsibleUser().getUserId() != null) {
            UserEntity responsible = userRepository.findById(downtime.getResponsibleUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            downtime.setResponsibleUser(responsible);
            downtime.setResponsibleName(responsible.getFullName());
        }

        // Set defaults
        if (downtime.getIsResolved() == null) {
            downtime.setIsResolved(false);
        }
        if (downtime.getIsActive() == null) {
            downtime.setIsActive(true);
        }

        DowntimeEntity saved = downtimeRepository.save(downtime);
        log.info("Downtime created successfully: {}", saved.getDowntimeCode());
        return saved;
    }

    /**
     * Update downtime
     */
    @Transactional
    public DowntimeEntity updateDowntime(Long downtimeId, DowntimeEntity updateData) {
        log.info("Updating downtime ID: {}", downtimeId);

        DowntimeEntity existing = downtimeRepository.findByIdWithAllRelations(downtimeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOWNTIME_NOT_FOUND));

        // Update fields
        if (updateData.getEndTime() != null) {
            existing.setEndTime(updateData.getEndTime());
        }
        if (updateData.getDowntimeType() != null) {
            existing.setDowntimeType(updateData.getDowntimeType());
        }
        if (updateData.getDowntimeCategory() != null) {
            existing.setDowntimeCategory(updateData.getDowntimeCategory());
        }
        if (updateData.getCause() != null) {
            existing.setCause(updateData.getCause());
        }
        if (updateData.getCountermeasure() != null) {
            existing.setCountermeasure(updateData.getCountermeasure());
        }
        if (updateData.getPreventiveAction() != null) {
            existing.setPreventiveAction(updateData.getPreventiveAction());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        DowntimeEntity updated = downtimeRepository.save(existing);
        log.info("Downtime updated successfully: {}", updated.getDowntimeCode());
        return updated;
    }

    /**
     * End downtime
     */
    @Transactional
    public DowntimeEntity endDowntime(Long downtimeId) {
        log.info("Ending downtime ID: {}", downtimeId);

        DowntimeEntity downtime = downtimeRepository.findByIdWithAllRelations(downtimeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOWNTIME_NOT_FOUND));

        if (downtime.getEndTime() == null) {
            downtime.setEndTime(LocalDateTime.now());
        }

        DowntimeEntity updated = downtimeRepository.save(downtime);
        log.info("Downtime ended successfully: {}", updated.getDowntimeCode());
        return updated;
    }

    /**
     * Resolve downtime
     */
    @Transactional
    public DowntimeEntity resolveDowntime(Long downtimeId) {
        log.info("Resolving downtime ID: {}", downtimeId);

        DowntimeEntity downtime = downtimeRepository.findByIdWithAllRelations(downtimeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOWNTIME_NOT_FOUND));

        downtime.setIsResolved(true);
        downtime.setResolvedAt(LocalDateTime.now());

        // Also end the downtime if not already ended
        if (downtime.getEndTime() == null) {
            downtime.setEndTime(LocalDateTime.now());
        }

        DowntimeEntity updated = downtimeRepository.save(downtime);
        log.info("Downtime resolved successfully: {}", updated.getDowntimeCode());
        return updated;
    }

    /**
     * Activate downtime
     */
    @Transactional
    public DowntimeEntity activate(Long downtimeId) {
        log.info("Activating downtime ID: {}", downtimeId);

        DowntimeEntity downtime = downtimeRepository.findByIdWithAllRelations(downtimeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOWNTIME_NOT_FOUND));

        downtime.setIsActive(true);

        DowntimeEntity updated = downtimeRepository.save(downtime);
        log.info("Downtime activated successfully: {}", updated.getDowntimeCode());
        return updated;
    }

    /**
     * Deactivate downtime
     */
    @Transactional
    public DowntimeEntity deactivate(Long downtimeId) {
        log.info("Deactivating downtime ID: {}", downtimeId);

        DowntimeEntity downtime = downtimeRepository.findByIdWithAllRelations(downtimeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOWNTIME_NOT_FOUND));

        downtime.setIsActive(false);

        DowntimeEntity updated = downtimeRepository.save(downtime);
        log.info("Downtime deactivated successfully: {}", updated.getDowntimeCode());
        return updated;
    }

    /**
     * Delete downtime
     */
    @Transactional
    public void deleteDowntime(Long downtimeId) {
        log.info("Deleting downtime ID: {}", downtimeId);

        DowntimeEntity downtime = downtimeRepository.findById(downtimeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOWNTIME_NOT_FOUND));

        downtimeRepository.delete(downtime);
        log.info("Downtime deleted successfully: {}", downtime.getDowntimeCode());
    }
}
