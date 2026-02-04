package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Defect Entity
 * 불량 관리 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "qms", name = "si_defects",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_defect_no", columnNames = {"tenant_id", "defect_no"})
    },
    indexes = {
        @Index(name = "idx_defect_tenant", columnList = "tenant_id"),
        @Index(name = "idx_defect_date", columnList = "defect_date"),
        @Index(name = "idx_defect_status", columnList = "status"),
        @Index(name = "idx_defect_product", columnList = "product_id"),
        @Index(name = "idx_defect_lot", columnList = "lot_no"),
        @Index(name = "idx_defect_type", columnList = "defect_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "defect_id")
    private Long defectId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "defect_no", nullable = false, length = 50)
    private String defectNo;

    @Column(name = "defect_date", nullable = false)
    private LocalDateTime defectDate;

    // Source (어디서 발생했는지)
    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType; // PRODUCTION, RECEIVING, SHIPPING, INSPECTION, CUSTOMER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_result_id")
    private WorkResultEntity workResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id")
    private GoodsReceiptEntity goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_id")
    private ShippingEntity shipping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_inspection_id")
    private QualityInspectionEntity qualityInspection;

    // Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    // Defect Details
    @Column(name = "defect_type", length = 50)
    private String defectType; // APPEARANCE, DIMENSION, FUNCTION, MATERIAL, ASSEMBLY, OTHER

    @Column(name = "defect_category", length = 50)
    private String defectCategory; // SCRATCH, CRACK, BURR, DEFORMATION, etc

    @Column(name = "defect_location", length = 200)
    private String defectLocation;

    @Column(name = "defect_description", columnDefinition = "TEXT")
    private String defectDescription;

    @Column(name = "defect_quantity", precision = 15, scale = 3)
    private BigDecimal defectQuantity;

    // LOT
    @Column(name = "lot_no", length = 100)
    private String lotNo;

    // Severity
    @Column(name = "severity", length = 30)
    private String severity; // CRITICAL, MAJOR, MINOR

    // Status & Action
    @Column(name = "status", nullable = false, length = 30)
    private String status; // REPORTED, IN_REVIEW, REWORK, SCRAP, CLOSED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_department_id")
    private DepartmentEntity responsibleDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private UserEntity responsibleUser;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "preventive_action", columnDefinition = "TEXT")
    private String preventiveAction;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    // Reporter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id")
    private UserEntity reporterUser;

    @Column(name = "reporter_name", length = 100)
    private String reporterName;

    // Cost
    @Column(name = "defect_cost", precision = 15, scale = 2)
    private BigDecimal defectCost;

    // Additional
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
