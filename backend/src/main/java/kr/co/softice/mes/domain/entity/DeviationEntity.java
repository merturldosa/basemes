package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Deviation Entity
 * 이탈 관리 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "si_deviations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_deviation_no", columnNames = {"tenant_id", "deviation_no"})
    },
    indexes = {
        @Index(name = "idx_deviation_tenant", columnList = "tenant_id"),
        @Index(name = "idx_deviation_equipment", columnList = "equipment_id"),
        @Index(name = "idx_deviation_status", columnList = "status"),
        @Index(name = "idx_deviation_severity", columnList = "severity"),
        @Index(name = "idx_deviation_detected_at", columnList = "detected_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deviation_id")
    private Long deviationId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "deviation_no", nullable = false, length = 50)
    private String deviationNo;

    // Equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipment;

    @Column(name = "parameter_name", nullable = false, length = 200)
    private String parameterName;

    @Column(name = "standard_value", length = 100)
    private String standardValue;

    @Column(name = "actual_value", length = 100)
    private String actualValue;

    @Column(name = "deviation_value", length = 100)
    private String deviationValue;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    // Detected By User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detected_by_user_id")
    private UserEntity detectedByUser;

    @Column(name = "severity", length = 30)
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "preventive_action", columnDefinition = "TEXT")
    private String preventiveAction;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // OPEN, INVESTIGATING, RESOLVED, CLOSED

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Resolved By User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private UserEntity resolvedByUser;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
