package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Material Request Entity (불출 신청)
 * 생산 부서에서 자재 불출 요청
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_material_requests",
    schema = "wms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_material_request_no",
            columnNames = {"tenant_id", "request_no"}
        )
    },
    indexes = {
        @Index(name = "idx_material_request_tenant", columnList = "tenant_id"),
        @Index(name = "idx_material_request_date", columnList = "request_date"),
        @Index(name = "idx_material_request_status", columnList = "request_status"),
        @Index(name = "idx_material_request_work_order", columnList = "work_order_id"),
        @Index(name = "idx_material_request_requester", columnList = "requester_user_id"),
        @Index(name = "idx_material_request_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_material_request_required_date", columnList = "required_date"),
        @Index(name = "idx_material_request_priority", columnList = "priority")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequestEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_request_id")
    private Long materialRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_request_tenant"))
    private TenantEntity tenant;

    // Identification
    @Column(name = "request_no", nullable = false, length = 50)
    private String requestNo;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    // Work Order Reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", foreignKey = @ForeignKey(name = "fk_material_request_work_order"))
    private WorkOrderEntity workOrder;

    @Column(name = "work_order_no", length = 50)
    private String workOrderNo;

    // Requester Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_request_requester"))
    private UserEntity requester;

    @Column(name = "requester_name", length = 100)
    private String requesterName;

    @Column(name = "requester_department", length = 100)
    private String requesterDepartment;

    // Warehouse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_material_request_warehouse"))
    private WarehouseEntity warehouse;

    // Status
    @Column(name = "request_status", nullable = false, length = 30)
    @Builder.Default
    private String requestStatus = "PENDING"; // PENDING, APPROVED, REJECTED, ISSUED, COMPLETED, CANCELLED

    // Approval
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id", foreignKey = @ForeignKey(name = "fk_material_request_approver"))
    private UserEntity approver;

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approval_remarks", columnDefinition = "TEXT")
    private String approvalRemarks;

    // Issue Date
    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    // Completion Date
    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    // Rejection Reason
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Cancellation Reason
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Required Date
    @Column(name = "required_date")
    private LocalDate requiredDate;

    // Priority
    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "NORMAL"; // URGENT, HIGH, NORMAL, LOW

    // Purpose
    @Column(name = "purpose", length = 100)
    private String purpose; // PRODUCTION, MAINTENANCE, SAMPLE, OTHER

    // Notes
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Active Flag
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Items
    @OneToMany(mappedBy = "materialRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaterialRequestItemEntity> items = new ArrayList<>();

    // Helper method to add item
    public void addItem(MaterialRequestItemEntity item) {
        items.add(item);
        item.setMaterialRequest(this);
    }

    // Helper method to remove item
    public void removeItem(MaterialRequestItemEntity item) {
        items.remove(item);
        item.setMaterialRequest(null);
    }
}
