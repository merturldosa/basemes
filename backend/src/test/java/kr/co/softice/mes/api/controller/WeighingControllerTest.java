package kr.co.softice.mes.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.softice.mes.common.config.CorsProperties;
import kr.co.softice.mes.common.dto.weighing.*;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.common.security.TenantInterceptor;
import kr.co.softice.mes.domain.service.WeighingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Weighing Controller Test
 * 칭량 컨트롤러 단위 테스트
 *
 * @author Moon Myung-seop
 */
@WebMvcTest(controllers = WeighingController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        kr.co.softice.mes.common.config.SecurityConfig.class,
                        kr.co.softice.mes.common.config.WebMvcConfig.class,
                        kr.co.softice.mes.common.security.JwtAuthenticationFilter.class
                }))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("칭량 컨트롤러 테스트")
class WeighingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WeighingService weighingService;

    private static final String TENANT_ID = "TENANT001";
    private static final String WEIGHING_NO = "WG-20260204-0001";
    private static final Long WEIGHING_ID = 1L;
    private static final Long PRODUCT_ID = 1L;
    private static final Long OPERATOR_USER_ID = 1L;
    private static final Long VERIFIER_USER_ID = 2L;

    private WeighingResponse weighingResponse;
    private WeighingCreateRequest createRequest;
    private WeighingUpdateRequest updateRequest;
    private WeighingVerificationRequest verificationRequest;

    @BeforeEach
    void setUp() {
        // Create sample WeighingResponse
        weighingResponse = WeighingResponse.builder()
                .weighingId(WEIGHING_ID)
                .weighingNo(WEIGHING_NO)
                .tenantId(TENANT_ID)
                .weighingDate(LocalDateTime.now())
                .weighingType("INCOMING")
                .referenceType("GOODS_RECEIPT")
                .referenceId(1L)
                .productId(PRODUCT_ID)
                .productCode("PROD001")
                .productName("Test Product")
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .netWeight(new BigDecimal("1000.000"))
                .expectedWeight(new BigDecimal("1000.000"))
                .variance(new BigDecimal("0.000"))
                .variancePercentage(new BigDecimal("0.0000"))
                .unit("kg")
                .operatorUserId(OPERATOR_USER_ID)
                .operatorUsername("operator1")
                .operatorName("Operator User")
                .verificationStatus("PENDING")
                .toleranceExceeded(false)
                .tolerancePercentage(new BigDecimal("2.0"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create sample WeighingCreateRequest
        createRequest = WeighingCreateRequest.builder()
                .weighingType("INCOMING")
                .referenceType("GOODS_RECEIPT")
                .referenceId(1L)
                .productId(PRODUCT_ID)
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .expectedWeight(new BigDecimal("1000.000"))
                .unit("kg")
                .operatorUserId(OPERATOR_USER_ID)
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        // Create sample WeighingUpdateRequest
        updateRequest = WeighingUpdateRequest.builder()
                .grossWeight(new BigDecimal("1060.000"))
                .remarks("Updated remarks")
                .build();

        // Create sample WeighingVerificationRequest
        verificationRequest = WeighingVerificationRequest.builder()
                .verifierUserId(VERIFIER_USER_ID)
                .action("VERIFY")
                .remarks("Verified successfully")
                .build();
    }

    // === GET /api/weighings - List weighings ===

    @Test
    @DisplayName("칭량 목록 조회 - 성공")
    void getWeighings_Success() throws Exception {
        // Given
        List<WeighingResponse> responses = Arrays.asList(weighingResponse);
        when(weighingService.getAllWeighings(TENANT_ID)).thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("칭량 목록 조회 성공"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].weighingNo").value(WEIGHING_NO))
                    .andExpect(jsonPath("$.data[0].productId").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.data[0].verificationStatus").value("PENDING"));

            verify(weighingService).getAllWeighings(TENANT_ID);
        }
    }

    @Test
    @DisplayName("칭량 목록 조회 - 칭량 유형 필터")
    void getWeighings_FilterByWeighingType() throws Exception {
        // Given
        List<WeighingResponse> responses = Arrays.asList(weighingResponse);
        when(weighingService.getAllWeighings(TENANT_ID)).thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings")
                            .param("weighingType", "INCOMING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].weighingType").value("INCOMING"));

            verify(weighingService).getAllWeighings(TENANT_ID);
        }
    }

    @Test
    @DisplayName("칭량 목록 조회 - 검증 상태 필터")
    void getWeighings_FilterByVerificationStatus() throws Exception {
        // Given
        List<WeighingResponse> responses = Arrays.asList(weighingResponse);
        when(weighingService.getAllWeighings(TENANT_ID)).thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings")
                            .param("verificationStatus", "PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].verificationStatus").value("PENDING"));

            verify(weighingService).getAllWeighings(TENANT_ID);
        }
    }

    @Test
    @DisplayName("칭량 목록 조회 - 제품 필터")
    void getWeighings_FilterByProductId() throws Exception {
        // Given
        List<WeighingResponse> responses = Arrays.asList(weighingResponse);
        when(weighingService.getAllWeighings(TENANT_ID)).thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings")
                            .param("productId", String.valueOf(PRODUCT_ID)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].productId").value(PRODUCT_ID));

            verify(weighingService).getAllWeighings(TENANT_ID);
        }
    }

    @Test
    @DisplayName("칭량 목록 조회 - 빈 목록")
    void getWeighings_EmptyList() throws Exception {
        // Given
        when(weighingService.getAllWeighings(TENANT_ID)).thenReturn(Collections.emptyList());

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(0)));

            verify(weighingService).getAllWeighings(TENANT_ID);
        }
    }

    // === GET /api/weighings/{id} - Get weighing by ID ===

    @Test
    @DisplayName("칭량 상세 조회 - 성공")
    void getWeighing_Success() throws Exception {
        // Given
        when(weighingService.getWeighingById(TENANT_ID, WEIGHING_ID)).thenReturn(weighingResponse);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings/{id}", WEIGHING_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("칭량 조회 성공"))
                    .andExpect(jsonPath("$.data.weighingId").value(WEIGHING_ID))
                    .andExpect(jsonPath("$.data.weighingNo").value(WEIGHING_NO))
                    .andExpect(jsonPath("$.data.netWeight").value(1000.000));

            verify(weighingService).getWeighingById(TENANT_ID, WEIGHING_ID);
        }
    }

    @Test
    @DisplayName("칭량 상세 조회 - 존재하지 않음")
    void getWeighing_NotFound() throws Exception {
        // Given
        when(weighingService.getWeighingById(TENANT_ID, 999L))
                .thenThrow(new BusinessException(ErrorCode.WEIGHING_NOT_FOUND, "Weighing not found: 999"));

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings/{id}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(weighingService).getWeighingById(TENANT_ID, 999L);
        }
    }

    // === GET /api/weighings/tolerance-exceeded ===

    @Test
    @DisplayName("허용 오차 초과 칭량 조회 - 성공")
    void getToleranceExceeded_Success() throws Exception {
        // Given
        WeighingResponse exceededResponse = WeighingResponse.builder()
                .weighingId(2L)
                .weighingNo("WG-20260204-0002")
                .tenantId(TENANT_ID)
                .toleranceExceeded(true)
                .variancePercentage(new BigDecimal("-10.0000"))
                .verificationStatus("PENDING")
                .build();
        List<WeighingResponse> responses = Arrays.asList(exceededResponse);
        when(weighingService.getToleranceExceededWeighings(TENANT_ID)).thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings/tolerance-exceeded"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("허용 오차 초과 칭량 조회 성공"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].toleranceExceeded").value(true));

            verify(weighingService).getToleranceExceededWeighings(TENANT_ID);
        }
    }

    // === GET /api/weighings/pending-verification ===

    @Test
    @DisplayName("검증 대기 칭량 조회 - 성공")
    void getPendingVerification_Success() throws Exception {
        // Given
        List<WeighingResponse> responses = Arrays.asList(weighingResponse);
        when(weighingService.getPendingVerificationWeighings(TENANT_ID)).thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings/pending-verification"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("검증 대기 칭량 조회 성공"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].verificationStatus").value("PENDING"));

            verify(weighingService).getPendingVerificationWeighings(TENANT_ID);
        }
    }

    // === GET /api/weighings/unverified-tolerance-exceeded ===

    @Test
    @DisplayName("미검증 허용 오차 초과 칭량 조회 - 성공")
    void getUnverifiedToleranceExceeded_Success() throws Exception {
        // Given
        WeighingResponse exceededResponse = WeighingResponse.builder()
                .weighingId(2L)
                .weighingNo("WG-20260204-0002")
                .tenantId(TENANT_ID)
                .toleranceExceeded(true)
                .variancePercentage(new BigDecimal("-10.0000"))
                .verificationStatus("PENDING")
                .build();
        List<WeighingResponse> responses = Arrays.asList(exceededResponse);
        when(weighingService.getToleranceExceededWeighings(TENANT_ID)).thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings/unverified-tolerance-exceeded"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("미검증 허용 오차 초과 칭량 조회 성공"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].toleranceExceeded").value(true))
                    .andExpect(jsonPath("$.data[0].verificationStatus").value("PENDING"));

            verify(weighingService).getToleranceExceededWeighings(TENANT_ID);
        }
    }

    @Test
    @DisplayName("미검증 허용 오차 초과 칭량 조회 - 검증 완료된 항목 제외")
    void getUnverifiedToleranceExceeded_ExcludesVerified() throws Exception {
        // Given
        WeighingResponse verifiedResponse = WeighingResponse.builder()
                .weighingId(2L)
                .weighingNo("WG-20260204-0002")
                .tenantId(TENANT_ID)
                .toleranceExceeded(true)
                .verificationStatus("VERIFIED")
                .build();
        List<WeighingResponse> responses = Arrays.asList(verifiedResponse);
        when(weighingService.getToleranceExceededWeighings(TENANT_ID)).thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings/unverified-tolerance-exceeded"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(0)));

            verify(weighingService).getToleranceExceededWeighings(TENANT_ID);
        }
    }

    // === GET /api/weighings/reference/{type}/{id} ===

    @Test
    @DisplayName("참조 문서별 칭량 조회 - 성공")
    void getWeighingsByReference_Success() throws Exception {
        // Given
        List<WeighingResponse> responses = Arrays.asList(weighingResponse);
        when(weighingService.getWeighingsByReference(TENANT_ID, "GOODS_RECEIPT", 1L))
                .thenReturn(responses);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(get("/api/weighings/reference/{type}/{id}", "GOODS_RECEIPT", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("참조 문서 칭량 조회 성공"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].referenceType").value("GOODS_RECEIPT"))
                    .andExpect(jsonPath("$.data[0].referenceId").value(1));

            verify(weighingService).getWeighingsByReference(TENANT_ID, "GOODS_RECEIPT", 1L);
        }
    }

    // === POST /api/weighings - Create weighing ===

    @Test
    @DisplayName("칭량 생성 - 성공")
    void createWeighing_Success() throws Exception {
        // Given
        when(weighingService.createWeighing(eq(TENANT_ID), any(WeighingCreateRequest.class)))
                .thenReturn(weighingResponse);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(post("/api/weighings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("칭량 생성 성공"))
                    .andExpect(jsonPath("$.data.weighingNo").value(WEIGHING_NO))
                    .andExpect(jsonPath("$.data.netWeight").value(1000.000));

            verify(weighingService).createWeighing(eq(TENANT_ID), any(WeighingCreateRequest.class));
        }
    }

    @Test
    @DisplayName("칭량 생성 - 유효성 검사 실패 (칭량 유형 누락)")
    void createWeighing_ValidationFailed_MissingWeighingType() throws Exception {
        // Given
        WeighingCreateRequest invalidRequest = WeighingCreateRequest.builder()
                .productId(PRODUCT_ID)
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .unit("kg")
                .operatorUserId(OPERATOR_USER_ID)
                .build();

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(post("/api/weighings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(weighingService, never()).createWeighing(anyString(), any(WeighingCreateRequest.class));
        }
    }

    @Test
    @DisplayName("칭량 생성 - 유효성 검사 실패 (제품 ID 누락)")
    void createWeighing_ValidationFailed_MissingProductId() throws Exception {
        // Given
        WeighingCreateRequest invalidRequest = WeighingCreateRequest.builder()
                .weighingType("INCOMING")
                .tareWeight(new BigDecimal("50.000"))
                .grossWeight(new BigDecimal("1050.000"))
                .unit("kg")
                .operatorUserId(OPERATOR_USER_ID)
                .build();

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(post("/api/weighings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(weighingService, never()).createWeighing(anyString(), any(WeighingCreateRequest.class));
        }
    }

    @Test
    @DisplayName("칭량 생성 - 유효성 검사 실패 (총 중량이 용기 중량보다 작음)")
    void createWeighing_ValidationFailed_GrossWeightLessThanTareWeight() throws Exception {
        // Given
        WeighingCreateRequest invalidRequest = WeighingCreateRequest.builder()
                .weighingType("INCOMING")
                .productId(PRODUCT_ID)
                .tareWeight(new BigDecimal("1000.000"))
                .grossWeight(new BigDecimal("500.000"))
                .unit("kg")
                .operatorUserId(OPERATOR_USER_ID)
                .build();

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(post("/api/weighings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(weighingService, never()).createWeighing(anyString(), any(WeighingCreateRequest.class));
        }
    }

    // === PUT /api/weighings/{id} - Update weighing ===

    @Test
    @DisplayName("칭량 수정 - 성공")
    void updateWeighing_Success() throws Exception {
        // Given
        WeighingResponse updatedResponse = WeighingResponse.builder()
                .weighingId(WEIGHING_ID)
                .weighingNo(WEIGHING_NO)
                .tenantId(TENANT_ID)
                .grossWeight(new BigDecimal("1060.000"))
                .netWeight(new BigDecimal("1010.000"))
                .verificationStatus("PENDING")
                .remarks("Updated remarks")
                .build();

        when(weighingService.updateWeighing(eq(TENANT_ID), eq(WEIGHING_ID), any(WeighingUpdateRequest.class)))
                .thenReturn(updatedResponse);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(put("/api/weighings/{id}", WEIGHING_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("칭량 수정 성공"))
                    .andExpect(jsonPath("$.data.weighingId").value(WEIGHING_ID))
                    .andExpect(jsonPath("$.data.netWeight").value(1010.000));

            verify(weighingService).updateWeighing(eq(TENANT_ID), eq(WEIGHING_ID), any(WeighingUpdateRequest.class));
        }
    }

    @Test
    @DisplayName("칭량 수정 - 존재하지 않음")
    void updateWeighing_NotFound() throws Exception {
        // Given
        when(weighingService.updateWeighing(eq(TENANT_ID), eq(999L), any(WeighingUpdateRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.WEIGHING_NOT_FOUND, "Weighing not found: 999"));

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(put("/api/weighings/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(weighingService).updateWeighing(eq(TENANT_ID), eq(999L), any(WeighingUpdateRequest.class));
        }
    }

    // === POST /api/weighings/{id}/verify - Verify weighing ===

    @Test
    @DisplayName("칭량 검증 - 성공 (VERIFY)")
    void verifyWeighing_Success_Verify() throws Exception {
        // Given
        WeighingResponse verifiedResponse = WeighingResponse.builder()
                .weighingId(WEIGHING_ID)
                .weighingNo(WEIGHING_NO)
                .tenantId(TENANT_ID)
                .verifierUserId(VERIFIER_USER_ID)
                .verifierUsername("verifier1")
                .verificationStatus("VERIFIED")
                .verificationDate(LocalDateTime.now())
                .build();

        when(weighingService.verifyWeighing(eq(TENANT_ID), eq(WEIGHING_ID), any(WeighingVerificationRequest.class)))
                .thenReturn(verifiedResponse);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(post("/api/weighings/{id}/verify", WEIGHING_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verificationRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("칭량 검증 성공"))
                    .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                    .andExpect(jsonPath("$.data.verifierUserId").value(VERIFIER_USER_ID));

            verify(weighingService).verifyWeighing(eq(TENANT_ID), eq(WEIGHING_ID), any(WeighingVerificationRequest.class));
        }
    }

    @Test
    @DisplayName("칭량 검증 - 성공 (REJECT)")
    void verifyWeighing_Success_Reject() throws Exception {
        // Given
        WeighingVerificationRequest rejectRequest = WeighingVerificationRequest.builder()
                .verifierUserId(VERIFIER_USER_ID)
                .action("REJECT")
                .remarks("Rejected due to tolerance exceeded")
                .build();

        WeighingResponse rejectedResponse = WeighingResponse.builder()
                .weighingId(WEIGHING_ID)
                .weighingNo(WEIGHING_NO)
                .tenantId(TENANT_ID)
                .verifierUserId(VERIFIER_USER_ID)
                .verificationStatus("REJECTED")
                .verificationDate(LocalDateTime.now())
                .build();

        when(weighingService.verifyWeighing(eq(TENANT_ID), eq(WEIGHING_ID), any(WeighingVerificationRequest.class)))
                .thenReturn(rejectedResponse);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(post("/api/weighings/{id}/verify", WEIGHING_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rejectRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("칭량 거부 성공"))
                    .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"));

            verify(weighingService).verifyWeighing(eq(TENANT_ID), eq(WEIGHING_ID), any(WeighingVerificationRequest.class));
        }
    }

    @Test
    @DisplayName("칭량 검증 - 유효성 검사 실패 (검증자 ID 누락)")
    void verifyWeighing_ValidationFailed_MissingVerifierId() throws Exception {
        // Given
        WeighingVerificationRequest invalidRequest = WeighingVerificationRequest.builder()
                .action("VERIFY")
                .build();

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(post("/api/weighings/{id}/verify", WEIGHING_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(weighingService, never()).verifyWeighing(anyString(), anyLong(), any(WeighingVerificationRequest.class));
        }
    }

    @Test
    @DisplayName("칭량 검증 - 유효성 검사 실패 (잘못된 액션)")
    void verifyWeighing_ValidationFailed_InvalidAction() throws Exception {
        // Given
        WeighingVerificationRequest invalidRequest = WeighingVerificationRequest.builder()
                .verifierUserId(VERIFIER_USER_ID)
                .action("INVALID_ACTION")
                .build();

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(post("/api/weighings/{id}/verify", WEIGHING_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(weighingService, never()).verifyWeighing(anyString(), anyLong(), any(WeighingVerificationRequest.class));
        }
    }

    // === DELETE /api/weighings/{id} - Delete weighing ===

    @Test
    @DisplayName("칭량 삭제 - 성공")
    void deleteWeighing_Success() throws Exception {
        // Given
        doNothing().when(weighingService).deleteWeighing(TENANT_ID, WEIGHING_ID);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(delete("/api/weighings/{id}", WEIGHING_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("칭량 삭제 성공"));

            verify(weighingService).deleteWeighing(TENANT_ID, WEIGHING_ID);
        }
    }

    @Test
    @DisplayName("칭량 삭제 - 존재하지 않음")
    void deleteWeighing_NotFound() throws Exception {
        // Given
        doThrow(new BusinessException(ErrorCode.WEIGHING_NOT_FOUND, "Weighing not found: 999"))
                .when(weighingService).deleteWeighing(TENANT_ID, 999L);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

            // When & Then
            mockMvc.perform(delete("/api/weighings/{id}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(weighingService).deleteWeighing(TENANT_ID, 999L);
        }
    }
}
