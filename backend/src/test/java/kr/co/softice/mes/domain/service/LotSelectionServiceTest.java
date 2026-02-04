package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.InventoryRepository;
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
 * LotSelectionService Unit Test
 *
 * 테스트 대상:
 * - FIFO (First-In-First-Out) 로직
 * - FEFO (First-Expired-First-Out) 로직
 * - 여러 LOT에 걸친 수량 할당
 * - 유효기간 Null 처리
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LotSelectionService 단위 테스트")
class LotSelectionServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private LotSelectionService lotSelectionService;

    private TenantEntity testTenant;
    private WarehouseEntity testWarehouse;
    private ProductEntity testProduct;
    private List<InventoryEntity> testInventories;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");

        testWarehouse = new WarehouseEntity();
        testWarehouse.setWarehouseId(1L);
        testWarehouse.setWarehouseCode("WH-FG");
        testWarehouse.setTenant(testTenant);

        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P-LCD-001");
        testProduct.setTenant(testTenant);

        // 3개의 LOT 생성 (생성일 및 유효기간 다름)
        LotEntity lot1 = createLot(1L, "LOT-2026-001",
                LocalDateTime.now().minusDays(10),
                LocalDate.now().plusMonths(3));

        LotEntity lot2 = createLot(2L, "LOT-2026-002",
                LocalDateTime.now().minusDays(5),
                LocalDate.now().plusMonths(6));

        LotEntity lot3 = createLot(3L, "LOT-2026-003",
                LocalDateTime.now().minusDays(1),
                LocalDate.now().plusMonths(9));

        // 재고 엔티티 생성
        InventoryEntity inventory1 = createInventory(1L, lot1, new BigDecimal("100"));
        InventoryEntity inventory2 = createInventory(2L, lot2, new BigDecimal("150"));
        InventoryEntity inventory3 = createInventory(3L, lot3, new BigDecimal("200"));

        testInventories = Arrays.asList(inventory1, inventory2, inventory3);
    }

    private LotEntity createLot(Long lotId, String lotNo, LocalDateTime createdAt, LocalDate expiryDate) {
        LotEntity lot = new LotEntity();
        lot.setLotId(lotId);
        lot.setLotNo(lotNo);
        lot.setProduct(testProduct);
        lot.setQualityStatus("PASSED");
        lot.setCreatedAt(createdAt);
        lot.setExpiryDate(expiryDate);
        lot.setTenant(testTenant);
        return lot;
    }

    private InventoryEntity createInventory(Long inventoryId, LotEntity lot, BigDecimal quantity) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setInventoryId(inventoryId);
        inventory.setTenant(testTenant);
        inventory.setWarehouse(testWarehouse);
        inventory.setProduct(testProduct);
        inventory.setLot(lot);
        inventory.setAvailableQuantity(quantity);
        inventory.setReservedQuantity(BigDecimal.ZERO);
        return inventory;
    }

    @Test
    @DisplayName("FIFO - 단일 LOT 할당 (충분한 재고)")
    void testSelectLotsByFIFO_SingleLot_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("50");  // 첫 번째 LOT(100)에서 충분

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(testInventories);

        // When
        List<LotSelectionService.LotAllocation> result = lotSelectionService.selectLotsByFIFO(
                tenantId, warehouseId, productId, requiredQuantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);  // 단일 LOT만 사용
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT-2026-001");  // 가장 오래된 LOT
        assertThat(result.get(0).getAllocatedQuantity()).isEqualByComparingTo("50");
        assertThat(result.get(0).getAvailableQuantity()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("FIFO - 여러 LOT 할당 (첫 번째 LOT 부족)")
    void testSelectLotsByFIFO_MultipleLots_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("200");  // LOT1(100) + LOT2(100) 필요

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(testInventories);

        // When
        List<LotSelectionService.LotAllocation> result = lotSelectionService.selectLotsByFIFO(
                tenantId, warehouseId, productId, requiredQuantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);  // 2개 LOT 사용

        // 첫 번째 LOT (가장 오래된 것)
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT-2026-001");
        assertThat(result.get(0).getAllocatedQuantity()).isEqualByComparingTo("100");

        // 두 번째 LOT
        assertThat(result.get(1).getLotNo()).isEqualTo("LOT-2026-002");
        assertThat(result.get(1).getAllocatedQuantity()).isEqualByComparingTo("100");

        // 총 할당량 검증
        BigDecimal totalAllocated = result.stream()
                .map(LotSelectionService.LotAllocation::getAllocatedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalAllocated).isEqualByComparingTo("200");
    }

    @Test
    @DisplayName("FIFO - 모든 LOT 사용 (전체 재고 부족)")
    void testSelectLotsByFIFO_AllLots_InsufficientStock() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("500");  // 총 재고(450)보다 많음

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(testInventories);

        // When & Then - 재고 부족 예외 발생
        assertThatThrownBy(() ->
            lotSelectionService.selectLotsByFIFO(tenantId, warehouseId, productId, requiredQuantity))
                .isInstanceOf(kr.co.softice.mes.common.exception.BusinessException.class);
    }

    @Test
    @DisplayName("FIFO - LOT 생성일 순서 검증")
    void testSelectLotsByFIFO_OrderByCreatedDate() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("400");  // 3개 LOT 모두 사용

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(testInventories);

        // When
        List<LotSelectionService.LotAllocation> result = lotSelectionService.selectLotsByFIFO(
                tenantId, warehouseId, productId, requiredQuantity);

        // Then
        assertThat(result).hasSize(3);

        // 생성일 오름차순 검증 (LOT 번호로 확인)
        // LOT-2026-001 (10일 전), LOT-2026-002 (5일 전), LOT-2026-003 (1일 전)
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT-2026-001");
        assertThat(result.get(1).getLotNo()).isEqualTo("LOT-2026-002");
        assertThat(result.get(2).getLotNo()).isEqualTo("LOT-2026-003");
    }

    @Test
    @DisplayName("FEFO - 유효기간 빠른 순서 할당")
    void testSelectLotsByFEFO_OrderByExpiryDate() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("50");

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(testInventories);

        // When
        List<LotSelectionService.LotAllocation> result = lotSelectionService.selectLotsByFEFO(
                tenantId, warehouseId, productId, requiredQuantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        // 유효기간이 가장 빠른 LOT (3개월 후 만료)
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT-2026-001");
        assertThat(result.get(0).getExpiryDate())
                .isCloseTo(LocalDate.now().plusMonths(3), within(1, java.time.temporal.ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("FEFO - 여러 LOT 할당 (유효기간 순)")
    void testSelectLotsByFEFO_MultipleLots_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("200");

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(testInventories);

        // When
        List<LotSelectionService.LotAllocation> result = lotSelectionService.selectLotsByFEFO(
                tenantId, warehouseId, productId, requiredQuantity);

        // Then
        assertThat(result).hasSize(2);

        // 유효기간 순서 검증
        assertThat(result.get(0).getExpiryDate())
                .isBefore(result.get(1).getExpiryDate());

        // 첫 번째: 3개월 후, 두 번째: 6개월 후
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT-2026-001");
        assertThat(result.get(1).getLotNo()).isEqualTo("LOT-2026-002");
    }

    @Test
    @DisplayName("FEFO - 유효기간 Null 처리 (맨 뒤로)")
    void testSelectLotsByFEFO_NullExpiryDate_MovedToEnd() {
        // Given
        // LOT2의 유효기간을 null로 설정
        testInventories.get(1).getLot().setExpiryDate(null);

        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("300");

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(testInventories);

        // When
        List<LotSelectionService.LotAllocation> result = lotSelectionService.selectLotsByFEFO(
                tenantId, warehouseId, productId, requiredQuantity);

        // Then
        // LOT1(100, 3개월) + LOT3(200, 9개월) = 300으로 충분하므로 2개 LOT만 사용
        assertThat(result).hasSize(2);

        // 유효기간 있는 LOT가 먼저 사용됨
        assertThat(result.get(0).getExpiryDate()).isNotNull();
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT-2026-001");  // 3개월 후 만료
        assertThat(result.get(1).getExpiryDate()).isNotNull();
        assertThat(result.get(1).getLotNo()).isEqualTo("LOT-2026-003");  // 9개월 후 만료

        // 총 할당량 검증
        BigDecimal totalAllocated = result.stream()
                .map(LotSelectionService.LotAllocation::getAllocatedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalAllocated).isEqualByComparingTo("300");
    }

    @Test
    @DisplayName("FIFO - 가용 재고 없음")
    void testSelectLotsByFIFO_NoAvailableStock() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("100");

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(Arrays.asList());  // 빈 리스트

        // When & Then - 재고 부족 예외 발생
        assertThatThrownBy(() ->
            lotSelectionService.selectLotsByFIFO(tenantId, warehouseId, productId, requiredQuantity))
                .isInstanceOf(kr.co.softice.mes.common.exception.BusinessException.class);
    }

    @Test
    @DisplayName("FEFO - 할당 수량 정확성 검증")
    void testSelectLotsByFEFO_AllocationAccuracy() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("180");  // LOT1(100) + LOT2(80)

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                warehouseId, productId, BigDecimal.ZERO))
                .thenReturn(testInventories);

        // When
        List<LotSelectionService.LotAllocation> result = lotSelectionService.selectLotsByFEFO(
                tenantId, warehouseId, productId, requiredQuantity);

        // Then
        assertThat(result).hasSize(2);

        // 첫 번째 LOT 전량 사용
        assertThat(result.get(0).getAllocatedQuantity()).isEqualByComparingTo("100");
        assertThat(result.get(0).getAvailableQuantity()).isEqualByComparingTo("100");

        // 두 번째 LOT 부분 사용
        assertThat(result.get(1).getAllocatedQuantity()).isEqualByComparingTo("80");
        assertThat(result.get(1).getAvailableQuantity()).isEqualByComparingTo("150");

        // 총합 검증
        BigDecimal totalAllocated = result.stream()
                .map(LotSelectionService.LotAllocation::getAllocatedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalAllocated).isEqualByComparingTo("180");
    }

    @Test
    @DisplayName("특정 LOT 선택 - 성공")
    void testSelectSpecificLot_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("50");

        InventoryEntity specificInventory = testInventories.get(0);  // LOT-2026-001, 100개

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                warehouseId, productId, lotId))
                .thenReturn(java.util.Optional.of(specificInventory));

        // When
        LotSelectionService.LotAllocation result = lotSelectionService.selectSpecificLot(
                tenantId, warehouseId, productId, lotId, requiredQuantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLotId()).isEqualTo(1L);
        assertThat(result.getLotNo()).isEqualTo("LOT-2026-001");
        assertThat(result.getAllocatedQuantity()).isEqualByComparingTo("50");
        assertThat(result.getAvailableQuantity()).isEqualByComparingTo("100");
        assertThat(result.getExpiryDate()).isNotNull();

        verify(inventoryRepository, times(1))
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(warehouseId, productId, lotId);
    }

    @Test
    @DisplayName("특정 LOT 선택 - 실패 (재고 없음)")
    void testSelectSpecificLot_Fail_InventoryNotFound() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 999L;  // 존재하지 않는 LOT
        BigDecimal requiredQuantity = new BigDecimal("50");

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                warehouseId, productId, lotId))
                .thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                lotSelectionService.selectSpecificLot(tenantId, warehouseId, productId, lotId, requiredQuantity))
                .isInstanceOf(kr.co.softice.mes.common.exception.BusinessException.class);

        verify(inventoryRepository, times(1))
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(warehouseId, productId, lotId);
    }

    @Test
    @DisplayName("특정 LOT 선택 - 실패 (재고 부족)")
    void testSelectSpecificLot_Fail_InsufficientInventory() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        Long productId = 1L;
        Long lotId = 1L;
        BigDecimal requiredQuantity = new BigDecimal("150");  // 가용 재고(100)보다 많음

        InventoryEntity specificInventory = testInventories.get(0);  // 100개만 있음

        when(inventoryRepository.findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                warehouseId, productId, lotId))
                .thenReturn(java.util.Optional.of(specificInventory));

        // When & Then
        assertThatThrownBy(() ->
                lotSelectionService.selectSpecificLot(tenantId, warehouseId, productId, lotId, requiredQuantity))
                .isInstanceOf(kr.co.softice.mes.common.exception.BusinessException.class);
    }

    @Test
    @DisplayName("만료 예정 LOT 조회 - 성공")
    void testFindExpiringLots_Success() {
        // Given
        String tenantId = "TEST001";
        int daysUntilExpiry = 120;  // 120일 이내 만료
        LocalDate expiryThreshold = LocalDate.now().plusDays(120);

        // LOT1: 3개월(90일) 후 만료 → 포함
        // LOT2: 6개월(180일) 후 만료 → 제외
        // LOT3: 9개월(270일) 후 만료 → 제외
        List<LotEntity> expiringLots = Arrays.asList(testInventories.get(0).getLot());

        when(lotRepository.findByTenant_TenantIdAndExpiryDateBeforeAndIsActiveTrue(
                eq(tenantId), any(LocalDate.class)))
                .thenReturn(expiringLots);

        // When
        List<LotEntity> result = lotSelectionService.findExpiringLots(tenantId, daysUntilExpiry);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT-2026-001");
        assertThat(result.get(0).getExpiryDate()).isBefore(expiryThreshold);

        verify(lotRepository, times(1))
                .findByTenant_TenantIdAndExpiryDateBeforeAndIsActiveTrue(eq(tenantId), any(LocalDate.class));
    }

    @Test
    @DisplayName("만료 예정 LOT 조회 - 만료 예정 LOT 없음")
    void testFindExpiringLots_NoExpiringLots() {
        // Given
        String tenantId = "TEST001";
        int daysUntilExpiry = 30;  // 30일 이내 만료 (모든 LOT이 30일 이후 만료)

        when(lotRepository.findByTenant_TenantIdAndExpiryDateBeforeAndIsActiveTrue(
                eq(tenantId), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // When
        List<LotEntity> result = lotSelectionService.findExpiringLots(tenantId, daysUntilExpiry);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(lotRepository, times(1))
                .findByTenant_TenantIdAndExpiryDateBeforeAndIsActiveTrue(eq(tenantId), any(LocalDate.class));
    }
}
