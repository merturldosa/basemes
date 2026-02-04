package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * SOP (Standard Operating Procedure) Service
 * 표준 작업 절차 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SOPService {

    private final SOPRepository sopRepository;
    private final SOPStepRepository sopStepRepository;
    private final SOPExecutionRepository sopExecutionRepository;
    private final SOPExecutionStepRepository sopExecutionStepRepository;
    private final UserRepository userRepository;

    // ==================== SOP CRUD ====================

    /**
     * Find all SOPs by tenant
     */
    @Transactional(readOnly = true)
    public List<SOPEntity> findAllSOPs(String tenantId) {
        log.debug("Finding all SOPs for tenant: {}", tenantId);
        return sopRepository.findAllByTenantIdWithSteps(tenantId);
    }

    /**
     * Find active SOPs
     */
    @Transactional(readOnly = true)
    public List<SOPEntity> findActiveSOPs(String tenantId) {
        log.debug("Finding active SOPs for tenant: {}", tenantId);
        return sopRepository.findActiveByTenantId(tenantId);
    }

    /**
     * Find approved SOPs
     */
    @Transactional(readOnly = true)
    public List<SOPEntity> findApprovedSOPs(String tenantId) {
        log.debug("Finding approved SOPs for tenant: {}", tenantId);
        return sopRepository.findApprovedByTenantId(tenantId);
    }

    /**
     * Find SOPs by type
     */
    @Transactional(readOnly = true)
    public List<SOPEntity> findSOPsByType(String tenantId, String sopType) {
        log.debug("Finding SOPs by type: {} for tenant: {}", sopType, tenantId);
        return sopRepository.findByTenantIdAndSopType(tenantId, sopType);
    }

    /**
     * Find SOPs by category
     */
    @Transactional(readOnly = true)
    public List<SOPEntity> findSOPsByCategory(String tenantId, String category) {
        log.debug("Finding SOPs by category: {} for tenant: {}", category, tenantId);
        return sopRepository.findByTenantIdAndCategory(tenantId, category);
    }

    /**
     * Find SOP by ID with steps
     */
    @Transactional(readOnly = true)
    public Optional<SOPEntity> findSOPById(Long sopId) {
        log.debug("Finding SOP by ID: {}", sopId);
        return sopRepository.findByIdWithSteps(sopId);
    }

    /**
     * Find latest SOP by code
     */
    @Transactional(readOnly = true)
    public Optional<SOPEntity> findLatestSOPByCode(String tenantId, String sopCode) {
        log.debug("Finding latest SOP by code: {} for tenant: {}", sopCode, tenantId);
        return sopRepository.findLatestBySopCode(tenantId, sopCode);
    }

    /**
     * Find SOPs by target process
     */
    @Transactional(readOnly = true)
    public List<SOPEntity> findSOPsByTargetProcess(String tenantId, String targetProcess) {
        log.debug("Finding SOPs by target process: {} for tenant: {}", targetProcess, tenantId);
        return sopRepository.findByTenantIdAndTargetProcess(tenantId, targetProcess);
    }

    /**
     * Find SOPs requiring review
     */
    @Transactional(readOnly = true)
    public List<SOPEntity> findSOPsRequiringReview(String tenantId) {
        log.debug("Finding SOPs requiring review for tenant: {}", tenantId);
        return sopRepository.findRequiringReview(tenantId, LocalDate.now());
    }

    /**
     * Find SOPs pending approval
     */
    @Transactional(readOnly = true)
    public List<SOPEntity> findSOPsPendingApproval(String tenantId) {
        log.debug("Finding SOPs pending approval for tenant: {}", tenantId);
        return sopRepository.findPendingApprovalByTenantId(tenantId);
    }

    /**
     * Create SOP
     */
    @Transactional
    public SOPEntity createSOP(SOPEntity sop) {
        log.info("Creating SOP: {} for tenant: {}", sop.getSopCode(), sop.getTenant().getTenantId());

        // Validate SOP code uniqueness
        String tenantId = sop.getTenant().getTenantId();
        if (sopRepository.existsByTenantIdAndSopCode(tenantId, sop.getSopCode())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "SOP code already exists: " + sop.getSopCode());
        }

        // Set default values
        sop.setApprovalStatus("DRAFT");
        sop.setIsActive(true);

        return sopRepository.save(sop);
    }

    /**
     * Update SOP
     */
    @Transactional
    public SOPEntity updateSOP(SOPEntity sop) {
        log.info("Updating SOP: {}", sop.getSopId());

        SOPEntity existing = sopRepository.findById(sop.getSopId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if editable
        if (!existing.isEditable()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot edit SOP with status: " + existing.getApprovalStatus());
        }

        // Update fields
        existing.setSopName(sop.getSopName());
        existing.setDescription(sop.getDescription());
        existing.setSopType(sop.getSopType());
        existing.setCategory(sop.getCategory());
        existing.setTargetProcess(sop.getTargetProcess());
        existing.setTemplate(sop.getTemplate());
        existing.setDocumentUrl(sop.getDocumentUrl());
        existing.setAttachments(sop.getAttachments());
        existing.setRequiredRole(sop.getRequiredRole());
        existing.setRestricted(sop.getRestricted());
        existing.setDisplayOrder(sop.getDisplayOrder());

        return sopRepository.save(existing);
    }

    /**
     * Delete SOP (soft delete)
     */
    @Transactional
    public void deleteSOP(Long sopId) {
        log.info("Deleting SOP: {}", sopId);

        SOPEntity sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if can delete
        if ("APPROVED".equals(sop.getApprovalStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot delete approved SOP. Mark as obsolete instead.");
        }

        sop.setIsActive(false);
        sopRepository.save(sop);
    }

    // ==================== SOP Approval Workflow ====================

    /**
     * Submit SOP for approval
     */
    @Transactional
    public SOPEntity submitForApproval(Long sopId) {
        log.info("Submitting SOP for approval: {}", sopId);

        SOPEntity sop = sopRepository.findByIdWithSteps(sopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Validate SOP has steps
        if (sop.getSteps() == null || sop.getSteps().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot submit SOP without steps");
        }

        // Change status to PENDING
        sop.setApprovalStatus("PENDING");
        sop.setRevisionDate(LocalDate.now());

        return sopRepository.save(sop);
    }

    /**
     * Approve SOP
     */
    @Transactional
    public SOPEntity approveSOP(Long sopId, Long approverId) {
        log.info("Approving SOP: {} by user: {}", sopId, approverId);

        SOPEntity sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!"PENDING".equals(sop.getApprovalStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Only PENDING SOPs can be approved");
        }

        UserEntity approver = userRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Approver not found"));

        sop.approve(approver);
        sop.setEffectiveDate(LocalDate.now());

        // Set next review date (1 year from now)
        sop.setNextReviewDate(LocalDate.now().plusYears(1));

        return sopRepository.save(sop);
    }

    /**
     * Reject SOP
     */
    @Transactional
    public SOPEntity rejectSOP(Long sopId) {
        log.info("Rejecting SOP: {}", sopId);

        SOPEntity sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!"PENDING".equals(sop.getApprovalStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Only PENDING SOPs can be rejected");
        }

        sop.reject();
        return sopRepository.save(sop);
    }

    /**
     * Mark SOP as obsolete
     */
    @Transactional
    public SOPEntity markObsolete(Long sopId) {
        log.info("Marking SOP as obsolete: {}", sopId);

        SOPEntity sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        sop.markObsolete();
        return sopRepository.save(sop);
    }

    // ==================== SOP Steps ====================

    /**
     * Add step to SOP
     */
    @Transactional
    public SOPStepEntity addStep(Long sopId, SOPStepEntity step) {
        log.info("Adding step to SOP: {}", sopId);

        SOPEntity sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!sop.isEditable()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot add steps to non-editable SOP");
        }

        // Auto-generate step number if not provided
        if (step.getStepNumber() == null) {
            Integer maxStepNumber = sopStepRepository.getMaxStepNumberBySopId(sopId);
            step.setStepNumber(maxStepNumber + 1);
        }

        step.setSop(sop);
        return sopStepRepository.save(step);
    }

    /**
     * Update step
     */
    @Transactional
    public SOPStepEntity updateStep(SOPStepEntity step) {
        log.info("Updating SOP step: {}", step.getSopStepId());

        SOPStepEntity existing = sopStepRepository.findById(step.getSopStepId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!existing.getSop().isEditable()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot update steps in non-editable SOP");
        }

        // Update fields
        existing.setStepTitle(step.getStepTitle());
        existing.setStepDescription(step.getStepDescription());
        existing.setStepType(step.getStepType());
        existing.setEstimatedDuration(step.getEstimatedDuration());
        existing.setDetailedInstruction(step.getDetailedInstruction());
        existing.setCautionNotes(step.getCautionNotes());
        existing.setQualityPoints(step.getQualityPoints());
        existing.setImageUrls(step.getImageUrls());
        existing.setVideoUrl(step.getVideoUrl());
        existing.setChecklistItems(step.getChecklistItems());
        existing.setPrerequisiteStep(step.getPrerequisiteStep());
        existing.setIsCritical(step.getIsCritical());
        existing.setIsMandatory(step.getIsMandatory());

        return sopStepRepository.save(existing);
    }

    /**
     * Delete step
     */
    @Transactional
    public void deleteStep(Long stepId) {
        log.info("Deleting SOP step: {}", stepId);

        SOPStepEntity step = sopStepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!step.getSop().isEditable()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot delete steps from non-editable SOP");
        }

        // Check if other steps depend on this step
        List<SOPStepEntity> dependentSteps = sopStepRepository.findDependentSteps(stepId);
        if (!dependentSteps.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot delete step with dependent steps");
        }

        sopStepRepository.delete(step);
    }

    // ==================== SOP Execution ====================

    /**
     * Start SOP execution
     */
    @Transactional
    public SOPExecutionEntity startExecution(Long sopId, Long executorId, String referenceType, Long referenceId, String referenceNo) {
        log.info("Starting SOP execution for SOP: {} by user: {}", sopId, executorId);

        SOPEntity sop = sopRepository.findByIdWithSteps(sopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!sop.isExecutable()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "SOP is not executable. Status: " + sop.getApprovalStatus());
        }

        UserEntity executor = userRepository.findById(executorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Executor not found"));

        // Generate execution number
        String executionNo = generateExecutionNumber(sop.getTenant().getTenantId());

        // Create execution record
        SOPExecutionEntity execution = SOPExecutionEntity.builder()
                .tenant(sop.getTenant())
                .sop(sop)
                .executionNo(executionNo)
                .executionDate(LocalDateTime.now())
                .executor(executor)
                .executorName(executor.getUserName())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .referenceNo(referenceNo)
                .build();

        execution.start();
        execution = sopExecutionRepository.save(execution);

        // Create execution step records for all SOP steps
        for (SOPStepEntity sopStep : sop.getSteps()) {
            SOPExecutionStepEntity executionStep = SOPExecutionStepEntity.builder()
                    .execution(execution)
                    .sopStep(sopStep)
                    .stepNumber(sopStep.getStepNumber())
                    .stepStatus("PENDING")
                    .build();

            sopExecutionStepRepository.save(executionStep);
        }

        return execution;
    }

    /**
     * Start execution step
     */
    @Transactional
    public SOPExecutionStepEntity startExecutionStep(Long executionId, Long stepId) {
        log.info("Starting execution step: {} for execution: {}", stepId, executionId);

        SOPExecutionStepEntity executionStep = sopExecutionStepRepository
                .findByExecutionIdAndSopStepId(executionId, stepId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!executionStep.canStart()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot start step. Check prerequisites.");
        }

        executionStep.start();
        return sopExecutionStepRepository.save(executionStep);
    }

    /**
     * Complete execution step
     */
    @Transactional
    public SOPExecutionStepEntity completeExecutionStep(Long executionStepId, String resultValue, String checklistResults) {
        log.info("Completing execution step: {}", executionStepId);

        SOPExecutionStepEntity executionStep = sopExecutionStepRepository.findById(executionStepId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        executionStep.complete(resultValue);
        executionStep.setChecklistResults(checklistResults);

        return sopExecutionStepRepository.save(executionStep);
    }

    /**
     * Complete SOP execution
     */
    @Transactional
    public SOPExecutionEntity completeExecution(Long executionId) {
        log.info("Completing SOP execution: {}", executionId);

        SOPExecutionEntity execution = sopExecutionRepository.findByIdWithSteps(executionId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if all mandatory steps are completed
        boolean allMandatoryCompleted = sopExecutionStepRepository.areAllMandatoryStepsCompleted(executionId);
        if (!allMandatoryCompleted) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot complete execution. Not all mandatory steps are completed.");
        }

        execution.complete();
        return sopExecutionRepository.save(execution);
    }

    /**
     * Cancel SOP execution
     */
    @Transactional
    public SOPExecutionEntity cancelExecution(Long executionId, String reason) {
        log.info("Cancelling SOP execution: {}", executionId);

        SOPExecutionEntity execution = sopExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        execution.cancel(reason);
        return sopExecutionRepository.save(execution);
    }

    // ==================== Helper Methods ====================

    /**
     * Generate execution number
     * Format: SOPE-YYYYMMDD-0001
     */
    private String generateExecutionNumber(String tenantId) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SOPE-" + today + "-";

        List<String> existingNumbers = sopExecutionRepository.findExecutionNumbersWithPrefix(tenantId, prefix);

        if (existingNumbers.isEmpty()) {
            return prefix + "0001";
        }

        // Extract sequence number from last execution number
        String lastNumber = existingNumbers.get(0);
        int lastSequence = Integer.parseInt(lastNumber.substring(lastNumber.lastIndexOf("-") + 1));

        return prefix + String.format("%04d", lastSequence + 1);
    }
}
