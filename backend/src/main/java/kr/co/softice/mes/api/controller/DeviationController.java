package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.DeviationCreateRequest;
import kr.co.softice.mes.common.dto.equipment.DeviationUpdateRequest;
import kr.co.softice.mes.common.dto.equipment.DeviationResponse;
import kr.co.softice.mes.domain.entity.DeviationEntity;
import kr.co.softice.mes.domain.service.DeviationService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Deviation Controller
 * 이탈 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/deviations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deviation", description = "이탈 관리 API")
public class DeviationController {

    private final DeviationService deviationService;

    /**
     * Get all deviations
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "이탈 목록 조회", description = "모든 이탈을 조회합니다.")
    public ResponseEntity<List<DeviationResponse>> getAllDeviations() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all deviations for tenant: {}", tenantId);

        List<DeviationEntity> deviations = deviationService.getAllDeviations(tenantId);
        List<DeviationResponse> response = deviations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get deviation by ID
     */
    @GetMapping("/{deviationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "이탈 상세 조회", description = "ID로 이탈을 조회합니다.")
    public ResponseEntity<DeviationResponse> getDeviationById(@PathVariable Long deviationId) {
        log.info("Getting deviation by ID: {}", deviationId);

        DeviationEntity deviation = deviationService.getDeviationById(deviationId);
        DeviationResponse response = toResponse(deviation);

        return ResponseEntity.ok(response);
    }

    /**
     * Get deviations by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 이탈 조회", description = "특정 상태의 이탈을 조회합니다.")
    public ResponseEntity<List<DeviationResponse>> getByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting deviations by status: {} for tenant: {}", status, tenantId);

        List<DeviationEntity> deviations = deviationService.getByStatus(tenantId, status);
        List<DeviationResponse> response = deviations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get deviations by equipment
     */
    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비별 이탈 조회", description = "특정 설비의 이탈을 조회합니다.")
    public ResponseEntity<List<DeviationResponse>> getByEquipment(@PathVariable Long equipmentId) {
        log.info("Getting deviations for equipment ID: {}", equipmentId);

        List<DeviationEntity> deviations = deviationService.getByEquipment(equipmentId);
        List<DeviationResponse> response = deviations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create deviation
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "이탈 등록", description = "새로운 이탈을 등록합니다.")
    public ResponseEntity<DeviationResponse> createDeviation(@Valid @RequestBody DeviationCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating deviation: {} for tenant: {}", request.getDeviationNo(), tenantId);

        DeviationEntity entity = toEntity(request);
        DeviationEntity created = deviationService.createDeviation(
                tenantId, entity, request.getEquipmentId(), request.getDetectedByUserId());
        DeviationResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update deviation
     */
    @PutMapping("/{deviationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "이탈 수정", description = "이탈 정보를 수정합니다.")
    public ResponseEntity<DeviationResponse> updateDeviation(
            @PathVariable Long deviationId,
            @Valid @RequestBody DeviationUpdateRequest request) {
        log.info("Updating deviation ID: {}", deviationId);

        DeviationEntity updateData = toEntity(request);
        DeviationEntity updated = deviationService.updateDeviation(
                deviationId, updateData, request.getResolvedByUserId());
        DeviationResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Change deviation status
     */
    @PatchMapping("/{deviationId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "이탈 상태 변경", description = "이탈 상태를 변경합니다. (OPEN -> INVESTIGATING -> RESOLVED -> CLOSED)")
    public ResponseEntity<DeviationResponse> changeStatus(
            @PathVariable Long deviationId,
            @RequestParam String status) {
        log.info("Changing deviation ID: {} status to: {}", deviationId, status);

        DeviationEntity updated = deviationService.changeStatus(deviationId, status);
        DeviationResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete deviation
     */
    @DeleteMapping("/{deviationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "이탈 삭제", description = "이탈을 삭제합니다.")
    public ResponseEntity<Void> deleteDeviation(@PathVariable Long deviationId) {
        log.info("Deleting deviation ID: {}", deviationId);

        deviationService.deleteDeviation(deviationId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert DeviationCreateRequest to Entity
     */
    private DeviationEntity toEntity(DeviationCreateRequest request) {
        return DeviationEntity.builder()
                .deviationNo(request.getDeviationNo())
                .parameterName(request.getParameterName())
                .standardValue(request.getStandardValue())
                .actualValue(request.getActualValue())
                .deviationValue(request.getDeviationValue())
                .detectedAt(request.getDetectedAt())
                .severity(request.getSeverity())
                .description(request.getDescription())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert DeviationUpdateRequest to Entity
     */
    private DeviationEntity toEntity(DeviationUpdateRequest request) {
        return DeviationEntity.builder()
                .parameterName(request.getParameterName())
                .standardValue(request.getStandardValue())
                .actualValue(request.getActualValue())
                .deviationValue(request.getDeviationValue())
                .severity(request.getSeverity())
                .description(request.getDescription())
                .rootCause(request.getRootCause())
                .correctiveAction(request.getCorrectiveAction())
                .preventiveAction(request.getPreventiveAction())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to DeviationResponse
     */
    private DeviationResponse toResponse(DeviationEntity entity) {
        return DeviationResponse.builder()
                .deviationId(entity.getDeviationId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .deviationNo(entity.getDeviationNo())
                .equipmentId(entity.getEquipment() != null ? entity.getEquipment().getEquipmentId() : null)
                .equipmentCode(entity.getEquipment() != null ? entity.getEquipment().getEquipmentCode() : null)
                .equipmentName(entity.getEquipment() != null ? entity.getEquipment().getEquipmentName() : null)
                .parameterName(entity.getParameterName())
                .standardValue(entity.getStandardValue())
                .actualValue(entity.getActualValue())
                .deviationValue(entity.getDeviationValue())
                .detectedAt(entity.getDetectedAt())
                .detectedByUserId(entity.getDetectedByUser() != null ? entity.getDetectedByUser().getUserId() : null)
                .detectedByUserName(entity.getDetectedByUser() != null ? entity.getDetectedByUser().getFullName() : null)
                .severity(entity.getSeverity())
                .description(entity.getDescription())
                .rootCause(entity.getRootCause())
                .correctiveAction(entity.getCorrectiveAction())
                .preventiveAction(entity.getPreventiveAction())
                .status(entity.getStatus())
                .resolvedAt(entity.getResolvedAt())
                .resolvedByUserId(entity.getResolvedByUser() != null ? entity.getResolvedByUser().getUserId() : null)
                .resolvedByUserName(entity.getResolvedByUser() != null ? entity.getResolvedByUser().getFullName() : null)
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
