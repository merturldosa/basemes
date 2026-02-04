package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.entity.WorkResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Work Result Repository
 * 작업 실적 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface WorkResultRepository extends JpaRepository<WorkResultEntity, Long> {

    /**
     * Find by work order
     */
    List<WorkResultEntity> findByWorkOrder(WorkOrderEntity workOrder);

    /**
     * Find by work order ID
     */
    List<WorkResultEntity> findByWorkOrder_WorkOrderId(Long workOrderId);

    /**
     * Find by work order ordered by result date
     */
    List<WorkResultEntity> findByWorkOrderOrderByResultDateDesc(WorkOrderEntity workOrder);

    /**
     * Find by tenant
     */
    List<WorkResultEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<WorkResultEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and date range
     */
    List<WorkResultEntity> findByTenantAndResultDateBetween(
        TenantEntity tenant,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Find by tenant ID and date range
     */
    @Query("SELECT wr FROM WorkResultEntity wr WHERE wr.tenant.tenantId = :tenantId " +
           "AND wr.resultDate BETWEEN :startDate AND :endDate " +
           "ORDER BY wr.resultDate DESC")
    List<WorkResultEntity> findByTenantIdAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find by worker user ID
     */
    List<WorkResultEntity> findByWorker_UserId(Long userId);

    /**
     * Count results by work order
     */
    long countByWorkOrder(WorkOrderEntity workOrder);

    /**
     * Count results by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Delete by work order (cascade)
     */
    void deleteByWorkOrder(WorkOrderEntity workOrder);

    /**
     * Find all work results by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT wr FROM WorkResultEntity wr " +
           "JOIN FETCH wr.tenant " +
           "JOIN FETCH wr.workOrder " +
           "LEFT JOIN FETCH wr.worker " +
           "WHERE wr.tenant.tenantId = :tenantId " +
           "ORDER BY wr.resultDate DESC")
    List<WorkResultEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find work results by work order ID with all relationships eagerly loaded
     */
    @Query("SELECT wr FROM WorkResultEntity wr " +
           "JOIN FETCH wr.tenant " +
           "JOIN FETCH wr.workOrder " +
           "LEFT JOIN FETCH wr.worker " +
           "WHERE wr.workOrder.workOrderId = :workOrderId " +
           "ORDER BY wr.resultDate DESC")
    List<WorkResultEntity> findByWorkOrderIdWithAllRelations(@Param("workOrderId") Long workOrderId);
}
