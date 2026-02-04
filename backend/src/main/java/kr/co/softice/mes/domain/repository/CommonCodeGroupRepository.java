package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.CommonCodeGroupEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Common Code Group Repository
 * 공통 코드 그룹 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface CommonCodeGroupRepository extends JpaRepository<CommonCodeGroupEntity, Long> {

    /**
     * Find all code groups by tenant ID
     */
    List<CommonCodeGroupEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find all code groups by tenant ID (active only)
     */
    List<CommonCodeGroupEntity> findByTenant_TenantIdAndIsActiveTrue(String tenantId);

    /**
     * Find all code groups by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT DISTINCT cg FROM CommonCodeGroupEntity cg " +
           "LEFT JOIN FETCH cg.tenant " +
           "LEFT JOIN FETCH cg.details " +
           "WHERE cg.tenant.tenantId = :tenantId " +
           "ORDER BY cg.displayOrder, cg.codeGroup")
    List<CommonCodeGroupEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find code group by ID with all relations
     */
    @Query("SELECT cg FROM CommonCodeGroupEntity cg " +
           "LEFT JOIN FETCH cg.tenant " +
           "LEFT JOIN FETCH cg.details " +
           "WHERE cg.codeGroupId = :codeGroupId")
    Optional<CommonCodeGroupEntity> findByIdWithAllRelations(@Param("codeGroupId") Long codeGroupId);

    /**
     * Find code group by tenant and code group name
     */
    Optional<CommonCodeGroupEntity> findByTenant_TenantIdAndCodeGroup(String tenantId, String codeGroup);

    /**
     * Find code group by tenant and code group name with details
     */
    @Query("SELECT cg FROM CommonCodeGroupEntity cg " +
           "LEFT JOIN FETCH cg.tenant " +
           "LEFT JOIN FETCH cg.details cd " +
           "WHERE cg.tenant.tenantId = :tenantId " +
           "AND cg.codeGroup = :codeGroup " +
           "ORDER BY cd.displayOrder")
    Optional<CommonCodeGroupEntity> findByTenantIdAndCodeGroupWithDetails(
        @Param("tenantId") String tenantId,
        @Param("codeGroup") String codeGroup);

    /**
     * Check if code group exists
     */
    boolean existsByTenantAndCodeGroup(TenantEntity tenant, String codeGroup);

    /**
     * Find system code groups
     */
    List<CommonCodeGroupEntity> findByIsSystemTrue();

    /**
     * Count by tenant
     */
    long countByTenant_TenantId(String tenantId);
}
