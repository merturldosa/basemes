package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.BomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BomService Unit Test
 *
 * 테스트 대상:
 * - BOM CRUD
 * - BOM 활성화 토글
 * - BOM 버전 복사
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BomService 단위 테스트")
class BomServiceTest {

    @Mock
    private BomRepository bomRepository;

    @InjectMocks
    private BomService bomService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private ProductEntity testMaterial;
    private BomEntity testBom;
    private BomDetailEntity testDetail;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        // 테스트 제품 (완제품)
        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P-FIN-001");
        testProduct.setProductName("완제품");

        // 테스트 자재
        testMaterial = new ProductEntity();
        testMaterial.setProductId(2L);
        testMaterial.setProductCode("P-MAT-001");
        testMaterial.setProductName("원자재");

        // 테스트 BOM
        testBom = new BomEntity();
        testBom.setBomId(1L);
        testBom.setBomCode("BOM-001");
        testBom.setBomName("완제품 BOM");
        testBom.setVersion("1.0");
        testBom.setIsActive(true);
        testBom.setTenant(testTenant);
        testBom.setProduct(testProduct);

        // 테스트 BOM 상세
        testDetail = new BomDetailEntity();
        testDetail.setBomDetailId(1L);
        testDetail.setSequence(1);
        testDetail.setMaterialProduct(testMaterial);
        testDetail.setQuantity(new BigDecimal("2.5"));
        testDetail.setUnit("KG");
        testDetail.setBom(testBom);

        // BOM에 상세 추가
        List<BomDetailEntity> details = new ArrayList<>();
        details.add(testDetail);
        testBom.setDetails(details);
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("BOM 조회 - 테넌트별 조회 성공")
    void testFindByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<BomEntity> expectedList = Arrays.asList(testBom);

        when(bomRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<BomEntity> result = bomService.findByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBomCode()).isEqualTo("BOM-001");
        verify(bomRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("BOM 조회 - 제품별 조회 성공")
    void testFindByTenantAndProduct_Success() {
        // Given
        String tenantId = "TEST001";
        Long productId = 1L;
        List<BomEntity> expectedList = Arrays.asList(testBom);

        when(bomRepository.findByTenantIdAndProductIdWithAllRelations(tenantId, productId))
                .thenReturn(expectedList);

        // When
        List<BomEntity> result = bomService.findByTenantAndProduct(tenantId, productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(bomRepository, times(1)).findByTenantIdAndProductIdWithAllRelations(tenantId, productId);
    }

    @Test
    @DisplayName("BOM 조회 - 활성 BOM만 조회 성공")
    void testFindActiveByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<BomEntity> expectedList = Arrays.asList(testBom);

        when(bomRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true))
                .thenReturn(expectedList);

        // When
        List<BomEntity> result = bomService.findActiveByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(bomRepository, times(1)).findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    @Test
    @DisplayName("BOM 조회 - ID로 조회 성공")
    void testFindById_Success() {
        // Given
        Long bomId = 1L;
        when(bomRepository.findByIdWithAllRelations(bomId))
                .thenReturn(Optional.of(testBom));

        // When
        Optional<BomEntity> result = bomService.findById(bomId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getBomId()).isEqualTo(bomId);
        verify(bomRepository, times(1)).findByIdWithAllRelations(bomId);
    }

    @Test
    @DisplayName("BOM 조회 - BOM 코드와 버전으로 조회 성공")
    void testFindByBomCodeAndVersion_Success() {
        // Given
        String tenantId = "TEST001";
        String bomCode = "BOM-001";
        String version = "1.0";

        when(bomRepository.findByTenant_TenantIdAndBomCodeAndVersion(tenantId, bomCode, version))
                .thenReturn(Optional.of(testBom));

        // When
        Optional<BomEntity> result = bomService.findByBomCodeAndVersion(tenantId, bomCode, version);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getBomCode()).isEqualTo(bomCode);
        assertThat(result.get().getVersion()).isEqualTo(version);
        verify(bomRepository, times(1)).findByTenant_TenantIdAndBomCodeAndVersion(tenantId, bomCode, version);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("BOM 생성 - 성공 (상세 시퀀스 자동 설정)")
    void testCreateBom_Success() {
        // Given
        BomDetailEntity detailWithoutSequence = new BomDetailEntity();
        detailWithoutSequence.setMaterialProduct(testMaterial);
        detailWithoutSequence.setQuantity(new BigDecimal("1.0"));

        testBom.getDetails().clear();
        testBom.getDetails().add(detailWithoutSequence);

        when(bomRepository.existsByTenantAndBomCodeAndVersion(any(), anyString(), anyString()))
                .thenReturn(false);

        when(bomRepository.save(any(BomEntity.class)))
                .thenAnswer(invocation -> {
                    BomEntity saved = invocation.getArgument(0);
                    saved.setBomId(1L);
                    // 시퀀스가 자동 설정되었는지 검증
                    assertThat(saved.getDetails().get(0).getSequence()).isEqualTo(1);
                    assertThat(saved.getDetails().get(0).getBom()).isEqualTo(saved);
                    return saved;
                });

        when(bomRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testBom));

        // When
        BomEntity result = bomService.createBom(testBom);

        // Then
        assertThat(result).isNotNull();
        verify(bomRepository, times(1)).existsByTenantAndBomCodeAndVersion(any(), anyString(), anyString());
        verify(bomRepository, times(1)).save(any(BomEntity.class));
    }

