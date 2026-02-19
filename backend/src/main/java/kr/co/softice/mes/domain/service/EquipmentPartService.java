package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Equipment Part Service
 * 설비 부품 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EquipmentPartService {

    private final EquipmentPartRepository equipmentPartRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;

    /**
     * Get all parts for tenant
     */
    public List<EquipmentPartEntity> getAllParts(String tenantId) {
        log.info("Getting all equipment parts for tenant: {}", tenantId);
        return equipmentPartRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get part by ID
     */
    public EquipmentPartEntity getPartById(Long partId) {
        log.info("Getting equipment part by ID: {}", partId);
        return equipmentPartRepository.findByIdWithAllRelations(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_PART_NOT_FOUND));
    }

    /**
     * Get parts by equipment
     */
    public List<EquipmentPartEntity> getPartsByEquipment(Long equipmentId) {
        log.info("Getting parts for equipment ID: {}", equipmentId);
        return equipmentPartRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Get parts needing replacement by due date
     */
    public List<EquipmentPartEntity> getNeedsReplacement(String tenantId, LocalDate dueDate) {
        log.info("Getting parts needing replacement by {} for tenant: {}", dueDate, tenantId);
        return equipmentPartRepository.findNeedsReplacement(tenantId, dueDate);
    }

    /**
     * Create equipment part
     */
    @Transactional
    public EquipmentPartEntity createPart(String tenantId, EquipmentPartEntity part, Long equipmentId) {
        log.info("Creating equipment part: {} for tenant: {}", part.getPartCode(), tenantId);

        // Check duplicate (tenantId + equipmentId + partCode)
        if (equipmentPartRepository.existsByTenant_TenantIdAndEquipment_EquipmentIdAndPartCode(tenantId, equipmentId, part.getPartCode())) {
            throw new BusinessException(ErrorCode.EQUIPMENT_PART_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        part.setTenant(tenant);

        // Set equipment (required)
        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
        part.setEquipment(equipment);

        // Set defaults
        if (part.getIsActive() == null) {
            part.setIsActive(true);
        }
        if (part.getStatus() == null) {
            part.setStatus("ACTIVE");
        }
        if (part.getReplacementCount() == null) {
            part.setReplacementCount(0);
        }

        // Calculate nextReplacementDate if expectedLifeDays and installationDate are set
        if (part.getExpectedLifeDays() != null && part.getInstallationDate() != null) {
            part.setNextReplacementDate(part.getInstallationDate().plusDays(part.getExpectedLifeDays()));
        }

        EquipmentPartEntity saved = equipmentPartRepository.save(part);
        log.info("Equipment part created successfully: {}", saved.getPartCode());
        return saved;
    }

    /**
     * Update equipment part
     */
    @Transactional
    public EquipmentPartEntity updatePart(Long partId, EquipmentPartEntity updateData, Long equipmentId) {
        log.info("Updating equipment part ID: {}", partId);

        EquipmentPartEntity existing = equipmentPartRepository.findByIdWithAllRelations(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_PART_NOT_FOUND));

        // Update fields
        if (updateData.getPartName() != null) {
            existing.setPartName(updateData.getPartName());
        }
        if (updateData.getPartType() != null) {
            existing.setPartType(updateData.getPartType());
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
        if (updateData.getInstallationDate() != null) {
            existing.setInstallationDate(updateData.getInstallationDate());
        }
        if (updateData.getExpectedLifeDays() != null) {
            existing.setExpectedLifeDays(updateData.getExpectedLifeDays());
        }
        if (updateData.getUnitPrice() != null) {
            existing.setUnitPrice(updateData.getUnitPrice());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Update equipment (optional)
        if (equipmentId != null) {
            EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
            existing.setEquipment(equipment);
        }

        EquipmentPartEntity updated = equipmentPartRepository.save(existing);
        log.info("Equipment part updated successfully: {}", updated.getPartCode());
        return updated;
    }

    /**
     * Record replacement
     * KEY LOGIC: set replacementDate, increment replacementCount, recalculate nextReplacementDate if expectedLifeDays set
     */
    @Transactional
    public EquipmentPartEntity recordReplacement(Long partId, LocalDate replacementDate) {
        log.info("Recording replacement for part ID: {} on date: {}", partId, replacementDate);

        EquipmentPartEntity existing = equipmentPartRepository.findByIdWithAllRelations(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_PART_NOT_FOUND));

        // Set replacement date
        existing.setReplacementDate(replacementDate);

        // Increment replacement count
        int currentCount = existing.getReplacementCount() != null ? existing.getReplacementCount() : 0;
        existing.setReplacementCount(currentCount + 1);

        // Recalculate nextReplacementDate if expectedLifeDays is set
        if (existing.getExpectedLifeDays() != null) {
            existing.setNextReplacementDate(replacementDate.plusDays(existing.getExpectedLifeDays()));
        }

        EquipmentPartEntity updated = equipmentPartRepository.save(existing);
        log.info("Replacement recorded successfully for part: {}, count: {}", updated.getPartCode(), updated.getReplacementCount());
        return updated;
    }

    /**
     * Delete equipment part
     */
    @Transactional
    public void deletePart(Long partId) {
        log.info("Deleting equipment part ID: {}", partId);

        EquipmentPartEntity part = equipmentPartRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_PART_NOT_FOUND));

        equipmentPartRepository.delete(part);
        log.info("Equipment part deleted successfully: {}", part.getPartCode());
    }
}
