package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ProductionScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Production Schedule Repository
 * 생산 일정 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ProductionScheduleRepository extends JpaRepository<ProductionScheduleEntity, Long> {

    // 기본 조회
    List<ProductionScheduleEntity> findByTenant_TenantId(String tenantId);

    List<ProductionScheduleEntity> findByWorkOrder_WorkOrderId(Long workOrderId);

    List<ProductionScheduleEntity> findByTenant_TenantIdAndStatus(String tenantId, String status);

    List<ProductionScheduleEntity> findByTenant_TenantIdAndIsDelayed(String tenantId, Boolean isDelayed);

    // JOIN FETCH로 N+1 방지
    @Query("SELECT DISTINCT s FROM ProductionScheduleEntity s " +
           "LEFT JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.workOrder wo " +
           "LEFT JOIN FETCH wo.product " +
           "LEFT JOIN FETCH s.routingStep rs " +
           "LEFT JOIN FETCH rs.process " +
           "LEFT JOIN FETCH s.assignedEquipment " +
           "LEFT JOIN FETCH s.assignedUser " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "ORDER BY s.plannedStartTime ASC")
    List<ProductionScheduleEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT s FROM ProductionScheduleEntity s " +
           "LEFT JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.workOrder wo " +
           "LEFT JOIN FETCH wo.product " +
           "LEFT JOIN FETCH s.routingStep rs " +
           "LEFT JOIN FETCH rs.process " +
           "LEFT JOIN FETCH s.assignedEquipment " +
           "LEFT JOIN FETCH s.assignedUser " +
           "WHERE s.scheduleId = :scheduleId")
    Optional<ProductionScheduleEntity> findByIdWithAllRelations(@Param("scheduleId") Long scheduleId);

    @Query("SELECT DISTINCT s FROM ProductionScheduleEntity s " +
           "LEFT JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.workOrder wo " +
           "LEFT JOIN FETCH wo.product " +
           "LEFT JOIN FETCH s.routingStep rs " +
           "LEFT JOIN FETCH rs.process " +
           "LEFT JOIN FETCH s.assignedEquipment " +
           "LEFT JOIN FETCH s.assignedUser " +
           "WHERE s.workOrder.workOrderId = :workOrderId " +
           "ORDER BY s.sequenceOrder ASC")
    List<ProductionScheduleEntity> findByWorkOrderIdWithAllRelations(@Param("workOrderId") Long workOrderId);

    // 기간별 조회 (Gantt Chart용)
    @Query("SELECT DISTINCT s FROM ProductionScheduleEntity s " +
           "LEFT JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.workOrder wo " +
           "LEFT JOIN FETCH wo.product " +
           "LEFT JOIN FETCH s.routingStep rs " +
           "LEFT JOIN FETCH rs.process " +
           "LEFT JOIN FETCH s.assignedEquipment " +
           "LEFT JOIN FETCH s.assignedUser " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.plannedStartTime >= :startTime " +
           "AND s.plannedEndTime <= :endTime " +
           "ORDER BY s.plannedStartTime ASC")
    List<ProductionScheduleEntity> findByPeriod(
        @Param("tenantId") String tenantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // 상태별 조회
    @Query("SELECT DISTINCT s FROM ProductionScheduleEntity s " +
           "LEFT JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.workOrder wo " +
           "LEFT JOIN FETCH wo.product " +
           "LEFT JOIN FETCH s.routingStep rs " +
           "LEFT JOIN FETCH rs.process " +
           "LEFT JOIN FETCH s.assignedEquipment " +
           "LEFT JOIN FETCH s.assignedUser " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.status = :status " +
           "ORDER BY s.plannedStartTime ASC")
    List<ProductionScheduleEntity> findByTenantIdAndStatusWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("status") String status
    );

    // 지연 일정 조회
    @Query("SELECT DISTINCT s FROM ProductionScheduleEntity s " +
           "LEFT JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.workOrder wo " +
           "LEFT JOIN FETCH wo.product " +
           "LEFT JOIN FETCH s.routingStep rs " +
           "LEFT JOIN FETCH rs.process " +
           "LEFT JOIN FETCH s.assignedEquipment " +
           "LEFT JOIN FETCH s.assignedUser " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.isDelayed = true " +
           "ORDER BY s.delayMinutes DESC")
    List<ProductionScheduleEntity> findDelayedSchedules(@Param("tenantId") String tenantId);

    // 설비별 일정 조회 (리소스 충돌 체크용)
    @Query("SELECT s FROM ProductionScheduleEntity s " +
           "WHERE s.assignedEquipment.equipmentId = :equipmentId " +
           "AND s.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "AND ((s.plannedStartTime <= :endTime AND s.plannedEndTime >= :startTime)) " +
           "ORDER BY s.plannedStartTime ASC")
    List<ProductionScheduleEntity> findConflictingSchedulesByEquipment(
        @Param("equipmentId") Long equipmentId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // 작업자별 일정 조회
    @Query("SELECT DISTINCT s FROM ProductionScheduleEntity s " +
           "LEFT JOIN FETCH s.tenant " +
           "LEFT JOIN FETCH s.workOrder wo " +
           "LEFT JOIN FETCH wo.product " +
           "LEFT JOIN FETCH s.routingStep rs " +
           "LEFT JOIN FETCH rs.process " +
           "WHERE s.assignedUser.userId = :userId " +
           "AND s.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY s.plannedStartTime ASC")
    List<ProductionScheduleEntity> findByAssignedUser(@Param("userId") Long userId);

    // 통계 쿼리
    @Query("SELECT COUNT(s) FROM ProductionScheduleEntity s " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.status = :status")
    long countByTenantAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    @Query("SELECT COUNT(s) FROM ProductionScheduleEntity s " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.isDelayed = true")
    long countDelayedSchedules(@Param("tenantId") String tenantId);
}
