package kr.co.softice.mes.domain.entity;

import lombok.*;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Physical Inventory Entity
 * 실사 계획 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_physical_inventories",
    schema = "inventory",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_physical_inventory_no", columnNames = {"tenant_id", "inventory_no"})
    },
    indexes = {
        @Index(name = "idx_physical_inventories_tenant_warehouse", columnList = "tenant_id, warehouse_id"),
        @Index(name = "idx_physical_inventories_status", columnList = "inventory_status"),
        @Index(name = "idx_physical_inventories_date", columnList = "inventory_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalInventoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "physical_inventory_id")
    @Comment("실사 ID")
    private Long physicalInventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", nullable = false)
    @Comment("테넌트")
    private TenantEntity tenant;

    @Column(name = "inventory_no", length = 50, nullable = false)
    @Comment("실사 번호 (PI-YYYYMMDD-0001)")
    private String inventoryNo;

    @Column(name = "inventory_date", nullable = false)
    @Comment("실사 일자")
    private LocalDateTime inventoryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @Comment("창고")
    private WarehouseEntity warehouse;

    @Column(name = "inventory_status", length = 20, nullable = false)
    @Comment("실사 상태 (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)")
    private String inventoryStatus;

    @Column(name = "planned_by_user_id")
    @Comment("계획자 ID")
    private Long plannedByUserId;

    @Column(name = "approved_by_user_id")
    @Comment("승인자 ID")
    private Long approvedByUserId;

    @Column(name = "approval_date")
    @Comment("승인 일자")
    private LocalDateTime approvalDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    @Comment("비고")
    private String remarks;

    @OneToMany(mappedBy = "physicalInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PhysicalInventoryItemEntity> items = new ArrayList<>();

    /**
     * 실사 상태 Enum
     */
    public enum InventoryStatus {
        PLANNED,        // 계획됨
        IN_PROGRESS,    // 진행 중
        COMPLETED,      // 완료
        CANCELLED       // 취소
    }

    /**
     * 실사 항목 추가
     */
    public void addItem(PhysicalInventoryItemEntity item) {
        items.add(item);
        item.setPhysicalInventory(this);
    }

    /**
     * 실사 항목 제거
     */
    public void removeItem(PhysicalInventoryItemEntity item) {
        items.remove(item);
        item.setPhysicalInventory(null);
    }
}
