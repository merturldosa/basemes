package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ApprovalLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Approval Line Repository
 * 결재라인 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ApprovalLineRepository extends JpaRepository<ApprovalLineEntity, Long> {

    /**
     * Find all approval lines by tenant with all relations
     */
    @Query("SELECT DISTINCT al FROM ApprovalLineEntity al " +
           "JOIN FETCH al.tenant " +
           "LEFT JOIN FETCH al.department d " +
           "LEFT JOIN FETCH d.tenant " +
           "WHERE al.tenant.tenantId = :tenantId " +
           "ORDER BY al.priority ASC, al.lineCode ASC")
    List<ApprovalLineEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find approval line by ID with all relations
     */
    @Query("SELECT DISTINCT al FROM ApprovalLineEntity al " +
           "JOIN FETCH al.tenant " +
           "LEFT JOIN FETCH al.department d " +
           "LEFT JOIN FETCH d.tenant " +
           "WHERE al.approvalLineId = :approvalLineId")
    Optional<ApprovalLineEntity> findByIdWithAllRelations(@Param("approvalLineId") Long approvalLineId);

    /**
     * Find active approval lines by tenant
     */
    @Query("SELECT DISTINCT al FROM ApprovalLineEntity al " +
           "JOIN FETCH al.tenant " +
           "LEFT JOIN FETCH al.department " +
           "WHERE al.tenant.tenantId = :tenantId " +
           "AND al.isActive = true " +
           "ORDER BY al.priority ASC, al.lineCode ASC")
    List<ApprovalLineEntity> findActiveApprovalLinesByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find approval lines by document type
     */
    @Query("SELECT DISTINCT al FROM ApprovalLineEntity al " +
           "JOIN FETCH al.tenant " +
           "LEFT JOIN FETCH al.department " +
           "WHERE al.tenant.tenantId = :tenantId " +
           "AND al.documentType = :documentType " +
           "AND al.isActive = true " +
           "ORDER BY al.priority ASC")
    List<ApprovalLineEntity> findByTenantIdAndDocumentType(@Param("tenantId") String tenantId,
                                                            @Param("documentType") String documentType);

    /**
     * Find approval lines by department
     */
    @Query("SELECT DISTINCT al FROM ApprovalLineEntity al " +
           "JOIN FETCH al.tenant " +
           "JOIN FETCH al.department d " +
           "WHERE al.tenant.tenantId = :tenantId " +
           "AND al.department.departmentId = :departmentId " +
           "AND al.isActive = true " +
           "ORDER BY al.priority ASC")
    List<ApprovalLineEntity> findByTenantIdAndDepartmentId(@Param("tenantId") String tenantId,
                                                            @Param("departmentId") Long departmentId);

    /**
     * Find default approval line by document type
     */
    @Query("SELECT al FROM ApprovalLineEntity al " +
           "JOIN FETCH al.tenant " +
           "LEFT JOIN FETCH al.department " +
           "WHERE al.tenant.tenantId = :tenantId " +
           "AND al.documentType = :documentType " +
           "AND al.isDefault = true " +
           "AND al.isActive = true")
    Optional<ApprovalLineEntity> findDefaultByTenantIdAndDocumentType(@Param("tenantId") String tenantId,
                                                                       @Param("documentType") String documentType);

    /**
     * Check if line code exists
     */
    boolean existsByTenant_TenantIdAndLineCode(String tenantId, String lineCode);

    /**
     * Find by tenant and line code
     */
    Optional<ApprovalLineEntity> findByTenant_TenantIdAndLineCode(String tenantId, String lineCode);
}
