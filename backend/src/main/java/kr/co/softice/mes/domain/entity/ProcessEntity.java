package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Process Entity - 공정 마스터
 * Maps to: mes.SI_Processes
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_processes",
    schema = "mes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_si_processes_tenant_code",
            columnNames = {"tenant_id", "process_code"}
        )
    },
    indexes = {
        @Index(name = "idx_si_processes_tenant", columnList = "tenant_id"),
        @Index(name = "idx_si_processes_code", columnList = "process_code"),
        @Index(name = "idx_si_processes_sequence", columnList = "sequence_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "process_id")
    private Long processId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_processes_tenant"))
    private TenantEntity tenant;

    @Column(name = "process_code", nullable = false, length = 50)
    private String processCode;

    @Column(name = "process_name", nullable = false, length = 200)
    private String processName;

    @Column(name = "process_type", length = 50)
    private String processType;  // 제조, 조립, 검사, 포장 등

    @Column(name = "sequence_order", nullable = false)
    @Builder.Default
    private Integer sequenceOrder = 1;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
