package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Weighing Repository Test
 * 칭량 리포지토리 테스트
 *
 * @author Moon Myung-seop
 */
@DataJpaTest
@DisplayName("칭량 리포지토리 테스트")
class WeighingRepositoryTest {

    @Autowired
    private WeighingRepository weighingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TenantEntity tenant;
    private ProductEntity product;
    private UserEntity operator;
    private UserEntity verifier;
    private WeighingEntity weighing1;
    private WeighingEntity weighing2;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        // Create tenant
        tenant = new TenantEntity();
        tenant.setTenantId(tenantId);
        tenant.setTenantName("Test Tenant");
        entityManager.persist(tenant);

        // Create product
        product = new ProductEntity();
        product.setTenant(tenant);
        product.setProductCode("PROD001");
        product.setProductName("Test Product");
        entityManager.persist(product);

        // Create users
        operator = new UserEntity();
        operator.setTenant(tenant);
        operator.setUsername("operator1");
        operator.setEmail("operator1@test.com");
        entityManager.persist(operator);

        verifier = new UserEntity();
        verifier.setTenant(tenant);
        verifier.setUsername("verifier1");
        verifier.setEmail("verifier1@test.com");
        entityManager.persist(verifier);

        // Create weighing 1 (verified, within tolerance)
        weighing1 = WeighingEntity.builder()
                .tenant(tenant)
                .weighingNo("WG-20260204-0001")
                .weighingDate(LocalDateTime.now().minusDays(1))
                .weighingType("INCOMING")
                .referenceType("GOODS_RECEIPT")
                .referenceId(1L)
                .product(product)
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .netWeight(new BigDecimal("1000.000"))
                .expectedWeight(new BigDecimal("1000.000"))
                .variance(new BigDecimal("0.000"))
                .variancePercentage(new BigDecimal("0.0000"))
                .unit("kg")
                .operator(operator)
                .verifier(verifier)
                .verificationDate(LocalDateTime.now().minusDays(1))
                .verificationStatus("VERIFIED")
                .toleranceExceeded(false)
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();
        entityManager.persist(weighing1);

        // Create weighing 2 (pending, tolerance exceeded)
        weighing2 = WeighingEntity.builder()
                .tenant(tenant)
                .weighingNo("WG-20260204-0002")
                .weighingDate(LocalDateTime.now())
                .weighingType("PRODUCTION")
                .referenceType("WORK_ORDER")
                .referenceId(1L)
                .product(product)
                .tareWeight(new BigDecimal("100.000"))
                .grossWeight(new BigDecimal("550.000"))
                .netWeight(new BigDecimal("450.000"))
                .expectedWeight(new BigDecimal("500.000"))
                .variance(new BigDecimal("-50.000"))
                .variancePercentage(new BigDecimal("-10.0000"))
                .unit("kg")
                .operator(operator)
                .verificationStatus("PENDING")
                .toleranceExceeded(true)
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();
        entityManager.persist(weighing2);

