package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Purchase Order Item Entity
 * 구매 주문 상세
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "purchase",
    name = "si_purchase_order_items",
    indexes = {
        @Index(name = "idx_purchase_order_item_order", columnList = "purchase_order_id"),
        @Index(name = "idx_purchase_order_item_material", columnList = "material_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_purchase_order_item_line", columnNames = {"purchase_order_id", "line_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_item_id")
    private Long purchaseOrderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrderEntity purchaseOrder;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    // Material
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private MaterialEntity material;

    // Quantity
    @Column(name = "ordered_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal orderedQuantity;

    @Column(name = "received_quantity", precision = 15, scale = 3)
    private BigDecimal receivedQuantity;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    // Price
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    // Delivery
    @Column(name = "required_date")
    private LocalDateTime requiredDate;

    // Reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequestEntity purchaseRequest;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
