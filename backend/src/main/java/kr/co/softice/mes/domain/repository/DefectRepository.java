package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.DefectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Defect Repository
 * 불량 관리 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface DefectRepository extends JpaRepository<DefectEntity, Long> {

    /**
     * Get all defects by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT d FROM DefectEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.product " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.workResult " +
           "LEFT JOIN FETCH d.goodsReceipt " +
           "LEFT JOIN FETCH d.shipping " +
           "LEFT JOIN FETCH d.qualityInspection " +
           "LEFT JOIN FETCH d.responsibleDepartment " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "LEFT JOIN FETCH d.reporterUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "ORDER BY d.defectDate DESC")
    List<DefectEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get defect by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT d FROM DefectEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.product " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.workResult " +
           "LEFT JOIN FETCH d.goodsReceipt " +
           "LEFT JOIN FETCH d.shipping " +
           "LEFT JOIN FETCH d.qualityInspection " +
           "LEFT JOIN FETCH d.responsibleDepartment " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "LEFT JOIN FETCH d.reporterUser " +
           "WHERE d.defectId = :defectId")
    Optional<DefectEntity> findByIdWithAllRelations(@Param("defectId") Long defectId);

    /**
     * Get defects by tenant and status
     */
    @Query("SELECT d FROM DefectEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.product " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.workResult " +
           "LEFT JOIN FETCH d.goodsReceipt " +
           "LEFT JOIN FETCH d.shipping " +
           "LEFT JOIN FETCH d.qualityInspection " +
           "LEFT JOIN FETCH d.responsibleDepartment " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "LEFT JOIN FETCH d.reporterUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.status = :status " +
           "ORDER BY d.defectDate DESC")
    List<DefectEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Get defects by tenant and source type
     */
    @Query("SELECT d FROM DefectEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.product " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.workResult " +
           "LEFT JOIN FETCH d.goodsReceipt " +
           "LEFT JOIN FETCH d.shipping " +
           "LEFT JOIN FETCH d.qualityInspection " +
           "LEFT JOIN FETCH d.responsibleDepartment " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "LEFT JOIN FETCH d.reporterUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.sourceType = :sourceType " +
           "ORDER BY d.defectDate DESC")
    List<DefectEntity> findByTenantIdAndSourceType(@Param("tenantId") String tenantId, @Param("sourceType") String sourceType);

    /**
     * Check if defect number exists for tenant
     */
    boolean existsByTenant_TenantIdAndDefectNo(String tenantId, String defectNo);

    /**
     * Count defects by tenant and defect number prefix (for auto-generation)
     */
    long countByTenant_TenantIdAndDefectNoStartingWith(String tenantId, String prefix);
}
