package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Work Progress Entity - 실시간 작업 진행 기록
 * Maps to: mes.SD_Work_Progress
 *
 * 분/시간 단위로 작업 진행 상황을 추적합니다.
 * 각 작업 세션(시작~완료/일시정지)마다 하나의 레코드가 생성됩니다.
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_work_progress",
    schema = "mes",
    indexes = {
        @Index(name = "idx_sd_work_progress_work_order", columnList = "work_order_id"),
        @Index(name = "idx_sd_work_progress_operator", columnList = "operator_user_id"),
        @Index(name = "idx_sd_work_progress_active", columnList = "work_order_id, is_active"),
        @Index(name = "idx_sd_work_progress_operator_date", columnList = "operator_user_id, record_date"),
        @Index(name = "idx_sd_work_progress_tenant", columnList = "tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkProgressEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_work_progress_tenant"))
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_work_progress_work_order"))
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_work_progress_operator"))
    private UserEntity operator;

    // 작업 일자 및 시간
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    // 생산 수량
    @Column(name = "produced_quantity", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal producedQuantity = BigDecimal.ZERO;

    @Column(name = "good_quantity", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal goodQuantity = BigDecimal.ZERO;

    @Column(name = "defect_quantity", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal defectQuantity = BigDecimal.ZERO;

    // 작업 상태: IN_PROGRESS, PAUSED, COMPLETED
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "IN_PROGRESS";

    // 일시정지 통계
    @Column(name = "pause_count")
    @Builder.Default
    private Integer pauseCount = 0;

    @Column(name = "total_pause_duration")
    @Builder.Default
    private Integer totalPauseDuration = 0;  // 총 일시정지 시간 (분)

    // 작업 메모
    @Column(name = "work_notes", columnDefinition = "TEXT")
    private String workNotes;

    // 활성 상태 (현재 진행 중인 작업 세션)
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // 설비 정보 (선택)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", foreignKey = @ForeignKey(name = "fk_sd_work_progress_equipment"))
    private EquipmentEntity equipment;
}
