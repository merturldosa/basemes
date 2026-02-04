package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.WorkingHoursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Working Hours Repository
 * 근무 시간 설정 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHoursEntity, Long> {

    /**
     * Find all working hours by tenant ID
     */
    @Query("SELECT wh FROM WorkingHoursEntity wh " +
            "WHERE wh.tenant.tenantId = :tenantId " +
            "ORDER BY wh.scheduleName ASC")
    List<WorkingHoursEntity> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find active working hours by tenant ID
     */
    @Query("SELECT wh FROM WorkingHoursEntity wh " +
            "WHERE wh.tenant.tenantId = :tenantId " +
            "AND wh.isActive = true " +
            "ORDER BY wh.scheduleName ASC")
    List<WorkingHoursEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find default working hours
     */
    @Query("SELECT wh FROM WorkingHoursEntity wh " +
            "WHERE wh.tenant.tenantId = :tenantId " +
            "AND wh.isDefault = true " +
            "AND wh.isActive = true")
    Optional<WorkingHoursEntity> findDefaultByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find working hours effective on given date
     */
    @Query("SELECT wh FROM WorkingHoursEntity wh " +
            "WHERE wh.tenant.tenantId = :tenantId " +
            "AND wh.isActive = true " +
            "AND (wh.effectiveFrom IS NULL OR wh.effectiveFrom <= :date) " +
            "AND (wh.effectiveTo IS NULL OR wh.effectiveTo >= :date) " +
            "ORDER BY wh.isDefault DESC, wh.effectiveFrom DESC")
    List<WorkingHoursEntity> findEffectiveByTenantIdAndDate(
            @Param("tenantId") String tenantId,
            @Param("date") LocalDate date);

    /**
     * Find working hours by schedule name
     */
    @Query("SELECT wh FROM WorkingHoursEntity wh " +
            "WHERE wh.tenant.tenantId = :tenantId " +
            "AND wh.scheduleName = :scheduleName")
    Optional<WorkingHoursEntity> findByTenantIdAndScheduleName(
            @Param("tenantId") String tenantId,
            @Param("scheduleName") String scheduleName);

    /**
     * Check if schedule name exists
     */
    @Query("SELECT COUNT(wh) > 0 FROM WorkingHoursEntity wh " +
            "WHERE wh.tenant.tenantId = :tenantId " +
            "AND wh.scheduleName = :scheduleName")
    boolean existsByTenantIdAndScheduleName(
            @Param("tenantId") String tenantId,
            @Param("scheduleName") String scheduleName);
}
