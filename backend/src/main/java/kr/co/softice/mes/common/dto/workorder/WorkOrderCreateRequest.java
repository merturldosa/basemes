package kr.co.softice.mes.common.dto.workorder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Work Order Create Request DTO
 * 작업 지시 생성 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderCreateRequest {

    @NotBlank(message = "작업 지시 번호는 필수입니다")
    @Size(max = 50, message = "작업 지시 번호는 50자 이하여야 합니다")
    private String workOrderNo;

    @NotNull(message = "제품 ID는 필수입니다")
    private Long productId;

    @NotNull(message = "공정 ID는 필수입니다")
    private Long processId;

    @NotNull(message = "계획 수량은 필수입니다")
    private BigDecimal plannedQuantity;

    @NotNull(message = "계획 시작일은 필수입니다")
    private LocalDateTime plannedStartDate;

    @NotNull(message = "계획 종료일은 필수입니다")
    private LocalDateTime plannedEndDate;

    private Long assignedUserId;  // 담당자 ID

    @Size(max = 20, message = "우선순위는 20자 이하여야 합니다")
    private String priority;  // HIGH, MEDIUM, LOW

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
