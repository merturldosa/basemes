package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.PauseResumeEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.WorkProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Pause/Resume Repository
 * 일시정지/재개 이력 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface PauseResumeRepository extends JpaRepository<PauseResumeEntity, Long> {

    /**
     * Find by work progress
     */
    List<PauseResumeEntity> findByWorkProgress(WorkProgressEntity workProgress);

    /**
     * Find by work progress ID
     */
    List<PauseResumeEntity> findByWorkProgress_ProgressId(Long progressId);

    /**
     * Find by work progress ordered by pause time
     */
    List<PauseResumeEntity> findByWorkProgressOrderByPauseTimeDesc(WorkProgressEntity workProgress);

    /**
     * Find active pause (not resumed yet)
     */
    Optional<PauseResumeEntity> findByWorkProgressAndResumeTimeIsNull(WorkProgressEntity workProgress);

    /**
     * Find active pause by work progress ID
     */
    Optional<PauseResumeEntity> findByWorkProgress_ProgressIdAndResumeTimeIsNull(Long progressId);

    /**
     * Find by tenant
     */
    List<PauseResumeEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<PauseResumeEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and pause type
     */
    List<PauseResumeEntity> findByTenantAndPauseType(TenantEntity tenant, String pauseType);

    /**
     * Find by tenant and date range
     */
    @Query("SELECT pr FROM PauseResumeEntity pr WHERE pr.tenant.tenantId = :tenantId " +
           "AND pr.pauseTime BETWEEN :startDate AND :endDate " +
           "ORDER BY pr.pauseTime DESC")
    List<PauseResumeEntity> findByTenantIdAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find pauses requiring approval
     */
    List<PauseResumeEntity> findByTenantAndRequiresApprovalTrueAndApprovalTimeIsNull(TenantEntity tenant);

    /**
     * Find pauses requiring approval by tenant ID
     */
    @Query("SELECT pr FROM PauseResumeEntity pr WHERE pr.tenant.tenantId = :tenantId " +
           "AND pr.requiresApproval = true AND pr.approvalTime IS NULL " +
           "ORDER BY pr.pauseTime DESC")
    List<PauseResumeEntity> findPendingApprovalsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Count by work progress
     */
    long countByWorkProgress(WorkProgressEntity workProgress);

    /**
     * Count by work progress ID
     */
    long countByWorkProgress_ProgressId(Long progressId);

    /**
     * Calculate total pause duration by work progress
     */
    @Query("SELECT COALESCE(SUM(pr.durationMinutes), 0) FROM PauseResumeEntity pr " +
           "WHERE pr.workProgress.progressId = :progressId AND pr.resumeTime IS NOT NULL")
    Integer calculateTotalPauseDuration(@Param("progressId") Long progressId);

    /**
     * Find pause/resume history with all relationships
     */
    @Query("SELECT pr FROM PauseResumeEntity pr " +
           "JOIN FETCH pr.tenant " +
           "JOIN FETCH pr.workProgress wp " +
           "JOIN FETCH wp.workOrder " +
           "WHERE pr.pauseResumeId = :pauseResumeId")
    Optional<PauseResumeEntity> findByIdWithAllRelations(@Param("pauseResumeId") Long pauseResumeId);
}
