package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Inspection Plan Entity
 * 점검 계획 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_inspection_plans",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inspection_plan_code", columnNames = {"tenant_id", "plan_code"})
    },
    indexes = {
        @Index(name = "idx_inspection_plan_tenant", columnList = "tenant_id"),
        @Index(name = "idx_inspection_plan_equipment", columnList = "equipment_id"),
        @Index(name = "idx_inspection_plan_form", columnList = "form_id"),
        @Index(name = "idx_inspection_plan_status", columnList = "status"),
        @Index(name = "idx_inspection_plan_next_due", columnList = "next_due_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionPlanEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "plan_code", nullable = false, length = 50)
    private String planCode;

    @Column(name = "plan_name", nullable = false, length = 200)
    private String planName;

    // Equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipment;

    // Inspection Form
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private InspectionFormEntity form;

    @Column(name = "inspection_type", nullable = false, length = 30)
    private String inspectionType;

    @Column(name = "cycle_days")
    private Integer cycleDays;

    // Assigned User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;

    @Column(name = "last_execution_date")
    private LocalDate lastExecutionDate;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
