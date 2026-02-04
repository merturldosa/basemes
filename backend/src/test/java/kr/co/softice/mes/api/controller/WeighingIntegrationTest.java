package kr.co.softice.mes.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.softice.mes.common.dto.weighing.*;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.config.TestSecurityConfig;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Weighing Integration Test
 * 칭량 워크플로우 완전한 통합 테스트
 *
 * 테스트 시나리오:
 * 1. 칭량 생성 워크플로우 (자동 계산 검증)
 * 2. 이중 검증 워크플로우 (GMP 준수)
 * 3. 자가 검증 방지
 * 4. 허용 오차 초과 감지
 * 5. 검증 대기 큐
 * 6. 참조 링크
 * 7. 칭량 수정
 * 8. 칭량 거부
 *
 * @author Moon Myung-seop
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("칭량 통합 테스트")
class WeighingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeighingRepository weighingRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String TENANT_ID = "TEST_TENANT";
    private static final String PRODUCT_CODE = "PROD_TEST_001";

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private UserEntity operator1;
    private UserEntity verifier1;

    @BeforeEach
    void setUp() {
        // Set tenant context
        TenantContext.setCurrentTenant(TENANT_ID);

        // Create test tenant
        testTenant = TenantEntity.builder()
                .tenantId(TENANT_ID)
                .tenantName("Test Tenant")
                .tenantCode("TEST")
                .companyName("Test Company")
                .industryType("chemical")
                .status("active")
                .build();
        testTenant = tenantRepository.save(testTenant);

        // Create test product
        testProduct = ProductEntity.builder()
                .tenant(testTenant)
                .productCode(PRODUCT_CODE)
                .productName("Test Product")
                .productType("원자재")
                .unit("kg")
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create operator user
        operator1 = UserEntity.builder()
                .tenant(testTenant)
                .username("operator1")
                .email("operator1@test.com")
                .passwordHash("$2a$10$dummyhash")
                .fullName("Operator 1")
                .status("active")
                .build();
        operator1 = userRepository.save(operator1);

        // Create verifier user
        verifier1 = UserEntity.builder()
                .tenant(testTenant)
                .username("verifier1")
                .email("verifier1@test.com")
                .passwordHash("$2a$10$dummyhash")
                .fullName("Verifier 1")
                .status("active")
                .build();
        verifier1 = userRepository.save(verifier1);
    }

    /**
     * Scenario 1: 칭량 생성 워크플로우
     * - POST /api/weighings로 칭량 생성
     * - 자동 생성된 weighingNo 형식 검증 (WG-YYYYMMDD-0001)
     * - 자동 계산 검증:
     *   - netWeight = grossWeight - tareWeight
     *   - variance = netWeight - expectedWeight
     *   - variancePercentage 계산
     *   - toleranceExceeded 플래그 설정
     * - 응답 상태 201 Created 검증
     * - 응답 본문에 모든 필드 포함 검증
     */
    @Test
    @DisplayName("시나리오 1: 칭량 생성 워크플로우 - 자동 계산 및 번호 생성")
    void scenario1_CompleteWeighingCreationWorkflow() {
        // Given
        WeighingCreateRequest request = WeighingCreateRequest.builder()
                .weighingType("INCOMING")
                .referenceType("MATERIAL_REQUEST")
                .referenceId(100L)
                .productId(testProduct.getProductId())
                .tareWeight(new BigDecimal("50.0"))
                .grossWeight(new BigDecimal("1050.0"))
                .expectedWeight(new BigDecimal("1000.0"))
                .unit("kg")
                .operatorUserId(operator1.getUserId())
                .tolerancePercentage(new BigDecimal("2.0"))
                .temperature(new BigDecimal("20.5"))
                .humidity(new BigDecimal("45.0"))
                .remarks("Test weighing")
                .build();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);
        HttpEntity<WeighingCreateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                createUrl("/api/weighings"),
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("칭량 생성 성공");

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data).isNotNull();

        // Verify weighingNo format: WG-YYYYMMDD-0001
        String weighingNo = (String) data.get("weighingNo");
        assertThat(weighingNo).isNotNull();
        assertThat(weighingNo).matches("WG-\\d{8}-\\d{4}");

        // Extract date part from weighingNo
        String dateStr = weighingNo.substring(3, 11);
        String todayStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertThat(dateStr).isEqualTo(todayStr);

        // Verify automatic calculations
        assertThat(data.get("tareWeight")).isEqualTo(50.0);
        assertThat(data.get("grossWeight")).isEqualTo(1050.0);
        assertThat(data.get("netWeight")).isEqualTo(1000.0); // grossWeight - tareWeight
        assertThat(data.get("expectedWeight")).isEqualTo(1000.0);
        assertThat(data.get("variance")).isEqualTo(0.0); // netWeight - expectedWeight
        assertThat(data.get("variancePercentage")).isEqualTo(0.0); // (variance / expectedWeight) * 100
        assertThat(data.get("toleranceExceeded")).isEqualTo(false); // abs(variancePercentage) <= tolerancePercentage

        // Verify other fields
        assertThat(data.get("weighingType")).isEqualTo("INCOMING");
        assertThat(data.get("referenceType")).isEqualTo("MATERIAL_REQUEST");
        assertThat(data.get("referenceId")).isEqualTo(100);
        assertThat(data.get("productId")).isEqualTo(testProduct.getProductId().intValue());
        assertThat(data.get("productCode")).isEqualTo(PRODUCT_CODE);
        assertThat(data.get("operatorUserId")).isEqualTo(operator1.getUserId().intValue());
        assertThat(data.get("operatorUsername")).isEqualTo("operator1");
        assertThat(data.get("verificationStatus")).isEqualTo("PENDING");
        assertThat(data.get("unit")).isEqualTo("kg");
        assertThat(data.get("temperature")).isEqualTo(20.5);
        assertThat(data.get("humidity")).isEqualTo(45.0);
    }

    /**
     * Scenario 2: 이중 검증 워크플로우
     * - 작업자(operatorUserId=1)가 칭량 생성
     * - 다른 사용자(verifierUserId=2)가 검증
     * - POST /api/weighings/{id}/verify (action=VERIFY)
     * - 상태 변경 검증: PENDING → VERIFIED
     * - 검증자 정보 저장 검증
     * - 검증 날짜 설정 검증
     */
    @Test
    @DisplayName("시나리오 2: 이중 검증 워크플로우 - 정상 검증")
    void scenario2_DualVerificationWorkflow() {
        // Given - Create weighing by operator1
        WeighingCreateRequest createRequest = WeighingCreateRequest.builder()
                .weighingType("PRODUCTION")
                .productId(testProduct.getProductId())
                .tareWeight(new BigDecimal("50.0"))
                .grossWeight(new BigDecimal("1050.0"))
                .expectedWeight(new BigDecimal("1000.0"))
                .unit("kg")
                .operatorUserId(operator1.getUserId())
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);
        HttpEntity<WeighingCreateRequest> createEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<Map> createResponse = restTemplate.exchange(
                createUrl("/api/weighings"),
                HttpMethod.POST,
                createEntity,
                Map.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
        Integer weighingId = (Integer) createData.get("weighingId");

        // When - Verify by verifier1 (different user)
        WeighingVerificationRequest verifyRequest = WeighingVerificationRequest.builder()
                .verifierUserId(verifier1.getUserId())
                .action("VERIFY")
                .remarks("Verified successfully")
                .build();

        HttpEntity<WeighingVerificationRequest> verifyEntity = new HttpEntity<>(verifyRequest, headers);

        ResponseEntity<Map> verifyResponse = restTemplate.exchange(
                createUrl("/api/weighings/" + weighingId + "/verify"),
                HttpMethod.POST,
                verifyEntity,
                Map.class
        );

        // Then
        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(verifyResponse.getBody().get("success")).isEqualTo(true);
        assertThat(verifyResponse.getBody().get("message")).isEqualTo("칭량 검증 성공");

        Map<String, Object> verifyData = (Map<String, Object>) verifyResponse.getBody().get("data");

        // Verify status changed: PENDING → VERIFIED
        assertThat(verifyData.get("verificationStatus")).isEqualTo("VERIFIED");

        // Verify verifier info saved
        assertThat(verifyData.get("verifierUserId")).isEqualTo(verifier1.getUserId().intValue());
        assertThat(verifyData.get("verifierUsername")).isEqualTo("verifier1");

        // Verify verificationDate is set
        assertThat(verifyData.get("verificationDate")).isNotNull();

        // Verify remarks updated
        String remarks = (String) verifyData.get("remarks");
        assertThat(remarks).contains("Verified successfully");
    }

    /**
     * Scenario 3: 자가 검증 방지
     * - 작업자(operatorUserId=1)가 칭량 생성
     * - 동일한 사용자(verifierUserId=1)가 검증 시도
     * - POST /api/weighings/{id}/verify
     * - BusinessException 발생 검증
     * - 자가 검증 불가 에러 메시지 검증
     */
    @Test
    @DisplayName("시나리오 3: 자가 검증 방지 - GMP 준수")
    void scenario3_SelfVerificationPrevention() {
        // Given - Create weighing by operator1
        WeighingCreateRequest createRequest = WeighingCreateRequest.builder()
                .weighingType("OUTGOING")
                .productId(testProduct.getProductId())
                .tareWeight(new BigDecimal("50.0"))
                .grossWeight(new BigDecimal("1050.0"))
                .expectedWeight(new BigDecimal("1000.0"))
                .unit("kg")
                .operatorUserId(operator1.getUserId())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);
        HttpEntity<WeighingCreateRequest> createEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<Map> createResponse = restTemplate.exchange(
                createUrl("/api/weighings"),
                HttpMethod.POST,
                createEntity,
                Map.class
        );

        Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
        Integer weighingId = (Integer) createData.get("weighingId");

        // When - Attempt to verify by same user (operator1)
        WeighingVerificationRequest verifyRequest = WeighingVerificationRequest.builder()
                .verifierUserId(operator1.getUserId()) // Same as operator
                .action("VERIFY")
                .build();

        HttpEntity<WeighingVerificationRequest> verifyEntity = new HttpEntity<>(verifyRequest, headers);

        ResponseEntity<Map> verifyResponse = restTemplate.exchange(
                createUrl("/api/weighings/" + weighingId + "/verify"),
                HttpMethod.POST,
                verifyEntity,
                Map.class
        );

        // Then - Should fail with error about self-verification
        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(verifyResponse.getBody().get("success")).isEqualTo(false);

        String errorMessage = (String) verifyResponse.getBody().get("message");
        assertThat(errorMessage).containsIgnoringCase("self-verification");
    }

    /**
     * Scenario 4: 허용 오차 초과 감지
     * - 높은 변동을 가진 칭량 생성 (expected=1000, net=1050)
     * - toleranceExceeded = true 검증
     * - GET /api/weighings/tolerance-exceeded
     * - 목록에 칭량이 표시되는지 검증
     */
    @Test
    @DisplayName("시나리오 4: 허용 오차 초과 감지")
    void scenario4_ToleranceExceededDetection() {
        // Given - Create weighing with high variance
        WeighingCreateRequest request = WeighingCreateRequest.builder()
                .weighingType("SAMPLING")
                .productId(testProduct.getProductId())
                .tareWeight(new BigDecimal("50.0"))
                .grossWeight(new BigDecimal("1100.0")) // Net will be 1050
                .expectedWeight(new BigDecimal("1000.0")) // Variance = +50 (5%)
                .unit("kg")
                .operatorUserId(operator1.getUserId())
                .tolerancePercentage(new BigDecimal("2.0")) // 2% tolerance
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);
        HttpEntity<WeighingCreateRequest> createEntity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> createResponse = restTemplate.exchange(
                createUrl("/api/weighings"),
                HttpMethod.POST,
                createEntity,
                Map.class
        );

        // Then - Verify toleranceExceeded = true
        Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
        assertThat(createData.get("netWeight")).isEqualTo(1050.0);
        assertThat(createData.get("variance")).isEqualTo(50.0);
        assertThat(createData.get("variancePercentage")).isEqualTo(5.0);
        assertThat(createData.get("toleranceExceeded")).isEqualTo(true);

        // When - Get tolerance exceeded list
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                createUrl("/api/weighings/tolerance-exceeded"),
                HttpMethod.GET,
                getEntity,
                Map.class
        );

        // Then - Verify weighing appears in list
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> data = (List<Map<String, Object>>) getResponse.getBody().get("data");
        assertThat(data).isNotEmpty();

        boolean found = data.stream()
                .anyMatch(w -> w.get("weighingId").equals(createData.get("weighingId")));
        assertThat(found).isTrue();
    }

    /**
     * Scenario 5: 검증 대기 큐
     * - 3개의 칭량 생성 (모두 PENDING 상태)
     * - 1개 칭량 검증
     * - GET /api/weighings/pending-verification
     * - 목록에 2개만 표시되는지 검증 (미검증 항목만)
     */
    @Test
    @DisplayName("시나리오 5: 검증 대기 큐")
    void scenario5_PendingVerificationQueue() {
        // Given - Create 3 weighings (all PENDING)
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);

        Integer weighing1Id = null;
        for (int i = 0; i < 3; i++) {
            WeighingCreateRequest request = WeighingCreateRequest.builder()
                    .weighingType("INCOMING")
                    .productId(testProduct.getProductId())
                    .tareWeight(new BigDecimal("50.0"))
                    .grossWeight(new BigDecimal("1050.0"))
                    .expectedWeight(new BigDecimal("1000.0"))
                    .unit("kg")
                    .operatorUserId(operator1.getUserId())
                    .build();

            HttpEntity<WeighingCreateRequest> createEntity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> createResponse = restTemplate.exchange(
                    createUrl("/api/weighings"),
                    HttpMethod.POST,
                    createEntity,
                    Map.class
            );

            if (i == 0) {
                Map<String, Object> data = (Map<String, Object>) createResponse.getBody().get("data");
                weighing1Id = (Integer) data.get("weighingId");
            }
        }

        // When - Verify 1 weighing
        WeighingVerificationRequest verifyRequest = WeighingVerificationRequest.builder()
                .verifierUserId(verifier1.getUserId())
                .action("VERIFY")
                .build();

        HttpEntity<WeighingVerificationRequest> verifyEntity = new HttpEntity<>(verifyRequest, headers);
        restTemplate.exchange(
                createUrl("/api/weighings/" + weighing1Id + "/verify"),
                HttpMethod.POST,
                verifyEntity,
                Map.class
        );

        // Then - Get pending verification list
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                createUrl("/api/weighings/pending-verification"),
                HttpMethod.GET,
                getEntity,
                Map.class
        );

        // Verify only 2 weighings in list (unverified ones)
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> data = (List<Map<String, Object>>) getResponse.getBody().get("data");
        assertThat(data).hasSize(2);

        // Verify all are PENDING status
        assertThat(data).allMatch(w -> "PENDING".equals(w.get("verificationStatus")));
    }

    /**
     * Scenario 6: 참조 링크
     * - referenceType=MATERIAL_REQUEST, referenceId=100으로 칭량 생성
     * - GET /api/weighings/reference/MATERIAL_REQUEST/100
     * - 칭량 반환 검증
     * - 다른 참조 유형 테스트
     */
    @Test
    @DisplayName("시나리오 6: 참조 링크")
    void scenario6_ReferenceLinkage() {
        // Given - Create weighing with reference
        WeighingCreateRequest request = WeighingCreateRequest.builder()
                .weighingType("INCOMING")
                .referenceType("MATERIAL_REQUEST")
                .referenceId(100L)
                .productId(testProduct.getProductId())
                .tareWeight(new BigDecimal("50.0"))
                .grossWeight(new BigDecimal("1050.0"))
                .unit("kg")
                .operatorUserId(operator1.getUserId())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);
        HttpEntity<WeighingCreateRequest> createEntity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> createResponse = restTemplate.exchange(
                createUrl("/api/weighings"),
                HttpMethod.POST,
                createEntity,
                Map.class
        );

        Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
        Integer weighingId = (Integer) createData.get("weighingId");

        // When - Get weighings by reference
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                createUrl("/api/weighings/reference/MATERIAL_REQUEST/100"),
                HttpMethod.GET,
                getEntity,
                Map.class
        );

        // Then - Verify weighing returned
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> data = (List<Map<String, Object>>) getResponse.getBody().get("data");
        assertThat(data).isNotEmpty();

        Map<String, Object> weighing = data.get(0);
        assertThat(weighing.get("weighingId")).isEqualTo(weighingId);
        assertThat(weighing.get("referenceType")).isEqualTo("MATERIAL_REQUEST");
        assertThat(weighing.get("referenceId")).isEqualTo(100);

        // Test with different reference type
        WeighingCreateRequest request2 = WeighingCreateRequest.builder()
                .weighingType("PRODUCTION")
                .referenceType("WORK_ORDER")
                .referenceId(200L)
                .productId(testProduct.getProductId())
                .tareWeight(new BigDecimal("50.0"))
                .grossWeight(new BigDecimal("1050.0"))
                .unit("kg")
                .operatorUserId(operator1.getUserId())
                .build();

        HttpEntity<WeighingCreateRequest> createEntity2 = new HttpEntity<>(request2, headers);
        restTemplate.exchange(
                createUrl("/api/weighings"),
                HttpMethod.POST,
                createEntity2,
                Map.class
        );

        ResponseEntity<Map> getResponse2 = restTemplate.exchange(
                createUrl("/api/weighings/reference/WORK_ORDER/200"),
                HttpMethod.GET,
                getEntity,
                Map.class
        );

        List<Map<String, Object>> data2 = (List<Map<String, Object>>) getResponse2.getBody().get("data");
        assertThat(data2).isNotEmpty();
        assertThat(data2.get(0).get("referenceType")).isEqualTo("WORK_ORDER");
        assertThat(data2.get(0).get("referenceId")).isEqualTo(200);
    }

    /**
     * Scenario 7: 칭량 수정
     * - 칭량 생성
     * - PUT /api/weighings/{id} (새로운 tareWeight 및 grossWeight)
     * - netWeight 재계산 검증
     * - variance 재계산 검증
     * - 응답 상태 200 검증
     */
    @Test
    @DisplayName("시나리오 7: 칭량 수정 - 자동 재계산")
    void scenario7_UpdateWeighing() {
        // Given - Create weighing
        WeighingCreateRequest createRequest = WeighingCreateRequest.builder()
                .weighingType("INCOMING")
                .productId(testProduct.getProductId())
                .tareWeight(new BigDecimal("50.0"))
                .grossWeight(new BigDecimal("1050.0"))
                .expectedWeight(new BigDecimal("1000.0"))
                .unit("kg")
                .operatorUserId(operator1.getUserId())
                .tolerancePercentage(new BigDecimal("2.0"))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);
        HttpEntity<WeighingCreateRequest> createEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<Map> createResponse = restTemplate.exchange(
                createUrl("/api/weighings"),
                HttpMethod.POST,
                createEntity,
                Map.class
        );

        Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
        Integer weighingId = (Integer) createData.get("weighingId");

        // When - Update weighing with new weights
        WeighingUpdateRequest updateRequest = WeighingUpdateRequest.builder()
                .tareWeight(new BigDecimal("60.0")) // Changed from 50
                .grossWeight(new BigDecimal("1060.0")) // Changed from 1050
                .remarks("Updated weights")
                .build();

        HttpEntity<WeighingUpdateRequest> updateEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                createUrl("/api/weighings/" + weighingId),
                HttpMethod.PUT,
                updateEntity,
                Map.class
        );

        // Then - Verify response status 200
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().get("success")).isEqualTo(true);

        Map<String, Object> updateData = (Map<String, Object>) updateResponse.getBody().get("data");

        // Verify netWeight recalculated: 1060 - 60 = 1000
        assertThat(updateData.get("tareWeight")).isEqualTo(60.0);
        assertThat(updateData.get("grossWeight")).isEqualTo(1060.0);
        assertThat(updateData.get("netWeight")).isEqualTo(1000.0);

        // Verify variance recalculated: 1000 - 1000 = 0
        assertThat(updateData.get("variance")).isEqualTo(0.0);
        assertThat(updateData.get("variancePercentage")).isEqualTo(0.0);

        // Verify remarks updated
        assertThat(updateData.get("remarks")).isEqualTo("Updated weights");
    }

    /**
     * Scenario 8: 칭량 거부
     * - 칭량 생성
     * - POST /api/weighings/{id}/verify (action=REJECT)
     * - 상태 변경 검증: PENDING → REJECTED
     * - 거부 사유 저장 검증
     */
    @Test
    @DisplayName("시나리오 8: 칭량 거부")
    void scenario8_RejectWeighing() {
        // Given - Create weighing
        WeighingCreateRequest createRequest = WeighingCreateRequest.builder()
                .weighingType("PRODUCTION")
                .productId(testProduct.getProductId())
                .tareWeight(new BigDecimal("50.0"))
                .grossWeight(new BigDecimal("1050.0"))
                .expectedWeight(new BigDecimal("1000.0"))
                .unit("kg")
                .operatorUserId(operator1.getUserId())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);
        HttpEntity<WeighingCreateRequest> createEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<Map> createResponse = restTemplate.exchange(
                createUrl("/api/weighings"),
                HttpMethod.POST,
                createEntity,
                Map.class
        );

        Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
        Integer weighingId = (Integer) createData.get("weighingId");

        // When - Reject weighing
        WeighingVerificationRequest rejectRequest = WeighingVerificationRequest.builder()
                .verifierUserId(verifier1.getUserId())
                .action("REJECT")
                .remarks("Weight measurement appears incorrect")
                .build();

        HttpEntity<WeighingVerificationRequest> rejectEntity = new HttpEntity<>(rejectRequest, headers);

        ResponseEntity<Map> rejectResponse = restTemplate.exchange(
                createUrl("/api/weighings/" + weighingId + "/verify"),
                HttpMethod.POST,
                rejectEntity,
                Map.class
        );

        // Then - Verify status changed: PENDING → REJECTED
        assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rejectResponse.getBody().get("success")).isEqualTo(true);
        assertThat(rejectResponse.getBody().get("message")).isEqualTo("칭량 거부 성공");

        Map<String, Object> rejectData = (Map<String, Object>) rejectResponse.getBody().get("data");
        assertThat(rejectData.get("verificationStatus")).isEqualTo("REJECTED");

        // Verify verifier info saved
        assertThat(rejectData.get("verifierUserId")).isEqualTo(verifier1.getUserId().intValue());

        // Verify rejection captured
        String remarks = (String) rejectData.get("remarks");
        assertThat(remarks).contains("REJECTED");
        assertThat(remarks).contains("Weight measurement appears incorrect");
    }

    /**
     * Helper method to create full URL
     */
    private String createUrl(String uri) {
        return "http://localhost:" + port + uri;
    }
}
