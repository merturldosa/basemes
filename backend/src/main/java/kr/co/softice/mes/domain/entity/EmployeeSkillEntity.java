package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import kr.co.softice.mes.domain.entity.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employee Skill Entity
 * 사원 스킬/자격 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "core", name = "si_employee_skills",
        indexes = {
                @Index(name = "idx_employee_skill_tenant", columnList = "tenant_id"),
                @Index(name = "idx_employee_skill_employee", columnList = "employee_id"),
                @Index(name = "idx_employee_skill_skill", columnList = "skill_id"),
                @Index(name = "idx_employee_skill_level", columnList = "skill_level"),
                @Index(name = "idx_employee_skill_expiry", columnList = "expiry_date"),
                @Index(name = "idx_employee_skill_active", columnList = "is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_skill", columnNames = {"tenant_id", "employee_id", "skill_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkillEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_skill_id")
    private Long employeeSkillId;

    // Tenant relationship (multi-tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Employee relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    // Skill relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private SkillMatrixEntity skill;

    // Skill Level
    @Column(name = "skill_level", length = 30)
    private String skillLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT, MASTER

    @Column(name = "skill_level_numeric")
    private Integer skillLevelNumeric; // 1~5

    // Dates
    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "last_assessment_date")
    private LocalDate lastAssessmentDate;

    @Column(name = "next_assessment_date")
    private LocalDate nextAssessmentDate;

    // Certification Info
    @Column(name = "certification_no", length = 100)
    private String certificationNo;

    @Column(name = "issuing_authority", length = 200)
    private String issuingAuthority;

    // Assessment
    @Column(name = "assessor_name", length = 100)
    private String assessorName;

    @Column(name = "assessment_score", precision = 5, scale = 2)
    private BigDecimal assessmentScore;

    @Column(name = "assessment_result", length = 30)
    private String assessmentResult; // PASS, FAIL, CONDITIONAL

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
