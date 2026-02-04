package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Disposal Entity (폐기)
 * 불량품/만료품 폐기 처리
 *
 * 폐기 유형:
 * - DEFECTIVE: 불량품 폐기
 * - EXPIRED: 만료품 폐기
 * - DAMAGED: 파손품 폐기
 * - OBSOLETE: 노후품 폐기
 * - OTHER: 기타
 *
 * 워크플로우:
 * PENDING → APPROVED → PROCESSED → COMPLETED
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_disposals",
    schema = "wms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_disposal_no",
            columnNames = {"tenant_id", "disposal_no"}
        )
    },
    indexes = {
        @Index(name = "idx_disposal_tenant", columnList = "tenant_id"),
        @Index(name = "idx_disposal_date", columnList = "disposal_date"),
        @Index(name = "idx_disposal_work_order", columnList = "work_order_id"),
        @Index(name = "idx_disposal_requester", columnList = "requester_user_id"),
        @Index(name = "idx_disposal_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_disposal_status", columnList = "disposal_status"),
        @Index(name = "idx_disposal_type", columnList = "disposal_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisposalEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disposal_id")
    private Long disposalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disposal_tenant"))
    private TenantEntity tenant;

    // Disposal Information
    @Column(name = "disposal_no", nullable = false, length = 50)
    private String disposalNo;

    @Column(name = "disposal_date", nullable = false)
    private LocalDateTime disposalDate;

    @Column(name = "disposal_type", nullable = false, length = 30)
    private String disposalType; // DEFECTIVE, EXPIRED, DAMAGED, OBSOLETE, OTHER

    // References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", foreignKey = @ForeignKey(name = "fk_disposal_work_order"))
    private WorkOrderEntity workOrder;

    // Requester
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disposal_requester"))
    private UserEntity requester;

    @Column(name = "requester_name", length = 100)
    private String requesterName;

    // Warehouse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disposal_warehouse"))
    private WarehouseEntity warehouse;

    // Status
    @Column(name = "disposal_status", length = 30)
    @Builder.Default
    private String disposalStatus = "PENDING"; // PENDING, APPROVED, REJECTED, PROCESSED, COMPLETED, CANCELLED

    // Approval
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id", foreignKey = @ForeignKey(name = "fk_disposal_approver"))
    private UserEntity approver;

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    // Processor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_user_id", foreignKey = @ForeignKey(name = "fk_disposal_processor"))
    private UserEntity processor;

    @Column(name = "processor_name", length = 100)
    private String processorName;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    // Completion
    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "disposal_method", length = 100)
    private String disposalMethod; // 소각, 매립, 위탁 처리, 재활용 등

    @Column(name = "disposal_location", length = 200)
    private String disposalLocation;

    // Totals
    @Column(name = "total_disposal_quantity", precision = 15, scale = 3)
    private BigDecimal totalDisposalQuantity;

    // Notes
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Common
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Items (OneToMany)
    @OneToMany(mappedBy = "disposal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DisposalItemEntity> items = new ArrayList<>();

    // Helper method to add item
    public void addItem(DisposalItemEntity item) {
        items.add(item);
        item.setDisposal(this);
    }

    // Helper method to calculate totals
    public void calculateTotals() {
        this.totalDisposalQuantity = items.stream()
                .map(DisposalItemEntity::getDisposalQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
