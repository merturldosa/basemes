package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Mold Production History Service Test
 */
@ExtendWith(MockitoExtension.class)
class MoldProductionHistoryServiceTest {

    @Mock
    private MoldProductionHistoryRepository historyRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private MoldRepository moldRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkResultRepository workResultRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MoldProductionHistoryService historyService;

    private TenantEntity testTenant;
    private MoldEntity testMold;
    private WorkOrderEntity testWorkOrder;
    private WorkResultEntity testWorkResult;
    private UserEntity testOperator;
    private MoldProductionHistoryEntity testHistory;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("Test Tenant");

        // 테스트 금형
        testMold = new MoldEntity();
        testMold.setMoldId(1L);
        testMold.setMoldCode("MOLD-001");
        testMold.setMoldName("Test Mold");
        testMold.setCurrentShotCount(100000L);

        // 테스트 작업지시
        testWorkOrder = new WorkOrderEntity();
        testWorkOrder.setWorkOrderId(1L);
        testWorkOrder.setWorkOrderNo("WO-2026-001");

        // 테스트 작업결과
        testWorkResult = new WorkResultEntity();
        testWorkResult.setWorkResultId(1L);

        // 테스트 작업자
        testOperator = new UserEntity();
        testOperator.setUserId(1L);
        testOperator.setUsername("operator01");
        testOperator.setFullName("작업자 이름");

