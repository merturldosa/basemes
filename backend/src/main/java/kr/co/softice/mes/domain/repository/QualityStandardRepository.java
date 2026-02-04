package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.QualityStandardEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Quality Standard Repository
 * 품질 기준 마스터 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface QualityStandardRepository extends JpaRepository<QualityStandardEntity, Long> {

    /**
     * Find by tenant and standard code
     */
    Optional<QualityStandardEntity> findByTenantAndStandardCodeAndStandardVersion(
        TenantEntity tenant, String standardCode, String standardVersion);

    /**
     * Find by tenant ID
     */
    List<QualityStandardEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and is active
     */
    List<QualityStandardEntity> findByTenantAndIsActive(TenantEntity tenant, Boolean isActive);

    /**
     * Find active standards by tenant ID
     */
    List<QualityStandardEntity> findByTenant_TenantIdAndIsActive(String tenantId, Boolean isActive);

    /**
     * Find by product
     */
    List<QualityStandardEntity> findByProduct(ProductEntity product);

    /**
     * Find by tenant and product
     */
    List<QualityStandardEntity> findByTenantAndProduct(TenantEntity tenant, ProductEntity product);

    /**
     * Find by tenant and inspection type
     */
    List<QualityStandardEntity> findByTenantAndInspectionType(TenantEntity tenant, String inspectionType);

    /**
     * Check if standard code exists for tenant
     */
    boolean existsByTenantAndStandardCodeAndStandardVersion(
        TenantEntity tenant, String standardCode, String standardVersion);

    /**
     * Count standards by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Find all quality standards by tenant ID with relationships eagerly loaded
     */
    @Query("SELECT qs FROM QualityStandardEntity qs " +
           "JOIN FETCH qs.tenant " +
           "JOIN FETCH qs.product " +
           "WHERE qs.tenant.tenantId = :tenantId")
    List<QualityStandardEntity> findByTenantIdWithRelations(@Param("tenantId") String tenantId);

    /**
     * Find active quality standards by tenant ID with relationships eagerly loaded
     */
    @Query("SELECT qs FROM QualityStandardEntity qs " +
           "JOIN FETCH qs.tenant " +
           "JOIN FETCH qs.product " +
           "WHERE qs.tenant.tenantId = :tenantId AND qs.isActive = :isActive")
    List<QualityStandardEntity> findByTenantIdAndIsActiveWithRelations(
        @Param("tenantId") String tenantId, @Param("isActive") Boolean isActive);

    /**
     * Find by ID with relationships eagerly loaded
     */
    @Query("SELECT qs FROM QualityStandardEntity qs " +
           "JOIN FETCH qs.tenant " +
           "JOIN FETCH qs.product " +
           "WHERE qs.qualityStandardId = :id")
    Optional<QualityStandardEntity> findByIdWithRelations(@Param("id") Long id);

    /**
     * Find by product ID with relationships eagerly loaded
     */
    @Query("SELECT qs FROM QualityStandardEntity qs " +
           "JOIN FETCH qs.tenant " +
           "JOIN FETCH qs.product " +
           "WHERE qs.product.productId = :productId")
    List<QualityStandardEntity> findByProductIdWithRelations(@Param("productId") Long productId);

    /**
     * Find by inspection type with relationships eagerly loaded
     */
    @Query("SELECT qs FROM QualityStandardEntity qs " +
           "JOIN FETCH qs.tenant " +
           "JOIN FETCH qs.product " +
           "WHERE qs.tenant.tenantId = :tenantId AND qs.inspectionType = :inspectionType")
    List<QualityStandardEntity> findByTenantIdAndInspectionTypeWithRelations(
        @Param("tenantId") String tenantId, @Param("inspectionType") String inspectionType);
}
