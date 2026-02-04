package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Site Repository
 * 사업장 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Long> {

    /**
     * Find all sites by tenant with all relations
     */
    @Query("SELECT s FROM SiteEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "ORDER BY s.siteCode ASC")
    List<SiteEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find site by ID with all relations
     */
    @Query("SELECT s FROM SiteEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.siteId = :siteId")
    Optional<SiteEntity> findByIdWithAllRelations(@Param("siteId") Long siteId);

    /**
     * Find active sites by tenant
     */
    @Query("SELECT s FROM SiteEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.isActive = true " +
           "ORDER BY s.siteCode ASC")
    List<SiteEntity> findActiveSitesByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find sites by type
     */
    @Query("SELECT s FROM SiteEntity s " +
           "JOIN FETCH s.tenant " +
           "WHERE s.tenant.tenantId = :tenantId " +
           "AND s.siteType = :siteType " +
           "ORDER BY s.siteCode ASC")
    List<SiteEntity> findByTenantIdAndSiteType(@Param("tenantId") String tenantId,
                                                @Param("siteType") String siteType);

    /**
     * Check if site code exists
     */
    boolean existsByTenant_TenantIdAndSiteCode(String tenantId, String siteCode);

    /**
     * Find by tenant and site code
     */
    Optional<SiteEntity> findByTenant_TenantIdAndSiteCode(String tenantId, String siteCode);
}
