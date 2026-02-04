package kr.co.softice.mes.common.dto.mold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mold Production History Update Request DTO
 * 금형 생산 이력 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldProductionHistoryUpdateRequest {

    private LocalDate productionDate;
    private Integer shotCount;

    private BigDecimal productionQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;

    private Long operatorUserId;
    private String operatorName;

    private String remarks;
}
