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
import java.time.LocalDate;
import java.util.List;

/**
 * Equipment Service
 * 설비 마스터 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final TenantRepository tenantRepository;
    private final SiteRepository siteRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Get all equipments for tenant
     */
    public List<EquipmentEntity> getAllEquipments(String tenantId) {
        log.info("Getting all equipments for tenant: {}", tenantId);
        return equipmentRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get active equipments for tenant
     */
    public List<EquipmentEntity> getActiveEquipments(String tenantId) {
        log.info("Getting active equipments for tenant: {}", tenantId);
        return equipmentRepository.findActiveByTenantId(tenantId);
    }

    /**
     * Get equipment by ID
     */
    public EquipmentEntity getEquipmentById(Long equipmentId) {
        log.info("Getting equipment by ID: {}", equipmentId);
        return equipmentRepository.findByIdWithAllRelations(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
    }

    /**
     * Get equipments by status
     */
    public List<EquipmentEntity> getEquipmentsByStatus(String tenantId, String status) {
        log.info("Getting equipments for tenant: {} with status: {}", tenantId, status);
        return equipmentRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get equipments by type
     */
    public List<EquipmentEntity> getEquipmentsByType(String tenantId, String equipmentType) {
        log.info("Getting equipments for tenant: {} with type: {}", tenantId, equipmentType);
        return equipmentRepository.findByTenantIdAndEquipmentType(tenantId, equipmentType);
    }

    /**
     * Create equipment
     */
    @Transactional
    public EquipmentEntity createEquipment(String tenantId, EquipmentEntity equipment) {
        log.info("Creating equipment: {} for tenant: {}", equipment.getEquipmentCode(), tenantId);

        // Check duplicate
        if (equipmentRepository.existsByTenant_TenantIdAndEquipmentCode(tenantId, equipment.getEquipmentCode())) {
            throw new BusinessException(ErrorCode.EQUIPMENT_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        equipment.setTenant(tenant);

        // Set optional relations
        if (equipment.getSite() != null && equipment.getSite().getSiteId() != null) {
            SiteEntity site = siteRepository.findByIdWithAllRelations(equipment.getSite().getSiteId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SITE_NOT_FOUND));
            equipment.setSite(site);
        }

        if (equipment.getDepartment() != null && equipment.getDepartment().getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findByIdWithAllRelations(equipment.getDepartment().getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            equipment.setDepartment(department);
        }

        // Set defaults
        if (equipment.getPurchasePrice() == null) {
            equipment.setPurchasePrice(BigDecimal.ZERO);
        }
        if (equipment.getPowerRating() == null) {
            equipment.setPowerRating(BigDecimal.ZERO);
        }
        if (equipment.getWeight() == null) {
            equipment.setWeight(BigDecimal.ZERO);
        }
        if (equipment.getIsActive() == null) {
            equipment.setIsActive(true);
        }
        if (equipment.getStatus() == null) {
            equipment.setStatus("OPERATIONAL");
        }

        // Calculate next maintenance date if maintenance cycle is set
        if (equipment.getMaintenanceCycleDays() != null && equipment.getLastMaintenanceDate() != null) {
            equipment.setNextMaintenanceDate(
                equipment.getLastMaintenanceDate().plusDays(equipment.getMaintenanceCycleDays())
            );
        }

        EquipmentEntity saved = equipmentRepository.save(equipment);
        log.info("Equipment created successfully: {}", saved.getEquipmentCode());
        return saved;
    }

    /**
     * Update equipment
     */
    @Transactional
    public EquipmentEntity updateEquipment(Long equipmentId, EquipmentEntity updateData) {
        log.info("Updating equipment ID: {}", equipmentId);

        EquipmentEntity existing = equipmentRepository.findByIdWithAllRelations(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));

        // Update fields
        if (updateData.getEquipmentName() != null) {
            existing.setEquipmentName(updateData.getEquipmentName());
        }
        if (updateData.getEquipmentType() != null) {
            existing.setEquipmentType(updateData.getEquipmentType());
        }
        if (updateData.getEquipmentCategory() != null) {
            existing.setEquipmentCategory(updateData.getEquipmentCategory());
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
        if (updateData.getLocation() != null) {
            existing.setLocation(updateData.getLocation());
        }
        if (updateData.getCapacity() != null) {
            existing.setCapacity(updateData.getCapacity());
        }
        if (updateData.getPowerRating() != null) {
            existing.setPowerRating(updateData.getPowerRating());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getMaintenanceCycleDays() != null) {
            existing.setMaintenanceCycleDays(updateData.getMaintenanceCycleDays());
            // Recalculate next maintenance date
            if (existing.getLastMaintenanceDate() != null) {
                existing.setNextMaintenanceDate(
                    existing.getLastMaintenanceDate().plusDays(updateData.getMaintenanceCycleDays())
                );
            }
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        EquipmentEntity updated = equipmentRepository.save(existing);
        log.info("Equipment updated successfully: {}", updated.getEquipmentCode());
        return updated;
    }

    /**
     * Change equipment status
     */
    @Transactional
    public EquipmentEntity changeStatus(Long equipmentId, String status) {
        log.info("Changing equipment ID: {} status to: {}", equipmentId, status);

        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));

        equipment.setStatus(status);

        EquipmentEntity updated = equipmentRepository.save(equipment);
        log.info("Equipment status changed successfully: {} -> {}", updated.getEquipmentCode(), status);
        return updated;
    }

    /**
     * Record maintenance
     */
    @Transactional
    public EquipmentEntity recordMaintenance(Long equipmentId, LocalDate maintenanceDate) {
        log.info("Recording maintenance for equipment ID: {} on: {}", equipmentId, maintenanceDate);

        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));

        equipment.setLastMaintenanceDate(maintenanceDate);

        // Calculate next maintenance date
        if (equipment.getMaintenanceCycleDays() != null) {
            equipment.setNextMaintenanceDate(
                maintenanceDate.plusDays(equipment.getMaintenanceCycleDays())
            );
        }

        EquipmentEntity updated = equipmentRepository.save(equipment);
        log.info("Maintenance recorded successfully for: {}", updated.getEquipmentCode());
        return updated;
    }

    /**
     * Activate equipment
     */
    @Transactional
    public EquipmentEntity activate(Long equipmentId) {
        log.info("Activating equipment ID: {}", equipmentId);

        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));

        equipment.setIsActive(true);

        EquipmentEntity updated = equipmentRepository.save(equipment);
        log.info("Equipment activated successfully: {}", updated.getEquipmentCode());
        return updated;
    }

    /**
     * Deactivate equipment
     */
    @Transactional
    public EquipmentEntity deactivate(Long equipmentId) {
        log.info("Deactivating equipment ID: {}", equipmentId);

        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));

        equipment.setIsActive(false);

        EquipmentEntity updated = equipmentRepository.save(equipment);
        log.info("Equipment deactivated successfully: {}", updated.getEquipmentCode());
        return updated;
    }

    /**
     * Delete equipment
     */
    @Transactional
    public void deleteEquipment(Long equipmentId) {
        log.info("Deleting equipment ID: {}", equipmentId);

        EquipmentEntity equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));

        equipmentRepository.delete(equipment);
        log.info("Equipment deleted successfully: {}", equipment.getEquipmentCode());
    }
}
