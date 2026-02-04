package kr.co.softice.mes.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * REST API Controller 통합 테스트
 *
 * MockMvc를 사용한 HTTP 엔드포인트 통합 테스트
 *
 * @author Claude Code (Sonnet 4.5)
 * @company SoftIce Co., Ltd.
 * @since 2026-01-27
 */
@DisplayName("REST API Controller 통합 테스트")
@AutoConfigureMockMvc
public class RestApiControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Product API - 제품 생성 (POST)")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testProductApi_Create() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 제품 생성 요청 JSON
        // ═══════════════════════════════════════════════════════════════

        ProductEntity newProduct = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("API-PROD-001")
                .productName("API 테스트 제품")
                .productType("FINISHED")
                .unit("EA")
                .isActive(true)
                .build();

        String productJson = objectMapper.writeValueAsString(newProduct);

        // ═══════════════════════════════════════════════════════════════
        // When & Then: POST /api/products
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productCode").value("API-PROD-001"))
                .andExpect(jsonPath("$.productName").value("API 테스트 제품"))
                .andExpect(jsonPath("$.productType").value("FINISHED"));
    }

    @Test
    @DisplayName("Product API - 제품 목록 조회 (GET)")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testProductApi_GetList() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 테스트 제품 생성
        // ═══════════════════════════════════════════════════════════════

        ProductEntity product1 = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("LIST-001")
                .productName("목록 제품 1")
                .productType("FINISHED")
                .unit("EA")
                .isActive(true)
                .build();
        productRepository.save(product1);

        ProductEntity product2 = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("LIST-002")
                .productName("목록 제품 2")
                .productType("SEMI_FINISHED")
                .unit("EA")
                .isActive(true)
                .build();
        productRepository.save(product2);

        // ═══════════════════════════════════════════════════════════════
        // When & Then: GET /api/products
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(get("/api/products")
                        .param("tenantId", testTenant.getTenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].productCode", hasItem("LIST-001")))
                .andExpect(jsonPath("$[*].productCode", hasItem("LIST-002")));
    }

    @Test
    @DisplayName("Product API - 제품 상세 조회 (GET by ID)")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testProductApi_GetById() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 테스트 제품 생성
        // ═══════════════════════════════════════════════════════════════

        ProductEntity product = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("DETAIL-001")
                .productName("상세 제품")
                .productType("FINISHED")
                .unit("EA")
                .isActive(true)
                .build();
        product = productRepository.save(product);

        // ═══════════════════════════════════════════════════════════════
        // When & Then: GET /api/products/{id}
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(get("/api/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.productCode").value("DETAIL-001"))
                .andExpect(jsonPath("$.productName").value("상세 제품"));
    }

    @Test
    @DisplayName("Product API - 제품 수정 (PUT)")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testProductApi_Update() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 기존 제품 생성
        // ═══════════════════════════════════════════════════════════════

        ProductEntity product = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("UPDATE-001")
                .productName("수정 전 제품")
                .productType("FINISHED")
                .unit("EA")
                .isActive(true)
                .build();
        product = productRepository.save(product);

        // 수정할 내용
        product.setProductName("수정 후 제품");
        product.setStandardPrice(10000.0);

        String updatedJson = objectMapper.writeValueAsString(product);

        // ═══════════════════════════════════════════════════════════════
        // When & Then: PUT /api/products/{id}
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(put("/api/products/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("수정 후 제품"))
                .andExpect(jsonPath("$.standardPrice").value(10000.0));
    }

    @Test
    @DisplayName("Product API - 제품 삭제 (DELETE)")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testProductApi_Delete() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 삭제할 제품 생성
        // ═══════════════════════════════════════════════════════════════

        ProductEntity product = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("DELETE-001")
                .productName("삭제 제품")
                .productType("FINISHED")
                .unit("EA")
                .isActive(true)
                .build();
        product = productRepository.save(product);

        Long productId = product.getId();

        // ═══════════════════════════════════════════════════════════════
        // When & Then: DELETE /api/products/{id}
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(delete("/api/products/" + productId))
                .andExpect(status().isNoContent());

        // 삭제 후 조회 시 404 또는 isActive=false 확인
        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Inventory API - 재고 조회 (GET)")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInventoryApi_GetStockLevel() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 재고 데이터 생성
        // ═══════════════════════════════════════════════════════════════

        StockLevelEntity stock = createStockLevel(testProduct, 100.0);

        // ═══════════════════════════════════════════════════════════════
        // When & Then: GET /api/inventory/stock-levels
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(get("/api/inventory/stock-levels")
                        .param("tenantId", testTenant.getTenantId())
                        .param("productId", testProduct.getId().toString())
                        .param("warehouseId", testWarehouse.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(100.0))
                .andExpect(jsonPath("$.onHandQuantity").value(100.0));
    }

    @Test
    @DisplayName("WorkOrder API - 작업지시 생성 및 조회")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testWorkOrderApi_CreateAndGet() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 작업지시 생성 요청
        // ═══════════════════════════════════════════════════════════════

        WorkOrderEntity workOrder = new WorkOrderEntity();
        workOrder.setTenant(testTenant);
        workOrder.setWoNumber("API-WO-001");
        workOrder.setProduct(testProduct);
        workOrder.setPlannedQuantity(100.0);
        workOrder.setActualQuantity(0.0);
        workOrder.setStatus("DRAFT");
        workOrder.setPlannedStartDate(LocalDateTime.now());
        workOrder.setPlannedEndDate(LocalDateTime.now().plusDays(7));

        String woJson = objectMapper.writeValueAsString(workOrder);

        // ═══════════════════════════════════════════════════════════════
        // When: POST /api/work-orders
        // ═══════════════════════════════════════════════════════════════

        MvcResult createResult = mockMvc.perform(post("/api/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(woJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.woNumber").value("API-WO-001"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        WorkOrderEntity createdWO = objectMapper.readValue(responseContent, WorkOrderEntity.class);

        // ═══════════════════════════════════════════════════════════════
        // Then: GET /api/work-orders/{id}
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(get("/api/work-orders/" + createdWO.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.woNumber").value("API-WO-001"))
                .andExpect(jsonPath("$.plannedQuantity").value(100.0));
    }

    @Test
    @DisplayName("Dashboard API - 대시보드 통계 조회")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDashboardApi_GetStatistics() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // When & Then: GET /api/dashboard/stats
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(get("/api/dashboard/stats")
                        .param("tenantId", testTenant.getTenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.totalRoles").exists())
                .andExpect(jsonPath("$.totalPermissions").exists())
                .andExpect(jsonPath("$.activeUsers").exists());
    }

    @Test
    @DisplayName("인증 없이 API 접근 시 401 Unauthorized")
    void testApi_WithoutAuthentication_Unauthorized() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // When & Then: 인증 없이 API 호출
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 권한으로 API 접근 시 403 Forbidden")
    @WithMockUser(username = "readonly", roles = {"READONLY"})
    void testApi_WithInsufficientPermission_Forbidden() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 읽기 전용 권한만 있는 사용자
        // ═══════════════════════════════════════════════════════════════

        ProductEntity product = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("FORBIDDEN-001")
                .productName("권한 테스트")
                .productType("FINISHED")
                .unit("EA")
                .isActive(true)
                .build();

        String productJson = objectMapper.writeValueAsString(product);

        // ═══════════════════════════════════════════════════════════════
        // When & Then: 쓰기 권한이 필요한 API 호출 (403 예상)
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 리소스 조회 시 404 Not Found")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testApi_ResourceNotFound_404() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // When & Then: 존재하지 않는 ID로 조회
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(get("/api/products/99999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 요청 데이터 전송 시 400 Bad Request")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testApi_InvalidRequest_BadRequest() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 필수 필드가 누락된 요청 JSON
        // ═══════════════════════════════════════════════════════════════

        String invalidJson = "{ \"productCode\": \"\" }"; // productName 누락

        // ═══════════════════════════════════════════════════════════════
        // When & Then: 잘못된 요청 전송 (400 예상)
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JSON 직렬화/역직렬화 검증")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testJson_SerializationDeserialization() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // Given: 복잡한 객체 생성
        // ═══════════════════════════════════════════════════════════════

        ProductEntity product = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("JSON-001")
                .productName("JSON 테스트")
                .productType("FINISHED")
                .unit("EA")
                .standardPrice(50000.0)
                .minimumOrderQuantity(10.0)
                .leadTime(7)
                .isActive(true)
                .build();

        String json = objectMapper.writeValueAsString(product);

        // ═══════════════════════════════════════════════════════════════
        // When: API 호출 및 응답 확인
        // ═══════════════════════════════════════════════════════════════

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        // ═══════════════════════════════════════════════════════════════
        // Then: 응답 JSON 역직렬화 검증
        // ═══════════════════════════════════════════════════════════════

        String responseJson = result.getResponse().getContentAsString();
        ProductEntity responseProduct = objectMapper.readValue(responseJson, ProductEntity.class);

        assertThat(responseProduct.getProductCode()).isEqualTo("JSON-001");
        assertThat(responseProduct.getStandardPrice()).isEqualTo(50000.0);
        assertThat(responseProduct.getLeadTime()).isEqualTo(7);
    }

    @Test
    @DisplayName("CORS 설정 검증")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCors_Configuration() throws Exception {
        // ═══════════════════════════════════════════════════════════════
        // When & Then: CORS 헤더 확인
        // ═══════════════════════════════════════════════════════════════

        mockMvc.perform(get("/api/products")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
