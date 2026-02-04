package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.AuditLogEntity;
import kr.co.softice.mes.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Audit Log Service
 * 감사 로그 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * ID로 감사 로그 조회
     */
    public AuditLogEntity findById(Long auditId) {
        return auditLogRepository.findById(auditId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));
    }

    /**
     * 테넌트별 감사 로그 조회
     */
    public Page<AuditLogEntity> findByTenant(String tenantId, Pageable pageable) {
        return auditLogRepository.findByTenantId(tenantId, pageable);
    }

    /**
     * 사용자별 감사 로그 조회
     */
    public Page<AuditLogEntity> findByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * 작업 유형별 감사 로그 조회
     */
    public Page<AuditLogEntity> findByAction(String tenantId, String action, Pageable pageable) {
        return auditLogRepository.findByTenantIdAndAction(tenantId, action, pageable);
    }

    /**
     * 엔티티별 감사 로그 조회 (특정 엔티티의 모든 변경 이력)
     */
    public Page<AuditLogEntity> findByEntity(
            String tenantId,
            String entityType,
            String entityId,
            Pageable pageable) {
        return auditLogRepository.findByTenantIdAndEntityTypeAndEntityId(
                tenantId, entityType, entityId, pageable
        );
    }

    /**
     * 기간별 감사 로그 조회
     */
    public Page<AuditLogEntity> findByDateRange(
            String tenantId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return auditLogRepository.findByTenantIdAndDateRange(
                tenantId, startDate, endDate, pageable
        );
    }

    /**
     * 성공/실패별 감사 로그 조회
     */
    public Page<AuditLogEntity> findBySuccess(
            String tenantId,
            Boolean success,
            Pageable pageable) {
        return auditLogRepository.findByTenantIdAndSuccess(tenantId, success, pageable);
    }

    /**
     * IP 주소별 감사 로그 조회
     */
    public Page<AuditLogEntity> findByIpAddress(
            String tenantId,
            String ipAddress,
            Pageable pageable) {
        return auditLogRepository.findByTenantIdAndIpAddress(tenantId, ipAddress, pageable);
    }

    /**
     * 복합 조건 검색
     */
    public Page<AuditLogEntity> searchAuditLogs(
            String tenantId,
            String username,
            String action,
            String entityType,
            Boolean success,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return auditLogRepository.searchAuditLogs(
                tenantId, username, action, entityType, success, startDate, endDate, pageable
        );
    }

    /**
     * 작업 통계 - 작업 유형별
     */
    public Map<String, Long> getActionStatistics(
            String tenantId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<Object[]> results = auditLogRepository.countByActionAndDateRange(
                tenantId, startDate, endDate
        );

        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            String action = (String) result[0];
            Long count = (Long) result[1];
            statistics.put(action, count);
        }

        return statistics;
    }

    /**
     * 사용자 활동 통계
     */
    public Map<String, Long> getUserActivityStatistics(
            String tenantId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<Object[]> results = auditLogRepository.countByUserAndDateRange(
                tenantId, startDate, endDate
        );

        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            String username = (String) result[0];
            Long count = (Long) result[1];
            statistics.put(username, count);
        }

        return statistics;
    }

    /**
     * 감사 로그 수동 생성 (특수한 경우)
     */
    @Transactional
    public AuditLogEntity createAuditLog(AuditLogEntity auditLog) {
        return auditLogRepository.save(auditLog);
    }

    /**
     * 오래된 감사 로그 정리 (데이터 보관 정책)
     */
    @Transactional
    public void cleanOldAuditLogs(String tenantId, LocalDateTime beforeDate) {
        log.info("Cleaning audit logs for tenant {} before {}", tenantId, beforeDate);

        Page<AuditLogEntity> oldLogs = auditLogRepository.findByTenantIdAndDateRange(
                tenantId,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                beforeDate,
                Pageable.unpaged()
        );

        auditLogRepository.deleteAll(oldLogs.getContent());

        log.info("Deleted {} old audit logs", oldLogs.getTotalElements());
    }
}
