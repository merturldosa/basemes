package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SOP (Standard Operating Procedure) Entity
 * 표준 작업 절차 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "sops",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sop_code", columnNames = {"tenant_id", "sop_code", "version"})
    },
    indexes = {
        @Index(name = "idx_sop_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sop_type", columnList = "sop_type"),
        @Index(name = "idx_sop_category", columnList = "category"),
        @Index(name = "idx_sop_status", columnList = "approval_status"),
        @Index(name = "idx_sop_active", columnList = "is_active"),
        @Index(name = "idx_sop_effective", columnList = "effective_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SOPEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sop_id")
    private Long sopId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "sop_code", nullable = false, length = 50)
    private String sopCode;

    @Column(name = "sop_name", nullable = false, length = 200)
    private String sopName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // SOP classification
    @Column(name = "sop_type", nullable = false, length = 50)
    private String sopType; // PRODUCTION, WAREHOUSE, QUALITY, FACILITY, SAFETY, MAINTENANCE

    @Column(name = "category", length = 50)
    private String category; // Sub-category

    @Column(name = "target_process", length = 100)
    private String targetProcess; // Target process/operation

    // Template link
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private DocumentTemplateEntity template;

    // Version management
    @Column(name = "version", length = 20)
    @Builder.Default
    private String version = "1.0";

    @Column(name = "revision_date")
    private LocalDate revisionDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    // Approval workflow
    @Column(name = "approval_status", length = 50)
    @Builder.Default
    private String approvalStatus = "DRAFT"; // DRAFT, PENDING, APPROVED, REJECTED, OBSOLETE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private UserEntity approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // Document information
    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "attachments", columnDefinition = "JSONB")
    private String attachments; // JSON array of attachment info

    // Access control
    @Column(name = "required_role", length = 100)
    private String requiredRole; // Required role to execute this SOP

    @Column(name = "restricted")
    @Builder.Default
    private Boolean restricted = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // SOP Steps (One-to-Many)
    @OneToMany(mappedBy = "sop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepNumber ASC")
    @Builder.Default
    private List<SOPStepEntity> steps = new ArrayList<>();

    // SOP Executions (One-to-Many)
    @OneToMany(mappedBy = "sop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SOPExecutionEntity> executions = new ArrayList<>();

    // Helper methods
    public void addStep(SOPStepEntity step) {
        steps.add(step);
        step.setSop(this);
    }

    public void removeStep(SOPStepEntity step) {
        steps.remove(step);
        step.setSop(null);
    }

    /**
     * Approve SOP
     */
    public void approve(UserEntity approver) {
        this.approvalStatus = "APPROVED";
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Reject SOP
     */
    public void reject() {
        this.approvalStatus = "REJECTED";
    }

    /**
     * Mark as obsolete
     */
    public void markObsolete() {
        this.approvalStatus = "OBSOLETE";
        this.isActive = false;
    }

    /**
     * Check if SOP is editable
     */
    public boolean isEditable() {
        return "DRAFT".equals(approvalStatus) || "REJECTED".equals(approvalStatus);
    }

    /**
     * Check if SOP is executable
     */
    public boolean isExecutable() {
        return "APPROVED".equals(approvalStatus) && isActive;
    }
}
