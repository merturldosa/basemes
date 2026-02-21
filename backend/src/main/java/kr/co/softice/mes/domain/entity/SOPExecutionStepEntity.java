package kr.co.softice.mes.domain.entity;

import javax.persistence.*;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import lombok.*;
import java.time.LocalDateTime;

/**
 * SOP Execution Step Result Entity
 * SOP 실행 단계별 결과 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_sop_execution_steps",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_sop_exec_step", columnNames = {"execution_id", "sop_step_id"})
    },
    indexes = {
        @Index(name = "idx_sd_sop_exec_step_exec", columnList = "execution_id"),
        @Index(name = "idx_sd_sop_exec_step_sop_step", columnList = "sop_step_id"),
        @Index(name = "idx_sd_sop_exec_step_status", columnList = "step_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SOPExecutionStepEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_step_id")
    private Long executionStepId;

    // Parent execution
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private SOPExecutionEntity execution;

    // SOP Step definition
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sop_step_id", nullable = false)
    private SOPStepEntity sopStep;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "step_status", length = 50)
    @Builder.Default
    private String stepStatus = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED, SKIPPED, FAILED

    // Execution data
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration")
    private Integer duration; // Minutes

    // Results
    @Column(name = "result_value", columnDefinition = "TEXT")
    private String resultValue;

    @Column(name = "checklist_results", columnDefinition = "JSONB")
    private String checklistResults; // Results for each checklist item

    // Evidence
    @Column(name = "photos", columnDefinition = "JSONB")
    private String photos; // Array of photo URLs

    @Column(name = "signature", length = 500)
    private String signature; // Digital signature or approver name

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Helper methods

    /**
     * Start step execution
     */
    public void start() {
        this.stepStatus = "IN_PROGRESS";
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Complete step execution
     */
    public void complete(String resultValue) {
        this.stepStatus = "COMPLETED";
        this.completedAt = LocalDateTime.now();
        this.resultValue = resultValue;
        calculateDuration();

        // Update parent execution completion rate
        if (execution != null) {
            execution.updateCompletionRate();
        }
    }

    /**
     * Skip step
     */
    public void skip(String reason) {
        if (!sopStep.isSkippable()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "필수 또는 중요 단계는 건너뛸 수 없습니다");
        }
        this.stepStatus = "SKIPPED";
        this.completedAt = LocalDateTime.now();
        this.remarks = reason;
        calculateDuration();

        // Update parent execution completion rate
        if (execution != null) {
            execution.updateCompletionRate();
        }
    }

    /**
     * Fail step
     */
    public void fail(String reason) {
        this.stepStatus = "FAILED";
        this.completedAt = LocalDateTime.now();
        this.remarks = reason;
        calculateDuration();

        // If critical step fails, fail the entire execution
        if (sopStep.getIsCritical()) {
            execution.fail("Critical step failed: " + sopStep.getStepTitle());
        }
    }

    /**
     * Calculate duration in minutes
     */
    private void calculateDuration() {
        if (startedAt != null && completedAt != null) {
            long minutes = java.time.Duration.between(startedAt, completedAt).toMinutes();
            this.duration = (int) minutes;
        }
    }

    /**
     * Add photo evidence
     */
    public void addPhoto(String photoUrl) {
        // In real implementation, append to JSONB array
        // This is a simplified version
        if (this.photos == null || this.photos.isEmpty()) {
            this.photos = "[\"" + photoUrl + "\"]";
        } else {
            // Parse, append, and re-serialize JSON
            // For now, simple string concatenation
            this.photos = this.photos.substring(0, this.photos.length() - 1) + ",\"" + photoUrl + "\"]";
        }
    }

    /**
     * Set digital signature
     */
    public void setDigitalSignature(String signerName, String signatureData) {
        this.signature = signerName + "|" + signatureData;
    }

    /**
     * Check if step is complete
     */
    public boolean isComplete() {
        return "COMPLETED".equals(stepStatus) || "SKIPPED".equals(stepStatus);
    }

    /**
     * Check if step can be started
     */
    public boolean canStart() {
        // Must be in PENDING status
        if (!"PENDING".equals(stepStatus)) {
            return false;
        }

        // Check prerequisite step
        return sopStep.canStart(execution);
    }
}
