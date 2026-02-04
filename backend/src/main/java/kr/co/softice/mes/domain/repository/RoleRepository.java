package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.RoleEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    /**
     * Find by tenant and role code
     */
    Optional<RoleEntity> findByTenantAndRoleCode(TenantEntity tenant, String roleCode);

    /**
     * Find by tenant ID and role code
     */
    Optional<RoleEntity> findByTenant_TenantIdAndRoleCode(String tenantId, String roleCode);

    /**
     * Find by tenant
     */
    List<RoleEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<RoleEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and isActive
     */
    List<RoleEntity> findByTenantAndIsActive(TenantEntity tenant, Boolean isActive);

    /**
     * Check if role code exists for tenant
     */
    boolean existsByTenantAndRoleCode(TenantEntity tenant, String roleCode);

    /**
     * Count roles by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Find all roles by tenant ID with tenant eagerly loaded
     */
    @Query("SELECT r FROM RoleEntity r JOIN FETCH r.tenant WHERE r.tenant.tenantId = :tenantId")
    List<RoleEntity> findByTenantIdWithTenant(@Param("tenantId") String tenantId);

    /**
     * Find active roles by tenant ID with tenant eagerly loaded
     */
    @Query("SELECT r FROM RoleEntity r JOIN FETCH r.tenant WHERE r.tenant.tenantId = :tenantId AND r.isActive = :isActive")
    List<RoleEntity> findByTenantIdAndIsActiveWithTenant(@Param("tenantId") String tenantId, @Param("isActive") Boolean isActive);
}
