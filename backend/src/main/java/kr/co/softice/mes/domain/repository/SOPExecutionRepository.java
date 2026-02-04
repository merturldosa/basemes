package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SOPExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SOP Execution Repository
 * SOP 실행 기록 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface SOPExecutionRepository extends JpaRepository<SOPExecutionEntity, Long> {

    /**
     * Find all executions by tenant ID
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.sop " +
            "LEFT JOIN FETCH se.executor " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "ORDER BY se.executionDate DESC")
    List<SOPExecutionEntity> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find execution by ID with steps
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.executionSteps " +
            "WHERE se.executionId = :executionId")
    Optional<SOPExecutionEntity> findByIdWithSteps(@Param("executionId") Long executionId);

    /**
     * Find executions by SOP ID
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.executor " +
            "WHERE se.sop.sopId = :sopId " +
            "ORDER BY se.executionDate DESC")
    List<SOPExecutionEntity> findBySopId(@Param("sopId") Long sopId);

    /**
     * Find executions by executor
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.sop " +
            "WHERE se.executor.userId = :executorId " +
            "ORDER BY se.executionDate DESC")
    List<SOPExecutionEntity> findByExecutorId(@Param("executorId") Long executorId);

    /**
     * Find executions by status
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.sop " +
            "LEFT JOIN FETCH se.executor " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "AND se.executionStatus = :status " +
            "ORDER BY se.executionDate DESC")
    List<SOPExecutionEntity> findByTenantIdAndStatus(
            @Param("tenantId") String tenantId,
            @Param("status") String status);

    /**
     * Find in-progress executions
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.sop " +
            "LEFT JOIN FETCH se.executor " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "AND se.executionStatus = 'IN_PROGRESS' " +
            "ORDER BY se.executionDate DESC")
    List<SOPExecutionEntity> findInProgressByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find executions by reference
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.sop " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "AND se.referenceType = :referenceType " +
            "AND se.referenceId = :referenceId " +
            "ORDER BY se.executionDate DESC")
    List<SOPExecutionEntity> findByTenantIdAndReference(
            @Param("tenantId") String tenantId,
            @Param("referenceType") String referenceType,
            @Param("referenceId") Long referenceId);

    /**
     * Find executions by date range
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.sop " +
            "LEFT JOIN FETCH se.executor " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "AND se.executionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY se.executionDate DESC")
    List<SOPExecutionEntity> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find executions pending review
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.sop " +
            "LEFT JOIN FETCH se.executor " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "AND se.executionStatus = 'COMPLETED' " +
            "AND (se.reviewStatus IS NULL OR se.reviewStatus = 'PENDING') " +
            "ORDER BY se.endTime ASC")
    List<SOPExecutionEntity> findPendingReviewByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find execution by execution number
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "LEFT JOIN FETCH se.executionSteps " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "AND se.executionNo = :executionNo")
    Optional<SOPExecutionEntity> findByTenantIdAndExecutionNo(
            @Param("tenantId") String tenantId,
            @Param("executionNo") String executionNo);

    /**
     * Count executions by SOP
     */
    @Query("SELECT COUNT(se) FROM SOPExecutionEntity se " +
            "WHERE se.sop.sopId = :sopId")
    Long countBySopId(@Param("sopId") Long sopId);

    /**
     * Count completed executions by SOP
     */
    @Query("SELECT COUNT(se) FROM SOPExecutionEntity se " +
            "WHERE se.sop.sopId = :sopId " +
            "AND se.executionStatus = 'COMPLETED'")
    Long countCompletedBySopId(@Param("sopId") Long sopId);

    /**
     * Find latest execution for reference
     */
    @Query("SELECT se FROM SOPExecutionEntity se " +
            "WHERE se.referenceType = :referenceType " +
            "AND se.referenceId = :referenceId " +
            "ORDER BY se.executionDate DESC")
    Optional<SOPExecutionEntity> findLatestByReference(
            @Param("referenceType") String referenceType,
            @Param("referenceId") Long referenceId);

    /**
     * Check if execution number exists
     */
    @Query("SELECT COUNT(se) > 0 FROM SOPExecutionEntity se " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "AND se.executionNo = :executionNo")
    boolean existsByTenantIdAndExecutionNo(
            @Param("tenantId") String tenantId,
            @Param("executionNo") String executionNo);

    /**
     * Generate next execution number
     */
    @Query("SELECT se.executionNo FROM SOPExecutionEntity se " +
            "WHERE se.tenant.tenantId = :tenantId " +
            "AND se.executionNo LIKE :prefix% " +
            "ORDER BY se.executionNo DESC")
    List<String> findExecutionNumbersWithPrefix(
            @Param("tenantId") String tenantId,
            @Param("prefix") String prefix);
}
