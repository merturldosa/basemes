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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EquipmentOperationService Unit Test
 *
 * 테스트 대상:
 * - 설비 가동 CRUD
 * - OEE 계산 로직
 * - 가동 완료 처리
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentOperationService 단위 테스트")
class EquipmentOperationServiceTest {

    @Mock
    private EquipmentOperationRepository operationRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkResultRepository workResultRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EquipmentOperationService operationService;

    private TenantEntity testTenant;
    private EquipmentEntity testEquipment;
    private UserEntity testUser;
    private EquipmentOperationEntity testOperation;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        // 테스트 설비 (표준 사이클 타임 설정)
        testEquipment = new EquipmentEntity();
        testEquipment.setEquipmentId(1L);
        testEquipment.setEquipmentCode("EQ-001");
        testEquipment.setEquipmentName("사출기 1호");
        testEquipment.setStandardCycleTime(new BigDecimal("10.0")); // 표준 사이클 타임 10초

        // 테스트 사용자
        testUser = new UserEntity();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setFullName("테스트 작업자");

        // 테스트 가동 이력
        testOperation = new EquipmentOperationEntity();
        testOperation.setOperationId(1L);
        testOperation.setStartTime(LocalDateTime.now().minusHours(8));
        testOperation.setEndTime(LocalDateTime.now());
        testOperation.setOperationDate(LocalDate.now());
        testOperation.setOperationHours(new BigDecimal("8.0"));
        testOperation.setProductionQuantity(new BigDecimal("1000"));
        testOperation.setGoodQuantity(new BigDecimal("950"));
        testOperation.setDefectQuantity(new BigDecimal("50"));
        testOperation.setStopDurationMinutes(60); // 1시간 정지
        testOperation.setCycleTime(new BigDecimal("12.0")); // 실제 사이클 타임 12초
        testOperation.setOperationStatus("RUNNING");
        testOperation.setTenant(testTenant);
        testOperation.setEquipment(testEquipment);
        testOperation.setOperatorUser(testUser);
        testOperation.setOperatorName(testUser.getFullName());
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("가동 이력 조회 - 전체 조회 성공")
    void testGetAllOperations_Success() {
        // Given
        String tenantId = "TEST001";
        List<EquipmentOperationEntity> expectedList = Arrays.asList(testOperation);

        when(operationRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<EquipmentOperationEntity> result = operationService.getAllOperations(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(operationRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("가동 이력 조회 - ID로 조회 성공")
    void testGetOperationById_Success() {
        // Given
        Long operationId = 1L;
        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.of(testOperation));

        // When
        EquipmentOperationEntity result = operationService.getOperationById(operationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOperationId()).isEqualTo(operationId);
        verify(operationRepository, times(1)).findByIdWithAllRelations(operationId);
    }

    @Test
    @DisplayName("가동 이력 조회 - ID로 조회 실패 (없음)")
    void testGetOperationById_Fail_NotFound() {
        // Given
        Long operationId = 999L;
        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> operationService.getOperationById(operationId))
                .isInstanceOf(BusinessException.class);

        verify(operationRepository, times(1)).findByIdWithAllRelations(operationId);
    }

    @Test
    @DisplayName("가동 이력 조회 - 설비별 조회 성공")
    void testGetOperationsByEquipment_Success() {
        // Given
        Long equipmentId = 1L;
        List<EquipmentOperationEntity> expectedList = Arrays.asList(testOperation);

        when(operationRepository.findByEquipmentId(equipmentId))
                .thenReturn(expectedList);

        // When
        List<EquipmentOperationEntity> result = operationService.getOperationsByEquipment(equipmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(operationRepository, times(1)).findByEquipmentId(equipmentId);
    }

    @Test
    @DisplayName("가동 이력 조회 - 날짜 범위 조회 성공")
    void testGetOperationsByDateRange_Success() {
        // Given
        String tenantId = "TEST001";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<EquipmentOperationEntity> expectedList = Arrays.asList(testOperation);

        when(operationRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate))
                .thenReturn(expectedList);

        // When
        List<EquipmentOperationEntity> result = operationService.getOperationsByDateRange(tenantId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(operationRepository, times(1)).findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Test
    @DisplayName("가동 이력 조회 - 상태별 조회 성공")
    void testGetOperationsByStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String status = "RUNNING";
        List<EquipmentOperationEntity> expectedList = Arrays.asList(testOperation);

        when(operationRepository.findByTenantIdAndOperationStatus(tenantId, status))
                .thenReturn(expectedList);

        // When
        List<EquipmentOperationEntity> result = operationService.getOperationsByStatus(tenantId, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOperationStatus()).isEqualTo(status);
        verify(operationRepository, times(1)).findByTenantIdAndOperationStatus(tenantId, status);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("가동 이력 생성 - 성공")
    void testCreateOperation_Success() {
        // Given
        String tenantId = "TEST001";

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(testEquipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(userRepository.findById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    assertThat(saved.getTenant()).isEqualTo(testTenant);
                    assertThat(saved.getEquipment()).isEqualTo(testEquipment);
                    assertThat(saved.getOperatorName()).isEqualTo(testUser.getFullName());
                    return saved;
                });

        // When
        EquipmentOperationEntity result = operationService.createOperation(tenantId, testOperation);

        // Then
        assertThat(result).isNotNull();
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    @Test
    @DisplayName("가동 이력 생성 - 성공 (기본값 설정)")
    void testCreateOperation_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        EquipmentOperationEntity minimalOperation = new EquipmentOperationEntity();
        minimalOperation.setStartTime(LocalDateTime.now());
        minimalOperation.setOperationDate(LocalDate.now());

        EquipmentEntity equipment = new EquipmentEntity();
        equipment.setEquipmentId(1L);
        minimalOperation.setEquipment(equipment);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(equipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    // 기본값 검증
                    assertThat(saved.getProductionQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getGoodQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getDefectQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getOperationStatus()).isEqualTo("RUNNING");
                    return saved;
                });

        // When
        EquipmentOperationEntity result = operationService.createOperation(tenantId, minimalOperation);

        // Then
        assertThat(result).isNotNull();
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    // ================== 가동 완료 및 OEE 계산 테스트 ==================

    @Test
    @DisplayName("가동 완료 - 성공 (OEE 자동 계산)")
    void testCompleteOperation_Success() {
        // Given
        Long operationId = 1L;
        testOperation.setEndTime(null); // 종료 시간 없음

        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.of(testOperation));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    assertThat(saved.getEndTime()).isNotNull();
                    assertThat(saved.getOperationStatus()).isEqualTo("COMPLETED");
                    assertThat(saved.getOee()).isNotNull();
                    assertThat(saved.getQualityRate()).isNotNull();
                    assertThat(saved.getUtilizationRate()).isNotNull();
                    assertThat(saved.getPerformanceRate()).isNotNull();
                    return saved;
                });

        // When
        EquipmentOperationEntity result = operationService.completeOperation(operationId);

        // Then
        assertThat(result).isNotNull();
        verify(operationRepository, times(1)).findByIdWithAllRelations(operationId);
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    @Test
    @DisplayName("OEE 계산 - Quality Rate 검증")
    void testCompleteOperation_QualityRate() {
        // Given
        Long operationId = 1L;
        testOperation.setProductionQuantity(new BigDecimal("1000"));
        testOperation.setGoodQuantity(new BigDecimal("950"));
        // Expected Quality Rate = 950/1000 * 100 = 95%

        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.of(testOperation));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    // Quality Rate should be 95%
                    assertThat(saved.getQualityRate()).isEqualByComparingTo(new BigDecimal("95.00"));
                    return saved;
                });

