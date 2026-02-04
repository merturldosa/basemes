package kr.co.softice.mes.common.dto.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Routing Step Response DTO
 * 공정 라우팅 단계 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingStepResponse {

    private Long routingStepId;
    private Long routingId;
    private Integer sequenceOrder;
    private Long processId;
    private String processCode;
    private String processName;
    private Integer standardTime;
    private Integer setupTime;
    private Integer waitTime;
    private Integer requiredWorkers;
    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private Boolean isParallel;
    private Integer parallelGroup;
    private Boolean isOptional;
    private Long alternateProcessId;
    private String alternateProcessCode;
    private String alternateProcessName;
    private Boolean qualityCheckRequired;
    private String qualityStandard;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
