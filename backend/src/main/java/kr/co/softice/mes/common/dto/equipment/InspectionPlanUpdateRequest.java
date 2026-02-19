package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Inspection Plan Update Request DTO
 * 점검 계획 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionPlanUpdateRequest {

    private String planName;
    private Long equipmentId;
    private String inspectionType;
    private Integer cycleDays;
    private Long formId;
    private Long assignedUserId;
    private LocalDate nextDueDate;
    private String remarks;
}
