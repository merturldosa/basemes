package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Quality Standard Entity - 품질 기준 마스터
 * Maps to: qms.si_quality_standards
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_quality_standards",
    schema = "qms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_quality_standard_code",
            columnNames = {"tenant_id", "standard_code", "standard_version"}
        )
    },
    indexes = {
        @Index(name = "idx_quality_standard_tenant", columnList = "tenant_id"),
        @Index(name = "idx_quality_standard_product", columnList = "product_id"),
        @Index(name = "idx_quality_standard_code", columnList = "standard_code"),
        @Index(name = "idx_quality_standard_type", columnList = "inspection_type"),
        @Index(name = "idx_quality_standard_active", columnList = "is_active"),
        @Index(name = "idx_quality_standard_effective", columnList = "effective_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityStandardEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_standard_id")
    private Long qualityStandardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quality_standard_tenant"))
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quality_standard_product"))
    private ProductEntity product;

    // Identification
    @Column(name = "standard_code", nullable = false, length = 50)
    private String standardCode;

    @Column(name = "standard_name", nullable = false, length = 200)
    private String standardName;

    @Column(name = "standard_version", nullable = false, length = 20)
    @Builder.Default
    private String standardVersion = "1.0";

    // Inspection Configuration
    @Column(name = "inspection_type", nullable = false, length = 20)
    private String inspectionType;  // INCOMING, IN_PROCESS, OUTGOING, FINAL

    @Column(name = "inspection_method", length = 100)
    private String inspectionMethod;

    // Quality Criteria
    @Column(name = "min_value", precision = 15, scale = 3)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 15, scale = 3)
    private BigDecimal maxValue;

    @Column(name = "target_value", precision = 15, scale = 3)
    private BigDecimal targetValue;

    @Column(name = "tolerance_value", precision = 15, scale = 3)
    private BigDecimal toleranceValue;

    @Column(name = "unit", length = 20)
    private String unit;

    // Measurement
    @Column(name = "measurement_item", length = 200)
    private String measurementItem;

    @Column(name = "measurement_equipment", length = 100)
    private String measurementEquipment;

    // Sampling
    @Column(name = "sampling_method", length = 100)
    private String samplingMethod;

    @Column(name = "sample_size")
    private Integer sampleSize;

    // Status and Validity
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // Additional Information
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
