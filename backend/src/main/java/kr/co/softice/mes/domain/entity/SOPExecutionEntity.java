package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SOP Execution Record Entity
 * SOP 실행 기록 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_sop_executions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_sop_exec_no", columnNames = {"tenant_id", "execution_no"})
    },
    indexes = {
        @Index(name = "idx_sd_sop_exec_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_sop_exec_sop", columnList = "sop_id"),
        @Index(name = "idx_sd_sop_exec_date", columnList = "execution_date"),
        @Index(name = "idx_sd_sop_exec_executor", columnList = "executor_id"),
        @Index(name = "idx_sd_sop_exec_status", columnList = "execution_status"),
        @Index(name = "idx_sd_sop_exec_reference", columnList = "reference_type, reference_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SOPExecutionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long executionId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // SOP
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sop_id", nullable = false)
    private SOPEntity sop;

    // Execution info
    @Column(name = "execution_no", nullable = false, length = 50)
    private String executionNo;

    @Column(name = "execution_date", nullable = false)
    private LocalDateTime executionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id", nullable = false)
    private UserEntity executor;

    @Column(name = "executor_name", length = 100)
    private String executorName;

    // Context - what triggered this SOP execution
    @Column(name = "reference_type", length = 50)
    private String referenceType; // WORK_ORDER, INSPECTION, MAINTENANCE, etc.

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    // Execution status
    @Column(name = "execution_status", length = 50)
    @Builder.Default
    private String executionStatus = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, FAILED, CANCELLED

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Integer duration; // Minutes

    // Completion data
    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate; // Percentage

    @Column(name = "steps_completed")
    private Integer stepsCompleted;

    @Column(name = "steps_total")
    private Integer stepsTotal;

    // Review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private UserEntity reviewer;

    @Column(name = "review_status", length = 50)
    private String reviewStatus; // PENDING, APPROVED, REJECTED

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Execution Steps (One-to-Many)
    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepNumber ASC")
    @Builder.Default
    private List<SOPExecutionStepEntity> executionSteps = new ArrayList<>();

    // Helper methods

    /**
     * Start execution
     */
    public void start() {
        this.executionStatus = "IN_PROGRESS";
        this.startTime = LocalDateTime.now();
        this.stepsTotal = this.sop.getSteps().size();
        this.stepsCompleted = 0;
        this.completionRate = BigDecimal.ZERO;
    }

    /**
     * Complete execution
     */
    public void complete() {
        this.executionStatus = "COMPLETED";
        this.endTime = LocalDateTime.now();
        this.completionRate = BigDecimal.valueOf(100);
        this.stepsCompleted = this.stepsTotal;
        calculateDuration();
    }

    /**
     * Fail execution
     */
    public void fail(String reason) {
        this.executionStatus = "FAILED";
        this.endTime = LocalDateTime.now();
        this.remarks = reason;
        calculateDuration();
    }

    /**
     * Cancel execution
     */
    public void cancel(String reason) {
        this.executionStatus = "CANCELLED";
        this.endTime = LocalDateTime.now();
        this.remarks = reason;
        calculateDuration();
    }

    /**
     * Calculate duration in minutes
     */
    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            this.duration = (int) minutes;
        }
    }

    /**
     * Update completion rate
     */
    public void updateCompletionRate() {
        if (stepsTotal == null || stepsTotal == 0) {
            this.completionRate = BigDecimal.ZERO;
            return;
        }

        long completedCount = executionSteps.stream()
                .filter(es -> "COMPLETED".equals(es.getStepStatus()))
                .count();

        this.stepsCompleted = (int) completedCount;
        this.completionRate = BigDecimal.valueOf(completedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(stepsTotal), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Approve review
     */
    public void approveReview(UserEntity reviewer, String comments) {
        this.reviewer = reviewer;
        this.reviewStatus = "APPROVED";
        this.reviewComments = comments;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * Reject review
     */
    public void rejectReview(UserEntity reviewer, String comments) {
        this.reviewer = reviewer;
        this.reviewStatus = "REJECTED";
        this.reviewComments = comments;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * Check if all critical steps are completed
     */
    public boolean areAllCriticalStepsCompleted() {
        List<SOPStepEntity> criticalSteps = sop.getSteps().stream()
                .filter(SOPStepEntity::getIsCritical)
                .collect(Collectors.toList());

        if (criticalSteps.isEmpty()) {
            return true;
        }

        return executionSteps.stream()
                .filter(es -> es.getSopStep().getIsCritical())
                .allMatch(es -> "COMPLETED".equals(es.getStepStatus()));
    }

    /**
     * Add execution step
     */
    public void addExecutionStep(SOPExecutionStepEntity executionStep) {
        executionSteps.add(executionStep);
        executionStep.setExecution(this);
    }
}
