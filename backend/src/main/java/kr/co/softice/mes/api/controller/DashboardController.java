package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.dashboard.DashboardStatsResponse;
import kr.co.softice.mes.common.dto.dashboard.LoginTrendResponse;
import kr.co.softice.mes.common.dto.dashboard.RoleDistributionResponse;
import kr.co.softice.mes.common.dto.dashboard.UserStatsResponse;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dashboard Controller
 * 대시보드 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "대시보드 API")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 통계 조회
     * GET /api/dashboard/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "대시보드 통계", description = "전체 통계 데이터 조회")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting dashboard stats for tenant: {}", tenantId);

        DashboardStatsResponse stats = dashboardService.getDashboardStats(tenantId);

        return ResponseEntity.ok(ApiResponse.success("대시보드 통계 조회 성공", stats));
    }

    /**
     * 사용자 상태별 통계
     * GET /api/dashboard/user-stats
     */
    @GetMapping("/user-stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자 상태별 통계", description = "사용자 상태별 집계 데이터")
    public ResponseEntity<ApiResponse<List<UserStatsResponse>>> getUserStats() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting user stats for tenant: {}", tenantId);

        List<UserStatsResponse> stats = dashboardService.getUserStats(tenantId);

        return ResponseEntity.ok(ApiResponse.success("사용자 통계 조회 성공", stats));
    }

    /**
     * 일별 로그인 추이
     * GET /api/dashboard/login-trend?days=7
     */
    @GetMapping("/login-trend")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "일별 로그인 추이", description = "최근 N일간의 로그인 추이 조회")
    public ResponseEntity<ApiResponse<List<LoginTrendResponse>>> getLoginTrend(
            @RequestParam(defaultValue = "7") int days) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting login trend for tenant: {}, days: {}", tenantId, days);

        List<LoginTrendResponse> trend = dashboardService.getLoginTrend(tenantId, days);

        return ResponseEntity.ok(ApiResponse.success("로그인 추이 조회 성공", trend));
    }

    /**
     * 역할별 사용자 분포
     * GET /api/dashboard/role-distribution
     */
    @GetMapping("/role-distribution")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "역할별 사용자 분포", description = "각 역할별 사용자 수 조회")
    public ResponseEntity<ApiResponse<List<RoleDistributionResponse>>> getRoleDistribution() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting role distribution for tenant: {}", tenantId);

        List<RoleDistributionResponse> distribution = dashboardService.getRoleDistribution(tenantId);

        return ResponseEntity.ok(ApiResponse.success("역할별 사용자 분포 조회 성공", distribution));
    }
}
