package kr.co.softice.mes.common.dto.routing;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Routing Step Request DTO
 * 공정 라우팅 단계 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingStepRequest {

    private Integer sequenceOrder;

    @NotNull(message = "Process ID is required")
    private Long processId;

    @NotNull(message = "Standard time is required")
    @Min(value = 0, message = "Standard time must be non-negative")
    private Integer standardTime;

    @Min(value = 0, message = "Setup time must be non-negative")
    @Builder.Default
    private Integer setupTime = 0;

    @Min(value = 0, message = "Wait time must be non-negative")
    @Builder.Default
    private Integer waitTime = 0;

    @Min(value = 1, message = "Required workers must be at least 1")
    @Builder.Default
    private Integer requiredWorkers = 1;

    private Long equipmentId;

    @Builder.Default
    private Boolean isParallel = false;

    private Integer parallelGroup;

    @Builder.Default
    private Boolean isOptional = false;

    private Long alternateProcessId;

    @Builder.Default
    private Boolean qualityCheckRequired = false;

    private String qualityStandard;

    private String remarks;
}
