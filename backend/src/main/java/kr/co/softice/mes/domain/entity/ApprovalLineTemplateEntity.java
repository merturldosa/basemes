package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Approval Line Template Entity
 * 결재 라인 템플릿 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "approval_line_templates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_approval_template_code", columnNames = {"tenant_id", "template_code"})
    },
    indexes = {
        @Index(name = "idx_approval_template_tenant", columnList = "tenant_id"),
        @Index(name = "idx_approval_template_doc_type", columnList = "document_type"),
        @Index(name = "idx_approval_template_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLineTemplateEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;  // PURCHASE_ORDER, WORK_ORDER, SALES_ORDER, etc.

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Approval Settings
    @Column(name = "approval_type", nullable = false, length = 20)
    @Builder.Default
    private String approvalType = "SEQUENTIAL";  // SEQUENTIAL, PARALLEL, HYBRID

    @Column(name = "auto_approve_amount", precision = 15, scale = 2)
    private BigDecimal autoApproveAmount;

    @Column(name = "skip_if_same_person")
    @Builder.Default
    private Boolean skipIfSamePerson = true;

    // Status
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Relationships
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<ApprovalLineStepEntity> steps = new ArrayList<>();

    /**
     * Add approval step
     */
    public void addStep(ApprovalLineStepEntity step) {
        steps.add(step);
        step.setTemplate(this);
    }

    /**
     * Remove approval step
     */
    public void removeStep(ApprovalLineStepEntity step) {
        steps.remove(step);
        step.setTemplate(null);
    }

    /**
     * Check if this is a sequential approval
     */
    public boolean isSequentialApproval() {
        return "SEQUENTIAL".equals(approvalType);
    }

    /**
     * Check if this is a parallel approval
     */
    public boolean isParallelApproval() {
        return "PARALLEL".equals(approvalType);
    }

    /**
     * Check if auto-approval is enabled for given amount
     */
    public boolean shouldAutoApprove(BigDecimal amount) {
        if (autoApproveAmount == null || amount == null) {
            return false;
        }
        return amount.compareTo(autoApproveAmount) < 0;
    }

    /**
     * Get total number of steps
     */
    public int getTotalSteps() {
        return steps.size();
    }

    /**
     * Get mandatory steps count
     */
    public long getMandatoryStepsCount() {
        return steps.stream()
                .filter(step -> step.getIsMandatory() != null && step.getIsMandatory())
                .count();
    }

    /**
     * Check if template is valid
     */
    public boolean isValid() {
        return isActive && !steps.isEmpty();
    }
}
