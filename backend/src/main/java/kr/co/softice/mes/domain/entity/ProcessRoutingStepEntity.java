package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Process Routing Step Entity
 * 공정 라우팅 상세(단계) 엔티티
 * 라우팅의 개별 공정 단계를 정의
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "mes",
    name = "sd_process_routing_steps",
    indexes = {
        @Index(name = "idx_step_routing", columnList = "routing_id"),
        @Index(name = "idx_step_process", columnList = "process_id"),
        @Index(name = "idx_step_equipment", columnList = "equipment_id"),
        @Index(name = "idx_step_sequence", columnList = "routing_id,sequence_order")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_step_sequence", columnNames = {"routing_id", "sequence_order"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessRoutingStepEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routing_step_id")
    private Long routingStepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_id", nullable = false)
    private ProcessRoutingEntity routing;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessEntity process;

    // 시간 정보 (분 단위)
    @Column(name = "standard_time", nullable = false)
    private Integer standardTime;

    @Column(name = "setup_time")
    private Integer setupTime;

    @Column(name = "wait_time")
    private Integer waitTime;

    // 리소스 정보
    @Column(name = "required_workers")
    private Integer requiredWorkers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private EquipmentEntity equipment;

    // 공정 흐름 제어
    @Column(name = "is_parallel")
    private Boolean isParallel;

    @Column(name = "parallel_group")
    private Integer parallelGroup;

    @Column(name = "is_optional")
    private Boolean isOptional;

    // 대체 공정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternate_process_id")
    private ProcessEntity alternateProcess;

    // 품질 요구사항
    @Column(name = "quality_check_required")
    private Boolean qualityCheckRequired;

    @Column(name = "quality_standard", columnDefinition = "TEXT")
    private String qualityStandard;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
