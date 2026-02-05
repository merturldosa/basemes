package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.QualityStandardEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.QualityStandardRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * QualityStandardService Unit Test
 *
 * 테스트 대상:
 * - 품질 기준 CRUD
 * - 활성화/비활성화
 * - 중복 기준 검증
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QualityStandardService 단위 테스트")
class QualityStandardServiceTest {

    @Mock
    private QualityStandardRepository qualityStandardRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private QualityStandardService qualityStandardService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private QualityStandardEntity testStandard;

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

        // 테스트 품질 기준
        testStandard = new QualityStandardEntity();
        testStandard.setQualityStandardId(1L);
        testStandard.setStandardCode("QS-001");
        testStandard.setStandardName("테스트 품질 기준");
        testStandard.setStandardVersion("1.0");
        testStandard.setInspectionType("INCOMING");
        testStandard.setMinValue(new BigDecimal("90"));
        testStandard.setMaxValue(new BigDecimal("110"));
        testStandard.setToleranceValue(new BigDecimal("5"));
        testStandard.setIsActive(true);
        testStandard.setTenant(testTenant);
        testStandard.setProduct(testProduct);
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("품질 기준 조회 - 테넌트별 조회 성공")
    void testFindByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<QualityStandardEntity> expectedList = Arrays.asList(testStandard);