        // 테스트 생산 이력
        testHistory = new MoldProductionHistoryEntity();
        testHistory.setHistoryId(1L);
        testHistory.setProductionDate(LocalDate.now());
        testHistory.setShotCount(1000);
        testHistory.setCumulativeShotCount(101000L);
        testHistory.setProductionQuantity(new BigDecimal("4000"));
        testHistory.setGoodQuantity(new BigDecimal("3950"));
        testHistory.setDefectQuantity(new BigDecimal("50"));
        testHistory.setTenant(testTenant);
        testHistory.setMold(testMold);
        testHistory.setWorkOrder(testWorkOrder);
        testHistory.setWorkResult(testWorkResult);
        testHistory.setOperatorUser(testOperator);
        testHistory.setOperatorName("작업자 이름");
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("생산 이력 조회 - 전체 조회 성공")
    void testGetAllHistories_Success() {
        // Given
        String tenantId = "TEST001";
        List<MoldProductionHistoryEntity> expectedList = Arrays.asList(testHistory);

        when(historyRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<MoldProductionHistoryEntity> result = historyService.getAllHistories(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(historyRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("생산 이력 조회 - ID로 조회 성공")
    void testGetHistoryById_Success() {
        // Given
        Long historyId = 1L;

        when(historyRepository.findByIdWithAllRelations(historyId))
                .thenReturn(Optional.of(testHistory));

        // When
        MoldProductionHistoryEntity result = historyService.getHistoryById(historyId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHistoryId()).isEqualTo(historyId);
        verify(historyRepository, times(1)).findByIdWithAllRelations(historyId);
    }

    @Test
    @DisplayName("생산 이력 조회 - ID로 조회 실패 (없음)")
    void testGetHistoryById_Fail_NotFound() {
        // Given
        Long historyId = 999L;

        when(historyRepository.findByIdWithAllRelations(historyId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> historyService.getHistoryById(historyId))
                .isInstanceOf(BusinessException.class);
        verify(historyRepository, times(1)).findByIdWithAllRelations(historyId);
    }

    @Test
    @DisplayName("생산 이력 조회 - 금형별 조회 성공")
    void testGetHistoriesByMold_Success() {
        // Given
        Long moldId = 1L;
        List<MoldProductionHistoryEntity> expectedList = Arrays.asList(testHistory);

        when(historyRepository.findByMoldId(moldId))
                .thenReturn(expectedList);

        // When
        List<MoldProductionHistoryEntity> result = historyService.getHistoriesByMold(moldId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(historyRepository, times(1)).findByMoldId(moldId);
    }

    @Test
    @DisplayName("생산 이력 조회 - 기간별 조회 성공")
    void testGetHistoriesByDateRange_Success() {
        // Given
        String tenantId = "TEST001";
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<MoldProductionHistoryEntity> expectedList = Arrays.asList(testHistory);

        when(historyRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate))
                .thenReturn(expectedList);

        // When
        List<MoldProductionHistoryEntity> result = historyService.getHistoriesByDateRange(tenantId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(historyRepository, times(1)).findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Test
    @DisplayName("생산 이력 조회 - 작업지시별 조회 성공")
    void testGetHistoriesByWorkOrder_Success() {
        // Given
        Long workOrderId = 1L;
        List<MoldProductionHistoryEntity> expectedList = Arrays.asList(testHistory);

        when(historyRepository.findByWorkOrderId(workOrderId))
                .thenReturn(expectedList);

        // When
        List<MoldProductionHistoryEntity> result = historyService.getHistoriesByWorkOrder(workOrderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(historyRepository, times(1)).findByWorkOrderId(workOrderId);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("생산 이력 생성 - 성공 (전체 정보)")
    void testCreateHistory_Success_FullInfo() {
        // Given
        String tenantId = "TEST001";
        MoldProductionHistoryEntity newHistory = new MoldProductionHistoryEntity();
        newHistory.setProductionDate(LocalDate.now());
        newHistory.setShotCount(500);
        newHistory.setProductionQuantity(new BigDecimal("2000"));

        MoldEntity mold = new MoldEntity();
        mold.setMoldId(1L);
        newHistory.setMold(mold);

        WorkOrderEntity workOrder = new WorkOrderEntity();
        workOrder.setWorkOrderId(1L);
        newHistory.setWorkOrder(workOrder);

        WorkResultEntity workResult = new WorkResultEntity();
        workResult.setWorkResultId(1L);
        newHistory.setWorkResult(workResult);

        UserEntity operator = new UserEntity();
        operator.setUserId(1L);
        newHistory.setOperatorUser(operator);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(moldRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testMold));
        when(workOrderRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testWorkOrder));
        when(workResultRepository.findById(1L))
                .thenReturn(Optional.of(testWorkResult));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testOperator));
        when(historyRepository.save(any(MoldProductionHistoryEntity.class)))
                .thenAnswer(invocation -> {
                    MoldProductionHistoryEntity saved = invocation.getArgument(0);
                    saved.setHistoryId(2L);
                    assertThat(saved.getOperatorName()).isEqualTo("작업자 이름");
                    return saved;
                });

        // When
        MoldProductionHistoryEntity result = historyService.createHistory(tenantId, newHistory);

        // Then
        assertThat(result).isNotNull();
        verify(historyRepository, times(1)).save(any(MoldProductionHistoryEntity.class));
    }

    @Test
    @DisplayName("생산 이력 생성 - 성공 (최소 정보)")
    void testCreateHistory_Success_MinimalInfo() {
        // Given
        String tenantId = "TEST001";
        MoldProductionHistoryEntity newHistory = new MoldProductionHistoryEntity();
        newHistory.setProductionDate(LocalDate.now());
        newHistory.setShotCount(300);

        MoldEntity mold = new MoldEntity();
        mold.setMoldId(1L);
        newHistory.setMold(mold);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(moldRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testMold));
        when(historyRepository.save(any(MoldProductionHistoryEntity.class)))
                .thenAnswer(invocation -> {
                    MoldProductionHistoryEntity saved = invocation.getArgument(0);
                    saved.setHistoryId(3L);
                    return saved;
                });

        // When
        MoldProductionHistoryEntity result = historyService.createHistory(tenantId, newHistory);

        // Then
        assertThat(result).isNotNull();
        verify(historyRepository, times(1)).save(any(MoldProductionHistoryEntity.class));
        verify(workOrderRepository, never()).findByIdWithAllRelations(any());
        verify(workResultRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("생산 이력 생성 - 실패 (테넌트 없음)")
    void testCreateHistory_Fail_TenantNotFound() {
        // Given
        String tenantId = "TEST999";
        MoldProductionHistoryEntity newHistory = new MoldProductionHistoryEntity();

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> historyService.createHistory(tenantId, newHistory))
                .isInstanceOf(BusinessException.class);
        verify(historyRepository, never()).save(any(MoldProductionHistoryEntity.class));
    }

    @Test
    @DisplayName("생산 이력 생성 - 실패 (금형 없음)")
    void testCreateHistory_Fail_MoldNotFound() {
        // Given
        String tenantId = "TEST001";
        MoldProductionHistoryEntity newHistory = new MoldProductionHistoryEntity();

        MoldEntity mold = new MoldEntity();
        mold.setMoldId(999L);
        newHistory.setMold(mold);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(moldRepository.findByIdWithAllRelations(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> historyService.createHistory(tenantId, newHistory))
                .isInstanceOf(BusinessException.class);
        verify(historyRepository, never()).save(any(MoldProductionHistoryEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("생산 이력 수정 - 성공")
    void testUpdateHistory_Success() {
        // Given
        Long historyId = 1L;
        MoldProductionHistoryEntity updateData = new MoldProductionHistoryEntity();
        updateData.setProductionQuantity(new BigDecimal("4100"));
        updateData.setGoodQuantity(new BigDecimal("4050"));
        updateData.setDefectQuantity(new BigDecimal("50"));
        updateData.setRemarks("Updated remarks");

        when(historyRepository.findByIdWithAllRelations(historyId))
                .thenReturn(Optional.of(testHistory));
        when(historyRepository.save(any(MoldProductionHistoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MoldProductionHistoryEntity result = historyService.updateHistory(historyId, updateData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductionQuantity()).isEqualByComparingTo(new BigDecimal("4100"));
        assertThat(result.getGoodQuantity()).isEqualByComparingTo(new BigDecimal("4050"));
        assertThat(result.getDefectQuantity()).isEqualByComparingTo(new BigDecimal("50"));
        assertThat(result.getRemarks()).isEqualTo("Updated remarks");
        verify(historyRepository, times(1)).save(any(MoldProductionHistoryEntity.class));
    }

    @Test
    @DisplayName("생산 이력 수정 - 실패 (없음)")
    void testUpdateHistory_Fail_NotFound() {
        // Given
        Long historyId = 999L;
        MoldProductionHistoryEntity updateData = new MoldProductionHistoryEntity();

        when(historyRepository.findByIdWithAllRelations(historyId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> historyService.updateHistory(historyId, updateData))
                .isInstanceOf(BusinessException.class);
        verify(historyRepository, never()).save(any(MoldProductionHistoryEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("생산 이력 삭제 - 성공")
    void testDeleteHistory_Success() {
        // Given
        Long historyId = 1L;

        when(historyRepository.findById(historyId))
                .thenReturn(Optional.of(testHistory));
        doNothing().when(historyRepository).delete(testHistory);

        // When
        historyService.deleteHistory(historyId);

        // Then
        verify(historyRepository, times(1)).findById(historyId);
        verify(historyRepository, times(1)).delete(testHistory);
    }

    @Test
    @DisplayName("생산 이력 삭제 - 실패 (없음)")
    void testDeleteHistory_Fail_NotFound() {
        // Given
        Long historyId = 999L;

        when(historyRepository.findById(historyId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> historyService.deleteHistory(historyId))
                .isInstanceOf(BusinessException.class);
        verify(historyRepository, never()).delete(any(MoldProductionHistoryEntity.class));
    }
}
