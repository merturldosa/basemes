package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Approval Line Step Entity
 * 결재 라인 단계 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_approval_line_steps",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_approval_step_order", columnNames = {"template_id", "step_order"})
    },
    indexes = {
        @Index(name = "idx_sd_approval_step_template", columnList = "template_id"),
        @Index(name = "idx_sd_approval_step_order", columnList = "template_id, step_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLineStepEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;

    // Template
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ApprovalLineTemplateEntity template;

    // Step Information
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @Column(name = "step_type", nullable = false, length = 20)
    @Builder.Default
    private String stepType = "APPROVAL";  // APPROVAL, REVIEW, NOTIFICATION

    // Approver Settings
    @Column(name = "approver_type", nullable = false, length = 20)
    private String approverType;  // ROLE, POSITION, DEPARTMENT, USER

    @Column(name = "approver_role", length = 50)
    private String approverRole;

    @Column(name = "approver_position", length = 50)
    private String approverPosition;

    @Column(name = "approver_department", length = 50)
    private String approverDepartment;

    @Column(name = "approver_user_id")
    private Long approverUserId;

    // Step Settings
    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = true;

    @Column(name = "approval_method", length = 20)
    @Builder.Default
    private String approvalMethod = "SINGLE";  // SINGLE, ALL, MAJORITY

    @Column(name = "parallel_group")
    private Integer parallelGroup;

    @Column(name = "auto_approve_on_timeout")
    @Builder.Default
    private Boolean autoApproveOnTimeout = false;

    @Column(name = "timeout_hours")
    private Integer timeoutHours;

    // Delegation Settings
    @Column(name = "allow_delegation")
    @Builder.Default
    private Boolean allowDelegation = true;

    @Column(name = "allow_skip")
    @Builder.Default
    private Boolean allowSkip = false;

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

    /**
     * Check if approver is by role
     */
    public boolean isApproverByRole() {
        return "ROLE".equals(approverType);
    }

    /**
     * Check if approver is by position
     */
    public boolean isApproverByPosition() {
        return "POSITION".equals(approverType);
    }

    /**
     * Check if approver is by department
     */
    public boolean isApproverByDepartment() {
        return "DEPARTMENT".equals(approverType);
    }

    /**
     * Check if approver is a specific user
     */
    public boolean isApproverSpecificUser() {
        return "USER".equals(approverType);
    }

    /**
     * Check if this step is in a parallel group
     */
    public boolean isParallel() {
        return parallelGroup != null;
    }

    /**
     * Check if this step requires all approvers
     */
    public boolean requiresAllApprovers() {
        return "ALL".equals(approvalMethod);
    }

    /**
     * Check if this step requires majority
     */
    public boolean requiresMajority() {
        return "MAJORITY".equals(approvalMethod);
    }

    /**
     * Check if delegation is allowed
     */
    public boolean canDelegate() {
        return allowDelegation != null && allowDelegation;
    }

    /**
     * Check if step can be skipped
     */
    public boolean canSkip() {
        return allowSkip != null && allowSkip;
    }

    /**
     * Check if step has timeout
     */
    public boolean hasTimeout() {
        return timeoutHours != null && timeoutHours > 0;
    }

    /**
     * Get approver identifier
     */
    public String getApproverIdentifier() {
        switch (approverType) {
            case "ROLE":
                return approverRole;
            case "POSITION":
                return approverPosition;
            case "DEPARTMENT":
                return approverDepartment;
            case "USER":
                return approverUserId != null ? approverUserId.toString() : null;
            default:
                return null;
        }
    }
}
