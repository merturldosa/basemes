package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ExternalCalibrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * External Calibration Repository
 * 외부 검교정 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface ExternalCalibrationRepository extends JpaRepository<ExternalCalibrationEntity, Long> {

    /**
     * Get all external calibrations by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT c FROM ExternalCalibrationEntity c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH c.gauge " +
           "WHERE c.tenant.tenantId = :tenantId " +
           "ORDER BY c.requestedDate DESC")
    List<ExternalCalibrationEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get external calibration by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT c FROM ExternalCalibrationEntity c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH c.gauge " +
           "WHERE c.calibrationId = :calibrationId")
    Optional<ExternalCalibrationEntity> findByIdWithAllRelations(@Param("calibrationId") Long calibrationId);

    /**
     * Get external calibrations by gauge ID with all relations (JOIN FETCH)
     */
    @Query("SELECT c FROM ExternalCalibrationEntity c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH c.gauge " +
           "WHERE c.gauge.gaugeId = :gaugeId " +
           "ORDER BY c.requestedDate DESC")
    List<ExternalCalibrationEntity> findByGaugeId(@Param("gaugeId") Long gaugeId);

    /**
     * Get external calibrations by tenant and status with all relations (JOIN FETCH)
     */
    @Query("SELECT c FROM ExternalCalibrationEntity c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH c.gauge " +
           "WHERE c.tenant.tenantId = :tenantId " +
           "AND c.status = :status " +
           "ORDER BY c.requestedDate DESC")
    List<ExternalCalibrationEntity> findByStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Check if calibration number exists for tenant
     */
    boolean existsByTenant_TenantIdAndCalibrationNo(String tenantId, String calibrationNo);
}
