package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.EquipmentCreateRequest;
import kr.co.softice.mes.common.dto.equipment.EquipmentResponse;
import kr.co.softice.mes.common.dto.equipment.EquipmentUpdateRequest;
import kr.co.softice.mes.domain.entity.EquipmentEntity;
import kr.co.softice.mes.domain.service.EquipmentService;
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
 * Equipment Controller
 * 설비 마스터 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment", description = "설비 관리 API")
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * Get all equipments
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비 목록 조회", description = "모든 설비를 조회합니다.")
    public ResponseEntity<List<EquipmentResponse>> getAllEquipments() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all equipments for tenant: {}", tenantId);

        List<EquipmentEntity> equipments = equipmentService.getAllEquipments(tenantId);
        List<EquipmentResponse> response = equipments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get active equipments
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 설비 목록 조회", description = "활성 상태의 설비를 조회합니다.")
    public ResponseEntity<List<EquipmentResponse>> getActiveEquipments() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active equipments for tenant: {}", tenantId);

        List<EquipmentEntity> equipments = equipmentService.getActiveEquipments(tenantId);
        List<EquipmentResponse> response = equipments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get equipment by ID
     */
    @GetMapping("/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비 상세 조회", description = "ID로 설비를 조회합니다.")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long equipmentId) {
        log.info("Getting equipment by ID: {}", equipmentId);

        EquipmentEntity equipment = equipmentService.getEquipmentById(equipmentId);
        EquipmentResponse response = toResponse(equipment);

        return ResponseEntity.ok(response);
    }

    /**
     * Get equipments by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 설비 조회", description = "특정 상태의 설비를 조회합니다.")
    public ResponseEntity<List<EquipmentResponse>> getEquipmentsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting equipments by status: {} for tenant: {}", status, tenantId);

        List<EquipmentEntity> equipments = equipmentService.getEquipmentsByStatus(tenantId, status);
        List<EquipmentResponse> response = equipments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get equipments by type
     */
    @GetMapping("/type/{equipmentType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "유형별 설비 조회", description = "특정 유형의 설비를 조회합니다.")
    public ResponseEntity<List<EquipmentResponse>> getEquipmentsByType(@PathVariable String equipmentType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting equipments by type: {} for tenant: {}", equipmentType, tenantId);

        List<EquipmentEntity> equipments = equipmentService.getEquipmentsByType(tenantId, equipmentType);
        List<EquipmentResponse> response = equipments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create equipment
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "설비 등록", description = "새로운 설비를 등록합니다.")
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid @RequestBody EquipmentCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating equipment: {} for tenant: {}", request.getEquipmentCode(), tenantId);

        EquipmentEntity equipment = toEntity(request);
        EquipmentEntity created = equipmentService.createEquipment(tenantId, equipment);
        EquipmentResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update equipment
     */
    @PutMapping("/{equipmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "설비 수정", description = "설비 정보를 수정합니다.")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable Long equipmentId,
            @Valid @RequestBody EquipmentUpdateRequest request) {
        log.info("Updating equipment ID: {}", equipmentId);

        EquipmentEntity updateData = toEntity(request);
        EquipmentEntity updated = equipmentService.updateEquipment(equipmentId, updateData);
        EquipmentResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Change equipment status
     */
    @PatchMapping("/{equipmentId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "설비 상태 변경", description = "설비의 상태를 변경합니다.")
    public ResponseEntity<EquipmentResponse> changeStatus(
            @PathVariable Long equipmentId,
            @RequestParam String status) {
        log.info("Changing equipment ID: {} status to: {}", equipmentId, status);

        EquipmentEntity updated = equipmentService.changeStatus(equipmentId, status);
        EquipmentResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Activate equipment
     */
    @PatchMapping("/{equipmentId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "설비 활성화", description = "설비를 활성화합니다.")
    public ResponseEntity<EquipmentResponse> activate(@PathVariable Long equipmentId) {
        log.info("Activating equipment ID: {}", equipmentId);

        EquipmentEntity activated = equipmentService.activate(equipmentId);
        EquipmentResponse response = toResponse(activated);

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate equipment
     */
    @PatchMapping("/{equipmentId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "설비 비활성화", description = "설비를 비활성화합니다.")
    public ResponseEntity<EquipmentResponse> deactivate(@PathVariable Long equipmentId) {
        log.info("Deactivating equipment ID: {}", equipmentId);

        EquipmentEntity deactivated = equipmentService.deactivate(equipmentId);
        EquipmentResponse response = toResponse(deactivated);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete equipment
     */
    @DeleteMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "설비 삭제", description = "설비를 삭제합니다.")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long equipmentId) {
        log.info("Deleting equipment ID: {}", equipmentId);

        equipmentService.deleteEquipment(equipmentId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert EquipmentCreateRequest to Entity
     */
    private EquipmentEntity toEntity(EquipmentCreateRequest request) {
        return EquipmentEntity.builder()
                .equipmentCode(request.getEquipmentCode())
                .equipmentName(request.getEquipmentName())
                .equipmentType(request.getEquipmentType())
                .equipmentCategory(request.getEquipmentCategory())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .serialNo(request.getSerialNo())
                .location(request.getLocation())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .capacity(request.getCapacity())
                .powerRating(request.getPowerRating())
                .weight(request.getWeight())
                .status(request.getStatus())
                .maintenanceCycleDays(request.getMaintenanceCycleDays())
                .lastMaintenanceDate(request.getLastMaintenanceDate())
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .standardCycleTime(request.getStandardCycleTime())
                .actualOeeTarget(request.getActualOeeTarget())
                .warrantyEndDate(request.getWarrantyExpiryDate())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert EquipmentUpdateRequest to Entity
     */
    private EquipmentEntity toEntity(EquipmentUpdateRequest request) {
        return EquipmentEntity.builder()
                .equipmentName(request.getEquipmentName())
                .equipmentType(request.getEquipmentType())
                .equipmentCategory(request.getEquipmentCategory())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .serialNo(request.getSerialNo())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .powerRating(request.getPowerRating())
                .weight(request.getWeight())
                .status(request.getStatus())
                .maintenanceCycleDays(request.getMaintenanceCycleDays())
                .lastMaintenanceDate(request.getLastMaintenanceDate())
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .standardCycleTime(request.getStandardCycleTime())
                .actualOeeTarget(request.getActualOeeTarget())
                .warrantyEndDate(request.getWarrantyExpiryDate())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to EquipmentResponse
     */
    private EquipmentResponse toResponse(EquipmentEntity entity) {
        return EquipmentResponse.builder()
                .equipmentId(entity.getEquipmentId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .equipmentCode(entity.getEquipmentCode())
                .equipmentName(entity.getEquipmentName())
                .equipmentType(entity.getEquipmentType())
                .equipmentCategory(entity.getEquipmentCategory())
                .siteId(entity.getSite() != null ? entity.getSite().getSiteId() : null)
                .siteCode(entity.getSite() != null ? entity.getSite().getSiteCode() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getSiteName() : null)
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getDepartmentId() : null)
                .departmentCode(entity.getDepartment() != null ? entity.getDepartment().getDepartmentCode() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getDepartmentName() : null)
                .manufacturer(entity.getManufacturer())
                .modelName(entity.getModelName())
                .serialNo(entity.getSerialNo())
                .location(entity.getLocation())
                .purchaseDate(entity.getPurchaseDate())
                .purchasePrice(entity.getPurchasePrice())
                .capacity(entity.getCapacity())
                .powerRating(entity.getPowerRating())
                .weight(entity.getWeight())
                .status(entity.getStatus())
                .maintenanceCycleDays(entity.getMaintenanceCycleDays())
                .lastMaintenanceDate(entity.getLastMaintenanceDate())
                .nextMaintenanceDate(entity.getNextMaintenanceDate())
                .standardCycleTime(entity.getStandardCycleTime())
                .actualOeeTarget(entity.getActualOeeTarget())
                .warrantyExpiryDate(entity.getWarrantyEndDate())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
