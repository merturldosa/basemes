package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shipping Item Entity (출하 상세)
 * 출하 품목별 상세 정보
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "wms", name = "sd_shipping_items",
        indexes = {
                @Index(name = "idx_shipping_item_header", columnList = "shipping_id"),
                @Index(name = "idx_shipping_item_product", columnList = "product_id"),
                @Index(name = "idx_shipping_item_lot", columnList = "lot_no"),
                @Index(name = "idx_shipping_item_inspection", columnList = "quality_inspection_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipping_item_id")
    private Long shippingItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_id", nullable = false)
    private ShippingEntity shipping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_item_id")
    private SalesOrderItemEntity salesOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "ordered_quantity", precision = 15, scale = 3)
    private BigDecimal orderedQuantity;

    @Column(name = "shipped_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal shippedQuantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_amount", precision = 15, scale = 2)
    private BigDecimal lineAmount;

    @Column(name = "lot_no", length = 100)
    private String lotNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "inspection_status", length = 30)
    private String inspectionStatus; // NOT_REQUIRED, PENDING, PASS, FAIL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_inspection_id")
    private QualityInspectionEntity qualityInspection;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
