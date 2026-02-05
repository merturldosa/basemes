package kr.co.softice.mes.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.softice.mes.common.dto.weighing.WeighingCreateRequest;
import kr.co.softice.mes.common.dto.weighing.WeighingVerificationRequest;
import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.entity.WeighingEntity;
import kr.co.softice.mes.domain.repository.WeighingRepository;
import kr.co.softice.mes.integration.BaseIntegrationTest;
import kr.co.softice.mes.integration.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Weighing API Integration Test
 * Tests REST API endpoints for weighing management
 * @author Moon Myung-seop
 */
@DisplayName("Weighing API Integration Tests")
public class WeighingApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeighingRepository weighingRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private ProductEntity testProduct;
    private UserEntity verifierUser;

    @BeforeEach
    public void setUpWeighingApi() {
        testProduct = testDataFactory.createProduct(testTenant, "PROD-API-001");
        verifierUser = testDataFactory.createUser(testTenant, "verifier-api");
    }

    @Test
    @DisplayName("POST /api/weighings - Create weighing")
    public void testCreateWeighing() throws Exception {
        // Given
        WeighingCreateRequest request = new WeighingCreateRequest();
        request.setWeighingNo("WG-API-001");
        request.setWeighingType("INCOMING");
        request.setProductId(testProduct.getProductId());
        request.setTareWeight(new BigDecimal("100.500"));
        request.setGrossWeight(new BigDecimal("1500.300"));
        request.setExpectedWeight(new BigDecimal("1400.000"));
        request.setUnit("kg");
        request.setOperatorUserId(testUser.getUserId());

        // When & Then
        mockMvc.perform(post("/api/weighings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-ID", testTenantId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weighingNo").value("WG-API-001"))
                .andExpect(jsonPath("$.data.netWeight").value(1399.800))
                .andExpect(jsonPath("$.data.variance").exists())
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/weighings - List all weighings")
    public void testGetAllWeighings() throws Exception {
        // Given - Create test weighings
        for (int i = 1; i <= 3; i++) {
            WeighingEntity weighing = testDataFactory.createWeighing(
                    testTenant,
                    testProduct,
                    testUser,
                    "WG-API-00" + i,
                    new BigDecimal("100.000"),
                    new BigDecimal("1500.000")
            );
            weighingRepository.save(weighing);
        }

        // When & Then
        mockMvc.perform(get("/api/weighings")
                .header("X-Tenant-ID", testTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("GET /api/weighings/{id} - Get weighing by ID")
    public void testGetWeighingById() throws Exception {
        // Given
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-API-004",
                new BigDecimal("100.000"),
                new BigDecimal("1500.000")
        );
        WeighingEntity saved = weighingRepository.save(weighing);

        // When & Then
        mockMvc.perform(get("/api/weighings/{id}", saved.getWeighingId())
                .header("X-Tenant-ID", testTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weighingId").value(saved.getWeighingId()))
                .andExpect(jsonPath("$.data.weighingNo").value("WG-API-004"));
    }

    @Test
    @DisplayName("POST /api/weighings/{id}/verify - Verify weighing")
    public void testVerifyWeighing() throws Exception {
        // Given
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-API-005",
                new BigDecimal("100.000"),
                new BigDecimal("1500.000")
        );
        WeighingEntity saved = weighingRepository.save(weighing);

        WeighingVerificationRequest request = new WeighingVerificationRequest();
        request.setVerifierUserId(verifierUser.getUserId());
        request.setRemarks("Verified through API");

        // When & Then
        mockMvc.perform(post("/api/weighings/{id}/verify", saved.getWeighingId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-ID", testTenantId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.verifier.username").value("verifier-api"));
    }

    @Test
    @DisplayName("GET /api/weighings/tolerance-exceeded - Get tolerance exceeded weighings")
    public void testGetToleranceExceededWeighings() throws Exception {
        // Given - Create weighing with large variance
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-API-006",
                new BigDecimal("100.000"),
                new BigDecimal("2000.000")
        );
        weighing.setExpectedWeight(new BigDecimal("1400.000"));
        weighing.performCalculations();
        weighingRepository.save(weighing);

        // When & Then
        mockMvc.perform(get("/api/weighings/tolerance-exceeded")
                .header("X-Tenant-ID", testTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("POST /api/weighings - Validation error for missing required fields")
    public void testCreateWeighingValidationError() throws Exception {
        // Given - Invalid request (missing required fields)
        WeighingCreateRequest request = new WeighingCreateRequest();
        request.setWeighingType("INCOMING");
        // Missing productId, tareWeight, grossWeight

        // When & Then
        mockMvc.perform(post("/api/weighings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-ID", testTenantId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/weighings/{id} - Not found error")
    public void testGetWeighingNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/weighings/{id}", 999999L)
                .header("X-Tenant-ID", testTenantId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/weighings/{id} - Delete weighing")
    public void testDeleteWeighing() throws Exception {
        // Given
        WeighingEntity weighing = testDataFactory.createWeighing(
                testTenant,
                testProduct,
                testUser,
                "WG-API-007",
                new BigDecimal("100.000"),
                new BigDecimal("1500.000")
        );
        WeighingEntity saved = weighingRepository.save(weighing);

        // When & Then
        mockMvc.perform(delete("/api/weighings/{id}", saved.getWeighingId())
                .header("X-Tenant-ID", testTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify deletion
        mockMvc.perform(get("/api/weighings/{id}", saved.getWeighingId())
                .header("X-Tenant-ID", testTenantId))
                .andExpect(status().isNotFound());
    }
}
