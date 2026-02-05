package kr.co.softice.mes.api.controller;

import kr.co.softice.mes.common.dto.weighing.*;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.WeighingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Weighing Integration Test
 * 칭량 통합 테스트
 *
 * End-to-end workflow tests for weighing management
 *
 * @author Moon Myung-seop
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("칭량 통합 테스트")
class WeighingIntegrationTest {

    @Autowired
    private WeighingService weighingService;

    @Autowired
    private WeighingRepository weighingRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LotRepository lotRepository;

    private TenantEntity tenant;
    private ProductEntity product;
    private UserEntity operator;
    private UserEntity verifier;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST_TENANT";

        // Create tenant
        tenant = new TenantEntity();
        tenant.setTenantId(tenantId);
        tenant.setTenantName("Test Tenant");
        tenant = tenantRepository.save(tenant);

        // Create product
        product = new ProductEntity();
        product.setTenant(tenant);
        product.setProductCode("PROD001");
        product.setProductName("Test Product");
        product = productRepository.save(product);

        // Create operator
        operator = new UserEntity();
        operator.setTenant(tenant);
        operator.setUsername("operator1");
        operator.setEmail("operator1@test.com");
        operator.setName("Operator One");
        operator = userRepository.save(operator);

        // Create verifier
        verifier = new UserEntity();
        verifier.setTenant(tenant);
        verifier.setUsername("verifier1");
        verifier.setEmail("verifier1@test.com");
        verifier.setName("Verifier One");
        verifier = userRepository.save(verifier);
    }

    // === Complete Weighing Workflow Test ===

    @Test
    @DisplayName("완전한 칭량 워크플로우 테스트: 생성 → 검증")
    void testCompleteWeighingWorkflow() {
        // 1. Create weighing
        WeighingEntity weighing = WeighingEntity.builder()
                .tenant(tenant)
                .weighingDate(LocalDateTime.now())
                .weighingType("INCOMING")
                .referenceType("GOODS_RECEIPT")
                .referenceId(1L)
                .product(product)
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .expectedWeight(new BigDecimal("1000.000"))
                .operator(operator)
                .tolerancePercentage(new BigDecimal("2.0"))
                .remarks("Test weighing for incoming material")
                .build();

        WeighingEntity created = weighingService.createWeighing(weighing);

        // Verify created
        assertThat(created).isNotNull();
        assertThat(created.getWeighingId()).isNotNull();
        assertThat(created.getWeighingNo()).startsWith("WG-");
        assertThat(created.getNetWeight()).isEqualByComparingTo(new BigDecimal("1000.000"));
        assertThat(created.getVariance()).isEqualByComparingTo(new BigDecimal("0.000"));
        assertThat(created.getVariancePercentage()).isEqualByComparingTo(new BigDecimal("0.0000"));
        assertThat(created.getToleranceExceeded()).isFalse();
        assertThat(created.getVerificationStatus()).isEqualTo("PENDING");

        // 2. Verify weighing (GMP dual verification)
        WeighingEntity verified = weighingService.verifyWeighing(
                created.getWeighingId(),
                verifier.getUserId(),
                "Verified - measurements accurate"
        );

        // Verify verified
        assertThat(verified).isNotNull();
        assertThat(verified.getVerificationStatus()).isEqualTo("VERIFIED");
        assertThat(verified.getVerifier()).isNotNull();
        assertThat(verified.getVerifier().getUserId()).isEqualTo(verifier.getUserId());
        assertThat(verified.getVerificationDate()).isNotNull();

        // 3. Verify cannot be modified after verification
        WeighingEntity updates = WeighingEntity.builder()
                .tareWeight(new BigDecimal("55.000"))
                .build();

        assertThatThrownBy(() -> weighingService.updateWeighing(verified.getWeighingId(), updates))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update verified");
    }

    @Test
    @DisplayName("허용 오차 초과 워크플로우 테스트")
    void testToleranceExceededWorkflow() {
        // Create weighing with tolerance exceeded
        WeighingEntity weighing = WeighingEntity.builder()
                .tenant(tenant)
                .weighingDate(LocalDateTime.now())
                .weighingType("PRODUCTION")
                .referenceType("WORK_ORDER")
                .referenceId(1L)
                .product(product)
                .tareWeight(new BigDecimal("100.000"))
                .grossWeight(new BigDecimal("550.000"))
                .expectedWeight(new BigDecimal("500.000"))
                .operator(operator)
                .tolerancePercentage(new BigDecimal("2.0"))
                .remarks("Production weighing with variance")
                .build();

        WeighingEntity created = weighingService.createWeighing(weighing);

        // Verify tolerance exceeded
        // Net: 550 - 100 = 450
        // Variance: 450 - 500 = -50
        // Variance %: -50 / 500 * 100 = -10%
        // abs(-10%) > 2% => true
        assertThat(created.getNetWeight()).isEqualByComparingTo(new BigDecimal("450.000"));
        assertThat(created.getVariance()).isEqualByComparingTo(new BigDecimal("-50.000"));
        assertThat(created.getVariancePercentage()).isEqualByComparingTo(new BigDecimal("-10.0000"));
        assertThat(created.getToleranceExceeded()).isTrue();

        // Find tolerance exceeded weighings
        List<WeighingEntity> exceeded = weighingService.findToleranceExceeded(tenantId);
        assertThat(exceeded).hasSize(1);
        assertThat(exceeded.get(0).getWeighingId()).isEqualTo(created.getWeighingId());

        // Find unverified tolerance exceeded
        List<WeighingEntity> unverified = weighingService.findUnverifiedToleranceExceeded(tenantId);
        assertThat(unverified).hasSize(1);

        // Reject weighing due to excessive variance
        WeighingEntity rejected = weighingService.rejectWeighing(
                created.getWeighingId(),
                verifier.getUserId(),
                "Variance too high - re-measure required"
        );

        assertThat(rejected.getVerificationStatus()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("GMP 이중 검증 규칙 테스트")
    void testGMPDualVerificationRule() {
        // Create weighing
        WeighingEntity weighing = WeighingEntity.builder()
                .tenant(tenant)
                .weighingDate(LocalDateTime.now())
                .weighingType("SAMPLING")
                .product(product)
                .tareWeight(new BigDecimal("5.000"))
                .grossWeight(new BigDecimal("10.000"))
                .expectedWeight(new BigDecimal("5.000"))
                .operator(operator)
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        WeighingEntity created = weighingService.createWeighing(weighing);

        // Attempt to verify with same user as operator (should fail)
        assertThatThrownBy(() -> weighingService.verifyWeighing(
                created.getWeighingId(),
                operator.getUserId(),
                "Self-verification attempt"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be the same as operator");
    }

    @Test
    @DisplayName("칭량 수정 테스트 (PENDING 상태만)")
    void testUpdateWeighing() {
        // Create weighing
        WeighingEntity weighing = WeighingEntity.builder()
                .tenant(tenant)
                .weighingDate(LocalDateTime.now())
                .weighingType("OUTGOING")
                .product(product)
                .tareWeight(new BigDecimal("30.000"))
                .grossWeight(new BigDecimal("530.000"))
                .expectedWeight(new BigDecimal("500.000"))
                .operator(operator)
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        WeighingEntity created = weighingService.createWeighing(weighing);

        // Update tare and gross weight
        WeighingEntity updates = WeighingEntity.builder()
                .tareWeight(new BigDecimal("32.000"))
                .grossWeight(new BigDecimal("532.000"))
                .build();

        WeighingEntity updated = weighingService.updateWeighing(created.getWeighingId(), updates);

        // Verify update and recalculation
        assertThat(updated.getTareWeight()).isEqualByComparingTo(new BigDecimal("32.000"));
        assertThat(updated.getGrossWeight()).isEqualByComparingTo(new BigDecimal("532.000"));
        assertThat(updated.getNetWeight()).isEqualByComparingTo(new BigDecimal("500.000"));
        assertThat(updated.getVariance()).isEqualByComparingTo(new BigDecimal("0.000"));
        assertThat(updated.getToleranceExceeded()).isFalse();
    }

    @Test
    @DisplayName("칭량 삭제 테스트")
    void testDeleteWeighing() {
        // Create weighing
        WeighingEntity weighing = WeighingEntity.builder()
                .tenant(tenant)
                .weighingDate(LocalDateTime.now())
                .weighingType("INCOMING")
                .product(product)
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .operator(operator)
                .build();

        WeighingEntity created = weighingService.createWeighing(weighing);
        Long weighingId = created.getWeighingId();

        // Delete PENDING weighing (should succeed)
        weighingService.deleteWeighing(weighingId);

        // Verify deleted
        assertThat(weighingRepository.findById(weighingId)).isEmpty();
    }

    @Test
    @DisplayName("참조 문서별 칭량 조회 테스트")
    void testFindByReference() {
        // Create weighings for different references
        for (int i = 1; i <= 3; i++) {
            WeighingEntity weighing = WeighingEntity.builder()
                    .tenant(tenant)
                    .weighingDate(LocalDateTime.now())
                    .weighingType("INCOMING")
                    .referenceType("GOODS_RECEIPT")
                    .referenceId(1L)
                    .product(product)
                    .tareWeight(new BigDecimal("50.000"))
                    .grossWeight(new BigDecimal("1050.000"))
                    .operator(operator)
                    .build();
            weighingService.createWeighing(weighing);
        }

        // Find by reference
        List<WeighingEntity> weighings = weighingService.findByReference("GOODS_RECEIPT", 1L);

        assertThat(weighings).hasSize(3);
        assertThat(weighings).allMatch(w -> "GOODS_RECEIPT".equals(w.getReferenceType()));
        assertThat(weighings).allMatch(w -> Long.valueOf(1L).equals(w.getReferenceId()));
    }

    @Test
    @DisplayName("검증 대기 목록 조회 테스트")
    void testFindPendingVerification() {
        // Create pending weighings
        for (int i = 1; i <= 2; i++) {
            WeighingEntity weighing = WeighingEntity.builder()
                    .tenant(tenant)
                    .weighingDate(LocalDateTime.now())
                    .weighingType("PRODUCTION")
                    .product(product)
                    .tareWeight(new BigDecimal("50.000"))
                    .grossWeight(new BigDecimal("550.000"))
                    .operator(operator)
                    .build();
            weighingService.createWeighing(weighing);
        }

        // Find pending verification
        List<WeighingEntity> pending = weighingService.findPendingVerification(tenantId);

        assertThat(pending).hasSize(2);
        assertThat(pending).allMatch(w -> "PENDING".equals(w.getVerificationStatus()));
    }
}
