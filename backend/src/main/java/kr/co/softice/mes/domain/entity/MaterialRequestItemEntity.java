package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Material Request Item Entity (불출 신청 상세)
 * 불출 신청 품목별 상세 정보
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_material_request_items",
    schema = "wms",
    indexes = {
        @Index(name = "idx_material_request_item_header", columnList = "material_request_id"),
        @Index(name = "idx_material_request_item_product", columnList = "product_id"),
        @Index(name = "idx_material_request_item_lot", columnList = "requested_lot_no"),
        @Index(name = "idx_material_request_item_transaction", columnList = "inventory_transaction_id"),
        @Index(name = "idx_material_request_item_status", columnList = "issue_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequestItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_request_item_id")
    private Long materialRequestItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_request_item_header"))
    private MaterialRequestEntity materialRequest;

    // Product Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_request_item_product"))
    private ProductEntity product;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    // Quantity
    @Column(name = "requested_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal requestedQuantity;

    @Column(name = "approved_quantity", precision = 15, scale = 3)
    private BigDecimal approvedQuantity;

    @Column(name = "issued_quantity", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal issuedQuantity = BigDecimal.ZERO;

    @Column(name = "unit", length = 20)
    private String unit;

    // LOT Specification (optional - for specific LOT request)
    @Column(name = "requested_lot_no", length = 100)
    private String requestedLotNo;

    // Issued LOT (actual LOT issued)
    @Column(name = "issued_lot_no", length = 100)
    private String issuedLotNo;

    // Inventory Transaction Link
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_transaction_id", foreignKey = @ForeignKey(name = "fk_material_request_item_transaction"))
    private InventoryTransactionEntity inventoryTransaction;

    // Issue Status
    @Column(name = "issue_status", length = 30)
    @Builder.Default
    private String issueStatus = "PENDING"; // PENDING, PARTIAL, COMPLETED, CANCELLED

    // Notes
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
