package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ShippingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Shipping Repository
 * 출하 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ShippingRepository extends JpaRepository<ShippingEntity, Long> {

    /**
     * Find all shippings by tenant with all relations
     */
    @Query("SELECT DISTINCT s FROM ShippingEntity s " +
           "JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.salesOrder so " +
           "LEFT JOIN FETCH so.tenant " +
           "LEFT JOIN FETCH s.customer c " +
           "LEFT JOIN FETCH c.tenant " +
           "JOIN FETCH s.warehouse w " +
           "JOIN FETCH w.tenant " +
           "LEFT JOIN FETCH s.shipper " +
           "LEFT JOIN FETCH s.items si " +
           "LEFT JOIN FETCH si.salesOrderItem " +
           "LEFT JOIN FETCH si.product p " +
           "LEFT JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH si.qualityInspection " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "ORDER BY s.shippingDate DESC")
    List<ShippingEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find shipping by ID with all relations
     */
    @Query("SELECT DISTINCT s FROM ShippingEntity s " +
           "JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.salesOrder so " +
           "LEFT JOIN FETCH so.tenant " +
           "LEFT JOIN FETCH s.customer c " +
           "LEFT JOIN FETCH c.tenant " +
           "JOIN FETCH s.warehouse w " +
           "JOIN FETCH w.tenant " +
           "LEFT JOIN FETCH s.shipper " +
           "LEFT JOIN FETCH s.items si " +
           "LEFT JOIN FETCH si.salesOrderItem " +
           "LEFT JOIN FETCH si.product p " +
           "LEFT JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH si.qualityInspection " +
           "WHERE s.shippingId = :shippingId")
    Optional<ShippingEntity> findByIdWithAllRelations(@Param("shippingId") Long shippingId);

    /**
     * Find shippings by status
     */
    @Query("SELECT DISTINCT s FROM ShippingEntity s " +
           "JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.salesOrder " +
           "LEFT JOIN FETCH s.customer " +
           "JOIN FETCH s.warehouse " +
           "LEFT JOIN FETCH s.shipper " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.shippingStatus = :status " +
           "ORDER BY s.shippingDate DESC")
    List<ShippingEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId,
                                                  @Param("status") String status);

    /**
     * Find shippings by sales order
     */
    @Query("SELECT DISTINCT s FROM ShippingEntity s " +
           "JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.salesOrder " +
           "LEFT JOIN FETCH s.customer " +
           "JOIN FETCH s.warehouse " +
           "LEFT JOIN FETCH s.shipper " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.salesOrder.salesOrderId = :salesOrderId " +
           "ORDER BY s.shippingDate DESC")
    List<ShippingEntity> findByTenantIdAndSalesOrderId(@Param("tenantId") String tenantId,
                                                        @Param("salesOrderId") Long salesOrderId);

    /**
     * Find shippings by warehouse
     */
    @Query("SELECT DISTINCT s FROM ShippingEntity s " +
           "JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.salesOrder " +
           "LEFT JOIN FETCH s.customer " +
           "JOIN FETCH s.warehouse " +
           "LEFT JOIN FETCH s.shipper " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.warehouse.warehouseId = :warehouseId " +
           "ORDER BY s.shippingDate DESC")
    List<ShippingEntity> findByTenantIdAndWarehouseId(@Param("tenantId") String tenantId,
                                                       @Param("warehouseId") Long warehouseId);

    /**
     * Find shippings by date range
     */
    @Query("SELECT DISTINCT s FROM ShippingEntity s " +
           "JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.salesOrder " +
           "LEFT JOIN FETCH s.customer " +
           "JOIN FETCH s.warehouse " +
           "LEFT JOIN FETCH s.shipper " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.shippingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.shippingDate DESC")
    List<ShippingEntity> findByTenantIdAndDateRange(@Param("tenantId") String tenantId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Check if shipping no exists
     */
    boolean existsByTenant_TenantIdAndShippingNo(String tenantId, String shippingNo);
}
