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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * InventoryTransactionService Unit Test
 *
 * 테스트 대상:
 * - 재고 트랜잭션 생성 (IN/OUT/MOVE/ADJUST)
 * - 재고 자동 업데이트
 * - 중복 트랜잭션 번호 검증
 * - 재고 부족 예외 처리
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryTransactionService 단위 테스트")
class InventoryTransactionServiceTest {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InventoryTransactionService inventoryTransactionService;

    private TenantEntity testTenant;
    private WarehouseEntity testWarehouse;
    private ProductEntity testProduct;
    private LotEntity testLot;
    private InventoryEntity testInventory;
    private InventoryTransactionEntity testTransaction;

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
        testWarehouse.setTenant(testTenant);

        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P-LCD-001");
        testProduct.setProductName("LCD 패널");
        testProduct.setUnit("EA");
        testProduct.setTenant(testTenant);

        testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT-2026-001");
        testLot.setProduct(testProduct);
        testLot.setInitialQuantity(new BigDecimal("1000"));
        testLot.setCurrentQuantity(new BigDecimal("1000"));
        testLot.setReservedQuantity(BigDecimal.ZERO);
        testLot.setQualityStatus("PASSED");
        testLot.setExpiryDate(LocalDate.now().plusYears(1));
        testLot.setTenant(testTenant);

        testInventory = new InventoryEntity();
        testInventory.setInventoryId(1L);
        testInventory.setTenant(testTenant);
        testInventory.setWarehouse(testWarehouse);
        testInventory.setProduct(testProduct);
        testInventory.setLot(testLot);
        testInventory.setAvailableQuantity(new BigDecimal("1000"));
        testInventory.setReservedQuantity(BigDecimal.ZERO);
        testInventory.setUnit("EA");

