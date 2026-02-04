package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Disposal Item Entity (폐기 항목)
 * 폐기별 제품/수량 상세
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_disposal_items",
    schema = "wms",
    indexes = {
        @Index(name = "idx_disposal_item_disposal", columnList = "disposal_id"),
        @Index(name = "idx_disposal_item_product", columnList = "product_id"),
        @Index(name = "idx_disposal_item_lot", columnList = "lot_id"),
        @Index(name = "idx_disposal_item_lot_no", columnList = "lot_no"),
        @Index(name = "idx_disposal_item_defect_type", columnList = "defect_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisposalItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disposal_item_id")
    private Long disposalItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disposal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disposal_item_disposal"))
    private DisposalEntity disposal;

    // Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disposal_item_product"))
    private ProductEntity product;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    // LOT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", foreignKey = @ForeignKey(name = "fk_disposal_item_lot"))
    private LotEntity lot;

    @Column(name = "lot_no", length = 100)
    private String lotNo;

    // Warehouse Location
    @Column(name = "warehouse_zone", length = 50)
    private String warehouseZone;

    @Column(name = "warehouse_rack", length = 50)
    private String warehouseRack;

    @Column(name = "warehouse_shelf", length = 50)
    private String warehouseShelf;

    @Column(name = "warehouse_bin", length = 50)
    private String warehouseBin;

    // Quantity
    @Column(name = "disposal_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal disposalQuantity; // 폐기 신청 수량

    @Column(name = "processed_quantity", precision = 15, scale = 3)
    private BigDecimal processedQuantity; // 실제 폐기 처리 수량

    // Inventory Transaction Reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disposal_transaction_id", foreignKey = @ForeignKey(name = "fk_disposal_item_transaction"))
    private InventoryTransactionEntity disposalTransaction; // 폐기 트랜잭션 (OUT_DISPOSAL)

    // Quality Issue
    @Column(name = "defect_type", length = 100)
    private String defectType; // 불량 유형

    @Column(name = "defect_description", columnDefinition = "TEXT")
    private String defectDescription; // 불량 설명

    @Column(name = "expiry_date")
    private LocalDate expiryDate; // 유효기간 (만료품인 경우)

    // Notes
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
