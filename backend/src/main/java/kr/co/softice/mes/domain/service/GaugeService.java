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
 * Gauge Service
 * 계측기 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GaugeService {

    private final GaugeRepository gaugeRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Get all gauges for tenant
     */
    public List<GaugeEntity> getAllGauges(String tenantId) {
        log.info("Getting all gauges for tenant: {}", tenantId);
        return gaugeRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get gauge by ID
     */
    public GaugeEntity getGaugeById(Long gaugeId) {
        log.info("Getting gauge by ID: {}", gaugeId);
        return gaugeRepository.findByIdWithAllRelations(gaugeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAUGE_NOT_FOUND));
    }

    /**
     * Get gauges with calibration due by date
     */
    public List<GaugeEntity> getCalibrationDue(String tenantId, LocalDate dueDate) {
        log.info("Getting gauges with calibration due by {} for tenant: {}", dueDate, tenantId);
        return gaugeRepository.findCalibrationDue(tenantId, dueDate);
    }

    /**
     * Create gauge
     */
    @Transactional
    public GaugeEntity createGauge(String tenantId, GaugeEntity gauge, Long equipmentId, Long departmentId) {
        log.info("Creating gauge: {} for tenant: {}", gauge.getGaugeCode(), tenantId);

        // Check duplicate gaugeCode
        if (gaugeRepository.existsByTenant_TenantIdAndGaugeCode(tenantId, gauge.getGaugeCode())) {
            throw new BusinessException(ErrorCode.GAUGE_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        gauge.setTenant(tenant);

        // Set equipment (optional)
        if (equipmentId != null) {
            EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
            gauge.setEquipment(equipment);
        }

        // Set department (optional)
        if (departmentId != null) {
            DepartmentEntity department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            gauge.setDepartment(department);
        }

        // Set defaults
        if (gauge.getIsActive() == null) {
            gauge.setIsActive(true);
        }
        if (gauge.getStatus() == null) {
            gauge.setStatus("ACTIVE");
        }
        if (gauge.getCalibrationStatus() == null) {
            gauge.setCalibrationStatus("VALID");
        }

        GaugeEntity saved = gaugeRepository.save(gauge);
        log.info("Gauge created successfully: {}", saved.getGaugeCode());
        return saved;
    }

    /**
     * Update gauge
     */
    @Transactional
    public GaugeEntity updateGauge(Long gaugeId, GaugeEntity updateData, Long equipmentId, Long departmentId) {
        log.info("Updating gauge ID: {}", gaugeId);

        GaugeEntity existing = gaugeRepository.findByIdWithAllRelations(gaugeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAUGE_NOT_FOUND));

        // Update fields
        if (updateData.getGaugeName() != null) {
            existing.setGaugeName(updateData.getGaugeName());
        }
        if (updateData.getGaugeType() != null) {
            existing.setGaugeType(updateData.getGaugeType());
        }
        if (updateData.getManufacturer() != null) {
            existing.setManufacturer(updateData.getManufacturer());
        }
        if (updateData.getModelName() != null) {
            existing.setModelName(updateData.getModelName());
        }
        if (updateData.getSerialNo() != null) {
            existing.setSerialNo(updateData.getSerialNo());
        }
        if (updateData.getLocation() != null) {
            existing.setLocation(updateData.getLocation());
        }
        if (updateData.getMeasurementRange() != null) {
            existing.setMeasurementRange(updateData.getMeasurementRange());
        }
        if (updateData.getAccuracy() != null) {
            existing.setAccuracy(updateData.getAccuracy());
        }
        if (updateData.getCalibrationCycleDays() != null) {
            existing.setCalibrationCycleDays(updateData.getCalibrationCycleDays());
        }
        if (updateData.getLastCalibrationDate() != null) {
            existing.setLastCalibrationDate(updateData.getLastCalibrationDate());
        }
        if (updateData.getNextCalibrationDate() != null) {
            existing.setNextCalibrationDate(updateData.getNextCalibrationDate());
        }
        if (updateData.getCalibrationStatus() != null) {
            existing.setCalibrationStatus(updateData.getCalibrationStatus());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Update equipment (optional)
        if (equipmentId != null) {
            EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
            existing.setEquipment(equipment);
        }

        // Update department (optional)
        if (departmentId != null) {
            DepartmentEntity department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            existing.setDepartment(department);
        }

        GaugeEntity updated = gaugeRepository.save(existing);
        log.info("Gauge updated successfully: {}", updated.getGaugeCode());
        return updated;
    }

    /**
     * Delete gauge
     */
    @Transactional
    public void deleteGauge(Long gaugeId) {
        log.info("Deleting gauge ID: {}", gaugeId);

        GaugeEntity gauge = gaugeRepository.findById(gaugeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAUGE_NOT_FOUND));

        gaugeRepository.delete(gauge);
        log.info("Gauge deleted successfully: {}", gauge.getGaugeCode());
    }
}
