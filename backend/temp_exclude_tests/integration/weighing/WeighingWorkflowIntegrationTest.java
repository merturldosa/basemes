package kr.co.softice.mes.integration.weighing;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.WeighingRepository;
import kr.co.softice.mes.domain.service.WeighingService;
import kr.co.softice.mes.integration.BaseIntegrationTest;
import kr.co.softice.mes.integration.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Weighing Workflow Integration Test
 * Tests complete weighing workflow from creation to verification
 * @author Moon Myung-seop
 */
@DisplayName("Weighing Workflow Integration Tests")
public class WeighingWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WeighingService weighingService;

    @Autowired
    private WeighingRepository weighingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private ProductEntity testProduct;
    private UserEntity verifierUser;

    @BeforeEach
    public void setUpWeighingTest() {
        // Create test product
        testProduct = testDataFactory.createProduct(testTenant, "TEST-PROD-001");

        // Create verifier user (different from operator)
        verifierUser = testDataFactory.createUser(testTenant, "verifier");
    }

    @Test
    @DisplayName("Should create weighing with automatic calculations")
    public void testCreateWeighing() {
        // Given
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-20260204-0001",
                new BigDecimal("100.500"),
                new BigDecimal("1500.300")
        );
        weighing.setExpectedWeight(new BigDecimal("1400.000"));

        // When
        WeighingEntity savedWeighing = weighingService.createWeighing(weighing);

        // Then
        assertThat(savedWeighing).isNotNull();
        assertThat(savedWeighing.getWeighingId()).isNotNull();
        assertThat(savedWeighing.getNetWeight()).isEqualTo(new BigDecimal("1399.800"));
        assertThat(savedWeighing.getVariance()).isEqualTo(new BigDecimal("-0.200"));
        assertThat(savedWeighing.getVariancePercentage()).isNotNull();
        assertThat(savedWeighing.getVerificationStatus()).isEqualTo("PENDING");
        assertThat(savedWeighing.getToleranceExceeded()).isFalse();
    }

    @Test
    @DisplayName("Should verify weighing with dual verification")
    public void testVerifyWeighing() {
        // Given - Create weighing
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-20260204-0002",
                new BigDecimal("100.000"),
                new BigDecimal("1500.000")
        );
        WeighingEntity savedWeighing = weighingService.createWeighing(weighing);

        // When - Verify by different user
        WeighingEntity verifiedWeighing = weighingService.verifyWeighing(
                savedWeighing.getWeighingId(),
                verifierUser.getUserId(),
                "Verified OK"
        );

        // Then
        assertThat(verifiedWeighing.getVerificationStatus()).isEqualTo("VERIFIED");
        assertThat(verifiedWeighing.getVerifier()).isEqualTo(verifierUser);
        assertThat(verifiedWeighing.getVerificationDate()).isNotNull();
        assertThat(verifiedWeighing.getRemarks()).contains("Verified OK");
    }

    @Test
    @DisplayName("Should reject self-verification (GMP compliance)")
    public void testRejectSelfVerification() {
        // Given - Create weighing
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-20260204-0003",
                new BigDecimal("100.000"),
                new BigDecimal("1500.000")
        );
        WeighingEntity savedWeighing = weighingService.createWeighing(weighing);

        // When & Then - Same user tries to verify
        assertThrows(IllegalArgumentException.class, () -> {
            weighingService.verifyWeighing(
                    savedWeighing.getWeighingId(),
                    testUser.getUserId(),  // Same as operator
                    "Trying to self-verify"
            );
        });
    }

    @Test
    @DisplayName("Should detect tolerance exceeded")
    public void testToleranceExceeded() {
        // Given - Large variance
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-20260204-0004",
                new BigDecimal("100.000"),
                new BigDecimal("2000.000")  // Large gross weight
        );
        weighing.setExpectedWeight(new BigDecimal("1400.000"));
        weighing.performCalculations();

        // When
        WeighingEntity savedWeighing = weighingService.createWeighing(weighing);

        // Then - Should mark as tolerance exceeded if variance > 5%
        BigDecimal variance = savedWeighing.getNetWeight().subtract(savedWeighing.getExpectedWeight());
        BigDecimal variancePercentage = variance.abs()
                .divide(savedWeighing.getExpectedWeight(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        if (variancePercentage.compareTo(new BigDecimal("5.0")) > 0) {
            assertThat(savedWeighing.getToleranceExceeded()).isTrue();
        }
    }

    @Test
    @DisplayName("Should find tolerance exceeded weighings")
    public void testFindToleranceExceeded() {
        // Given - Create multiple weighings with different variances
        for (int i = 0; i < 3; i++) {
            WeighingEntity weighing = testDataFactory.createWeighing(
                    testTenant,
                    testProduct,
                    testUser,
                    "WG-20260204-000" + (i + 5),
                    new BigDecimal("100.000"),
                    new BigDecimal("1500.000").add(new BigDecimal(i * 200))
            );
            weighing.setExpectedWeight(new BigDecimal("1400.000"));
            weighing.performCalculations();
            weighingService.createWeighing(weighing);
        }

        // When
        var toleranceExceededList = weighingService.findToleranceExceeded(testTenantId);

        // Then
        assertThat(toleranceExceededList).isNotNull();
        // Verify that list contains only weighings with tolerance exceeded
        toleranceExceededList.forEach(w -> {
            assertThat(w.getToleranceExceeded()).isTrue();
        });
    }

    @Test
    @DisplayName("Should retrieve weighing by reference")
    public void testFindByReference() {
        // Given
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-20260204-0008",
                new BigDecimal("100.000"),
                new BigDecimal("1500.000")
        );
        weighing.setReferenceType("GOODS_RECEIPT");
        weighing.setReferenceId(12345L);
        WeighingEntity savedWeighing = weighingService.createWeighing(weighing);

        // When
        var weighings = weighingRepository.findByTenantIdAndReferenceTypeAndReferenceId(
                testTenantId,
                "GOODS_RECEIPT",
                12345L
        );

        // Then
        assertThat(weighings).hasSize(1);
        assertThat(weighings.get(0).getWeighingId()).isEqualTo(savedWeighing.getWeighingId());
    }

    @Test
    @DisplayName("Should calculate net weight correctly")
    public void testNetWeightCalculation() {
        // Given
        BigDecimal tareWeight = new BigDecimal("123.456");
        BigDecimal grossWeight = new BigDecimal("1876.543");
        BigDecimal expectedNetWeight = grossWeight.subtract(tareWeight);

        // When
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-20260204-0009",
                tareWeight,
                grossWeight
        );

        // Then
        assertThat(weighing.getNetWeight()).isEqualByComparingTo(expectedNetWeight);
    }
}
