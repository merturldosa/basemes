package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GoodsReceiptService Unit Test
 *
 * 테스트 대상:
 * - 입하 생성 (createGoodsReceipt)
 * - 입하 번호 자동 생성
 * - LOT 자동 생성
 * - 재고 트랜잭션 생성
 * - IQC 자동 생성 (QMS 통합)
 * - 입하 완료 (completeGoodsReceipt)
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoodsReceiptService 단위 테스트")
class GoodsReceiptServiceTest {

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QualityInspectionRepository qualityInspectionRepository;

    @Mock
    private QualityStandardRepository qualityStandardRepository;

    @InjectMocks
    private GoodsReceiptService goodsReceiptService;

    private TenantEntity testTenant;
    private WarehouseEntity testWarehouse;
    private ProductEntity testProduct;
    private UserEntity testUser;
    private SupplierEntity testSupplier;
    private PurchaseOrderEntity testPurchaseOrder;
    private GoodsReceiptEntity testGoodsReceipt;
    private GoodsReceiptItemEntity testGoodsReceiptItem;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");

        testWarehouse = new WarehouseEntity();
        testWarehouse.setWarehouseId(1L);
        testWarehouse.setWarehouseCode("WH-RAW");
        testWarehouse.setWarehouseName("원자재 창고");
        testWarehouse.setWarehouseType("RAW_MATERIAL");
        testWarehouse.setTenant(testTenant);

        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P-PCB-001");
        testProduct.setProductName("LCD 구동 PCB");
        testProduct.setTenant(testTenant);

        testUser = new UserEntity();
        testUser.setUserId(1L);
        testUser.setUsername("admin");
        testUser.setTenant(testTenant);

        testSupplier = new SupplierEntity();
        testSupplier.setSupplierId(1L);
        testSupplier.setSupplierCode("SUP-001");
        testSupplier.setSupplierName("ABC 전자부품");
        testSupplier.setTenant(testTenant);

        testPurchaseOrder = new PurchaseOrderEntity();
        testPurchaseOrder.setPurchaseOrderId(1L);
        testPurchaseOrder.setOrderNo("PO-2026-001");
        testPurchaseOrder.setStatus("APPROVED");
        testPurchaseOrder.setTenant(testTenant);

        // 입하 헤더
        testGoodsReceipt = new GoodsReceiptEntity();
        testGoodsReceipt.setTenant(testTenant);
        testGoodsReceipt.setWarehouse(testWarehouse);
        testGoodsReceipt.setReceiver(testUser);
        testGoodsReceipt.setSupplier(testSupplier);
        testGoodsReceipt.setPurchaseOrder(testPurchaseOrder);
        testGoodsReceipt.setReceiptDate(LocalDateTime.now());
        testGoodsReceipt.setReceiptType("PURCHASE");

        // 입하 항목
        testGoodsReceiptItem = new GoodsReceiptItemEntity();
        testGoodsReceiptItem.setGoodsReceipt(testGoodsReceipt);
        testGoodsReceiptItem.setProduct(testProduct);
        testGoodsReceiptItem.setReceivedQuantity(new BigDecimal("1000"));
        testGoodsReceiptItem.setUnitPrice(new BigDecimal("5000"));
        testGoodsReceiptItem.setLineAmount(new BigDecimal("5000000"));
        testGoodsReceiptItem.setLotNo("LOT-2026-001");
        testGoodsReceiptItem.setExpiryDate(LocalDate.now().plusYears(1));
        testGoodsReceiptItem.setInspectionStatus("PENDING");  // 품질 검사 필요

