package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Breakdown Entity
 * 고장 관리 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "si_breakdowns",
        indexes = {
                @Index(name = "idx_breakdown_tenant", columnList = "tenant_id"),
                @Index(name = "idx_breakdown_equipment", columnList = "equipment_id"),
                @Index(name = "idx_breakdown_status", columnList = "status"),
                @Index(name = "idx_breakdown_reported_at", columnList = "reported_at"),
                @Index(name = "idx_breakdown_failure_type", columnList = "failure_type"),
                @Index(name = "idx_breakdown_severity", columnList = "severity")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_breakdown_no", columnNames = {"tenant_id", "breakdown_no"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakdownEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "breakdown_id")
    private Long breakdownId;

    // Tenant relationship (multi-tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "breakdown_no", length = 50, nullable = false)
    private String breakdownNo;

    // Equipment relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipment;

    // Downtime relationship (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "downtime_id")
    private DowntimeEntity downtime;

    // Report information
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id")
    private UserEntity reportedByUser;

    // Classification
    @Column(name = "failure_type", length = 50)
    private String failureType;

    @Column(name = "severity", length = 30)
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    // Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    // Repair tracking
    @Column(name = "repair_started_at")
    private LocalDateTime repairStartedAt;

    @Column(name = "repair_completed_at")
    private LocalDateTime repairCompletedAt;

    @Column(name = "repair_duration_minutes")
    private Integer repairDurationMinutes;

    @Column(name = "repair_description", columnDefinition = "TEXT")
    private String repairDescription;

    @Column(name = "parts_used", columnDefinition = "TEXT")
    private String partsUsed;

    @Column(name = "repair_cost", precision = 15, scale = 2)
    private BigDecimal repairCost;

    // Root cause analysis
    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "preventive_action", columnDefinition = "TEXT")
    private String preventiveAction;

    // Status management
    @Column(name = "status", length = 30, nullable = false)
    private String status; // REPORTED, ASSIGNED, IN_PROGRESS, COMPLETED, CLOSED

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_user_id")
    private UserEntity closedByUser;

    // Common fields
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
