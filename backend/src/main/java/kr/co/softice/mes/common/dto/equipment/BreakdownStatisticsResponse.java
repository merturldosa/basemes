package kr.co.softice.mes.common.dto.equipment;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Breakdown Statistics Response DTO
 * 고장 통계 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakdownStatisticsResponse {

    private Long totalBreakdowns;
    private Map<String, Long> byStatus;
    private Map<String, Long> byFailureType;
    private Map<String, Long> bySeverity;
    private BigDecimal mtbfHours;       // Mean Time Between Failures
    private BigDecimal mttrMinutes;     // Mean Time To Repair
    private BigDecimal failureRate;     // failures per day
    private List<TopEquipmentBreakdown> topEquipments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopEquipmentBreakdown {
        private Long equipmentId;
        private String equipmentCode;
        private String equipmentName;
        private Long breakdownCount;
    }
}
