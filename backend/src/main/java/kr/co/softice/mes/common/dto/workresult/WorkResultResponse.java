package kr.co.softice.mes.common.dto.workresult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Work Result Response DTO
 * 작업 실적 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkResultResponse {

    private Long workResultId;

    // Work order info
    private Long workOrderId;
    private String workOrderNo;

    private LocalDateTime resultDate;

    // Quantities
    private BigDecimal quantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;

    // Work time
    private LocalDateTime workStartTime;
    private LocalDateTime workEndTime;
    private Integer workDuration;  // 분

    // Worker info
    private Long workerId;
    private String workerName;

    private String defectReason;
    private String tenantId;
    private String tenantName;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
