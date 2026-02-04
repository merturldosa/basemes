package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sales Order Item Entity
 * 판매 주문 상세
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "sales",
    name = "si_sales_order_items",
    indexes = {
        @Index(name = "idx_sales_order_item_order", columnList = "sales_order_id"),
        @Index(name = "idx_sales_order_item_product", columnList = "product_id"),
        @Index(name = "idx_sales_order_item_material", columnList = "material_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sales_order_item_line", columnNames = {"sales_order_id", "line_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_order_item_id")
    private Long salesOrderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrderEntity salesOrder;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    // Product/Material (one of them)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private MaterialEntity material;

    // Quantity
    @Column(name = "ordered_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal orderedQuantity;

    @Column(name = "delivered_quantity", precision = 15, scale = 3)
    private BigDecimal deliveredQuantity;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    // Price
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    // Delivery
    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
