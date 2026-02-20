package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import kr.co.softice.mes.domain.entity.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mold Production History Entity
 * 금형 생산 이력 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_mold_production_history",
        indexes = {
                @Index(name = "idx_mold_history_tenant", columnList = "tenant_id"),
                @Index(name = "idx_mold_history_mold", columnList = "mold_id"),
                @Index(name = "idx_mold_history_date", columnList = "production_date"),
                @Index(name = "idx_mold_history_work_order", columnList = "work_order_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoldProductionHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    // Tenant relationship (multi-tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Mold relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mold_id", nullable = false)
    private MoldEntity mold;

    // Production reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_result_id")
    private WorkResultEntity workResult;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    // Shot tracking
    @Column(name = "shot_count", nullable = false)
    private Integer shotCount;

    @Column(name = "cumulative_shot_count")
    private Long cumulativeShotCount; // Auto-calculated by database trigger

    // Production quantities
    @Column(name = "production_quantity", precision = 15, scale = 3)
    private BigDecimal productionQuantity;

    @Column(name = "good_quantity", precision = 15, scale = 3)
    private BigDecimal goodQuantity;

    @Column(name = "defect_quantity", precision = 15, scale = 3)
    private BigDecimal defectQuantity;

    // Operator
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_user_id")
    private UserEntity operatorUser;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    // Common fields
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
