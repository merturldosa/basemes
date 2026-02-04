package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Quality Inspection Entity - 품질 검사 기록
 * Maps to: qms.si_quality_inspections
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_quality_inspections",
    schema = "qms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_quality_inspection_no",
            columnNames = {"tenant_id", "inspection_no"}
        )
    },
    indexes = {
        @Index(name = "idx_quality_inspection_tenant", columnList = "tenant_id"),
        @Index(name = "idx_quality_inspection_standard", columnList = "quality_standard_id"),
        @Index(name = "idx_quality_inspection_work_order", columnList = "work_order_id"),
        @Index(name = "idx_quality_inspection_work_result", columnList = "work_result_id"),
        @Index(name = "idx_quality_inspection_product", columnList = "product_id"),
        @Index(name = "idx_quality_inspection_inspector", columnList = "inspector_user_id"),
        @Index(name = "idx_quality_inspection_no", columnList = "inspection_no"),
        @Index(name = "idx_quality_inspection_date", columnList = "inspection_date"),
        @Index(name = "idx_quality_inspection_type", columnList = "inspection_type"),
        @Index(name = "idx_quality_inspection_result", columnList = "inspection_result")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityInspectionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_inspection_id")
    private Long qualityInspectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quality_inspection_tenant"))
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_standard_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quality_inspection_standard"))
    private QualityStandardEntity qualityStandard;

    // Optional References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", foreignKey = @ForeignKey(name = "fk_quality_inspection_work_order"))
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_result_id", foreignKey = @ForeignKey(name = "fk_quality_inspection_work_result"))
    private WorkResultEntity workResult;

    // Product Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quality_inspection_product"))
    private ProductEntity product;

    // Inspection Identification
    @Column(name = "inspection_no", nullable = false, length = 50)
    private String inspectionNo;

    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate;

    @Column(name = "inspection_type", nullable = false, length = 20)
    private String inspectionType;  // INCOMING, IN_PROCESS, OUTGOING, FINAL

    // Inspector
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quality_inspection_inspector"))
    private UserEntity inspector;

    // Quantities
    @Column(name = "inspected_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal inspectedQuantity = BigDecimal.ZERO;

    @Column(name = "passed_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal passedQuantity = BigDecimal.ZERO;

    @Column(name = "failed_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal failedQuantity = BigDecimal.ZERO;

    // Measurement
    @Column(name = "measured_value", precision = 15, scale = 3)
    private BigDecimal measuredValue;

    @Column(name = "measurement_unit", length = 20)
    private String measurementUnit;

    // Result
    @Column(name = "inspection_result", nullable = false, length = 20)
    private String inspectionResult;  // PASS, FAIL, CONDITIONAL

    // Defect Information
    @Column(name = "defect_type", length = 100)
    private String defectType;

    @Column(name = "defect_reason", columnDefinition = "TEXT")
    private String defectReason;

    @Column(name = "defect_location", length = 200)
    private String defectLocation;

    // Corrective Action
    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "corrective_action_date")
    private LocalDate correctiveActionDate;

    // Additional Information
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
