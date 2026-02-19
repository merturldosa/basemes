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
 * Inspection Plan Service
 * 점검 계획 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InspectionPlanService {

    private final InspectionPlanRepository inspectionPlanRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;
    private final InspectionFormRepository inspectionFormRepository;
    private final UserRepository userRepository;

    /**
     * Get all inspection plans for tenant
     */
    public List<InspectionPlanEntity> getAllPlans(String tenantId) {
        log.info("Getting all inspection plans for tenant: {}", tenantId);
        return inspectionPlanRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get inspection plan by ID
     */
    public InspectionPlanEntity getPlanById(Long planId) {
        log.info("Getting inspection plan by ID: {}", planId);
        return inspectionPlanRepository.findByIdWithAllRelations(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_PLAN_NOT_FOUND));
    }

    /**
     * Get due inspection plans for tenant by due date
     */
    public List<InspectionPlanEntity> getDuePlans(String tenantId, LocalDate dueDate) {
        log.info("Getting due inspection plans for tenant: {} by date: {}", tenantId, dueDate);
        return inspectionPlanRepository.findDuePlans(tenantId, dueDate);
    }

    /**
     * Create inspection plan
     */
    @Transactional
    public InspectionPlanEntity createPlan(String tenantId, InspectionPlanEntity plan,
                                           Long equipmentId, Long formId, Long assignedUserId) {
        log.info("Creating inspection plan: {} for tenant: {}", plan.getPlanCode(), tenantId);

        // Check duplicate
        if (inspectionPlanRepository.existsByTenant_TenantIdAndPlanCode(tenantId, plan.getPlanCode())) {
            throw new BusinessException(ErrorCode.INSPECTION_PLAN_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        plan.setTenant(tenant);

        // Set equipment (required)
        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
        plan.setEquipment(equipment);

        // Set form (optional)
        if (formId != null) {
            InspectionFormEntity form = inspectionFormRepository.findByIdWithAllRelations(formId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_FORM_NOT_FOUND));
            plan.setForm(form);
        }

        // Set assigned user (optional)
        if (assignedUserId != null) {
            UserEntity assignedUser = userRepository.findByIdWithAllRelations(assignedUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            plan.setAssignedUser(assignedUser);
        }

        // Set defaults
        if (plan.getIsActive() == null) {
            plan.setIsActive(true);
        }
        if (plan.getStatus() == null) {
            plan.setStatus("ACTIVE");
        }

        // Calculate next due date if not set and cycle days is provided
        if (plan.getNextDueDate() == null && plan.getCycleDays() != null) {
            plan.setNextDueDate(LocalDate.now().plusDays(plan.getCycleDays()));
        }

        InspectionPlanEntity saved = inspectionPlanRepository.save(plan);
        log.info("Inspection plan created successfully: {}", saved.getPlanCode());
        return saved;
    }

    /**
     * Update inspection plan
     */
    @Transactional
    public InspectionPlanEntity updatePlan(Long planId, InspectionPlanEntity updateData,
                                           Long equipmentId, Long formId, Long assignedUserId) {
        log.info("Updating inspection plan ID: {}", planId);

        InspectionPlanEntity existing = inspectionPlanRepository.findByIdWithAllRelations(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_PLAN_NOT_FOUND));

        // Update non-null fields
        if (updateData.getPlanName() != null) {
            existing.setPlanName(updateData.getPlanName());
        }
        if (updateData.getInspectionType() != null) {
            existing.setInspectionType(updateData.getInspectionType());
        }
        if (updateData.getCycleDays() != null) {
            existing.setCycleDays(updateData.getCycleDays());
        }
        if (updateData.getNextDueDate() != null) {
            existing.setNextDueDate(updateData.getNextDueDate());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }
        if (updateData.getIsActive() != null) {
            existing.setIsActive(updateData.getIsActive());
        }

        // Update equipment if provided
        if (equipmentId != null) {
            EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
            existing.setEquipment(equipment);
        }

        // Update form if provided
        if (formId != null) {
            InspectionFormEntity form = inspectionFormRepository.findByIdWithAllRelations(formId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_FORM_NOT_FOUND));
            existing.setForm(form);
        }

        // Update assigned user if provided
        if (assignedUserId != null) {
            UserEntity assignedUser = userRepository.findByIdWithAllRelations(assignedUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            existing.setAssignedUser(assignedUser);
        }

        InspectionPlanEntity updated = inspectionPlanRepository.save(existing);
        log.info("Inspection plan updated successfully: {}", updated.getPlanCode());
        return updated;
    }

    /**
     * Execute inspection plan - update execution date and calculate next due date
     */
    @Transactional
    public InspectionPlanEntity executePlan(Long planId, LocalDate executionDate) {
        log.info("Executing inspection plan ID: {} on date: {}", planId, executionDate);

        InspectionPlanEntity plan = inspectionPlanRepository.findByIdWithAllRelations(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_PLAN_NOT_FOUND));

        // Set last execution date
        plan.setLastExecutionDate(executionDate);

        // Calculate next due date based on cycle days
        if (plan.getCycleDays() != null) {
            plan.setNextDueDate(executionDate.plusDays(plan.getCycleDays()));
        }

        InspectionPlanEntity updated = inspectionPlanRepository.save(plan);
        log.info("Inspection plan executed successfully: {} next due: {}", updated.getPlanCode(), updated.getNextDueDate());
        return updated;
    }

    /**
     * Delete inspection plan
     */
    @Transactional
    public void deletePlan(Long planId) {
        log.info("Deleting inspection plan ID: {}", planId);

        InspectionPlanEntity plan = inspectionPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_PLAN_NOT_FOUND));

        inspectionPlanRepository.delete(plan);
        log.info("Inspection plan deleted successfully: {}", plan.getPlanCode());
    }
}
