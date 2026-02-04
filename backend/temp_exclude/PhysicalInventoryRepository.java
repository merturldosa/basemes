package kr.co.softice.mes.domain.repository.inventory;

import kr.co.softice.mes.domain.entity.inventory.PhysicalInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Physical Inventory Repository
 * 실사 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface PhysicalInventoryRepository extends JpaRepository<PhysicalInventoryEntity, Long> {

    List<PhysicalInventoryEntity> findByTenant_TenantId(String tenantId);

    List<PhysicalInventoryEntity> findByTenant_TenantIdAndInventoryStatus(String tenantId, String inventoryStatus);

    List<PhysicalInventoryEntity> findByTenant_TenantIdAndWarehouse_WarehouseId(String tenantId, Long warehouseId);

    Optional<PhysicalInventoryEntity> findByTenant_TenantIdAndInventoryNo(String tenantId, String inventoryNo);

    boolean existsByTenant_TenantIdAndInventoryNo(String tenantId, String inventoryNo);

    @Query("SELECT pi FROM PhysicalInventoryEntity pi " +
           "JOIN FETCH pi.tenant " +
           "JOIN FETCH pi.warehouse " +
           "LEFT JOIN FETCH pi.items " +
           "WHERE pi.tenant.tenantId = :tenantId " +
           "ORDER BY pi.inventoryDate DESC")
    List<PhysicalInventoryEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT pi FROM PhysicalInventoryEntity pi " +
           "JOIN FETCH pi.tenant " +
           "JOIN FETCH pi.warehouse " +
           "LEFT JOIN FETCH pi.items items " +
           "LEFT JOIN FETCH items.product " +
           "LEFT JOIN FETCH items.lot " +
           "WHERE pi.physicalInventoryId = :physicalInventoryId")
    Optional<PhysicalInventoryEntity> findByIdWithAllRelations(@Param("physicalInventoryId") Long physicalInventoryId);

    @Query("SELECT pi FROM PhysicalInventoryEntity pi " +
           "JOIN FETCH pi.tenant " +
           "JOIN FETCH pi.warehouse " +
           "WHERE pi.tenant.tenantId = :tenantId " +
           "AND pi.inventoryDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pi.inventoryDate DESC")
    List<PhysicalInventoryEntity> findByTenantIdAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(pi) FROM PhysicalInventoryEntity pi " +
           "WHERE pi.tenant.tenantId = :tenantId " +
           "AND pi.inventoryNo LIKE :prefix%")
    long countByTenantIdAndInventoryNoPrefix(
        @Param("tenantId") String tenantId,
        @Param("prefix") String prefix
    );
}
