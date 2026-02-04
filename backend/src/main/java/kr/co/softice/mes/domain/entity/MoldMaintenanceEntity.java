package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import kr.co.softice.mes.domain.entity.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mold Maintenance Entity
 * 금형 보전 이력 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "si_mold_maintenances",
        indexes = {
                @Index(name = "idx_mold_maintenance_tenant", columnList = "tenant_id"),
                @Index(name = "idx_mold_maintenance_mold", columnList = "mold_id"),
                @Index(name = "idx_mold_maintenance_type", columnList = "maintenance_type"),
                @Index(name = "idx_mold_maintenance_date", columnList = "maintenance_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mold_maintenance_no", columnNames = {"tenant_id", "maintenance_no"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoldMaintenanceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_id")
    private Long maintenanceId;

    // Tenant relationship (multi-tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Mold relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mold_id", nullable = false)
    private MoldEntity mold;

    @Column(name = "maintenance_no", length = 50, nullable = false)
    private String maintenanceNo;

    // Maintenance classification
    @Column(name = "maintenance_type", length = 30, nullable = false)
    private String maintenanceType; // DAILY_CHECK, PERIODIC, SHOT_BASED, EMERGENCY_REPAIR, OVERHAUL

    @Column(name = "maintenance_date", nullable = false)
    private LocalDateTime maintenanceDate;

    // Shot tracking
    @Column(name = "shot_count_before")
    private Long shotCountBefore;

    @Column(name = "shot_count_reset")
    private Boolean shotCountReset;

    @Column(name = "shot_count_after")
    private Long shotCountAfter;

    // Personnel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_user_id")
    private UserEntity technicianUser;

    @Column(name = "technician_name", length = 100)
    private String technicianName;

    // Maintenance details
    @Column(name = "maintenance_content", columnDefinition = "TEXT")
    private String maintenanceContent;

    @Column(name = "parts_replaced", columnDefinition = "TEXT")
    private String partsReplaced;

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    // Cost
    @Column(name = "parts_cost", precision = 15, scale = 2)
    private BigDecimal partsCost;

    @Column(name = "labor_cost", precision = 15, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost; // Auto-calculated by database trigger

    @Column(name = "labor_hours")
    private Integer laborHours;

    // Result
    @Column(name = "maintenance_result", length = 30)
    private String maintenanceResult; // COMPLETED, PARTIAL, FAILED

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    // Common fields
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
