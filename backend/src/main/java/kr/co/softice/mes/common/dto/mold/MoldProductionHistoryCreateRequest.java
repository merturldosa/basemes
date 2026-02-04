package kr.co.softice.mes.common.dto.mold;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mold Production History Create Request DTO
 * 금형 생산 이력 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldProductionHistoryCreateRequest {

    @NotNull(message = "금형 ID는 필수입니다.")
    private Long moldId;

    private Long workOrderId;
    private Long workResultId;

    @NotNull(message = "생산일자는 필수입니다.")
    private LocalDate productionDate;

    @NotNull(message = "Shot 수는 필수입니다.")
    private Integer shotCount; // 이번 생산의 Shot 수

    private BigDecimal productionQuantity; // 생산 수량
    private BigDecimal goodQuantity; // 양품 수량
    private BigDecimal defectQuantity; // 불량 수량

    private Long operatorUserId;
    private String operatorName;

    private String remarks;
}
