package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Order Entity
 * 구매 주문 (발주)
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "purchase",
    name = "si_purchase_orders",
    indexes = {
        @Index(name = "idx_purchase_order_tenant", columnList = "tenant_id"),
        @Index(name = "idx_purchase_order_supplier", columnList = "supplier_id"),
        @Index(name = "idx_purchase_order_status", columnList = "status"),
        @Index(name = "idx_purchase_order_date", columnList = "order_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_purchase_order_no", columnNames = {"tenant_id", "order_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    // Supplier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;

    // Delivery
    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    // Payment
    @Column(name = "payment_terms", length = 20)
    private String paymentTerms;

    @Column(name = "currency", length = 10)
    private String currency;

    // Status
    @Column(name = "status", nullable = false, length = 30)
    private String status;  // DRAFT, CONFIRMED, PARTIALLY_RECEIVED, RECEIVED, CANCELLED

    // Totals
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    private UserEntity buyer;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Items
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseOrderItemEntity> items = new ArrayList<>();

    // Helper method
    public void addItem(PurchaseOrderItemEntity item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }

    public void removeItem(PurchaseOrderItemEntity item) {
        items.remove(item);
        item.setPurchaseOrder(null);
    }
}
