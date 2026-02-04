package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Log Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    /**
     * 테넌트별 감사 로그 조회 (페이징)
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    /**
     * 사용자별 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.user.userId = :userId ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 작업 유형별 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId AND a.action = :action ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findByTenantIdAndAction(
            @Param("tenantId") String tenantId,
            @Param("action") String action,
            Pageable pageable
    );

    /**
     * 엔티티별 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId AND a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findByTenantIdAndEntityTypeAndEntityId(
            @Param("tenantId") String tenantId,
            @Param("entityType") String entityType,
            @Param("entityId") String entityId,
            Pageable pageable
    );

    /**
     * 기간별 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 성공/실패별 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId AND a.success = :success ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findByTenantIdAndSuccess(
            @Param("tenantId") String tenantId,
            @Param("success") Boolean success,
            Pageable pageable
    );

    /**
     * 복합 조건 검색
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId " +
            "AND (:username IS NULL OR a.username = :username) " +
            "AND (:action IS NULL OR a.action = :action) " +
            "AND (:entityType IS NULL OR a.entityType = :entityType) " +
            "AND (:success IS NULL OR a.success = :success) " +
            "AND (:startDate IS NULL OR a.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR a.createdAt <= :endDate) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> searchAuditLogs(
            @Param("tenantId") String tenantId,
            @Param("username") String username,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("success") Boolean success,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * IP 주소별 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId AND a.ipAddress = :ipAddress ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findByTenantIdAndIpAddress(
            @Param("tenantId") String tenantId,
            @Param("ipAddress") String ipAddress,
            Pageable pageable
    );

    /**
     * 작업 통계 - 작업 유형별 카운트
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId AND a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.action")
    List<Object[]> countByActionAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 사용자 활동 통계
     */
    @Query("SELECT a.username, COUNT(a) FROM AuditLogEntity a WHERE a.tenant.tenantId = :tenantId AND a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.username ORDER BY COUNT(a) DESC")
    List<Object[]> countByUserAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
