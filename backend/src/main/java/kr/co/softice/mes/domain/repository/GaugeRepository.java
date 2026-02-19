package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.GaugeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Gauge Repository
 * 계측기 마스터 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface GaugeRepository extends JpaRepository<GaugeEntity, Long> {

    /**
     * Get all gauges by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT g FROM GaugeEntity g " +
           "JOIN FETCH g.tenant " +
           "LEFT JOIN FETCH g.equipment " +
           "LEFT JOIN FETCH g.department " +
           "WHERE g.tenant.tenantId = :tenantId " +
           "ORDER BY g.gaugeCode")
    List<GaugeEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get gauge by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT g FROM GaugeEntity g " +
           "JOIN FETCH g.tenant " +
           "LEFT JOIN FETCH g.equipment " +
           "LEFT JOIN FETCH g.department " +
           "WHERE g.gaugeId = :gaugeId")
    Optional<GaugeEntity> findByIdWithAllRelations(@Param("gaugeId") Long gaugeId);

    /**
     * Get gauges with calibration due by date
     */
    @Query("SELECT g FROM GaugeEntity g " +
           "JOIN FETCH g.tenant " +
           "LEFT JOIN FETCH g.equipment " +
           "LEFT JOIN FETCH g.department " +
           "WHERE g.tenant.tenantId = :tenantId " +
           "AND g.nextCalibrationDate <= :dueDate " +
           "AND g.isActive = true " +
           "ORDER BY g.nextCalibrationDate")
    List<GaugeEntity> findCalibrationDue(@Param("tenantId") String tenantId, @Param("dueDate") LocalDate dueDate);

    /**
     * Check if gauge code exists for tenant
     */
    boolean existsByTenant_TenantIdAndGaugeCode(String tenantId, String gaugeCode);
}
