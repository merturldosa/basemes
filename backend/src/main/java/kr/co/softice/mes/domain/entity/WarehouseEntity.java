package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Warehouse Entity - 창고 마스터
 * Maps to: inventory.si_warehouses
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_warehouses",
    schema = "inventory",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_warehouse_code",
            columnNames = {"tenant_id", "warehouse_code"}
        )
    },
    indexes = {
        @Index(name = "idx_warehouse_tenant", columnList = "tenant_id"),
        @Index(name = "idx_warehouse_code", columnList = "warehouse_code"),
        @Index(name = "idx_warehouse_type", columnList = "warehouse_type"),
        @Index(name = "idx_warehouse_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long warehouseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_warehouse_tenant"))
    private TenantEntity tenant;

    // Identification
    @Column(name = "warehouse_code", nullable = false, length = 50)
    private String warehouseCode;

    @Column(name = "warehouse_name", nullable = false, length = 200)
    private String warehouseName;

    @Column(name = "warehouse_type", nullable = false, length = 20)
    private String warehouseType;  // RAW_MATERIAL, WORK_IN_PROCESS, FINISHED_GOODS, QUARANTINE, SCRAP

    // Location
    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "building", length = 100)
    private String building;

    @Column(name = "floor", length = 50)
    private String floor;

    // Manager
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_user_id", foreignKey = @ForeignKey(name = "fk_warehouse_manager"))
    private UserEntity manager;

    // Capacity
    @Column(name = "total_capacity", precision = 15, scale = 3)
    private BigDecimal totalCapacity;

    @Column(name = "capacity_unit", length = 20)
    private String capacityUnit;

    // Status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Additional Information
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
