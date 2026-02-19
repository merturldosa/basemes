package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.EquipmentPartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Equipment Part Repository
 * 설비 부품 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface EquipmentPartRepository extends JpaRepository<EquipmentPartEntity, Long> {

    /**
     * Get all equipment parts by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT p FROM EquipmentPartEntity p " +
           "JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH p.equipment " +
           "WHERE p.tenant.tenantId = :tenantId " +
           "ORDER BY p.partCode")
    List<EquipmentPartEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get equipment part by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT p FROM EquipmentPartEntity p " +
           "JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH p.equipment " +
           "WHERE p.partId = :partId")
    Optional<EquipmentPartEntity> findByIdWithAllRelations(@Param("partId") Long partId);

    /**
     * Get parts by equipment ID with all relations (JOIN FETCH)
     */
    @Query("SELECT p FROM EquipmentPartEntity p " +
           "JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH p.equipment " +
           "WHERE p.equipment.equipmentId = :equipmentId " +
           "ORDER BY p.partCode")
    List<EquipmentPartEntity> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * Get parts needing replacement by due date
     */
    @Query("SELECT p FROM EquipmentPartEntity p " +
           "JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH p.equipment " +
           "WHERE p.tenant.tenantId = :tenantId " +
           "AND p.nextReplacementDate <= :dueDate " +
           "AND p.isActive = true " +
           "ORDER BY p.nextReplacementDate")
    List<EquipmentPartEntity> findNeedsReplacement(@Param("tenantId") String tenantId, @Param("dueDate") LocalDate dueDate);

    /**
     * Check if part code exists for tenant and equipment
     */
    boolean existsByTenant_TenantIdAndEquipment_EquipmentIdAndPartCode(String tenantId, Long equipmentId, String partCode);
}
