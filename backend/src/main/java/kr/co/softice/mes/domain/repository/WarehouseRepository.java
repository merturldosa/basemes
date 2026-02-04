package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Warehouse Repository
 * 창고 마스터 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseEntity, Long> {

    /**
     * Find by tenant and warehouse code
     */
    Optional<WarehouseEntity> findByTenantAndWarehouseCode(TenantEntity tenant, String warehouseCode);

    /**
     * Find by tenant ID and warehouse code
     */
    Optional<WarehouseEntity> findByTenant_TenantIdAndWarehouseCode(String tenantId, String warehouseCode);

    /**
     * Find by tenant
     */
    List<WarehouseEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<WarehouseEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and warehouse type
     */
    List<WarehouseEntity> findByTenantAndWarehouseType(TenantEntity tenant, String warehouseType);

    /**
     * Find by tenant ID and warehouse type
     */
    List<WarehouseEntity> findByTenant_TenantIdAndWarehouseType(String tenantId, String warehouseType);

    /**
     * Find by tenant and active status
     */
    List<WarehouseEntity> findByTenantAndIsActive(TenantEntity tenant, Boolean isActive);

    /**
     * Find by tenant ID and active status
     */
    List<WarehouseEntity> findByTenant_TenantIdAndIsActive(String tenantId, Boolean isActive);

    /**
     * Check if warehouse code exists for tenant
     */
    boolean existsByTenantAndWarehouseCode(TenantEntity tenant, String warehouseCode);

    /**
     * Count warehouses by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Find all warehouses by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT w FROM WarehouseEntity w " +
           "JOIN FETCH w.tenant " +
           "LEFT JOIN FETCH w.manager " +
           "WHERE w.tenant.tenantId = :tenantId " +
           "ORDER BY w.warehouseCode ASC")
    List<WarehouseEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find warehouse by ID with all relationships eagerly loaded
     */
    @Query("SELECT w FROM WarehouseEntity w " +
           "JOIN FETCH w.tenant " +
           "LEFT JOIN FETCH w.manager " +
           "WHERE w.warehouseId = :warehouseId")
    Optional<WarehouseEntity> findByIdWithAllRelations(@Param("warehouseId") Long warehouseId);

    /**
     * Find active warehouses by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT w FROM WarehouseEntity w " +
           "JOIN FETCH w.tenant " +
           "LEFT JOIN FETCH w.manager " +
           "WHERE w.tenant.tenantId = :tenantId AND w.isActive = :isActive " +
           "ORDER BY w.warehouseCode ASC")
    List<WarehouseEntity> findByTenantIdAndIsActiveWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("isActive") Boolean isActive
    );
}
