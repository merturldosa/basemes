package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.equipment.BreakdownStatisticsResponse;
import kr.co.softice.mes.common.dto.equipment.BreakdownTrendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Breakdown Statistics Service
 * 고장 통계 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BreakdownStatisticsService {

    private final EntityManager entityManager;

    /**
     * Get breakdown statistics for date range
     */
    @SuppressWarnings("unchecked")
    public BreakdownStatisticsResponse getStatistics(String tenantId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting breakdown statistics for tenant: {} from {} to {}", tenantId, startDate, endDate);

        // Total breakdowns
        Query totalQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM equipment.sd_breakdowns " +
                "WHERE tenant_id = (SELECT tenant_id FROM common.sd_tenants WHERE tenant_id = :tenantId) " +
                "AND reported_at BETWEEN :startDate AND :endDate");
        totalQuery.setParameter("tenantId", tenantId);
        totalQuery.setParameter("startDate", startDate.atStartOfDay());
        totalQuery.setParameter("endDate", endDate.plusDays(1).atStartOfDay());
        Long totalBreakdowns = ((Number) totalQuery.getSingleResult()).longValue();

        // By status
        Query statusQuery = entityManager.createNativeQuery(
                "SELECT status, COUNT(*) FROM equipment.sd_breakdowns " +
                "WHERE tenant_id = (SELECT tenant_id FROM common.sd_tenants WHERE tenant_id = :tenantId) " +
                "AND reported_at BETWEEN :startDate AND :endDate " +
                "GROUP BY status");
        statusQuery.setParameter("tenantId", tenantId);
        statusQuery.setParameter("startDate", startDate.atStartOfDay());
        statusQuery.setParameter("endDate", endDate.plusDays(1).atStartOfDay());
        List<Object[]> statusResults = statusQuery.getResultList();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : statusResults) {
            byStatus.put((String) row[0], ((Number) row[1]).longValue());
        }

        // By failure type
        Query failureTypeQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(failure_type, 'UNKNOWN'), COUNT(*) FROM equipment.sd_breakdowns " +
                "WHERE tenant_id = (SELECT tenant_id FROM common.sd_tenants WHERE tenant_id = :tenantId) " +
                "AND reported_at BETWEEN :startDate AND :endDate " +
                "GROUP BY failure_type");
        failureTypeQuery.setParameter("tenantId", tenantId);
        failureTypeQuery.setParameter("startDate", startDate.atStartOfDay());
        failureTypeQuery.setParameter("endDate", endDate.plusDays(1).atStartOfDay());
        List<Object[]> failureTypeResults = failureTypeQuery.getResultList();
        Map<String, Long> byFailureType = new LinkedHashMap<>();
        for (Object[] row : failureTypeResults) {
            byFailureType.put((String) row[0], ((Number) row[1]).longValue());
        }

        // By severity
        Query severityQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(severity, 'UNKNOWN'), COUNT(*) FROM equipment.sd_breakdowns " +
                "WHERE tenant_id = (SELECT tenant_id FROM common.sd_tenants WHERE tenant_id = :tenantId) " +
                "AND reported_at BETWEEN :startDate AND :endDate " +
                "GROUP BY severity");
        severityQuery.setParameter("tenantId", tenantId);
        severityQuery.setParameter("startDate", startDate.atStartOfDay());
        severityQuery.setParameter("endDate", endDate.plusDays(1).atStartOfDay());
        List<Object[]> severityResults = severityQuery.getResultList();
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Object[] row : severityResults) {
            bySeverity.put((String) row[0], ((Number) row[1]).longValue());
        }

        // MTBF (Mean Time Between Failures): total operating hours / number of breakdowns
        BigDecimal mtbfHours = BigDecimal.ZERO;
        if (totalBreakdowns > 0) {
            long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            long totalOperatingHours = totalDays * 24; // Assume 24hr operation
            mtbfHours = BigDecimal.valueOf(totalOperatingHours)
                    .divide(BigDecimal.valueOf(totalBreakdowns), 2, RoundingMode.HALF_UP);
        }

        // MTTR (Mean Time To Repair): avg of repair_duration_minutes
        Query mttrQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(AVG(repair_duration_minutes), 0) FROM equipment.sd_breakdowns " +
                "WHERE tenant_id = (SELECT tenant_id FROM common.sd_tenants WHERE tenant_id = :tenantId) " +
                "AND reported_at BETWEEN :startDate AND :endDate " +
                "AND repair_duration_minutes IS NOT NULL");
        mttrQuery.setParameter("tenantId", tenantId);
        mttrQuery.setParameter("startDate", startDate.atStartOfDay());
        mttrQuery.setParameter("endDate", endDate.plusDays(1).atStartOfDay());
        BigDecimal mttrMinutes = BigDecimal.valueOf(((Number) mttrQuery.getSingleResult()).doubleValue())
                .setScale(2, RoundingMode.HALF_UP);

        // Failure rate (failures per day)
        BigDecimal failureRate = BigDecimal.ZERO;
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (totalDays > 0) {
            failureRate = BigDecimal.valueOf(totalBreakdowns)
                    .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
        }

        // Top 5 equipment by breakdown count
        Query topEquipmentQuery = entityManager.createNativeQuery(
                "SELECT b.equipment_id, e.equipment_code, e.equipment_name, COUNT(*) as cnt " +
                "FROM equipment.sd_breakdowns b " +
                "JOIN equipment.sd_equipments e ON b.equipment_id = e.equipment_id " +
                "WHERE b.tenant_id = (SELECT tenant_id FROM common.sd_tenants WHERE tenant_id = :tenantId) " +
                "AND b.reported_at BETWEEN :startDate AND :endDate " +
                "GROUP BY b.equipment_id, e.equipment_code, e.equipment_name " +
                "ORDER BY cnt DESC " +
                "LIMIT 5");
        topEquipmentQuery.setParameter("tenantId", tenantId);
        topEquipmentQuery.setParameter("startDate", startDate.atStartOfDay());
        topEquipmentQuery.setParameter("endDate", endDate.plusDays(1).atStartOfDay());
        List<Object[]> topEquipmentResults = topEquipmentQuery.getResultList();
        List<BreakdownStatisticsResponse.TopEquipmentBreakdown> topEquipments = new ArrayList<>();
        for (Object[] row : topEquipmentResults) {
            topEquipments.add(BreakdownStatisticsResponse.TopEquipmentBreakdown.builder()
                    .equipmentId(((Number) row[0]).longValue())
                    .equipmentCode((String) row[1])
                    .equipmentName((String) row[2])
                    .breakdownCount(((Number) row[3]).longValue())
                    .build());
        }

        return BreakdownStatisticsResponse.builder()
                .totalBreakdowns(totalBreakdowns)
                .byStatus(byStatus)
                .byFailureType(byFailureType)
                .bySeverity(bySeverity)
                .mtbfHours(mtbfHours)
                .mttrMinutes(mttrMinutes)
                .failureRate(failureRate)
                .topEquipments(topEquipments)
                .build();
    }

    /**
     * Get monthly breakdown trend
     */
    @SuppressWarnings("unchecked")
    public List<BreakdownTrendResponse> getMonthlyTrend(String tenantId, int months) {
        log.info("Getting monthly breakdown trend for tenant: {} for {} months", tenantId, months);

        Query trendQuery = entityManager.createNativeQuery(
                "SELECT TO_CHAR(reported_at, 'YYYY-MM') as month, " +
                "COUNT(*) as breakdown_count, " +
                "COALESCE(AVG(repair_duration_minutes), 0) as avg_repair_minutes " +
                "FROM equipment.sd_breakdowns " +
                "WHERE tenant_id = (SELECT tenant_id FROM common.sd_tenants WHERE tenant_id = :tenantId) " +
                "AND reported_at >= (CURRENT_DATE - INTERVAL '" + months + " months') " +
                "GROUP BY TO_CHAR(reported_at, 'YYYY-MM') " +
                "ORDER BY month");
        trendQuery.setParameter("tenantId", tenantId);

        List<Object[]> results = trendQuery.getResultList();
        List<BreakdownTrendResponse> trends = new ArrayList<>();
        for (Object[] row : results) {
            trends.add(BreakdownTrendResponse.builder()
                    .month((String) row[0])
                    .breakdownCount(((Number) row[1]).longValue())
                    .avgRepairMinutes(BigDecimal.valueOf(((Number) row[2]).doubleValue())
                            .setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return trends;
    }
}
