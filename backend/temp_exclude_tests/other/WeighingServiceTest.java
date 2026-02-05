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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Weighing Service Test
 * 칭량 서비스 테스트
 *
 * @author Moon Myung-seop
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("칭량 서비스 테스트")
class WeighingServiceTest {

    @Mock
    private WeighingRepository weighingRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WeighingService weighingService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private LotEntity testLot;
    private UserEntity testOperator;
    private UserEntity testVerifier;
    private WeighingEntity testWeighing;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("PROD001");
        testProduct.setProductName("Test Product");
        testProduct.setTenant(testTenant);

        testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT-20260204-0001");
        testLot.setTenant(testTenant);

        testOperator = new UserEntity();
        testOperator.setUserId(1L);
        testOperator.setUsername("operator1");
        testOperator.setTenant(testTenant);

        testVerifier = new UserEntity();
        testVerifier.setUserId(2L);
        testVerifier.setUsername("verifier1");
        testVerifier.setTenant(testTenant);

        testWeighing = WeighingEntity.builder()
                .weighingId(1L)
                .tenant(testTenant)
                .weighingNo("WG-20260204-0001")
                .weighingDate(LocalDateTime.now())
                .weighingType("INCOMING")
                .referenceType("GOODS_RECEIPT")
                .referenceId(1L)
                .product(testProduct)
                .lot(testLot)
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .netWeight(new BigDecimal("1000.000"))
                .expectedWeight(new BigDecimal("1000.000"))
                .variance(new BigDecimal("0.000"))
                .variancePercentage(new BigDecimal("0.0000"))
                .unit("kg")
                .operator(testOperator)
                .verificationStatus("PENDING")
                .toleranceExceeded(false)
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();
    }

    // === Find Tests ===

    @Test
    @DisplayName("테넌트별 전체 조회 - 성공")
    void testFindByTenant_Success() {
        List<WeighingEntity> weighings = Arrays.asList(testWeighing);
        when(weighingRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(weighings);

        List<WeighingEntity> result = weighingService.findByTenant(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWeighingNo()).isEqualTo("WG-20260204-0001");
        verify(weighingRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("ID로 조회 - 성공")
    void testFindById_Success() {
        when(weighingRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testWeighing));

        Optional<WeighingEntity> result = weighingService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getWeighingId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("유형별 조회 - 성공")
    void testFindByType_Success() {
        List<WeighingEntity> weighings = Arrays.asList(testWeighing);
        when(weighingRepository.findByTenant_TenantIdAndWeighingType(tenantId, "INCOMING"))
                .thenReturn(weighings);

        List<WeighingEntity> result = weighingService.findByType(tenantId, "INCOMING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWeighingType()).isEqualTo("INCOMING");
    }

    @Test
    @DisplayName("검증 상태별 조회 - 성공")
    void testFindByVerificationStatus_Success() {
        List<WeighingEntity> weighings = Arrays.asList(testWeighing);
        when(weighingRepository.findByVerificationStatusAndTenantWithRelations(tenantId, "PENDING"))
                .thenReturn(weighings);

        List<WeighingEntity> result = weighingService.findByVerificationStatus(tenantId, "PENDING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVerificationStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("참조로 조회 - 성공")
    void testFindByReference_Success() {
        List<WeighingEntity> weighings = Arrays.asList(testWeighing);
        when(weighingRepository.findByReferenceTypeAndReferenceIdWithRelations("GOODS_RECEIPT", 1L))
                .thenReturn(weighings);

        List<WeighingEntity> result = weighingService.findByReference("GOODS_RECEIPT", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReferenceType()).isEqualTo("GOODS_RECEIPT");
    }

    @Test
    @DisplayName("허용 오차 초과 조회 - 성공")
    void testFindToleranceExceeded_Success() {
        WeighingEntity exceededWeighing = WeighingEntity.builder()
                .weighingId(2L)
                .toleranceExceeded(true)
                .variancePercentage(new BigDecimal("5.0"))
                .build();

        List<WeighingEntity> weighings = Arrays.asList(exceededWeighing);
        when(weighingRepository.findToleranceExceededWeighings(tenantId))
                .thenReturn(weighings);

        List<WeighingEntity> result = weighingService.findToleranceExceeded(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getToleranceExceeded()).isTrue();
    }

    @Test
    @DisplayName("검증 대기 조회 - 성공")
    void testFindPendingVerification_Success() {
        List<WeighingEntity> weighings = Arrays.asList(testWeighing);
        when(weighingRepository.findPendingVerificationWeighings(tenantId))
                .thenReturn(weighings);

        List<WeighingEntity> result = weighingService.findPendingVerification(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVerificationStatus()).isEqualTo("PENDING");
    }

    // === Create Tests ===

    @Test
    @DisplayName("칭량 생성 - 성공 (자동 계산)")
    void testCreateWeighing_Success_WithAutoCalculation() {
        WeighingEntity newWeighing = WeighingEntity.builder()
                .tenant(testTenant)
                .weighingDate(LocalDateTime.now())
                .weighingType("INCOMING")
                .product(testProduct)
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .expectedWeight(new BigDecimal("1000.000"))
                .operator(testOperator)
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        when(weighingRepository.existsByTenant_TenantIdAndWeighingNo(anyString(), anyString()))
                .thenReturn(false);
        when(weighingRepository.save(any(WeighingEntity.class)))
                .thenAnswer(invocation -> {
                    WeighingEntity w = invocation.getArgument(0);
                    w.setWeighingId(1L);
                    return w;
                });
        when(weighingRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testWeighing));

        WeighingEntity result = weighingService.createWeighing(newWeighing);

        assertThat(result).isNotNull();
        assertThat(result.getNetWeight()).isEqualByComparingTo(new BigDecimal("1000.000"));
        assertThat(result.getVariance()).isEqualByComparingTo(new BigDecimal("0.000"));
        assertThat(result.getVariancePercentage()).isEqualByComparingTo(new BigDecimal("0.0000"));
        assertThat(result.getToleranceExceeded()).isFalse();
        verify(weighingRepository).save(any(WeighingEntity.class));
    }

    @Test
    @DisplayName("칭량 생성 - 허용 오차 초과")
    void testCreateWeighing_ToleranceExceeded() {
        WeighingEntity newWeighing = WeighingEntity.builder()
                .tenant(testTenant)
                .weighingDate(LocalDateTime.now())
                .weighingType("PRODUCTION")
                .product(testProduct)
                .tareWeight(new BigDecimal("100.000"))
                .grossWeight(new BigDecimal("550.000"))
                .expectedWeight(new BigDecimal("500.000"))
                .operator(testOperator)
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        when(weighingRepository.existsByTenant_TenantIdAndWeighingNo(anyString(), anyString()))
                .thenReturn(false);
        when(weighingRepository.save(any(WeighingEntity.class)))
                .thenAnswer(invocation -> {
                    WeighingEntity w = invocation.getArgument(0);
                    w.setWeighingId(2L);
                    return w;
                });
        when(weighingRepository.findByIdWithAllRelations(2L))
                .thenReturn(Optional.of(newWeighing));

        WeighingEntity result = weighingService.createWeighing(newWeighing);

        // Net weight: 550 - 100 = 450
        // Variance: 450 - 500 = -50
        // Variance %: -50 / 500 * 100 = -10%
        // Tolerance: 2%
        // abs(-10%) > 2% => true
        assertThat(result.getToleranceExceeded()).isTrue();
        verify(weighingRepository).save(any(WeighingEntity.class));
    }

    @Test
    @DisplayName("칭량 생성 - 실패 (중복 번호)")
    void testCreateWeighing_Fail_DuplicateNumber() {
        WeighingEntity newWeighing = WeighingEntity.builder()
                .tenant(testTenant)
                .weighingNo("WG-20260204-0001")
                .build();

        when(weighingRepository.existsByTenant_TenantIdAndWeighingNo(tenantId, "WG-20260204-0001"))
                .thenReturn(true);

        assertThatThrownBy(() -> weighingService.createWeighing(newWeighing))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // === Update Tests ===

    @Test
    @DisplayName("칭량 수정 - 성공")
    void testUpdateWeighing_Success() {
        WeighingEntity updates = WeighingEntity.builder()
                .tareWeight(new BigDecimal("55.000"))
                .grossWeight(new BigDecimal("1055.000"))
                .build();

        when(weighingRepository.findById(1L))
                .thenReturn(Optional.of(testWeighing));
        when(weighingRepository.save(any(WeighingEntity.class)))
                .thenReturn(testWeighing);
        when(weighingRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testWeighing));

        WeighingEntity result = weighingService.updateWeighing(1L, updates);

        assertThat(result).isNotNull();
        verify(weighingRepository).save(any(WeighingEntity.class));
    }

    @Test
    @DisplayName("칭량 수정 - 실패 (검증됨)")
    void testUpdateWeighing_Fail_AlreadyVerified() {
        testWeighing.setVerificationStatus("VERIFIED");
        WeighingEntity updates = WeighingEntity.builder().build();

        when(weighingRepository.findById(1L))
                .thenReturn(Optional.of(testWeighing));

        assertThatThrownBy(() -> weighingService.updateWeighing(1L, updates))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update verified");
    }

    // === Verification Tests ===

    @Test
    @DisplayName("칭량 검증 - 성공")
    void testVerifyWeighing_Success() {
        when(weighingRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testWeighing));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(testVerifier));
        when(weighingRepository.save(any(WeighingEntity.class)))
                .thenReturn(testWeighing);

        WeighingEntity result = weighingService.verifyWeighing(1L, 2L, "Verified OK");

        assertThat(result).isNotNull();
        verify(weighingRepository).save(any(WeighingEntity.class));
    }

    @Test
    @DisplayName("칭량 검증 - 실패 (검증자 = 작업자)")
    void testVerifyWeighing_Fail_SameUser() {
        when(weighingRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testWeighing));

        assertThatThrownBy(() -> weighingService.verifyWeighing(1L, 1L, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be the same as operator");
    }

    @Test
    @DisplayName("칭량 검증 - 실패 (이미 검증됨)")
    void testVerifyWeighing_Fail_AlreadyVerified() {
        testWeighing.setVerificationStatus("VERIFIED");
        when(weighingRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testWeighing));

        assertThatThrownBy(() -> weighingService.verifyWeighing(1L, 2L, "Test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not pending verification");
    }

    @Test
    @DisplayName("칭량 거부 - 성공")
    void testRejectWeighing_Success() {
        when(weighingRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testWeighing));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(testVerifier));
        when(weighingRepository.save(any(WeighingEntity.class)))
                .thenReturn(testWeighing);

        WeighingEntity result = weighingService.rejectWeighing(1L, 2L, "Measurement error");

        assertThat(result).isNotNull();
        verify(weighingRepository).save(any(WeighingEntity.class));
    }

    // === Delete Tests ===

    @Test
    @DisplayName("칭량 삭제 - 성공 (PENDING)")
    void testDeleteWeighing_Success_Pending() {
        when(weighingRepository.findById(1L))
                .thenReturn(Optional.of(testWeighing));

        weighingService.deleteWeighing(1L);

        verify(weighingRepository).delete(testWeighing);
    }

    @Test
    @DisplayName("칭량 삭제 - 성공 (REJECTED)")
    void testDeleteWeighing_Success_Rejected() {
        testWeighing.setVerificationStatus("REJECTED");
        when(weighingRepository.findById(1L))
                .thenReturn(Optional.of(testWeighing));

        weighingService.deleteWeighing(1L);

        verify(weighingRepository).delete(testWeighing);
    }

    @Test
    @DisplayName("칭량 삭제 - 실패 (VERIFIED)")
    void testDeleteWeighing_Fail_Verified() {
        testWeighing.setVerificationStatus("VERIFIED");
        when(weighingRepository.findById(1L))
                .thenReturn(Optional.of(testWeighing));

        assertThatThrownBy(() -> weighingService.deleteWeighing(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete verified");
    }

    // === Helper Method Tests ===

    @Test
    @DisplayName("참조로 칭량 생성 - 성공")
    void testCreateWeighingFromReference_Success() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testOperator));
        when(lotRepository.findById(1L))
                .thenReturn(Optional.of(testLot));
        when(weighingRepository.existsByTenant_TenantIdAndWeighingNo(anyString(), anyString()))
                .thenReturn(false);
        when(weighingRepository.save(any(WeighingEntity.class)))
                .thenReturn(testWeighing);
        when(weighingRepository.findByIdWithAllRelations(any()))
                .thenReturn(Optional.of(testWeighing));

        WeighingEntity result = weighingService.createWeighingFromReference(
                tenantId, "INCOMING", "GOODS_RECEIPT", 1L,
                1L, 1L,
                new BigDecimal("50.000"), new BigDecimal("1050.000"), new BigDecimal("1000.000"),
                1L, "Test remarks"
        );

        assertThat(result).isNotNull();
        verify(weighingRepository).save(any(WeighingEntity.class));
    }

    @Test
    @DisplayName("참조로 칭량 생성 - 실패 (제품 없음)")
    void testCreateWeighingFromReference_Fail_ProductNotFound() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> weighingService.createWeighingFromReference(
                tenantId, "INCOMING", "GOODS_RECEIPT", 1L,
                999L, null,
                new BigDecimal("50.000"), new BigDecimal("1050.000"), new BigDecimal("1000.000"),
                1L, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }
}
