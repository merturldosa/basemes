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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Equipment Inspection Service
 * 설비 점검 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EquipmentInspectionService {

    private final EquipmentInspectionRepository inspectionRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentService equipmentService;
    private final UserRepository userRepository;

    /**
     * Get all inspections for tenant
     */
    public List<EquipmentInspectionEntity> getAllInspections(String tenantId) {
        log.info("Getting all inspections for tenant: {}", tenantId);
        return inspectionRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get inspection by ID
     */
    public EquipmentInspectionEntity getInspectionById(Long inspectionId) {
        log.info("Getting inspection by ID: {}", inspectionId);
        return inspectionRepository.findByIdWithAllRelations(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_INSPECTION_NOT_FOUND));
    }

    /**
     * Get inspections by equipment
     */
    public List<EquipmentInspectionEntity> getInspectionsByEquipment(Long equipmentId) {
        log.info("Getting inspections for equipment ID: {}", equipmentId);
        return inspectionRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Get inspections by type
     */
    public List<EquipmentInspectionEntity> getInspectionsByType(String tenantId, String inspectionType) {
        log.info("Getting inspections for tenant: {} with type: {}", tenantId, inspectionType);
        return inspectionRepository.findByTenantIdAndInspectionType(tenantId, inspectionType);
    }

    /**
     * Get inspections by result
     */
    public List<EquipmentInspectionEntity> getInspectionsByResult(String tenantId, String inspectionResult) {
        log.info("Getting inspections for tenant: {} with result: {}", tenantId, inspectionResult);
        return inspectionRepository.findByTenantIdAndInspectionResult(tenantId, inspectionResult);
    }

    /**
     * Create inspection
     */
    @Transactional
    public EquipmentInspectionEntity createInspection(String tenantId, EquipmentInspectionEntity inspection) {
        log.info("Creating inspection: {} for tenant: {}", inspection.getInspectionNo(), tenantId);

        // Check duplicate
        if (inspectionRepository.existsByTenant_TenantIdAndInspectionNo(tenantId, inspection.getInspectionNo())) {
            throw new BusinessException(ErrorCode.EQUIPMENT_INSPECTION_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        inspection.setTenant(tenant);

        // Get equipment
        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(inspection.getEquipment().getEquipmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
        inspection.setEquipment(equipment);

        // Set optional relations
        if (inspection.getInspectorUser() != null && inspection.getInspectorUser().getUserId() != null) {
            UserEntity inspector = userRepository.findById(inspection.getInspectorUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            inspection.setInspectorUser(inspector);
            inspection.setInspectorName(inspector.getFullName());
        }

        if (inspection.getResponsibleUser() != null && inspection.getResponsibleUser().getUserId() != null) {
            UserEntity responsible = userRepository.findById(inspection.getResponsibleUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            inspection.setResponsibleUser(responsible);
            inspection.setResponsibleUserName(responsible.getFullName());
        }

        // Set defaults
        if (inspection.getAbnormalityDetected() == null) {
            inspection.setAbnormalityDetected(false);
        }
        if (inspection.getPartsCost() == null) {
            inspection.setPartsCost(BigDecimal.ZERO);
        }
        if (inspection.getLaborCost() == null) {
            inspection.setLaborCost(BigDecimal.ZERO);
        }
        if (inspection.getIsActive() == null) {
            inspection.setIsActive(true);
        }

        // Calculate total cost
        calculateTotalCost(inspection);

        // Set next inspection date based on type and equipment maintenance cycle
        if (inspection.getNextInspectionDate() == null) {
            calculateNextInspectionDate(inspection, equipment);
        }

        EquipmentInspectionEntity saved = inspectionRepository.save(inspection);

        // Update equipment status based on inspection result
        updateEquipmentStatusAfterInspection(equipment, inspection);

        // Record maintenance in equipment if this is periodic or preventive
        if ("PERIODIC".equals(inspection.getInspectionType()) || "PREVENTIVE".equals(inspection.getInspectionType())) {
            equipmentService.recordMaintenance(equipment.getEquipmentId(), inspection.getInspectionDate().toLocalDate());
        }

        log.info("Inspection created successfully: {}", saved.getInspectionNo());
        return saved;
    }

    /**
     * Calculate total cost
     */
    private void calculateTotalCost(EquipmentInspectionEntity inspection) {
        BigDecimal partsCost = inspection.getPartsCost() != null ? inspection.getPartsCost() : BigDecimal.ZERO;
        BigDecimal laborCost = inspection.getLaborCost() != null ? inspection.getLaborCost() : BigDecimal.ZERO;
        inspection.setTotalCost(partsCost.add(laborCost));
    }

    /**
     * Calculate next inspection date
     */
    private void calculateNextInspectionDate(EquipmentInspectionEntity inspection, EquipmentEntity equipment) {
        LocalDate today = LocalDate.now();

        switch (inspection.getInspectionType()) {
            case "DAILY":
                inspection.setNextInspectionDate(today.plusDays(1));
                inspection.setNextInspectionType("DAILY");
                break;
            case "PERIODIC":
            case "PREVENTIVE":
                if (equipment.getMaintenanceCycleDays() != null) {
                    inspection.setNextInspectionDate(today.plusDays(equipment.getMaintenanceCycleDays()));
                    inspection.setNextInspectionType("PERIODIC");
                } else {
                    inspection.setNextInspectionDate(today.plusMonths(1)); // Default 1 month
                    inspection.setNextInspectionType("PERIODIC");
                }
                break;
            case "CORRECTIVE":
            case "BREAKDOWN":
                // No automatic next inspection for corrective/breakdown
                break;
        }
    }

    /**
     * Update equipment status after inspection
     */
    private void updateEquipmentStatusAfterInspection(EquipmentEntity equipment, EquipmentInspectionEntity inspection) {
        if ("FAIL".equals(inspection.getInspectionResult())) {
            if ("BREAKDOWN".equals(inspection.getInspectionType())) {
                equipmentService.changeStatus(equipment.getEquipmentId(), "BREAKDOWN");
            } else {
                equipmentService.changeStatus(equipment.getEquipmentId(), "MAINTENANCE");
            }
        } else if ("PASS".equals(inspection.getInspectionResult())) {
            // If equipment is under maintenance and inspection passed, set to operational
            if ("MAINTENANCE".equals(equipment.getStatus()) || "BREAKDOWN".equals(equipment.getStatus())) {
                equipmentService.changeStatus(equipment.getEquipmentId(), "OPERATIONAL");
            }
        }
    }

    /**
     * Update inspection
     */
    @Transactional
    public EquipmentInspectionEntity updateInspection(Long inspectionId, EquipmentInspectionEntity updateData) {
        log.info("Updating inspection ID: {}", inspectionId);

        EquipmentInspectionEntity existing = inspectionRepository.findByIdWithAllRelations(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_INSPECTION_NOT_FOUND));

        // Update fields
        if (updateData.getInspectionResult() != null) {
            existing.setInspectionResult(updateData.getInspectionResult());
        }
        if (updateData.getFindings() != null) {
            existing.setFindings(updateData.getFindings());
        }
        if (updateData.getAbnormalityDetected() != null) {
            existing.setAbnormalityDetected(updateData.getAbnormalityDetected());
        }
        if (updateData.getSeverity() != null) {
            existing.setSeverity(updateData.getSeverity());
        }
        if (updateData.getCorrectiveAction() != null) {
            existing.setCorrectiveAction(updateData.getCorrectiveAction());
        }
        if (updateData.getCorrectiveActionDate() != null) {
            existing.setCorrectiveActionDate(updateData.getCorrectiveActionDate());
        }
        if (updateData.getPartsReplaced() != null) {
            existing.setPartsReplaced(updateData.getPartsReplaced());
        }
        if (updateData.getPartsCost() != null) {
            existing.setPartsCost(updateData.getPartsCost());
        }
        if (updateData.getLaborCost() != null) {
            existing.setLaborCost(updateData.getLaborCost());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Recalculate total cost
        calculateTotalCost(existing);

        EquipmentInspectionEntity updated = inspectionRepository.save(existing);
        log.info("Inspection updated successfully: {}", updated.getInspectionNo());
        return updated;
    }

    /**
     * Complete inspection with corrective action
     */
    @Transactional
    public EquipmentInspectionEntity completeInspection(Long inspectionId) {
        log.info("Completing inspection ID: {}", inspectionId);

        EquipmentInspectionEntity inspection = inspectionRepository.findByIdWithAllRelations(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_INSPECTION_NOT_FOUND));

        if (inspection.getCorrectiveActionDate() == null) {
            inspection.setCorrectiveActionDate(LocalDateTime.now());
        }

        // Update equipment status
        updateEquipmentStatusAfterInspection(inspection.getEquipment(), inspection);

        EquipmentInspectionEntity updated = inspectionRepository.save(inspection);
        log.info("Inspection completed successfully: {}", updated.getInspectionNo());
        return updated;
    }

    /**
     * Delete inspection
     */
    @Transactional
    public void deleteInspection(Long inspectionId) {
        log.info("Deleting inspection ID: {}", inspectionId);

        EquipmentInspectionEntity inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_INSPECTION_NOT_FOUND));

        inspectionRepository.delete(inspection);
        log.info("Inspection deleted successfully: {}", inspection.getInspectionNo());
    }
}
