package kr.co.softice.mes.common.config;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Initial Data Loader
 * 초기 테스트 데이터 로딩
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProductRepository productRepository;
    private final ProcessRepository processRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========================================");
        log.info("Starting Initial Data Loading...");
        log.info("========================================");

        // Check if data already exists
        if (tenantRepository.count() > 0) {
            log.info("Data already exists. Skipping initial data loading.");
            return;
        }

        try {
            // 1. Create Tenant
            TenantEntity tenant = createTenant();
            log.info("✓ Tenant created: {}", tenant.getTenantId());

            // 2. Create Roles
            RoleEntity adminRole = createRole(tenant, "ADMIN", "관리자", "시스템 전체 관리자");
            RoleEntity managerRole = createRole(tenant, "PRODUCTION_MANAGER", "생산 관리자", "생산 관리 담당자");
            RoleEntity operatorRole = createRole(tenant, "OPERATOR", "작업자", "현장 작업자");
            log.info("✓ Roles created: 3 roles");

            // 3. Create Permissions
            createPermissions(tenant);
            log.info("✓ Permissions created: 12 permissions");

            // 4. Assign Permissions to Roles
            assignPermissionsToRole(adminRole, tenant);
            assignPermissionsToRole(managerRole, tenant);
            log.info("✓ Permissions assigned to roles");

            // 5. Create Users
            UserEntity adminUser = createUser(tenant, "admin", "관리자", "admin@smartdocking.co.kr", "admin123");
            UserEntity managerUser = createUser(tenant, "manager", "김생산", "manager@smartdocking.co.kr", "manager123");
            UserEntity operatorUser = createUser(tenant, "operator", "이작업", "operator@smartdocking.co.kr", "operator123");
            log.info("✓ Users created: 3 users");

            // 6. Assign Roles to Users
            assignRoleToUser(adminUser, adminRole);
            assignRoleToUser(managerUser, managerRole);
            assignRoleToUser(operatorUser, operatorRole);
            log.info("✓ Roles assigned to users");

            // 7. Create Products
            createProducts(tenant);
            log.info("✓ Products created: 5 products");

            // 8. Create Processes
            createProcesses(tenant);
            log.info("✓ Processes created: 5 processes");

            // 9. Create Sample Work Orders
            createWorkOrders(tenant);
            log.info("✓ Work Orders created: 3 work orders");

            log.info("========================================");
            log.info("Initial Data Loading Completed!");
            log.info("========================================");
            log.info("");
            log.info("Test Credentials:");
            log.info("  Admin    - ID: admin    / PW: admin123");
            log.info("  Manager  - ID: manager  / PW: manager123");
            log.info("  Operator - ID: operator / PW: operator123");
            log.info("  Tenant   - DEMO001");
            log.info("");

        } catch (Exception e) {
            log.error("Error loading initial data", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to load initial data");
        }
    }

    private TenantEntity createTenant() {
        Map<String, Object> config = new HashMap<>();
        config.put("timezone", "Asia/Seoul");
        config.put("locale", "ko_KR");

        TenantEntity tenant = TenantEntity.builder()
                .tenantId("DEMO001")
                .tenantName("데모 회사")
                .tenantCode("DEMO001")
                .companyName("(주)스마트도킹스테이션 데모")
                .industryType("electronics")
                .description("테스트용 데모 회사")
                .config(config)
                .status("active")
                .build();
        return tenantRepository.save(tenant);
    }

    private RoleEntity createRole(TenantEntity tenant, String code, String name, String description) {
        RoleEntity role = RoleEntity.builder()
                .tenant(tenant)
                .roleCode(code)
                .roleName(name)
                .description(description)
                .isActive(true)
                .build();
        return roleRepository.save(role);
    }

    private void createPermissions(TenantEntity tenant) {
        String[][] permissions = {
            {"USER_READ", "사용자 조회", "SYSTEM"},
            {"USER_WRITE", "사용자 쓰기", "SYSTEM"},
            {"ROLE_READ", "역할 조회", "SYSTEM"},
            {"ROLE_WRITE", "역할 쓰기", "SYSTEM"},
            {"PRODUCT_READ", "제품 조회", "PRODUCTION"},
            {"PRODUCT_WRITE", "제품 쓰기", "PRODUCTION"},
            {"PROCESS_READ", "공정 조회", "PRODUCTION"},
            {"PROCESS_WRITE", "공정 쓰기", "PRODUCTION"},
            {"WORK_ORDER_READ", "작업지시 조회", "PRODUCTION"},
            {"WORK_ORDER_WRITE", "작업지시 쓰기", "PRODUCTION"},
            {"WORK_RESULT_READ", "작업실적 조회", "PRODUCTION"},
            {"WORK_RESULT_WRITE", "작업실적 쓰기", "PRODUCTION"},
        };

        for (String[] perm : permissions) {
            PermissionEntity permission = PermissionEntity.builder()
                    .permissionCode(perm[0])
                    .permissionName(perm[1])
                    .module(perm[2])
                    .status("active")
                    .build();
            permissionRepository.save(permission);
        }
    }

    private void assignPermissionsToRole(RoleEntity role, TenantEntity tenant) {
        if ("ADMIN".equals(role.getRoleCode())) {
            // Admin gets all permissions
            permissionRepository.findAll().forEach(permission -> {
                RolePermissionEntity rp = RolePermissionEntity.builder()
                        .role(role)
                        .permission(permission)
                        .build();
                rolePermissionRepository.save(rp);
            });
        } else if ("PRODUCTION_MANAGER".equals(role.getRoleCode())) {
            // Production manager gets production permissions
            permissionRepository.findAll().stream()
                    .filter(p -> "PRODUCTION".equals(p.getModule()))
                    .forEach(permission -> {
                        RolePermissionEntity rp = RolePermissionEntity.builder()
                                .role(role)
                                .permission(permission)
                                .build();
                        rolePermissionRepository.save(rp);
                    });
        }
    }

    private UserEntity createUser(TenantEntity tenant, String username, String fullName,
                                   String email, String password) {
        UserEntity user = UserEntity.builder()
                .tenant(tenant)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .email(email)
                .fullName(fullName)
                .status("active")
                .preferredLanguage("ko")
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .build();
        return userRepository.save(user);
    }

    private void assignRoleToUser(UserEntity user, RoleEntity role) {
        UserRoleEntity userRole = UserRoleEntity.builder()
                .user(user)
                .role(role)
                .build();
        userRoleRepository.save(userRole);
    }

    private void createProducts(TenantEntity tenant) {
        String[][] products = {
            {"P-LCD-001", "32인치 LCD 패널", "완제품", "1920x1080, IPS", "EA"},
            {"P-LCD-002", "43인치 LCD 패널", "완제품", "3840x2160, VA", "EA"},
            {"P-PCB-001", "LCD 구동 PCB", "반제품", "4-Layer PCB", "EA"},
            {"P-BL-001", "백라이트 유닛", "반제품", "LED Type", "EA"},
            {"P-GLASS-001", "강화 유리", "원자재", "0.5mm 두께", "EA"},
        };

        for (String[] prod : products) {
            ProductEntity product = ProductEntity.builder()
                    .tenant(tenant)
                    .productCode(prod[0])
                    .productName(prod[1])
                    .productType(prod[2])
                    .specification(prod[3])
                    .unit(prod[4])
                    .standardCycleTime(600) // 10 minutes
                    .isActive(true)
                    .description("테스트용 " + prod[1])
                    .build();
            productRepository.save(product);
        }
    }

    private void createProcesses(TenantEntity tenant) {
        String[][] processes = {
            {"PROC-001", "PCB 제조", "제조", "1"},
            {"PROC-002", "SMT 실장", "조립", "2"},
            {"PROC-003", "패널 조립", "조립", "3"},
            {"PROC-004", "기능 검사", "검사", "4"},
            {"PROC-005", "포장", "포장", "5"},
        };

        for (String[] proc : processes) {
            ProcessEntity process = ProcessEntity.builder()
                    .tenant(tenant)
                    .processCode(proc[0])
                    .processName(proc[1])
                    .processType(proc[2])
                    .sequenceOrder(Integer.parseInt(proc[3]))
                    .isActive(true)
                    .description("테스트용 " + proc[1] + " 공정")
                    .build();
            processRepository.save(process);
        }
    }

    private void createWorkOrders(TenantEntity tenant) {
        ProductEntity product1 = productRepository.findByTenant_TenantIdAndProductCode("DEMO001", "P-LCD-001")
                .orElseThrow();
        ProductEntity product2 = productRepository.findByTenant_TenantIdAndProductCode("DEMO001", "P-LCD-002")
                .orElseThrow();

        ProcessEntity process1 = processRepository.findByTenant_TenantIdAndProcessCode("DEMO001", "PROC-001")
                .orElseThrow();
        ProcessEntity process3 = processRepository.findByTenant_TenantIdAndProcessCode("DEMO001", "PROC-003")
                .orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        // Work Order 1 - READY
        WorkOrderEntity wo1 = WorkOrderEntity.builder()
                .tenant(tenant)
                .workOrderNo("WO-2026-001")
                .product(product1)
                .process(process1)
                .status("READY")
                .plannedQuantity(new BigDecimal("100"))
                .actualQuantity(BigDecimal.ZERO)
                .goodQuantity(BigDecimal.ZERO)
                .defectQuantity(BigDecimal.ZERO)
                .plannedStartDate(now.minusDays(1))
                .plannedEndDate(now.plusDays(2))
                .priority(3)
                .remarks("긴급 주문 - 우선 생산")
                .build();
        workOrderRepository.save(wo1);

        // Work Order 2 - IN_PROGRESS
        WorkOrderEntity wo2 = WorkOrderEntity.builder()
                .tenant(tenant)
                .workOrderNo("WO-2026-002")
                .product(product2)
                .process(process3)
                .status("IN_PROGRESS")
                .plannedQuantity(new BigDecimal("50"))
                .actualQuantity(new BigDecimal("20"))
                .goodQuantity(new BigDecimal("18"))
                .defectQuantity(new BigDecimal("2"))
                .plannedStartDate(now.minusDays(2))
                .plannedEndDate(now.plusDays(1))
                .actualStartDate(now.minusDays(2))
                .priority(5)
                .remarks("정상 생산")
                .build();
        workOrderRepository.save(wo2);

        // Work Order 3 - PENDING
        WorkOrderEntity wo3 = WorkOrderEntity.builder()
                .tenant(tenant)
                .workOrderNo("WO-2026-003")
                .product(product1)
                .process(process3)
                .status("PENDING")
                .plannedQuantity(new BigDecimal("200"))
                .actualQuantity(BigDecimal.ZERO)
                .goodQuantity(BigDecimal.ZERO)
                .defectQuantity(BigDecimal.ZERO)
                .plannedStartDate(now.plusDays(1))
                .plannedEndDate(now.plusDays(5))
                .priority(7)
                .remarks("대량 주문")
                .build();
        workOrderRepository.save(wo3);
    }
}
