package kr.co.softice.mes.common.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard Statistics Response DTO
 * 대시보드 통계 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    /**
     * 전체 사용자 수
     */
    private Long totalUsers;

    /**
     * 활성 사용자 수
     */
    private Long activeUsers;

    /**
     * 전체 역할 수
     */
    private Long totalRoles;

    /**
     * 전체 권한 수
     */
    private Long totalPermissions;

    /**
     * 오늘 로그인 수
     */
    private Long todayLogins;

    /**
     * 현재 활성 세션 수 (예상치)
     */
    private Long activeSessions;
}
