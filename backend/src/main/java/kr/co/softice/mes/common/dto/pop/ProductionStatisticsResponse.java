package kr.co.softice.mes.common.dto.pop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Production Statistics Response DTO
 * 생산 통계 응답
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionStatisticsResponse {

    private LocalDate date;
    private String tenantId;

    // Operator Information (if filtered)
    private Long operatorUserId;
    private String operatorUserName;

    // Production Summary
    private BigDecimal totalProduced;
    private BigDecimal totalGood;
    private BigDecimal totalDefects;
    private Long completedWorkOrders;
    private Long inProgressWorkOrders;

    // Quality Metrics
    private Double defectRate; // (totalDefects / totalProduced) * 100
    private Double yieldRate; // (totalGood / totalProduced) * 100

    // Time Metrics
    private Integer totalWorkMinutes;
    private Integer totalPauseMinutes;
    private Double efficiency; // (totalWorkMinutes - totalPauseMinutes) / totalWorkMinutes * 100

    // Equipment Utilization
    private Integer equipmentCount;
    private Double utilizationRate;

    // Hourly Breakdown
    private List<HourlyProduction> hourlyBreakdown;

    /**
     * Hourly Production Data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyProduction {
        private Integer hour; // 0-23
        private BigDecimal produced;
        private BigDecimal defects;
    }
}
