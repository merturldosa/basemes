package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository
 * 제품 마스터 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * Find by tenant and product code
     */
    Optional<ProductEntity> findByTenantAndProductCode(TenantEntity tenant, String productCode);

    /**
     * Find by tenant ID and product code
     */
    Optional<ProductEntity> findByTenant_TenantIdAndProductCode(String tenantId, String productCode);

    /**
     * Find by tenant
     */
    List<ProductEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<ProductEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and product type
     */
    List<ProductEntity> findByTenantAndProductType(TenantEntity tenant, String productType);

    /**
     * Find by tenant and is active
     */
    List<ProductEntity> findByTenantAndIsActive(TenantEntity tenant, Boolean isActive);

    /**
     * Find active products by tenant ID
     */
    List<ProductEntity> findByTenant_TenantIdAndIsActive(String tenantId, Boolean isActive);

    /**
     * Check if product code exists for tenant
     */
    boolean existsByTenantAndProductCode(TenantEntity tenant, String productCode);

    /**
     * Count products by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Find all products by tenant ID with tenant eagerly loaded
     */
    @Query("SELECT p FROM ProductEntity p JOIN FETCH p.tenant WHERE p.tenant.tenantId = :tenantId")
    List<ProductEntity> findByTenantIdWithTenant(@Param("tenantId") String tenantId);

    /**
     * Find active products by tenant ID with tenant eagerly loaded
     */
    @Query("SELECT p FROM ProductEntity p JOIN FETCH p.tenant WHERE p.tenant.tenantId = :tenantId AND p.isActive = :isActive")
    List<ProductEntity> findByTenantIdAndIsActiveWithTenant(@Param("tenantId") String tenantId, @Param("isActive") Boolean isActive);
}