        when(qualityStandardRepository.findByTenantIdWithRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<QualityStandardEntity> result = qualityStandardService.findByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStandardCode()).isEqualTo("QS-001");
        verify(qualityStandardRepository, times(1)).findByTenantIdWithRelations(tenantId);
    }

    @Test
    @DisplayName("품질 기준 조회 - 활성 기준만 조회 성공")
    void testFindActiveByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<QualityStandardEntity> expectedList = Arrays.asList(testStandard);

        when(qualityStandardRepository.findByTenantIdAndIsActiveWithRelations(tenantId, true))
                .thenReturn(expectedList);

        // When
        List<QualityStandardEntity> result = qualityStandardService.findActiveByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(qualityStandardRepository, times(1))
                .findByTenantIdAndIsActiveWithRelations(tenantId, true);
    }

    @Test
    @DisplayName("품질 기준 조회 - ID로 조회 성공")
    void testFindById_Success() {
        // Given
        Long standardId = 1L;
        when(qualityStandardRepository.findByIdWithRelations(standardId))
                .thenReturn(Optional.of(testStandard));

        // When
        Optional<QualityStandardEntity> result = qualityStandardService.findById(standardId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getQualityStandardId()).isEqualTo(standardId);
        verify(qualityStandardRepository, times(1)).findByIdWithRelations(standardId);
    }

    @Test
    @DisplayName("품질 기준 조회 - 제품별 조회 성공")
    void testFindByProductId_Success() {
        // Given
        Long productId = 1L;
        List<QualityStandardEntity> expectedList = Arrays.asList(testStandard);

        when(qualityStandardRepository.findByProductIdWithRelations(productId))
                .thenReturn(expectedList);

        // When
        List<QualityStandardEntity> result = qualityStandardService.findByProductId(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(qualityStandardRepository, times(1)).findByProductIdWithRelations(productId);
    }

    @Test
    @DisplayName("품질 기준 조회 - 검사 유형별 조회 성공")
    void testFindByInspectionType_Success() {
        // Given
        String tenantId = "TEST001";
        String inspectionType = "INCOMING";
        List<QualityStandardEntity> expectedList = Arrays.asList(testStandard);

        when(qualityStandardRepository.findByTenantIdAndInspectionTypeWithRelations(tenantId, inspectionType))
                .thenReturn(expectedList);

        // When
        List<QualityStandardEntity> result = qualityStandardService.findByInspectionType(tenantId, inspectionType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInspectionType()).isEqualTo(inspectionType);
        verify(qualityStandardRepository, times(1))
                .findByTenantIdAndInspectionTypeWithRelations(tenantId, inspectionType);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("품질 기준 생성 - 성공")
    void testCreateQualityStandard_Success() {
        // Given
        when(qualityStandardRepository.existsByTenantAndStandardCodeAndStandardVersion(
                any(), anyString(), anyString()))
                .thenReturn(false);

        when(qualityStandardRepository.save(any(QualityStandardEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        QualityStandardEntity result = qualityStandardService.createQualityStandard(testStandard);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStandardCode()).isEqualTo("QS-001");
        verify(qualityStandardRepository, times(1))
                .existsByTenantAndStandardCodeAndStandardVersion(any(), anyString(), anyString());
        verify(qualityStandardRepository, times(1)).save(any(QualityStandardEntity.class));
    }

    @Test
    @DisplayName("품질 기준 생성 - 실패 (중복 기준)")
    void testCreateQualityStandard_Fail_Duplicate() {
        // Given
        when(qualityStandardRepository.existsByTenantAndStandardCodeAndStandardVersion(
                any(), anyString(), anyString()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> qualityStandardService.createQualityStandard(testStandard))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quality standard code already exists")
                .hasMessageContaining("QS-001");

        verify(qualityStandardRepository, never()).save(any(QualityStandardEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("품질 기준 수정 - 성공")
    void testUpdateQualityStandard_Success() {
        // Given
        when(qualityStandardRepository.existsById(testStandard.getQualityStandardId()))
                .thenReturn(true);

        when(qualityStandardRepository.save(any(QualityStandardEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        QualityStandardEntity result = qualityStandardService.updateQualityStandard(testStandard);

        // Then
        assertThat(result).isNotNull();
        verify(qualityStandardRepository, times(1)).existsById(testStandard.getQualityStandardId());
        verify(qualityStandardRepository, times(1)).save(any(QualityStandardEntity.class));
    }

    @Test
    @DisplayName("품질 기준 수정 - 실패 (기준 없음)")
    void testUpdateQualityStandard_Fail_NotFound() {
        // Given
        when(qualityStandardRepository.existsById(testStandard.getQualityStandardId()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> qualityStandardService.updateQualityStandard(testStandard))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quality standard not found");

        verify(qualityStandardRepository, never()).save(any(QualityStandardEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("품질 기준 삭제 - 성공")
    void testDeleteQualityStandard_Success() {
        // Given
        Long standardId = 1L;
        doNothing().when(qualityStandardRepository).deleteById(standardId);

        // When
        qualityStandardService.deleteQualityStandard(standardId);

        // Then
        verify(qualityStandardRepository, times(1)).deleteById(standardId);
    }

    // ================== 활성화/비활성화 테스트 ==================

    @Test
    @DisplayName("품질 기준 활성화 - 성공")
    void testActivateQualityStandard_Success() {
        // Given
        Long standardId = 1L;
        testStandard.setIsActive(false);  // 비활성 상태

        when(qualityStandardRepository.findById(standardId))
                .thenReturn(Optional.of(testStandard));

        when(qualityStandardRepository.save(any(QualityStandardEntity.class)))
                .thenAnswer(invocation -> {
                    QualityStandardEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });

        // When
        QualityStandardEntity result = qualityStandardService.activateQualityStandard(standardId);

        // Then
        assertThat(result).isNotNull();
        verify(qualityStandardRepository, times(1)).findById(standardId);
        verify(qualityStandardRepository, times(1)).save(any(QualityStandardEntity.class));
    }

    @Test
    @DisplayName("품질 기준 활성화 - 실패 (기준 없음)")
    void testActivateQualityStandard_Fail_NotFound() {
        // Given
        Long standardId = 999L;

        when(qualityStandardRepository.findById(standardId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> qualityStandardService.activateQualityStandard(standardId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quality standard not found")
                .hasMessageContaining("999");

        verify(qualityStandardRepository, never()).save(any(QualityStandardEntity.class));
    }

    @Test
    @DisplayName("품질 기준 비활성화 - 성공")
    void testDeactivateQualityStandard_Success() {
        // Given
        Long standardId = 1L;
        testStandard.setIsActive(true);  // 활성 상태

        when(qualityStandardRepository.findById(standardId))
                .thenReturn(Optional.of(testStandard));

        when(qualityStandardRepository.save(any(QualityStandardEntity.class)))
                .thenAnswer(invocation -> {
                    QualityStandardEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });

        // When
        QualityStandardEntity result = qualityStandardService.deactivateQualityStandard(standardId);

        // Then
        assertThat(result).isNotNull();
        verify(qualityStandardRepository, times(1)).findById(standardId);
        verify(qualityStandardRepository, times(1)).save(any(QualityStandardEntity.class));
    }

    @Test
    @DisplayName("품질 기준 비활성화 - 실패 (기준 없음)")
    void testDeactivateQualityStandard_Fail_NotFound() {
        // Given
        Long standardId = 999L;

        when(qualityStandardRepository.findById(standardId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> qualityStandardService.deactivateQualityStandard(standardId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quality standard not found")
                .hasMessageContaining("999");

        verify(qualityStandardRepository, never()).save(any(QualityStandardEntity.class));
    }

    // ================== 통계 테스트 ==================

    @Test
    @DisplayName("통계 - 테넌트별 카운트 성공")
    void testCountByTenant_Success() {
        // Given
        String tenantId = "TEST001";

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(qualityStandardRepository.countByTenant(testTenant))
                .thenReturn(10L);

        // When
        long count = qualityStandardService.countByTenant(tenantId);

        // Then
        assertThat(count).isEqualTo(10L);
        verify(tenantRepository, times(1)).findById(tenantId);
        verify(qualityStandardRepository, times(1)).countByTenant(testTenant);
    }
}
