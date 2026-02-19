package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.ExternalCalibrationCreateRequest;
import kr.co.softice.mes.common.dto.equipment.ExternalCalibrationUpdateRequest;
import kr.co.softice.mes.common.dto.equipment.ExternalCalibrationResponse;
import kr.co.softice.mes.domain.entity.ExternalCalibrationEntity;
import kr.co.softice.mes.domain.service.ExternalCalibrationService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * External Calibration Controller
 * 외부 검교정 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/external-calibrations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ExternalCalibration", description = "외부 검교정 API")
public class ExternalCalibrationController {

    private final ExternalCalibrationService externalCalibrationService;

    /**
     * Get all external calibrations
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "외부 검교정 목록 조회", description = "모든 외부 검교정을 조회합니다.")
    public ResponseEntity<List<ExternalCalibrationResponse>> getAllCalibrations() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all external calibrations for tenant: {}", tenantId);

        List<ExternalCalibrationEntity> calibrations = externalCalibrationService.getAllCalibrations(tenantId);
        List<ExternalCalibrationResponse> response = calibrations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get external calibration by ID
     */
    @GetMapping("/{calibrationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "외부 검교정 상세 조회", description = "ID로 외부 검교정을 조회합니다.")
    public ResponseEntity<ExternalCalibrationResponse> getCalibrationById(@PathVariable Long calibrationId) {
        log.info("Getting external calibration by ID: {}", calibrationId);

        ExternalCalibrationEntity calibration = externalCalibrationService.getCalibrationById(calibrationId);
        ExternalCalibrationResponse response = toResponse(calibration);

        return ResponseEntity.ok(response);
    }

    /**
     * Get external calibrations by gauge
     */
    @GetMapping("/gauge/{gaugeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "계측기별 외부 검교정 조회", description = "특정 계측기의 외부 검교정을 조회합니다.")
    public ResponseEntity<List<ExternalCalibrationResponse>> getByGauge(@PathVariable Long gaugeId) {
        log.info("Getting external calibrations for gauge ID: {}", gaugeId);

        List<ExternalCalibrationEntity> calibrations = externalCalibrationService.getByGauge(gaugeId);
        List<ExternalCalibrationResponse> response = calibrations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get external calibrations by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 외부 검교정 조회", description = "특정 상태의 외부 검교정을 조회합니다.")
    public ResponseEntity<List<ExternalCalibrationResponse>> getByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting external calibrations by status: {} for tenant: {}", status, tenantId);

        List<ExternalCalibrationEntity> calibrations = externalCalibrationService.getByStatus(tenantId, status);
        List<ExternalCalibrationResponse> response = calibrations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create external calibration
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "외부 검교정 등록", description = "새로운 외부 검교정을 등록합니다.")
    public ResponseEntity<ExternalCalibrationResponse> createCalibration(@Valid @RequestBody ExternalCalibrationCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating external calibration: {} for tenant: {}", request.getCalibrationNo(), tenantId);

        ExternalCalibrationEntity entity = toEntity(request);
        ExternalCalibrationEntity created = externalCalibrationService.createCalibration(
                tenantId, entity, request.getGaugeId());
        ExternalCalibrationResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update external calibration
     */
    @PutMapping("/{calibrationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "외부 검교정 수정", description = "외부 검교정 정보를 수정합니다.")
    public ResponseEntity<ExternalCalibrationResponse> updateCalibration(
            @PathVariable Long calibrationId,
            @Valid @RequestBody ExternalCalibrationUpdateRequest request) {
        log.info("Updating external calibration ID: {}", calibrationId);

        ExternalCalibrationEntity updateData = toEntity(request);
        ExternalCalibrationEntity updated = externalCalibrationService.updateCalibration(calibrationId, updateData);
        ExternalCalibrationResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Complete external calibration
     */
    @PostMapping("/{calibrationId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "외부 검교정 완료", description = "외부 검교정을 완료 처리합니다. 계측기 교정 상태도 함께 업데이트됩니다.")
    public ResponseEntity<ExternalCalibrationResponse> completeCalibration(
            @PathVariable Long calibrationId,
            @RequestParam String result,
            @RequestParam(required = false) String certificateNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextCalibrationDate) {
        log.info("Completing external calibration ID: {} result: {}", calibrationId, result);

        ExternalCalibrationEntity completed = externalCalibrationService.completeCalibration(
                calibrationId, result, certificateNo, nextCalibrationDate);
        ExternalCalibrationResponse response = toResponse(completed);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete external calibration
     */
    @DeleteMapping("/{calibrationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "외부 검교정 삭제", description = "외부 검교정을 삭제합니다.")
    public ResponseEntity<Void> deleteCalibration(@PathVariable Long calibrationId) {
        log.info("Deleting external calibration ID: {}", calibrationId);

        externalCalibrationService.deleteCalibration(calibrationId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert ExternalCalibrationCreateRequest to Entity
     */
    private ExternalCalibrationEntity toEntity(ExternalCalibrationCreateRequest request) {
        return ExternalCalibrationEntity.builder()
                .calibrationNo(request.getCalibrationNo())
                .requestedDate(request.getRequestedDate())
                .calibrationVendor(request.getCalibrationVendor())
                .cost(request.getCost())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert ExternalCalibrationUpdateRequest to Entity
     */
    private ExternalCalibrationEntity toEntity(ExternalCalibrationUpdateRequest request) {
        return ExternalCalibrationEntity.builder()
                .calibrationVendor(request.getCalibrationVendor())
                .sentDate(request.getSentDate())
                .cost(request.getCost())
                .remarks(request.getRemarks())
                .status(request.getStatus())
                .build();
    }

    /**
     * Convert Entity to ExternalCalibrationResponse
     */
    private ExternalCalibrationResponse toResponse(ExternalCalibrationEntity entity) {
        return ExternalCalibrationResponse.builder()
                .calibrationId(entity.getCalibrationId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .calibrationNo(entity.getCalibrationNo())
                .gaugeId(entity.getGauge() != null ? entity.getGauge().getGaugeId() : null)
                .gaugeCode(entity.getGauge() != null ? entity.getGauge().getGaugeCode() : null)
                .gaugeName(entity.getGauge() != null ? entity.getGauge().getGaugeName() : null)
                .calibrationVendor(entity.getCalibrationVendor())
                .requestedDate(entity.getRequestedDate())
                .sentDate(entity.getSentDate())
                .completedDate(entity.getCompletedDate())
                .certificateNo(entity.getCertificateNo())
                .certificateUrl(entity.getCertificateUrl())
                .calibrationResult(entity.getCalibrationResult())
                .cost(entity.getCost())
                .nextCalibrationDate(entity.getNextCalibrationDate())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
