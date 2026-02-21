package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Approval Step Instance Entity
 * 결재 단계 인스턴스 엔티티 (각 결재 단계의 실제 처리 현황)
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_approval_step_instances",
    indexes = {
        @Index(name = "idx_sd_approval_step_inst_instance", columnList = "instance_id"),
        @Index(name = "idx_sd_approval_step_inst_approver", columnList = "approver_id"),
        @Index(name = "idx_sd_approval_step_inst_status", columnList = "step_status"),
        @Index(name = "idx_sd_approval_step_inst_order", columnList = "instance_id, step_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStepInstanceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_instance_id")
    private Long stepInstanceId;

    // Instance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    private ApprovalInstanceEntity instance;

    // Step (template reference)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private ApprovalLineStepEntity step;

    // Step Information
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @Column(name = "step_type", nullable = false, length = 20)
    private String stepType;

    // Approver Information
    @Column(name = "approver_id", nullable = false)
    private Long approverId;

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @Column(name = "approver_department", length = 100)
    private String approverDepartment;

    @Column(name = "approver_position", length = 100)
    private String approverPosition;

    // Delegation Information
    @Column(name = "delegated_to_id")
    private Long delegatedToId;

    @Column(name = "delegated_to_name", length = 100)
    private String delegatedToName;

    @Column(name = "delegation_reason", columnDefinition = "TEXT")
    private String delegationReason;

    // Approval Status
    @Column(name = "step_status", nullable = false, length = 20)
    @Builder.Default
    private String stepStatus = "PENDING";  // PENDING, IN_PROGRESS, APPROVED, REJECTED, SKIPPED, TIMEOUT

    // Approval Result
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Time Management
    @Column(name = "assigned_date")
    @Builder.Default
    private LocalDateTime assignedDate = LocalDateTime.now();

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // From template step settings
    @Transient
    private Boolean isMandatory;

    @Transient
    private String approvalMethod;

    @Transient
    private Boolean allowDelegation;

    /**
     * Check if step is pending
     */
    public boolean isPending() {
        return "PENDING".equals(stepStatus);
    }

    /**
     * Check if step is in progress
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(stepStatus);
    }

    /**
     * Check if approved
     */
    public boolean isApproved() {
        return "APPROVED".equals(stepStatus);
    }

    /**
     * Check if rejected
     */
    public boolean isRejected() {
        return "REJECTED".equals(stepStatus);
    }

    /**
     * Check if skipped
     */
    public boolean isSkipped() {
        return "SKIPPED".equals(stepStatus);
    }

    /**
     * Check if timed out
     */
    public boolean isTimeout() {
        return "TIMEOUT".equals(stepStatus);
    }

    /**
     * Check if step is completed (approved, rejected, skipped, or timeout)
     */
    public boolean isCompleted() {
        return isApproved() || isRejected() || isSkipped() || isTimeout();
    }

    /**
     * Check if step is active (pending or in progress)
     */
    public boolean isActive() {
        return isPending() || isInProgress();
    }

    /**
     * Check if step is delegated
     */
    public boolean isDelegated() {
        return delegatedToId != null;
    }

    /**
     * Check if step is overdue
     */
    public boolean isOverdue() {
        if (dueDate == null || isCompleted()) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Get actual approver ID (delegated or original)
     */
    public Long getActualApproverId() {
        return isDelegated() ? delegatedToId : approverId;
    }

    /**
     * Get actual approver name (delegated or original)
     */
    public String getActualApproverName() {
        return isDelegated() ? delegatedToName : approverName;
    }

    /**
     * Start processing this step
     */
    public void startProcessing() {
        this.stepStatus = "IN_PROGRESS";
    }

    /**
     * Approve this step
     */
    public void approve(String comment) {
        this.stepStatus = "APPROVED";
        this.approvalDate = LocalDateTime.now();
        this.approvalComment = comment;
    }

    /**
     * Reject this step
     */
    public void reject(String reason) {
        this.stepStatus = "REJECTED";
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * Skip this step
     */
    public void skip(String reason) {
        this.stepStatus = "SKIPPED";
        this.approvalDate = LocalDateTime.now();
        this.approvalComment = "Skipped: " + reason;
    }

    /**
     * Mark as timeout
     */
    public void timeout() {
        this.stepStatus = "TIMEOUT";
        this.approvalDate = LocalDateTime.now();
    }

    /**
     * Delegate to another user
     */
    public void delegateTo(Long delegateId, String delegateName, String reason) {
        this.delegatedToId = delegateId;
        this.delegatedToName = delegateName;
        this.delegationReason = reason;
    }

    /**
     * Calculate remaining hours until due
     */
    public long getRemainingHours() {
        if (dueDate == null || isCompleted()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dueDate)) {
            return 0;
        }
        return java.time.Duration.between(now, dueDate).toHours();
    }

    /**
     * Get processing duration in hours
     */
    public long getProcessingDurationHours() {
        if (approvalDate == null) {
            return java.time.Duration.between(assignedDate, LocalDateTime.now()).toHours();
        }
        return java.time.Duration.between(assignedDate, approvalDate).toHours();
    }

    /**
     * Check if this is an approval step
     */
    public boolean isApprovalStep() {
        return "APPROVAL".equals(stepType);
    }

    /**
     * Check if this is a review step
     */
    public boolean isReviewStep() {
        return "REVIEW".equals(stepType);
    }

    /**
     * Check if this is a notification step
     */
    public boolean isNotificationStep() {
        return "NOTIFICATION".equals(stepType);
    }
}
