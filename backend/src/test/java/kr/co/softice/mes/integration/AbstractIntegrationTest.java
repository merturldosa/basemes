package kr.co.softice.mes.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 통합 테스트 기반 클래스
 *
 * TestContainers PostgreSQL을 사용한 실제 데이터베이스 통합 테스트
 *
 * @author Claude Code (Sonnet 4.5)
 * @company SoftIce Co., Ltd.
 * @since 2026-01-27
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    /**
     * TestContainers PostgreSQL 컨테이너
     * 모든 통합 테스트에서 공유되는 단일 컨테이너 (재사용으로 성능 최적화)
     */
    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("soice_mes_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * Spring Boot 애플리케이션에 PostgreSQL 연결 정보 동적 주입
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    // ═══════════════════════════════════════════════════════════════
    // Repositories
    // ═══════════════════════════════════════════════════════════════

    @Autowired
    protected TenantRepository tenantRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PermissionRepository permissionRepository;

    @Autowired
    protected UserRoleRepository userRoleRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected CustomerRepository customerRepository;

    @Autowired
    protected SupplierRepository supplierRepository;

    @Autowired
    protected MaterialRepository materialRepository;

    @Autowired
    protected BomRepository bomRepository;

    @Autowired
    protected ProcessRepository processRepository;

    @Autowired
    protected WarehouseRepository warehouseRepository;

    @Autowired
    protected InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    protected StockLevelRepository stockLevelRepository;

    @Autowired
    protected WorkOrderRepository workOrderRepository;

    @Autowired
    protected ProductionRecordRepository productionRecordRepository;

    @Autowired
    protected PurchaseRequestRepository purchaseRequestRepository;

    @Autowired
    protected PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    protected GoodsReceiptRepository goodsReceiptRepository;

    @Autowired
    protected GoodsIssueRepository goodsIssueRepository;

    @Autowired
    protected IQCInspectionRepository iqcInspectionRepository;

    @Autowired
    protected OQCInspectionRepository oqcInspectionRepository;

    @Autowired
    protected ApprovalRepository approvalRepository;

    @Autowired
    protected ApprovalLineRepository approvalLineRepository;

    @Autowired
    protected MaterialHandoverRepository materialHandoverRepository;

    @Autowired
    protected AuditLogRepository auditLogRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    // ═══════════════════════════════════════════════════════════════
    // 테스트 데이터 헬퍼
    // ═══════════════════════════════════════════════════════════════

    protected TenantEntity testTenant;
    protected UserEntity testUser;
    protected RoleEntity testRole;
    protected PermissionEntity testPermission;
    protected WarehouseEntity testWarehouse;
    protected ProductEntity testProduct;
    protected MaterialEntity testMaterial;
    protected SupplierEntity testSupplier;
    protected CustomerEntity testCustomer;

    /**
     * 각 테스트 메서드 실행 전 기본 테스트 데이터 생성
     */
    @BeforeEach
    public void setUpTestData() {
        // 트랜잭션 롤백으로 데이터가 자동으로 정리되므로
        // 매번 새로운 테스트 데이터 생성
        createBasicTestData();
    }

    /**
     * 기본 테스트 데이터 생성
     * - Tenant, User, Role, Permission, Warehouse
     */
    protected void createBasicTestData() {
        // 1. Tenant 생성
        testTenant = TenantEntity.builder()
                .tenantCode("TEST001")
                .tenantName("테스트 회사")
                .businessType("MANUFACTURING")
                .contactPerson("홍길동")
                .contactEmail("test@example.com")
                .contactPhone("010-1234-5678")
                .isActive(true)
                .build();
        testTenant = tenantRepository.save(testTenant);

        // 2. Permission 생성
        testPermission = PermissionEntity.builder()
                .permissionCode("TEST_READ")
                .permissionName("테스트 읽기 권한")
                .module("TEST")
                .description("테스트용 권한")
                .build();
        testPermission = permissionRepository.save(testPermission);

        // 3. Role 생성
        testRole = RoleEntity.builder()
                .roleCode("TEST_ROLE")
                .roleName("테스트 역할")
                .description("테스트용 역할")
                .isActive(true)
                .build();
        testRole.addPermission(testPermission);
        testRole = roleRepository.save(testRole);

        // 4. User 생성
        testUser = UserEntity.builder()
                .tenant(testTenant)
                .username("testuser")
                .password(passwordEncoder.encode("password"))
                .email("testuser@example.com")
                .fullName("테스트 사용자")
                .status("active")
                .build();
        testUser = userRepository.save(testUser);

        // 5. User-Role 연결
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(testUser);
        userRole.setRole(testRole);
        userRoleRepository.save(userRole);

        // 6. Warehouse 생성
        testWarehouse = WarehouseEntity.builder()
                .tenant(testTenant)
                .warehouseCode("WH001")
                .warehouseName("메인 창고")
                .warehouseType("MAIN")
                .isActive(true)
                .build();
        testWarehouse = warehouseRepository.save(testWarehouse);

        // 7. Product 생성
        testProduct = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("PROD001")
                .productName("테스트 제품")
                .productType("FINISHED")
                .unit("EA")
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);

        // 8. Material 생성
        testMaterial = MaterialEntity.builder()
                .tenant(testTenant)
                .materialCode("MAT001")
                .materialName("테스트 자재")
                .materialType("RAW")
                .unit("KG")
                .isActive(true)
                .build();
        testMaterial = materialRepository.save(testMaterial);

        // 9. Supplier 생성
        testSupplier = SupplierEntity.builder()
                .tenant(testTenant)
                .supplierCode("SUP001")
                .supplierName("테스트 공급업체")
                .contactPerson("김공급")
                .contactPhone("010-2222-3333")
                .contactEmail("supplier@example.com")
                .qualityRating("A")
                .isActive(true)
                .build();
        testSupplier = supplierRepository.save(testSupplier);

        // 10. Customer 생성
        testCustomer = CustomerEntity.builder()
                .tenant(testTenant)
                .customerCode("CUST001")
                .customerName("테스트 고객사")
                .customerType("DOMESTIC")
                .contactPerson("이고객")
                .contactPhone("010-3333-4444")
                .contactEmail("customer@example.com")
                .isActive(true)
                .build();
        testCustomer = customerRepository.save(testCustomer);
    }

    /**
     * Work Order 생성 헬퍼
     */
    protected WorkOrderEntity createWorkOrder(String woNumber, String status) {
        WorkOrderEntity wo = new WorkOrderEntity();
        wo.setTenant(testTenant);
        wo.setWoNumber(woNumber);
        wo.setProduct(testProduct);
        wo.setPlannedQuantity(100.0);
        wo.setActualQuantity(0.0);
        wo.setStatus(status);
        wo.setPlannedStartDate(LocalDateTime.now());
        wo.setPlannedEndDate(LocalDateTime.now().plusDays(7));
        return workOrderRepository.save(wo);
    }

    /**
     * Purchase Request 생성 헬퍼
     */
    protected PurchaseRequestEntity createPurchaseRequest(String prNumber, String status) {
        PurchaseRequestEntity pr = new PurchaseRequestEntity();
        pr.setTenant(testTenant);
        pr.setPrNumber(prNumber);
        pr.setRequestDate(LocalDateTime.now());
        pr.setRequester(testUser);
        pr.setStatus(status);
        pr.setTotalAmount(1000.0);
        return purchaseRequestRepository.save(pr);
    }

    /**
     * Purchase Order 생성 헬퍼
     */
    protected PurchaseOrderEntity createPurchaseOrder(String poNumber, String status) {
        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setTenant(testTenant);
        po.setPoNumber(poNumber);
        po.setSupplier(testSupplier);
        po.setOrderDate(LocalDateTime.now());
        po.setStatus(status);
        po.setTotalAmount(1000.0);
        return purchaseOrderRepository.save(po);
    }

    /**
     * Approval Line 생성 헬퍼
     */
    protected ApprovalLineEntity createApprovalLine(String lineCode, String templateName) {
        ApprovalLineEntity line = new ApprovalLineEntity();
        line.setTenant(testTenant);
        line.setLineCode(lineCode);
        line.setLineName(templateName);
        line.setDescription("테스트 승인 라인");
        line.setIsActive(true);
        return approvalLineRepository.save(line);
    }

    /**
     * Stock Level 생성 헬퍼 (재고 수준 설정)
     */
    protected StockLevelEntity createStockLevel(ProductEntity product, Double quantity) {
        StockLevelEntity stock = new StockLevelEntity();
        stock.setTenant(testTenant);
        stock.setProduct(product);
        stock.setWarehouse(testWarehouse);
        stock.setAvailableQuantity(quantity);
        stock.setOnHandQuantity(quantity);
        stock.setAllocatedQuantity(0.0);
        return stockLevelRepository.save(stock);
    }

    /**
     * Inventory Transaction 생성 헬퍼
     */
    protected InventoryTransactionEntity createInventoryTransaction(
            String transactionType, ProductEntity product, Double quantity) {
        InventoryTransactionEntity transaction = new InventoryTransactionEntity();
        transaction.setTenant(testTenant);
        transaction.setTransactionType(transactionType);
        transaction.setProduct(product);
        transaction.setWarehouse(testWarehouse);
        transaction.setQuantity(quantity);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setReferenceType("TEST");
        transaction.setReferenceId(1L);
        return inventoryTransactionRepository.save(transaction);
    }

    /**
     * BOM 생성 헬퍼
     */
    protected BomEntity createBom(ProductEntity product, String version) {
        BomEntity bom = new BomEntity();
        bom.setTenant(testTenant);
        bom.setProduct(product);
        bom.setVersion(version);
        bom.setIsActive(true);
        return bomRepository.save(bom);
    }

    /**
     * Process 생성 헬퍼
     */
    protected ProcessEntity createProcess(String processCode, String processName) {
        ProcessEntity process = ProcessEntity.builder()
                .tenant(testTenant)
                .processCode(processCode)
                .processName(processName)
                .processType("ASSEMBLY")
                .sequence(1)
                .isActive(true)
                .build();
        return processRepository.save(process);
    }

    /**
     * Audit Log 생성 헬퍼
     */
    protected AuditLogEntity createAuditLog(String action, String entityType) {
        AuditLogEntity log = new AuditLogEntity();
        log.setTenant(testTenant);
        log.setUser(testUser);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(1L);
        log.setIpAddress("127.0.0.1");
        log.setUserAgent("Test Agent");
        log.setCreatedAt(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    /**
     * 테스트 데이터 정리 헬퍼
     * (@Transactional로 자동 롤백되지만 명시적 정리가 필요한 경우 사용)
     */
    protected void cleanupTestData() {
        // 트랜잭션 롤백으로 자동 정리됨
    }
}
