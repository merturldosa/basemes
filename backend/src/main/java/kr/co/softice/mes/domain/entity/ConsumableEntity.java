package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Consumable Entity
 * 소모품 마스터 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "si_consumables",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_consumable_code", columnNames = {"tenant_id", "consumable_code"})
    },
    indexes = {
        @Index(name = "idx_consumable_tenant", columnList = "tenant_id"),
        @Index(name = "idx_consumable_equipment", columnList = "equipment_id"),
        @Index(name = "idx_consumable_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumableEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumable_id")
    private Long consumableId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "consumable_code", nullable = false, length = 50)
    private String consumableCode;

    @Column(name = "consumable_name", nullable = false, length = 200)
    private String consumableName;

    @Column(name = "category", length = 50)
    private String category;

    // Equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private EquipmentEntity equipment;

    @Column(name = "unit", length = 20)
    private String unit;

    // Stock
    @Column(name = "current_stock", precision = 15, scale = 3)
    private BigDecimal currentStock;

    @Column(name = "minimum_stock", precision = 15, scale = 3)
    private BigDecimal minimumStock;

    @Column(name = "maximum_stock", precision = 15, scale = 3)
    private BigDecimal maximumStock;

    // Pricing & Supplier
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "supplier", length = 200)
    private String supplier;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "last_replenished_date")
    private LocalDate lastReplenishedDate;

    // Status
    @Column(name = "status", nullable = false, length = 30)
    private String status; // ACTIVE, DISCONTINUED

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
