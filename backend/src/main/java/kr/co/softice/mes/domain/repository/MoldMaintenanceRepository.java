package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.MoldMaintenanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Mold Maintenance Repository
 * 금형 보전 이력 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface MoldMaintenanceRepository extends JpaRepository<MoldMaintenanceEntity, Long> {

    /**
     * Get all maintenances by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT mm FROM MoldMaintenanceEntity mm " +
           "JOIN FETCH mm.tenant " +
           "JOIN FETCH mm.mold " +
           "LEFT JOIN FETCH mm.technicianUser " +
           "WHERE mm.tenant.tenantId = :tenantId " +
           "ORDER BY mm.maintenanceDate DESC")
    List<MoldMaintenanceEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get maintenance by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT mm FROM MoldMaintenanceEntity mm " +
           "JOIN FETCH mm.tenant " +
           "JOIN FETCH mm.mold " +
           "LEFT JOIN FETCH mm.technicianUser " +
           "WHERE mm.maintenanceId = :maintenanceId")
    Optional<MoldMaintenanceEntity> findByIdWithAllRelations(@Param("maintenanceId") Long maintenanceId);

    /**
     * Get maintenances by mold
     */
    @Query("SELECT mm FROM MoldMaintenanceEntity mm " +
           "JOIN FETCH mm.tenant " +
           "JOIN FETCH mm.mold " +
           "LEFT JOIN FETCH mm.technicianUser " +
           "WHERE mm.mold.moldId = :moldId " +
           "ORDER BY mm.maintenanceDate DESC")
    List<MoldMaintenanceEntity> findByMoldId(@Param("moldId") Long moldId);

    /**
     * Get maintenances by type
     */
    @Query("SELECT mm FROM MoldMaintenanceEntity mm " +
           "JOIN FETCH mm.tenant " +
           "JOIN FETCH mm.mold " +
           "LEFT JOIN FETCH mm.technicianUser " +
           "WHERE mm.tenant.tenantId = :tenantId " +
           "AND mm.maintenanceType = :maintenanceType " +
           "ORDER BY mm.maintenanceDate DESC")
    List<MoldMaintenanceEntity> findByTenantIdAndMaintenanceType(@Param("tenantId") String tenantId, @Param("maintenanceType") String maintenanceType);

    /**
     * Get maintenances by date range
     */
    @Query("SELECT mm FROM MoldMaintenanceEntity mm " +
           "JOIN FETCH mm.tenant " +
           "JOIN FETCH mm.mold " +
           "LEFT JOIN FETCH mm.technicianUser " +
           "WHERE mm.tenant.tenantId = :tenantId " +
           "AND mm.maintenanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY mm.maintenanceDate DESC")
    List<MoldMaintenanceEntity> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Check if maintenance number exists for tenant
     */
    boolean existsByTenant_TenantIdAndMaintenanceNo(String tenantId, String maintenanceNo);
}
