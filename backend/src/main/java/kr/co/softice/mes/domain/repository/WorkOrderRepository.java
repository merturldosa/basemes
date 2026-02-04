package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Work Order Repository
 * 작업 지시 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrderEntity, Long> {

    /**
     * Find by tenant and work order number
     */
    Optional<WorkOrderEntity> findByTenantAndWorkOrderNo(TenantEntity tenant, String workOrderNo);

    /**
     * Find by tenant ID and work order number
     */
    Optional<WorkOrderEntity> findByTenant_TenantIdAndWorkOrderNo(String tenantId, String workOrderNo);

    /**
     * Find by tenant
     */
    List<WorkOrderEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<WorkOrderEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and status
     */
    List<WorkOrderEntity> findByTenantAndStatus(TenantEntity tenant, String status);

    /**
     * Find by tenant ID and status
     */
    List<WorkOrderEntity> findByTenant_TenantIdAndStatus(String tenantId, String status);

    /**
     * Find by tenant ordered by planned start date
     */
    List<WorkOrderEntity> findByTenantOrderByPlannedStartDateDesc(TenantEntity tenant);

    /**
     * Find by tenant ID ordered by planned start date
     */
    List<WorkOrderEntity> findByTenant_TenantIdOrderByPlannedStartDateDesc(String tenantId);

    /**
     * Find by assigned user
     */
    List<WorkOrderEntity> findByAssignedUser(UserEntity user);

    /**
     * Find by tenant and date range
     */
    List<WorkOrderEntity> findByTenantAndPlannedStartDateBetween(
        TenantEntity tenant,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Find by tenant ID and date range
     */
    @Query("SELECT wo FROM WorkOrderEntity wo WHERE wo.tenant.tenantId = :tenantId " +
           "AND wo.plannedStartDate BETWEEN :startDate AND :endDate " +
           "ORDER BY wo.plannedStartDate DESC")
    List<WorkOrderEntity> findByTenantIdAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Check if work order number exists for tenant
     */
    boolean existsByTenantAndWorkOrderNo(TenantEntity tenant, String workOrderNo);

    /**
     * Count work orders by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Count work orders by tenant and status
     */
    long countByTenantAndStatus(TenantEntity tenant, String status);

    /**
     * Count work orders by tenant ID and status
     */
    long countByTenant_TenantIdAndStatus(String tenantId, String status);

    /**
     * Find all work orders by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT wo FROM WorkOrderEntity wo " +
           "JOIN FETCH wo.tenant " +
           "JOIN FETCH wo.product " +
           "JOIN FETCH wo.process " +
           "LEFT JOIN FETCH wo.assignedUser " +
           "WHERE wo.tenant.tenantId = :tenantId " +
           "ORDER BY wo.plannedStartDate DESC")
    List<WorkOrderEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find work orders by tenant ID and status with all relationships eagerly loaded
     */
    @Query("SELECT wo FROM WorkOrderEntity wo " +
           "JOIN FETCH wo.tenant " +
           "JOIN FETCH wo.product " +
           "JOIN FETCH wo.process " +
           "LEFT JOIN FETCH wo.assignedUser " +
           "WHERE wo.tenant.tenantId = :tenantId AND wo.status = :status " +
           "ORDER BY wo.plannedStartDate DESC")
    List<WorkOrderEntity> findByTenantIdAndStatusWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("status") String status
    );

    /**
     * Find work order by ID with all relationships eagerly loaded
     */
    @Query("SELECT wo FROM WorkOrderEntity wo " +
           "JOIN FETCH wo.tenant " +
           "JOIN FETCH wo.product " +
           "JOIN FETCH wo.process " +
           "LEFT JOIN FETCH wo.assignedUser " +
           "WHERE wo.workOrderId = :workOrderId")
    Optional<WorkOrderEntity> findByIdWithAllRelations(@Param("workOrderId") Long workOrderId);
}
