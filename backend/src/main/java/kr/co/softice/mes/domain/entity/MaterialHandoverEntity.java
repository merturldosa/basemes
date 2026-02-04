package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Material Handover Entity (자재 인수인계)
 * 창고에서 생산으로 자재 인수인계 기록
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_material_handovers",
    schema = "wms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_material_handover_no",
            columnNames = {"tenant_id", "handover_no"}
        )
    },
    indexes = {
        @Index(name = "idx_material_handover_tenant", columnList = "tenant_id"),
        @Index(name = "idx_material_handover_date", columnList = "handover_date"),
        @Index(name = "idx_material_handover_request", columnList = "material_request_id"),
        @Index(name = "idx_material_handover_transaction", columnList = "inventory_transaction_id"),
        @Index(name = "idx_material_handover_product", columnList = "product_id"),
        @Index(name = "idx_material_handover_lot", columnList = "lot_id"),
        @Index(name = "idx_material_handover_issuer", columnList = "issuer_user_id"),
        @Index(name = "idx_material_handover_receiver", columnList = "receiver_user_id"),
        @Index(name = "idx_material_handover_status", columnList = "handover_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialHandoverEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_handover_id")
    private Long materialHandoverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_handover_tenant"))
    private TenantEntity tenant;

    // Reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_handover_request"))
    private MaterialRequestEntity materialRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_request_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_handover_request_item"))
    private MaterialRequestItemEntity materialRequestItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_transaction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_handover_transaction"))
    private InventoryTransactionEntity inventoryTransaction;

    // Handover Information
    @Column(name = "handover_no", nullable = false, length = 50)
    private String handoverNo;

    @Column(name = "handover_date", nullable = false)
    private LocalDateTime handoverDate;

    // Product and LOT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_handover_product"))
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", foreignKey = @ForeignKey(name = "fk_material_handover_lot"))
    private LotEntity lot;

    @Column(name = "lot_no", length = 100)
    private String lotNo;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    // Issuer (출고자 - 창고 담당)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_handover_issuer"))
    private UserEntity issuer;

    @Column(name = "issuer_name", length = 100)
    private String issuerName;

    @Column(name = "issue_location", length = 200)
    private String issueLocation;

    // Receiver (인수자 - 생산 담당)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_handover_receiver"))
    private UserEntity receiver;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receive_location", length = 200)
    private String receiveLocation;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    // Status
    @Column(name = "handover_status", length = 30)
    @Builder.Default
    private String handoverStatus = "PENDING"; // PENDING, CONFIRMED, REJECTED

    // Confirmation
    @Column(name = "confirmation_remarks", columnDefinition = "TEXT")
    private String confirmationRemarks;

    // Notes
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
