package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.AfterSalesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * After Sales Repository
 * A/S 관리 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface AfterSalesRepository extends JpaRepository<AfterSalesEntity, Long> {

    /**
     * Get all after sales by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT a FROM AfterSalesEntity a " +
           "JOIN FETCH a.tenant " +
           "JOIN FETCH a.customer " +
           "JOIN FETCH a.product " +
           "LEFT JOIN FETCH a.salesOrder " +
           "LEFT JOIN FETCH a.shipping " +
           "LEFT JOIN FETCH a.assignedEngineer " +
           "WHERE a.tenant.tenantId = :tenantId " +
           "ORDER BY a.receiptDate DESC")
    List<AfterSalesEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get after sales by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT a FROM AfterSalesEntity a " +
           "JOIN FETCH a.tenant " +
           "JOIN FETCH a.customer " +
           "JOIN FETCH a.product " +
           "LEFT JOIN FETCH a.salesOrder " +
           "LEFT JOIN FETCH a.shipping " +
           "LEFT JOIN FETCH a.assignedEngineer " +
           "WHERE a.afterSalesId = :afterSalesId")
    Optional<AfterSalesEntity> findByIdWithAllRelations(@Param("afterSalesId") Long afterSalesId);

    /**
     * Get after sales by tenant and service status
     */
    @Query("SELECT a FROM AfterSalesEntity a " +
           "JOIN FETCH a.tenant " +
           "JOIN FETCH a.customer " +
           "JOIN FETCH a.product " +
           "LEFT JOIN FETCH a.salesOrder " +
           "LEFT JOIN FETCH a.shipping " +
           "LEFT JOIN FETCH a.assignedEngineer " +
           "WHERE a.tenant.tenantId = :tenantId " +
           "AND a.serviceStatus = :serviceStatus " +
           "ORDER BY a.receiptDate DESC")
    List<AfterSalesEntity> findByTenantIdAndServiceStatus(@Param("tenantId") String tenantId, @Param("serviceStatus") String serviceStatus);

    /**
     * Get after sales by tenant and priority
     */
    @Query("SELECT a FROM AfterSalesEntity a " +
           "JOIN FETCH a.tenant " +
           "JOIN FETCH a.customer " +
           "JOIN FETCH a.product " +
           "LEFT JOIN FETCH a.salesOrder " +
           "LEFT JOIN FETCH a.shipping " +
           "LEFT JOIN FETCH a.assignedEngineer " +
           "WHERE a.tenant.tenantId = :tenantId " +
           "AND a.priority = :priority " +
           "ORDER BY a.receiptDate DESC")
    List<AfterSalesEntity> findByTenantIdAndPriority(@Param("tenantId") String tenantId, @Param("priority") String priority);

    /**
     * Check if A/S number exists for tenant
     */
    boolean existsByTenant_TenantIdAndAsNo(String tenantId, String asNo);
}
