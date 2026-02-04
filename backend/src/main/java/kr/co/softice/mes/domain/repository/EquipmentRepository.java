package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Equipment Repository
 * 설비 마스터 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Long> {

    /**
     * Get all equipments by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT e FROM EquipmentEntity e " +
           "JOIN FETCH e.tenant " +
           "LEFT JOIN FETCH e.site " +
           "LEFT JOIN FETCH e.department " +
           "WHERE e.tenant.tenantId = :tenantId " +
           "ORDER BY e.equipmentCode")
    List<EquipmentEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get equipment by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT e FROM EquipmentEntity e " +
           "JOIN FETCH e.tenant " +
           "LEFT JOIN FETCH e.site " +
           "LEFT JOIN FETCH e.department " +
           "WHERE e.equipmentId = :equipmentId")
    Optional<EquipmentEntity> findByIdWithAllRelations(@Param("equipmentId") Long equipmentId);

    /**
     * Get active equipments by tenant
     */
    @Query("SELECT e FROM EquipmentEntity e " +
           "JOIN FETCH e.tenant " +
           "LEFT JOIN FETCH e.site " +
           "LEFT JOIN FETCH e.department " +
           "WHERE e.tenant.tenantId = :tenantId " +
           "AND e.isActive = true " +
           "ORDER BY e.equipmentCode")
    List<EquipmentEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Get equipments by tenant and status
     */
    @Query("SELECT e FROM EquipmentEntity e " +
           "JOIN FETCH e.tenant " +
           "LEFT JOIN FETCH e.site " +
           "LEFT JOIN FETCH e.department " +
           "WHERE e.tenant.tenantId = :tenantId " +
           "AND e.status = :status " +
           "ORDER BY e.equipmentCode")
    List<EquipmentEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Get equipments by tenant and type
     */
    @Query("SELECT e FROM EquipmentEntity e " +
           "JOIN FETCH e.tenant " +
           "LEFT JOIN FETCH e.site " +
           "LEFT JOIN FETCH e.department " +
           "WHERE e.tenant.tenantId = :tenantId " +
           "AND e.equipmentType = :equipmentType " +
           "ORDER BY e.equipmentCode")
    List<EquipmentEntity> findByTenantIdAndEquipmentType(@Param("tenantId") String tenantId, @Param("equipmentType") String equipmentType);

    /**
     * Check if equipment code exists for tenant
     */
    boolean existsByTenant_TenantIdAndEquipmentCode(String tenantId, String equipmentCode);
}
