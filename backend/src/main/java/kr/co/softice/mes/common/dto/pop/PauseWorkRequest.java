package kr.co.softice.mes.common.dto.pop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pause Work Request DTO
 * 작업 일시정지 요청
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PauseWorkRequest {

    private String pauseReason;

    private String pauseType; // BREAK, EQUIPMENT_CHECK, MATERIAL_WAIT, OTHER

    private Boolean requiresApproval;
}
