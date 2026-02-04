package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Work Order Entity - 작업 지시서
 * Maps to: mes.SI_Work_Orders
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_work_orders",
    schema = "mes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_si_work_orders_tenant_no",
            columnNames = {"tenant_id", "work_order_no"}
        )
    },
    indexes = {
        @Index(name = "idx_si_work_orders_tenant", columnList = "tenant_id"),
        @Index(name = "idx_si_work_orders_no", columnList = "work_order_no"),
        @Index(name = "idx_si_work_orders_status", columnList = "status"),
        @Index(name = "idx_si_work_orders_product", columnList = "product_id"),
        @Index(name = "idx_si_work_orders_process", columnList = "process_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_order_id")
    private Long workOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_work_orders_tenant"))
    private TenantEntity tenant;

    @Column(name = "work_order_no", nullable = false, length = 50)
    private String workOrderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_work_orders_product"))
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_work_orders_process"))
    private ProcessEntity process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_id", foreignKey = @ForeignKey(name = "fk_work_order_routing"))
    private ProcessRoutingEntity routing;

    // 생산 계획
    @Column(name = "planned_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal plannedQuantity;

    @Column(name = "planned_start_date", nullable = false)
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date", nullable = false)
    private LocalDateTime plannedEndDate;

    // 실적 집계
    @Column(name = "actual_quantity", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal actualQuantity = BigDecimal.ZERO;

    @Column(name = "good_quantity", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal goodQuantity = BigDecimal.ZERO;

    @Column(name = "defect_quantity", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal defectQuantity = BigDecimal.ZERO;

    // 작업 상태: PENDING, READY, IN_PROGRESS, COMPLETED, CANCELLED
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 5;  // 1(높음) ~ 10(낮음)

    // 실제 작업 일정
    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    // 담당자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id", foreignKey = @ForeignKey(name = "fk_si_work_orders_assigned_user"))
    private UserEntity assignedUser;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
