package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Equipment Inspection Entity
 * 설비 점검 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_equipment_inspections",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inspection_no", columnNames = {"tenant_id", "inspection_no"})
    },
    indexes = {
        @Index(name = "idx_inspection_tenant", columnList = "tenant_id"),
        @Index(name = "idx_inspection_equipment", columnList = "equipment_id"),
        @Index(name = "idx_inspection_date", columnList = "inspection_date"),
        @Index(name = "idx_inspection_type", columnList = "inspection_type"),
        @Index(name = "idx_inspection_result", columnList = "inspection_result")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentInspectionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id")
    private Long inspectionId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipment;

    @Column(name = "inspection_no", nullable = false, length = 50)
    private String inspectionNo;

    // Inspection Info
    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate;

    @Column(name = "inspection_type", nullable = false, length = 30)
    private String inspectionType; // DAILY, PERIODIC, PREVENTIVE, CORRECTIVE, BREAKDOWN

    // Inspector
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_user_id")
    private UserEntity inspectorUser;

    @Column(name = "inspector_name", length = 100)
    private String inspectorName;

    // Inspection Details
    @Column(name = "inspection_items", columnDefinition = "TEXT")
    private String inspectionItems; // JSON array

    @Column(name = "inspection_result", nullable = false, length = 30)
    private String inspectionResult; // PASS, FAIL, CONDITIONAL

    // Findings
    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "abnormality_detected")
    private Boolean abnormalityDetected;

    @Column(name = "severity", length = 30)
    private String severity; // CRITICAL, MAJOR, MINOR

    // Actions
    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "corrective_action_date")
    private LocalDateTime correctiveActionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private UserEntity responsibleUser;

    @Column(name = "responsible_user_name", length = 100)
    private String responsibleUserName;

    // Parts Replacement
    @Column(name = "parts_replaced", columnDefinition = "TEXT")
    private String partsReplaced;

    @Column(name = "parts_cost", precision = 15, scale = 2)
    private BigDecimal partsCost;

    @Column(name = "labor_hours", precision = 10, scale = 2)
    private BigDecimal laborHours;

    @Column(name = "labor_cost", precision = 15, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;

    // Next Inspection
    @Column(name = "next_inspection_date")
    private LocalDate nextInspectionDate;

    @Column(name = "next_inspection_type", length = 30)
    private String nextInspectionType;

    // Additional
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // URLs

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
