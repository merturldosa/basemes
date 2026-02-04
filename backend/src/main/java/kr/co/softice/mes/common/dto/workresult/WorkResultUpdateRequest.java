package kr.co.softice.mes.common.dto.workresult;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Work Result Update Request DTO
 * 작업 실적 수정 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkResultUpdateRequest {

    private LocalDateTime resultDate;

    private BigDecimal quantity;

    private BigDecimal goodQuantity;

    private BigDecimal defectQuantity;

    private LocalDateTime workStartTime;

    private LocalDateTime workEndTime;

    private Integer workDuration;

    private Long workerId;

    @Size(max = 100, message = "작업자명은 100자 이하여야 합니다")
    private String workerName;

    @Size(max = 500, message = "불량 사유는 500자 이하여야 합니다")
    private String defectReason;

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
