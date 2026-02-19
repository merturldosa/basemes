package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * External Calibration Service
 * 외부 검교정 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExternalCalibrationService {

    private final ExternalCalibrationRepository externalCalibrationRepository;
    private final TenantRepository tenantRepository;
    private final GaugeRepository gaugeRepository;

    /**
     * Get all external calibrations for tenant
     */
    public List<ExternalCalibrationEntity> getAllCalibrations(String tenantId) {
        log.info("Getting all external calibrations for tenant: {}", tenantId);
        return externalCalibrationRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get external calibration by ID
     */
    public ExternalCalibrationEntity getCalibrationById(Long calibrationId) {
        log.info("Getting external calibration by ID: {}", calibrationId);
        return externalCalibrationRepository.findByIdWithAllRelations(calibrationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXTERNAL_CALIBRATION_NOT_FOUND));
    }

    /**
     * Get external calibrations by gauge ID
     */
    public List<ExternalCalibrationEntity> getByGauge(Long gaugeId) {
        log.info("Getting external calibrations for gauge ID: {}", gaugeId);
        return externalCalibrationRepository.findByGaugeId(gaugeId);
    }

    /**
     * Get external calibrations by status for tenant
     */
    public List<ExternalCalibrationEntity> getByStatus(String tenantId, String status) {
        log.info("Getting external calibrations for tenant: {} with status: {}", tenantId, status);
        return externalCalibrationRepository.findByStatus(tenantId, status);
    }

    /**
     * Create external calibration
     */
    @Transactional
    public ExternalCalibrationEntity createCalibration(String tenantId, ExternalCalibrationEntity calibration,
                                                        Long gaugeId) {
        log.info("Creating external calibration for tenant: {} gauge: {}", tenantId, gaugeId);

        // Check duplicate
        if (externalCalibrationRepository.existsByTenant_TenantIdAndCalibrationNo(tenantId, calibration.getCalibrationNo())) {
            throw new BusinessException(ErrorCode.EXTERNAL_CALIBRATION_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        calibration.setTenant(tenant);

        // Set gauge (required)
        GaugeEntity gauge = gaugeRepository.findById(gaugeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAUGE_NOT_FOUND));
        calibration.setGauge(gauge);

        // Set default status
        if (calibration.getStatus() == null) {
            calibration.setStatus("REQUESTED");
        }

        ExternalCalibrationEntity saved = externalCalibrationRepository.save(calibration);
        log.info("External calibration created successfully: {}", saved.getCalibrationId());
        return saved;
    }

    /**
     * Update external calibration
     */
    @Transactional
    public ExternalCalibrationEntity updateCalibration(Long calibrationId, ExternalCalibrationEntity updateData) {
        log.info("Updating external calibration ID: {}", calibrationId);

        ExternalCalibrationEntity existing = externalCalibrationRepository.findByIdWithAllRelations(calibrationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXTERNAL_CALIBRATION_NOT_FOUND));

        // Update non-null fields
        if (updateData.getCalibrationVendor() != null) {
            existing.setCalibrationVendor(updateData.getCalibrationVendor());
        }
        if (updateData.getSentDate() != null) {
            existing.setSentDate(updateData.getSentDate());
        }
        if (updateData.getCost() != null) {
            existing.setCost(updateData.getCost());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }

        ExternalCalibrationEntity updated = externalCalibrationRepository.save(existing);
        log.info("External calibration updated successfully: {}", updated.getCalibrationId());
        return updated;
    }

    /**
     * Complete external calibration
     * KEY LOGIC: Updates calibration status and syncs gauge calibration data
     */
    @Transactional
    public ExternalCalibrationEntity completeCalibration(Long calibrationId, String calibrationResult,
                                                          String certificateNo, LocalDate nextCalibrationDate) {
        log.info("Completing external calibration ID: {} result: {}", calibrationId, calibrationResult);

        ExternalCalibrationEntity existing = externalCalibrationRepository.findByIdWithAllRelations(calibrationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXTERNAL_CALIBRATION_NOT_FOUND));

        // Set calibration completion data
        existing.setStatus("COMPLETED");
        existing.setCompletedDate(LocalDate.now());
        existing.setCalibrationResult(calibrationResult);
        if (certificateNo != null) {
            existing.setCertificateNo(certificateNo);
        }
        if (nextCalibrationDate != null) {
            existing.setNextCalibrationDate(nextCalibrationDate);
        }

        // UPDATE GAUGE: sync calibration data to gauge
        GaugeEntity gauge = existing.getGauge();
        gauge.setLastCalibrationDate(existing.getCompletedDate());
        if (nextCalibrationDate != null) {
            gauge.setNextCalibrationDate(nextCalibrationDate);
        }
        gauge.setCalibrationStatus("PASS".equals(calibrationResult) ? "VALID" : "EXPIRED");
        gaugeRepository.save(gauge);

        ExternalCalibrationEntity updated = externalCalibrationRepository.save(existing);
        log.info("External calibration completed successfully: {} gauge updated: {}",
                updated.getCalibrationId(), gauge.getGaugeId());
        return updated;
    }

    /**
     * Delete external calibration
     */
    @Transactional
    public void deleteCalibration(Long calibrationId) {
        log.info("Deleting external calibration ID: {}", calibrationId);

        ExternalCalibrationEntity calibration = externalCalibrationRepository.findById(calibrationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXTERNAL_CALIBRATION_NOT_FOUND));

        externalCalibrationRepository.delete(calibration);
        log.info("External calibration deleted successfully: {}", calibration.getCalibrationId());
    }
}
