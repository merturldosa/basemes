package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SOPExecutionStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SOP Execution Step Repository
 * SOP 실행 단계 결과 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface SOPExecutionStepRepository extends JpaRepository<SOPExecutionStepEntity, Long> {

    /**
     * Find all execution steps for an execution
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "LEFT JOIN FETCH ses.sopStep " +
            "WHERE ses.execution.executionId = :executionId " +
            "ORDER BY ses.stepNumber ASC")
    List<SOPExecutionStepEntity> findByExecutionId(@Param("executionId") Long executionId);

    /**
     * Find execution step by execution and SOP step
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.sopStep.sopStepId = :sopStepId")
    Optional<SOPExecutionStepEntity> findByExecutionIdAndSopStepId(
            @Param("executionId") Long executionId,
            @Param("sopStepId") Long sopStepId);

    /**
     * Find execution steps by status
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "LEFT JOIN FETCH ses.sopStep " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.stepStatus = :status " +
            "ORDER BY ses.stepNumber ASC")
    List<SOPExecutionStepEntity> findByExecutionIdAndStatus(
            @Param("executionId") Long executionId,
            @Param("status") String status);

    /**
     * Find completed steps
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.stepStatus = 'COMPLETED' " +
            "ORDER BY ses.stepNumber ASC")
    List<SOPExecutionStepEntity> findCompletedByExecutionId(@Param("executionId") Long executionId);

    /**
     * Find pending steps
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "LEFT JOIN FETCH ses.sopStep " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.stepStatus = 'PENDING' " +
            "ORDER BY ses.stepNumber ASC")
    List<SOPExecutionStepEntity> findPendingByExecutionId(@Param("executionId") Long executionId);

    /**
     * Find failed steps
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "LEFT JOIN FETCH ses.sopStep " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.stepStatus = 'FAILED' " +
            "ORDER BY ses.stepNumber ASC")
    List<SOPExecutionStepEntity> findFailedByExecutionId(@Param("executionId") Long executionId);

    /**
     * Find next pending step (first in order)
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "LEFT JOIN FETCH ses.sopStep " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.stepStatus = 'PENDING' " +
            "ORDER BY ses.stepNumber ASC")
    Optional<SOPExecutionStepEntity> findNextPendingStep(@Param("executionId") Long executionId);

    /**
     * Find execution steps with photos
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.photos IS NOT NULL " +
            "ORDER BY ses.stepNumber ASC")
    List<SOPExecutionStepEntity> findWithPhotosByExecutionId(@Param("executionId") Long executionId);

    /**
     * Find execution steps with signature
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.signature IS NOT NULL " +
            "ORDER BY ses.stepNumber ASC")
    List<SOPExecutionStepEntity> findWithSignatureByExecutionId(@Param("executionId") Long executionId);

    /**
     * Count steps by status
     */
    @Query("SELECT COUNT(ses) FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.stepStatus = :status")
    Long countByExecutionIdAndStatus(
            @Param("executionId") Long executionId,
            @Param("status") String status);

    /**
     * Count completed steps
     */
    @Query("SELECT COUNT(ses) FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.stepStatus = 'COMPLETED'")
    Long countCompletedByExecutionId(@Param("executionId") Long executionId);

    /**
     * Count total steps
     */
    @Query("SELECT COUNT(ses) FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId")
    Long countByExecutionId(@Param("executionId") Long executionId);

    /**
     * Check if all critical steps are completed
     */
    @Query("SELECT CASE WHEN COUNT(ses) = 0 THEN true ELSE false END " +
            "FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.sopStep.isCritical = true " +
            "AND ses.stepStatus <> 'COMPLETED'")
    boolean areAllCriticalStepsCompleted(@Param("executionId") Long executionId);

    /**
     * Check if all mandatory steps are completed
     */
    @Query("SELECT CASE WHEN COUNT(ses) = 0 THEN true ELSE false END " +
            "FROM SOPExecutionStepEntity ses " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.sopStep.isMandatory = true " +
            "AND ses.stepStatus NOT IN ('COMPLETED', 'SKIPPED')")
    boolean areAllMandatoryStepsCompleted(@Param("executionId") Long executionId);

    /**
     * Find execution step by step number
     */
    @Query("SELECT ses FROM SOPExecutionStepEntity ses " +
            "LEFT JOIN FETCH ses.sopStep " +
            "WHERE ses.execution.executionId = :executionId " +
            "AND ses.stepNumber = :stepNumber")
    Optional<SOPExecutionStepEntity> findByExecutionIdAndStepNumber(
            @Param("executionId") Long executionId,
            @Param("stepNumber") Integer stepNumber);
}