    @Test
    @DisplayName("BOM 생성 - 실패 (중복 BOM)")
    void testCreateBom_Fail_Duplicate() {
        // Given
        when(bomRepository.existsByTenantAndBomCodeAndVersion(any(), anyString(), anyString()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> bomService.createBom(testBom))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BOM already exists")
                .hasMessageContaining("BOM-001")
                .hasMessageContaining("1.0");

        verify(bomRepository, never()).save(any(BomEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("BOM 수정 - 성공 (상세 시퀀스 재설정)")
    void testUpdateBom_Success() {
        // Given
        BomDetailEntity newDetail = new BomDetailEntity();
        newDetail.setMaterialProduct(testMaterial);
        newDetail.setQuantity(new BigDecimal("3.0"));
        newDetail.setUnit("EA");

        testBom.clearDetails();
        testBom.addDetail(newDetail);

        when(bomRepository.save(any(BomEntity.class)))
                .thenAnswer(invocation -> {
                    BomEntity saved = invocation.getArgument(0);
                    // 시퀀스가 재설정되었는지 검증
                    assertThat(saved.getDetails()).hasSize(1);
                    assertThat(saved.getDetails().get(0).getSequence()).isEqualTo(1);
                    return saved;
                });

        when(bomRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testBom));

        // When
        BomEntity result = bomService.updateBom(testBom);

        // Then
        assertThat(result).isNotNull();
        verify(bomRepository, times(1)).save(any(BomEntity.class));
        verify(bomRepository, times(1)).findByIdWithAllRelations(anyLong());
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("BOM 삭제 - 성공")
    void testDeleteBom_Success() {
        // Given
        Long bomId = 1L;
        doNothing().when(bomRepository).deleteById(bomId);

        // When
        bomService.deleteBom(bomId);

        // Then
        verify(bomRepository, times(1)).deleteById(bomId);
    }

    // ================== 활성화 토글 테스트 ==================

    @Test
    @DisplayName("BOM 활성화 토글 - 성공 (활성 → 비활성)")
    void testToggleActive_ActiveToInactive() {
        // Given
        Long bomId = 1L;
        testBom.setIsActive(true);

        when(bomRepository.findById(bomId))
                .thenReturn(Optional.of(testBom));

        when(bomRepository.save(any(BomEntity.class)))
                .thenAnswer(invocation -> {
                    BomEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();  // 토글됨
                    return saved;
                });

        when(bomRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testBom));

        // When
        BomEntity result = bomService.toggleActive(bomId);

        // Then
        assertThat(result).isNotNull();
        verify(bomRepository, times(1)).findById(bomId);
        verify(bomRepository, times(1)).save(any(BomEntity.class));
    }

    @Test
    @DisplayName("BOM 활성화 토글 - 실패 (BOM 없음)")
    void testToggleActive_Fail_NotFound() {
        // Given
        Long bomId = 999L;

        when(bomRepository.findById(bomId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bomService.toggleActive(bomId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BOM not found")
                .hasMessageContaining("999");

        verify(bomRepository, never()).save(any(BomEntity.class));
    }

    // ================== BOM 복사 테스트 ==================

    @Test
    @DisplayName("BOM 복사 - 성공 (새 버전 생성)")
    void testCopyBom_Success() {
        // Given
        Long sourceBomId = 1L;
        String newVersion = "2.0";

        when(bomRepository.findByIdWithAllRelations(sourceBomId))
                .thenReturn(Optional.of(testBom));

        when(bomRepository.existsByTenantAndBomCodeAndVersion(any(), anyString(), eq(newVersion)))
                .thenReturn(false);

        when(bomRepository.save(any(BomEntity.class)))
                .thenAnswer(invocation -> {
                    BomEntity saved = invocation.getArgument(0);
                    saved.setBomId(2L);
                    // 버전이 변경되었는지 확인
                    assertThat(saved.getVersion()).isEqualTo(newVersion);
                    // 상세도 복사되었는지 확인
                    assertThat(saved.getDetails()).hasSize(1);
                    return saved;
                });

        // When
        BomEntity result = bomService.copyBom(sourceBomId, newVersion);

        // Then
        assertThat(result).isNotNull();
        verify(bomRepository, times(1)).findByIdWithAllRelations(sourceBomId);
        verify(bomRepository, times(1)).save(any(BomEntity.class));
    }

    @Test
    @DisplayName("BOM 복사 - 실패 (원본 BOM 없음)")
    void testCopyBom_Fail_SourceNotFound() {
        // Given
        Long sourceBomId = 999L;
        String newVersion = "2.0";

        when(bomRepository.findByIdWithAllRelations(sourceBomId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bomService.copyBom(sourceBomId, newVersion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source BOM not found")
                .hasMessageContaining("999");

        verify(bomRepository, never()).save(any(BomEntity.class));
    }

    @Test
    @DisplayName("BOM 복사 - 실패 (대상 버전 이미 존재)")
    void testCopyBom_Fail_TargetVersionExists() {
        // Given
        Long sourceBomId = 1L;
        String newVersion = "2.0";

        when(bomRepository.findByIdWithAllRelations(sourceBomId))
                .thenReturn(Optional.of(testBom));

        when(bomRepository.existsByTenantAndBomCodeAndVersion(any(), anyString(), eq(newVersion)))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> bomService.copyBom(sourceBomId, newVersion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target BOM version already exists")
                .hasMessageContaining("BOM-001")
                .hasMessageContaining("2.0");

        verify(bomRepository, never()).save(any(BomEntity.class));
    }
}
