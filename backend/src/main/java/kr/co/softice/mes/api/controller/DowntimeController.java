package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.downtime.DowntimeCreateRequest;
import kr.co.softice.mes.common.dto.downtime.DowntimeResponse;
import kr.co.softice.mes.common.dto.downtime.DowntimeUpdateRequest;
import kr.co.softice.mes.domain.entity.DowntimeEntity;
import kr.co.softice.mes.domain.service.DowntimeService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Downtime Controller
 * 비가동 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/downtimes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Downtime", description = "비가동 관리 API")
public class DowntimeController {

    private final DowntimeService downtimeService;

    /**
     * Get all downtimes
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "비가동 목록 조회", description = "모든 비가동 이력을 조회합니다.")
    public ResponseEntity<List<DowntimeResponse>> getAllDowntimes() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all downtimes for tenant: {}", tenantId);

        List<DowntimeEntity> downtimes = downtimeService.getAllDowntimes(tenantId);
        List<DowntimeResponse> response = downtimes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get downtime by ID
     */
    @GetMapping("/{downtimeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "비가동 상세 조회", description = "ID로 비가동 이력을 조회합니다.")
    public ResponseEntity<DowntimeResponse> getDowntimeById(@PathVariable Long downtimeId) {
        log.info("Getting downtime by ID: {}", downtimeId);

        DowntimeEntity downtime = downtimeService.getDowntimeById(downtimeId);
        DowntimeResponse response = toResponse(downtime);

        return ResponseEntity.ok(response);
    }

    /**
     * Get downtimes by equipment
     */
    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비별 비가동 조회", description = "특정 설비의 비가동 이력을 조회합니다.")
    public ResponseEntity<List<DowntimeResponse>> getDowntimesByEquipment(@PathVariable Long equipmentId) {
        log.info("Getting downtimes for equipment ID: {}", equipmentId);

        List<DowntimeEntity> downtimes = downtimeService.getDowntimesByEquipment(equipmentId);
        List<DowntimeResponse> response = downtimes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get downtimes by type
     */
    @GetMapping("/type/{downtimeType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "유형별 비가동 조회", description = "특정 유형의 비가동 이력을 조회합니다.")
    public ResponseEntity<List<DowntimeResponse>> getDowntimesByType(@PathVariable String downtimeType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting downtimes by type: {} for tenant: {}", downtimeType, tenantId);

        List<DowntimeEntity> downtimes = downtimeService.getDowntimesByType(tenantId, downtimeType);
        List<DowntimeResponse> response = downtimes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get downtimes by date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기간별 비가동 조회", description = "특정 기간의 비가동 이력을 조회합니다.")
    public ResponseEntity<List<DowntimeResponse>> getDowntimesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting downtimes for tenant: {} from {} to {}", tenantId, startDate, endDate);

        List<DowntimeEntity> downtimes = downtimeService.getDowntimesByDateRange(tenantId, startDate, endDate);
        List<DowntimeResponse> response = downtimes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get unresolved downtimes
     */
    @GetMapping("/unresolved")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "미해결 비가동 조회", description = "미해결 비가동 이력을 조회합니다.")
    public ResponseEntity<List<DowntimeResponse>> getUnresolvedDowntimes() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting unresolved downtimes for tenant: {}", tenantId);

        List<DowntimeEntity> downtimes = downtimeService.getUnresolvedDowntimes(tenantId);
        List<DowntimeResponse> response = downtimes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get ongoing downtimes
     */
    @GetMapping("/ongoing")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "진행중 비가동 조회", description = "진행중인 비가동 이력을 조회합니다.")
    public ResponseEntity<List<DowntimeResponse>> getOngoingDowntimes() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting ongoing downtimes for tenant: {}", tenantId);

        List<DowntimeEntity> downtimes = downtimeService.getOngoingDowntimes(tenantId);
        List<DowntimeResponse> response = downtimes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create downtime
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "비가동 등록", description = "새로운 비가동 이력을 등록합니다.")
    public ResponseEntity<DowntimeResponse> createDowntime(@Valid @RequestBody DowntimeCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating downtime: {} for tenant: {}", request.getDowntimeCode(), tenantId);

        DowntimeEntity downtime = toEntity(request);
        DowntimeEntity created = downtimeService.createDowntime(tenantId, downtime);
        DowntimeResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update downtime
     */
    @PutMapping("/{downtimeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "비가동 수정", description = "비가동 이력을 수정합니다.")
    public ResponseEntity<DowntimeResponse> updateDowntime(
            @PathVariable Long downtimeId,
            @Valid @RequestBody DowntimeUpdateRequest request) {
        log.info("Updating downtime ID: {}", downtimeId);

        DowntimeEntity updateData = toEntity(request);
        DowntimeEntity updated = downtimeService.updateDowntime(downtimeId, updateData);
        DowntimeResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * End downtime
     */
    @PatchMapping("/{downtimeId}/end")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "비가동 종료", description = "비가동을 종료합니다.")
    public ResponseEntity<DowntimeResponse> endDowntime(@PathVariable Long downtimeId) {
        log.info("Ending downtime ID: {}", downtimeId);

        DowntimeEntity ended = downtimeService.endDowntime(downtimeId);
        DowntimeResponse response = toResponse(ended);

        return ResponseEntity.ok(response);
    }

    /**
     * Resolve downtime
     */
    @PatchMapping("/{downtimeId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "비가동 해결", description = "비가동을 해결 처리합니다.")
    public ResponseEntity<DowntimeResponse> resolveDowntime(@PathVariable Long downtimeId) {
        log.info("Resolving downtime ID: {}", downtimeId);

        DowntimeEntity resolved = downtimeService.resolveDowntime(downtimeId);
        DowntimeResponse response = toResponse(resolved);

        return ResponseEntity.ok(response);
    }

    /**
     * Activate downtime
     */
    @PatchMapping("/{downtimeId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "비가동 활성화", description = "비가동을 활성화합니다.")
    public ResponseEntity<DowntimeResponse> activate(@PathVariable Long downtimeId) {
        log.info("Activating downtime ID: {}", downtimeId);

        DowntimeEntity activated = downtimeService.activate(downtimeId);
        DowntimeResponse response = toResponse(activated);

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate downtime
     */
    @PatchMapping("/{downtimeId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "비가동 비활성화", description = "비가동을 비활성화합니다.")
    public ResponseEntity<DowntimeResponse> deactivate(@PathVariable Long downtimeId) {
        log.info("Deactivating downtime ID: {}", downtimeId);

        DowntimeEntity deactivated = downtimeService.deactivate(downtimeId);
        DowntimeResponse response = toResponse(deactivated);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete downtime
     */
    @DeleteMapping("/{downtimeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "비가동 삭제", description = "비가동 이력을 삭제합니다.")
    public ResponseEntity<Void> deleteDowntime(@PathVariable Long downtimeId) {
        log.info("Deleting downtime ID: {}", downtimeId);

        downtimeService.deleteDowntime(downtimeId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert DowntimeCreateRequest to Entity
     */
    private DowntimeEntity toEntity(DowntimeCreateRequest request) {
        return DowntimeEntity.builder()
                .downtimeCode(request.getDowntimeCode())
                .downtimeType(request.getDowntimeType())
                .downtimeCategory(request.getDowntimeCategory())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .responsibleName(request.getResponsibleName())
                .cause(request.getCause())
                .countermeasure(request.getCountermeasure())
                .preventiveAction(request.getPreventiveAction())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert DowntimeUpdateRequest to Entity
     */
    private DowntimeEntity toEntity(DowntimeUpdateRequest request) {
        return DowntimeEntity.builder()
                .endTime(request.getEndTime())
                .downtimeType(request.getDowntimeType())
                .downtimeCategory(request.getDowntimeCategory())
                .cause(request.getCause())
                .countermeasure(request.getCountermeasure())
                .preventiveAction(request.getPreventiveAction())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to DowntimeResponse
     */
    private DowntimeResponse toResponse(DowntimeEntity entity) {
        return DowntimeResponse.builder()
                .downtimeId(entity.getDowntimeId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .equipmentId(entity.getEquipment().getEquipmentId())
                .equipmentCode(entity.getEquipment().getEquipmentCode())
                .equipmentName(entity.getEquipment().getEquipmentName())
                .downtimeCode(entity.getDowntimeCode())
                .downtimeType(entity.getDowntimeType())
                .downtimeCategory(entity.getDowntimeCategory())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationMinutes(entity.getDurationMinutes())
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderNo() : null)
                .operationId(entity.getOperation() != null ? entity.getOperation().getOperationId() : null)
                .responsibleUserId(entity.getResponsibleUser() != null ? entity.getResponsibleUser().getUserId() : null)
                .responsibleName(entity.getResponsibleName())
                .cause(entity.getCause())
                .countermeasure(entity.getCountermeasure())
                .preventiveAction(entity.getPreventiveAction())
                .isResolved(entity.getIsResolved())
                .resolvedAt(entity.getResolvedAt())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
