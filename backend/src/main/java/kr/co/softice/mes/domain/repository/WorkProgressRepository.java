package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.entity.WorkProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Work Progress Repository
 * 작업 진행 기록 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface WorkProgressRepository extends JpaRepository<WorkProgressEntity, Long> {

    /**
     * Find by work order
     */
    List<WorkProgressEntity> findByWorkOrder(WorkOrderEntity workOrder);

    /**
     * Find by work order ID
     */
    List<WorkProgressEntity> findByWorkOrder_WorkOrderId(Long workOrderId);

    /**
     * Find active work progress by work order
     */
    Optional<WorkProgressEntity> findByWorkOrderAndIsActiveTrue(WorkOrderEntity workOrder);

    /**
     * Find active work progress by work order ID
     */
    Optional<WorkProgressEntity> findByWorkOrder_WorkOrderIdAndIsActiveTrue(Long workOrderId);

    /**
     * Find by operator
     */
    List<WorkProgressEntity> findByOperator(UserEntity operator);

    /**
     * Find by operator and record date
     */
    List<WorkProgressEntity> findByOperatorAndRecordDate(UserEntity operator, LocalDate recordDate);

    /**
     * Find by operator ID and record date
     */
    List<WorkProgressEntity> findByOperator_UserIdAndRecordDate(Long operatorId, LocalDate recordDate);

    /**
     * Find by tenant and record date
     */
    List<WorkProgressEntity> findByTenantAndRecordDate(TenantEntity tenant, LocalDate recordDate);

    /**
     * Find by tenant ID and record date
     */
    List<WorkProgressEntity> findByTenant_TenantIdAndRecordDate(String tenantId, LocalDate recordDate);

    /**
     * Find by tenant and status
     */
    List<WorkProgressEntity> findByTenantAndStatus(TenantEntity tenant, String status);

    /**
     * Find by tenant ID and status
     */
    List<WorkProgressEntity> findByTenant_TenantIdAndStatus(String tenantId, String status);

    /**
     * Find by tenant and date range
     */
    List<WorkProgressEntity> findByTenantAndRecordDateBetween(
        TenantEntity tenant,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Find by tenant ID and date range
     */
    @Query("SELECT wp FROM WorkProgressEntity wp WHERE wp.tenant.tenantId = :tenantId " +
           "AND wp.recordDate BETWEEN :startDate AND :endDate " +
           "ORDER BY wp.recordDate DESC, wp.startTime DESC")
    List<WorkProgressEntity> findByTenantIdAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find today's work progress by operator
     */
    @Query("SELECT wp FROM WorkProgressEntity wp WHERE wp.operator.userId = :operatorId " +
           "AND wp.recordDate = :today " +
           "ORDER BY wp.startTime DESC")
    List<WorkProgressEntity> findTodayWorkProgressByOperator(
        @Param("operatorId") Long operatorId,
        @Param("today") LocalDate today
    );

    /**
     * Find active work progress by tenant
     */
    List<WorkProgressEntity> findByTenantAndIsActiveTrue(TenantEntity tenant);

    /**
     * Find active work progress by tenant ID
     */
    List<WorkProgressEntity> findByTenant_TenantIdAndIsActiveTrue(String tenantId);

    /**
     * Count by work order
     */
    long countByWorkOrder(WorkOrderEntity workOrder);

    /**
     * Count by tenant and status
     */
    long countByTenantAndStatus(TenantEntity tenant, String status);

    /**
     * Find work progress with all relationships
     */
    @Query("SELECT wp FROM WorkProgressEntity wp " +
           "JOIN FETCH wp.tenant " +
           "JOIN FETCH wp.workOrder " +
           "JOIN FETCH wp.operator " +
           "LEFT JOIN FETCH wp.equipment " +
           "WHERE wp.progressId = :progressId")
    Optional<WorkProgressEntity> findByIdWithAllRelations(@Param("progressId") Long progressId);

    /**
     * Find active work progress by tenant with all relationships
     */
    @Query("SELECT wp FROM WorkProgressEntity wp " +
           "JOIN FETCH wp.tenant " +
           "JOIN FETCH wp.workOrder wo " +
           "JOIN FETCH wo.product " +
           "JOIN FETCH wo.process " +
           "JOIN FETCH wp.operator " +
           "LEFT JOIN FETCH wp.equipment " +
           "WHERE wp.tenant.tenantId = :tenantId AND wp.isActive = true " +
           "ORDER BY wp.startTime DESC")
    List<WorkProgressEntity> findActiveByTenantIdWithAllRelations(@Param("tenantId") String tenantId);
}
