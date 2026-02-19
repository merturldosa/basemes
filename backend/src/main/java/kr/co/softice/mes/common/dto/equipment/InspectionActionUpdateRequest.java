package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Inspection Action Update Request DTO
 * 점검 조치 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionActionUpdateRequest {

    private String description;
    private Long assignedUserId;
    private LocalDate dueDate;
    private LocalDate completedDate;
    private String status;
    private String result;
    private String remarks;
}
