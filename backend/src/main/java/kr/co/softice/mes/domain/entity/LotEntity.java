package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lot Entity - LOT/배치 관리
 * Maps to: inventory.si_lots
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_lots",
    schema = "inventory",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_lot_no",
            columnNames = {"tenant_id", "lot_no"}
        )
    },
    indexes = {
        @Index(name = "idx_lot_tenant", columnList = "tenant_id"),
        @Index(name = "idx_lot_product", columnList = "product_id"),
        @Index(name = "idx_lot_no", columnList = "lot_no"),
        @Index(name = "idx_lot_quality_status", columnList = "quality_status"),
        @Index(name = "idx_lot_active", columnList = "is_active"),
        @Index(name = "idx_lot_expiry_date", columnList = "expiry_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lot_id")
    private Long lotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lot_tenant"))
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lot_product"))
    private ProductEntity product;

    // LOT Identification
    @Column(name = "lot_no", nullable = false, length = 100)
    private String lotNo;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    // Manufacturing Information
    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // Quantities
    @Column(name = "initial_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal initialQuantity = BigDecimal.ZERO;

    @Column(name = "current_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "unit", length = 20)
    private String unit;

    // Supplier Information
    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "supplier_lot_no", length = 100)
    private String supplierLotNo;

    // Quality Status
    @Column(name = "quality_status", nullable = false, length = 20)
    @Builder.Default
    private String qualityStatus = "PENDING";  // PENDING, PASSED, FAILED, QUARANTINE

    // Reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", foreignKey = @ForeignKey(name = "fk_lot_work_order"))
    private WorkOrderEntity workOrder;

    // Status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Additional Information
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
