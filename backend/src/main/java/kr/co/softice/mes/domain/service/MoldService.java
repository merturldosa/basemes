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
import java.util.List;

/**
 * Mold Service
 * 금형 마스터 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MoldService {

    private final MoldRepository moldRepository;
    private final TenantRepository tenantRepository;
    private final SiteRepository siteRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Get all molds for tenant
     */
    public List<MoldEntity> getAllMolds(String tenantId) {
        log.info("Getting all molds for tenant: {}", tenantId);
        return moldRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get active molds for tenant
     */
    public List<MoldEntity> getActiveMolds(String tenantId) {
        log.info("Getting active molds for tenant: {}", tenantId);
        return moldRepository.findActiveByTenantId(tenantId);
    }

    /**
     * Get mold by ID
     */
    public MoldEntity getMoldById(Long moldId) {
        log.info("Getting mold by ID: {}", moldId);
        return moldRepository.findByIdWithAllRelations(moldId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));
    }

    /**
     * Get molds by status
     */
    public List<MoldEntity> getMoldsByStatus(String tenantId, String status) {
        log.info("Getting molds for tenant: {} with status: {}", tenantId, status);
        return moldRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get molds by type
     */
    public List<MoldEntity> getMoldsByType(String tenantId, String moldType) {
        log.info("Getting molds for tenant: {} with type: {}", tenantId, moldType);
        return moldRepository.findByTenantIdAndMoldType(tenantId, moldType);
    }

    /**
     * Get molds requiring maintenance
     */
    public List<MoldEntity> getMoldsRequiringMaintenance(String tenantId) {
        log.info("Getting molds requiring maintenance for tenant: {}", tenantId);
        return moldRepository.findMoldsRequiringMaintenance(tenantId);
    }

    /**
     * Create mold
     */
    @Transactional
    public MoldEntity createMold(String tenantId, MoldEntity mold) {
        log.info("Creating mold: {} for tenant: {}", mold.getMoldCode(), tenantId);

        // Check duplicate
        if (moldRepository.existsByTenant_TenantIdAndMoldCode(tenantId, mold.getMoldCode())) {
            throw new BusinessException(ErrorCode.MOLD_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        mold.setTenant(tenant);

        // Set optional relations
        if (mold.getSite() != null && mold.getSite().getSiteId() != null) {
            SiteEntity site = siteRepository.findByIdWithAllRelations(mold.getSite().getSiteId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SITE_NOT_FOUND));
            mold.setSite(site);
        }

        if (mold.getDepartment() != null && mold.getDepartment().getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findByIdWithAllRelations(mold.getDepartment().getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            mold.setDepartment(department);
        }

        // Set defaults
        if (mold.getCurrentShotCount() == null) {
            mold.setCurrentShotCount(0L);
        }
        if (mold.getLastMaintenanceShot() == null) {
            mold.setLastMaintenanceShot(0L);
        }
        if (mold.getPurchasePrice() == null) {
            mold.setPurchasePrice(BigDecimal.ZERO);
        }
        if (mold.getWeight() == null) {
            mold.setWeight(BigDecimal.ZERO);
        }
        if (mold.getIsActive() == null) {
            mold.setIsActive(true);
        }
        if (mold.getStatus() == null) {
            mold.setStatus("AVAILABLE");
        }

        MoldEntity saved = moldRepository.save(mold);
        log.info("Mold created successfully: {}", saved.getMoldCode());
        return saved;
    }

    /**
     * Update mold
     */
    @Transactional
    public MoldEntity updateMold(Long moldId, MoldEntity updateData) {
        log.info("Updating mold ID: {}", moldId);

        MoldEntity existing = moldRepository.findByIdWithAllRelations(moldId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));

        // Update fields
        if (updateData.getMoldName() != null) {
            existing.setMoldName(updateData.getMoldName());
        }
        if (updateData.getMoldType() != null) {
            existing.setMoldType(updateData.getMoldType());
        }
        if (updateData.getMoldGrade() != null) {
            existing.setMoldGrade(updateData.getMoldGrade());
        }
        if (updateData.getCavityCount() != null) {
            existing.setCavityCount(updateData.getCavityCount());
        }
        if (updateData.getMaxShotCount() != null) {
            existing.setMaxShotCount(updateData.getMaxShotCount());
        }
        if (updateData.getMaintenanceShotInterval() != null) {
            existing.setMaintenanceShotInterval(updateData.getMaintenanceShotInterval());
        }
        if (updateData.getManufacturer() != null) {
            existing.setManufacturer(updateData.getManufacturer());
        }
        if (updateData.getModelName() != null) {
            existing.setModelName(updateData.getModelName());
        }
        if (updateData.getSerialNo() != null) {
            existing.setSerialNo(updateData.getSerialNo());
        }
        if (updateData.getMaterial() != null) {
            existing.setMaterial(updateData.getMaterial());
        }
        if (updateData.getLocation() != null) {
            existing.setLocation(updateData.getLocation());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        MoldEntity updated = moldRepository.save(existing);
        log.info("Mold updated successfully: {}", updated.getMoldCode());
        return updated;
    }

    /**
     * Change mold status
     */
    @Transactional
    public MoldEntity changeStatus(Long moldId, String status) {
        log.info("Changing mold ID: {} status to: {}", moldId, status);

        MoldEntity mold = moldRepository.findByIdWithAllRelations(moldId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));

        mold.setStatus(status);

        MoldEntity updated = moldRepository.save(mold);
        log.info("Mold status changed successfully: {} -> {}", updated.getMoldCode(), status);
        return updated;
    }

    /**
     * Reset shot count (after major maintenance/overhaul)
     */
    @Transactional
    public MoldEntity resetShotCount(Long moldId) {
        log.info("Resetting shot count for mold ID: {}", moldId);

        MoldEntity mold = moldRepository.findByIdWithAllRelations(moldId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));

        mold.setCurrentShotCount(0L);
        mold.setLastMaintenanceShot(0L);

        MoldEntity updated = moldRepository.save(mold);
        log.info("Shot count reset successfully for: {}", updated.getMoldCode());
        return updated;
    }

    /**
     * Check if maintenance is required
     */
    public boolean isMaintenanceRequired(Long moldId) {
        MoldEntity mold = getMoldById(moldId);

        if (mold.getMaintenanceShotInterval() == null) {
            return false;
        }

        long shotsSinceLastMaintenance = mold.getCurrentShotCount() - mold.getLastMaintenanceShot();
        return shotsSinceLastMaintenance >= mold.getMaintenanceShotInterval();
    }

    /**
     * Activate mold
     */
    @Transactional
    public MoldEntity activate(Long moldId) {
        log.info("Activating mold ID: {}", moldId);

        MoldEntity mold = moldRepository.findByIdWithAllRelations(moldId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));

        mold.setIsActive(true);

        MoldEntity updated = moldRepository.save(mold);
        log.info("Mold activated successfully: {}", updated.getMoldCode());
        return updated;
    }

    /**
     * Deactivate mold
     */
    @Transactional
    public MoldEntity deactivate(Long moldId) {
        log.info("Deactivating mold ID: {}", moldId);

        MoldEntity mold = moldRepository.findByIdWithAllRelations(moldId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));

        mold.setIsActive(false);

        MoldEntity updated = moldRepository.save(mold);
        log.info("Mold deactivated successfully: {}", updated.getMoldCode());
        return updated;
    }

    /**
     * Delete mold
     */
    @Transactional
    public void deleteMold(Long moldId) {
        log.info("Deleting mold ID: {}", moldId);

        MoldEntity mold = moldRepository.findById(moldId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));

        moldRepository.delete(mold);
        log.info("Mold deleted successfully: {}", mold.getMoldCode());
    }
}
