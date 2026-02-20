package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Delivery Item Entity
 * 출하 상세
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "sales",
    name = "sd_delivery_items",
    indexes = {
        @Index(name = "idx_delivery_item_delivery", columnList = "delivery_id"),
        @Index(name = "idx_delivery_item_product", columnList = "product_id"),
        @Index(name = "idx_delivery_item_material", columnList = "material_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_delivery_item_line", columnNames = {"delivery_id", "line_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_item_id")
    private Long deliveryItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private DeliveryEntity delivery;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    // Sales Order Item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_item_id", nullable = false)
    private SalesOrderItemEntity salesOrderItem;

    // Product/Material (one of them)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private MaterialEntity material;

    // Quantity
    @Column(name = "delivered_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal deliveredQuantity;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    // LOT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private LotEntity lot;

    // Location
    @Column(name = "location", length = 100)
    private String location;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
