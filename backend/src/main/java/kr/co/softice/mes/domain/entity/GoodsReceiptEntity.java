package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Goods Receipt Entity (입하)
 * 구매 주문으로부터 물품을 입하받는 프로세스
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "wms", name = "sd_goods_receipts",
        indexes = {
                @Index(name = "idx_goods_receipt_tenant", columnList = "tenant_id"),
                @Index(name = "idx_goods_receipt_date", columnList = "receipt_date"),
                @Index(name = "idx_goods_receipt_status", columnList = "receipt_status"),
                @Index(name = "idx_goods_receipt_po", columnList = "purchase_order_id"),
                @Index(name = "idx_goods_receipt_supplier", columnList = "supplier_id"),
                @Index(name = "idx_goods_receipt_warehouse", columnList = "warehouse_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_goods_receipt_no", columnNames = {"tenant_id", "receipt_no"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goods_receipt_id")
    private Long goodsReceiptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrderEntity purchaseOrder;

    @Column(name = "receipt_no", nullable = false, length = 50)
    private String receiptNo;

    @Column(name = "receipt_date", nullable = false)
    private LocalDateTime receiptDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    @Column(name = "receipt_type", nullable = false, length = 30)
    private String receiptType; // PURCHASE, RETURN, TRANSFER, OTHER

    @Column(name = "receipt_status", nullable = false, length = 30)
    private String receiptStatus; // PENDING, INSPECTING, COMPLETED, REJECTED, CANCELLED

    @Column(name = "total_quantity", precision = 15, scale = 3)
    private BigDecimal totalQuantity;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id")
    private UserEntity receiver;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GoodsReceiptItemEntity> items = new ArrayList<>();

    // Helper methods for bidirectional relationship
    public void addItem(GoodsReceiptItemEntity item) {
        items.add(item);
        item.setGoodsReceipt(this);
    }

    public void removeItem(GoodsReceiptItemEntity item) {
        items.remove(item);
        item.setGoodsReceipt(null);
    }
}
