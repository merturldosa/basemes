package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.mold.MoldCreateRequest;
import kr.co.softice.mes.common.dto.mold.MoldResponse;
import kr.co.softice.mes.common.dto.mold.MoldUpdateRequest;
import kr.co.softice.mes.domain.entity.MoldEntity;
import kr.co.softice.mes.domain.service.MoldService;
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
 * Mold Controller
 * 금형 마스터 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/molds")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mold", description = "금형 관리 API")
public class MoldController {

    private final MoldService moldService;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "금형 목록 조회", description = "모든 금형을 조회합니다.")
    public ResponseEntity<List<MoldResponse>> getAllMolds() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all molds for tenant: {}", tenantId);

        List<MoldEntity> molds = moldService.getAllMolds(tenantId);
        List<MoldResponse> response = molds.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 금형 목록 조회", description = "활성 상태의 금형을 조회합니다.")
    public ResponseEntity<List<MoldResponse>> getActiveMolds() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active molds for tenant: {}", tenantId);

        List<MoldEntity> molds = moldService.getActiveMolds(tenantId);
        List<MoldResponse> response = molds.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{moldId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "금형 상세 조회", description = "ID로 금형을 조회합니다.")
    public ResponseEntity<MoldResponse> getMoldById(@PathVariable Long moldId) {
        log.info("Getting mold by ID: {}", moldId);

        MoldEntity mold = moldService.getMoldById(moldId);
        MoldResponse response = toResponse(mold);

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 금형 조회", description = "특정 상태의 금형을 조회합니다.")
    public ResponseEntity<List<MoldResponse>> getMoldsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting molds by status: {} for tenant: {}", status, tenantId);

        List<MoldEntity> molds = moldService.getMoldsByStatus(tenantId, status);
        List<MoldResponse> response = molds.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/requiring-maintenance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "보전 필요 금형 조회", description = "보전이 필요한 금형을 조회합니다.")
    public ResponseEntity<List<MoldResponse>> getMoldsRequiringMaintenance() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting molds requiring maintenance for tenant: {}", tenantId);

        List<MoldEntity> molds = moldService.getMoldsRequiringMaintenance(tenantId);
        List<MoldResponse> response = molds.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "금형 등록", description = "새로운 금형을 등록합니다.")
    public ResponseEntity<MoldResponse> createMold(@Valid @RequestBody MoldCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating mold: {} for tenant: {}", request.getMoldCode(), tenantId);

        MoldEntity mold = toEntity(request);
        MoldEntity created = moldService.createMold(tenantId, mold);
        MoldResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{moldId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "금형 수정", description = "금형 정보를 수정합니다.")
    public ResponseEntity<MoldResponse> updateMold(
            @PathVariable Long moldId,
            @Valid @RequestBody MoldUpdateRequest request) {
        log.info("Updating mold ID: {}", moldId);

        MoldEntity updateData = toEntity(request);
        MoldEntity updated = moldService.updateMold(moldId, updateData);
        MoldResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{moldId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "금형 상태 변경", description = "금형의 상태를 변경합니다.")
    public ResponseEntity<MoldResponse> changeStatus(
            @PathVariable Long moldId,
            @RequestParam String status) {
        log.info("Changing mold ID: {} status to: {}", moldId, status);

        MoldEntity updated = moldService.changeStatus(moldId, status);
        MoldResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{moldId}/reset-shot-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "Shot 수 초기화", description = "금형의 Shot 수를 초기화합니다.")
    public ResponseEntity<MoldResponse> resetShotCount(@PathVariable Long moldId) {
        log.info("Resetting shot count for mold ID: {}", moldId);

        MoldEntity updated = moldService.resetShotCount(moldId);
        MoldResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{moldId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "금형 활성화", description = "금형을 활성화합니다.")
    public ResponseEntity<MoldResponse> activate(@PathVariable Long moldId) {
        log.info("Activating mold ID: {}", moldId);

        MoldEntity activated = moldService.activate(moldId);
        MoldResponse response = toResponse(activated);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{moldId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "금형 비활성화", description = "금형을 비활성화합니다.")
    public ResponseEntity<MoldResponse> deactivate(@PathVariable Long moldId) {
        log.info("Deactivating mold ID: {}", moldId);

        MoldEntity deactivated = moldService.deactivate(moldId);
        MoldResponse response = toResponse(deactivated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{moldId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "금형 삭제", description = "금형을 삭제합니다.")
    public ResponseEntity<Void> deleteMold(@PathVariable Long moldId) {
        log.info("Deleting mold ID: {}", moldId);

        moldService.deleteMold(moldId);

        return ResponseEntity.ok().build();
    }

    private MoldEntity toEntity(MoldCreateRequest request) {
        return MoldEntity.builder()
                .moldCode(request.getMoldCode())
                .moldName(request.getMoldName())
                .moldType(request.getMoldType())
                .moldGrade(request.getMoldGrade())
                .cavityCount(request.getCavityCount())
                .maxShotCount(request.getMaxShotCount())
                .maintenanceShotInterval(request.getMaintenanceShotInterval())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .serialNo(request.getSerialNo())
                .material(request.getMaterial())
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .manufactureDate(request.getManufactureDate())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .firstUseDate(request.getFirstUseDate())
                .warrantyPeriod(request.getWarrantyPeriod())
                .warrantyExpiryDate(request.getWarrantyExpiryDate())
                .status(request.getStatus())
                .location(request.getLocation())
                .remarks(request.getRemarks())
                .build();
    }

    private MoldEntity toEntity(MoldUpdateRequest request) {
        return MoldEntity.builder()
                .moldName(request.getMoldName())
                .moldType(request.getMoldType())
                .moldGrade(request.getMoldGrade())
                .cavityCount(request.getCavityCount())
                .maxShotCount(request.getMaxShotCount())
                .maintenanceShotInterval(request.getMaintenanceShotInterval())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .serialNo(request.getSerialNo())
                .material(request.getMaterial())
                .weight(request.getWeight())
                .status(request.getStatus())
                .location(request.getLocation())
                .remarks(request.getRemarks())
                .build();
    }

    private MoldResponse toResponse(MoldEntity entity) {
        return MoldResponse.builder()
                .moldId(entity.getMoldId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .moldCode(entity.getMoldCode())
                .moldName(entity.getMoldName())
                .moldType(entity.getMoldType())
                .moldGrade(entity.getMoldGrade())
                .cavityCount(entity.getCavityCount())
                .currentShotCount(entity.getCurrentShotCount())
                .maxShotCount(entity.getMaxShotCount())
                .maintenanceShotInterval(entity.getMaintenanceShotInterval())
                .lastMaintenanceShot(entity.getLastMaintenanceShot())
                .siteId(entity.getSite() != null ? entity.getSite().getSiteId() : null)
                .siteCode(entity.getSite() != null ? entity.getSite().getSiteCode() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getSiteName() : null)
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getDepartmentId() : null)
                .departmentCode(entity.getDepartment() != null ? entity.getDepartment().getDepartmentCode() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getDepartmentName() : null)
                .manufacturer(entity.getManufacturer())
                .modelName(entity.getModelName())
                .serialNo(entity.getSerialNo())
                .material(entity.getMaterial())
                .weight(entity.getWeight())
                .dimensions(entity.getDimensions())
                .manufactureDate(entity.getManufactureDate())
                .purchaseDate(entity.getPurchaseDate())
                .purchasePrice(entity.getPurchasePrice())
                .firstUseDate(entity.getFirstUseDate())
                .warrantyPeriod(entity.getWarrantyPeriod())
                .warrantyExpiryDate(entity.getWarrantyExpiryDate())
                .status(entity.getStatus())
                .location(entity.getLocation())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
