package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ProcessRoutingEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Process Routing Repository
 * 공정 라우팅 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ProcessRoutingRepository extends JpaRepository<ProcessRoutingEntity, Long> {

    Optional<ProcessRoutingEntity> findByTenantAndRoutingCodeAndVersion(
        TenantEntity tenant, String routingCode, String version);

    Optional<ProcessRoutingEntity> findByTenant_TenantIdAndRoutingCodeAndVersion(
        String tenantId, String routingCode, String version);

    List<ProcessRoutingEntity> findByTenant_TenantId(String tenantId);

    List<ProcessRoutingEntity> findByTenant_TenantIdAndProduct_ProductId(String tenantId, Long productId);

    List<ProcessRoutingEntity> findByTenant_TenantIdAndIsActive(String tenantId, Boolean isActive);

    boolean existsByTenantAndRoutingCodeAndVersion(
        TenantEntity tenant, String routingCode, String version);

    @Query("SELECT DISTINCT r FROM ProcessRoutingEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.product " +
           "LEFT JOIN FETCH r.steps s " +
           "LEFT JOIN FETCH s.process " +
           "LEFT JOIN FETCH s.equipment " +
           "LEFT JOIN FETCH s.alternateProcess " +
           "WHERE r.tenant.tenantId = :tenantId " +
           "ORDER BY r.routingCode ASC, r.version ASC")
    List<ProcessRoutingEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT r FROM ProcessRoutingEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.product " +
           "LEFT JOIN FETCH r.steps s " +
           "LEFT JOIN FETCH s.process " +
           "LEFT JOIN FETCH s.equipment " +
           "LEFT JOIN FETCH s.alternateProcess " +
           "WHERE r.routingId = :routingId")
    Optional<ProcessRoutingEntity> findByIdWithAllRelations(@Param("routingId") Long routingId);

    @Query("SELECT DISTINCT r FROM ProcessRoutingEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.product " +
           "LEFT JOIN FETCH r.steps s " +
           "LEFT JOIN FETCH s.process " +
           "LEFT JOIN FETCH s.equipment " +
           "LEFT JOIN FETCH s.alternateProcess " +
           "WHERE r.tenant.tenantId = :tenantId AND r.product.productId = :productId " +
           "ORDER BY r.version DESC")
    List<ProcessRoutingEntity> findByTenantIdAndProductIdWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("productId") Long productId
    );

    @Query("SELECT DISTINCT r FROM ProcessRoutingEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.product " +
           "LEFT JOIN FETCH r.steps s " +
           "LEFT JOIN FETCH s.process " +
           "LEFT JOIN FETCH s.equipment " +
           "LEFT JOIN FETCH s.alternateProcess " +
           "WHERE r.tenant.tenantId = :tenantId AND r.isActive = :isActive " +
           "ORDER BY r.routingCode ASC")
    List<ProcessRoutingEntity> findByTenantIdAndIsActiveWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("isActive") Boolean isActive
    );
}
