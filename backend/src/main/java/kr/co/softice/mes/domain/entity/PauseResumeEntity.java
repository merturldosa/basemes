package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Pause/Resume Entity - 작업 일시정지/재개 이력
 * Maps to: mes.SD_Pause_Resume_History
 *
 * 작업 중 일시정지와 재개 이벤트를 추적합니다.
 * 휴식, 설비 점검, 자재 대기 등 다양한 일시정지 사유를 기록합니다.
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_pause_resume_history",
    schema = "mes",
    indexes = {
        @Index(name = "idx_sd_pause_resume_progress", columnList = "progress_id"),
        @Index(name = "idx_sd_pause_resume_active", columnList = "progress_id, resume_time"),
        @Index(name = "idx_sd_pause_resume_tenant", columnList = "tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PauseResumeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pause_resume_id")
    private Long pauseResumeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_pause_resume_tenant"))
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_pause_resume_progress"))
    private WorkProgressEntity workProgress;

    // 일시정지 시간
    @Column(name = "pause_time", nullable = false)
    private LocalDateTime pauseTime;

    // 재개 시간 (NULL이면 아직 재개되지 않음)
    @Column(name = "resume_time")
    private LocalDateTime resumeTime;

    // 일시정지 사유
    @Column(name = "pause_reason", length = 500)
    private String pauseReason;

    // 일시정지 타입: BREAK(휴식), EQUIPMENT_CHECK(설비점검), MATERIAL_WAIT(자재대기), OTHER(기타)
    @Column(name = "pause_type", length = 50)
    private String pauseType;

    // 일시정지 시간 (분) - 재개 시 자동 계산
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    // 승인 정보 (일부 일시정지는 승인 필요)
    @Column(name = "requires_approval")
    @Builder.Default
    private Boolean requiresApproval = false;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;
}
