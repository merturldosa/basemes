package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * After Sales Entity
 * A/S 관리 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "qms", name = "sd_after_sales",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_after_sales_no", columnNames = {"tenant_id", "as_no"})
    },
    indexes = {
        @Index(name = "idx_after_sales_tenant", columnList = "tenant_id"),
        @Index(name = "idx_after_sales_date", columnList = "receipt_date"),
        @Index(name = "idx_after_sales_status", columnList = "service_status"),
        @Index(name = "idx_after_sales_customer", columnList = "customer_id"),
        @Index(name = "idx_after_sales_product", columnList = "product_id"),
        @Index(name = "idx_after_sales_priority", columnList = "priority")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfterSalesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "after_sales_id")
    private Long afterSalesId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "as_no", nullable = false, length = 50)
    private String asNo;

    @Column(name = "receipt_date", nullable = false)
    private LocalDateTime receiptDate;

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
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "serial_no", length = 100)
    private String serialNo;

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

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "warranty_status", length = 30)
    private String warrantyStatus; // IN_WARRANTY, OUT_OF_WARRANTY, EXTENDED

    // Issue Details
    @Column(name = "issue_category", length = 50)
    private String issueCategory; // DEFECT, BREAKDOWN, INSTALLATION, USAGE, OTHER

    @Column(name = "issue_description", nullable = false, columnDefinition = "TEXT")
    private String issueDescription;

    @Column(name = "symptom", columnDefinition = "TEXT")
    private String symptom;

    // Service
    @Column(name = "service_type", length = 30)
    private String serviceType; // REPAIR, REPLACEMENT, REFUND, TECHNICAL_SUPPORT

    @Column(name = "service_status", nullable = false, length = 30)
    private String serviceStatus; // RECEIVED, IN_PROGRESS, COMPLETED, CLOSED, CANCELLED

    @Column(name = "priority", length = 30)
    private String priority; // URGENT, HIGH, NORMAL, LOW

    // Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_engineer_id")
    private UserEntity assignedEngineer;

    @Column(name = "assigned_engineer_name", length = 100)
    private String assignedEngineerName;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    // Service Details
    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "service_action", columnDefinition = "TEXT")
    private String serviceAction;

    @Column(name = "parts_replaced", columnDefinition = "TEXT")
    private String partsReplaced;

    @Column(name = "service_start_date")
    private LocalDateTime serviceStartDate;

    @Column(name = "service_end_date")
    private LocalDateTime serviceEndDate;

    // Cost
    @Column(name = "service_cost", precision = 15, scale = 2)
    private BigDecimal serviceCost;

    @Column(name = "parts_cost", precision = 15, scale = 2)
    private BigDecimal partsCost;

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "charge_to_customer", precision = 15, scale = 2)
    private BigDecimal chargeToCustomer;

    // Resolution
    @Column(name = "resolution_description", columnDefinition = "TEXT")
    private String resolutionDescription;

    @Column(name = "customer_satisfaction", length = 30)
    private String customerSatisfaction; // VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED

    // Additional
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
