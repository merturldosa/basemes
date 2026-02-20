package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Production Schedule Entity
 * 생산 일정 엔티티
 * WorkOrder의 공정별 상세 일정을 관리
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "mes",
    name = "sd_production_schedules",
    indexes = {
        @Index(name = "idx_schedule_tenant", columnList = "tenant_id"),
        @Index(name = "idx_schedule_work_order", columnList = "work_order_id"),
        @Index(name = "idx_schedule_routing_step", columnList = "routing_step_id"),
        @Index(name = "idx_schedule_status", columnList = "status"),
        @Index(name = "idx_schedule_planned_time", columnList = "planned_start_time,planned_end_time"),
        @Index(name = "idx_schedule_equipment", columnList = "assigned_equipment_id"),
        @Index(name = "idx_schedule_sequence", columnList = "work_order_id,sequence_order"),
        @Index(name = "idx_schedule_delayed", columnList = "is_delayed"),
        @Index(name = "idx_schedule_user", columnList = "assigned_user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_schedule_work_order_step", columnNames = {"work_order_id", "routing_step_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionScheduleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_step_id", nullable = false)
    private ProcessRoutingStepEntity routingStep;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    // 계획 일정
    @Column(name = "planned_start_time", nullable = false)
    private LocalDateTime plannedStartTime;

    @Column(name = "planned_end_time", nullable = false)
    private LocalDateTime plannedEndTime;

    @Column(name = "planned_duration", nullable = false)
    private Integer plannedDuration;  // 분 단위

    // 실제 일정
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @Column(name = "actual_duration")
    private Integer actualDuration;  // 분 단위, 트리거로 자동 계산

    // 리소스 할당
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_equipment_id")
    private EquipmentEntity assignedEquipment;

    @Column(name = "assigned_workers")
    @Builder.Default
    private Integer assignedWorkers = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;

    // 상태: SCHEDULED, READY, IN_PROGRESS, COMPLETED, DELAYED, CANCELLED
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "SCHEDULED";

    // 진행률 (%)
    @Column(name = "progress_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressRate = BigDecimal.ZERO;

    // 지연 정보 (트리거로 자동 계산)
    @Column(name = "is_delayed")
    @Builder.Default
    private Boolean isDelayed = false;

    @Column(name = "delay_minutes")
    @Builder.Default
    private Integer delayMinutes = 0;

    @Column(name = "delay_reason", columnDefinition = "TEXT")
    private String delayReason;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
