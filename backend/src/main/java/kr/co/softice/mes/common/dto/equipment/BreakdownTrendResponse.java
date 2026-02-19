package kr.co.softice.mes.common.dto.equipment;

import lombok.*;
import java.math.BigDecimal;

/**
 * Breakdown Trend Response DTO
 * 고장 추이 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakdownTrendResponse {

    private String month;               // yyyy-MM format
    private Long breakdownCount;
    private BigDecimal avgRepairMinutes;
}
