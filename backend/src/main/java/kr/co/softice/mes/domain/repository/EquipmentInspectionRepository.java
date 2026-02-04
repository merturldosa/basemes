package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.EquipmentInspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Equipment Inspection Repository
 * 설비 점검 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface EquipmentInspectionRepository extends JpaRepository<EquipmentInspectionEntity, Long> {

    /**
     * Get all inspections by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT i FROM EquipmentInspectionEntity i " +
           "JOIN FETCH i.tenant " +
           "JOIN FETCH i.equipment " +
           "LEFT JOIN FETCH i.inspectorUser " +
           "LEFT JOIN FETCH i.responsibleUser " +
           "WHERE i.tenant.tenantId = :tenantId " +
           "ORDER BY i.inspectionDate DESC")
    List<EquipmentInspectionEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get inspection by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT i FROM EquipmentInspectionEntity i " +
           "JOIN FETCH i.tenant " +
           "JOIN FETCH i.equipment " +
           "LEFT JOIN FETCH i.inspectorUser " +
           "LEFT JOIN FETCH i.responsibleUser " +
           "WHERE i.inspectionId = :inspectionId")
    Optional<EquipmentInspectionEntity> findByIdWithAllRelations(@Param("inspectionId") Long inspectionId);

    /**
     * Get inspections by equipment
     */
    @Query("SELECT i FROM EquipmentInspectionEntity i " +
           "JOIN FETCH i.tenant " +
           "JOIN FETCH i.equipment " +
           "LEFT JOIN FETCH i.inspectorUser " +
           "LEFT JOIN FETCH i.responsibleUser " +
           "WHERE i.equipment.equipmentId = :equipmentId " +
           "ORDER BY i.inspectionDate DESC")
    List<EquipmentInspectionEntity> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * Get inspections by tenant and type
     */
    @Query("SELECT i FROM EquipmentInspectionEntity i " +
           "JOIN FETCH i.tenant " +
           "JOIN FETCH i.equipment " +
           "LEFT JOIN FETCH i.inspectorUser " +
           "LEFT JOIN FETCH i.responsibleUser " +
           "WHERE i.tenant.tenantId = :tenantId " +
           "AND i.inspectionType = :inspectionType " +
           "ORDER BY i.inspectionDate DESC")
    List<EquipmentInspectionEntity> findByTenantIdAndInspectionType(@Param("tenantId") String tenantId, @Param("inspectionType") String inspectionType);

    /**
     * Get inspections by tenant and result
     */
    @Query("SELECT i FROM EquipmentInspectionEntity i " +
           "JOIN FETCH i.tenant " +
           "JOIN FETCH i.equipment " +
           "LEFT JOIN FETCH i.inspectorUser " +
           "LEFT JOIN FETCH i.responsibleUser " +
           "WHERE i.tenant.tenantId = :tenantId " +
           "AND i.inspectionResult = :inspectionResult " +
           "ORDER BY i.inspectionDate DESC")
    List<EquipmentInspectionEntity> findByTenantIdAndInspectionResult(@Param("tenantId") String tenantId, @Param("inspectionResult") String inspectionResult);

    /**
     * Check if inspection number exists for tenant
     */
    boolean existsByTenant_TenantIdAndInspectionNo(String tenantId, String inspectionNo);
}
