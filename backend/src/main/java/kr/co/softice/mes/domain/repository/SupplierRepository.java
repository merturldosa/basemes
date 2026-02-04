package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SupplierEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Supplier Repository
 * 공급업체 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {

    Optional<SupplierEntity> findByTenantAndSupplierCode(TenantEntity tenant, String supplierCode);
    Optional<SupplierEntity> findByTenant_TenantIdAndSupplierCode(String tenantId, String supplierCode);
    List<SupplierEntity> findByTenant_TenantId(String tenantId);
    List<SupplierEntity> findByTenant_TenantIdAndSupplierType(String tenantId, String supplierType);
    List<SupplierEntity> findByTenant_TenantIdAndIsActive(String tenantId, Boolean isActive);
    List<SupplierEntity> findByTenant_TenantIdAndRating(String tenantId, String rating);
    boolean existsByTenantAndSupplierCode(TenantEntity tenant, String supplierCode);

    @Query("SELECT s FROM SupplierEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "ORDER BY s.supplierCode ASC")
    List<SupplierEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT s FROM SupplierEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.supplierId = :supplierId")
    Optional<SupplierEntity> findByIdWithAllRelations(@Param("supplierId") Long supplierId);

    @Query("SELECT s FROM SupplierEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId AND s.isActive = :isActive " +
           "ORDER BY s.supplierCode ASC")
    List<SupplierEntity> findByTenantIdAndIsActiveWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("isActive") Boolean isActive
    );

    @Query("SELECT s FROM SupplierEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId AND s.supplierType = :supplierType " +
           "ORDER BY s.supplierCode ASC")
    List<SupplierEntity> findByTenantIdAndSupplierTypeWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("supplierType") String supplierType
    );

    @Query("SELECT s FROM SupplierEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId AND s.rating = :rating " +
           "ORDER BY s.supplierCode ASC")
    List<SupplierEntity> findByTenantIdAndRatingWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("rating") String rating
    );
}
