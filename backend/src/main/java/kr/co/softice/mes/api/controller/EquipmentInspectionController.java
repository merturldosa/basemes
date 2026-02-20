package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.EquipmentInspectionCreateRequest;
import kr.co.softice.mes.common.dto.equipment.EquipmentInspectionResponse;
import kr.co.softice.mes.common.dto.equipment.EquipmentInspectionUpdateRequest;
import kr.co.softice.mes.domain.entity.EquipmentInspectionEntity;
import kr.co.softice.mes.domain.service.EquipmentInspectionService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Equipment Inspection Controller
 * 설비 점검 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/equipment-inspections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment Inspection", description = "설비 점검 관리 API")
public class EquipmentInspectionController {

    private final EquipmentInspectionService inspectionService;

    /**
     * Get all inspections
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비 점검 목록 조회", description = "모든 설비 점검을 조회합니다.")
    public ResponseEntity<List<EquipmentInspectionResponse>> getAllInspections() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all inspections for tenant: {}", tenantId);

        List<EquipmentInspectionEntity> inspections = inspectionService.getAllInspections(tenantId);
        List<EquipmentInspectionResponse> response = inspections.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspection by ID
     */
    @Transactional(readOnly = true)
    @GetMapping("/{inspectionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비 점검 상세 조회", description = "ID로 설비 점검을 조회합니다.")
    public ResponseEntity<EquipmentInspectionResponse> getInspectionById(@PathVariable Long inspectionId) {
        log.info("Getting inspection by ID: {}", inspectionId);

        EquipmentInspectionEntity inspection = inspectionService.getInspectionById(inspectionId);
        EquipmentInspectionResponse response = toResponse(inspection);

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspections by equipment
     */
    @Transactional(readOnly = true)
    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비별 점검 이력 조회", description = "특정 설비의 점검 이력을 조회합니다.")
    public ResponseEntity<List<EquipmentInspectionResponse>> getInspectionsByEquipment(@PathVariable Long equipmentId) {
        log.info("Getting inspections for equipment ID: {}", equipmentId);

        List<EquipmentInspectionEntity> inspections = inspectionService.getInspectionsByEquipment(equipmentId);
        List<EquipmentInspectionResponse> response = inspections.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspections by type
     */
    @Transactional(readOnly = true)
    @GetMapping("/type/{inspectionType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검 유형별 조회", description = "특정 유형의 점검을 조회합니다.")
    public ResponseEntity<List<EquipmentInspectionResponse>> getInspectionsByType(@PathVariable String inspectionType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting inspections by type: {} for tenant: {}", inspectionType, tenantId);

        List<EquipmentInspectionEntity> inspections = inspectionService.getInspectionsByType(tenantId, inspectionType);
        List<EquipmentInspectionResponse> response = inspections.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspections by result
     */
    @Transactional(readOnly = true)
    @GetMapping("/result/{inspectionResult}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검 결과별 조회", description = "특정 결과의 점검을 조회합니다.")
    public ResponseEntity<List<EquipmentInspectionResponse>> getInspectionsByResult(@PathVariable String inspectionResult) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting inspections by result: {} for tenant: {}", inspectionResult, tenantId);

        List<EquipmentInspectionEntity> inspections = inspectionService.getInspectionsByResult(tenantId, inspectionResult);
        List<EquipmentInspectionResponse> response = inspections.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create inspection
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'MAINTENANCE_STAFF')")
    @Operation(summary = "설비 점검 등록", description = "새로운 설비 점검을 등록합니다.")
    public ResponseEntity<EquipmentInspectionResponse> createInspection(@Valid @RequestBody EquipmentInspectionCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating inspection: {} for tenant: {}", request.getInspectionNo(), tenantId);

        EquipmentInspectionEntity inspection = toEntity(request);
        EquipmentInspectionEntity created = inspectionService.createInspection(tenantId, inspection);
        EquipmentInspectionResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update inspection
     */
    @PutMapping("/{inspectionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'MAINTENANCE_STAFF')")
    @Operation(summary = "설비 점검 수정", description = "설비 점검 정보를 수정합니다.")
    public ResponseEntity<EquipmentInspectionResponse> updateInspection(
            @PathVariable Long inspectionId,
            @Valid @RequestBody EquipmentInspectionUpdateRequest request) {
        log.info("Updating inspection ID: {}", inspectionId);

        EquipmentInspectionEntity updateData = toEntity(request);
        EquipmentInspectionEntity updated = inspectionService.updateInspection(inspectionId, updateData);
        EquipmentInspectionResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Complete inspection with corrective action
     */
    @PatchMapping("/{inspectionId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'MAINTENANCE_STAFF')")
    @Operation(summary = "설비 점검 완료", description = "설비 점검을 완료합니다.")
    public ResponseEntity<EquipmentInspectionResponse> completeInspection(@PathVariable Long inspectionId) {
        log.info("Completing inspection ID: {}", inspectionId);

        EquipmentInspectionEntity completed = inspectionService.completeInspection(inspectionId);
        EquipmentInspectionResponse response = toResponse(completed);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete inspection
     */
    @DeleteMapping("/{inspectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "설비 점검 삭제", description = "설비 점검을 삭제합니다.")
    public ResponseEntity<Void> deleteInspection(@PathVariable Long inspectionId) {
        log.info("Deleting inspection ID: {}", inspectionId);

        inspectionService.deleteInspection(inspectionId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert EquipmentInspectionCreateRequest to Entity
     */
    private EquipmentInspectionEntity toEntity(EquipmentInspectionCreateRequest request) {
        return EquipmentInspectionEntity.builder()
                .inspectionNo(request.getInspectionNo())
                .inspectionType(request.getInspectionType())
                .inspectionDate(request.getInspectionDate())
                .inspectionResult(request.getInspectionResult())
                .inspectorName(request.getInspectorName())
                .responsibleUserName(request.getResponsibleUserName())
                .findings(request.getFindings())
                .abnormalityDetected(request.getAbnormalityDetected())
                .severity(request.getSeverity())
                .correctiveAction(request.getCorrectiveAction())
                .correctiveActionDate(request.getCorrectiveActionDate())
                .partsReplaced(request.getPartsReplaced())
                .partsCost(request.getPartsCost())
                .laborCost(request.getLaborCost())
                .laborHours(request.getLaborHours() != null ? new java.math.BigDecimal(request.getLaborHours()) : null)
                .nextInspectionDate(request.getNextInspectionDate())
                .nextInspectionType(request.getNextInspectionType())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert EquipmentInspectionUpdateRequest to Entity
     */
    private EquipmentInspectionEntity toEntity(EquipmentInspectionUpdateRequest request) {
        return EquipmentInspectionEntity.builder()
                .inspectionResult(request.getInspectionResult())
                .findings(request.getFindings())
                .abnormalityDetected(request.getAbnormalityDetected())
                .severity(request.getSeverity())
                .correctiveAction(request.getCorrectiveAction())
                .correctiveActionDate(request.getCorrectiveActionDate())
                .partsReplaced(request.getPartsReplaced())
                .partsCost(request.getPartsCost())
                .laborCost(request.getLaborCost())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to EquipmentInspectionResponse
     */
    private EquipmentInspectionResponse toResponse(EquipmentInspectionEntity entity) {
        return EquipmentInspectionResponse.builder()
                .inspectionId(entity.getInspectionId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .equipmentId(entity.getEquipment().getEquipmentId())
                .equipmentCode(entity.getEquipment().getEquipmentCode())
                .equipmentName(entity.getEquipment().getEquipmentName())
                .inspectionNo(entity.getInspectionNo())
                .inspectionType(entity.getInspectionType())
                .inspectionDate(entity.getInspectionDate())
                .inspectionResult(entity.getInspectionResult())
                .inspectorUserId(entity.getInspectorUser() != null ? entity.getInspectorUser().getUserId() : null)
                .inspectorName(entity.getInspectorName())
                .responsibleUserId(entity.getResponsibleUser() != null ? entity.getResponsibleUser().getUserId() : null)
                .responsibleUserName(entity.getResponsibleUserName())
                .findings(entity.getFindings())
                .abnormalityDetected(entity.getAbnormalityDetected())
                .severity(entity.getSeverity())
                .correctiveAction(entity.getCorrectiveAction())
                .correctiveActionDate(entity.getCorrectiveActionDate())
                .partsReplaced(entity.getPartsReplaced())
                .partsCost(entity.getPartsCost())
                .laborCost(entity.getLaborCost())
                .totalCost(entity.getTotalCost())
                .laborHours(entity.getLaborHours() != null ? entity.getLaborHours().intValue() : null)
                .nextInspectionDate(entity.getNextInspectionDate())
                .nextInspectionType(entity.getNextInspectionType())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
