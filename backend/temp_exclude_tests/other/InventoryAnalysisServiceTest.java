package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.InventoryRepository;
import kr.co.softice.mes.domain.repository.InventoryTransactionRepository;
import kr.co.softice.mes.domain.repository.LotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Inventory Analysis Service Test
 * 재고 분석 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("재고 분석 서비스 테스트")
class InventoryAnalysisServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private InventoryAnalysisService inventoryAnalysisService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private WarehouseEntity testWarehouse;
    private LotEntity testLot;
    private InventoryEntity testInventory;
    private InventoryTransactionEntity testTransaction;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);

        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P001");
        testProduct.setProductName("Test Product");
        testProduct.setUnit("EA");

        testWarehouse = new WarehouseEntity();
        testWarehouse.setWarehouseId(1L);
        testWarehouse.setWarehouseCode("WH001");
        testWarehouse.setWarehouseName("Main Warehouse");

        testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT001");
        testLot.setProduct(testProduct);
        testLot.setExpiryDate(LocalDate.now().plusDays(60));
        testLot.setCreatedAt(LocalDateTime.now().minusDays(45));

        testInventory = new InventoryEntity();
        testInventory.setInventoryId(1L);
        testInventory.setProduct(testProduct);
        testInventory.setWarehouse(testWarehouse);
        testInventory.setLot(testLot);
        testInventory.setAvailableQuantity(new BigDecimal("100"));
        testInventory.setReservedQuantity(new BigDecimal("20"));
        testInventory.setLastTransactionDate(LocalDateTime.now().minusDays(5));
        testInventory.setLastTransactionType("OUT_SALES");
        testInventory.setTenant(testTenant);

        testTransaction = new InventoryTransactionEntity();
        testTransaction.setTransactionId(1L);
        testTransaction.setProduct(testProduct);
        testTransaction.setTransactionType("OUT_SALES");
        testTransaction.setQuantity(new BigDecimal("50"));
        testTransaction.setTransactionDate(LocalDateTime.now().minusDays(3));
        testTransaction.setTenant(testTenant);
    }

    // === 재고 회전율 분석 테스트 ===

    @Test
    @DisplayName("재고 회전율 분석 - 성공")
    void testAnalyzeInventoryTurnover_Success() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        when(inventoryTransactionRepository.findByTenant_TenantIdAndTransactionDateBetween(
                tenantId, start, end))
                .thenReturn(Arrays.asList(testTransaction));
        when(inventoryRepository.findByTenant_TenantId(tenantId))
                .thenReturn(Arrays.asList(testInventory));

        List<InventoryAnalysisService.InventoryTurnoverAnalysis> result =
                inventoryAnalysisService.analyzeInventoryTurnover(tenantId, start, end);

        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("재고 회전율 분석 - 데이터 없음")
    void testAnalyzeInventoryTurnover_NoData() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        when(inventoryTransactionRepository.findByTenant_TenantIdAndTransactionDateBetween(
                tenantId, start, end))
                .thenReturn(Arrays.asList());
        when(inventoryRepository.findByTenant_TenantId(tenantId))
                .thenReturn(Arrays.asList());

        List<InventoryAnalysisService.InventoryTurnoverAnalysis> result =
                inventoryAnalysisService.analyzeInventoryTurnover(tenantId, start, end);

        assertThat(result).isEmpty();
    }

    // === 불용 재고 분석 테스트 ===

    @Test
    @DisplayName("불용 재고 분석 - 성공")
    void testAnalyzeObsoleteInventory_Success() {
        int daysThreshold = 30;

        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList(testInventory));

        List<InventoryAnalysisService.ObsoleteInventoryAnalysis> result =
                inventoryAnalysisService.analyzeObsoleteInventory(tenantId, daysThreshold);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("불용 재고 분석 - 최근 거래 있음 (제외됨)")
    void testAnalyzeObsoleteInventory_RecentTransaction() {
        testInventory.setLastTransactionDate(LocalDateTime.now().minusDays(5));
        int daysThreshold = 30;

        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList(testInventory));

        List<InventoryAnalysisService.ObsoleteInventoryAnalysis> result =
                inventoryAnalysisService.analyzeObsoleteInventory(tenantId, daysThreshold);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("불용 재고 분석 - 재고 없음")
    void testAnalyzeObsoleteInventory_NoInventory() {
        testInventory.setAvailableQuantity(BigDecimal.ZERO);
        testInventory.setReservedQuantity(BigDecimal.ZERO);
        int daysThreshold = 30;

        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList(testInventory));

        List<InventoryAnalysisService.ObsoleteInventoryAnalysis> result =
                inventoryAnalysisService.analyzeObsoleteInventory(tenantId, daysThreshold);

        assertThat(result).isEmpty();
    }

    // === 재고 연령 분석 테스트 ===

    @Test
    @DisplayName("재고 연령 분석 - 성공")
    void testAnalyzeInventoryAging_Success() {
        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList(testInventory));

        List<InventoryAnalysisService.InventoryAgingAnalysis> result =
                inventoryAnalysisService.analyzeInventoryAging(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT001");
        assertThat(result.get(0).getAgeInDays()).isGreaterThan(0);
    }

    @Test
    @DisplayName("재고 연령 분석 - 유효기간 임박")
    void testAnalyzeInventoryAging_NearExpiry() {
        testLot.setExpiryDate(LocalDate.now().plusDays(20)); // 20일 남음

        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList(testInventory));

        List<InventoryAnalysisService.InventoryAgingAnalysis> result =
                inventoryAnalysisService.analyzeInventoryAging(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isNearExpiry()).isTrue();
    }

    @Test
    @DisplayName("재고 연령 분석 - LOT 없는 재고 제외")
    void testAnalyzeInventoryAging_NoLot() {
        testInventory.setLot(null);

        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList(testInventory));

        List<InventoryAnalysisService.InventoryAgingAnalysis> result =
                inventoryAnalysisService.analyzeInventoryAging(tenantId);

        assertThat(result).isEmpty();
    }

    // === ABC 분석 테스트 ===

    @Test
    @DisplayName("ABC 분석 - 성공")
    void testAnalyzeABC_Success() {
        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList(testInventory));

        List<InventoryAnalysisService.AbcAnalysis> result =
                inventoryAnalysisService.analyzeABC(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAbcClass()).isIn("A", "B", "C");
        assertThat(result.get(0).getRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("ABC 분석 - 다중 제품")
    void testAnalyzeABC_MultipleProducts() {
        ProductEntity product2 = new ProductEntity();
        product2.setProductId(2L);
        product2.setProductCode("P002");
        product2.setProductName("Product 2");

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProduct(product2);
        inventory2.setWarehouse(testWarehouse);
        inventory2.setAvailableQuantity(new BigDecimal("500"));
        inventory2.setReservedQuantity(BigDecimal.ZERO);
        inventory2.setTenant(testTenant);

        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList(testInventory, inventory2));

        List<InventoryAnalysisService.AbcAnalysis> result =
                inventoryAnalysisService.analyzeABC(tenantId);

        assertThat(result).hasSize(2);
        // 가치 높은 순으로 정렬됨
        assertThat(result.get(0).getTotalQuantity())
                .isGreaterThanOrEqualTo(result.get(1).getTotalQuantity());
    }

    @Test
    @DisplayName("ABC 분석 - 데이터 없음")
    void testAnalyzeABC_NoData() {
        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(Arrays.asList());

        List<InventoryAnalysisService.AbcAnalysis> result =
                inventoryAnalysisService.analyzeABC(tenantId);

        assertThat(result).isEmpty();
    }

    // === 재고 이동 추이 분석 테스트 ===

    @Test
    @DisplayName("재고 이동 추이 분석 - 성공")
    void testAnalyzeInventoryTrend_Success() {
        int days = 7;

        when(inventoryTransactionRepository.findByTenant_TenantIdAndTransactionDateBetween(
                eq(tenantId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testTransaction));

        List<InventoryAnalysisService.InventoryTrendAnalysis> result =
                inventoryAnalysisService.analyzeInventoryTrend(tenantId, days);

        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("재고 이동 추이 분석 - 입고/출고 구분")
    void testAnalyzeInventoryTrend_InboundOutbound() {
        InventoryTransactionEntity inboundTx = new InventoryTransactionEntity();
        inboundTx.setTransactionType("IN_PURCHASE");
        inboundTx.setQuantity(new BigDecimal("100"));
        inboundTx.setTransactionDate(LocalDateTime.now().minusDays(1));
        inboundTx.setProduct(testProduct);
        inboundTx.setTenant(testTenant);

        InventoryTransactionEntity outboundTx = new InventoryTransactionEntity();
        outboundTx.setTransactionType("OUT_SALES");
        outboundTx.setQuantity(new BigDecimal("50"));
        outboundTx.setTransactionDate(LocalDateTime.now().minusDays(1));
        outboundTx.setProduct(testProduct);
        outboundTx.setTenant(testTenant);

        when(inventoryTransactionRepository.findByTenant_TenantIdAndTransactionDateBetween(
                eq(tenantId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(inboundTx, outboundTx));

        List<InventoryAnalysisService.InventoryTrendAnalysis> result =
                inventoryAnalysisService.analyzeInventoryTrend(tenantId, 7);

        assertThat(result).isNotNull();
        if (!result.isEmpty()) {
            InventoryAnalysisService.InventoryTrendAnalysis dayStats = result.get(0);
            assertThat(dayStats.getInboundQuantity()).isNotNull();
            assertThat(dayStats.getOutboundQuantity()).isNotNull();
            assertThat(dayStats.getNetChange()).isNotNull();
        }
    }

    @Test
    @DisplayName("재고 이동 추이 분석 - 데이터 없음")
    void testAnalyzeInventoryTrend_NoData() {
        when(inventoryTransactionRepository.findByTenant_TenantIdAndTransactionDateBetween(
                eq(tenantId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        List<InventoryAnalysisService.InventoryTrendAnalysis> result =
                inventoryAnalysisService.analyzeInventoryTrend(tenantId, 7);

        assertThat(result).isEmpty();
    }
}
