package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Deviation Update Request DTO
 * 이탈 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviationUpdateRequest {

    private String parameterName;
    private String standardValue;
    private String actualValue;
    private String deviationValue;
    private String severity;
    private String description;
    private String rootCause;
    private String correctiveAction;
    private String preventiveAction;
    private Long resolvedByUserId;
    private String remarks;
}
