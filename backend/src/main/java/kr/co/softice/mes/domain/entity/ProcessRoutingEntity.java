package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Process Routing Entity
 * 공정 라우팅 마스터 엔티티
 * 제품별 공정 순서 및 작업 표준을 정의
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "mes",
    name = "sd_process_routings",
    indexes = {
        @Index(name = "idx_routing_tenant", columnList = "tenant_id"),
        @Index(name = "idx_routing_product", columnList = "product_id"),
        @Index(name = "idx_routing_code", columnList = "routing_code"),
        @Index(name = "idx_routing_effective_date", columnList = "effective_date"),
        @Index(name = "idx_routing_active", columnList = "is_active"),
        @Index(name = "idx_routing_version", columnList = "version")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_routing_code_version", columnNames = {"tenant_id", "routing_code", "version"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessRoutingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routing_id")
    private Long routingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "routing_code", nullable = false, length = 50)
    private String routingCode;

    @Column(name = "routing_name", nullable = false, length = 200)
    private String routingName;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "total_standard_time")
    private Integer totalStandardTime;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "routing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    @Builder.Default
    private List<ProcessRoutingStepEntity> steps = new ArrayList<>();

    // Helper methods for managing bidirectional relationship
    public void addStep(ProcessRoutingStepEntity step) {
        steps.add(step);
        step.setRouting(this);
    }

    public void removeStep(ProcessRoutingStepEntity step) {
        steps.remove(step);
        step.setRouting(null);
    }

    public void clearSteps() {
        steps.forEach(step -> step.setRouting(null));
        steps.clear();
    }
}
