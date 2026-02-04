package kr.co.softice.mes.common.dto.mold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalDateTime;

/**
 * Mold Production History Response DTO
 * 금형 생산 이력 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldProductionHistoryResponse {

    private Long historyId;
    private String tenantId;
    private String tenantName;

    private Long moldId;
    private String moldCode;
    private String moldName;

    private Long workOrderId;
    private String workOrderNo;

    private Long workResultId;

    private LocalDate productionDate;

    private Integer shotCount;
    private Long cumulativeShotCount; // 누적 Shot 수 (자동 계산)

    private BigDecimal productionQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;

    private Long operatorUserId;
    private String operatorUsername;
    private String operatorName;

    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