        // When
        operationService.completeOperation(operationId);

        // Then
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    @Test
    @DisplayName("OEE 계산 - Utilization Rate 검증")
    void testCompleteOperation_UtilizationRate() {
        // Given
        Long operationId = 1L;
        testOperation.setOperationHours(new BigDecimal("8.0"));
        testOperation.setStopDurationMinutes(60); // 1시간 = 60분
        // Expected Utilization Rate = (8 - 1) / 8 * 100 = 87.5%

        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.of(testOperation));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    // Utilization Rate should be 87.5%
                    assertThat(saved.getUtilizationRate()).isEqualByComparingTo(new BigDecimal("87.50"));
                    return saved;
                });

        // When
        operationService.completeOperation(operationId);

        // Then
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    @Test
    @DisplayName("OEE 계산 - Performance Rate 검증")
    void testCompleteOperation_PerformanceRate() {
        // Given
        Long operationId = 1L;
        testEquipment.setStandardCycleTime(new BigDecimal("10.0")); // 표준 10초
        testOperation.setCycleTime(new BigDecimal("12.0")); // 실제 12초
        // Expected Performance Rate = 10/12 * 100 = 83.33%

        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.of(testOperation));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    // Performance Rate should be 83.33%
                    assertThat(saved.getPerformanceRate()).isEqualByComparingTo(new BigDecimal("83.33"));
                    return saved;
                });

        // When
        operationService.completeOperation(operationId);

        // Then
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    @Test
    @DisplayName("OEE 계산 - 종합 OEE 검증")
    void testCompleteOperation_OverallOEE() {
        // Given
        Long operationId = 1L;
        testOperation.setOperationHours(new BigDecimal("8.0"));
        testOperation.setStopDurationMinutes(60);
        testOperation.setProductionQuantity(new BigDecimal("1000"));
        testOperation.setGoodQuantity(new BigDecimal("950"));
        testEquipment.setStandardCycleTime(new BigDecimal("10.0"));
        testOperation.setCycleTime(new BigDecimal("12.0"));
        // Utilization = 87.5%, Performance = 83.33%, Quality = 95%
        // Expected OEE = 87.5 * 83.33 * 95 / 10000 = 69.29%

        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.of(testOperation));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    // OEE should be around 69.29%
                    assertThat(saved.getOee()).isBetween(new BigDecimal("69.0"), new BigDecimal("70.0"));
                    return saved;
                });

        // When
        operationService.completeOperation(operationId);

        // Then
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("가동 이력 수정 - 성공")
    void testUpdateOperation_Success() {
        // Given
        Long operationId = 1L;
        EquipmentOperationEntity updateData = new EquipmentOperationEntity();
        updateData.setProductionQuantity(new BigDecimal("1200"));
        updateData.setGoodQuantity(new BigDecimal("1150"));
        updateData.setDefectQuantity(new BigDecimal("50"));

        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.of(testOperation));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    assertThat(saved.getProductionQuantity()).isEqualByComparingTo(new BigDecimal("1200"));
                    assertThat(saved.getGoodQuantity()).isEqualByComparingTo(new BigDecimal("1150"));
                    return saved;
                });

        // When
        EquipmentOperationEntity result = operationService.updateOperation(operationId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(operationRepository, times(1)).findByIdWithAllRelations(operationId);
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    @Test
    @DisplayName("가동 이력 수정 - 완료 상태일 때 OEE 재계산")
    void testUpdateOperation_RecalculateOEE() {
        // Given
        Long operationId = 1L;
        testOperation.setOperationStatus("COMPLETED"); // 완료 상태

        EquipmentOperationEntity updateData = new EquipmentOperationEntity();
        updateData.setGoodQuantity(new BigDecimal("980")); // 양품 수량 변경

        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.of(testOperation));

        when(operationRepository.save(any(EquipmentOperationEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentOperationEntity saved = invocation.getArgument(0);
                    // OEE가 재계산되어야 함
                    assertThat(saved.getOee()).isNotNull();
                    return saved;
                });

        // When
        operationService.updateOperation(operationId, updateData);

        // Then
        verify(operationRepository, times(1)).save(any(EquipmentOperationEntity.class));
    }

    @Test
    @DisplayName("가동 이력 수정 - 실패 (가동 이력 없음)")
    void testUpdateOperation_Fail_NotFound() {
        // Given
        Long operationId = 999L;
        EquipmentOperationEntity updateData = new EquipmentOperationEntity();

        when(operationRepository.findByIdWithAllRelations(operationId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> operationService.updateOperation(operationId, updateData))
                .isInstanceOf(BusinessException.class);

        verify(operationRepository, never()).save(any(EquipmentOperationEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("가동 이력 삭제 - 성공")
    void testDeleteOperation_Success() {
        // Given
        Long operationId = 1L;

        when(operationRepository.findById(operationId))
                .thenReturn(Optional.of(testOperation));

        doNothing().when(operationRepository).delete(testOperation);

        // When
        operationService.deleteOperation(operationId);

        // Then
        verify(operationRepository, times(1)).findById(operationId);
        verify(operationRepository, times(1)).delete(testOperation);
    }

    @Test
    @DisplayName("가동 이력 삭제 - 실패 (가동 이력 없음)")
    void testDeleteOperation_Fail_NotFound() {
        // Given
        Long operationId = 999L;

        when(operationRepository.findById(operationId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> operationService.deleteOperation(operationId))
                .isInstanceOf(BusinessException.class);

        verify(operationRepository, never()).delete(any(EquipmentOperationEntity.class));
    }
}
