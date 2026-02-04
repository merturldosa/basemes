package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Delivery Entity
 * 출하
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "sales",
    name = "si_deliveries",
    indexes = {
        @Index(name = "idx_delivery_tenant", columnList = "tenant_id"),
        @Index(name = "idx_delivery_order", columnList = "sales_order_id"),
        @Index(name = "idx_delivery_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_delivery_status", columnList = "status"),
        @Index(name = "idx_delivery_date", columnList = "delivery_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_delivery_no", columnNames = {"tenant_id", "delivery_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long deliveryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "delivery_no", nullable = false, length = 50)
    private String deliveryNo;

    @Column(name = "delivery_date", nullable = false)
    private LocalDateTime deliveryDate;

    // Sales Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrderEntity salesOrder;

    // Warehouse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    // Quality Check
    @Column(name = "quality_check_status", length = 20)
    private String qualityCheckStatus;  // PENDING, INSPECTING, PASSED, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_user_id")
    private UserEntity inspector;

    @Column(name = "inspection_date")
    private LocalDateTime inspectionDate;

    // Shipment
    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    @Column(name = "tracking_no", length = 100)
    private String trackingNo;

    @Column(name = "carrier", length = 100)
    private String carrier;

    // Status
    @Column(name = "status", nullable = false, length = 20)
    private String status;  // PENDING, COMPLETED

    // User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_user_id", nullable = false)
    private UserEntity shipper;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Items
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryItemEntity> items = new ArrayList<>();

    // Helper method
    public void addItem(DeliveryItemEntity item) {
        items.add(item);
        item.setDelivery(this);
    }

    public void removeItem(DeliveryItemEntity item) {
        items.remove(item);
        item.setDelivery(null);
    }
}
