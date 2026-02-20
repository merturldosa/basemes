package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import kr.co.softice.mes.domain.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Downtime Entity
 * 설비 비가동 이력 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_downtimes",
        indexes = {
                @Index(name = "idx_downtime_tenant", columnList = "tenant_id"),
                @Index(name = "idx_downtime_equipment", columnList = "equipment_id"),
                @Index(name = "idx_downtime_type", columnList = "downtime_type"),
                @Index(name = "idx_downtime_start_time", columnList = "start_time"),
                @Index(name = "idx_downtime_work_order", columnList = "work_order_id"),
                @Index(name = "idx_downtime_operation", columnList = "operation_id"),
                @Index(name = "idx_downtime_is_resolved", columnList = "is_resolved")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_downtime_code", columnNames = {"tenant_id", "downtime_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DowntimeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "downtime_id")
    private Long downtimeId;

    // Tenant relationship (multi-tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Equipment relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipment;

    @Column(name = "downtime_code", length = 50, nullable = false)
    private String downtimeCode;

    // Downtime classification
    @Column(name = "downtime_type", length = 30, nullable = false)
    private String downtimeType; // BREAKDOWN, SETUP_CHANGE, MATERIAL_SHORTAGE, QUALITY_ISSUE, PLANNED_MAINTENANCE, UNPLANNED_MAINTENANCE, NO_ORDER, OTHER

    @Column(name = "downtime_category", length = 100)
    private String downtimeCategory;

    // Time tracking
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // Auto-calculated by database trigger

    // Optional relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private EquipmentOperationEntity operation;

    // Responsible person
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private UserEntity responsibleUser;

    @Column(name = "responsible_name", length = 100)
    private String responsibleName;

    // Analysis
    @Column(name = "cause", columnDefinition = "TEXT")
    private String cause;

    @Column(name = "countermeasure", columnDefinition = "TEXT")
    private String countermeasure;

    @Column(name = "preventive_action", columnDefinition = "TEXT")
    private String preventiveAction;

    // Status
    @Column(name = "is_resolved")
    private Boolean isResolved;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Common fields
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
