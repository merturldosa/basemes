package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.DepartmentEntity;
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
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    /**
     * 테넌트와 부서코드로 부서 조회
     */
    Optional<DepartmentEntity> findByTenantAndDepartmentCode(TenantEntity tenant, String departmentCode);

    /**
     * 테넌트별 부서 목록 조회
     */
    List<DepartmentEntity> findByTenantOrderBySortOrderAsc(TenantEntity tenant);

    /**
     * 테넌트별 활성 부서 목록 조회
     */
    List<DepartmentEntity> findByTenantAndIsActiveTrueOrderBySortOrderAsc(TenantEntity tenant);

    /**
     * 테넌트별 부서 페이징 조회
     */
    Page<DepartmentEntity> findByTenant(TenantEntity tenant, Pageable pageable);

    /**
     * 부서명으로 검색 (LIKE)
     */
    @Query("SELECT d FROM DepartmentEntity d WHERE d.tenant = :tenant " +
           "AND (LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.departmentCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<DepartmentEntity> searchByKeyword(@Param("tenant") TenantEntity tenant,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    /**
     * 상위 부서로 하위 부서 조회
     */
    List<DepartmentEntity> findByParentDepartment(DepartmentEntity parentDepartment);

    /**
     * 테넌트별 부서 수 카운트
     */
    long countByTenant(TenantEntity tenant);

    /**
     * 부서코드 중복 체크
     */
    boolean existsByTenantAndDepartmentCode(TenantEntity tenant, String departmentCode);

    /**
     * ID로 부서 조회 (모든 관계 fetch join)
     */
    @Query("SELECT d FROM DepartmentEntity d " +
           "LEFT JOIN FETCH d.tenant " +
           "LEFT JOIN FETCH d.parentDepartment " +
           "WHERE d.departmentId = :id")
    Optional<DepartmentEntity> findByIdWithAllRelations(@Param("id") Long id);
}
