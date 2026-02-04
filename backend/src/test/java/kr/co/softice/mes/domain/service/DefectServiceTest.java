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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DefectService Unit Test
 *
 * 테스트 대상:
 * - 불량품 CRUD
 * - 불량품 종료 (Close)
 * - 상태 관리
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefectService 단위 테스트")
class DefectServiceTest {

    @Mock
    private DefectRepository defectRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkResultRepository workResultRepository;

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private QualityInspectionRepository qualityInspectionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefectService defectService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private UserEntity testUser;
    private DefectEntity testDefect;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        // 테스트 제품
        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P-DEFECT-001");
        testProduct.setProductName("불량 테스트 제품");

        // 테스트 사용자
        testUser = new UserEntity();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setFullName("테스트 사용자");

        // 테스트 불량품
        testDefect = new DefectEntity();
        testDefect.setDefectId(1L);
        testDefect.setDefectNo("DEF-2026-001");
        testDefect.setDefectType("APPEARANCE");
        testDefect.setDefectCategory("SCRATCH");
        testDefect.setDefectDescription("표면 스크래치 발생");
        testDefect.setDefectQuantity(new BigDecimal("10"));
        testDefect.setSeverity("MAJOR");
        testDefect.setStatus("REPORTED");
        testDefect.setSourceType("PRODUCTION");
        testDefect.setTenant(testTenant);
        testDefect.setProduct(testProduct);
        testDefect.setReporterUser(testUser);
        testDefect.setIsActive(true);
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("불량품 조회 - 전체 조회 성공")
    void testGetAllDefects_Success() {
        // Given
        String tenantId = "TEST001";
        List<DefectEntity> expectedList = Arrays.asList(testDefect);

        when(defectRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<DefectEntity> result = defectService.getAllDefects(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDefectNo()).isEqualTo("DEF-2026-001");
        verify(defectRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("불량품 조회 - ID로 조회 성공")
    void testGetDefectById_Success() {
        // Given
        Long defectId = 1L;
        when(defectRepository.findByIdWithAllRelations(defectId))
                .thenReturn(Optional.of(testDefect));

        // When
        DefectEntity result = defectService.getDefectById(defectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDefectId()).isEqualTo(defectId);
        verify(defectRepository, times(1)).findByIdWithAllRelations(defectId);
    }

    @Test
    @DisplayName("불량품 조회 - ID로 조회 실패 (없음)")
    void testGetDefectById_Fail_NotFound() {
        // Given
        Long defectId = 999L;
        when(defectRepository.findByIdWithAllRelations(defectId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> defectService.getDefectById(defectId))
                .isInstanceOf(BusinessException.class);

        verify(defectRepository, times(1)).findByIdWithAllRelations(defectId);
    }

    @Test
    @DisplayName("불량품 조회 - 상태별 조회 성공")
    void testGetDefectsByStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String status = "REPORTED";
        List<DefectEntity> expectedList = Arrays.asList(testDefect);

        when(defectRepository.findByTenantIdAndStatus(tenantId, status))
                .thenReturn(expectedList);

        // When
        List<DefectEntity> result = defectService.getDefectsByStatus(tenantId, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);
        verify(defectRepository, times(1)).findByTenantIdAndStatus(tenantId, status);
    }

    @Test
    @DisplayName("불량품 조회 - 소스 타입별 조회 성공")
    void testGetDefectsBySourceType_Success() {
        // Given
        String tenantId = "TEST001";
        String sourceType = "PRODUCTION";
        List<DefectEntity> expectedList = Arrays.asList(testDefect);

        when(defectRepository.findByTenantIdAndSourceType(tenantId, sourceType))
                .thenReturn(expectedList);

        // When
        List<DefectEntity> result = defectService.getDefectsBySourceType(tenantId, sourceType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSourceType()).isEqualTo(sourceType);
        verify(defectRepository, times(1)).findByTenantIdAndSourceType(tenantId, sourceType);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("불량품 생성 - 성공")
    void testCreateDefect_Success() {
        // Given
        String tenantId = "TEST001";

        when(defectRepository.existsByTenant_TenantIdAndDefectNo(tenantId, testDefect.getDefectNo()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(productRepository.findById(testProduct.getProductId()))
                .thenReturn(Optional.of(testProduct));

        when(userRepository.findById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));

        when(defectRepository.save(any(DefectEntity.class)))
                .thenAnswer(invocation -> {
                    DefectEntity saved = invocation.getArgument(0);
                    assertThat(saved.getTenant()).isEqualTo(testTenant);
                    assertThat(saved.getProduct()).isEqualTo(testProduct);
                    assertThat(saved.getProductCode()).isEqualTo(testProduct.getProductCode());
                    assertThat(saved.getProductName()).isEqualTo(testProduct.getProductName());
                    return saved;
                });

        // When
        DefectEntity result = defectService.createDefect(tenantId, testDefect);

        // Then
        assertThat(result).isNotNull();
        verify(defectRepository, times(1)).save(any(DefectEntity.class));
    }

    @Test
    @DisplayName("불량품 생성 - 실패 (중복 번호)")
    void testCreateDefect_Fail_Duplicate() {
        // Given
        String tenantId = "TEST001";

        when(defectRepository.existsByTenant_TenantIdAndDefectNo(tenantId, testDefect.getDefectNo()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> defectService.createDefect(tenantId, testDefect))
                .isInstanceOf(BusinessException.class);

        verify(defectRepository, never()).save(any(DefectEntity.class));
    }

    @Test
    @DisplayName("불량품 생성 - 기본값 설정 확인")
    void testCreateDefect_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        DefectEntity defectWithoutDefaults = new DefectEntity();
        defectWithoutDefaults.setDefectNo("DEF-2026-002");
        defectWithoutDefaults.setDefectType("DIMENSION");
        defectWithoutDefaults.setProduct(testProduct);

        when(defectRepository.existsByTenant_TenantIdAndDefectNo(tenantId, defectWithoutDefaults.getDefectNo()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(productRepository.findById(testProduct.getProductId()))
                .thenReturn(Optional.of(testProduct));

        when(defectRepository.save(any(DefectEntity.class)))
                .thenAnswer(invocation -> {
                    DefectEntity saved = invocation.getArgument(0);
                    // 기본값 검증
                    assertThat(saved.getDefectQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getDefectCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getIsActive()).isTrue();
                    assertThat(saved.getStatus()).isEqualTo("REPORTED");
                    return saved;
                });

        // When
        DefectEntity result = defectService.createDefect(tenantId, defectWithoutDefaults);

        // Then
        assertThat(result).isNotNull();
        verify(defectRepository, times(1)).save(any(DefectEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("불량품 수정 - 성공")
    void testUpdateDefect_Success() {
        // Given
        Long defectId = 1L;
        DefectEntity updateData = new DefectEntity();
        updateData.setDefectDescription("수정된 설명");
        updateData.setRootCause("근본 원인 분석 완료");
        updateData.setCorrectiveAction("시정조치 완료");

        when(defectRepository.findByIdWithAllRelations(defectId))
                .thenReturn(Optional.of(testDefect));

        when(defectRepository.save(any(DefectEntity.class)))
                .thenAnswer(invocation -> {
                    DefectEntity saved = invocation.getArgument(0);
                    assertThat(saved.getDefectDescription()).isEqualTo("수정된 설명");
                    assertThat(saved.getRootCause()).isEqualTo("근본 원인 분석 완료");
                    assertThat(saved.getCorrectiveAction()).isEqualTo("시정조치 완료");
                    assertThat(saved.getActionDate()).isNotNull();  // 시정조치 입력 시 자동 설정
                    return saved;
                });

        // When
        DefectEntity result = defectService.updateDefect(defectId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(defectRepository, times(1)).findByIdWithAllRelations(defectId);
        verify(defectRepository, times(1)).save(any(DefectEntity.class));
    }

    @Test
    @DisplayName("불량품 수정 - 실패 (불량품 없음)")
    void testUpdateDefect_Fail_NotFound() {
        // Given
        Long defectId = 999L;
        DefectEntity updateData = new DefectEntity();

        when(defectRepository.findByIdWithAllRelations(defectId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> defectService.updateDefect(defectId, updateData))
                .isInstanceOf(BusinessException.class);

        verify(defectRepository, never()).save(any(DefectEntity.class));
    }

    // ================== 종료 테스트 ==================

    @Test
    @DisplayName("불량품 종료 - 성공")
    void testCloseDefect_Success() {
        // Given
        Long defectId = 1L;

        when(defectRepository.findByIdWithAllRelations(defectId))
                .thenReturn(Optional.of(testDefect));

        when(defectRepository.save(any(DefectEntity.class)))
                .thenAnswer(invocation -> {
                    DefectEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("CLOSED");
                    assertThat(saved.getActionDate()).isNotNull();
                    return saved;
                });

        // When
        DefectEntity result = defectService.closeDefect(defectId);

        // Then
        assertThat(result).isNotNull();
        verify(defectRepository, times(1)).findByIdWithAllRelations(defectId);
        verify(defectRepository, times(1)).save(any(DefectEntity.class));
    }

    @Test
    @DisplayName("불량품 종료 - 실패 (불량품 없음)")
    void testCloseDefect_Fail_NotFound() {
        // Given
        Long defectId = 999L;

        when(defectRepository.findByIdWithAllRelations(defectId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> defectService.closeDefect(defectId))
                .isInstanceOf(BusinessException.class);

        verify(defectRepository, never()).save(any(DefectEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("불량품 삭제 - 성공")
    void testDeleteDefect_Success() {
        // Given
        Long defectId = 1L;

        when(defectRepository.findById(defectId))
                .thenReturn(Optional.of(testDefect));

        doNothing().when(defectRepository).delete(testDefect);

        // When
        defectService.deleteDefect(defectId);

        // Then
        verify(defectRepository, times(1)).findById(defectId);
        verify(defectRepository, times(1)).delete(testDefect);
    }

    @Test
    @DisplayName("불량품 삭제 - 실패 (불량품 없음)")
    void testDeleteDefect_Fail_NotFound() {
        // Given
        Long defectId = 999L;

        when(defectRepository.findById(defectId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> defectService.deleteDefect(defectId))
                .isInstanceOf(BusinessException.class);

        verify(defectRepository, never()).delete(any(DefectEntity.class));
    }
}
