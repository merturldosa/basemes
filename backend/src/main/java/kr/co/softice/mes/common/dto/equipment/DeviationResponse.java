package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Deviation Response DTO
 * 이탈 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviationResponse {

    private Long deviationId;
    private String tenantId;
    private String tenantName;

    private String deviationNo;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private String parameterName;
    private String standardValue;
    private String actualValue;
    private String deviationValue;

    private LocalDateTime detectedAt;
    private Long detectedByUserId;
    private String detectedByUserName;

    private String severity;
    private String description;

    private String rootCause;
    private String correctiveAction;
    private String preventiveAction;

    private String status;

    private LocalDateTime resolvedAt;
    private Long resolvedByUserId;
    private String resolvedByUserName;

    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
