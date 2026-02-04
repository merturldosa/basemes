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
 * QualityInspectionService Unit Test
 *
 * 테스트 대상:
 * - 품질 검사 생성 및 자동 판정 (PASS/FAIL/CONDITIONAL)
 * - 합격/불합격 수량 자동 계산
 * - 품질 기준 기반 측정값 검증
 * - 통계 및 합격률 계산
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QualityInspectionService 단위 테스트")
class QualityInspectionServiceTest {

    @Mock
    private QualityInspectionRepository qualityInspectionRepository;

    @Mock
    private QualityStandardRepository qualityStandardRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @InjectMocks
    private QualityInspectionService qualityInspectionService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private QualityStandardEntity testStandard;
    private QualityInspectionEntity testInspection;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        // 테스트 제품
        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P-TEST-001");
        testProduct.setProductName("테스트 제품");
        testProduct.setTenant(testTenant);

        // 테스트 품질 기준 (Min: 90, Max: 110, Tolerance: 5)
        testStandard = new QualityStandardEntity();
        testStandard.setQualityStandardId(1L);
        testStandard.setStandardCode("QS-2026-001");
        testStandard.setStandardName("테스트 품질 기준");
        testStandard.setInspectionType("INCOMING");
        testStandard.setMinValue(new BigDecimal("90"));
        testStandard.setMaxValue(new BigDecimal("110"));
        testStandard.setToleranceValue(new BigDecimal("5"));
        testStandard.setTenant(testTenant);
        testStandard.setProduct(testProduct);

        // 테스트 검사
        testInspection = new QualityInspectionEntity();
        testInspection.setQualityInspectionId(1L);
        testInspection.setInspectionNo("IQC-2026-001");
        testInspection.setInspectionType("INCOMING");
        testInspection.setInspectionDate(LocalDateTime.now());
        testInspection.setInspectedQuantity(new BigDecimal("1000"));
        testInspection.setMeasuredValue(new BigDecimal("100"));  // 정상 범위
        testInspection.setTenant(testTenant);
        testInspection.setProduct(testProduct);
        testInspection.setQualityStandard(testStandard);
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("품질 검사 조회 - 테넌트별 조회 성공")
    void testFindByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<QualityInspectionEntity> expectedList = Arrays.asList(testInspection);

