package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.InspectionActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Inspection Action Repository
 * 점검 조치 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface InspectionActionRepository extends JpaRepository<InspectionActionEntity, Long> {

    /**
     * Get all inspection actions by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT a FROM InspectionActionEntity a " +
           "JOIN FETCH a.tenant " +
           "LEFT JOIN FETCH a.inspection " +
           "LEFT JOIN FETCH a.assignedUser " +
           "WHERE a.tenant.tenantId = :tenantId " +
           "ORDER BY a.createdAt DESC")
    List<InspectionActionEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get inspection action by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT a FROM InspectionActionEntity a " +
           "JOIN FETCH a.tenant " +
           "LEFT JOIN FETCH a.inspection " +
           "LEFT JOIN FETCH a.assignedUser " +
           "WHERE a.actionId = :actionId")
    Optional<InspectionActionEntity> findByIdWithAllRelations(@Param("actionId") Long actionId);

    /**
     * Get inspection actions by inspection ID
     */
    @Query("SELECT a FROM InspectionActionEntity a " +
           "JOIN FETCH a.tenant " +
           "LEFT JOIN FETCH a.inspection " +
           "LEFT JOIN FETCH a.assignedUser " +
           "WHERE a.inspection.inspectionId = :inspectionId")
    List<InspectionActionEntity> findByInspectionId(@Param("inspectionId") Long inspectionId);

    /**
     * Get inspection actions by tenant and status
     */
    @Query("SELECT a FROM InspectionActionEntity a " +
           "JOIN FETCH a.tenant " +
           "LEFT JOIN FETCH a.inspection " +
           "LEFT JOIN FETCH a.assignedUser " +
           "WHERE a.tenant.tenantId = :tenantId " +
           "AND a.status = :status")
    List<InspectionActionEntity> findByStatus(@Param("tenantId") String tenantId, @Param("status") String status);
}
