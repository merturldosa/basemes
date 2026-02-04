package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Return Entity (반품)
 * 생산/창고에서 반품 처리
 *
 * 반품 유형:
 * - DEFECTIVE: 불량품 반품
 * - EXCESS: 과잉 반품 (필요 수량 초과)
 * - WRONG_DELIVERY: 오배송 반품
 * - OTHER: 기타
 *
 * 워크플로우:
 * PENDING → APPROVED → RECEIVED → INSPECTING → COMPLETED
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_returns",
    schema = "wms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_return_no",
            columnNames = {"tenant_id", "return_no"}
        )
    },
    indexes = {
        @Index(name = "idx_return_tenant", columnList = "tenant_id"),
        @Index(name = "idx_return_date", columnList = "return_date"),
        @Index(name = "idx_return_material_request", columnList = "material_request_id"),
        @Index(name = "idx_return_work_order", columnList = "work_order_id"),
        @Index(name = "idx_return_requester", columnList = "requester_user_id"),
        @Index(name = "idx_return_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_return_status", columnList = "return_status"),
        @Index(name = "idx_return_type", columnList = "return_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Long returnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_tenant"))
    private TenantEntity tenant;

    // Return Information
    @Column(name = "return_no", nullable = false, length = 50)
    private String returnNo;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "return_type", nullable = false, length = 30)
    private String returnType; // DEFECTIVE, EXCESS, WRONG_DELIVERY, OTHER

    // References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_request_id", foreignKey = @ForeignKey(name = "fk_return_material_request"))
    private MaterialRequestEntity materialRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", foreignKey = @ForeignKey(name = "fk_return_work_order"))
    private WorkOrderEntity workOrder;

    // Requester
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_requester"))
    private UserEntity requester;

    @Column(name = "requester_name", length = 100)
    private String requesterName;

    // Warehouse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_warehouse"))
    private WarehouseEntity warehouse;

    // Status
    @Column(name = "return_status", length = 30)
    @Builder.Default
    private String returnStatus = "PENDING"; // PENDING, APPROVED, REJECTED, RECEIVED, INSPECTING, COMPLETED, CANCELLED

    // Approval
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id", foreignKey = @ForeignKey(name = "fk_return_approver"))
    private UserEntity approver;

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    // Dates
    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    // Totals
    @Column(name = "total_return_quantity", precision = 15, scale = 3)
    private BigDecimal totalReturnQuantity;

    @Column(name = "total_received_quantity", precision = 15, scale = 3)
    private BigDecimal totalReceivedQuantity;

    @Column(name = "total_passed_quantity", precision = 15, scale = 3)
    private BigDecimal totalPassedQuantity;

    @Column(name = "total_failed_quantity", precision = 15, scale = 3)
    private BigDecimal totalFailedQuantity;

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
    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReturnItemEntity> items = new ArrayList<>();

    // Helper method to add item
    public void addItem(ReturnItemEntity item) {
        items.add(item);
        item.setReturnEntity(this);
    }

    // Helper method to calculate totals
    public void calculateTotals() {
        this.totalReturnQuantity = items.stream()
                .map(ReturnItemEntity::getReturnQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalReceivedQuantity = items.stream()
                .map(item -> item.getReceivedQuantity() != null ? item.getReceivedQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalPassedQuantity = items.stream()
                .map(item -> item.getPassedQuantity() != null ? item.getPassedQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalFailedQuantity = items.stream()
                .map(item -> item.getFailedQuantity() != null ? item.getFailedQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
