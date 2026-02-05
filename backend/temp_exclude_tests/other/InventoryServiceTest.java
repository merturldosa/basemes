package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.InventoryRepository;
import kr.co.softice.mes.domain.repository.LotRepository;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.WarehouseRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * InventoryService Unit Test
 *
 * 테스트 대상:
 * - 재고 조회
 * - 재고 예약 (reserveInventory)
 * - 예약 해제 (releaseReservedInventory)
 * - 재고 부족 예외 처리
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService 단위 테스트")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private TenantEntity testTenant;
    private WarehouseEntity testWarehouse;
    private ProductEntity testProduct;
    private LotEntity testLot;
    private InventoryEntity testInventory;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        testWarehouse = new WarehouseEntity();
        testWarehouse.setWarehouseId(1L);
        testWarehouse.setWarehouseCode("WH-RAW");
        testWarehouse.setWarehouseName("원자재 창고");
        testWarehouse.setWarehouseType("RAW_MATERIAL");
        testWarehouse.setTenant(testTenant);

        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P-001");
        testProduct.setProductName("테스트 제품");
        testProduct.setTenant(testTenant);

        testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT-2026-001");
        testLot.setProduct(testProduct);
        testLot.setQualityStatus("PASSED");
        testLot.setInitialQuantity(new BigDecimal("1000"));
        testLot.setCurrentQuantity(new BigDecimal("1000"));
        testLot.setExpiryDate(LocalDate.now().plusMonths(6));
        testLot.setCreatedAt(LocalDateTime.now());
        testLot.setTenant(testTenant);

        testInventory = new InventoryEntity();
        testInventory.setInventoryId(1L);
        testInventory.setTenant(testTenant);
        testInventory.setWarehouse(testWarehouse);
        testInventory.setProduct(testProduct);
        testInventory.setLot(testLot);
        testInventory.setAvailableQuantity(new BigDecimal("1000"));
        testInventory.setReservedQuantity(BigDecimal.ZERO);
        // Note: InventoryEntity does not have isActive field
    }

    @Test
    @DisplayName("재고 조회 - 테넌트별 조회 성공")
    void testFindByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<InventoryEntity> expectedInventories = Arrays.asList(testInventory);
        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedInventories);

        // When
        List<InventoryEntity> result = inventoryService.findByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInventoryId()).isEqualTo(1L);
        assertThat(result.get(0).getAvailableQuantity()).isEqualByComparingTo("1000");
        verify(inventoryRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("재고 예약 - 성공 (가용 재고 충분)")
    void testReserveInventory_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 1L;
        BigDecimal reserveQuantity = new BigDecimal("200");

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryRepository.findByIdWithAllRelations(anyLong()))
                .thenAnswer(invocation -> Optional.of(testInventory));

        // When
        InventoryEntity result = inventoryService.reserveInventory(
                tenantId, warehouseId, productId, lotId, reserveQuantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("800");  // 1000 - 200
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("200");    // 0 + 200
        assertThat(result.getLastTransactionType()).isEqualTo("RESERVE");
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("재고 예약 - 실패 (가용 재고 부족)")
    void testReserveInventory_Fail_InsufficientStock() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 1L;
        BigDecimal reserveQuantity = new BigDecimal("1500");  // 가용 재고(1000)보다 많음

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId))
                .thenReturn(Optional.of(testInventory));

        // When & Then
        assertThatThrownBy(() ->
            inventoryService.reserveInventory(tenantId, warehouseId, productId, lotId, reserveQuantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient inventory")
                .hasMessageContaining("available=1000")
                .hasMessageContaining("requested=1500");

        // 저장이 호출되지 않아야 함
        verify(inventoryRepository, never()).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("재고 예약 - 실패 (재고 레코드 없음)")
    void testReserveInventory_Fail_InventoryNotFound() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 999L;  // 존재하지 않는 제품
        Long lotId = 1L;
        BigDecimal reserveQuantity = new BigDecimal("100");

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
            inventoryService.reserveInventory(tenantId, warehouseId, productId, lotId, reserveQuantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient inventory available");
    }

    @Test
    @DisplayName("예약 해제 - 성공")
    void testReleaseReservedInventory_Success() {
        // Given
        testInventory.setAvailableQuantity(new BigDecimal("800"));
        testInventory.setReservedQuantity(new BigDecimal("200"));

        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 1L;
        BigDecimal releaseQuantity = new BigDecimal("200");

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryRepository.findByIdWithAllRelations(anyLong()))
                .thenAnswer(invocation -> Optional.of(testInventory));

        // When
        InventoryEntity result = inventoryService.releaseReservedInventory(
                tenantId, warehouseId, productId, lotId, releaseQuantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("1000");  // 800 + 200
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("0");      // 200 - 200
        assertThat(result.getLastTransactionType()).isEqualTo("RELEASE");
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("예약 해제 - 실패 (예약 수량 부족)")
    void testReleaseReservedInventory_Fail_InsufficientReserved() {
        // Given
        testInventory.setAvailableQuantity(new BigDecimal("900"));
        testInventory.setReservedQuantity(new BigDecimal("100"));  // 예약 수량 100

        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 1L;
        BigDecimal releaseQuantity = new BigDecimal("200");  // 200 해제 시도 (100보다 많음)

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId))
                .thenReturn(Optional.of(testInventory));

        // When & Then
        assertThatThrownBy(() ->
            inventoryService.releaseReservedInventory(
                tenantId, warehouseId, productId, lotId, releaseQuantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient reserved inventory");

        verify(inventoryRepository, never()).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("재고 조회 - 창고 및 제품별 조회")
    void testFindByTenantAndWarehouse_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        List<InventoryEntity> expectedInventories = Arrays.asList(testInventory);

        when(inventoryRepository.findByTenantIdAndWarehouseIdWithAllRelations(tenantId, warehouseId))
                .thenReturn(expectedInventories);

        // When
        List<InventoryEntity> result = inventoryService.findByTenantAndWarehouse(tenantId, warehouseId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWarehouse().getWarehouseId()).isEqualTo(warehouseId);
        verify(inventoryRepository, times(1))
                .findByTenantIdAndWarehouseIdWithAllRelations(tenantId, warehouseId);
    }

    @Test
    @DisplayName("재고 예약 - LOT 미지정 시 자동 선택")
    void testReserveInventory_AutoSelectLot_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = null;  // LOT 미지정
        BigDecimal reserveQuantity = new BigDecimal("100");

        List<InventoryEntity> inventories = Arrays.asList(testInventory);
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
                tenantId, warehouseId, productId))
                .thenReturn(inventories);

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryRepository.findByIdWithAllRelations(anyLong()))
                .thenAnswer(invocation -> Optional.of(testInventory));

        // When
        InventoryEntity result = inventoryService.reserveInventory(
                tenantId, warehouseId, productId, lotId, reserveQuantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("900");  // 1000 - 100
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("100");   // 0 + 100
        verify(inventoryRepository, times(1))
                .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
                        tenantId, warehouseId, productId);
    }

    @Test
    @DisplayName("재고 일관성 검증 - 가용 + 예약 = 총 재고")
    void testInventoryConsistency_AvailablePlusReservedEqualsTotal() {
        // Given
        BigDecimal initialAvailable = new BigDecimal("1000");
        BigDecimal initialReserved = BigDecimal.ZERO;
        BigDecimal reserveAmount = new BigDecimal("300");

        testInventory.setAvailableQuantity(initialAvailable);
        testInventory.setReservedQuantity(initialReserved);

        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 1L;

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryRepository.findByIdWithAllRelations(anyLong()))
                .thenAnswer(invocation -> Optional.of(testInventory));

        // When
        InventoryEntity result = inventoryService.reserveInventory(
                tenantId, warehouseId, productId, lotId, reserveAmount);

        // Then - 재고 일관성 검증
        BigDecimal totalBefore = initialAvailable.add(initialReserved);  // 1000 + 0 = 1000
        BigDecimal totalAfter = result.getAvailableQuantity().add(result.getReservedQuantity());  // 700 + 300 = 1000

        assertThat(totalBefore).isEqualByComparingTo(totalAfter);
        assertThat(totalAfter).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("재고 조회 - 제품별 조회 성공")
    void testFindByTenantAndProduct_Success() {
        // Given
        String tenantId = "TEST001";
        Long productId = 1L;
        List<InventoryEntity> expectedInventories = Arrays.asList(testInventory);

        when(inventoryRepository.findByTenant_TenantIdAndProduct_ProductId(tenantId, productId))
                .thenReturn(expectedInventories);

        // When
        List<InventoryEntity> result = inventoryService.findByTenantAndProduct(tenantId, productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getProductId()).isEqualTo(productId);
        verify(inventoryRepository, times(1))
                .findByTenant_TenantIdAndProduct_ProductId(tenantId, productId);
    }

    @Test
    @DisplayName("재고 조회 - ID로 조회 성공")
    void testFindById_Success() {
        // Given
        Long inventoryId = 1L;
        when(inventoryRepository.findByIdWithAllRelations(inventoryId))
                .thenReturn(Optional.of(testInventory));

        // When
        Optional<InventoryEntity> result = inventoryService.findById(inventoryId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getInventoryId()).isEqualTo(inventoryId);
        verify(inventoryRepository, times(1)).findByIdWithAllRelations(inventoryId);
    }

    @Test
    @DisplayName("재고 조회 - ID로 조회 실패 (존재하지 않음)")
    void testFindById_NotFound() {
        // Given
        Long inventoryId = 999L;
        when(inventoryRepository.findByIdWithAllRelations(inventoryId))
                .thenReturn(Optional.empty());

        // When
        Optional<InventoryEntity> result = inventoryService.findById(inventoryId);

        // Then
        assertThat(result).isEmpty();
        verify(inventoryRepository, times(1)).findByIdWithAllRelations(inventoryId);
    }

    @Test
    @DisplayName("재고 조회 - 위치별 조회 성공")
    void testFindByLocation_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 1L;

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId))
                .thenReturn(Optional.of(testInventory));

        // When
        Optional<InventoryEntity> result = inventoryService.findByLocation(
                tenantId, warehouseId, productId, lotId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getWarehouse().getWarehouseId()).isEqualTo(warehouseId);
        assertThat(result.get().getProduct().getProductId()).isEqualTo(productId);
        assertThat(result.get().getLot().getLotId()).isEqualTo(lotId);
    }

    @Test
    @DisplayName("재고 업데이트 - 성공")
    void testUpdateInventory_Success() {
        // Given
        testInventory.setAvailableQuantity(new BigDecimal("900"));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenReturn(testInventory);

        when(inventoryRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testInventory));

        // When
        InventoryEntity result = inventoryService.updateInventory(testInventory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("900");
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
        verify(inventoryRepository, times(1)).findByIdWithAllRelations(anyLong());
    }

    @Test
    @DisplayName("재고 삭제 - 성공")
    void testDeleteInventory_Success() {
        // Given
        Long inventoryId = 1L;
        doNothing().when(inventoryRepository).deleteById(inventoryId);

        // When
        inventoryService.deleteInventory(inventoryId);

        // Then
        verify(inventoryRepository, times(1)).deleteById(inventoryId);
    }

    @Test
    @DisplayName("재고 잔액 업데이트 - IN_RECEIVE 유형")
    void testUpdateInventoryBalance_InReceive() {
        // Given
        BigDecimal quantityChange = new BigDecimal("500");

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        InventoryEntity result = inventoryService.updateInventoryBalance(
                testWarehouse, testProduct, testLot, quantityChange, "IN_RECEIVE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("1500");  // 1000 + 500
        assertThat(result.getLastTransactionType()).isEqualTo("IN_RECEIVE");
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("재고 잔액 업데이트 - OUT_ISSUE 유형")
    void testUpdateInventoryBalance_OutIssue() {
        // Given
        BigDecimal quantityChange = new BigDecimal("300");

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        InventoryEntity result = inventoryService.updateInventoryBalance(
                testWarehouse, testProduct, testLot, quantityChange, "OUT_ISSUE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("700");  // 1000 - 300
        assertThat(result.getLastTransactionType()).isEqualTo("OUT_ISSUE");
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("재고 잔액 업데이트 - 신규 레코드 생성")
    void testUpdateInventoryBalance_CreateNew() {
        // Given
        BigDecimal quantityChange = new BigDecimal("100");

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());  // 기존 재고 없음

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        InventoryEntity result = inventoryService.updateInventoryBalance(
                testWarehouse, testProduct, testLot, quantityChange, "IN_RECEIVE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("100");  // 0 + 100
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("0");
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("재고 생성 또는 조회 - 기존 레코드 반환")
    void testFindOrCreateInventory_ExistingRecord() {
        // Given
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        // When
        InventoryEntity result = inventoryService.findOrCreateInventory(
                testTenant, testWarehouse, testProduct, testLot);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getInventoryId()).isEqualTo(1L);
        verify(inventoryRepository, never()).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("재고 생성 또는 조회 - 신규 레코드 생성")
    void testFindOrCreateInventory_CreateNew() {
        // Given
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryEntity saved = invocation.getArgument(0);
                    saved.setInventoryId(2L);
                    return saved;
                });

        // When
        InventoryEntity result = inventoryService.findOrCreateInventory(
                testTenant, testWarehouse, testProduct, testLot);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getInventoryId()).isEqualTo(2L);
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("0");
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("0");
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("저재고 조회 - 임계값 미만 재고 조회")
    void testCalculateLowStock_Success() {
        // Given
        String tenantId = "TEST001";
        BigDecimal threshold = new BigDecimal("50");

        // 저재고 제품 생성
        InventoryEntity lowStockInventory = new InventoryEntity();
        lowStockInventory.setInventoryId(2L);
        lowStockInventory.setTenant(testTenant);
        lowStockInventory.setWarehouse(testWarehouse);
        lowStockInventory.setProduct(testProduct);
        lowStockInventory.setLot(testLot);
        lowStockInventory.setAvailableQuantity(new BigDecimal("30"));  // 30 < 50
        lowStockInventory.setReservedQuantity(new BigDecimal("10"));   // 합계 40 < 50

        List<InventoryEntity> allInventories = Arrays.asList(testInventory, lowStockInventory);

        when(inventoryRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(allInventories);

        // When
        List<InventoryEntity> result = inventoryService.calculateLowStock(tenantId, threshold);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);  // testInventory는 1000+0=1000 > 50이므로 제외
        assertThat(result.get(0).getInventoryId()).isEqualTo(2L);
        verify(inventoryRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("총 재고 수량 계산 - 가용 + 예약")
    void testGetTotalQuantity_Success() {
        // Given
        testInventory.setAvailableQuantity(new BigDecimal("700"));
        testInventory.setReservedQuantity(new BigDecimal("300"));

        // When
        BigDecimal totalQuantity = inventoryService.getTotalQuantity(testInventory);

        // Then
        assertThat(totalQuantity).isEqualByComparingTo("1000");  // 700 + 300
    }

    @Test
    @DisplayName("재고 예약 - LOT 지정 없음 (자동 선택)")
    void testReserveInventory_WithoutLotId_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = null;  // LOT을 지정하지 않음
        BigDecimal quantity = new BigDecimal("50");

        // 여러 재고 중 첫 번째 충분한 재고를 선택
        List<InventoryEntity> inventories = Arrays.asList(testInventory);

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
                tenantId, warehouseId, productId))
                .thenReturn(inventories);

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testInventory));

        // When
        InventoryEntity result = inventoryService.reserveInventory(
                tenantId, warehouseId, productId, lotId, quantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("950");  // 1000 - 50
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("50");
        assertThat(result.getLastTransactionType()).isEqualTo("RESERVE");

        verify(inventoryRepository, times(1))
                .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(tenantId, warehouseId, productId);
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("재고 예약 - LOT 지정 없음 (충분한 재고 없음)")
    void testReserveInventory_WithoutLotId_InsufficientStock() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = null;
        BigDecimal quantity = new BigDecimal("2000");  // 모든 재고보다 많음

        // 충분한 재고가 없는 경우
        testInventory.setAvailableQuantity(new BigDecimal("100"));
        List<InventoryEntity> inventories = Arrays.asList(testInventory);

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
                tenantId, warehouseId, productId))
                .thenReturn(inventories);

        // When & Then
        assertThatThrownBy(() ->
                inventoryService.reserveInventory(tenantId, warehouseId, productId, lotId, quantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient inventory available");

        verify(inventoryRepository, never()).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("findOrCreate - LOT이 null인 경우")
    void testFindOrCreateInventory_WithNullLot() {
        // Given
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), isNull()))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryEntity saved = invocation.getArgument(0);
                    saved.setInventoryId(99L);
                    return saved;
                });

        // When
        InventoryEntity result = inventoryService.findOrCreateInventory(
                testTenant, testWarehouse, testProduct, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getInventoryId()).isEqualTo(99L);
        assertThat(result.getTenant()).isEqualTo(testTenant);
        assertThat(result.getWarehouse()).isEqualTo(testWarehouse);
        assertThat(result.getProduct()).isEqualTo(testProduct);
        assertThat(result.getLot()).isNull();
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("0");
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("0");

        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
    }
}
