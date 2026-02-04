package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.DeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Delivery Repository
 * 출하 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {

    /**
     * Find all deliveries by tenant with all relations
     */
    @Query("SELECT DISTINCT d FROM DeliveryEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.salesOrder so " +
           "JOIN FETCH so.customer c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH d.warehouse w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH d.shipper " +
           "LEFT JOIN FETCH d.inspector " +
           "LEFT JOIN FETCH d.items di " +
           "LEFT JOIN FETCH di.salesOrderItem soi " +
           "LEFT JOIN FETCH di.product p " +
           "LEFT JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH di.material m " +
           "LEFT JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH di.lot " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "ORDER BY d.deliveryDate DESC")
    List<DeliveryEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find delivery by ID with all relations
     */
    @Query("SELECT DISTINCT d FROM DeliveryEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.salesOrder so " +
           "JOIN FETCH so.customer c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH d.warehouse w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH d.shipper " +
           "LEFT JOIN FETCH d.inspector " +
           "LEFT JOIN FETCH d.items di " +
           "LEFT JOIN FETCH di.salesOrderItem soi " +
           "LEFT JOIN FETCH di.product p " +
           "LEFT JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH di.material m " +
           "LEFT JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH di.lot " +
           "WHERE d.deliveryId = :deliveryId")
    Optional<DeliveryEntity> findByIdWithAllRelations(@Param("deliveryId") Long deliveryId);

    /**
     * Find deliveries by status
     */
    @Query("SELECT DISTINCT d FROM DeliveryEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.salesOrder so " +
           "JOIN FETCH so.customer " +
           "JOIN FETCH d.warehouse " +
           "JOIN FETCH d.shipper " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.status = :status " +
           "ORDER BY d.deliveryDate DESC")
    List<DeliveryEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId,
                                                  @Param("status") String status);

    /**
     * Find deliveries by sales order
     */
    @Query("SELECT DISTINCT d FROM DeliveryEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.salesOrder so " +
           "JOIN FETCH so.customer " +
           "JOIN FETCH d.warehouse " +
           "JOIN FETCH d.shipper " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.salesOrder.salesOrderId = :salesOrderId " +
           "ORDER BY d.deliveryDate DESC")
    List<DeliveryEntity> findByTenantIdAndSalesOrderId(@Param("tenantId") String tenantId,
                                                        @Param("salesOrderId") Long salesOrderId);

    /**
     * Find deliveries by quality check status
     */
    @Query("SELECT DISTINCT d FROM DeliveryEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.salesOrder so " +
           "JOIN FETCH so.customer " +
           "JOIN FETCH d.warehouse " +
           "JOIN FETCH d.shipper " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.qualityCheckStatus = :qualityCheckStatus " +
           "ORDER BY d.deliveryDate DESC")
    List<DeliveryEntity> findByTenantIdAndQualityCheckStatus(@Param("tenantId") String tenantId,
                                                              @Param("qualityCheckStatus") String qualityCheckStatus);

    /**
     * Find deliveries by date range
     */
    @Query("SELECT DISTINCT d FROM DeliveryEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.salesOrder so " +
           "JOIN FETCH so.customer " +
           "JOIN FETCH d.warehouse " +
           "JOIN FETCH d.shipper " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.deliveryDate BETWEEN :startDate AND :endDate " +
           "ORDER BY d.deliveryDate DESC")
    List<DeliveryEntity> findByTenantIdAndDeliveryDateBetween(@Param("tenantId") String tenantId,
                                                               @Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Check if delivery number exists
     */
    boolean existsByTenant_TenantIdAndDeliveryNo(String tenantId, String deliveryNo);

    /**
     * Find by tenant and delivery number
     */
    Optional<DeliveryEntity> findByTenant_TenantIdAndDeliveryNo(String tenantId, String deliveryNo);
}