        testTransaction = new InventoryTransactionEntity();
        testTransaction.setTransactionId(1L);
        testTransaction.setTenant(testTenant);
        testTransaction.setTransactionNo("TXN-2026-001");
        testTransaction.setTransactionType("IN_RECEIVE");
        testTransaction.setTransactionDate(LocalDateTime.now());
        testTransaction.setWarehouse(testWarehouse);
        testTransaction.setProduct(testProduct);
        testTransaction.setLot(testLot);
        testTransaction.setQuantity(new BigDecimal("100"));
        testTransaction.setApprovalStatus("PENDING");
    }

    @Test
    @DisplayName("트랜잭션 생성 - 성공 (IN 타입)")
    void testCreateTransaction_Success_InType() {
        // Given
        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(
                any(TenantEntity.class), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    saved.setApprovalStatus("APPROVED");
                    saved.setApprovedDate(LocalDateTime.now());
                    return saved;
                });

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.createTransaction(testTransaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionNo()).isEqualTo("TXN-2026-001");
        assertThat(result.getTransactionType()).isEqualTo("IN_RECEIVE");

        // 재고 업데이트 검증
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
        verify(lotRepository, times(1)).save(any(LotEntity.class));
    }

    @Test
    @DisplayName("트랜잭션 생성 - 실패 (중복 트랜잭션 번호)")
    void testCreateTransaction_Fail_DuplicateTransactionNo() {
        // Given
        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(
                any(TenantEntity.class), anyString()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> inventoryTransactionService.createTransaction(testTransaction))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction number already exists")
                .hasMessageContaining("TXN-2026-001");

        // 저장이 호출되지 않아야 함
        verify(inventoryTransactionRepository, never()).save(any(InventoryTransactionEntity.class));
    }

    @Test
    @DisplayName("트랜잭션 조회 - 테넌트별 조회 성공")
    void testFindByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<InventoryTransactionEntity> expectedTransactions = Arrays.asList(testTransaction);

        when(inventoryTransactionRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedTransactions);

        // When
        List<InventoryTransactionEntity> result = inventoryTransactionService.findByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionNo()).isEqualTo("TXN-2026-001");
        verify(inventoryTransactionRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("트랜잭션 조회 - 날짜 범위별 조회")
    void testFindByDateRange_Success() {
        // Given
        String tenantId = "TEST001";
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<InventoryTransactionEntity> expectedTransactions = Arrays.asList(testTransaction);

        when(inventoryTransactionRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate))
                .thenReturn(expectedTransactions);

        // When
        List<InventoryTransactionEntity> result =
                inventoryTransactionService.findByDateRange(tenantId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(inventoryTransactionRepository, times(1))
                .findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Test
    @DisplayName("트랜잭션 조회 - 승인 상태별 조회")
    void testFindByApprovalStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String approvalStatus = "PENDING";
        List<InventoryTransactionEntity> expectedTransactions = Arrays.asList(testTransaction);

        when(inventoryTransactionRepository.findByTenant_TenantIdAndApprovalStatus(tenantId, approvalStatus))
                .thenReturn(expectedTransactions);

        // When
        List<InventoryTransactionEntity> result =
                inventoryTransactionService.findByApprovalStatus(tenantId, approvalStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApprovalStatus()).isEqualTo("PENDING");
        verify(inventoryTransactionRepository, times(1))
                .findByTenant_TenantIdAndApprovalStatus(tenantId, approvalStatus);
    }

    @Test
    @DisplayName("트랜잭션 조회 - ID로 조회 성공")
    void testFindById_Success() {
        // Given
        Long transactionId = 1L;
        when(inventoryTransactionRepository.findByIdWithAllRelations(transactionId))
                .thenReturn(Optional.of(testTransaction));

        // When
        Optional<InventoryTransactionEntity> result = inventoryTransactionService.findById(transactionId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo(transactionId);
        verify(inventoryTransactionRepository, times(1)).findByIdWithAllRelations(transactionId);
    }

    @Test
    @DisplayName("재고 증가 - 성공 (IN_RECEIVE 타입)")
    void testCreateTransaction_IncreasesInventory() {
        // Given
        testTransaction.setTransactionType("IN_RECEIVE");
        testTransaction.setQuantity(new BigDecimal("200"));

        BigDecimal initialInventory = new BigDecimal("1000");
        testInventory.setAvailableQuantity(initialInventory);

        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(any(), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    saved.setApprovalStatus("APPROVED");
                    return saved;
                });

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryEntity saved = invocation.getArgument(0);
                    // 재고가 증가했는지 검증
                    assertThat(saved.getAvailableQuantity())
                            .isEqualByComparingTo(initialInventory.add(new BigDecimal("200")));
                    return saved;
                });

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.createTransaction(testTransaction);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
        verify(lotRepository, times(1)).save(any(LotEntity.class));
    }

    @Test
    @DisplayName("재고 감소 - 성공 (OUT_ISSUE 타입)")
    void testCreateTransaction_DecreasesInventory() {
        // Given
        testTransaction.setTransactionType("OUT_ISSUE");
        testTransaction.setQuantity(new BigDecimal("200"));

        BigDecimal initialInventory = new BigDecimal("1000");
        testInventory.setAvailableQuantity(initialInventory);

        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(any(), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    saved.setApprovalStatus("APPROVED");
                    return saved;
                });

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryEntity saved = invocation.getArgument(0);
                    // 재고가 감소했는지 검증
                    assertThat(saved.getAvailableQuantity())
                            .isEqualByComparingTo(initialInventory.subtract(new BigDecimal("200")));
                    return saved;
                });

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.createTransaction(testTransaction);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
        verify(lotRepository, times(1)).save(any(LotEntity.class));
    }

    @Test
    @DisplayName("재고 감소 - 실패 (재고 부족)")
    void testCreateTransaction_Fail_InsufficientInventory() {
        // Given
        testTransaction.setTransactionType("OUT_ISSUE");
        testTransaction.setQuantity(new BigDecimal("1500"));  // 현재 재고(1000)보다 많음

        testInventory.setAvailableQuantity(new BigDecimal("1000"));

        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(any(), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    saved.setApprovalStatus("APPROVED");
                    return saved;
                });

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        // When & Then
        assertThatThrownBy(() -> inventoryTransactionService.createTransaction(testTransaction))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient inventory");

        // LOT 저장은 호출되지 않아야 함
        verify(lotRepository, never()).save(any(LotEntity.class));
    }

    @Test
    @DisplayName("재고 조정 - 성공 (ADJUST 타입)")
    void testCreateTransaction_AdjustInventory() {
        // Given
        testTransaction.setTransactionType("ADJUST");
        testTransaction.setQuantity(new BigDecimal("500"));  // 새로운 수량

        testInventory.setAvailableQuantity(new BigDecimal("1000"));

        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(any(), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    saved.setApprovalStatus("APPROVED");
                    return saved;
                });

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryEntity saved = invocation.getArgument(0);
                    // 정확히 500으로 조정되었는지 검증
                    assertThat(saved.getAvailableQuantity())
                            .isEqualByComparingTo(new BigDecimal("500"));
                    return saved;
                });

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.createTransaction(testTransaction);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));
        // ADJUST 타입은 LOT 수량을 업데이트하지 않음
        verify(lotRepository, never()).save(any(LotEntity.class));
    }

    // ================== 창고 간 이동 테스트 ==================

    @Test
    @DisplayName("창고 간 이동 - 성공 (MOVE 타입)")
    void testCreateTransaction_MoveInventory_Success() {
        // Given
        WarehouseEntity toWarehouse = new WarehouseEntity();
        toWarehouse.setWarehouseId(2L);
        toWarehouse.setWarehouseCode("WH-FIN");
        toWarehouse.setWarehouseName("완제품 창고");
        toWarehouse.setTenant(testTenant);

        testTransaction.setTransactionType("MOVE");
        testTransaction.setQuantity(new BigDecimal("100"));
        testTransaction.setFromWarehouse(testWarehouse);
        testTransaction.setToWarehouse(toWarehouse);

        // 출발 창고 재고
        InventoryEntity fromInventory = new InventoryEntity();
        fromInventory.setInventoryId(1L);
        fromInventory.setTenant(testTenant);
        fromInventory.setWarehouse(testWarehouse);
        fromInventory.setProduct(testProduct);
        fromInventory.setLot(testLot);
        fromInventory.setAvailableQuantity(new BigDecimal("500"));

        // 도착 창고 재고
        InventoryEntity toInventory = new InventoryEntity();
        toInventory.setInventoryId(2L);
        toInventory.setTenant(testTenant);
        toInventory.setWarehouse(toWarehouse);
        toInventory.setProduct(testProduct);
        toInventory.setLot(testLot);
        toInventory.setAvailableQuantity(new BigDecimal("200"));

        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(any(), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    saved.setApprovalStatus("APPROVED");
                    return saved;
                });

        // 출발 창고 재고 조회 (감소용)
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                eq("TEST001"), eq(1L), eq(1L), eq(1L)))
                .thenReturn(Optional.of(fromInventory));

        // 도착 창고 재고 조회 (증가용)
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                eq("TEST001"), eq(2L), eq(1L), eq(1L)))
                .thenReturn(Optional.of(toInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.createTransaction(testTransaction);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository, times(2)).save(any(InventoryEntity.class));  // 출발/도착 각 1번씩
        verify(lotRepository, times(1)).save(any(LotEntity.class));  // LOT 수량 감소 1번
    }

    @Test
    @DisplayName("창고 간 이동 - 실패 (재고 부족)")
    void testCreateTransaction_MoveInventory_InsufficientStock() {
        // Given
        WarehouseEntity toWarehouse = new WarehouseEntity();
        toWarehouse.setWarehouseId(2L);
        toWarehouse.setWarehouseCode("WH-FIN");
        toWarehouse.setWarehouseName("완제품 창고");
        toWarehouse.setTenant(testTenant);

        testTransaction.setTransactionType("MOVE");
        testTransaction.setQuantity(new BigDecimal("1000"));  // 재고보다 많음
        testTransaction.setFromWarehouse(testWarehouse);
        testTransaction.setToWarehouse(toWarehouse);

        // 출발 창고 재고 (부족)
        InventoryEntity fromInventory = new InventoryEntity();
        fromInventory.setInventoryId(1L);
        fromInventory.setTenant(testTenant);
        fromInventory.setWarehouse(testWarehouse);
        fromInventory.setProduct(testProduct);
        fromInventory.setLot(testLot);
        fromInventory.setAvailableQuantity(new BigDecimal("100"));  // 부족

        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(any(), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    saved.setApprovalStatus("APPROVED");
                    return saved;
                });

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                eq("TEST001"), eq(1L), eq(1L), eq(1L)))
                .thenReturn(Optional.of(fromInventory));

        // When & Then
        assertThatThrownBy(() -> inventoryTransactionService.createTransaction(testTransaction))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient inventory");

        verify(inventoryRepository, never()).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("창고 간 이동 - 도착 창고에 신규 재고 생성")
    void testCreateTransaction_MoveInventory_CreateNewInventory() {
        // Given
        WarehouseEntity toWarehouse = new WarehouseEntity();
        toWarehouse.setWarehouseId(2L);
        toWarehouse.setWarehouseCode("WH-FIN");
        toWarehouse.setWarehouseName("완제품 창고");
        toWarehouse.setTenant(testTenant);

        testTransaction.setTransactionType("MOVE");
        testTransaction.setQuantity(new BigDecimal("50"));
        testTransaction.setFromWarehouse(testWarehouse);
        testTransaction.setToWarehouse(toWarehouse);

        // 출발 창고 재고
        InventoryEntity fromInventory = new InventoryEntity();
        fromInventory.setInventoryId(1L);
        fromInventory.setTenant(testTenant);
        fromInventory.setWarehouse(testWarehouse);
        fromInventory.setProduct(testProduct);
        fromInventory.setLot(testLot);
        fromInventory.setAvailableQuantity(new BigDecimal("200"));

        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(any(), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    saved.setApprovalStatus("APPROVED");
                    return saved;
                });

        // 출발 창고 재고 조회
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                eq("TEST001"), eq(1L), eq(1L), eq(1L)))
                .thenReturn(Optional.of(fromInventory));

        // 도착 창고 재고 없음 (신규 생성)
        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                eq("TEST001"), eq(2L), eq(1L), eq(1L)))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryEntity saved = invocation.getArgument(0);
                    if (saved.getInventoryId() == null) {
                        saved.setInventoryId(2L);  // 신규 생성
                    }
                    return saved;
                });

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.createTransaction(testTransaction);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository, times(3)).save(any(InventoryEntity.class));  // 출발 감소 + 도착 신규 생성 + 도착 수량 업데이트
        verify(lotRepository, times(1)).save(any(LotEntity.class));
    }

    // ================== 승인 워크플로우 테스트 ==================

    @Test
    @DisplayName("트랜잭션 승인 - 성공")
    void testApproveTransaction_Success() {
        // Given
        Long transactionId = 1L;
        Long approverId = 100L;

        testTransaction.setApprovalStatus("PENDING");

        UserEntity approver = new UserEntity();
        approver.setUserId(approverId);
        approver.setUsername("approver01");
        approver.setTenant(testTenant);

        when(inventoryTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(testTransaction));

        when(userRepository.findById(approverId))
                .thenReturn(Optional.of(approver));

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getApprovalStatus()).isEqualTo("APPROVED");
                    assertThat(saved.getApprovedBy()).isEqualTo(approver);
                    assertThat(saved.getApprovedDate()).isNotNull();
                    return saved;
                });

        when(inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(testInventory));

        when(inventoryRepository.save(any(InventoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.approveTransaction(transactionId, approverId);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryTransactionRepository, times(1)).findById(transactionId);
        verify(userRepository, times(1)).findById(approverId);
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransactionEntity.class));
        verify(inventoryRepository, times(1)).save(any(InventoryEntity.class));  // 재고 업데이트
    }

    @Test
    @DisplayName("트랜잭션 승인 - 실패 (트랜잭션 없음)")
    void testApproveTransaction_Fail_TransactionNotFound() {
        // Given
        Long transactionId = 999L;
        Long approverId = 100L;

        when(inventoryTransactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryTransactionService.approveTransaction(transactionId, approverId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction not found")
                .hasMessageContaining("999");

        verify(inventoryTransactionRepository, times(1)).findById(transactionId);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("트랜잭션 승인 - 실패 (PENDING 상태가 아님)")
    void testApproveTransaction_Fail_InvalidStatus() {
        // Given
        Long transactionId = 1L;
        Long approverId = 100L;

        testTransaction.setApprovalStatus("APPROVED");  // 이미 승인됨

        when(inventoryTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(testTransaction));

        // When & Then
        assertThatThrownBy(() -> inventoryTransactionService.approveTransaction(transactionId, approverId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot approve transaction in status")
                .hasMessageContaining("APPROVED");

        verify(inventoryTransactionRepository, times(1)).findById(transactionId);
        verify(userRepository, never()).findById(anyLong());
        verify(inventoryTransactionRepository, never()).save(any(InventoryTransactionEntity.class));
    }

    @Test
    @DisplayName("트랜잭션 승인 - 실패 (승인자 없음)")
    void testApproveTransaction_Fail_ApproverNotFound() {
        // Given
        Long transactionId = 1L;
        Long approverId = 999L;

        testTransaction.setApprovalStatus("PENDING");

        when(inventoryTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(testTransaction));

        when(userRepository.findById(approverId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryTransactionService.approveTransaction(transactionId, approverId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining("999");

        verify(inventoryTransactionRepository, times(1)).findById(transactionId);
        verify(userRepository, times(1)).findById(approverId);
        verify(inventoryTransactionRepository, never()).save(any(InventoryTransactionEntity.class));
    }

    @Test
    @DisplayName("트랜잭션 거부 - 성공")
    void testRejectTransaction_Success() {
        // Given
        Long transactionId = 1L;
        Long approverId = 100L;
        String reason = "재고 수량 확인 필요";

        testTransaction.setApprovalStatus("PENDING");
        testTransaction.setRemarks("Initial remark");

        UserEntity approver = new UserEntity();
        approver.setUserId(approverId);
        approver.setUsername("approver01");
        approver.setTenant(testTenant);

        when(inventoryTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(testTransaction));

        when(userRepository.findById(approverId))
                .thenReturn(Optional.of(approver));

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getApprovalStatus()).isEqualTo("REJECTED");
                    assertThat(saved.getApprovedBy()).isEqualTo(approver);
                    assertThat(saved.getApprovedDate()).isNotNull();
                    assertThat(saved.getRemarks()).contains("Rejected:");
                    assertThat(saved.getRemarks()).contains(reason);
                    return saved;
                });

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.rejectTransaction(
                transactionId, approverId, reason);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryTransactionRepository, times(1)).findById(transactionId);
        verify(userRepository, times(1)).findById(approverId);
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransactionEntity.class));
        // 거부 시에는 재고 업데이트 없음
        verify(inventoryRepository, never()).save(any(InventoryEntity.class));
    }

    @Test
    @DisplayName("트랜잭션 거부 - 실패 (PENDING 상태가 아님)")
    void testRejectTransaction_Fail_InvalidStatus() {
        // Given
        Long transactionId = 1L;
        Long approverId = 100L;
        String reason = "재고 수량 확인 필요";

        testTransaction.setApprovalStatus("REJECTED");  // 이미 거부됨

        when(inventoryTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(testTransaction));

        // When & Then
        assertThatThrownBy(() -> inventoryTransactionService.rejectTransaction(
                transactionId, approverId, reason))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot reject transaction in status")
                .hasMessageContaining("REJECTED");

        verify(inventoryTransactionRepository, times(1)).findById(transactionId);
        verify(userRepository, never()).findById(anyLong());
        verify(inventoryTransactionRepository, never()).save(any(InventoryTransactionEntity.class));
    }

    @Test
    @DisplayName("PENDING 상태로 트랜잭션 생성 - 성공")
    void testCreateTransactionPending_Success() {
        // Given
        testTransaction.setTransactionType("ADJUST");  // 조정은 승인 필요
        testTransaction.setQuantity(new BigDecimal("500"));

        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(
                any(TenantEntity.class), anyString()))
                .thenReturn(false);

        when(inventoryTransactionRepository.save(any(InventoryTransactionEntity.class)))
                .thenAnswer(invocation -> {
                    InventoryTransactionEntity saved = invocation.getArgument(0);
                    saved.setTransactionId(1L);
                    assertThat(saved.getApprovalStatus()).isEqualTo("PENDING");
                    return saved;
                });

        when(inventoryTransactionRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testTransaction));

        // When
        InventoryTransactionEntity result = inventoryTransactionService.createTransactionPending(testTransaction);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransactionEntity.class));
        // PENDING 상태로 생성 시 재고 업데이트 없음
        verify(inventoryRepository, never()).save(any(InventoryEntity.class));
        verify(lotRepository, never()).save(any(LotEntity.class));
    }

    @Test
    @DisplayName("PENDING 상태로 트랜잭션 생성 - 실패 (중복 번호)")
    void testCreateTransactionPending_Fail_DuplicateTransactionNo() {
        // Given
        when(inventoryTransactionRepository.existsByTenantAndTransactionNo(
                any(TenantEntity.class), anyString()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> inventoryTransactionService.createTransactionPending(testTransaction))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction number already exists")
                .hasMessageContaining("TXN-2026-001");

        verify(inventoryTransactionRepository, never()).save(any(InventoryTransactionEntity.class));
    }
}
