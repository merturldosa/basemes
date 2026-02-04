package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.dashboard.DashboardStatsResponse;
import kr.co.softice.mes.common.dto.dashboard.LoginTrendResponse;
import kr.co.softice.mes.common.dto.dashboard.RoleDistributionResponse;
import kr.co.softice.mes.common.dto.dashboard.UserStatsResponse;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Dashboard Service Test
 * 대시보드 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 서비스 테스트")
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private TenantEntity testTenant;
    private String tenantId;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);

        testUser = new UserEntity();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setStatus("active");
        testUser.setTenant(testTenant);
        testUser.setLastLoginAt(LocalDateTime.now().minusHours(1));
    }

    // === 대시보드 통계 조회 테스트 ===

    @Test
    @DisplayName("대시보드 통계 조회 - 성공")
    void testGetDashboardStats_Success() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.countByTenant(testTenant))
                .thenReturn(100L);
        when(userRepository.countByTenantAndStatus(testTenant, "active"))
                .thenReturn(85L);
        when(roleRepository.countByTenant(testTenant))
                .thenReturn(10L);
        when(permissionRepository.count())
                .thenReturn(50L);
        when(userRepository.countByTenantAndLastLoginAtAfter(eq(testTenant), any(LocalDateTime.class)))
                .thenReturn(30L)  // today logins
                .thenReturn(15L); // active sessions

        DashboardStatsResponse result = dashboardService.getDashboardStats(tenantId);

        assertThat(result).isNotNull();
        assertThat(result.getTotalUsers()).isEqualTo(100L);
        assertThat(result.getActiveUsers()).isEqualTo(85L);
        assertThat(result.getTotalRoles()).isEqualTo(10L);
        assertThat(result.getTotalPermissions()).isEqualTo(50L);
        assertThat(result.getTodayLogins()).isEqualTo(30L);
        assertThat(result.getActiveSessions()).isEqualTo(15L);
    }

    @Test
    @DisplayName("대시보드 통계 조회 - 실패 (테넌트 없음)")
    void testGetDashboardStats_Fail_TenantNotFound() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getDashboardStats(tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("대시보드 통계 조회 - 모든 값 0")
    void testGetDashboardStats_AllZero() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.countByTenant(testTenant))
                .thenReturn(0L);
        when(userRepository.countByTenantAndStatus(testTenant, "active"))
                .thenReturn(0L);
        when(roleRepository.countByTenant(testTenant))
                .thenReturn(0L);
        when(permissionRepository.count())
                .thenReturn(0L);
        when(userRepository.countByTenantAndLastLoginAtAfter(eq(testTenant), any(LocalDateTime.class)))
                .thenReturn(0L)
                .thenReturn(0L);

        DashboardStatsResponse result = dashboardService.getDashboardStats(tenantId);

        assertThat(result).isNotNull();
        assertThat(result.getTotalUsers()).isZero();
        assertThat(result.getActiveUsers()).isZero();
        assertThat(result.getTotalRoles()).isZero();
        assertThat(result.getTotalPermissions()).isZero();
        assertThat(result.getTodayLogins()).isZero();
        assertThat(result.getActiveSessions()).isZero();
    }

    // === 사용자 상태별 통계 테스트 ===

    @Test
    @DisplayName("사용자 상태별 통계 - 성공")
    void testGetUserStats_Success() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.countByTenantAndStatus(testTenant, "active"))
                .thenReturn(85L);
        when(userRepository.countByTenantAndStatus(testTenant, "inactive"))
                .thenReturn(10L);
        when(userRepository.countByTenantAndStatus(testTenant, "locked"))
                .thenReturn(5L);

        List<UserStatsResponse> result = dashboardService.getUserStats(tenantId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStatus()).isEqualTo("active");
        assertThat(result.get(0).getCount()).isEqualTo(85L);
        assertThat(result.get(0).getDisplayName()).isEqualTo("활성");
        assertThat(result.get(1).getStatus()).isEqualTo("inactive");
        assertThat(result.get(1).getCount()).isEqualTo(10L);
        assertThat(result.get(2).getStatus()).isEqualTo("locked");
        assertThat(result.get(2).getCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("사용자 상태별 통계 - 실패 (테넌트 없음)")
    void testGetUserStats_Fail_TenantNotFound() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getUserStats(tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    // === 로그인 추이 조회 테스트 ===

    @Test
    @DisplayName("로그인 추이 조회 - 성공 (7일)")
    void testGetLoginTrend_Success_7Days() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        UserEntity user1 = new UserEntity();
        user1.setLastLoginAt(LocalDateTime.now().minusDays(1));
        UserEntity user2 = new UserEntity();
        user2.setLastLoginAt(LocalDateTime.now().minusDays(2));
        UserEntity user3 = new UserEntity();
        user3.setLastLoginAt(LocalDateTime.now().minusDays(2));

        when(userRepository.findByTenantAndLastLoginAtBetween(
                eq(testTenant), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(user1, user2, user3));

        List<LoginTrendResponse> result = dashboardService.getLoginTrend(tenantId, 7);

        assertThat(result).hasSize(7);
        assertThat(result).allMatch(r -> r.getDate() != null);
        assertThat(result).allMatch(r -> r.getDateLabel() != null);
        assertThat(result).allMatch(r -> r.getLoginCount() >= 0);
    }

    @Test
    @DisplayName("로그인 추이 조회 - 데이터 없음")
    void testGetLoginTrend_NoData() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.findByTenantAndLastLoginAtBetween(
                eq(testTenant), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        List<LoginTrendResponse> result = dashboardService.getLoginTrend(tenantId, 7);

        assertThat(result).hasSize(7);
        assertThat(result).allMatch(r -> r.getLoginCount() == 0L);
    }

    @Test
    @DisplayName("로그인 추이 조회 - 30일")
    void testGetLoginTrend_30Days() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.findByTenantAndLastLoginAtBetween(
                eq(testTenant), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        List<LoginTrendResponse> result = dashboardService.getLoginTrend(tenantId, 30);

        assertThat(result).hasSize(30);
    }

    @Test
    @DisplayName("로그인 추이 조회 - 실패 (테넌트 없음)")
    void testGetLoginTrend_Fail_TenantNotFound() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getLoginTrend(tenantId, 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("로그인 추이 조회 - null lastLoginAt 처리")
    void testGetLoginTrend_NullLastLoginAt() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        UserEntity userWithoutLogin = new UserEntity();
        userWithoutLogin.setLastLoginAt(null);

        when(userRepository.findByTenantAndLastLoginAtBetween(
                eq(testTenant), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(userWithoutLogin));

        List<LoginTrendResponse> result = dashboardService.getLoginTrend(tenantId, 7);

        assertThat(result).hasSize(7);
        assertThat(result).allMatch(r -> r.getLoginCount() == 0L);
    }

    // === 역할별 사용자 분포 테스트 ===

    @Test
    @DisplayName("역할별 사용자 분포 - 성공")
    void testGetRoleDistribution_Success() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"ADMIN", "관리자", 10L},
                new Object[]{"USER", "사용자", 85L},
                new Object[]{"MANAGER", "매니저", 15L}
        );

        when(userRoleRepository.countUsersByRoleForTenant(testTenant))
                .thenReturn(mockResults);

        List<RoleDistributionResponse> result = dashboardService.getRoleDistribution(tenantId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRoleCode()).isEqualTo("ADMIN");
        assertThat(result.get(0).getRoleName()).isEqualTo("관리자");
        assertThat(result.get(0).getUserCount()).isEqualTo(10L);
        assertThat(result.get(1).getRoleCode()).isEqualTo("USER");
        assertThat(result.get(1).getUserCount()).isEqualTo(85L);
        assertThat(result.get(2).getRoleCode()).isEqualTo("MANAGER");
    }

    @Test
    @DisplayName("역할별 사용자 분포 - 데이터 없음")
    void testGetRoleDistribution_NoData() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRoleRepository.countUsersByRoleForTenant(testTenant))
                .thenReturn(new ArrayList<>());

        List<RoleDistributionResponse> result = dashboardService.getRoleDistribution(tenantId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("역할별 사용자 분포 - 실패 (테넌트 없음)")
    void testGetRoleDistribution_Fail_TenantNotFound() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getRoleDistribution(tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }
}
