package kr.co.softice.mes.common.dto.workorder;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Work Order Update Request DTO
 * 작업 지시 수정 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderUpdateRequest {

    private Long productId;

    private Long processId;

    private BigDecimal plannedQuantity;

    private LocalDateTime plannedStartDate;

    private LocalDateTime plannedEndDate;

    private Long assignedUserId;

    @Size(max = 20, message = "우선순위는 20자 이하여야 합니다")
    private String priority;

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
