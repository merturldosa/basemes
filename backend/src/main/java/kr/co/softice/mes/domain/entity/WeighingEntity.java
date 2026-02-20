package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Weighing Entity (칭량 기록)
 * GMP 준수를 위한 칭량 관리
 * 입고/출고/생산/샘플링 시 무게 측정 및 이중 검증
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_weighings",
    schema = "wms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_weighing_no",
            columnNames = {"tenant_id", "weighing_no"}
        )
    },
    indexes = {
        @Index(name = "idx_weighings_tenant", columnList = "tenant_id"),
        @Index(name = "idx_weighings_date", columnList = "weighing_date"),
        @Index(name = "idx_weighings_reference", columnList = "reference_type, reference_id"),
        @Index(name = "idx_weighings_product", columnList = "product_id"),
        @Index(name = "idx_weighings_lot", columnList = "lot_id"),
        @Index(name = "idx_weighings_verification", columnList = "verification_status"),
        @Index(name = "idx_weighings_tolerance", columnList = "tolerance_exceeded"),
        @Index(name = "idx_weighings_operator", columnList = "operator_user_id"),
        @Index(name = "idx_weighings_verifier", columnList = "verifier_user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeighingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weighing_id")
    private Long weighingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_weighing_tenant"))
    private TenantEntity tenant;

    // Weighing Information
    @Column(name = "weighing_no", nullable = false, length = 50)
    private String weighingNo;

    @Column(name = "weighing_date", nullable = false)
    @Builder.Default
    private LocalDateTime weighingDate = LocalDateTime.now();

    @Column(name = "weighing_type", nullable = false, length = 30)
    private String weighingType; // INCOMING, OUTGOING, PRODUCTION, SAMPLING

    // Reference Information (Polymorphic)
    @Column(name = "reference_type", length = 50)
    private String referenceType; // MATERIAL_REQUEST, WORK_ORDER, GOODS_RECEIPT, SHIPPING, QUALITY_INSPECTION

    @Column(name = "reference_id")
    private Long referenceId;

    // Product/Material Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_weighing_product"))
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", foreignKey = @ForeignKey(name = "fk_weighing_lot"))
    private LotEntity lot;

    // Weight Measurements
    @Column(name = "tare_weight", nullable = false, precision = 15, scale = 3)
    private BigDecimal tareWeight;

    @Column(name = "gross_weight", nullable = false, precision = 15, scale = 3)
    private BigDecimal grossWeight;

    @Column(name = "net_weight", nullable = false, precision = 15, scale = 3)
    private BigDecimal netWeight;

    @Column(name = "expected_weight", precision = 15, scale = 3)
    private BigDecimal expectedWeight;

    @Column(name = "variance", precision = 15, scale = 3)
    private BigDecimal variance;

    @Column(name = "variance_percentage", precision = 10, scale = 4)
    private BigDecimal variancePercentage;

    @Column(name = "unit", nullable = false, length = 20)
    @Builder.Default
    private String unit = "kg";

    // Equipment Information
    @Column(name = "scale_id")
    private Long scaleId;

    @Column(name = "scale_name", length = 100)
    private String scaleName;

    // Personnel Information (GMP Dual Verification)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_weighing_operator"))
    private UserEntity operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verifier_user_id", foreignKey = @ForeignKey(name = "fk_weighing_verifier"))
    private UserEntity verifier;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "verification_status", nullable = false, length = 30)
    @Builder.Default
    private String verificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED

    // Tolerance Control
    @Column(name = "tolerance_exceeded", nullable = false)
    @Builder.Default
    private Boolean toleranceExceeded = false;

    @Column(name = "tolerance_percentage", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal tolerancePercentage = new BigDecimal("2.0");

    // Additional Information
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "attachments", columnDefinition = "jsonb")
    private String attachments;

    // Environmental Conditions (GMP)
    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "humidity", precision = 5, scale = 2)
    private BigDecimal humidity;

    /**
     * Calculate net weight (gross - tare)
     * Should be called before saving
     */
    public void calculateNetWeight() {
        if (this.grossWeight != null && this.tareWeight != null) {
            this.netWeight = this.grossWeight.subtract(this.tareWeight);
        }
    }

    /**
     * Calculate variance (net - expected) and variance percentage
     * Should be called after calculateNetWeight()
     */
    public void calculateVariance() {
        if (this.netWeight != null && this.expectedWeight != null) {
            this.variance = this.netWeight.subtract(this.expectedWeight);
            this.variancePercentage = this.variance
                .divide(this.expectedWeight, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
    }

    /**
     * Check if variance exceeds tolerance
     * Should be called after calculateVariance()
     */
    public void checkTolerance() {
        if (this.variancePercentage != null && this.tolerancePercentage != null) {
            BigDecimal absVariancePercentage = this.variancePercentage.abs();
            this.toleranceExceeded = absVariancePercentage.compareTo(this.tolerancePercentage) > 0;
        } else {
            this.toleranceExceeded = false;
        }
    }

    /**
     * Perform all calculations (convenience method)
     */
    public void performCalculations() {
        calculateNetWeight();
        calculateVariance();
        checkTolerance();
    }

    /**
     * Verify weighing (GMP dual verification)
     */
    public void verify(UserEntity verifier, String verificationRemarks) {
        this.verifier = verifier;
        this.verificationDate = LocalDateTime.now();
        this.verificationStatus = "VERIFIED";
        if (verificationRemarks != null && !verificationRemarks.isEmpty()) {
            this.remarks = (this.remarks != null ? this.remarks + "\n" : "") + verificationRemarks;
        }
    }

    /**
     * Reject weighing
     */
    public void reject(UserEntity verifier, String rejectionReason) {
        this.verifier = verifier;
        this.verificationDate = LocalDateTime.now();
        this.verificationStatus = "REJECTED";
        this.remarks = (this.remarks != null ? this.remarks + "\n" : "") + "REJECTED: " + rejectionReason;
    }

    /**
     * Check if weighing is verified
     */
    public boolean isVerified() {
        return "VERIFIED".equals(this.verificationStatus);
    }

    /**
     * Check if weighing is pending verification
     */
    public boolean isPendingVerification() {
        return "PENDING".equals(this.verificationStatus);
    }

    /**
     * Check if weighing is rejected
     */
    public boolean isRejected() {
        return "REJECTED".equals(this.verificationStatus);
    }
}
