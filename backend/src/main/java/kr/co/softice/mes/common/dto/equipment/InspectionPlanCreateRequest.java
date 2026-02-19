package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Inspection Plan Create Request DTO
 * 점검 계획 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionPlanCreateRequest {

    @NotBlank(message = "계획 코드는 필수입니다.")
    private String planCode;

    @NotBlank(message = "계획명은 필수입니다.")
    private String planName;

    @NotNull(message = "설비 ID는 필수입니다.")
    private Long equipmentId;

    @NotBlank(message = "점검 유형은 필수입니다.")
    private String inspectionType;

    @NotNull(message = "점검 주기(일)는 필수입니다.")
    private Integer cycleDays;

    private Long formId;
    private Long assignedUserId;
    private LocalDate nextDueDate;
    private String remarks;
}
