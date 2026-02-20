package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales Order Entity
 * 판매 주문
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "sales",
    name = "sd_sales_orders",
    indexes = {
        @Index(name = "idx_sales_order_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sales_order_customer", columnList = "customer_id"),
        @Index(name = "idx_sales_order_status", columnList = "status"),
        @Index(name = "idx_sales_order_date", columnList = "order_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sales_order_no", columnNames = {"tenant_id", "order_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_order_id")
    private Long salesOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    // Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    // Delivery
    @Column(name = "requested_delivery_date")
    private LocalDateTime requestedDeliveryDate;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    // Payment
    @Column(name = "payment_terms", length = 20)
    private String paymentTerms;

    @Column(name = "currency", length = 10)
    private String currency;

    // Status
    @Column(name = "status", nullable = false, length = 30)
    private String status;  // DRAFT, CONFIRMED, PARTIALLY_DELIVERED, DELIVERED, CANCELLED

    // Totals
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_user_id", nullable = false)
    private UserEntity salesUser;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Items
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderItemEntity> items = new ArrayList<>();

    // Helper method
    public void addItem(SalesOrderItemEntity item) {
        items.add(item);
        item.setSalesOrder(this);
    }

    public void removeItem(SalesOrderItemEntity item) {
        items.remove(item);
        item.setSalesOrder(null);
    }
}
