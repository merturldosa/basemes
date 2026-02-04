package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shipping Entity (출하)
 * 판매 주문으로부터 물품을 출하하는 프로세스
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "wms", name = "si_shippings",
        indexes = {
                @Index(name = "idx_shipping_tenant", columnList = "tenant_id"),
                @Index(name = "idx_shipping_date", columnList = "shipping_date"),
                @Index(name = "idx_shipping_status", columnList = "shipping_status"),
                @Index(name = "idx_shipping_so", columnList = "sales_order_id"),
                @Index(name = "idx_shipping_customer", columnList = "customer_id"),
                @Index(name = "idx_shipping_warehouse", columnList = "warehouse_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shipping_no", columnNames = {"tenant_id", "shipping_no"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipping_id")
    private Long shippingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrderEntity salesOrder;

    @Column(name = "shipping_no", nullable = false, length = 50)
    private String shippingNo;

    @Column(name = "shipping_date", nullable = false)
    private LocalDateTime shippingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    @Column(name = "shipping_type", nullable = false, length = 30)
    private String shippingType; // SALES, RETURN, TRANSFER, OTHER

    @Column(name = "shipping_status", nullable = false, length = 30)
    private String shippingStatus; // PENDING, INSPECTING, SHIPPED, CANCELLED

    @Column(name = "total_quantity", precision = 15, scale = 3)
    private BigDecimal totalQuantity;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_user_id")
    private UserEntity shipper;

    @Column(name = "shipper_name", length = 100)
    private String shipperName;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "shipping", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShippingItemEntity> items = new ArrayList<>();

    // Helper methods for bidirectional relationship
    public void addItem(ShippingItemEntity item) {
        items.add(item);
        item.setShipping(this);
    }

    public void removeItem(ShippingItemEntity item) {
        items.remove(item);
        item.setShipping(null);
    }
}
