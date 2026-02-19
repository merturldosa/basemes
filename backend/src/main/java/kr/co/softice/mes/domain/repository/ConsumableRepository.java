package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ConsumableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Consumable Repository
 * 소모품 마스터 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface ConsumableRepository extends JpaRepository<ConsumableEntity, Long> {

    /**
     * Get all consumables by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT c FROM ConsumableEntity c " +
           "JOIN FETCH c.tenant " +
           "LEFT JOIN FETCH c.equipment " +
           "WHERE c.tenant.tenantId = :tenantId " +
           "ORDER BY c.consumableCode")
    List<ConsumableEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get consumable by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT c FROM ConsumableEntity c " +
           "JOIN FETCH c.tenant " +
           "LEFT JOIN FETCH c.equipment " +
           "WHERE c.consumableId = :consumableId")
    Optional<ConsumableEntity> findByIdWithAllRelations(@Param("consumableId") Long consumableId);

    /**
     * Get consumables with low stock (currentStock <= minimumStock)
     */
    @Query("SELECT c FROM ConsumableEntity c " +
           "JOIN FETCH c.tenant " +
           "LEFT JOIN FETCH c.equipment " +
           "WHERE c.tenant.tenantId = :tenantId " +
           "AND c.currentStock <= c.minimumStock " +
           "AND c.isActive = true " +
           "ORDER BY c.consumableCode")
    List<ConsumableEntity> findLowStock(@Param("tenantId") String tenantId);

    /**
     * Check if consumable code exists for tenant
     */
    boolean existsByTenant_TenantIdAndConsumableCode(String tenantId, String consumableCode);
}
