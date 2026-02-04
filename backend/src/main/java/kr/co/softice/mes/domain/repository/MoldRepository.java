package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.MoldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Mold Repository
 * 금형 마스터 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface MoldRepository extends JpaRepository<MoldEntity, Long> {

    /**
     * Get all molds by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT m FROM MoldEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.site " +
           "LEFT JOIN FETCH m.department " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "ORDER BY m.moldCode")
    List<MoldEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get active molds by tenant
     */
    @Query("SELECT m FROM MoldEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.site " +
           "LEFT JOIN FETCH m.department " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "AND m.isActive = true " +
           "ORDER BY m.moldCode")
    List<MoldEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Get mold by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT m FROM MoldEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.site " +
           "LEFT JOIN FETCH m.department " +
           "WHERE m.moldId = :moldId")
    Optional<MoldEntity> findByIdWithAllRelations(@Param("moldId") Long moldId);

    /**
     * Get molds by status
     */
    @Query("SELECT m FROM MoldEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.site " +
           "LEFT JOIN FETCH m.department " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "AND m.status = :status " +
           "ORDER BY m.moldCode")
    List<MoldEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Get molds by type
     */
    @Query("SELECT m FROM MoldEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.site " +
           "LEFT JOIN FETCH m.department " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "AND m.moldType = :moldType " +
           "ORDER BY m.moldCode")
    List<MoldEntity> findByTenantIdAndMoldType(@Param("tenantId") String tenantId, @Param("moldType") String moldType);

    /**
     * Get molds requiring maintenance (shot count near max)
     */
    @Query("SELECT m FROM MoldEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.site " +
           "LEFT JOIN FETCH m.department " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "AND m.maintenanceShotInterval IS NOT NULL " +
           "AND (m.currentShotCount - m.lastMaintenanceShot) >= m.maintenanceShotInterval " +
           "AND m.isActive = true " +
           "ORDER BY m.moldCode")
    List<MoldEntity> findMoldsRequiringMaintenance(@Param("tenantId") String tenantId);

    /**
     * Check if mold code exists for tenant
     */
    boolean existsByTenant_TenantIdAndMoldCode(String tenantId, String moldCode);
}
