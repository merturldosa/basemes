package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Inventory Repository
 * 재고 현황 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    List<InventoryEntity> findByTenant_TenantId(String tenantId);
    List<InventoryEntity> findByTenant_TenantIdAndWarehouse_WarehouseId(String tenantId, Long warehouseId);
    List<InventoryEntity> findByTenant_TenantIdAndProduct_ProductId(String tenantId, Long productId);
    List<InventoryEntity> findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
        String tenantId, Long warehouseId, Long productId);
    Optional<InventoryEntity> findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
        String tenantId, Long warehouseId, Long productId, Long lotId);
    List<InventoryEntity> findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
        Long warehouseId, Long productId, BigDecimal minQuantity);
    Optional<InventoryEntity> findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
        Long warehouseId, Long productId, Long lotId);

    @Query("SELECT i FROM InventoryEntity i " +
           "JOIN FETCH i.tenant " +
           "JOIN FETCH i.warehouse " +
           "JOIN FETCH i.product " +
           "LEFT JOIN FETCH i.lot " +
           "WHERE i.tenant.tenantId = :tenantId " +
           "ORDER BY i.warehouse.warehouseCode, i.product.productCode")
    List<InventoryEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT i FROM InventoryEntity i " +
           "JOIN FETCH i.tenant " +
           "JOIN FETCH i.warehouse " +
           "JOIN FETCH i.product " +
           "LEFT JOIN FETCH i.lot " +
           "WHERE i.inventoryId = :inventoryId")
    Optional<InventoryEntity> findByIdWithAllRelations(@Param("inventoryId") Long inventoryId);

    @Query("SELECT i FROM InventoryEntity i " +
           "JOIN FETCH i.tenant " +
           "JOIN FETCH i.warehouse " +
           "JOIN FETCH i.product " +
           "LEFT JOIN FETCH i.lot " +
           "WHERE i.tenant.tenantId = :tenantId AND i.warehouse.warehouseId = :warehouseId")
    List<InventoryEntity> findByTenantIdAndWarehouseIdWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("warehouseId") Long warehouseId
    );
}
