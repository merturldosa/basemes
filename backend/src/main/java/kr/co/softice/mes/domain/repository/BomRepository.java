package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.BomEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BOM Repository
 * BOM 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface BomRepository extends JpaRepository<BomEntity, Long> {

    Optional<BomEntity> findByTenantAndBomCodeAndVersion(TenantEntity tenant, String bomCode, String version);
    Optional<BomEntity> findByTenant_TenantIdAndBomCodeAndVersion(String tenantId, String bomCode, String version);
    List<BomEntity> findByTenant_TenantId(String tenantId);
    List<BomEntity> findByTenant_TenantIdAndProduct_ProductId(String tenantId, Long productId);
    List<BomEntity> findByTenant_TenantIdAndIsActive(String tenantId, Boolean isActive);
    boolean existsByTenantAndBomCodeAndVersion(TenantEntity tenant, String bomCode, String version);

    @Query("SELECT b FROM BomEntity b " +
           "JOIN FETCH b.tenant " +
           "JOIN FETCH b.product " +
           "LEFT JOIN FETCH b.details d " +
           "LEFT JOIN FETCH d.materialProduct " +
           "LEFT JOIN FETCH d.process " +
           "WHERE b.tenant.tenantId = :tenantId " +
           "ORDER BY b.bomCode ASC")
    List<BomEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT b FROM BomEntity b " +
           "JOIN FETCH b.tenant " +
           "JOIN FETCH b.product " +
           "LEFT JOIN FETCH b.details d " +
           "LEFT JOIN FETCH d.materialProduct " +
           "LEFT JOIN FETCH d.process " +
           "WHERE b.bomId = :bomId")
    Optional<BomEntity> findByIdWithAllRelations(@Param("bomId") Long bomId);

    @Query("SELECT b FROM BomEntity b " +
           "JOIN FETCH b.tenant " +
           "JOIN FETCH b.product " +
           "LEFT JOIN FETCH b.details d " +
           "LEFT JOIN FETCH d.materialProduct " +
           "LEFT JOIN FETCH d.process " +
           "WHERE b.tenant.tenantId = :tenantId AND b.product.productId = :productId " +
           "ORDER BY b.version DESC")
    List<BomEntity> findByTenantIdAndProductIdWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("productId") Long productId
    );

    @Query("SELECT b FROM BomEntity b " +
           "JOIN FETCH b.tenant " +
           "JOIN FETCH b.product " +
           "LEFT JOIN FETCH b.details d " +
           "LEFT JOIN FETCH d.materialProduct " +
           "LEFT JOIN FETCH d.process " +
           "WHERE b.tenant.tenantId = :tenantId AND b.isActive = :isActive " +
           "ORDER BY b.bomCode ASC")
    List<BomEntity> findByTenantIdAndIsActiveWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("isActive") Boolean isActive
    );
}
