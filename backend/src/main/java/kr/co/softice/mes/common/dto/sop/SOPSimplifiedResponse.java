package kr.co.softice.mes.common.dto.sop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SOP Simplified Response DTO for Operators
 * 운영자용 간소화 SOP 응답
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SOPSimplifiedResponse {

    private Long sopId;
    private String sopCode;
    private String sopName;
    private String description;
    private String sopType;
    private String version;

    // Execution info (if applicable)
    private Long executionId;
    private String executionNo;
    private String executionStatus;
    private Integer totalSteps;
    private Integer completedSteps;
    private Double completionRate;

    // Simplified steps
    private List<SimplifiedStep> steps;

    /**
     * Simplified SOP Step for operator view
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimplifiedStep {
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private String stepDescription;
        private Boolean isRequired;
        private Boolean isCritical;

        // Execution step info (if applicable)
        private Long executionStepId;
        private String executionStatus; // PENDING, IN_PROGRESS, COMPLETED, SKIPPED
        private Boolean checkResult; // Pass/Fail for completed steps
        private String notes;
    }
}
