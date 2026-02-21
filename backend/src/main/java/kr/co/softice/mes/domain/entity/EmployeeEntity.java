package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사원 엔티티
 * 화면설계서 MES-TD4-008 사원 관리
 */
@Entity
@Table(name = "sd_employees", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "employee_code"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    /**
     * 테넌트 (다중 회사 지원)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    /**
     * 사원 번호 (예: 2014070701)
     */
    @Column(name = "employee_code", nullable = false, length = 20)
    private String employeeNo;

    /**
     * 사원명 (한글)
     */
    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    /**
     * 부서
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    /**
     * 사업장
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private SiteEntity site;

    /**
     * 직위 (예: 사장, 대리, 과장, 부장)
     */
    @Column(name = "position", length = 50)
    private String position;

    /**
     * 직급 (예: 주임, 대리, 과장)
     */
    @Column(name = "job_grade", length = 50)
    private String jobGrade;

    /**
     * 입사일
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /**
     * 전화번호 (예: 010-000-0000)
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * 이메일 (예: 0000@NAVER.COM)
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 연관된 사용자 (로그인 계정)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     * 생년월일
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * 성별 (M: 남성, F: 여성)
     */
    @Column(name = "gender", length = 1)
    private String gender;

    /**
     * 주소
     */
    @Column(name = "address", length = 200)
    private String address;

    /**
     * 상세 주소
     */
    @Column(name = "address_detail", length = 200)
    private String addressDetail;

    /**
     * 우편번호
     */
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    /**
     * 비상 연락처
     */
    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;

    /**
     * 비상 연락처 관계 (예: 배우자, 부모)
     */
    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    /**
     * 퇴사일
     */
    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    /**
     * 재직 상태 (ACTIVE: 재직, RESIGNED: 퇴사, LEAVE: 휴직)
     */
    @Column(name = "employment_status", length = 20)
    @Builder.Default
    private String employmentStatus = "ACTIVE";

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
     * 호환성을 위한 alias 메서드들
     * 기존 코드에서 사용하는 메서드명을 위함
     */
    public Long getId() {
        return this.employeeId;
    }

    public String getEmployeeCode() {
        return this.employeeNo;
    }

    public String getFullName() {
        return this.employeeName;
    }
}