        when(qualityInspectionRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<QualityInspectionEntity> result = qualityInspectionService.findByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInspectionNo()).isEqualTo("IQC-2026-001");
        verify(qualityInspectionRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("품질 검사 조회 - ID로 조회 성공")
    void testFindById_Success() {
        // Given
        Long inspectionId = 1L;
        when(qualityInspectionRepository.findByIdWithAllRelations(inspectionId))
                .thenReturn(Optional.of(testInspection));

        // When
        Optional<QualityInspectionEntity> result = qualityInspectionService.findById(inspectionId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getQualityInspectionId()).isEqualTo(inspectionId);
        verify(qualityInspectionRepository, times(1)).findByIdWithAllRelations(inspectionId);
    }

    @Test
    @DisplayName("품질 검사 조회 - 작업지시별 조회 성공")
    void testFindByWorkOrderId_Success() {
        // Given
        Long workOrderId = 100L;
        List<QualityInspectionEntity> expectedList = Arrays.asList(testInspection);

        when(qualityInspectionRepository.findByWorkOrderIdWithRelations(workOrderId))
                .thenReturn(expectedList);

        // When
        List<QualityInspectionEntity> result = qualityInspectionService.findByWorkOrderId(workOrderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(qualityInspectionRepository, times(1)).findByWorkOrderIdWithRelations(workOrderId);
    }

    @Test
    @DisplayName("품질 검사 조회 - 검사 결과별 조회 성공")
    void testFindByResult_Success() {
        // Given
        String tenantId = "TEST001";
        String result = "PASS";
        List<QualityInspectionEntity> expectedList = Arrays.asList(testInspection);

        when(qualityInspectionRepository.findByTenantIdAndResultWithRelations(tenantId, result))
                .thenReturn(expectedList);

        // When
        List<QualityInspectionEntity> resultList = qualityInspectionService.findByResult(tenantId, result);

        // Then
        assertThat(resultList).isNotNull();
        assertThat(resultList).hasSize(1);
        verify(qualityInspectionRepository, times(1))
                .findByTenantIdAndResultWithRelations(tenantId, result);
    }

    @Test
    @DisplayName("품질 검사 조회 - 검사 유형별 조회 성공")
    void testFindByInspectionType_Success() {
        // Given
        String tenantId = "TEST001";
        String type = "INCOMING";
        List<QualityInspectionEntity> expectedList = Arrays.asList(testInspection);

        when(qualityInspectionRepository.findByTenantIdAndTypeWithRelations(tenantId, type))
                .thenReturn(expectedList);

        // When
        List<QualityInspectionEntity> result = qualityInspectionService.findByInspectionType(tenantId, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(qualityInspectionRepository, times(1))
                .findByTenantIdAndTypeWithRelations(tenantId, type);
    }

    // ================== 생성 테스트 (자동 판정 로직) ==================

    @Test
    @DisplayName("품질 검사 생성 - PASS 판정 (정상 범위)")
    void testCreateQualityInspection_Pass() {
        // Given
        testInspection.setMeasuredValue(new BigDecimal("100"));  // 90-110 범위 내

        when(qualityInspectionRepository.existsByTenantAndInspectionNo(any(), anyString()))
                .thenReturn(false);

        when(qualityInspectionRepository.save(any(QualityInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    QualityInspectionEntity saved = invocation.getArgument(0);
                    // 자동 판정 결과 검증
                    assertThat(saved.getInspectionResult()).isEqualTo("PASS");
                    // 합격 수량 = 검사 수량
                    assertThat(saved.getPassedQuantity()).isEqualByComparingTo(new BigDecimal("1000"));
                    assertThat(saved.getFailedQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                    return saved;
                });

        // When
        QualityInspectionEntity result = qualityInspectionService.createQualityInspection(testInspection);

        // Then
        assertThat(result).isNotNull();
        verify(qualityInspectionRepository, times(1)).save(any(QualityInspectionEntity.class));
    }

    @Test
    @DisplayName("품질 검사 생성 - FAIL 판정 (범위 초과)")
    void testCreateQualityInspection_Fail() {
        // Given
        testInspection.setMeasuredValue(new BigDecimal("120"));  // Max(110) 초과

        when(qualityInspectionRepository.existsByTenantAndInspectionNo(any(), anyString()))
                .thenReturn(false);

        when(qualityInspectionRepository.save(any(QualityInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    QualityInspectionEntity saved = invocation.getArgument(0);
                    // FAIL 판정 검증
                    assertThat(saved.getInspectionResult()).isEqualTo("FAIL");
                    // 불합격 수량 = 검사 수량
                    assertThat(saved.getPassedQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getFailedQuantity()).isEqualByComparingTo(new BigDecimal("1000"));
                    return saved;
                });

        // When
        QualityInspectionEntity result = qualityInspectionService.createQualityInspection(testInspection);

        // Then
        assertThat(result).isNotNull();
        verify(qualityInspectionRepository, times(1)).save(any(QualityInspectionEntity.class));
    }

    @Test
    @DisplayName("품질 검사 생성 - CONDITIONAL 판정 (허용오차 범위)")
    void testCreateQualityInspection_Conditional() {
        // Given
        // Max(110) 초과이지만 Tolerance(5) 범위 내 (110 + 5 = 115)
        testInspection.setMeasuredValue(new BigDecimal("113"));

        when(qualityInspectionRepository.existsByTenantAndInspectionNo(any(), anyString()))
                .thenReturn(false);

        when(qualityInspectionRepository.save(any(QualityInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    QualityInspectionEntity saved = invocation.getArgument(0);
                    // CONDITIONAL 판정 검증
                    assertThat(saved.getInspectionResult()).isEqualTo("CONDITIONAL");
                    // CONDITIONAL은 합격 처리
                    assertThat(saved.getPassedQuantity()).isEqualByComparingTo(new BigDecimal("1000"));
                    assertThat(saved.getFailedQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                    return saved;
                });

        // When
        QualityInspectionEntity result = qualityInspectionService.createQualityInspection(testInspection);

        // Then
        assertThat(result).isNotNull();
        verify(qualityInspectionRepository, times(1)).save(any(QualityInspectionEntity.class));
    }

    @Test
    @DisplayName("품질 검사 생성 - 실패 (중복 검사 번호)")
    void testCreateQualityInspection_Fail_DuplicateInspectionNo() {
        // Given
        when(qualityInspectionRepository.existsByTenantAndInspectionNo(any(), anyString()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> qualityInspectionService.createQualityInspection(testInspection))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Inspection number already exists")
                .hasMessageContaining("IQC-2026-001");

        verify(qualityInspectionRepository, never()).save(any(QualityInspectionEntity.class));
    }

    @Test
    @DisplayName("품질 검사 생성 - 기준 없음 (기본 PASS)")
    void testCreateQualityInspection_NoStandard_DefaultPass() {
        // Given
        QualityStandardEntity noLimitStandard = new QualityStandardEntity();
        noLimitStandard.setQualityStandardId(2L);
        noLimitStandard.setMinValue(null);  // 기준 없음
        noLimitStandard.setMaxValue(null);  // 기준 없음

        testInspection.setQualityStandard(noLimitStandard);
        testInspection.setMeasuredValue(new BigDecimal("999"));  // 아무 값

        when(qualityInspectionRepository.existsByTenantAndInspectionNo(any(), anyString()))
                .thenReturn(false);

        when(qualityInspectionRepository.save(any(QualityInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    QualityInspectionEntity saved = invocation.getArgument(0);
                    // 기준 없으면 기본 PASS
                    assertThat(saved.getInspectionResult()).isEqualTo("PASS");
                    return saved;
                });

        // When
        QualityInspectionEntity result = qualityInspectionService.createQualityInspection(testInspection);

        // Then
        assertThat(result).isNotNull();
        verify(qualityInspectionRepository, times(1)).save(any(QualityInspectionEntity.class));
    }

    @Test
    @DisplayName("품질 검사 생성 - 최소값 미만 (FAIL)")
    void testCreateQualityInspection_BelowMin_Fail() {
        // Given
        testInspection.setMeasuredValue(new BigDecimal("80"));  // Min(90) 미만

        when(qualityInspectionRepository.existsByTenantAndInspectionNo(any(), anyString()))
                .thenReturn(false);

        when(qualityInspectionRepository.save(any(QualityInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    QualityInspectionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getInspectionResult()).isEqualTo("FAIL");
                    return saved;
                });

        // When
        QualityInspectionEntity result = qualityInspectionService.createQualityInspection(testInspection);

        // Then
        assertThat(result).isNotNull();
        verify(qualityInspectionRepository, times(1)).save(any(QualityInspectionEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("품질 검사 수정 - 성공 (결과 재계산)")
    void testUpdateQualityInspection_Success() {
        // Given
        testInspection.setMeasuredValue(new BigDecimal("95"));  // PASS

        when(qualityInspectionRepository.existsById(testInspection.getQualityInspectionId()))
                .thenReturn(true);

        when(qualityInspectionRepository.save(any(QualityInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    QualityInspectionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getInspectionResult()).isEqualTo("PASS");
                    return saved;
                });

        // When
        QualityInspectionEntity result = qualityInspectionService.updateQualityInspection(testInspection);

        // Then
        assertThat(result).isNotNull();
        verify(qualityInspectionRepository, times(1)).existsById(testInspection.getQualityInspectionId());
        verify(qualityInspectionRepository, times(1)).save(any(QualityInspectionEntity.class));
    }

    @Test
    @DisplayName("품질 검사 수정 - 실패 (검사 없음)")
    void testUpdateQualityInspection_Fail_NotFound() {
        // Given
        when(qualityInspectionRepository.existsById(testInspection.getQualityInspectionId()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> qualityInspectionService.updateQualityInspection(testInspection))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quality inspection not found");

        verify(qualityInspectionRepository, never()).save(any(QualityInspectionEntity.class));
    }

    @Test
    @DisplayName("품질 검사 수정 - 측정값 변경으로 결과 변경 (PASS → FAIL)")
    void testUpdateQualityInspection_ResultChange() {
        // Given
        testInspection.setMeasuredValue(new BigDecimal("120"));  // Max(110) 초과 → FAIL

        when(qualityInspectionRepository.existsById(testInspection.getQualityInspectionId()))
                .thenReturn(true);

        when(qualityInspectionRepository.save(any(QualityInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    QualityInspectionEntity saved = invocation.getArgument(0);
                    // FAIL로 변경 확인
                    assertThat(saved.getInspectionResult()).isEqualTo("FAIL");
                    assertThat(saved.getFailedQuantity()).isEqualByComparingTo(new BigDecimal("1000"));
                    return saved;
                });

        // When
        QualityInspectionEntity result = qualityInspectionService.updateQualityInspection(testInspection);

        // Then
        assertThat(result).isNotNull();
        verify(qualityInspectionRepository, times(1)).save(any(QualityInspectionEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("품질 검사 삭제 - 성공")
    void testDeleteQualityInspection_Success() {
        // Given
        Long inspectionId = 1L;
        doNothing().when(qualityInspectionRepository).deleteById(inspectionId);

        // When
        qualityInspectionService.deleteQualityInspection(inspectionId);

        // Then
        verify(qualityInspectionRepository, times(1)).deleteById(inspectionId);
    }

    // ================== 통계 테스트 ==================

    @Test
    @DisplayName("통계 - 결과별 카운트 성공")
    void testCountByTenantAndResult_Success() {
        // Given
        String tenantId = "TEST001";
        String result = "PASS";

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(qualityInspectionRepository.countByTenantAndInspectionResult(testTenant, result))
                .thenReturn(50L);

        // When
        long count = qualityInspectionService.countByTenantAndResult(tenantId, result);

        // Then
        assertThat(count).isEqualTo(50L);
        verify(tenantRepository, times(1)).findById(tenantId);
        verify(qualityInspectionRepository, times(1))
                .countByTenantAndInspectionResult(testTenant, result);
    }

    @Test
    @DisplayName("통계 - 전체 카운트 성공")
    void testCountByTenant_Success() {
        // Given
        String tenantId = "TEST001";

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(qualityInspectionRepository.countByTenant(testTenant))
                .thenReturn(100L);

        // When
        long count = qualityInspectionService.countByTenant(tenantId);

        // Then
        assertThat(count).isEqualTo(100L);
        verify(tenantRepository, times(1)).findById(tenantId);
        verify(qualityInspectionRepository, times(1)).countByTenant(testTenant);
    }

    @Test
    @DisplayName("통계 - 합격률 계산 성공")
    void testCalculatePassRate_Success() {
        // Given
        String tenantId = "TEST001";

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        // 총 100개 중 75개 합격
        when(qualityInspectionRepository.countByTenant(testTenant))
                .thenReturn(100L);

        when(qualityInspectionRepository.countByTenantAndInspectionResult(testTenant, "PASS"))
                .thenReturn(75L);

        // When
        double passRate = qualityInspectionService.calculatePassRate(tenantId);

        // Then
        assertThat(passRate).isEqualTo(75.0);
        verify(tenantRepository, times(2)).findById(tenantId);  // countByTenant + countByTenantAndResult
    }

    @Test
    @DisplayName("통계 - 합격률 계산 (검사 없음 → 0%)")
    void testCalculatePassRate_NoInspections() {
        // Given
        String tenantId = "TEST001";

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(qualityInspectionRepository.countByTenant(testTenant))
                .thenReturn(0L);  // 검사 없음

        // When
        double passRate = qualityInspectionService.calculatePassRate(tenantId);

        // Then
        assertThat(passRate).isEqualTo(0.0);
        verify(tenantRepository, times(1)).findById(tenantId);
    }

    // ========================================
    // 재시험 및 반품 워크플로우 테스트
    // ========================================

    @Test
    @DisplayName("재시험 필요 검사 조회 - 성공")
    void testFindRetestRequired_Success() {
        // Given
        String tenantId = "TEST001";

        // Failed inspection with corrective action defined but not completed
        QualityInspectionEntity failedWithAction = QualityInspectionEntity.builder()
                .qualityInspectionId(1L)
                .inspectionNo("QI-001")
                .inspectionResult("FAIL")
                .correctiveAction("재작업 필요")
                .correctiveActionDate(null)  // Not completed yet
                .build();

        // Failed inspection with corrective action completed
        QualityInspectionEntity failedCompleted = QualityInspectionEntity.builder()
                .qualityInspectionId(2L)
                .inspectionNo("QI-002")
                .inspectionResult("FAIL")
                .correctiveAction("재작업 완료")
                .correctiveActionDate(LocalDateTime.now())  // Already completed
                .build();

        // Failed inspection without corrective action
        QualityInspectionEntity failedNoAction = QualityInspectionEntity.builder()
                .qualityInspectionId(3L)
                .inspectionNo("QI-003")
                .inspectionResult("FAIL")
                .correctiveAction(null)
                .build();

        List<QualityInspectionEntity> failedInspections = Arrays.asList(
                failedWithAction, failedCompleted, failedNoAction
        );

        when(qualityInspectionRepository.findByTenantIdAndResultWithRelations(tenantId, "FAIL"))
                .thenReturn(failedInspections);

        // When
        List<QualityInspectionEntity> result = qualityInspectionService.findRetestRequired(tenantId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInspectionNo()).isEqualTo("QI-001");
        assertThat(result.get(0).getCorrectiveAction()).isNotNull();
        assertThat(result.get(0).getCorrectiveActionDate()).isNull();
        verify(qualityInspectionRepository).findByTenantIdAndResultWithRelations(tenantId, "FAIL");
    }

    @Test
    @DisplayName("재시험 필요 검사 조회 - 결과 없음")
    void testFindRetestRequired_NoResults() {
        // Given
        String tenantId = "TEST001";

        // All inspections are either completed or have no corrective action
        QualityInspectionEntity failedCompleted = QualityInspectionEntity.builder()
                .qualityInspectionId(1L)
                .inspectionNo("QI-001")
                .inspectionResult("FAIL")
                .correctiveAction("완료")
                .correctiveActionDate(LocalDateTime.now())
                .build();

        List<QualityInspectionEntity> failedInspections = Arrays.asList(failedCompleted);

        when(qualityInspectionRepository.findByTenantIdAndResultWithRelations(tenantId, "FAIL"))
                .thenReturn(failedInspections);

        // When
        List<QualityInspectionEntity> result = qualityInspectionService.findRetestRequired(tenantId);

        // Then
        assertThat(result).isEmpty();
        verify(qualityInspectionRepository).findByTenantIdAndResultWithRelations(tenantId, "FAIL");
    }

    @Test
    @DisplayName("반품용 실패 항목 조회 - 성공")
    void testFindFailedItemsForReturns_Success() {
        // Given
        String tenantId = "TEST001";

        QualityInspectionEntity failed1 = QualityInspectionEntity.builder()
                .qualityInspectionId(1L)
                .inspectionNo("QI-001")
                .inspectionResult("FAIL")
                .build();

        QualityInspectionEntity failed2 = QualityInspectionEntity.builder()
                .qualityInspectionId(2L)
                .inspectionNo("QI-002")
                .inspectionResult("FAIL")
                .build();

        List<QualityInspectionEntity> failedInspections = Arrays.asList(failed1, failed2);

        when(qualityInspectionRepository.findByTenantIdAndResultWithRelations(tenantId, "FAIL"))
                .thenReturn(failedInspections);

        // When
        List<QualityInspectionEntity> result = qualityInspectionService.findFailedItemsForReturns(tenantId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(qi -> "FAIL".equals(qi.getInspectionResult()));
        verify(qualityInspectionRepository).findByTenantIdAndResultWithRelations(tenantId, "FAIL");
    }

    @Test
    @DisplayName("반품용 실패 항목 조회 - 결과 없음")
    void testFindFailedItemsForReturns_NoResults() {
        // Given
        String tenantId = "TEST001";

        when(qualityInspectionRepository.findByTenantIdAndResultWithRelations(tenantId, "FAIL"))
                .thenReturn(Arrays.asList());

        // When
        List<QualityInspectionEntity> result = qualityInspectionService.findFailedItemsForReturns(tenantId);

        // Then
        assertThat(result).isEmpty();
        verify(qualityInspectionRepository).findByTenantIdAndResultWithRelations(tenantId, "FAIL");
    }
}
