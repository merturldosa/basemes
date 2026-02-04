package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SalesOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Sales Order Repository
 * 판매 주문 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrderEntity, Long> {

    /**
     * Find all sales orders by tenant with all relations
     */
    @Query("SELECT DISTINCT so FROM SalesOrderEntity so " +
           "JOIN FETCH so.tenant " +
           "JOIN FETCH so.customer c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH so.salesUser " +
           "LEFT JOIN FETCH so.items soi " +
           "LEFT JOIN FETCH soi.product p " +
           "LEFT JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH soi.material m " +
           "LEFT JOIN FETCH m.tenant " +
           "WHERE so.tenant.tenantId = :tenantId " +
           "ORDER BY so.orderDate DESC")
    List<SalesOrderEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find sales order by ID with all relations
     */
    @Query("SELECT DISTINCT so FROM SalesOrderEntity so " +
           "JOIN FETCH so.tenant " +
           "JOIN FETCH so.customer c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH so.salesUser " +
           "LEFT JOIN FETCH so.items soi " +
           "LEFT JOIN FETCH soi.product p " +
           "LEFT JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH soi.material m " +
           "LEFT JOIN FETCH m.tenant " +
           "WHERE so.salesOrderId = :salesOrderId")
    Optional<SalesOrderEntity> findByIdWithAllRelations(@Param("salesOrderId") Long salesOrderId);

    /**
     * Find sales orders by status
     */
    @Query("SELECT DISTINCT so FROM SalesOrderEntity so " +
           "JOIN FETCH so.tenant " +
           "JOIN FETCH so.customer c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH so.salesUser " +
           "WHERE so.tenant.tenantId = :tenantId " +
           "AND so.status = :status " +
           "ORDER BY so.orderDate DESC")
    List<SalesOrderEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId,
                                                    @Param("status") String status);

    /**
     * Find sales orders by customer
     */
    @Query("SELECT DISTINCT so FROM SalesOrderEntity so " +
           "JOIN FETCH so.tenant " +
           "JOIN FETCH so.customer c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH so.salesUser " +
           "WHERE so.tenant.tenantId = :tenantId " +
           "AND so.customer.customerId = :customerId " +
           "ORDER BY so.orderDate DESC")
    List<SalesOrderEntity> findByTenantIdAndCustomerId(@Param("tenantId") String tenantId,
                                                        @Param("customerId") Long customerId);

    /**
     * Find sales orders by date range
     */
    @Query("SELECT DISTINCT so FROM SalesOrderEntity so " +
           "JOIN FETCH so.tenant " +
           "JOIN FETCH so.customer c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH so.salesUser " +
           "WHERE so.tenant.tenantId = :tenantId " +
           "AND so.orderDate BETWEEN :startDate AND :endDate " +
           "ORDER BY so.orderDate DESC")
    List<SalesOrderEntity> findByTenantIdAndOrderDateBetween(@Param("tenantId") String tenantId,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Check if sales order number exists
     */
    boolean existsByTenant_TenantIdAndOrderNo(String tenantId, String orderNo);

    /**
     * Find by tenant and order number
     */
    Optional<SalesOrderEntity> findByTenant_TenantIdAndOrderNo(String tenantId, String orderNo);
}
