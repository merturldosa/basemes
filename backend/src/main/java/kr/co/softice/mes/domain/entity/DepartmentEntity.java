package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 부서 엔티티
 * 화면설계서 MES-TD4-007 부서 관리
 */
@Entity
@Table(name = "SI_Departments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "department_code"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 테넌트 (다중 회사 지원)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    /**
     * 부서 코드 (예: D001, D002)
     */
    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    /**
     * 부서명 (예: 영업, 생산, QC, QA, IT)
     */
    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    /**
     * 상위 부서 (부서 계층 구조 지원)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private DepartmentEntity parentDepartment;

    /**
     * 부서 설명
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 정렬 순서
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 사용 여부
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 생성 일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 생성자
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 수정자
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 호환성을 위한 alias 메서드
     * 기존 코드에서 getId()를 사용하는 경우를 위함
     */
    public Long getId() {
        return this.departmentId;
    }
}
