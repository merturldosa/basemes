package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mold Maintenance Service
 * 금형 보전 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MoldMaintenanceService {

    private final MoldMaintenanceRepository maintenanceRepository;
    private final TenantRepository tenantRepository;
    private final MoldRepository moldRepository;
    private final UserRepository userRepository;

    public List<MoldMaintenanceEntity> getAllMaintenances(String tenantId) {
        log.info("Getting all mold maintenances for tenant: {}", tenantId);
        return maintenanceRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public MoldMaintenanceEntity getMaintenanceById(Long maintenanceId) {
        log.info("Getting mold maintenance by ID: {}", maintenanceId);
        return maintenanceRepository.findByIdWithAllRelations(maintenanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_MAINTENANCE_NOT_FOUND));
    }

    public List<MoldMaintenanceEntity> getMaintenancesByMold(Long moldId) {
        log.info("Getting maintenances for mold ID: {}", moldId);
        return maintenanceRepository.findByMoldId(moldId);
    }

    public List<MoldMaintenanceEntity> getMaintenancesByType(String tenantId, String maintenanceType) {
        log.info("Getting maintenances by type: {} for tenant: {}", maintenanceType, tenantId);
        return maintenanceRepository.findByTenantIdAndMaintenanceType(tenantId, maintenanceType);
    }

    public List<MoldMaintenanceEntity> getMaintenancesByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting maintenances for tenant: {} from {} to {}", tenantId, startDate, endDate);
        return maintenanceRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Transactional
    public MoldMaintenanceEntity createMaintenance(String tenantId, MoldMaintenanceEntity maintenance) {
        log.info("Creating mold maintenance: {} for tenant: {}", maintenance.getMaintenanceNo(), tenantId);

        if (maintenanceRepository.existsByTenant_TenantIdAndMaintenanceNo(tenantId, maintenance.getMaintenanceNo())) {
            throw new BusinessException(ErrorCode.MOLD_MAINTENANCE_ALREADY_EXISTS);
        }

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        maintenance.setTenant(tenant);

        MoldEntity mold = moldRepository.findByIdWithAllRelations(maintenance.getMold().getMoldId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));
        maintenance.setMold(mold);

        // Record shot count before maintenance
        maintenance.setShotCountBefore(mold.getCurrentShotCount());

        if (maintenance.getTechnicianUser() != null && maintenance.getTechnicianUser().getUserId() != null) {
            UserEntity technician = userRepository.findById(maintenance.getTechnicianUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            maintenance.setTechnicianUser(technician);
            maintenance.setTechnicianName(technician.getFullName());
        }

        if (maintenance.getPartsCost() == null) {
            maintenance.setPartsCost(BigDecimal.ZERO);
        }
        if (maintenance.getLaborCost() == null) {
            maintenance.setLaborCost(BigDecimal.ZERO);
        }
        if (maintenance.getShotCountReset() == null) {
            maintenance.setShotCountReset(false);
        }
        if (maintenance.getIsActive() == null) {
            maintenance.setIsActive(true);
        }

        MoldMaintenanceEntity saved = maintenanceRepository.save(maintenance);

        // Update mold if shot count reset
        if (Boolean.TRUE.equals(maintenance.getShotCountReset())) {
            mold.setLastMaintenanceShot(0L);
            moldRepository.save(mold);
            saved.setShotCountAfter(0L);
        } else {
            mold.setLastMaintenanceShot(mold.getCurrentShotCount());
            moldRepository.save(mold);
            saved.setShotCountAfter(mold.getCurrentShotCount());
        }

        log.info("Mold maintenance created successfully: {}", saved.getMaintenanceNo());
        return saved;
    }

    @Transactional
    public MoldMaintenanceEntity updateMaintenance(Long maintenanceId, MoldMaintenanceEntity updateData) {
        log.info("Updating mold maintenance ID: {}", maintenanceId);

        MoldMaintenanceEntity existing = maintenanceRepository.findByIdWithAllRelations(maintenanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_MAINTENANCE_NOT_FOUND));

        if (updateData.getMaintenanceContent() != null) {
            existing.setMaintenanceContent(updateData.getMaintenanceContent());
        }
        if (updateData.getPartsReplaced() != null) {
            existing.setPartsReplaced(updateData.getPartsReplaced());
        }
        if (updateData.getFindings() != null) {
            existing.setFindings(updateData.getFindings());
        }
        if (updateData.getCorrectiveAction() != null) {
            existing.setCorrectiveAction(updateData.getCorrectiveAction());
        }
        if (updateData.getPartsCost() != null) {
            existing.setPartsCost(updateData.getPartsCost());
        }
        if (updateData.getLaborCost() != null) {
            existing.setLaborCost(updateData.getLaborCost());
        }
        if (updateData.getMaintenanceResult() != null) {
            existing.setMaintenanceResult(updateData.getMaintenanceResult());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        MoldMaintenanceEntity updated = maintenanceRepository.save(existing);
        log.info("Mold maintenance updated successfully: {}", updated.getMaintenanceNo());
        return updated;
    }

    @Transactional
    public void deleteMaintenance(Long maintenanceId) {
        log.info("Deleting mold maintenance ID: {}", maintenanceId);

        MoldMaintenanceEntity maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_MAINTENANCE_NOT_FOUND));

        maintenanceRepository.delete(maintenance);
        log.info("Mold maintenance deleted successfully: {}", maintenance.getMaintenanceNo());
    }
}
