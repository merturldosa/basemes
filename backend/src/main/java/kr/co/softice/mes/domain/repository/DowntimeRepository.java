package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.DowntimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Downtime Repository
 * 비가동 이력 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface DowntimeRepository extends JpaRepository<DowntimeEntity, Long> {

    /**
     * Get all downtimes by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT d FROM DowntimeEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.operation " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "ORDER BY d.startTime DESC")
    List<DowntimeEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get downtime by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT d FROM DowntimeEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.operation " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "WHERE d.downtimeId = :downtimeId")
    Optional<DowntimeEntity> findByIdWithAllRelations(@Param("downtimeId") Long downtimeId);

    /**
     * Get downtimes by equipment
     */
    @Query("SELECT d FROM DowntimeEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.operation " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "WHERE d.equipment.equipmentId = :equipmentId " +
           "ORDER BY d.startTime DESC")
    List<DowntimeEntity> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * Get downtimes by tenant and type
     */
    @Query("SELECT d FROM DowntimeEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.operation " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.downtimeType = :downtimeType " +
           "ORDER BY d.startTime DESC")
    List<DowntimeEntity> findByTenantIdAndDowntimeType(@Param("tenantId") String tenantId, @Param("downtimeType") String downtimeType);

    /**
     * Get downtimes by tenant and date range
     */
    @Query("SELECT d FROM DowntimeEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.operation " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.startTime BETWEEN :startDate AND :endDate " +
           "ORDER BY d.startTime DESC")
    List<DowntimeEntity> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get unresolved downtimes
     */
    @Query("SELECT d FROM DowntimeEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.operation " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.isResolved = false " +
           "ORDER BY d.startTime DESC")
    List<DowntimeEntity> findUnresolvedByTenantId(@Param("tenantId") String tenantId);

    /**
     * Get ongoing downtimes (not ended yet)
     */
    @Query("SELECT d FROM DowntimeEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.operation " +
           "LEFT JOIN FETCH d.responsibleUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.endTime IS NULL " +
           "ORDER BY d.startTime DESC")
    List<DowntimeEntity> findOngoingByTenantId(@Param("tenantId") String tenantId);

    /**
     * Check if downtime code exists for tenant
     */
    boolean existsByTenant_TenantIdAndDowntimeCode(String tenantId, String downtimeCode);
}
