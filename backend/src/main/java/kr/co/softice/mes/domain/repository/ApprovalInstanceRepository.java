package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ApprovalInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Approval Instance Repository
 * 결재 인스턴스 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ApprovalInstanceRepository extends JpaRepository<ApprovalInstanceEntity, Long> {

    /**
     * Find all instances by tenant ID
     */
    @Query("SELECT i FROM ApprovalInstanceEntity i " +
            "JOIN FETCH i.tenant " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "ORDER BY i.requestDate DESC")
    List<ApprovalInstanceEntity> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find instance by ID with step instances
     */
    @Query("SELECT i FROM ApprovalInstanceEntity i " +
            "LEFT JOIN FETCH i.stepInstances " +
            "WHERE i.instanceId = :instanceId")
    Optional<ApprovalInstanceEntity> findByIdWithStepInstances(@Param("instanceId") Long instanceId);

    /**
     * Find instance by document
     */
    @Query("SELECT i FROM ApprovalInstanceEntity i " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.documentType = :documentType " +
            "AND i.documentId = :documentId")
    Optional<ApprovalInstanceEntity> findByTenantIdAndDocument(
            @Param("tenantId") String tenantId,
            @Param("documentType") String documentType,
            @Param("documentId") Long documentId);

    /**
     * Find instances by status
     */
    @Query("SELECT i FROM ApprovalInstanceEntity i " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.approvalStatus = :status " +
            "ORDER BY i.requestDate DESC")
    List<ApprovalInstanceEntity> findByTenantIdAndStatus(
            @Param("tenantId") String tenantId,
            @Param("status") String status);

    /**
     * Find instances by requester
     */
    @Query("SELECT i FROM ApprovalInstanceEntity i " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.requesterId = :requesterId " +
            "ORDER BY i.requestDate DESC")
    List<ApprovalInstanceEntity> findByTenantIdAndRequesterId(
            @Param("tenantId") String tenantId,
            @Param("requesterId") Long requesterId);

    /**
     * Find pending instances for approver
     */
    @Query("SELECT DISTINCT i FROM ApprovalInstanceEntity i " +
            "JOIN i.stepInstances si " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.approvalStatus IN ('PENDING', 'IN_PROGRESS') " +
            "AND si.approverId = :approverId " +
            "AND si.stepStatus IN ('PENDING', 'IN_PROGRESS') " +
            "ORDER BY i.requestDate ASC")
    List<ApprovalInstanceEntity> findPendingByApprover(
            @Param("tenantId") String tenantId,
            @Param("approverId") Long approverId);

    /**
     * Find instances by document type
     */
    @Query("SELECT i FROM ApprovalInstanceEntity i " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.documentType = :documentType " +
            "ORDER BY i.requestDate DESC")
    List<ApprovalInstanceEntity> findByTenantIdAndDocumentType(
            @Param("tenantId") String tenantId,
            @Param("documentType") String documentType);

    /**
     * Find instances by date range
     */
    @Query("SELECT i FROM ApprovalInstanceEntity i " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.requestDate BETWEEN :startDate AND :endDate " +
            "ORDER BY i.requestDate DESC")
    List<ApprovalInstanceEntity> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count instances by status
     */
    @Query("SELECT COUNT(i) FROM ApprovalInstanceEntity i " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.approvalStatus = :status")
    Long countByTenantIdAndStatus(
            @Param("tenantId") String tenantId,
            @Param("status") String status);

    /**
     * Count pending instances for approver
     */
    @Query("SELECT COUNT(DISTINCT i) FROM ApprovalInstanceEntity i " +
            "JOIN i.stepInstances si " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.approvalStatus IN ('PENDING', 'IN_PROGRESS') " +
            "AND si.approverId = :approverId " +
            "AND si.stepStatus IN ('PENDING', 'IN_PROGRESS')")
    Long countPendingByApprover(
            @Param("tenantId") String tenantId,
            @Param("approverId") Long approverId);

    /**
     * Find overdue instances
     */
    @Query("SELECT DISTINCT i FROM ApprovalInstanceEntity i " +
            "JOIN i.stepInstances si " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.approvalStatus IN ('PENDING', 'IN_PROGRESS') " +
            "AND si.stepStatus IN ('PENDING', 'IN_PROGRESS') " +
            "AND si.dueDate < :now " +
            "ORDER BY si.dueDate ASC")
    List<ApprovalInstanceEntity> findOverdueInstances(
            @Param("tenantId") String tenantId,
            @Param("now") LocalDateTime now);

    /**
     * Check if document has approval instance
     */
    @Query("SELECT COUNT(i) > 0 FROM ApprovalInstanceEntity i " +
            "WHERE i.tenant.tenantId = :tenantId " +
            "AND i.documentType = :documentType " +
            "AND i.documentId = :documentId")
    boolean existsByDocument(
            @Param("tenantId") String tenantId,
            @Param("documentType") String documentType,
            @Param("documentId") Long documentId);
}
