package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Breakdown Update Request DTO
 * 고장 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakdownUpdateRequest {

    private String failureType;
    private String severity;
    private String description;
    private Long assignedUserId;
    private String repairDescription;
    private String partsUsed;
    private BigDecimal repairCost;
    private String rootCause;
    private String preventiveAction;
    private String remarks;
}
