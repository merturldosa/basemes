package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

/**
 * Approval Line Entity
 * 결재라인
 *
 * @author Moon Myung-seop
 */
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Entity
@Table(
    schema = "core",
    name = "si_approval_lines",
    indexes = {
        @Index(name = "idx_approval_line_tenant", columnList = "tenant_id"),
        @Index(name = "idx_approval_line_document_type", columnList = "document_type"),
        @Index(name = "idx_approval_line_department", columnList = "department_id"),
        @Index(name = "idx_approval_line_active", columnList = "is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_approval_line_code", columnNames = {"tenant_id", "line_code"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLineEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_line_id")
    private Long approvalLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "line_code", nullable = false, length = 50)
    private String lineCode;

    @Column(name = "line_name", nullable = false, length = 200)
    private String lineName;

    // Scope
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;  // PURCHASE_REQUEST, PURCHASE_ORDER, WORK_ORDER, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    // Approval Steps (JSON Array)
    // Example: [{"step":1,"approverUserId":10,"approverRole":"MANAGER"},{"step":2,"approverUserId":20,"approverRole":"DIRECTOR"}]
    @Type(type = "jsonb")
    @Column(name = "approval_steps", nullable = false, columnDefinition = "jsonb")
    private String approvalSteps;

    // Conditions (JSON)
    // Example: {"amountMin":0,"amountMax":1000000}
    @Type(type = "jsonb")
    @Column(name = "conditions", columnDefinition = "jsonb")
    private String conditions;

    // Status
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_default")
    private Boolean isDefault;

    // Priority (lower number = higher priority)
    @Column(name = "priority")
    private Integer priority;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
