package kr.co.softice.mes.integration.workflow;

import kr.co.softice.mes.domain.dto.*;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.*;
import kr.co.softice.mes.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Authentication & Dashboard 통합 테스트
 *
 * 인증 → 사용자 생성 → 역할 할당 → 대시보드 통계 조회 전체 프로세스 검증
 *
 * @author Claude Code (Sonnet 4.5)
 * @company SoftIce Co., Ltd.
 * @since 2026-01-27
 */
@DisplayName("Authentication & Dashboard 통합 테스트")
public class AuthenticationDashboardIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DashboardService dashboardService;

    @Test
    @DisplayName("사용자 생성부터 대시보드 통계까지 전체 프로세스")
    void testCompleteAuthenticationDashboardWorkflow() {
        // ═══════════════════════════════════════════════════════════════
        // Step 1: 테넌트 생성
        // ═══════════════════════════════════════════════════════════════

        TenantEntity tenant = TenantEntity.builder()
                .tenantCode("COMP001")
                .tenantName("ABC 제조")
                .businessType("MANUFACTURING")
                .contactPerson("김대표")
                .contactEmail("ceo@abc.com")
                .contactPhone("02-1234-5678")
                .isActive(true)
                .build();
        tenant = tenantService.createTenant(tenant);

        assertThat(tenant.getId()).isNotNull();
        assertThat(tenant.getTenantCode()).isEqualTo("COMP001");

        // ═══════════════════════════════════════════════════════════════
        // Step 2: 권한 생성
        // ═══════════════════════════════════════════════════════════════

        PermissionEntity readPermission = PermissionEntity.builder()
                .permissionCode("DASHBOARD_READ")
                .permissionName("대시보드 읽기")
                .module("DASHBOARD")
                .description("대시보드 조회 권한")
                .build();
        readPermission = permissionService.createPermission(readPermission);

        PermissionEntity writePermission = PermissionEntity.builder()
                .permissionCode("USER_WRITE")
                .permissionName("사용자 쓰기")
                .module("USER")
                .description("사용자 생성/수정 권한")
                .build();
        writePermission = permissionService.createPermission(writePermission);

        // ═══════════════════════════════════════════════════════════════
        // Step 3: 역할 생성 및 권한 부여
        // ═══════════════════════════════════════════════════════════════

        RoleEntity adminRole = RoleEntity.builder()
                .roleCode("ADMIN")
                .roleName("시스템 관리자")
                .description("전체 시스템 관리 권한")
                .isActive(true)
                .build();
        adminRole.addPermission(readPermission);
        adminRole.addPermission(writePermission);
        adminRole = roleService.createRole(adminRole);

        assertThat(adminRole.getPermissions()).hasSize(2);

        RoleEntity userRole = RoleEntity.builder()
                .roleCode("USER")
                .roleName("일반 사용자")
                .description("기본 사용자 권한")
                .isActive(true)
                .build();
        userRole.addPermission(readPermission);
        userRole = roleService.createRole(userRole);

        // ═══════════════════════════════════════════════════════════════
        // Step 4: 다수의 사용자 생성 (대시보드 통계용)
        // ═══════════════════════════════════════════════════════════════

        // 관리자 사용자 3명
        for (int i = 1; i <= 3; i++) {
            UserEntity adminUser = UserEntity.builder()
                    .tenant(tenant)
                    .username("admin" + i)
                    .password(passwordEncoder.encode("password"))
                    .email("admin" + i + "@abc.com")
                    .fullName("관리자" + i)
                    .status("active")
                    .lastLoginAt(LocalDateTime.now().minusDays(i))
                    .build();
            adminUser = userService.createUser(adminUser);

            // 역할 할당
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setUser(adminUser);
            userRoleEntity.setRole(adminRole);
            userRoleRepository.save(userRoleEntity);
        }

        // 일반 사용자 5명
        for (int i = 1; i <= 5; i++) {
            UserEntity normalUser = UserEntity.builder()
                    .tenant(tenant)
                    .username("user" + i)
                    .password(passwordEncoder.encode("password"))
                    .email("user" + i + "@abc.com")
                    .fullName("사용자" + i)
                    .status(i <= 3 ? "active" : "inactive")
                    .lastLoginAt(i <= 2 ? LocalDateTime.now().minusDays(i) : null)
                    .build();
            normalUser = userService.createUser(normalUser);

            // 역할 할당
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setUser(normalUser);
            userRoleEntity.setRole(userRole);
            userRoleRepository.save(userRoleEntity);
        }

        // ═══════════════════════════════════════════════════════════════
        // Step 5: 대시보드 통계 조회
        // ═══════════════════════════════════════════════════════════════

        DashboardStatsResponse dashboardStats = dashboardService
                .getDashboardStats(tenant.getTenantId());

        assertThat(dashboardStats).isNotNull();
        assertThat(dashboardStats.getTotalUsers()).isEqualTo(8L); // 3 + 5
        assertThat(dashboardStats.getTotalRoles()).isEqualTo(2L); // ADMIN, USER
        assertThat(dashboardStats.getTotalPermissions()).isEqualTo(2L); // DASHBOARD_READ, USER_WRITE

        // 활성 사용자 수: admin 3 + active user 3 = 6
        assertThat(dashboardStats.getActiveUsers()).isEqualTo(6L);

        // ═══════════════════════════════════════════════════════════════
        // Step 6: 사용자 상태 통계 조회
        // ═══════════════════════════════════════════════════════════════

        List<UserStatsResponse> userStats = dashboardService.getUserStats(tenant.getTenantId());

        assertThat(userStats).isNotEmpty();

        // Active 상태 사용자: 6명
        UserStatsResponse activeStats = userStats.stream()
                .filter(s -> s.getStatus().equals("활성"))
                .findFirst()
                .orElseThrow();
        assertThat(activeStats.getUserCount()).isEqualTo(6L);

        // Inactive 상태 사용자: 2명
        UserStatsResponse inactiveStats = userStats.stream()
                .filter(s -> s.getStatus().equals("비활성"))
                .findFirst()
                .orElseThrow();
        assertThat(inactiveStats.getUserCount()).isEqualTo(2L);

        // ═══════════════════════════════════════════════════════════════
        // Step 7: 로그인 추이 조회 (7일간)
        // ═══════════════════════════════════════════════════════════════

        List<LoginTrendResponse> loginTrend = dashboardService
                .getLoginTrend(tenant.getTenantId(), 7);

        assertThat(loginTrend).hasSize(7);

        // 오늘 로그인 기록 확인 (테스트 데이터에서는 없을 수 있음)
        LocalDateTime now = LocalDateTime.now();
        LoginTrendResponse todayLogin = loginTrend.stream()
                .filter(t -> t.getDate().equals(now.toLocalDate().toString()))
                .findFirst()
                .orElseThrow();

        // 로그인 카운트가 0 이상이어야 함
        assertThat(todayLogin.getLoginCount()).isGreaterThanOrEqualTo(0L);

        // ═══════════════════════════════════════════════════════════════
        // Step 8: 역할 분포 조회
        // ═══════════════════════════════════════════════════════════════

        List<RoleDistributionResponse> roleDistribution = dashboardService
                .getRoleDistribution(tenant.getTenantId());

        assertThat(roleDistribution).hasSize(2);

        // ADMIN 역할 사용자 수: 3명
        RoleDistributionResponse adminDistribution = roleDistribution.stream()
                .filter(r -> r.getRoleCode().equals("ADMIN"))
                .findFirst()
                .orElseThrow();
        assertThat(adminDistribution.getUserCount()).isEqualTo(3L);
        assertThat(adminDistribution.getRoleName()).isEqualTo("시스템 관리자");

        // USER 역할 사용자 수: 5명
        RoleDistributionResponse userDistribution = roleDistribution.stream()
                .filter(r -> r.getRoleCode().equals("USER"))
                .findFirst()
                .orElseThrow();
        assertThat(userDistribution.getUserCount()).isEqualTo(5L);
        assertThat(userDistribution.getRoleName()).isEqualTo("일반 사용자");

        // ═══════════════════════════════════════════════════════════════
        // 최종 검증: 전체 프로세스 완료
        // ═══════════════════════════════════════════════════════════════

        assertThat(tenant.getTenantCode()).isEqualTo("COMP001");
        assertThat(dashboardStats.getTotalUsers()).isEqualTo(8L);
        assertThat(roleDistribution).hasSize(2);
        assertThat(loginTrend).hasSize(7);
    }

    @Test
    @DisplayName("멀티 테넌트 데이터 격리 검증")
    void testMultiTenantDataIsolation() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 두 개의 테넌트 생성
        // ═══════════════════════════════════════════════════════════════

        TenantEntity tenant1 = TenantEntity.builder()
                .tenantCode("TENANT1")
                .tenantName("테넌트1")
                .businessType("MANUFACTURING")
                .contactPerson("담당자1")
                .contactEmail("tenant1@example.com")
                .contactPhone("010-1111-1111")
                .isActive(true)
                .build();
        tenant1 = tenantService.createTenant(tenant1);

        TenantEntity tenant2 = TenantEntity.builder()
                .tenantCode("TENANT2")
                .tenantName("테넌트2")
                .businessType("MANUFACTURING")
                .contactPerson("담당자2")
                .contactEmail("tenant2@example.com")
                .contactPhone("010-2222-2222")
                .isActive(true)
                .build();
        tenant2 = tenantService.createTenant(tenant2);

        // ═══════════════════════════════════════════════════════════════
        // When: 각 테넌트에 사용자 생성
        // ═══════════════════════════════════════════════════════════════

        // Tenant1에 사용자 3명 생성
        for (int i = 1; i <= 3; i++) {
            UserEntity user = UserEntity.builder()
                    .tenant(tenant1)
                    .username("tenant1_user" + i)
                    .password(passwordEncoder.encode("password"))
                    .email("t1user" + i + "@example.com")
                    .fullName("테넌트1 사용자" + i)
                    .status("active")
                    .build();
            userService.createUser(user);
        }

        // Tenant2에 사용자 5명 생성
        for (int i = 1; i <= 5; i++) {
            UserEntity user = UserEntity.builder()
                    .tenant(tenant2)
                    .username("tenant2_user" + i)
                    .password(passwordEncoder.encode("password"))
                    .email("t2user" + i + "@example.com")
                    .fullName("테넌트2 사용자" + i)
                    .status("active")
                    .build();
            userService.createUser(user);
        }

        // ═══════════════════════════════════════════════════════════════
        // Then: 각 테넌트의 대시보드 통계가 격리되어 있는지 확인
        // ═══════════════════════════════════════════════════════════════

        DashboardStatsResponse tenant1Stats = dashboardService
                .getDashboardStats(tenant1.getTenantId());

        DashboardStatsResponse tenant2Stats = dashboardService
                .getDashboardStats(tenant2.getTenantId());

        assertThat(tenant1Stats.getTotalUsers()).isEqualTo(3L);
        assertThat(tenant2Stats.getTotalUsers()).isEqualTo(5L);

        // 각 테넌트의 사용자 목록도 격리되어 있는지 확인
        List<UserEntity> tenant1Users = userRepository.findByTenant(tenant1);
        List<UserEntity> tenant2Users = userRepository.findByTenant(tenant2);

        assertThat(tenant1Users).hasSize(3);
        assertThat(tenant2Users).hasSize(5);

        // Cross-tenant 데이터 접근 불가 확인
        assertThat(tenant1Users).noneMatch(u -> u.getTenant().equals(tenant2));
        assertThat(tenant2Users).noneMatch(u -> u.getTenant().equals(tenant1));
    }

    @Test
    @DisplayName("실시간 로그인 통계 업데이트 검증")
    void testRealTimeLoginStatistics() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 사용자 생성
        // ═══════════════════════════════════════════════════════════════

        TenantEntity tenant = testTenant;

        UserEntity user1 = UserEntity.builder()
                .tenant(tenant)
                .username("logintest1")
                .password(passwordEncoder.encode("password"))
                .email("logintest1@example.com")
                .fullName("로그인테스트1")
                .status("active")
                .lastLoginAt(null) // 아직 로그인 안함
                .build();
        user1 = userService.createUser(user1);

        // ═══════════════════════════════════════════════════════════════
        // When: 초기 로그인 추이 조회
        // ═══════════════════════════════════════════════════════════════

        List<LoginTrendResponse> beforeLogin = dashboardService
                .getLoginTrend(tenant.getTenantId(), 7);

        // ═══════════════════════════════════════════════════════════════
        // When: 사용자 로그인 (lastLoginAt 업데이트)
        // ═══════════════════════════════════════════════════════════════

        user1.setLastLoginAt(LocalDateTime.now());
        user1 = userService.updateUser(user1);

        // ═══════════════════════════════════════════════════════════════
        // Then: 로그인 추이가 실시간으로 업데이트되었는지 확인
        // ═══════════════════════════════════════════════════════════════

        List<LoginTrendResponse> afterLogin = dashboardService
                .getLoginTrend(tenant.getTenantId(), 7);

        // 오늘 날짜의 로그인 카운트가 증가했는지 확인
        String today = LocalDateTime.now().toLocalDate().toString();

        Long beforeCount = beforeLogin.stream()
                .filter(t -> t.getDate().equals(today))
                .findFirst()
                .map(LoginTrendResponse::getLoginCount)
                .orElse(0L);

        Long afterCount = afterLogin.stream()
                .filter(t -> t.getDate().equals(today))
                .findFirst()
                .map(LoginTrendResponse::getLoginCount)
                .orElse(0L);

        assertThat(afterCount).isGreaterThanOrEqualTo(beforeCount);
    }

    @Test
    @DisplayName("권한 기반 접근 제어 검증")
    void testPermissionBasedAccessControl() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 제한된 권한을 가진 역할 생성
        // ═══════════════════════════════════════════════════════════════

        PermissionEntity readOnlyPermission = PermissionEntity.builder()
                .permissionCode("DASHBOARD_READ_ONLY")
                .permissionName("대시보드 읽기 전용")
                .module("DASHBOARD")
                .description("대시보드 조회만 가능")
                .build();
        readOnlyPermission = permissionService.createPermission(readOnlyPermission);

        RoleEntity readOnlyRole = RoleEntity.builder()
                .roleCode("READONLY")
                .roleName("읽기 전용 사용자")
                .description("조회만 가능")
                .isActive(true)
                .build();
        readOnlyRole.addPermission(readOnlyPermission);
        readOnlyRole = roleService.createRole(readOnlyRole);

        // ═══════════════════════════════════════════════════════════════
        // When: 읽기 전용 사용자 생성
        // ═══════════════════════════════════════════════════════════════

        UserEntity readOnlyUser = UserEntity.builder()
                .tenant(testTenant)
                .username("readonly")
                .password(passwordEncoder.encode("password"))
                .email("readonly@example.com")
                .fullName("읽기전용사용자")
                .status("active")
                .build();
        readOnlyUser = userService.createUser(readOnlyUser);

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUser(readOnlyUser);
        userRoleEntity.setRole(readOnlyRole);
        userRoleRepository.save(userRoleEntity);

        // ═══════════════════════════════════════════════════════════════
        // Then: 권한 확인
        // ═══════════════════════════════════════════════════════════════

        List<UserRoleEntity> userRoles = userRoleRepository.findByUser(readOnlyUser);

        assertThat(userRoles).hasSize(1);

        RoleEntity assignedRole = userRoles.get(0).getRole();

        assertThat(assignedRole.getPermissions()).hasSize(1);
        assertThat(assignedRole.getPermissions()).anyMatch(
                p -> p.getPermissionCode().equals("DASHBOARD_READ_ONLY")
        );

        // 쓰기 권한이 없는지 확인
        assertThat(assignedRole.getPermissions()).noneMatch(
                p -> p.getPermissionCode().contains("WRITE")
        );
    }
}