        entityManager.flush();
    }

    // === Basic CRUD Tests ===

    @Test
    @DisplayName("칭량 번호로 조회")
    void findByTenant_TenantIdAndWeighingNo() {
        // When
        Optional<WeighingEntity> result = weighingRepository.findByTenant_TenantIdAndWeighingNo(
                tenantId, "WG-20260204-0001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getWeighingNo()).isEqualTo("WG-20260204-0001");
        assertThat(result.get().getNetWeight()).isEqualByComparingTo("1000.000");
    }

    @Test
    @DisplayName("칭량 번호 존재 확인")
    void existsByTenant_TenantIdAndWeighingNo() {
        // When
        boolean exists = weighingRepository.existsByTenant_TenantIdAndWeighingNo(
                tenantId, "WG-20260204-0001");
        boolean notExists = weighingRepository.existsByTenant_TenantIdAndWeighingNo(
                tenantId, "WG-NONEXISTENT");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("테넌트별 전체 조회")
    void findByTenant_TenantId() {
        // When
        List<WeighingEntity> results = weighingRepository.findByTenant_TenantId(tenantId);

        // Then
        assertThat(results).hasSize(2);
    }

    // === Filter Tests ===

    @Test
    @DisplayName("칭량 유형별 조회")
    void findByTenant_TenantIdAndWeighingType() {
        // When
        List<WeighingEntity> incoming = weighingRepository.findByTenant_TenantIdAndWeighingType(
                tenantId, "INCOMING");
        List<WeighingEntity> production = weighingRepository.findByTenant_TenantIdAndWeighingType(
                tenantId, "PRODUCTION");

        // Then
        assertThat(incoming).hasSize(1);
        assertThat(production).hasSize(1);
        assertThat(incoming.get(0).getWeighingNo()).isEqualTo("WG-20260204-0001");
        assertThat(production.get(0).getWeighingNo()).isEqualTo("WG-20260204-0002");
    }

    @Test
    @DisplayName("검증 상태별 조회")
    void findByTenant_TenantIdAndVerificationStatus() {
        // When
        List<WeighingEntity> verified = weighingRepository.findByTenant_TenantIdAndVerificationStatus(
                tenantId, "VERIFIED");
        List<WeighingEntity> pending = weighingRepository.findByTenant_TenantIdAndVerificationStatus(
                tenantId, "PENDING");

        // Then
        assertThat(verified).hasSize(1);
        assertThat(pending).hasSize(1);
        assertThat(verified.get(0).getWeighingNo()).isEqualTo("WG-20260204-0001");
        assertThat(pending.get(0).getWeighingNo()).isEqualTo("WG-20260204-0002");
    }

    @Test
    @DisplayName("참조 타입 및 ID로 조회")
    void findByTenant_TenantIdAndReferenceTypeAndReferenceId() {
        // When
        List<WeighingEntity> goodsReceipt = weighingRepository
                .findByTenant_TenantIdAndReferenceTypeAndReferenceId(
                        tenantId, "GOODS_RECEIPT", 1L);
        List<WeighingEntity> workOrder = weighingRepository
                .findByTenant_TenantIdAndReferenceTypeAndReferenceId(
                        tenantId, "WORK_ORDER", 1L);

        // Then
        assertThat(goodsReceipt).hasSize(1);
        assertThat(workOrder).hasSize(1);
        assertThat(goodsReceipt.get(0).getWeighingType()).isEqualTo("INCOMING");
        assertThat(workOrder.get(0).getWeighingType()).isEqualTo("PRODUCTION");
    }

    @Test
    @DisplayName("제품별 조회")
    void findByTenant_TenantIdAndProduct_ProductId() {
        // When
        List<WeighingEntity> results = weighingRepository
                .findByTenant_TenantIdAndProduct_ProductId(tenantId, product.getProductId());

        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("작업자별 조회")
    void findByTenant_TenantIdAndOperator_UserId() {
        // When
        List<WeighingEntity> results = weighingRepository
                .findByTenant_TenantIdAndOperator_UserId(tenantId, operator.getUserId());

        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("날짜 범위로 조회")
    void findByTenant_TenantIdAndWeighingDateBetween() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<WeighingEntity> results = weighingRepository
                .findByTenant_TenantIdAndWeighingDateBetween(tenantId, startDate, endDate);

        // Then
        assertThat(results).hasSize(2);
    }

    // === Custom Query Tests ===

    @Test
    @DisplayName("관계 포함 전체 조회")
    void findByTenantIdWithAllRelations() {
        // When
        List<WeighingEntity> results = weighingRepository.findByTenantIdWithAllRelations(tenantId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTenant()).isNotNull();
        assertThat(results.get(0).getProduct()).isNotNull();
        assertThat(results.get(0).getOperator()).isNotNull();
    }

    @Test
    @DisplayName("ID로 관계 포함 조회")
    void findByIdWithAllRelations() {
        // When
        Optional<WeighingEntity> result = weighingRepository
                .findByIdWithAllRelations(weighing1.getWeighingId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTenant()).isNotNull();
        assertThat(result.get().getProduct()).isNotNull();
        assertThat(result.get().getOperator()).isNotNull();
        assertThat(result.get().getVerifier()).isNotNull();
    }

    @Test
    @DisplayName("참조로 관계 포함 조회")
    void findByReferenceTypeAndReferenceIdWithRelations() {
        // When
        List<WeighingEntity> results = weighingRepository
                .findByReferenceTypeAndReferenceIdWithRelations("GOODS_RECEIPT", 1L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTenant()).isNotNull();
        assertThat(results.get(0).getProduct()).isNotNull();
        assertThat(results.get(0).getOperator()).isNotNull();
    }

    @Test
    @DisplayName("제품 및 날짜 범위로 관계 포함 조회")
    void findByProductIdAndDateRangeWithRelations() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<WeighingEntity> results = weighingRepository
                .findByProductIdAndDateRangeWithRelations(
                        product.getProductId(), startDate, endDate);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTenant()).isNotNull();
        assertThat(results.get(0).getProduct()).isNotNull();
    }

    @Test
    @DisplayName("검증 상태로 관계 포함 조회")
    void findByVerificationStatusAndTenantWithRelations() {
        // When
        List<WeighingEntity> verified = weighingRepository
                .findByVerificationStatusAndTenantWithRelations(tenantId, "VERIFIED");
        List<WeighingEntity> pending = weighingRepository
                .findByVerificationStatusAndTenantWithRelations(tenantId, "PENDING");

        // Then
        assertThat(verified).hasSize(1);
        assertThat(pending).hasSize(1);
        assertThat(verified.get(0).getTenant()).isNotNull();
        assertThat(pending.get(0).getTenant()).isNotNull();
    }

    @Test
    @DisplayName("허용 오차 초과 조회")
    void findToleranceExceededWeighings() {
        // When
        List<WeighingEntity> results = weighingRepository
                .findToleranceExceededWeighings(tenantId);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getWeighingNo()).isEqualTo("WG-20260204-0002");
        assertThat(results.get(0).getToleranceExceeded()).isTrue();
        assertThat(results.get(0).getVariancePercentage())
                .isEqualByComparingTo(new BigDecimal("-10.0000"));
    }

    @Test
    @DisplayName("검증 대기 조회")
    void findPendingVerificationWeighings() {
        // When
        List<WeighingEntity> results = weighingRepository
                .findPendingVerificationWeighings(tenantId);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getWeighingNo()).isEqualTo("WG-20260204-0002");
        assertThat(results.get(0).getVerificationStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("유형 및 날짜 범위로 조회")
    void findByTypeAndDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<WeighingEntity> incoming = weighingRepository
                .findByTypeAndDateRange(tenantId, "INCOMING", startDate, endDate);
        List<WeighingEntity> production = weighingRepository
                .findByTypeAndDateRange(tenantId, "PRODUCTION", startDate, endDate);

        // Then
        assertThat(incoming).hasSize(1);
        assertThat(production).hasSize(1);
    }

    @Test
    @DisplayName("미검증 허용 오차 초과 조회 (긴급 주의 필요)")
    void findUnverifiedToleranceExceeded() {
        // When
        List<WeighingEntity> results = weighingRepository
                .findUnverifiedToleranceExceeded(tenantId);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getWeighingNo()).isEqualTo("WG-20260204-0002");
        assertThat(results.get(0).getToleranceExceeded()).isTrue();
        assertThat(results.get(0).getVerificationStatus()).isEqualTo("PENDING");
    }

    // === Entity Method Tests ===

    @Test
    @DisplayName("순중량 계산")
    void calculateNetWeight() {
        // Given
        WeighingEntity weighing = WeighingEntity.builder()
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .build();

        // When
        weighing.calculateNetWeight();

        // Then
        assertThat(weighing.getNetWeight()).isEqualByComparingTo("1000.000");
    }

    @Test
    @DisplayName("편차 계산")
    void calculateVariance() {
        // Given
        WeighingEntity weighing = WeighingEntity.builder()
                .netWeight(new BigDecimal("450.000"))
                .expectedWeight(new BigDecimal("500.000"))
                .build();

        // When
        weighing.calculateVariance();

        // Then
        assertThat(weighing.getVariance()).isEqualByComparingTo("-50.000");
        assertThat(weighing.getVariancePercentage()).isEqualByComparingTo("-10.0000");
    }

    @Test
    @DisplayName("허용 오차 확인")
    void checkTolerance() {
        // Given
        WeighingEntity weighing = WeighingEntity.builder()
                .variancePercentage(new BigDecimal("5.0"))
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        // When
        weighing.checkTolerance();

        // Then
        assertThat(weighing.getToleranceExceeded()).isTrue();
    }

    @Test
    @DisplayName("전체 계산 수행")
    void performCalculations() {
        // Given
        WeighingEntity weighing = WeighingEntity.builder()
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .expectedWeight(new BigDecimal("1000.000"))
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        // When
        weighing.performCalculations();

        // Then
        assertThat(weighing.getNetWeight()).isEqualByComparingTo("1000.000");
        assertThat(weighing.getVariance()).isEqualByComparingTo("0.000");
        assertThat(weighing.getVariancePercentage()).isEqualByComparingTo("0.0000");
        assertThat(weighing.getToleranceExceeded()).isFalse();
    }
}
