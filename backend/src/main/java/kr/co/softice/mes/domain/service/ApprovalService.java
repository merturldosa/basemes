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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Approval Service
 * 결재 서비스 (결재 프로세스 실행 및 관리)
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalLineTemplateRepository templateRepository;
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalDelegationRepository delegationRepository;
    private final UserRepository userRepository;

    // ==================== Template Management ====================

    /**
     * Find all templates
     */
    @Transactional(readOnly = true)
    public List<ApprovalLineTemplateEntity> findAllTemplates(String tenantId) {
        return templateRepository.findAllByTenantId(tenantId);
    }

    /**
     * Find template by ID with steps
     */
    @Transactional(readOnly = true)
    public Optional<ApprovalLineTemplateEntity> findTemplateById(Long templateId) {
        return templateRepository.findByIdWithSteps(templateId);
    }

    /**
     * Find templates by document type
     */
    @Transactional(readOnly = true)
    public List<ApprovalLineTemplateEntity> findTemplatesByDocumentType(String tenantId, String documentType) {
        return templateRepository.findByTenantIdAndDocumentType(tenantId, documentType);
    }

    /**
     * Find default template for document type
     */
    @Transactional(readOnly = true)
    public Optional<ApprovalLineTemplateEntity> findDefaultTemplate(String tenantId, String documentType) {
        return templateRepository.findDefaultByTenantIdAndDocumentType(tenantId, documentType);
    }

    /**
     * Create template
     */
    @Transactional
    public ApprovalLineTemplateEntity createTemplate(ApprovalLineTemplateEntity template) {
        log.info("Creating approval template: {} for document type: {}",
                template.getTemplateName(), template.getDocumentType());

        // Check duplicate code
        String tenantId = template.getTenant().getTenantId();
        if (templateRepository.existsByTenantIdAndTemplateCode(tenantId, template.getTemplateCode())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Template code already exists: " + template.getTemplateCode());
        }

        // If set as default, remove default from others
        if (template.getIsDefault()) {
            removeDefaultFlags(tenantId, template.getDocumentType());
        }

        return templateRepository.save(template);
    }

    /**
     * Update template
     */
    @Transactional
    public ApprovalLineTemplateEntity updateTemplate(ApprovalLineTemplateEntity template) {
        log.info("Updating approval template: {}", template.getTemplateId());

        ApprovalLineTemplateEntity existing = templateRepository.findById(template.getTemplateId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // If set as default, remove default from others
        if (template.getIsDefault() && !existing.getIsDefault()) {
            removeDefaultFlags(existing.getTenant().getTenantId(), existing.getDocumentType());
        }

        // Update fields
        existing.setTemplateName(template.getTemplateName());
        existing.setDescription(template.getDescription());
        existing.setApprovalType(template.getApprovalType());
        existing.setAutoApproveAmount(template.getAutoApproveAmount());
        existing.setSkipIfSamePerson(template.getSkipIfSamePerson());
        existing.setIsDefault(template.getIsDefault());
        existing.setIsActive(template.getIsActive());

        return templateRepository.save(existing);
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        log.info("Deleting approval template: {}", templateId);

        ApprovalLineTemplateEntity template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if there are active instances using this template
        // (In real implementation, add this check)

        templateRepository.delete(template);
    }

    // ==================== Approval Instance Management ====================

    /**
     * Create approval instance
     */
    @Transactional
    public ApprovalInstanceEntity createApprovalInstance(
            String tenantId,
            String documentType,
            Long documentId,
            String documentNo,
            String documentTitle,
            BigDecimal documentAmount,
            Long requesterId,
            String requesterName,
            String requesterDepartment,
            String requestComment
    ) {
        log.info("Creating approval instance for document: {} ({})", documentNo, documentType);

        // Check if instance already exists
        if (instanceRepository.existsByDocument(tenantId, documentType, documentId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Approval instance already exists for this document");
        }

        // Find default template for document type
        ApprovalLineTemplateEntity template = findDefaultTemplate(tenantId, documentType)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "No default approval template found for document type: " + documentType));

        // Check auto-approval
        if (template.shouldAutoApprove(documentAmount)) {
            log.info("Document amount {} is below auto-approve threshold {}, auto-approving",
                    documentAmount, template.getAutoApproveAmount());
            return createAutoApprovedInstance(tenantId, template, documentType, documentId,
                    documentNo, documentTitle, documentAmount, requesterId, requesterName,
                    requesterDepartment, requestComment);
        }

        // Create instance
        ApprovalInstanceEntity instance = ApprovalInstanceEntity.builder()
                .documentType(documentType)
                .documentId(documentId)
                .documentNo(documentNo)
                .documentTitle(documentTitle)
                .documentAmount(documentAmount)
                .requesterId(requesterId)
                .requesterName(requesterName)
                .requesterDepartment(requesterDepartment)
                .requestComment(requestComment)
                .approvalStatus("PENDING")
                .build();

        // Set relationships
        instance.setTenant(template.getTenant());
        instance.setTemplate(template);

        // Create step instances from template steps
        for (ApprovalLineStepEntity templateStep : template.getSteps()) {
            ApprovalStepInstanceEntity stepInstance = createStepInstance(templateStep, instance);
            instance.addStepInstance(stepInstance);
        }

        ApprovalInstanceEntity saved = instanceRepository.save(instance);

        // Start approval process
        saved.startApproval();
        return instanceRepository.save(saved);
    }

    /**
     * Create step instance from template step
     */
    private ApprovalStepInstanceEntity createStepInstance(
            ApprovalLineStepEntity templateStep,
            ApprovalInstanceEntity instance
    ) {
        // Resolve approver (simplified - in real implementation, query user by role/position/department)
        Long approverId = resolveApprover(templateStep);

        ApprovalStepInstanceEntity stepInstance = ApprovalStepInstanceEntity.builder()
                .stepOrder(templateStep.getStepOrder())
                .stepName(templateStep.getStepName())
                .stepType(templateStep.getStepType())
                .approverId(approverId)
                .approverName(userRepository.findById(approverId)
                        .map(user -> user.getFullName())
                        .orElse("Approver " + approverId))
                .stepStatus("PENDING")
                .assignedDate(LocalDateTime.now())
                .build();

        // Set due date if timeout is configured
        if (templateStep.getTimeoutHours() != null) {
            stepInstance.setDueDate(LocalDateTime.now().plusHours(templateStep.getTimeoutHours()));
        }

        stepInstance.setStep(templateStep);
        return stepInstance;
    }

    /**
     * Resolve approver from template step
     */
    private Long resolveApprover(ApprovalLineStepEntity templateStep) {
        // Simplified - in real implementation:
        // - Query UserEntity by role/position/department
        // - Handle multiple approvers for ALL/MAJORITY methods
        // - Check delegation
        return 1L;  // Placeholder
    }

    /**
     * Create auto-approved instance
     */
    private ApprovalInstanceEntity createAutoApprovedInstance(
            String tenantId, ApprovalLineTemplateEntity template,
            String documentType, Long documentId, String documentNo, String documentTitle,
            BigDecimal documentAmount, Long requesterId, String requesterName,
            String requesterDepartment, String requestComment
    ) {
        ApprovalInstanceEntity instance = ApprovalInstanceEntity.builder()
                .documentType(documentType)
                .documentId(documentId)
                .documentNo(documentNo)
                .documentTitle(documentTitle)
                .documentAmount(documentAmount)
                .requesterId(requesterId)
                .requesterName(requesterName)
                .requesterDepartment(requesterDepartment)
                .requestComment(requestComment)
                .approvalStatus("APPROVED")
                .completedDate(LocalDateTime.now())
                .finalApproverId(0L)  // System auto-approval
                .finalApproverName("SYSTEM (Auto-approved)")
                .build();

        instance.setTenant(template.getTenant());
        instance.setTemplate(template);

        return instanceRepository.save(instance);
    }

    /**
     * Approve step
     */
    @Transactional
    public void approveStep(Long instanceId, Long stepInstanceId, Long approverId, String comment) {
        log.info("Approving step: {} in instance: {} by approver: {}", stepInstanceId, instanceId, approverId);

        ApprovalInstanceEntity instance = instanceRepository.findByIdWithStepInstances(instanceId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        ApprovalStepInstanceEntity stepInstance = instance.getStepInstances().stream()
                .filter(s -> s.getStepInstanceId().equals(stepInstanceId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Validate approver
        if (!stepInstance.getActualApproverId().equals(approverId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "User is not authorized to approve this step");
        }

        // Approve step
        stepInstance.approve(comment);

        // Check if all steps are completed
        if (instance.areAllStepsCompleted()) {
            instance.approve(approverId, stepInstance.getActualApproverName());
        } else {
            // Move to next step
            instance.moveToNextStep();
        }

        instanceRepository.save(instance);
    }

    /**
     * Reject step
     */
    @Transactional
    public void rejectStep(Long instanceId, Long stepInstanceId, Long approverId, String reason) {
        log.info("Rejecting step: {} in instance: {} by approver: {}", stepInstanceId, instanceId, approverId);

        ApprovalInstanceEntity instance = instanceRepository.findByIdWithStepInstances(instanceId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        ApprovalStepInstanceEntity stepInstance = instance.getStepInstances().stream()
                .filter(s -> s.getStepInstanceId().equals(stepInstanceId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Validate approver
        if (!stepInstance.getActualApproverId().equals(approverId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "User is not authorized to reject this step");
        }

        // Reject step
        stepInstance.reject(reason);

        // Reject entire instance
        instance.reject(approverId, stepInstance.getActualApproverName());

        instanceRepository.save(instance);
    }

    /**
     * Find pending approvals for user
     */
    @Transactional(readOnly = true)
    public List<ApprovalInstanceEntity> findPendingApprovalsForUser(String tenantId, Long userId) {
        return instanceRepository.findPendingByApprover(tenantId, userId);
    }

    /**
     * Find instances by requester
     */
    @Transactional(readOnly = true)
    public List<ApprovalInstanceEntity> findInstancesByRequester(String tenantId, Long requesterId) {
        return instanceRepository.findByTenantIdAndRequesterId(tenantId, requesterId);
    }

    /**
     * Cancel approval instance
     */
    @Transactional
    public void cancelInstance(Long instanceId, Long requesterId) {
        log.info("Cancelling instance: {} by requester: {}", instanceId, requesterId);

        ApprovalInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Only requester can cancel
        if (!instance.getRequesterId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Only requester can cancel approval");
        }

        // Cannot cancel completed approvals
        if (instance.isCompleted()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Cannot cancel completed approval");
        }

        instance.cancel();
        instanceRepository.save(instance);
    }

    // ==================== Delegation Management ====================

    /**
     * Create delegation
     */
    @Transactional
    public ApprovalDelegationEntity createDelegation(ApprovalDelegationEntity delegation) {
        log.info("Creating delegation from user: {} to user: {}",
                delegation.getDelegatorId(), delegation.getDelegateId());

        // Check overlapping delegations
        String tenantId = delegation.getTenant().getTenantId();
        List<ApprovalDelegationEntity> overlapping = delegationRepository.findOverlappingDelegations(
                tenantId, delegation.getDelegatorId(), delegation.getStartDate(), delegation.getEndDate());

        if (!overlapping.isEmpty()) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Overlapping delegation already exists for this period");
        }

        return delegationRepository.save(delegation);
    }

    /**
     * Find delegations by delegator
     */
    @Transactional(readOnly = true)
    public List<ApprovalDelegationEntity> findDelegationsByDelegator(String tenantId, Long delegatorId) {
        return delegationRepository.findByTenantIdAndDelegatorId(tenantId, delegatorId);
    }

    /**
     * Find current effective delegations
     */
    @Transactional(readOnly = true)
    public List<ApprovalDelegationEntity> findCurrentDelegations(String tenantId) {
        return delegationRepository.findCurrentEffectiveDelegations(tenantId);
    }

    /**
     * Deactivate delegation
     */
    @Transactional
    public void deactivateDelegation(Long delegationId) {
        ApprovalDelegationEntity delegation = delegationRepository.findById(delegationId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));
        delegation.deactivate();
        delegationRepository.save(delegation);
    }

    // ==================== Helper Methods ====================

    /**
     * Remove default flags from templates of same document type
     */
    private void removeDefaultFlags(String tenantId, String documentType) {
        List<ApprovalLineTemplateEntity> templates =
                templateRepository.findByTenantIdAndDocumentType(tenantId, documentType);
        for (ApprovalLineTemplateEntity template : templates) {
            if (template.getIsDefault()) {
                template.setIsDefault(false);
                templateRepository.save(template);
            }
        }
    }

    /**
     * Get approval statistics
     */
    @Transactional(readOnly = true)
    public ApprovalStatistics getStatistics(String tenantId) {
        Long pending = instanceRepository.countByTenantIdAndStatus(tenantId, "PENDING");
        Long inProgress = instanceRepository.countByTenantIdAndStatus(tenantId, "IN_PROGRESS");
        Long approved = instanceRepository.countByTenantIdAndStatus(tenantId, "APPROVED");
        Long rejected = instanceRepository.countByTenantIdAndStatus(tenantId, "REJECTED");

        return new ApprovalStatistics(pending, inProgress, approved, rejected);
    }

    /**
     * Approval statistics inner class
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ApprovalStatistics {
        private Long pending;
        private Long inProgress;
        private Long approved;
        private Long rejected;

        public Long getTotal() {
            return pending + inProgress + approved + rejected;
        }

        public Long getActive() {
            return pending + inProgress;
        }

        public double getApprovalRate() {
            long completed = approved + rejected;
            if (completed == 0) return 0.0;
            return (double) approved / completed * 100;
        }
    }
}
