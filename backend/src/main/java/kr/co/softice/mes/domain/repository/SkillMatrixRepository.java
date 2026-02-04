package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SkillMatrixEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Skill Matrix Repository
 * 스킬 매트릭스 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface SkillMatrixRepository extends JpaRepository<SkillMatrixEntity, Long> {

    /**
     * Find all skills by tenant with all relations
     */
    @Query("SELECT s FROM SkillMatrixEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "ORDER BY s.skillCode")
    List<SkillMatrixEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find skill by ID with all relations
     */
    @Query("SELECT s FROM SkillMatrixEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.skillId = :skillId")
    Optional<SkillMatrixEntity> findByIdWithAllRelations(@Param("skillId") Long skillId);

    /**
     * Find active skills by tenant
     */
    @Query("SELECT s FROM SkillMatrixEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.isActive = true " +
           "ORDER BY s.skillCode")
    List<SkillMatrixEntity> findActiveSkillsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find skills by category
     */
    @Query("SELECT s FROM SkillMatrixEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.skillCategory = :skillCategory " +
           "ORDER BY s.skillCode")
    List<SkillMatrixEntity> findByTenantIdAndSkillCategory(@Param("tenantId") String tenantId,
                                                             @Param("skillCategory") String skillCategory);

    /**
     * Find skills requiring certification
     */
    @Query("SELECT s FROM SkillMatrixEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.certificationRequired = true " +
           "AND s.isActive = true " +
           "ORDER BY s.skillCode")
    List<SkillMatrixEntity> findSkillsRequiringCertification(@Param("tenantId") String tenantId);

    /**
     * Check if skill code exists
     */
    boolean existsByTenant_TenantIdAndSkillCode(String tenantId, String skillCode);

    /**
     * Find by tenant and skill code
     */
    Optional<SkillMatrixEntity> findByTenant_TenantIdAndSkillCode(String tenantId, String skillCode);
}
