package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.equipment.BreakdownStatisticsResponse;
import kr.co.softice.mes.common.dto.equipment.BreakdownTrendResponse;
import kr.co.softice.mes.domain.service.BreakdownStatisticsService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Breakdown Statistics Controller
 * 고장 통계 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/breakdown-statistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "BreakdownStatistics", description = "고장 통계 API")
public class BreakdownStatisticsController {

    private final BreakdownStatisticsService breakdownStatisticsService;

    /**
     * Get breakdown statistics
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "고장 통계 조회", description = "기간별 고장 통계를 조회합니다.")
    public ResponseEntity<BreakdownStatisticsResponse> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting breakdown statistics for tenant: {} from {} to {}", tenantId, startDate, endDate);

        BreakdownStatisticsResponse response = breakdownStatisticsService.getStatistics(tenantId, startDate, endDate);

        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly breakdown trend
     */
    @GetMapping("/trend")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "고장 추이 조회", description = "월별 고장 추이를 조회합니다.")
    public ResponseEntity<List<BreakdownTrendResponse>> getMonthlyTrend(
            @RequestParam(defaultValue = "12") int months) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting monthly breakdown trend for tenant: {} for {} months", tenantId, months);

        List<BreakdownTrendResponse> response = breakdownStatisticsService.getMonthlyTrend(tenantId, months);

        return ResponseEntity.ok(response);
    }
}
