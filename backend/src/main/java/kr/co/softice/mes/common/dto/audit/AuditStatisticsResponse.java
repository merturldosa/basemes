package kr.co.softice.mes.common.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit Statistics Response DTO
 * 감사 로그 통계 정보
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditStatisticsResponse {

    /**
     * 조회 기간 시작
     */
    private LocalDateTime startDate;

    /**
     * 조회 기간 종료
     */
    private LocalDateTime endDate;

    /**
     * 작업 유형별 카운트
     * Key: 작업 유형 (CREATE, UPDATE, DELETE, etc.)
     * Value: 카운트
     */
    private Map<String, Long> actionStatistics;

    /**
     * 사용자별 활동 카운트
     * Key: 사용자명
     * Value: 카운트
     */
    private Map<String, Long> userActivityStatistics;

    /**
     * 전체 로그 수
     */
    private Long totalLogs;

    /**
     * 성공한 작업 수
     */
    private Long successfulOperations;

    /**
     * 실패한 작업 수
     */
    private Long failedOperations;
}
