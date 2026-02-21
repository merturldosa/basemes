package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Approval Instance Entity
 * 결재 인스턴스 엔티티 (실제 문서의 결재 진행 상황)
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_approval_instances",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_approval_instance_document",
                columnNames = {"tenant_id", "document_type", "document_id"})
    },
    indexes = {
        @Index(name = "idx_sd_approval_instance_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_approval_instance_status", columnList = "approval_status"),
        @Index(name = "idx_sd_approval_instance_document", columnList = "document_type, document_id"),
        @Index(name = "idx_sd_approval_instance_requester", columnList = "requester_id"),
        @Index(name = "idx_sd_approval_instance_date", columnList = "request_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalInstanceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instance_id")
    private Long instanceId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Template
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ApprovalLineTemplateEntity template;

    // Document Information
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "document_no", length = 100)
    private String documentNo;

    @Column(name = "document_title", length = 500)
    private String documentTitle;

    @Column(name = "document_amount", precision = 15, scale = 2)
    private BigDecimal documentAmount;

    // Approval Status
    @Column(name = "approval_status", nullable = false, length = 20)
    @Builder.Default
    private String approvalStatus = "PENDING";  // PENDING, IN_PROGRESS, APPROVED, REJECTED, CANCELLED

    @Column(name = "current_step_order")
    private Integer currentStepOrder;

    // Requester Information
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "requester_name", length = 100)
    private String requesterName;

    @Column(name = "requester_department", length = 100)
    private String requesterDepartment;

    @Column(name = "request_date")
    @Builder.Default
    private LocalDateTime requestDate = LocalDateTime.now();

    @Column(name = "request_comment", columnDefinition = "TEXT")
    private String requestComment;

    // Completion Information
    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "final_approver_id")
    private Long finalApproverId;

    @Column(name = "final_approver_name", length = 100)
    private String finalApproverName;

    // Relationships
    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<ApprovalStepInstanceEntity> stepInstances = new ArrayList<>();

    /**
     * Add step instance
     */
    public void addStepInstance(ApprovalStepInstanceEntity stepInstance) {
        stepInstances.add(stepInstance);
        stepInstance.setInstance(this);
    }

    /**
     * Remove step instance
     */
    public void removeStepInstance(ApprovalStepInstanceEntity stepInstance) {
        stepInstances.remove(stepInstance);
        stepInstance.setInstance(null);
    }

    /**
     * Check if approval is pending
     */
    public boolean isPending() {
        return "PENDING".equals(approvalStatus);
    }

    /**
     * Check if approval is in progress
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(approvalStatus);
    }

    /**
     * Check if approved
     */
    public boolean isApproved() {
        return "APPROVED".equals(approvalStatus);
    }

    /**
     * Check if rejected
     */
    public boolean isRejected() {
        return "REJECTED".equals(approvalStatus);
    }

    /**
     * Check if cancelled
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(approvalStatus);
    }

    /**
     * Check if approval is completed (approved or rejected)
     */
    public boolean isCompleted() {
        return isApproved() || isRejected();
    }

    /**
     * Check if approval is active (pending or in progress)
     */
    public boolean isActive() {
        return isPending() || isInProgress();
    }

    /**
     * Get total steps count
     */
    public int getTotalSteps() {
        return stepInstances.size();
    }

    /**
     * Get completed steps count
     */
    public long getCompletedSteps() {
        return stepInstances.stream()
                .filter(step -> "APPROVED".equals(step.getStepStatus()))
                .count();
    }

    /**
     * Get pending steps count
     */
    public long getPendingSteps() {
        return stepInstances.stream()
                .filter(step -> "PENDING".equals(step.getStepStatus()) ||
                        "IN_PROGRESS".equals(step.getStepStatus()))
                .count();
    }

    /**
     * Get approval progress percentage
     */
    public double getProgressPercentage() {
        if (stepInstances.isEmpty()) {
            return 0.0;
        }
        return (double) getCompletedSteps() / stepInstances.size() * 100;
    }

    /**
     * Get current step instance
     */
    public ApprovalStepInstanceEntity getCurrentStepInstance() {
        if (currentStepOrder == null) {
            return null;
        }
        return stepInstances.stream()
                .filter(step -> step.getStepOrder().equals(currentStepOrder))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get next step instance
     */
    public ApprovalStepInstanceEntity getNextStepInstance() {
        if (currentStepOrder == null) {
            return stepInstances.isEmpty() ? null : stepInstances.get(0);
        }
        return stepInstances.stream()
                .filter(step -> step.getStepOrder() > currentStepOrder)
                .filter(step -> "PENDING".equals(step.getStepStatus()))
                .min((s1, s2) -> s1.getStepOrder().compareTo(s2.getStepOrder()))
                .orElse(null);
    }

    /**
     * Start approval process
     */
    public void startApproval() {
        this.approvalStatus = "IN_PROGRESS";
        if (!stepInstances.isEmpty()) {
            this.currentStepOrder = stepInstances.get(0).getStepOrder();
        }
    }

    /**
     * Approve instance
     */
    public void approve(Long approverId, String approverName) {
        this.approvalStatus = "APPROVED";
        this.completedDate = LocalDateTime.now();
        this.finalApproverId = approverId;
        this.finalApproverName = approverName;
    }

    /**
     * Reject instance
     */
    public void reject(Long approverId, String approverName) {
        this.approvalStatus = "REJECTED";
        this.completedDate = LocalDateTime.now();
        this.finalApproverId = approverId;
        this.finalApproverName = approverName;
    }

    /**
     * Cancel instance
     */
    public void cancel() {
        this.approvalStatus = "CANCELLED";
        this.completedDate = LocalDateTime.now();
    }

    /**
     * Move to next step
     */
    public void moveToNextStep() {
        ApprovalStepInstanceEntity nextStep = getNextStepInstance();
        if (nextStep != null) {
            this.currentStepOrder = nextStep.getStepOrder();
        }
    }

    /**
     * Check if all steps are completed
     */
    public boolean areAllStepsCompleted() {
        return stepInstances.stream()
                .filter(step -> step.getIsMandatory() != null && step.getIsMandatory())
                .allMatch(step -> "APPROVED".equals(step.getStepStatus()));
    }

    /**
     * Check if any step was rejected
     */
    public boolean isAnyStepRejected() {
        return stepInstances.stream()
                .anyMatch(step -> "REJECTED".equals(step.getStepStatus()));
    }
}
