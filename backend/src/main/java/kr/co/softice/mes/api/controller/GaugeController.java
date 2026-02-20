package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.GaugeCreateRequest;
import kr.co.softice.mes.common.dto.equipment.GaugeResponse;
import kr.co.softice.mes.common.dto.equipment.GaugeUpdateRequest;
import kr.co.softice.mes.domain.entity.GaugeEntity;
import kr.co.softice.mes.domain.service.GaugeService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gauge Controller
 * 계측기 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/gauges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gauge", description = "계측기 관리 API")
public class GaugeController {

    private final GaugeService gaugeService;

    /**
     * Get all gauges
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "계측기 목록 조회", description = "모든 계측기를 조회합니다.")
    public ResponseEntity<List<GaugeResponse>> getAllGauges() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all gauges for tenant: {}", tenantId);

        List<GaugeEntity> gauges = gaugeService.getAllGauges(tenantId);
        List<GaugeResponse> response = gauges.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get gauge by ID
     */
    @Transactional(readOnly = true)
    @GetMapping("/{gaugeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "계측기 상세 조회", description = "ID로 계측기를 조회합니다.")
    public ResponseEntity<GaugeResponse> getGaugeById(@PathVariable Long gaugeId) {
        log.info("Getting gauge by ID: {}", gaugeId);

        GaugeEntity gauge = gaugeService.getGaugeById(gaugeId);
        GaugeResponse response = toResponse(gauge);

        return ResponseEntity.ok(response);
    }

    /**
     * Get gauges with calibration due
     */
    @Transactional(readOnly = true)
    @GetMapping("/calibration-due")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "교정 예정 계측기 조회", description = "지정 일자까지 교정이 필요한 계측기를 조회합니다.")
    public ResponseEntity<List<GaugeResponse>> getCalibrationDue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting calibration due gauges by {} for tenant: {}", dueDate, tenantId);

        List<GaugeEntity> gauges = gaugeService.getCalibrationDue(tenantId, dueDate);
        List<GaugeResponse> response = gauges.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create gauge
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "계측기 등록", description = "새로운 계측기를 등록합니다.")
    public ResponseEntity<GaugeResponse> createGauge(@Valid @RequestBody GaugeCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating gauge: {} for tenant: {}", request.getGaugeCode(), tenantId);

        GaugeEntity gauge = toEntity(request);
        GaugeEntity created = gaugeService.createGauge(tenantId, gauge, request.getEquipmentId(), request.getDepartmentId());
        GaugeResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update gauge
     */
    @PutMapping("/{gaugeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "계측기 수정", description = "계측기 정보를 수정합니다.")
    public ResponseEntity<GaugeResponse> updateGauge(
            @PathVariable Long gaugeId,
            @Valid @RequestBody GaugeUpdateRequest request) {
        log.info("Updating gauge ID: {}", gaugeId);

        GaugeEntity updateData = toEntity(request);
        GaugeEntity updated = gaugeService.updateGauge(gaugeId, updateData, request.getEquipmentId(), request.getDepartmentId());
        GaugeResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete gauge
     */
    @DeleteMapping("/{gaugeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "계측기 삭제", description = "계측기를 삭제합니다.")
    public ResponseEntity<Void> deleteGauge(@PathVariable Long gaugeId) {
        log.info("Deleting gauge ID: {}", gaugeId);

        gaugeService.deleteGauge(gaugeId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert GaugeCreateRequest to Entity
     */
    private GaugeEntity toEntity(GaugeCreateRequest request) {
        return GaugeEntity.builder()
                .gaugeCode(request.getGaugeCode())
                .gaugeName(request.getGaugeName())
                .gaugeType(request.getGaugeType())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .serialNo(request.getSerialNo())
                .location(request.getLocation())
                .measurementRange(request.getMeasurementRange())
                .accuracy(request.getAccuracy())
                .calibrationCycleDays(request.getCalibrationCycleDays())
                .lastCalibrationDate(request.getLastCalibrationDate())
                .nextCalibrationDate(request.getNextCalibrationDate())
                .calibrationStatus(request.getCalibrationStatus())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert GaugeUpdateRequest to Entity
     */
    private GaugeEntity toEntity(GaugeUpdateRequest request) {
        return GaugeEntity.builder()
                .gaugeName(request.getGaugeName())
                .gaugeType(request.getGaugeType())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .serialNo(request.getSerialNo())
                .location(request.getLocation())
                .measurementRange(request.getMeasurementRange())
                .accuracy(request.getAccuracy())
                .calibrationCycleDays(request.getCalibrationCycleDays())
                .lastCalibrationDate(request.getLastCalibrationDate())
                .nextCalibrationDate(request.getNextCalibrationDate())
                .calibrationStatus(request.getCalibrationStatus())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to GaugeResponse
     */
    private GaugeResponse toResponse(GaugeEntity entity) {
        return GaugeResponse.builder()
                .gaugeId(entity.getGaugeId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .gaugeCode(entity.getGaugeCode())
                .gaugeName(entity.getGaugeName())
                .gaugeType(entity.getGaugeType())
                .manufacturer(entity.getManufacturer())
                .modelName(entity.getModelName())
                .serialNo(entity.getSerialNo())
                .equipmentId(entity.getEquipment() != null ? entity.getEquipment().getEquipmentId() : null)
                .equipmentCode(entity.getEquipment() != null ? entity.getEquipment().getEquipmentCode() : null)
                .equipmentName(entity.getEquipment() != null ? entity.getEquipment().getEquipmentName() : null)
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getDepartmentId() : null)
                .departmentCode(entity.getDepartment() != null ? entity.getDepartment().getDepartmentCode() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getDepartmentName() : null)
                .location(entity.getLocation())
                .measurementRange(entity.getMeasurementRange())
                .accuracy(entity.getAccuracy())
                .calibrationCycleDays(entity.getCalibrationCycleDays())
                .lastCalibrationDate(entity.getLastCalibrationDate())
                .nextCalibrationDate(entity.getNextCalibrationDate())
                .calibrationStatus(entity.getCalibrationStatus())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
