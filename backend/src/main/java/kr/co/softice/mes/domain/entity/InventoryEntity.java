package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Entity - 재고 현황
 * Maps to: inventory.si_inventory
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_inventory",
    schema = "inventory",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_inventory_product_warehouse_lot",
            columnNames = {"tenant_id", "warehouse_id", "product_id", "lot_id"}
        )
    },
    indexes = {
        @Index(name = "idx_inventory_tenant", columnList = "tenant_id"),
        @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_lot", columnList = "lot_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_tenant"))
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_warehouse"))
    private WarehouseEntity warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_product"))
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", foreignKey = @ForeignKey(name = "fk_inventory_lot"))
    private LotEntity lot;

    // Quantities
    @Column(name = "available_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal availableQuantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "unit", length = 20)
    private String unit;

    // Location in Warehouse
    @Column(name = "zone", length = 50)
    private String zone;

    @Column(name = "rack", length = 50)
    private String rack;

    @Column(name = "shelf", length = 50)
    private String shelf;

    @Column(name = "bin", length = 50)
    private String bin;

    // Last Movement
    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Column(name = "last_transaction_type", length = 20)
    private String lastTransactionType;

    // Additional Information
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
