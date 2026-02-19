package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Inspection Action Create Request DTO
 * 점검 조치 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionActionCreateRequest {

    @NotNull(message = "점검 ID는 필수입니다.")
    private Long inspectionId;

    @NotBlank(message = "조치 유형은 필수입니다.")
    private String actionType;

    @NotBlank(message = "조치 내용은 필수입니다.")
    private String description;

    private Long assignedUserId;
    private LocalDate dueDate;
    private String remarks;
}
