package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.CommonCodeDetailEntity;
import kr.co.softice.mes.domain.entity.CommonCodeGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Common Code Detail Repository
 * 공통 코드 상세 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface CommonCodeDetailRepository extends JpaRepository<CommonCodeDetailEntity, Long> {

    /**
     * Find all code details by code group
     */
    List<CommonCodeDetailEntity> findByCodeGroup_CodeGroupId(Long codeGroupId);

    /**
     * Find all code details by code group (active only)
     */
    List<CommonCodeDetailEntity> findByCodeGroup_CodeGroupIdAndIsActiveTrueOrderByDisplayOrder(Long codeGroupId);

    /**
     * Find code details by code group with all relations
     */
    @Query("SELECT cd FROM CommonCodeDetailEntity cd " +
           "LEFT JOIN FETCH cd.codeGroup cg " +
           "LEFT JOIN FETCH cg.tenant " +
           "WHERE cd.codeGroup.codeGroupId = :codeGroupId " +
           "ORDER BY cd.displayOrder")
    List<CommonCodeDetailEntity> findByCodeGroupIdWithAllRelations(@Param("codeGroupId") Long codeGroupId);

    /**
     * Find code detail by code group and code
     */
    Optional<CommonCodeDetailEntity> findByCodeGroup_CodeGroupIdAndCode(Long codeGroupId, String code);

    /**
     * Find code details by tenant and code group name
     */
    @Query("SELECT cd FROM CommonCodeDetailEntity cd " +
           "WHERE cd.codeGroup.tenant.tenantId = :tenantId " +
           "AND cd.codeGroup.codeGroup = :codeGroup " +
           "AND cd.isActive = true " +
           "ORDER BY cd.displayOrder")
    List<CommonCodeDetailEntity> findByTenantIdAndCodeGroup(
        @Param("tenantId") String tenantId,
        @Param("codeGroup") String codeGroup);

    /**
     * Find code detail by tenant, code group name, and code
     */
    @Query("SELECT cd FROM CommonCodeDetailEntity cd " +
           "WHERE cd.codeGroup.tenant.tenantId = :tenantId " +
           "AND cd.codeGroup.codeGroup = :codeGroup " +
           "AND cd.code = :code")
    Optional<CommonCodeDetailEntity> findByTenantIdAndCodeGroupAndCode(
        @Param("tenantId") String tenantId,
        @Param("codeGroup") String codeGroup,
        @Param("code") String code);

    /**
     * Check if code exists in code group
     */
    boolean existsByCodeGroupAndCode(CommonCodeGroupEntity codeGroup, String code);

    /**
     * Find default code in code group
     */
    Optional<CommonCodeDetailEntity> findByCodeGroup_CodeGroupIdAndIsDefaultTrue(Long codeGroupId);

    /**
     * Count by code group
     */
    long countByCodeGroup_CodeGroupId(Long codeGroupId);

    /**
     * Find active codes by code group ordered by display order
     */
    @Query("SELECT cd FROM CommonCodeDetailEntity cd " +
           "WHERE cd.codeGroup.codeGroupId = :codeGroupId " +
           "AND cd.isActive = true " +
           "ORDER BY cd.displayOrder, cd.codeName")
    List<CommonCodeDetailEntity> findActiveCodesByCodeGroupIdOrdered(@Param("codeGroupId") Long codeGroupId);
}
