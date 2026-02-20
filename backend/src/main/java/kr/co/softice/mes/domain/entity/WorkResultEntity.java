package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Work Result Entity - 작업 실적
 * Maps to: mes.SD_Work_Results
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_work_results",
    schema = "mes",
    indexes = {
        @Index(name = "idx_sd_work_results_work_order", columnList = "work_order_id"),
        @Index(name = "idx_sd_work_results_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_work_results_date", columnList = "result_date"),
        @Index(name = "idx_sd_work_results_worker", columnList = "worker_user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkResultEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_result_id")
    private Long workResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_work_results_work_order"))
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_work_results_tenant"))
    private TenantEntity tenant;

    // 실적 정보
    @Column(name = "result_date", nullable = false)
    @Builder.Default
    private LocalDateTime resultDate = LocalDateTime.now();

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "good_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal goodQuantity = BigDecimal.ZERO;

    @Column(name = "defect_quantity", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal defectQuantity = BigDecimal.ZERO;

    // 작업 시간
    @Column(name = "work_start_time", nullable = false)
    private LocalDateTime workStartTime;

    @Column(name = "work_end_time", nullable = false)
    private LocalDateTime workEndTime;

    @Column(name = "work_duration")
    private Integer workDuration;  // 작업 시간 (분)

    // 작업자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_work_results_worker"))
    private UserEntity worker;

    @Column(name = "worker_name", length = 100)
    private String workerName;

    // 추가 정보
    @Column(name = "defect_reason", columnDefinition = "TEXT")
    private String defectReason;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
