package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SOPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * SOP Repository
 * 표준 작업 절차 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface SOPRepository extends JpaRepository<SOPEntity, Long> {

    /**
     * Find all SOPs by tenant ID with steps
     */
    @Query("SELECT DISTINCT s FROM SOPEntity s " +
            "LEFT JOIN FETCH s.steps " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "ORDER BY s.displayOrder ASC, s.sopName ASC")
    List<SOPEntity> findAllByTenantIdWithSteps(@Param("tenantId") String tenantId);

    /**
     * Find active SOPs by tenant ID
     */
    @Query("SELECT s FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.isActive = true " +
            "ORDER BY s.displayOrder ASC, s.sopName ASC")
    List<SOPEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find approved SOPs by tenant ID
     */
    @Query("SELECT s FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.approvalStatus = 'APPROVED' " +
            "AND s.isActive = true " +
            "ORDER BY s.displayOrder ASC, s.sopName ASC")
    List<SOPEntity> findApprovedByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find SOPs by type
     */
    @Query("SELECT s FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.sopType = :sopType " +
            "AND s.isActive = true " +
            "ORDER BY s.displayOrder ASC, s.sopName ASC")
    List<SOPEntity> findByTenantIdAndSopType(
            @Param("tenantId") String tenantId,
            @Param("sopType") String sopType);

    /**
     * Find SOPs by category
     */
    @Query("SELECT s FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.category = :category " +
            "AND s.isActive = true " +
            "ORDER BY s.displayOrder ASC, s.sopName ASC")
    List<SOPEntity> findByTenantIdAndCategory(
            @Param("tenantId") String tenantId,
            @Param("category") String category);

    /**
     * Find SOP by ID with steps
     */
    @Query("SELECT s FROM SOPEntity s " +
            "LEFT JOIN FETCH s.steps " +
            "WHERE s.sopId = :sopId")
    Optional<SOPEntity> findByIdWithSteps(@Param("sopId") Long sopId);

    /**
     * Find SOP by code (latest version)
     */
    @Query("SELECT s FROM SOPEntity s " +
            "LEFT JOIN FETCH s.steps " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.sopCode = :sopCode " +
            "AND s.approvalStatus = 'APPROVED' " +
            "AND s.isActive = true " +
            "ORDER BY s.version DESC")
    Optional<SOPEntity> findLatestBySopCode(
            @Param("tenantId") String tenantId,
            @Param("sopCode") String sopCode);

    /**
     * Find SOP by code and version
     */
    @Query("SELECT s FROM SOPEntity s " +
            "LEFT JOIN FETCH s.steps " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.sopCode = :sopCode " +
            "AND s.version = :version")
    Optional<SOPEntity> findByTenantIdAndSopCodeAndVersion(
            @Param("tenantId") String tenantId,
            @Param("sopCode") String sopCode,
            @Param("version") String version);

    /**
     * Find all versions of an SOP
     */
    @Query("SELECT s FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.sopCode = :sopCode " +
            "ORDER BY s.version DESC")
    List<SOPEntity> findAllVersionsByTenantIdAndSopCode(
            @Param("tenantId") String tenantId,
            @Param("sopCode") String sopCode);

    /**
     * Find SOPs pending approval
     */
    @Query("SELECT s FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.approvalStatus = 'PENDING' " +
            "ORDER BY s.createdAt DESC")
    List<SOPEntity> findPendingApprovalByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find SOPs requiring review (next review date passed)
     */
    @Query("SELECT s FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.approvalStatus = 'APPROVED' " +
            "AND s.isActive = true " +
            "AND s.nextReviewDate <= :today " +
            "ORDER BY s.nextReviewDate ASC")
    List<SOPEntity> findRequiringReview(
            @Param("tenantId") String tenantId,
            @Param("today") LocalDate today);

    /**
     * Find SOPs by target process
     */
    @Query("SELECT s FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.targetProcess = :targetProcess " +
            "AND s.approvalStatus = 'APPROVED' " +
            "AND s.isActive = true " +
            "ORDER BY s.displayOrder ASC")
    List<SOPEntity> findByTenantIdAndTargetProcess(
            @Param("tenantId") String tenantId,
            @Param("targetProcess") String targetProcess);

    /**
     * Check if SOP code exists
     */
    @Query("SELECT COUNT(s) > 0 FROM SOPEntity s " +
            "WHERE s.tenant.tenantId = :tenantId " +
            "AND s.sopCode = :sopCode")
    boolean existsByTenantIdAndSopCode(
            @Param("tenantId") String tenantId,
            @Param("sopCode") String sopCode);
}
