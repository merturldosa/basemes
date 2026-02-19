package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.EquipmentPartCreateRequest;
import kr.co.softice.mes.common.dto.equipment.EquipmentPartResponse;
import kr.co.softice.mes.common.dto.equipment.EquipmentPartUpdateRequest;
import kr.co.softice.mes.domain.entity.EquipmentPartEntity;
import kr.co.softice.mes.domain.service.EquipmentPartService;
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
 * Equipment Part Controller
 * 설비 부품 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/equipment-parts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "EquipmentPart", description = "설비 부품 관리 API")
public class EquipmentPartController {

    private final EquipmentPartService equipmentPartService;

    /**
     * Get all parts
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비 부품 목록 조회", description = "모든 설비 부품을 조회합니다.")
    public ResponseEntity<List<EquipmentPartResponse>> getAllParts() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all equipment parts for tenant: {}", tenantId);

        List<EquipmentPartEntity> parts = equipmentPartService.getAllParts(tenantId);
        List<EquipmentPartResponse> response = parts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get part by ID
     */
    @GetMapping("/{partId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비 부품 상세 조회", description = "ID로 설비 부품을 조회합니다.")
    public ResponseEntity<EquipmentPartResponse> getPartById(@PathVariable Long partId) {
        log.info("Getting equipment part by ID: {}", partId);

        EquipmentPartEntity part = equipmentPartService.getPartById(partId);
        EquipmentPartResponse response = toResponse(part);

        return ResponseEntity.ok(response);
    }

    /**
     * Get parts by equipment
     */
    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비별 부품 조회", description = "특정 설비의 부품을 조회합니다.")
    public ResponseEntity<List<EquipmentPartResponse>> getPartsByEquipment(@PathVariable Long equipmentId) {
        log.info("Getting parts for equipment ID: {}", equipmentId);

        List<EquipmentPartEntity> parts = equipmentPartService.getPartsByEquipment(equipmentId);
        List<EquipmentPartResponse> response = parts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get parts needing replacement
     */
    @GetMapping("/needs-replacement")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "교체 예정 부품 조회", description = "지정 일자까지 교체가 필요한 부품을 조회합니다.")
    public ResponseEntity<List<EquipmentPartResponse>> getNeedsReplacement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting parts needing replacement by {} for tenant: {}", dueDate, tenantId);

        List<EquipmentPartEntity> parts = equipmentPartService.getNeedsReplacement(tenantId, dueDate);
        List<EquipmentPartResponse> response = parts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create part
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "설비 부품 등록", description = "새로운 설비 부품을 등록합니다.")
    public ResponseEntity<EquipmentPartResponse> createPart(@Valid @RequestBody EquipmentPartCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating equipment part: {} for tenant: {}", request.getPartCode(), tenantId);

        EquipmentPartEntity part = toEntity(request);
        EquipmentPartEntity created = equipmentPartService.createPart(tenantId, part, request.getEquipmentId());
        EquipmentPartResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update part
     */
    @PutMapping("/{partId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "설비 부품 수정", description = "설비 부품 정보를 수정합니다.")
    public ResponseEntity<EquipmentPartResponse> updatePart(
            @PathVariable Long partId,
            @Valid @RequestBody EquipmentPartUpdateRequest request) {
        log.info("Updating equipment part ID: {}", partId);

        EquipmentPartEntity updateData = toEntity(request);
        EquipmentPartEntity updated = equipmentPartService.updatePart(partId, updateData, request.getEquipmentId());
        EquipmentPartResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Record replacement
     */
    @PostMapping("/{partId}/replace")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "부품 교체 기록", description = "부품 교체를 기록합니다.")
    public ResponseEntity<EquipmentPartResponse> recordReplacement(
            @PathVariable Long partId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate replacementDate) {
        log.info("Recording replacement for part ID: {} on date: {}", partId, replacementDate);

        EquipmentPartEntity replaced = equipmentPartService.recordReplacement(partId, replacementDate);
        EquipmentPartResponse response = toResponse(replaced);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete part
     */
    @DeleteMapping("/{partId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "설비 부품 삭제", description = "설비 부품을 삭제합니다.")
    public ResponseEntity<Void> deletePart(@PathVariable Long partId) {
        log.info("Deleting equipment part ID: {}", partId);

        equipmentPartService.deletePart(partId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert EquipmentPartCreateRequest to Entity
     */
    private EquipmentPartEntity toEntity(EquipmentPartCreateRequest request) {
        return EquipmentPartEntity.builder()
                .partCode(request.getPartCode())
                .partName(request.getPartName())
                .partType(request.getPartType())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .serialNo(request.getSerialNo())
                .installationDate(request.getInstallationDate())
                .expectedLifeDays(request.getExpectedLifeDays())
                .unitPrice(request.getUnitPrice())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert EquipmentPartUpdateRequest to Entity
     */
    private EquipmentPartEntity toEntity(EquipmentPartUpdateRequest request) {
        return EquipmentPartEntity.builder()
                .partName(request.getPartName())
                .partType(request.getPartType())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .serialNo(request.getSerialNo())
                .installationDate(request.getInstallationDate())
                .expectedLifeDays(request.getExpectedLifeDays())
                .unitPrice(request.getUnitPrice())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to EquipmentPartResponse
     */
    private EquipmentPartResponse toResponse(EquipmentPartEntity entity) {
        return EquipmentPartResponse.builder()
                .partId(entity.getPartId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .equipmentId(entity.getEquipment() != null ? entity.getEquipment().getEquipmentId() : null)
                .equipmentCode(entity.getEquipment() != null ? entity.getEquipment().getEquipmentCode() : null)
                .equipmentName(entity.getEquipment() != null ? entity.getEquipment().getEquipmentName() : null)
                .partCode(entity.getPartCode())
                .partName(entity.getPartName())
                .partType(entity.getPartType())
                .manufacturer(entity.getManufacturer())
                .modelName(entity.getModelName())
                .serialNo(entity.getSerialNo())
                .installationDate(entity.getInstallationDate())
                .expectedLifeDays(entity.getExpectedLifeDays())
                .replacementDate(entity.getReplacementDate())
                .nextReplacementDate(entity.getNextReplacementDate())
                .replacementCount(entity.getReplacementCount())
                .unitPrice(entity.getUnitPrice())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
