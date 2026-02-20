package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Material Entity
 * 자재 마스터
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "material",
    name = "sd_materials",
    indexes = {
        @Index(name = "idx_material_tenant", columnList = "tenant_id"),
        @Index(name = "idx_material_type", columnList = "material_type"),
        @Index(name = "idx_material_supplier", columnList = "supplier_id"),
        @Index(name = "idx_material_active", columnList = "is_active"),
        @Index(name = "idx_material_name", columnList = "material_name")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_material_code", columnNames = {"tenant_id", "material_code"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long materialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "material_code", nullable = false, length = 50)
    private String materialCode;

    @Column(name = "material_name", nullable = false, length = 200)
    private String materialName;

    @Column(name = "material_type", nullable = false, length = 30)
    private String materialType;  // RAW_MATERIAL, SUB_MATERIAL, SEMI_FINISHED, FINISHED_PRODUCT

    @Column(name = "specification", length = 500)
    private String specification;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    // Pricing
    @Column(name = "standard_price", precision = 15, scale = 2)
    private BigDecimal standardPrice;

    @Column(name = "current_price", precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "currency", length = 10)
    private String currency;

    // Supplier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplier;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    // Stock Management
    @Column(name = "min_stock_quantity", precision = 15, scale = 3)
    private BigDecimal minStockQuantity;

    @Column(name = "max_stock_quantity", precision = 15, scale = 3)
    private BigDecimal maxStockQuantity;

    @Column(name = "safety_stock_quantity", precision = 15, scale = 3)
    private BigDecimal safetyStockQuantity;

    @Column(name = "reorder_point", precision = 15, scale = 3)
    private BigDecimal reorderPoint;

    // Storage
    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    // LOT Management
    @Column(name = "lot_managed")
    private Boolean lotManaged;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    // Status
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
