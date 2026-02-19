package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Breakdown Create Request DTO
 * 고장 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakdownCreateRequest {

    @NotBlank(message = "고장 번호는 필수입니다.")
    private String breakdownNo;

    @NotNull(message = "설비 ID는 필수입니다.")
    private Long equipmentId;

    @NotNull(message = "보고 일시는 필수입니다.")
    private LocalDateTime reportedAt;

    @NotBlank(message = "설명은 필수입니다.")
    private String description;

    private Long downtimeId;
    private Long reportedByUserId;
    private String failureType;
    private String severity;
    private String remarks;
}
