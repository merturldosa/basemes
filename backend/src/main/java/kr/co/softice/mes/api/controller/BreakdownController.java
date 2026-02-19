package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.BreakdownCreateRequest;
import kr.co.softice.mes.common.dto.equipment.BreakdownResponse;
import kr.co.softice.mes.common.dto.equipment.BreakdownUpdateRequest;
import kr.co.softice.mes.domain.entity.BreakdownEntity;
import kr.co.softice.mes.domain.service.BreakdownService;
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
 * Breakdown Controller
 * 고장 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/breakdowns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Breakdown", description = "고장 관리 API")
public class BreakdownController {

    private final BreakdownService breakdownService;

    /**
     * Get all breakdowns
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "고장 목록 조회", description = "모든 고장 이력을 조회합니다.")
    public ResponseEntity<List<BreakdownResponse>> getAll() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all breakdowns for tenant: {}", tenantId);

        List<BreakdownEntity> breakdowns = breakdownService.getAllBreakdowns(tenantId);
        List<BreakdownResponse> response = breakdowns.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get breakdown by ID
     */
    @GetMapping("/{breakdownId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "고장 상세 조회", description = "ID로 고장 이력을 조회합니다.")
    public ResponseEntity<BreakdownResponse> getById(@PathVariable Long breakdownId) {
        log.info("Getting breakdown by ID: {}", breakdownId);

        BreakdownEntity breakdown = breakdownService.getBreakdownById(breakdownId);
        BreakdownResponse response = toResponse(breakdown);

        return ResponseEntity.ok(response);
    }

    /**
     * Get breakdowns by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 고장 조회", description = "특정 상태의 고장 이력을 조회합니다.")
    public ResponseEntity<List<BreakdownResponse>> getByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting breakdowns by status: {} for tenant: {}", status, tenantId);

        List<BreakdownEntity> breakdowns = breakdownService.getByStatus(tenantId, status);
        List<BreakdownResponse> response = breakdowns.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get breakdowns by equipment
     */
    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비별 고장 조회", description = "특정 설비의 고장 이력을 조회합니다.")
    public ResponseEntity<List<BreakdownResponse>> getByEquipment(@PathVariable Long equipmentId) {
        log.info("Getting breakdowns for equipment ID: {}", equipmentId);

        List<BreakdownEntity> breakdowns = breakdownService.getByEquipment(equipmentId);
        List<BreakdownResponse> response = breakdowns.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create breakdown
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "고장 등록", description = "새로운 고장 이력을 등록합니다.")
    public ResponseEntity<BreakdownResponse> create(@Valid @RequestBody BreakdownCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating breakdown: {} for tenant: {}", request.getBreakdownNo(), tenantId);

        BreakdownEntity breakdown = toEntity(request);
        BreakdownEntity created = breakdownService.createBreakdown(
                tenantId, breakdown,
                request.getEquipmentId(),
                request.getDowntimeId(),
                request.getReportedByUserId());
        BreakdownResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update breakdown
     */
    @PutMapping("/{breakdownId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "고장 수정", description = "고장 이력을 수정합니다.")
    public ResponseEntity<BreakdownResponse> update(
            @PathVariable Long breakdownId,
            @Valid @RequestBody BreakdownUpdateRequest request) {
        log.info("Updating breakdown ID: {}", breakdownId);

        BreakdownEntity updateData = toEntity(request);
        BreakdownEntity updated = breakdownService.updateBreakdown(
                breakdownId, updateData, request.getAssignedUserId());
        BreakdownResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Change breakdown status
     */
    @PatchMapping("/{breakdownId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "고장 상태 변경", description = "고장 상태를 변경합니다.")
    public ResponseEntity<BreakdownResponse> changeStatus(
            @PathVariable Long breakdownId,
            @RequestParam String status) {
        log.info("Changing breakdown ID: {} status to: {}", breakdownId, status);

        BreakdownEntity updated = breakdownService.changeStatus(breakdownId, status);
        BreakdownResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete breakdown
     */
    @DeleteMapping("/{breakdownId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "고장 삭제", description = "고장 이력을 삭제합니다.")
    public ResponseEntity<Void> delete(@PathVariable Long breakdownId) {
        log.info("Deleting breakdown ID: {}", breakdownId);

        breakdownService.deleteBreakdown(breakdownId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert BreakdownCreateRequest to Entity
     */
    private BreakdownEntity toEntity(BreakdownCreateRequest request) {
        return BreakdownEntity.builder()
                .breakdownNo(request.getBreakdownNo())
                .reportedAt(request.getReportedAt())
                .failureType(request.getFailureType())
                .severity(request.getSeverity())
                .description(request.getDescription())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert BreakdownUpdateRequest to Entity
     */
    private BreakdownEntity toEntity(BreakdownUpdateRequest request) {
        return BreakdownEntity.builder()
                .failureType(request.getFailureType())
                .severity(request.getSeverity())
                .description(request.getDescription())
                .repairDescription(request.getRepairDescription())
                .partsUsed(request.getPartsUsed())
                .repairCost(request.getRepairCost())
                .rootCause(request.getRootCause())
                .preventiveAction(request.getPreventiveAction())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to BreakdownResponse
     */
    private BreakdownResponse toResponse(BreakdownEntity entity) {
        return BreakdownResponse.builder()
                .breakdownId(entity.getBreakdownId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .breakdownNo(entity.getBreakdownNo())
                .equipmentId(entity.getEquipment().getEquipmentId())
                .equipmentCode(entity.getEquipment().getEquipmentCode())
                .equipmentName(entity.getEquipment().getEquipmentName())
                .downtimeId(entity.getDowntime() != null ? entity.getDowntime().getDowntimeId() : null)
                .reportedAt(entity.getReportedAt())
                .reportedByUserId(entity.getReportedByUser() != null ? entity.getReportedByUser().getUserId() : null)
                .reportedByUserName(entity.getReportedByUser() != null ? entity.getReportedByUser().getFullName() : null)
                .failureType(entity.getFailureType())
                .severity(entity.getSeverity())
                .description(entity.getDescription())
                .assignedUserId(entity.getAssignedUser() != null ? entity.getAssignedUser().getUserId() : null)
                .assignedUserName(entity.getAssignedUser() != null ? entity.getAssignedUser().getFullName() : null)
                .assignedAt(entity.getAssignedAt())
                .repairStartedAt(entity.getRepairStartedAt())
                .repairCompletedAt(entity.getRepairCompletedAt())
                .repairDurationMinutes(entity.getRepairDurationMinutes())
                .repairDescription(entity.getRepairDescription())
                .partsUsed(entity.getPartsUsed())
                .repairCost(entity.getRepairCost())
                .rootCause(entity.getRootCause())
                .preventiveAction(entity.getPreventiveAction())
                .status(entity.getStatus())
                .closedAt(entity.getClosedAt())
                .closedByUserId(entity.getClosedByUser() != null ? entity.getClosedByUser().getUserId() : null)
                .closedByUserName(entity.getClosedByUser() != null ? entity.getClosedByUser().getFullName() : null)
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
