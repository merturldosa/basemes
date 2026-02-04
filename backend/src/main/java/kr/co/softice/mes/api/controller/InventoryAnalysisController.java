package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.service.InventoryAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inventory Analysis Controller
 * 재고 분석 컨트롤러
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory-analysis")
@RequiredArgsConstructor
@Tag(name = "Inventory Analysis", description = "재고 분석 API")
public class InventoryAnalysisController {

    private final InventoryAnalysisService inventoryAnalysisService;

    /**
     * 재고 회전율 분석
     */
    @GetMapping("/turnover")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "재고 회전율 분석", description = "기간별 제품별 재고 회전율 분석")
    public ResponseEntity<ApiResponse<List<InventoryAnalysisService.InventoryTurnoverAnalysis>>> analyzeTurnover(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Inventory turnover analysis request - Tenant: {}, Period: {} to {}",
                tenantId, startDate, endDate);

        List<InventoryAnalysisService.InventoryTurnoverAnalysis> result =
                inventoryAnalysisService.analyzeInventoryTurnover(tenantId, startDate, endDate);

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("재고 회전율 분석 완료 - %d개 제품 분석", result.size()),
                        result
                )
        );
    }

    /**
     * 불용 재고 분석
     */
    @GetMapping("/obsolete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "불용 재고 분석", description = "지정 기간 동안 출고가 없는 재고 분석")
    public ResponseEntity<ApiResponse<List<InventoryAnalysisService.ObsoleteInventoryAnalysis>>> analyzeObsolete(
            @RequestParam(defaultValue = "90") int daysThreshold) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Obsolete inventory analysis request - Tenant: {}, Days threshold: {}",
                tenantId, daysThreshold);

        List<InventoryAnalysisService.ObsoleteInventoryAnalysis> result =
                inventoryAnalysisService.analyzeObsoleteInventory(tenantId, daysThreshold);

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("%d일 이상 미출고 재고 %d건 발견", daysThreshold, result.size()),
                        result
                )
        );
    }

    /**
     * 재고 연령 분석
     */
    @GetMapping("/aging")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "재고 연령 분석", description = "LOT별 재고 연령 및 유효기간 분석")
    public ResponseEntity<ApiResponse<List<InventoryAnalysisService.InventoryAgingAnalysis>>> analyzeAging() {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Inventory aging analysis request - Tenant: {}", tenantId);

        List<InventoryAnalysisService.InventoryAgingAnalysis> result =
                inventoryAnalysisService.analyzeInventoryAging(tenantId);

        // 통계 계산
        long nearExpiryCount = result.stream()
                .filter(InventoryAnalysisService.InventoryAgingAnalysis::isNearExpiry)
                .count();

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("재고 연령 분석 완료 - %d개 LOT 분석, 만료 임박 %d건",
                                result.size(), nearExpiryCount),
                        result
                )
        );
    }

    /**
     * ABC 분석
     */
    @GetMapping("/abc")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "ABC 분석", description = "재고 가치 기준 중요도 분류 (A/B/C 등급)")
    public ResponseEntity<ApiResponse<AbcAnalysisResponse>> analyzeABC() {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("ABC analysis request - Tenant: {}", tenantId);

        List<InventoryAnalysisService.AbcAnalysis> result =
                inventoryAnalysisService.analyzeABC(tenantId);

        // 등급별 통계
        long classACount = result.stream()
                .filter(a -> "A".equals(a.getAbcClass()))
                .count();
        long classBCount = result.stream()
                .filter(a -> "B".equals(a.getAbcClass()))
                .count();
        long classCCount = result.stream()
                .filter(a -> "C".equals(a.getAbcClass()))
                .count();

        AbcAnalysisResponse response = AbcAnalysisResponse.builder()
                .items(result)
                .statistics(AbcStatistics.builder()
                        .totalProducts(result.size())
                        .classACount((int) classACount)
                        .classBCount((int) classBCount)
                        .classCCount((int) classCCount)
                        .build())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("ABC 분석 완료 - A등급: %d, B등급: %d, C등급: %d",
                                classACount, classBCount, classCCount),
                        response
                )
        );
    }

    /**
     * 재고 이동 추이 분석
     */
    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "재고 이동 추이 분석", description = "일별 입출고 추이 분석")
    public ResponseEntity<ApiResponse<List<InventoryAnalysisService.InventoryTrendAnalysis>>> analyzeTrend(
            @RequestParam(defaultValue = "30") int days) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Inventory trend analysis request - Tenant: {}, Days: {}", tenantId, days);

        List<InventoryAnalysisService.InventoryTrendAnalysis> result =
                inventoryAnalysisService.analyzeInventoryTrend(tenantId, days);

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("최근 %d일 재고 이동 추이 분석 완료", days),
                        result
                )
        );
    }

    /**
     * 통합 대시보드 (모든 분석 결과)
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "재고 분석 통합 대시보드", description = "모든 재고 분석 결과를 한 번에 조회")
    public ResponseEntity<ApiResponse<InventoryDashboard>> getDashboard(
            @RequestParam(defaultValue = "30") int trendDays,
            @RequestParam(defaultValue = "90") int obsoleteDays) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Inventory dashboard request - Tenant: {}", tenantId);

        // 기간 설정
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);

        // 모든 분석 실행
        List<InventoryAnalysisService.InventoryTurnoverAnalysis> turnover =
                inventoryAnalysisService.analyzeInventoryTurnover(tenantId, startDate, endDate);

        List<InventoryAnalysisService.ObsoleteInventoryAnalysis> obsolete =
                inventoryAnalysisService.analyzeObsoleteInventory(tenantId, obsoleteDays);

        List<InventoryAnalysisService.InventoryAgingAnalysis> aging =
                inventoryAnalysisService.analyzeInventoryAging(tenantId);

        List<InventoryAnalysisService.AbcAnalysis> abc =
                inventoryAnalysisService.analyzeABC(tenantId);

        List<InventoryAnalysisService.InventoryTrendAnalysis> trend =
                inventoryAnalysisService.analyzeInventoryTrend(tenantId, trendDays);

        // 대시보드 구성
        InventoryDashboard dashboard = InventoryDashboard.builder()
                .turnoverAnalysis(turnover.stream().limit(10).toList())
                .obsoleteInventory(obsolete.stream().limit(10).toList())
                .agingAnalysis(aging.stream().limit(10).toList())
                .abcAnalysis(abc.stream().limit(10).toList())
                .trendAnalysis(trend)
                .summary(InventoryDashboard.Summary.builder()
                        .totalProducts(abc.size())
                        .obsoleteItemsCount(obsolete.size())
                        .nearExpiryItemsCount(
                                (int) aging.stream()
                                        .filter(InventoryAnalysisService.InventoryAgingAnalysis::isNearExpiry)
                                        .count()
                        )
                        .classAProductsCount(
                                (int) abc.stream()
                                        .filter(a -> "A".equals(a.getAbcClass()))
                                        .count()
                        )
                        .build())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("재고 분석 대시보드 조회 완료", dashboard)
        );
    }

    // ===== Response DTOs =====

    @lombok.Getter
    @lombok.Builder
    private static class AbcAnalysisResponse {
        private List<InventoryAnalysisService.AbcAnalysis> items;
        private AbcStatistics statistics;
    }

    @lombok.Getter
    @lombok.Builder
    private static class AbcStatistics {
        private int totalProducts;
        private int classACount;
        private int classBCount;
        private int classCCount;
    }

    @lombok.Getter
    @lombok.Builder
    private static class InventoryDashboard {
        private List<InventoryAnalysisService.InventoryTurnoverAnalysis> turnoverAnalysis;
        private List<InventoryAnalysisService.ObsoleteInventoryAnalysis> obsoleteInventory;
        private List<InventoryAnalysisService.InventoryAgingAnalysis> agingAnalysis;
        private List<InventoryAnalysisService.AbcAnalysis> abcAnalysis;
        private List<InventoryAnalysisService.InventoryTrendAnalysis> trendAnalysis;
        private Summary summary;

        @lombok.Getter
        @lombok.Builder
        public static class Summary {
            private int totalProducts;
            private int obsoleteItemsCount;
            private int nearExpiryItemsCount;
            private int classAProductsCount;
        }
    }
}
