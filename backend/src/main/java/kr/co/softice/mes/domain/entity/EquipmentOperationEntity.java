package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Equipment Operation Entity
 * 설비 가동 이력 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "si_equipment_operations",
    indexes = {
        @Index(name = "idx_operation_tenant", columnList = "tenant_id"),
        @Index(name = "idx_operation_equipment", columnList = "equipment_id"),
        @Index(name = "idx_operation_date", columnList = "operation_date"),
        @Index(name = "idx_operation_status", columnList = "operation_status"),
        @Index(name = "idx_operation_work_order", columnList = "work_order_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentOperationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    private Long operationId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipment;

    // Work Order Link
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_result_id")
    private WorkResultEntity workResult;

    // Operation Time
    @Column(name = "operation_date", nullable = false)
    private LocalDate operationDate;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "operation_hours", precision = 10, scale = 2)
    private BigDecimal operationHours; // Calculated by trigger

    // Operator
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_user_id")
    private UserEntity operatorUser;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    // Production
    @Column(name = "production_quantity", precision = 15, scale = 3)
    private BigDecimal productionQuantity;

    @Column(name = "good_quantity", precision = 15, scale = 3)
    private BigDecimal goodQuantity;

    @Column(name = "defect_quantity", precision = 15, scale = 3)
    private BigDecimal defectQuantity;

    // Status
    @Column(name = "operation_status", nullable = false, length = 30)
    private String operationStatus; // RUNNING, COMPLETED, STOPPED, ABORTED

    // Stop Information
    @Column(name = "stop_reason", length = 100)
    private String stopReason;

    @Column(name = "stop_duration_minutes")
    private Integer stopDurationMinutes;

    // Performance Metrics
    @Column(name = "cycle_time", precision = 10, scale = 2)
    private BigDecimal cycleTime; // seconds

    @Column(name = "utilization_rate", precision = 5, scale = 2)
    private BigDecimal utilizationRate; // percentage

    @Column(name = "performance_rate", precision = 5, scale = 2)
    private BigDecimal performanceRate; // percentage

    @Column(name = "quality_rate", precision = 5, scale = 2)
    private BigDecimal qualityRate; // percentage

    @Column(name = "oee", precision = 5, scale = 2)
    private BigDecimal oee; // Overall Equipment Effectiveness percentage

    // Additional
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