        testGoodsReceipt.setItems(new ArrayList<>(Arrays.asList(testGoodsReceiptItem)));
    }

    @Test
    @DisplayName("입하 생성 - 성공 (검사 불요)")
    void testCreateGoodsReceipt_Success_NoInspection() {
        // Given
        testGoodsReceiptItem.setInspectionStatus("NOT_REQUIRED");  // 검사 불요
        testProduct.setUnit("EA");  // 단위 설정

        when(goodsReceiptRepository.existsByTenant_TenantIdAndReceiptNo(anyString(), anyString()))
                .thenReturn(false);

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> {
                    GoodsReceiptEntity saved = invocation.getArgument(0);
                    saved.setGoodsReceiptId(1L);
                    return saved;
                });

        // Mock LOT creation
        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.empty());

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity lot = invocation.getArgument(0);
                    lot.setLotId(1L);
                    return lot;
                });

        // Mock inventory transaction creation
        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock inventory update
        InventoryEntity mockInventory = new InventoryEntity();
        mockInventory.setInventoryId(1L);
        mockInventory.setAvailableQuantity(new BigDecimal("1000"));
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(mockInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(goodsReceiptRepository.findByIdWithAllRelations(anyLong()))
                .thenAnswer(invocation -> Optional.of(testGoodsReceipt));

        // When
        GoodsReceiptEntity result = goodsReceiptService.createGoodsReceipt(testGoodsReceipt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceiptStatus()).isEqualTo("PENDING");
        assertThat(result.getIsActive()).isTrue();

        // 저장 호출 검증
        verify(goodsReceiptRepository, times(1)).save(any(GoodsReceiptEntity.class));
    }

    @Test
    @DisplayName("입하 생성 - 입하 번호 자동 생성")
    void testCreateGoodsReceipt_AutoGenerateReceiptNo() {
        // Given
        testGoodsReceipt.setReceiptNo(null);  // 입하 번호 미지정
        testGoodsReceiptItem.setInspectionStatus("NOT_REQUIRED");
        testProduct.setUnit("EA");

        when(goodsReceiptRepository.existsByTenant_TenantIdAndReceiptNo(anyString(), anyString()))
                .thenReturn(false);

        when(goodsReceiptRepository.findByTenantIdWithAllRelations(anyString()))
                .thenReturn(Arrays.asList());  // 기존 입하 없음

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> {
                    GoodsReceiptEntity saved = invocation.getArgument(0);
                    saved.setGoodsReceiptId(1L);
                    // receiptNo가 자동 생성되었는지 확인
                    assertThat(saved.getReceiptNo()).isNotNull();
                    assertThat(saved.getReceiptNo()).startsWith("GR-");
                    return saved;
                });

        // Mock LOT creation
        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity lot = invocation.getArgument(0);
                    lot.setLotId(1L);
                    return lot;
                });

        // Mock inventory operations
        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        InventoryEntity mockInventory = new InventoryEntity();
        mockInventory.setInventoryId(1L);
        mockInventory.setAvailableQuantity(new BigDecimal("1000"));
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(goodsReceiptRepository.findByIdWithAllRelations(anyLong()))
                .thenAnswer(invocation -> Optional.of(testGoodsReceipt));

        // When
        GoodsReceiptEntity result = goodsReceiptService.createGoodsReceipt(testGoodsReceipt);

        // Then
        assertThat(result).isNotNull();
        verify(goodsReceiptRepository, times(1)).save(any(GoodsReceiptEntity.class));
    }

    @Test
    @DisplayName("입하 생성 - 중복 입하 번호 예외")
    void testCreateGoodsReceipt_Fail_DuplicateReceiptNo() {
        // Given
        testGoodsReceipt.setReceiptNo("GR-2026-001");

        when(goodsReceiptRepository.existsByTenant_TenantIdAndReceiptNo(
                testTenant.getTenantId(), "GR-2026-001"))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() ->
            goodsReceiptService.createGoodsReceipt(testGoodsReceipt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Receipt number already exists");

        // 저장이 호출되지 않아야 함
        verify(goodsReceiptRepository, never()).save(any(GoodsReceiptEntity.class));
    }

    @Test
    @DisplayName("입하 조회 - 테넌트별 조회 성공")
    void testFindByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<GoodsReceiptEntity> expectedReceipts = Arrays.asList(testGoodsReceipt);

        when(goodsReceiptRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedReceipts);

        // When
        List<GoodsReceiptEntity> result = goodsReceiptService.findByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testGoodsReceipt);
        verify(goodsReceiptRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("입하 조회 - ID로 조회 성공")
    void testFindById_Success() {
        // Given
        Long goodsReceiptId = 1L;
        testGoodsReceipt.setGoodsReceiptId(goodsReceiptId);

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.of(testGoodsReceipt));

        // When
        Optional<GoodsReceiptEntity> result = goodsReceiptService.findById(goodsReceiptId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getGoodsReceiptId()).isEqualTo(goodsReceiptId);
        verify(goodsReceiptRepository, times(1)).findByIdWithAllRelations(goodsReceiptId);
    }

    @Test
    @DisplayName("입하 조회 - ID로 조회 실패 (존재하지 않음)")
    void testFindById_NotFound() {
        // Given
        Long goodsReceiptId = 999L;

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.empty());

        // When
        Optional<GoodsReceiptEntity> result = goodsReceiptService.findById(goodsReceiptId);

        // Then
        assertThat(result).isEmpty();
        verify(goodsReceiptRepository, times(1)).findByIdWithAllRelations(goodsReceiptId);
    }

    @Test
    @DisplayName("입하 조회 - 상태별 조회")
    void testFindByStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String status = "PENDING";
        testGoodsReceipt.setReceiptStatus(status);
        List<GoodsReceiptEntity> expectedReceipts = Arrays.asList(testGoodsReceipt);

        when(goodsReceiptRepository.findByTenantIdAndStatus(tenantId, status))
                .thenReturn(expectedReceipts);

        // When
        List<GoodsReceiptEntity> result = goodsReceiptService.findByStatus(tenantId, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReceiptStatus()).isEqualTo(status);
        verify(goodsReceiptRepository, times(1)).findByTenantIdAndStatus(tenantId, status);
    }

    @Test
    @DisplayName("입하 조회 - 구매 주문별 조회")
    void testFindByPurchaseOrderId_Success() {
        // Given
        String tenantId = "TEST001";
        Long purchaseOrderId = 1L;
        List<GoodsReceiptEntity> expectedReceipts = Arrays.asList(testGoodsReceipt);

        when(goodsReceiptRepository.findByTenantIdAndPurchaseOrderId(tenantId, purchaseOrderId))
                .thenReturn(expectedReceipts);

        // When
        List<GoodsReceiptEntity> result = goodsReceiptService.findByPurchaseOrderId(tenantId, purchaseOrderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPurchaseOrder().getPurchaseOrderId()).isEqualTo(purchaseOrderId);
        verify(goodsReceiptRepository, times(1))
                .findByTenantIdAndPurchaseOrderId(tenantId, purchaseOrderId);
    }

    @Test
    @DisplayName("입하 조회 - 창고별 조회")
    void testFindByWarehouseId_Success() {
        // Given
        String tenantId = "TEST001";
        Long warehouseId = 1L;
        List<GoodsReceiptEntity> expectedReceipts = Arrays.asList(testGoodsReceipt);

        when(goodsReceiptRepository.findByTenantIdAndWarehouseId(tenantId, warehouseId))
                .thenReturn(expectedReceipts);

        // When
        List<GoodsReceiptEntity> result = goodsReceiptService.findByWarehouseId(tenantId, warehouseId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWarehouse().getWarehouseId()).isEqualTo(warehouseId);
        verify(goodsReceiptRepository, times(1))
                .findByTenantIdAndWarehouseId(tenantId, warehouseId);
    }

    @Test
    @DisplayName("입하 조회 - 날짜 범위별 조회")
    void testFindByDateRange_Success() {
        // Given
        String tenantId = "TEST001";
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<GoodsReceiptEntity> expectedReceipts = Arrays.asList(testGoodsReceipt);

        when(goodsReceiptRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate))
                .thenReturn(expectedReceipts);

        // When
        List<GoodsReceiptEntity> result = goodsReceiptService.findByDateRange(tenantId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(goodsReceiptRepository, times(1))
                .findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Test
    @DisplayName("입하 항목 - 합계 계산 검증")
    void testCreateGoodsReceipt_CalculateTotals() {
        // Given
        testGoodsReceiptItem.setInspectionStatus("NOT_REQUIRED");
        testGoodsReceiptItem.setLotNo("LOT-001");
        testProduct.setUnit("EA");

        // 2개 항목 추가
        GoodsReceiptItemEntity item2 = new GoodsReceiptItemEntity();
        item2.setGoodsReceipt(testGoodsReceipt);
        item2.setProduct(testProduct);
        item2.setReceivedQuantity(new BigDecimal("500"));
        item2.setUnitPrice(new BigDecimal("5000"));
        item2.setLineAmount(new BigDecimal("2500000"));
        item2.setInspectionStatus("NOT_REQUIRED");
        item2.setLotNo("LOT-002");

        testGoodsReceipt.getItems().add(item2);

        when(goodsReceiptRepository.existsByTenant_TenantIdAndReceiptNo(anyString(), anyString()))
                .thenReturn(false);

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> {
                    GoodsReceiptEntity saved = invocation.getArgument(0);
                    saved.setGoodsReceiptId(1L);

                    // 합계 계산 검증
                    assertThat(saved.getTotalQuantity())
                            .isEqualByComparingTo("1500");  // 1000 + 500
                    assertThat(saved.getTotalAmount())
                            .isEqualByComparingTo("7500000");  // 5000000 + 2500000

                    return saved;
                });

        // Mock LOT creation
        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity lot = invocation.getArgument(0);
                    lot.setLotId(1L);
                    return lot;
                });

        // Mock inventory operations
        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        InventoryEntity mockInventory = new InventoryEntity();
        mockInventory.setInventoryId(1L);
        mockInventory.setAvailableQuantity(new BigDecimal("1000"));
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(goodsReceiptRepository.findByIdWithAllRelations(anyLong()))
                .thenAnswer(invocation -> Optional.of(testGoodsReceipt));

        // When
        GoodsReceiptEntity result = goodsReceiptService.createGoodsReceipt(testGoodsReceipt);

        // Then
        assertThat(result).isNotNull();
        verify(goodsReceiptRepository, times(1)).save(any(GoodsReceiptEntity.class));
    }

    @Test
    @DisplayName("입하 생성 - 초기 상태 설정 검증")
    void testCreateGoodsReceipt_InitialStatusAndActiveFlag() {
        // Given
        testGoodsReceipt.setReceiptStatus(null);  // 상태 미지정
        testGoodsReceipt.setIsActive(null);  // 활성 여부 미지정
        testGoodsReceiptItem.setInspectionStatus("NOT_REQUIRED");
        testProduct.setUnit("EA");

        when(goodsReceiptRepository.existsByTenant_TenantIdAndReceiptNo(anyString(), anyString()))
                .thenReturn(false);

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> {
                    GoodsReceiptEntity saved = invocation.getArgument(0);
                    saved.setGoodsReceiptId(1L);

                    // 초기값 설정 검증
                    assertThat(saved.getReceiptStatus()).isEqualTo("PENDING");
                    assertThat(saved.getIsActive()).isTrue();

                    return saved;
                });

        // Mock LOT creation
        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity lot = invocation.getArgument(0);
                    lot.setLotId(1L);
                    return lot;
                });

        // Mock inventory operations
        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        InventoryEntity mockInventory = new InventoryEntity();
        mockInventory.setInventoryId(1L);
        mockInventory.setAvailableQuantity(new BigDecimal("1000"));
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(goodsReceiptRepository.findByIdWithAllRelations(anyLong()))
                .thenAnswer(invocation -> Optional.of(testGoodsReceipt));

        // When
        GoodsReceiptEntity result = goodsReceiptService.createGoodsReceipt(testGoodsReceipt);

        // Then
        assertThat(result).isNotNull();
        verify(goodsReceiptRepository, times(1)).save(any(GoodsReceiptEntity.class));
    }

    @Test
    @DisplayName("입하 업데이트 - 성공 (PENDING 상태)")
    void testUpdateGoodsReceipt_Success() {
        // Given
        testGoodsReceipt.setGoodsReceiptId(1L);
        testGoodsReceipt.setReceiptStatus("PENDING");
        testGoodsReceipt.setRemarks("Updated remarks");

        when(goodsReceiptRepository.findById(1L))
                .thenReturn(Optional.of(testGoodsReceipt));

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(goodsReceiptRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testGoodsReceipt));

        // When
        GoodsReceiptEntity result = goodsReceiptService.updateGoodsReceipt(testGoodsReceipt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGoodsReceiptId()).isEqualTo(1L);
        verify(goodsReceiptRepository, times(1)).save(any(GoodsReceiptEntity.class));
        verify(goodsReceiptRepository, times(1)).findByIdWithAllRelations(anyLong());
    }

    @Test
    @DisplayName("입하 업데이트 - 실패 (PENDING 아님)")
    void testUpdateGoodsReceipt_Fail_NotPending() {
        // Given
        testGoodsReceipt.setGoodsReceiptId(1L);
        testGoodsReceipt.setReceiptStatus("COMPLETED");  // PENDING이 아님

        GoodsReceiptEntity existing = new GoodsReceiptEntity();
        existing.setGoodsReceiptId(1L);
        existing.setReceiptStatus("COMPLETED");

        when(goodsReceiptRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        // When & Then
        assertThatThrownBy(() -> goodsReceiptService.updateGoodsReceipt(testGoodsReceipt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update goods receipt in status")
                .hasMessageContaining("COMPLETED");

        verify(goodsReceiptRepository, never()).save(any(GoodsReceiptEntity.class));
    }

    @Test
    @DisplayName("입하 업데이트 - 실패 (존재하지 않음)")
    void testUpdateGoodsReceipt_Fail_NotFound() {
        // Given
        testGoodsReceipt.setGoodsReceiptId(999L);

        when(goodsReceiptRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> goodsReceiptService.updateGoodsReceipt(testGoodsReceipt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Goods receipt not found")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("입하 완료 - 성공")
    void testCompleteGoodsReceipt_Success() {
        // Given
        Long goodsReceiptId = 1L;
        Long completedByUserId = 1L;

        testGoodsReceipt.setGoodsReceiptId(goodsReceiptId);
        testGoodsReceipt.setReceiptStatus("PENDING");
        testGoodsReceiptItem.setInspectionStatus("NOT_REQUIRED");

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.of(testGoodsReceipt));

        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.of(new LotEntity()));

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GoodsReceiptEntity result = goodsReceiptService.completeGoodsReceipt(goodsReceiptId, completedByUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceiptStatus()).isEqualTo("COMPLETED");
        verify(goodsReceiptRepository, atLeastOnce()).save(any(GoodsReceiptEntity.class));
    }

    @Test
    @DisplayName("입하 완료 - 실패 (잘못된 상태)")
    void testCompleteGoodsReceipt_Fail_InvalidStatus() {
        // Given
        Long goodsReceiptId = 1L;
        Long completedByUserId = 1L;

        testGoodsReceipt.setGoodsReceiptId(goodsReceiptId);
        testGoodsReceipt.setReceiptStatus("COMPLETED");  // 이미 완료됨

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.of(testGoodsReceipt));

        // When & Then
        assertThatThrownBy(() -> goodsReceiptService.completeGoodsReceipt(goodsReceiptId, completedByUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete goods receipt in status")
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("입하 취소 - 성공")
    void testCancelGoodsReceipt_Success() {
        // Given
        Long goodsReceiptId = 1L;
        String cancelReason = "Wrong product received";

        testGoodsReceipt.setGoodsReceiptId(goodsReceiptId);
        testGoodsReceipt.setReceiptStatus("PENDING");

        LotEntity testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT-2026-001");
        testLot.setIsActive(true);

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.of(testGoodsReceipt));

        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.of(testLot));

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GoodsReceiptEntity result = goodsReceiptService.cancelGoodsReceipt(goodsReceiptId, cancelReason);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceiptStatus()).isEqualTo("CANCELLED");
        verify(lotRepository, times(1)).save(any(LotEntity.class));
        verify(goodsReceiptRepository, atLeastOnce()).save(any(GoodsReceiptEntity.class));
    }

    @Test
    @DisplayName("입하 취소 - 실패 (이미 취소됨)")
    void testCancelGoodsReceipt_Fail_AlreadyCancelled() {
        // Given
        Long goodsReceiptId = 1L;
        String cancelReason = "Duplicate cancel";

        testGoodsReceipt.setGoodsReceiptId(goodsReceiptId);
        testGoodsReceipt.setReceiptStatus("CANCELLED");  // 이미 취소됨

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.of(testGoodsReceipt));

        // When & Then
        assertThatThrownBy(() -> goodsReceiptService.cancelGoodsReceipt(goodsReceiptId, cancelReason))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");
    }

    // ================== IQC 통합 테스트 ==================

    @Test
    @DisplayName("IQC 검사 의뢰 생성 - 성공")
    void testCreateGoodsReceipt_WithIQCRequest_Success() {
        // Given
        testGoodsReceiptItem.setInspectionStatus("PENDING");  // 검사 필요

        // Quality Standard mock
        QualityStandardEntity qualityStandard = new QualityStandardEntity();
        qualityStandard.setQualityStandardId(1L);
        qualityStandard.setInspectionType("INCOMING");
        qualityStandard.setIsActive(true);

        when(goodsReceiptRepository.existsByTenant_TenantIdAndReceiptNo(anyString(), anyString()))
                .thenReturn(false);

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> {
                    GoodsReceiptEntity gr = invocation.getArgument(0);
                    gr.setGoodsReceiptId(1L);
                    return gr;
                });

        when(goodsReceiptRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testGoodsReceipt));

        when(goodsReceiptRepository.findByTenantIdWithAllRelations(anyString()))
                .thenReturn(Arrays.asList());

        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.empty());

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity lot = invocation.getArgument(0);
                    lot.setLotId(1L);
                    return lot;
                });

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // QMS Mock - Quality Standard
        when(qualityStandardRepository.findByTenantAndProduct(any(TenantEntity.class), any(ProductEntity.class)))
                .thenReturn(Arrays.asList(qualityStandard));

        // QMS Mock - Quality Inspection
        when(qualityInspectionRepository.save(any(QualityInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    QualityInspectionEntity inspection = invocation.getArgument(0);
                    inspection.setQualityInspectionId(1L);
                    return inspection;
                });

        when(qualityInspectionRepository.findByTenant_TenantId(anyString()))
                .thenReturn(Arrays.asList());

        // When
        GoodsReceiptEntity result = goodsReceiptService.createGoodsReceipt(testGoodsReceipt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceiptStatus()).isEqualTo("INSPECTING");  // IQC 요청으로 상태 변경
        verify(qualityInspectionRepository, times(1)).save(any(QualityInspectionEntity.class));
        verify(qualityStandardRepository, times(1)).findByTenantAndProduct(any(TenantEntity.class), any(ProductEntity.class));
    }

    @Test
    @DisplayName("IQC 검사 의뢰 생성 - 품질 기준서 없음")
    void testCreateGoodsReceipt_WithIQCRequest_NoQualityStandard() {
        // Given
        testGoodsReceiptItem.setInspectionStatus("PENDING");  // 검사 필요

        when(goodsReceiptRepository.existsByTenant_TenantIdAndReceiptNo(anyString(), anyString()))
                .thenReturn(false);

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> {
                    GoodsReceiptEntity gr = invocation.getArgument(0);
                    gr.setGoodsReceiptId(1L);
                    return gr;
                });

        when(goodsReceiptRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testGoodsReceipt));

        when(goodsReceiptRepository.findByTenantIdWithAllRelations(anyString()))
                .thenReturn(Arrays.asList());

        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.empty());

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity lot = invocation.getArgument(0);
                    lot.setLotId(1L);
                    return lot;
                });

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // QMS Mock - 품질 기준서 없음
        when(qualityStandardRepository.findByTenantAndProduct(any(TenantEntity.class), any(ProductEntity.class)))
                .thenReturn(Arrays.asList());

        // When
        GoodsReceiptEntity result = goodsReceiptService.createGoodsReceipt(testGoodsReceipt);

        // Then
        assertThat(result).isNotNull();
        verify(qualityInspectionRepository, never()).save(any(QualityInspectionEntity.class));  // IQC 요청 생성 안됨
    }

    @Test
    @DisplayName("불합격품 격리 창고 이동 - 성공")
    void testCompleteGoodsReceipt_MoveToQuarantine_Success() {
        // Given
        Long goodsReceiptId = 1L;
        Long completedByUserId = 1L;

        testGoodsReceipt.setGoodsReceiptId(goodsReceiptId);
        testGoodsReceipt.setReceiptStatus("INSPECTING");
        testGoodsReceiptItem.setInspectionStatus("FAIL");  // 불합격

        // 격리 창고 준비
        WarehouseEntity quarantineWarehouse = new WarehouseEntity();
        quarantineWarehouse.setWarehouseId(99L);
        quarantineWarehouse.setWarehouseCode("WH-QUARANTINE");
        quarantineWarehouse.setWarehouseName("격리 창고");
        quarantineWarehouse.setWarehouseType("QUARANTINE");
        quarantineWarehouse.setTenant(testTenant);

        LotEntity testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT-2026-001");
        testLot.setProduct(testProduct);
        testLot.setQualityStatus("FAILED");

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.of(testGoodsReceipt));

        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.of(testLot));

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 격리 창고 조회
        when(warehouseRepository.findByTenant_TenantIdAndWarehouseType(anyString(), eq("QUARANTINE")))
                .thenReturn(Arrays.asList(quarantineWarehouse));

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), eq(99L), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GoodsReceiptEntity result = goodsReceiptService.completeGoodsReceipt(goodsReceiptId, completedByUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceiptStatus()).isEqualTo("COMPLETED");
        verify(warehouseRepository, times(1)).findByTenant_TenantIdAndWarehouseType(anyString(), eq("QUARANTINE"));
        verify(inventoryRepository, atLeastOnce()).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("불합격품 격리 창고 이동 - 격리 창고 없음")
    void testCompleteGoodsReceipt_MoveToQuarantine_NoQuarantineWarehouse() {
        // Given
        Long goodsReceiptId = 1L;
        Long completedByUserId = 1L;

        testGoodsReceipt.setGoodsReceiptId(goodsReceiptId);
        testGoodsReceipt.setReceiptStatus("INSPECTING");
        testGoodsReceiptItem.setInspectionStatus("FAIL");  // 불합격

        LotEntity testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT-2026-001");
        testLot.setProduct(testProduct);
        testLot.setQualityStatus("FAILED");

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.of(testGoodsReceipt));

        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.of(testLot));

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 격리 창고 없음
        when(warehouseRepository.findByTenant_TenantIdAndWarehouseType(anyString(), eq("QUARANTINE")))
                .thenReturn(Arrays.asList());

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GoodsReceiptEntity result = goodsReceiptService.completeGoodsReceipt(goodsReceiptId, completedByUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceiptStatus()).isEqualTo("COMPLETED");
        verify(warehouseRepository, times(1)).findByTenant_TenantIdAndWarehouseType(anyString(), eq("QUARANTINE"));
        // 격리 창고가 없으면 현재 창고에 유지됨
    }

    @Test
    @DisplayName("합격품 처리 - 성공")
    void testCompleteGoodsReceipt_PassedInspection_Success() {
        // Given
        Long goodsReceiptId = 1L;
        Long completedByUserId = 1L;

        testGoodsReceipt.setGoodsReceiptId(goodsReceiptId);
        testGoodsReceipt.setReceiptStatus("INSPECTING");
        testGoodsReceiptItem.setInspectionStatus("PASS");  // 합격

        LotEntity testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT-2026-001");
        testLot.setProduct(testProduct);
        testLot.setQualityStatus("PENDING");

        when(goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId))
                .thenReturn(Optional.of(testGoodsReceipt));

        when(lotRepository.findByTenant_TenantIdAndLotNo(anyString(), anyString()))
                .thenReturn(Optional.of(testLot));

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(goodsReceiptRepository.save(any(GoodsReceiptEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GoodsReceiptEntity result = goodsReceiptService.completeGoodsReceipt(goodsReceiptId, completedByUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceiptStatus()).isEqualTo("COMPLETED");
        verify(lotRepository, times(1)).save(argThat(lot ->
            "PASSED".equals(lot.getQualityStatus())
        ));
        verify(inventoryRepository, atLeastOnce()).save(any(InventoryEntity.class));
    }
}
