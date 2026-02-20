package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Claim Entity
 * 클레임 관리 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "qms", name = "sd_claims",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_claim_no", columnNames = {"tenant_id", "claim_no"})
    },
    indexes = {
        @Index(name = "idx_claim_tenant", columnList = "tenant_id"),
        @Index(name = "idx_claim_date", columnList = "claim_date"),
        @Index(name = "idx_claim_status", columnList = "status"),
        @Index(name = "idx_claim_customer", columnList = "customer_id"),
        @Index(name = "idx_claim_product", columnList = "product_id"),
        @Index(name = "idx_claim_type", columnList = "claim_type"),
        @Index(name = "idx_claim_priority", columnList = "priority")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private Long claimId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "claim_no", nullable = false, length = 50)
    private String claimNo;

    @Column(name = "claim_date", nullable = false)
    private LocalDateTime claimDate;

    // Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    // Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "lot_no", length = 100)
    private String lotNo;

    // Sales Info
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrderEntity salesOrder;

    @Column(name = "sales_order_no", length = 50)
    private String salesOrderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_id")
    private ShippingEntity shipping;

    // Claim Details
    @Column(name = "claim_type", length = 50)
    private String claimType; // QUALITY, DELIVERY, QUANTITY, PACKAGING, DOCUMENTATION, SERVICE, PRICE, OTHER

    @Column(name = "claim_category", length = 50)
    private String claimCategory; // DEFECT, DAMAGE, SHORTAGE, DELAY, MISMATCH, etc

    @Column(name = "claim_description", nullable = false, columnDefinition = "TEXT")
    private String claimDescription;

    @Column(name = "claimed_quantity", precision = 15, scale = 3)
    private BigDecimal claimedQuantity;

    @Column(name = "claimed_amount", precision = 15, scale = 2)
    private BigDecimal claimedAmount;

    // Severity & Priority
    @Column(name = "severity", length = 30)
    private String severity; // CRITICAL, MAJOR, MINOR

    @Column(name = "priority", length = 30)
    private String priority; // URGENT, HIGH, NORMAL, LOW

    // Status
    @Column(name = "status", nullable = false, length = 30)
    private String status; // RECEIVED, INVESTIGATING, IN_PROGRESS, RESOLVED, CLOSED, REJECTED

    // Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_department_id")
    private DepartmentEntity responsibleDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private UserEntity responsibleUser;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    // Investigation
    @Column(name = "investigation_findings", columnDefinition = "TEXT")
    private String investigationFindings;

    @Column(name = "root_cause_analysis", columnDefinition = "TEXT")
    private String rootCauseAnalysis;

    // Resolution
    @Column(name = "resolution_type", length = 50)
    private String resolutionType; // REPLACEMENT, REFUND, DISCOUNT, REWORK, APOLOGY, NO_ACTION

    @Column(name = "resolution_description", columnDefinition = "TEXT")
    private String resolutionDescription;

    @Column(name = "resolution_amount", precision = 15, scale = 2)
    private BigDecimal resolutionAmount;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    // Action Plan
    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "preventive_action", columnDefinition = "TEXT")
    private String preventiveAction;

    @Column(name = "action_completion_date")
    private LocalDateTime actionCompletionDate;

    // Customer Response
    @Column(name = "customer_acceptance", length = 30)
    private String customerAcceptance; // ACCEPTED, PARTIALLY_ACCEPTED, REJECTED, PENDING

    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    // Cost
    @Column(name = "claim_cost", precision = 15, scale = 2)
    private BigDecimal claimCost;

    @Column(name = "compensation_amount", precision = 15, scale = 2)
    private BigDecimal compensationAmount;

    // Additional
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
