package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SOPStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SOP Step Repository
 * SOP 단계 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface SOPStepRepository extends JpaRepository<SOPStepEntity, Long> {

    /**
     * Find all steps for an SOP
     */
    @Query("SELECT ss FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId " +
            "ORDER BY ss.stepNumber ASC")
    List<SOPStepEntity> findBySopId(@Param("sopId") Long sopId);

    /**
     * Find critical steps for an SOP
     */
    @Query("SELECT ss FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId " +
            "AND ss.isCritical = true " +
            "ORDER BY ss.stepNumber ASC")
    List<SOPStepEntity> findCriticalStepsBySopId(@Param("sopId") Long sopId);

    /**
     * Find mandatory steps for an SOP
     */
    @Query("SELECT ss FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId " +
            "AND ss.isMandatory = true " +
            "ORDER BY ss.stepNumber ASC")
    List<SOPStepEntity> findMandatoryStepsBySopId(@Param("sopId") Long sopId);

    /**
     * Find steps by type
     */
    @Query("SELECT ss FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId " +
            "AND ss.stepType = :stepType " +
            "ORDER BY ss.stepNumber ASC")
    List<SOPStepEntity> findBySopIdAndStepType(
            @Param("sopId") Long sopId,
            @Param("stepType") String stepType);

    /**
     * Find step by SOP ID and step number
     */
    @Query("SELECT ss FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId " +
            "AND ss.stepNumber = :stepNumber")
    Optional<SOPStepEntity> findBySopIdAndStepNumber(
            @Param("sopId") Long sopId,
            @Param("stepNumber") Integer stepNumber);

    /**
     * Find steps with prerequisites
     */
    @Query("SELECT ss FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId " +
            "AND ss.prerequisiteStep IS NOT NULL " +
            "ORDER BY ss.stepNumber ASC")
    List<SOPStepEntity> findStepsWithPrerequisitesBySopId(@Param("sopId") Long sopId);

    /**
     * Find steps that depend on a given step
     */
    @Query("SELECT ss FROM SOPStepEntity ss " +
            "WHERE ss.prerequisiteStep.sopStepId = :stepId " +
            "ORDER BY ss.stepNumber ASC")
    List<SOPStepEntity> findDependentSteps(@Param("stepId") Long stepId);

    /**
     * Count steps in SOP
     */
    @Query("SELECT COUNT(ss) FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId")
    Long countBySopId(@Param("sopId") Long sopId);

    /**
     * Count critical steps in SOP
     */
    @Query("SELECT COUNT(ss) FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId " +
            "AND ss.isCritical = true")
    Long countCriticalStepsBySopId(@Param("sopId") Long sopId);

    /**
     * Get maximum step number for an SOP
     */
    @Query("SELECT COALESCE(MAX(ss.stepNumber), 0) FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId")
    Integer getMaxStepNumberBySopId(@Param("sopId") Long sopId);

    /**
     * Check if step number exists in SOP
     */
    @Query("SELECT COUNT(ss) > 0 FROM SOPStepEntity ss " +
            "WHERE ss.sop.sopId = :sopId " +
            "AND ss.stepNumber = :stepNumber")
    boolean existsBySopIdAndStepNumber(
            @Param("sopId") Long sopId,
            @Param("stepNumber") Integer stepNumber);
}
