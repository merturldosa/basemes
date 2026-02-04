package kr.co.softice.mes.common.dto.workresult;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Work Result Create Request DTO
 * 작업 실적 생성 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkResultCreateRequest {

    @NotNull(message = "작업 지시 ID는 필수입니다")
    private Long workOrderId;

    @NotNull(message = "실적 일자는 필수입니다")
    private LocalDateTime resultDate;

    @NotNull(message = "생산 수량은 필수입니다")
    private BigDecimal quantity;

    @NotNull(message = "양품 수량은 필수입니다")
    private BigDecimal goodQuantity;

    @NotNull(message = "불량 수량은 필수입니다")
    private BigDecimal defectQuantity;

    @NotNull(message = "작업 시작 시간은 필수입니다")
    private LocalDateTime workStartTime;

    @NotNull(message = "작업 종료 시간은 필수입니다")
    private LocalDateTime workEndTime;

    private Integer workDuration;  // 작업 시간 (분) - null이면 자동 계산

    private Long workerId;  // 작업자 ID

    @Size(max = 100, message = "작업자명은 100자 이하여야 합니다")
    private String workerName;

    @Size(max = 500, message = "불량 사유는 500자 이하여야 합니다")
    private String defectReason;

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
