package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Return Item Entity (반품 항목)
 * 반품별 제품/수량 상세
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_return_items",
    schema = "wms",
    indexes = {
        @Index(name = "idx_return_item_return", columnList = "return_id"),
        @Index(name = "idx_return_item_product", columnList = "product_id"),
        @Index(name = "idx_return_item_inspection_status", columnList = "inspection_status"),
        @Index(name = "idx_return_item_quality_inspection", columnList = "quality_inspection_id"),
        @Index(name = "idx_return_item_original_lot", columnList = "original_lot_no")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_item_id")
    private Long returnItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_item_return"))
    private ReturnEntity returnEntity;

    // Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_item_product"))
    private ProductEntity product;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    // LOT
    @Column(name = "original_lot_no", length = 100)
    private String originalLotNo; // 원래 불출된 LOT

    @Column(name = "new_lot_no", length = 100)
    private String newLotNo; // 재입고 시 생성된 새 LOT

    // Quantities
    @Column(name = "return_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal returnQuantity; // 반품 신청 수량

    @Column(name = "received_quantity", precision = 15, scale = 3)
    private BigDecimal receivedQuantity; // 실제 입고 수량

    @Column(name = "passed_quantity", precision = 15, scale = 3)
    private BigDecimal passedQuantity; // 합격 수량 (재입고)

    @Column(name = "failed_quantity", precision = 15, scale = 3)
    private BigDecimal failedQuantity; // 불합격 수량 (격리)

    // Quality Inspection
    @Column(name = "inspection_status", length = 30)
    @Builder.Default
    private String inspectionStatus = "NOT_REQUIRED"; // NOT_REQUIRED, PENDING, PASS, FAIL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_inspection_id", foreignKey = @ForeignKey(name = "fk_return_item_quality_inspection"))
    private QualityInspectionEntity qualityInspection;

    // Inventory Transaction References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receive_transaction_id", foreignKey = @ForeignKey(name = "fk_return_item_receive_transaction"))
    private InventoryTransactionEntity receiveTransaction; // 입고 트랜잭션 (IN_RETURN)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pass_transaction_id", foreignKey = @ForeignKey(name = "fk_return_item_pass_transaction"))
    private InventoryTransactionEntity passTransaction; // 합격품 재입고 트랜잭션

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fail_transaction_id", foreignKey = @ForeignKey(name = "fk_return_item_fail_transaction"))
    private InventoryTransactionEntity failTransaction; // 불합격품 격리 트랜잭션

    // Return Reason
    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason; // 반품 사유

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
