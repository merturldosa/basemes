package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Transaction Entity - 재고 이동 내역
 * Maps to: inventory.sd_inventory_transactions
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_inventory_transactions",
    schema = "inventory",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_inv_trans_no",
            columnNames = {"tenant_id", "transaction_no"}
        )
    },
    indexes = {
        @Index(name = "idx_inv_trans_tenant", columnList = "tenant_id"),
        @Index(name = "idx_inv_trans_no", columnList = "transaction_no"),
        @Index(name = "idx_inv_trans_date", columnList = "transaction_date"),
        @Index(name = "idx_inv_trans_type", columnList = "transaction_type"),
        @Index(name = "idx_inv_trans_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_inv_trans_product", columnList = "product_id"),
        @Index(name = "idx_inv_trans_lot", columnList = "lot_id"),
        @Index(name = "idx_inv_trans_approval", columnList = "approval_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inv_trans_tenant"))
    private TenantEntity tenant;

    // Transaction Identification
    @Column(name = "transaction_no", nullable = false, length = 50)
    private String transactionNo;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;  // IN_RECEIVE, IN_PRODUCTION, IN_RETURN, OUT_ISSUE, OUT_SCRAP, MOVE, ADJUST

    // Related Entities
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inv_trans_warehouse"))
    private WarehouseEntity warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inv_trans_product"))
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", foreignKey = @ForeignKey(name = "fk_inv_trans_lot"))
    private LotEntity lot;

    // For MOVE type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_warehouse_id", foreignKey = @ForeignKey(name = "fk_inv_trans_from_warehouse"))
    private WarehouseEntity fromWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_warehouse_id", foreignKey = @ForeignKey(name = "fk_inv_trans_to_warehouse"))
    private WarehouseEntity toWarehouse;

    // Quantities
    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    // References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", foreignKey = @ForeignKey(name = "fk_inv_trans_work_order"))
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_inspection_id", foreignKey = @ForeignKey(name = "fk_inv_trans_quality_inspection"))
    private QualityInspectionEntity qualityInspection;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    // User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inv_trans_user"))
    private UserEntity transactionUser;

    // Approval
    @Column(name = "approval_status", length = 20)
    @Builder.Default
    private String approvalStatus = "PENDING";  // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id", foreignKey = @ForeignKey(name = "fk_inv_trans_approved_by"))
    private UserEntity approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    // Additional Information
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
