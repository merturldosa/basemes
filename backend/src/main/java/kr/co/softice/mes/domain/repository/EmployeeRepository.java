package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.DepartmentEntity;
import kr.co.softice.mes.domain.entity.EmployeeEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    /**
     * 테넌트와 사원코드로 사원 조회
     */
    Optional<EmployeeEntity> findByTenantAndEmployeeNo(TenantEntity tenant, String employeeNo);

    /**
     * 테넌트별 사원 목록 조회
     */
    List<EmployeeEntity> findByTenant(TenantEntity tenant);

    /**
     * 테넌트별 활성 사원 목록 조회
     */
    List<EmployeeEntity> findByTenantAndIsActiveTrueOrderByEmployeeNameAsc(TenantEntity tenant);

    /**
     * 테넌트별 사원 페이징 조회
     */
    Page<EmployeeEntity> findByTenant(TenantEntity tenant, Pageable pageable);

    /**
     * 부서별 사원 조회
     */
    List<EmployeeEntity> findByDepartment(DepartmentEntity department);

    /**
     * 부서별 활성 사원 조회
     */
    List<EmployeeEntity> findByDepartmentAndIsActiveTrue(DepartmentEntity department);

    /**
     * 사원명/사원코드로 검색 (LIKE)
     */
    @Query("SELECT e FROM EmployeeEntity e WHERE e.tenant = :tenant " +
           "AND (LOWER(e.employeeName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.employeeNo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<EmployeeEntity> searchByKeyword(@Param("tenant") TenantEntity tenant,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    /**
     * 재직 상태별 사원 조회
     */
    List<EmployeeEntity> findByTenantAndEmploymentStatus(TenantEntity tenant, String employmentStatus);

    /**
     * 테넌트별 사원 수 카운트
     */
    long countByTenant(TenantEntity tenant);

    /**
     * 부서별 사원 수 카운트
     */
    long countByDepartment(DepartmentEntity department);

    /**
     * 사원코드 중복 체크
     */
    boolean existsByTenantAndEmployeeNo(TenantEntity tenant, String employeeNo);

    /**
     * 이메일 중복 체크
     */
    boolean existsByTenantAndEmail(TenantEntity tenant, String email);

    /**
     * ID로 사원 조회 (모든 관계 fetch join)
     */
    @Query("SELECT e FROM EmployeeEntity e " +
           "LEFT JOIN FETCH e.tenant " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.user " +
           "WHERE e.employeeId = :id")
    Optional<EmployeeEntity> findByIdWithAllRelations(@Param("id") Long id);
}
