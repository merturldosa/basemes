package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Deviation Create Request DTO
 * 이탈 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviationCreateRequest {

    @NotBlank(message = "이탈번호는 필수입니다.")
    private String deviationNo;

    @NotNull(message = "설비 ID는 필수입니다.")
    private Long equipmentId;

    @NotBlank(message = "파라미터명은 필수입니다.")
    private String parameterName;

    private String standardValue;
    private String actualValue;
    private String deviationValue;

    @NotNull(message = "감지 일시는 필수입니다.")
    private LocalDateTime detectedAt;

    private Long detectedByUserId;
    private String severity;
    private String description;
    private String remarks;
}
