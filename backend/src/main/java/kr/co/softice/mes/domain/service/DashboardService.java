package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.dashboard.DashboardStatsResponse;
import kr.co.softice.mes.common.dto.dashboard.LoginTrendResponse;
import kr.co.softice.mes.common.dto.dashboard.RoleDistributionResponse;
import kr.co.softice.mes.common.dto.dashboard.UserStatsResponse;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard Service
 * 대시보드 통계 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TenantRepository tenantRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * 대시보드 통계 조회
     */
    public DashboardStatsResponse getDashboardStats(String tenantId) {
        log.debug("Getting dashboard stats for tenant: {}", tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        // 전체 사용자 수
        long totalUsers = userRepository.countByTenant(tenant);

        // 활성 사용자 수
        long activeUsers = userRepository.countByTenantAndStatus(tenant, "active");

        // 전체 역할 수
        long totalRoles = roleRepository.countByTenant(tenant);

        // 전체 권한 수 (권한은 테넌트 무관)
        long totalPermissions = permissionRepository.count();

        // 오늘 로그인 수 (lastLoginAt 기준)
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayLogins = userRepository.countByTenantAndLastLoginAtAfter(tenant, todayStart);

        // 활성 세션 수 (최근 30분 이내 로그인)
        LocalDateTime recentLoginTime = LocalDateTime.now().minusMinutes(30);
        long activeSessions = userRepository.countByTenantAndLastLoginAtAfter(tenant, recentLoginTime);

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalRoles(totalRoles)
                .totalPermissions(totalPermissions)
                .todayLogins(todayLogins)
                .activeSessions(activeSessions)
                .build();
    }

    /**
     * 사용자 상태별 통계
     */
    public List<UserStatsResponse> getUserStats(String tenantId) {
        log.debug("Getting user stats for tenant: {}", tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        List<UserStatsResponse> stats = new ArrayList<>();

        // 활성 사용자
        long activeCount = userRepository.countByTenantAndStatus(tenant, "active");
        stats.add(UserStatsResponse.builder()
                .status("active")
                .count(activeCount)
                .displayName("활성")
                .build());

        // 비활성 사용자
        long inactiveCount = userRepository.countByTenantAndStatus(tenant, "inactive");
        stats.add(UserStatsResponse.builder()
                .status("inactive")
                .count(inactiveCount)
                .displayName("비활성")
                .build());

        // 잠긴 사용자
        long lockedCount = userRepository.countByTenantAndStatus(tenant, "locked");
        stats.add(UserStatsResponse.builder()
                .status("locked")
                .count(lockedCount)
                .displayName("잠김")
                .build());

        return stats;
    }

    /**
     * 일별 로그인 추이 조회
     * @param tenantId 테넌트 ID
     * @param days 조회할 일수 (기본 7일)
     */
    public List<LoginTrendResponse> getLoginTrend(String tenantId, int days) {
        log.debug("Getting login trend for tenant: {}, days: {}", tenantId, days);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 날짜별로 데이터 초기화
        List<LoginTrendResponse> trendList = new ArrayList<>();
        Map<LocalDate, Long> loginCountByDate = new java.util.HashMap<>();

        // 초기값 0으로 설정
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            loginCountByDate.put(date, 0L);
        }

        // 기간 내 로그인한 사용자 조회
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<UserEntity> users = userRepository.findByTenantAndLastLoginAtBetween(
                tenant, startDateTime, endDateTime);

        // 날짜별로 그룹핑하여 count
        for (UserEntity user : users) {
            if (user.getLastLoginAt() != null) {
                LocalDate loginDate = user.getLastLoginAt().toLocalDate();
                if (!loginDate.isBefore(startDate) && !loginDate.isAfter(endDate)) {
                    loginCountByDate.merge(loginDate, 1L, Long::sum);
                }
            }
        }

        // 결과 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            trendList.add(LoginTrendResponse.builder()
                    .date(date)
                    .loginCount(loginCountByDate.getOrDefault(date, 0L))
                    .dateLabel(date.format(formatter))
                    .build());
        }

        return trendList;
    }

    /**
     * 역할별 사용자 분포 조회
     */
    public List<RoleDistributionResponse> getRoleDistribution(String tenantId) {
        log.debug("Getting role distribution for tenant: {}", tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        // 역할별 사용자 수 조회
        List<Object[]> results = userRoleRepository.countUsersByRoleForTenant(tenant);

        // DTO로 변환
        return results.stream()
                .map(row -> RoleDistributionResponse.builder()
                        .roleCode((String) row[0])
                        .roleName((String) row[1])
                        .userCount((Long) row[2])
                        .build())
                .collect(Collectors.toList());
    }
}
