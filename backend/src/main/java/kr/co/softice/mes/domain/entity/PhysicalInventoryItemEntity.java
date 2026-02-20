package kr.co.softice.mes.domain.entity;

import lombok.*;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Physical Inventory Item Entity
 * 실사 항목 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_physical_inventory_items",
    schema = "inventory",
    indexes = {
        @Index(name = "idx_physical_inventory_items_header", columnList = "physical_inventory_id"),
        @Index(name = "idx_physical_inventory_items_product", columnList = "product_id"),
        @Index(name = "idx_physical_inventory_items_lot", columnList = "lot_id"),
        @Index(name = "idx_physical_inventory_items_status", columnList = "adjustment_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalInventoryItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "physical_inventory_item_id")
    @Comment("실사 항목 ID")
    private Long physicalInventoryItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physical_inventory_id", nullable = false)
    @Comment("실사")
    private PhysicalInventoryEntity physicalInventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @Comment("제품")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    @Comment("LOT")
    private LotEntity lot;

    @Column(name = "location", length = 50)
    @Comment("위치")
    private String location;

    @Column(name = "system_quantity", precision = 15, scale = 3, nullable = false)
    @Comment("시스템 재고 수량")
    private BigDecimal systemQuantity;

    @Column(name = "counted_quantity", precision = 15, scale = 3)
    @Comment("실사 수량")
    private BigDecimal countedQuantity;

    @Column(name = "difference_quantity", precision = 15, scale = 3)
    @Comment("차이 수량")
    private BigDecimal differenceQuantity;

    @Column(name = "adjustment_status", length = 20, nullable = false)
    @Comment("조정 상태")
    private String adjustmentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjustment_transaction_id")
    @Comment("조정 트랜잭션")
    private InventoryTransactionEntity adjustmentTransaction;

    @Column(name = "counted_by_user_id")
    @Comment("실사자 ID")
    private Long countedByUserId;

    @Column(name = "counted_at")
    @Comment("실사 일시")
    private LocalDateTime countedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    @Comment("비고")
    private String remarks;

    /**
     * 조정 상태 Enum
     */
    public enum AdjustmentStatus {
        NOT_REQUIRED,   // 조정 불필요 (차이 없음)
        PENDING,        // 승인 대기
        APPROVED,       // 승인됨
        REJECTED        // 거부됨
    }

    /**
     * 차이 수량 계산
     */
    public void calculateDifference() {
        if (countedQuantity != null && systemQuantity != null) {
            this.differenceQuantity = countedQuantity.subtract(systemQuantity);
        }
    }

    /**
     * 조정 필요 여부 확인
     */
    public boolean isAdjustmentRequired() {
        return differenceQuantity != null &&
               differenceQuantity.compareTo(BigDecimal.ZERO) != 0;
    }
}
