package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.CodeGroupEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Code Group Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface CodeGroupRepository extends JpaRepository<CodeGroupEntity, Long> {

    /**
     * Find by tenant and group code
     */
    Optional<CodeGroupEntity> findByTenantAndGroupCode(TenantEntity tenant, String groupCode);

    /**
     * Find by tenant ID and group code
     */
    Optional<CodeGroupEntity> findByTenant_TenantIdAndGroupCode(String tenantId, String groupCode);

    /**
     * Find by tenant
     */
    List<CodeGroupEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<CodeGroupEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and status
     */
    List<CodeGroupEntity> findByTenantAndStatus(TenantEntity tenant, String status);

    /**
     * Check if group code exists for tenant
     */
    boolean existsByTenantAndGroupCode(TenantEntity tenant, String groupCode);
}
