package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.InspectionActionCreateRequest;
import kr.co.softice.mes.common.dto.equipment.InspectionActionUpdateRequest;
import kr.co.softice.mes.common.dto.equipment.InspectionActionResponse;
import kr.co.softice.mes.domain.entity.InspectionActionEntity;
import kr.co.softice.mes.domain.service.InspectionActionService;
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
 * Inspection Action Controller
 * 점검 조치 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/inspection-actions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "InspectionAction", description = "점검 조치 API")
public class InspectionActionController {

    private final InspectionActionService inspectionActionService;

    /**
     * Get all inspection actions
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검 조치 목록 조회", description = "모든 점검 조치를 조회합니다.")
    public ResponseEntity<List<InspectionActionResponse>> getAllActions() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all inspection actions for tenant: {}", tenantId);

        List<InspectionActionEntity> actions = inspectionActionService.getAllActions(tenantId);
        List<InspectionActionResponse> response = actions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspection action by ID
     */
    @GetMapping("/{actionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검 조치 상세 조회", description = "ID로 점검 조치를 조회합니다.")
    public ResponseEntity<InspectionActionResponse> getActionById(@PathVariable Long actionId) {
        log.info("Getting inspection action by ID: {}", actionId);

        InspectionActionEntity action = inspectionActionService.getActionById(actionId);
        InspectionActionResponse response = toResponse(action);

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspection actions by inspection ID
     */
    @GetMapping("/inspection/{inspectionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검별 조치 조회", description = "특정 점검의 조치를 조회합니다.")
    public ResponseEntity<List<InspectionActionResponse>> getActionsByInspection(@PathVariable Long inspectionId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting inspection actions for inspection ID: {} tenant: {}", inspectionId, tenantId);

        List<InspectionActionEntity> actions = inspectionActionService.getActionsByInspection(inspectionId);
        List<InspectionActionResponse> response = actions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspection actions by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 조치 조회", description = "특정 상태의 점검 조치를 조회합니다.")
    public ResponseEntity<List<InspectionActionResponse>> getActionsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting inspection actions by status: {} for tenant: {}", status, tenantId);

        List<InspectionActionEntity> actions = inspectionActionService.getActionsByStatus(tenantId, status);
        List<InspectionActionResponse> response = actions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create inspection action
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 조치 등록", description = "새로운 점검 조치를 등록합니다.")
    public ResponseEntity<InspectionActionResponse> createAction(@Valid @RequestBody InspectionActionCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating inspection action for inspection ID: {} tenant: {}", request.getInspectionId(), tenantId);

        InspectionActionEntity action = toEntity(request);
        InspectionActionEntity created = inspectionActionService.createAction(
                tenantId, action,
                request.getInspectionId(),
                request.getAssignedUserId());
        InspectionActionResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update inspection action
     */
    @PutMapping("/{actionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 조치 수정", description = "점검 조치 정보를 수정합니다.")
    public ResponseEntity<InspectionActionResponse> updateAction(
            @PathVariable Long actionId,
            @Valid @RequestBody InspectionActionUpdateRequest request) {
        log.info("Updating inspection action ID: {}", actionId);

        InspectionActionEntity updateData = toEntity(request);
        InspectionActionEntity updated = inspectionActionService.updateAction(
                actionId, updateData,
                request.getAssignedUserId());
        InspectionActionResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete inspection action
     */
    @DeleteMapping("/{actionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 조치 삭제", description = "점검 조치를 삭제합니다.")
    public ResponseEntity<Void> deleteAction(@PathVariable Long actionId) {
        log.info("Deleting inspection action ID: {}", actionId);

        inspectionActionService.deleteAction(actionId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert InspectionActionCreateRequest to Entity
     */
    private InspectionActionEntity toEntity(InspectionActionCreateRequest request) {
        return InspectionActionEntity.builder()
                .actionType(request.getActionType())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert InspectionActionUpdateRequest to Entity
     */
    private InspectionActionEntity toEntity(InspectionActionUpdateRequest request) {
        return InspectionActionEntity.builder()
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .completedDate(request.getCompletedDate())
                .status(request.getStatus())
                .result(request.getResult())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to InspectionActionResponse
     */
    private InspectionActionResponse toResponse(InspectionActionEntity entity) {
        return InspectionActionResponse.builder()
                .actionId(entity.getActionId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .inspectionId(entity.getInspection() != null ? entity.getInspection().getInspectionId() : null)
                .inspectionNo(entity.getInspection() != null ? entity.getInspection().getInspectionNo() : null)
                .actionType(entity.getActionType())
                .description(entity.getDescription())
                .assignedUserId(entity.getAssignedUser() != null ? entity.getAssignedUser().getUserId() : null)
                .assignedUserName(entity.getAssignedUser() != null ? entity.getAssignedUser().getFullName() : null)
                .dueDate(entity.getDueDate())
                .completedDate(entity.getCompletedDate())
                .status(entity.getStatus())
                .result(entity.getResult())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
