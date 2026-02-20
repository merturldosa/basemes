package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import kr.co.softice.mes.domain.entity.BaseEntity;
import lombok.*;

/**
 * Skill Matrix Entity
 * 스킬 매트릭스 마스터 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "core", name = "sd_skill_matrix",
        indexes = {
                @Index(name = "idx_skill_matrix_tenant", columnList = "tenant_id"),
                @Index(name = "idx_skill_matrix_category", columnList = "skill_category"),
                @Index(name = "idx_skill_matrix_active", columnList = "is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_skill_matrix_code", columnNames = {"tenant_id", "skill_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillMatrixEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Long skillId;

    // Tenant relationship (multi-tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "skill_code", length = 50, nullable = false)
    private String skillCode;

    @Column(name = "skill_name", length = 200, nullable = false)
    private String skillName;

    @Column(name = "skill_category", length = 30, nullable = false)
    private String skillCategory; // TECHNICAL, OPERATIONAL, QUALITY, SAFETY, MANAGEMENT

    @Column(name = "skill_level_definition", columnDefinition = "TEXT")
    private String skillLevelDefinition;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "certification_required")
    private Boolean certificationRequired;

    @Column(name = "certification_name", length = 200)
    private String certificationName;

    @Column(name = "validity_period_months")
    private Integer validityPeriodMonths;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
