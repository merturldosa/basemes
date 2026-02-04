package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ProcessEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Process Repository
 * 공정 마스터 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ProcessRepository extends JpaRepository<ProcessEntity, Long> {

    /**
     * Find by tenant and process code
     */
    Optional<ProcessEntity> findByTenantAndProcessCode(TenantEntity tenant, String processCode);

    /**
     * Find by tenant ID and process code
     */
    Optional<ProcessEntity> findByTenant_TenantIdAndProcessCode(String tenantId, String processCode);

    /**
     * Find by tenant
     */
    List<ProcessEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<ProcessEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant ordered by sequence
     */
    List<ProcessEntity> findByTenantOrderBySequenceOrder(TenantEntity tenant);

    /**
     * Find by tenant ID ordered by sequence
     */
    List<ProcessEntity> findByTenant_TenantIdOrderBySequenceOrder(String tenantId);

    /**
     * Find by tenant and process type
     */
    List<ProcessEntity> findByTenantAndProcessType(TenantEntity tenant, String processType);

    /**
     * Find by tenant and is active
     */
    List<ProcessEntity> findByTenantAndIsActive(TenantEntity tenant, Boolean isActive);

    /**
     * Check if process code exists for tenant
     */
    boolean existsByTenantAndProcessCode(TenantEntity tenant, String processCode);

    /**
     * Count processes by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Find all processes by tenant ID with tenant eagerly loaded, ordered by sequence
     */
    @Query("SELECT p FROM ProcessEntity p JOIN FETCH p.tenant WHERE p.tenant.tenantId = :tenantId ORDER BY p.sequenceOrder")
    List<ProcessEntity> findByTenantIdWithTenantOrderBySequence(@Param("tenantId") String tenantId);

    /**
     * Find active processes by tenant ID with tenant eagerly loaded
     */
    @Query("SELECT p FROM ProcessEntity p JOIN FETCH p.tenant WHERE p.tenant.tenantId = :tenantId AND p.isActive = :isActive")
    List<ProcessEntity> findByTenantIdAndIsActiveWithTenant(@Param("tenantId") String tenantId, @Param("isActive") Boolean isActive);
}
