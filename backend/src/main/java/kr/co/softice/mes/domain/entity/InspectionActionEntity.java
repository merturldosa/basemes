package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Inspection Action Entity
 * 점검 조치 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "si_inspection_actions",
    indexes = {
        @Index(name = "idx_inspection_action_tenant", columnList = "tenant_id"),
        @Index(name = "idx_inspection_action_inspection", columnList = "inspection_id"),
        @Index(name = "idx_inspection_action_type", columnList = "action_type"),
        @Index(name = "idx_inspection_action_status", columnList = "status"),
        @Index(name = "idx_inspection_action_due_date", columnList = "due_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionActionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Parent Inspection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private EquipmentInspectionEntity inspection;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Assigned User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
