package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.mold.MoldMaintenanceCreateRequest;
import kr.co.softice.mes.common.dto.mold.MoldMaintenanceResponse;
import kr.co.softice.mes.common.dto.mold.MoldMaintenanceUpdateRequest;
import kr.co.softice.mes.domain.entity.MoldEntity;
import kr.co.softice.mes.domain.entity.MoldMaintenanceEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.service.MoldMaintenanceService;
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
 * Mold Maintenance Controller
 * 금형 보전 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/mold-maintenances")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mold Maintenance", description = "금형 보전 관리 API")
public class MoldMaintenanceController {

    private final MoldMaintenanceService maintenanceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "금형 보전 목록 조회", description = "모든 금형 보전 이력을 조회합니다.")
    public ResponseEntity<List<MoldMaintenanceResponse>> getAllMaintenances() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all mold maintenances for tenant: {}", tenantId);

        List<MoldMaintenanceEntity> maintenances = maintenanceService.getAllMaintenances(tenantId);
        List<MoldMaintenanceResponse> response = maintenances.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{maintenanceId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "금형 보전 상세 조회", description = "ID로 금형 보전 이력을 조회합니다.")
    public ResponseEntity<MoldMaintenanceResponse> getMaintenanceById(@PathVariable Long maintenanceId) {
        log.info("Getting mold maintenance by ID: {}", maintenanceId);

        MoldMaintenanceEntity maintenance = maintenanceService.getMaintenanceById(maintenanceId);
        MoldMaintenanceResponse response = toResponse(maintenance);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mold/{moldId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "금형별 보전 이력 조회", description = "특정 금형의 보전 이력을 조회합니다.")
    public ResponseEntity<List<MoldMaintenanceResponse>> getMaintenancesByMold(@PathVariable Long moldId) {
        log.info("Getting maintenances for mold ID: {}", moldId);

        List<MoldMaintenanceEntity> maintenances = maintenanceService.getMaintenancesByMold(moldId);
        List<MoldMaintenanceResponse> response = maintenances.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{maintenanceType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "보전 유형별 조회", description = "특정 보전 유형의 이력을 조회합니다.")
    public ResponseEntity<List<MoldMaintenanceResponse>> getMaintenancesByType(@PathVariable String maintenanceType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting maintenances by type: {} for tenant: {}", maintenanceType, tenantId);

        List<MoldMaintenanceEntity> maintenances = maintenanceService.getMaintenancesByType(tenantId, maintenanceType);
        List<MoldMaintenanceResponse> response = maintenances.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기간별 보전 이력 조회", description = "특정 기간의 보전 이력을 조회합니다.")
    public ResponseEntity<List<MoldMaintenanceResponse>> getMaintenancesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting maintenances for tenant: {} from {} to {}", tenantId, startDate, endDate);

        List<MoldMaintenanceEntity> maintenances = maintenanceService.getMaintenancesByDateRange(tenantId, startDate, endDate);
        List<MoldMaintenanceResponse> response = maintenances.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "금형 보전 등록", description = "새로운 금형 보전 이력을 등록합니다.")
    public ResponseEntity<MoldMaintenanceResponse> createMaintenance(@Valid @RequestBody MoldMaintenanceCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating mold maintenance: {} for tenant: {}", request.getMaintenanceNo(), tenantId);

        MoldMaintenanceEntity maintenance = toEntity(request);
        MoldMaintenanceEntity created = maintenanceService.createMaintenance(tenantId, maintenance);
        MoldMaintenanceResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{maintenanceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "금형 보전 수정", description = "금형 보전 이력을 수정합니다.")
    public ResponseEntity<MoldMaintenanceResponse> updateMaintenance(
            @PathVariable Long maintenanceId,
            @Valid @RequestBody MoldMaintenanceUpdateRequest request) {
        log.info("Updating mold maintenance ID: {}", maintenanceId);

        MoldMaintenanceEntity updateData = toEntity(request);
        MoldMaintenanceEntity updated = maintenanceService.updateMaintenance(maintenanceId, updateData);
        MoldMaintenanceResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{maintenanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "금형 보전 삭제", description = "금형 보전 이력을 삭제합니다.")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable Long maintenanceId) {
        log.info("Deleting mold maintenance ID: {}", maintenanceId);

        maintenanceService.deleteMaintenance(maintenanceId);

        return ResponseEntity.ok().build();
    }

    private MoldMaintenanceEntity toEntity(MoldMaintenanceCreateRequest request) {
        MoldMaintenanceEntity.MoldMaintenanceEntityBuilder builder = MoldMaintenanceEntity.builder()
                .maintenanceNo(request.getMaintenanceNo())
                .maintenanceType(request.getMaintenanceType())
                .maintenanceDate(request.getMaintenanceDate())
                .shotCountBefore(request.getShotCountBefore())
                .shotCountReset(request.getShotCountReset())
                .shotCountAfter(request.getShotCountAfter())
                .maintenanceContent(request.getMaintenanceContent())
                .partsReplaced(request.getPartsReplaced())
                .findings(request.getFindings())
                .correctiveAction(request.getCorrectiveAction())
                .partsCost(request.getPartsCost())
                .laborCost(request.getLaborCost())
                .laborHours(request.getLaborHours())
                .maintenanceResult(request.getMaintenanceResult())
                .technicianName(request.getTechnicianName())
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .remarks(request.getRemarks());

        if (request.getMoldId() != null) {
            builder.mold(MoldEntity.builder().moldId(request.getMoldId()).build());
        }
        if (request.getTechnicianUserId() != null) {
            builder.technicianUser(UserEntity.builder().userId(request.getTechnicianUserId()).build());
        }

        return builder.build();
    }

    private MoldMaintenanceEntity toEntity(MoldMaintenanceUpdateRequest request) {
        return MoldMaintenanceEntity.builder()
                .maintenanceType(request.getMaintenanceType())
                .maintenanceDate(request.getMaintenanceDate())
                .shotCountBefore(request.getShotCountBefore())
                .shotCountReset(request.getShotCountReset())
                .shotCountAfter(request.getShotCountAfter())
                .maintenanceContent(request.getMaintenanceContent())
                .partsReplaced(request.getPartsReplaced())
                .findings(request.getFindings())
                .correctiveAction(request.getCorrectiveAction())
                .partsCost(request.getPartsCost())
                .laborCost(request.getLaborCost())
                .laborHours(request.getLaborHours())
                .maintenanceResult(request.getMaintenanceResult())
                .technicianName(request.getTechnicianName())
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .remarks(request.getRemarks())
                .build();
    }

    private MoldMaintenanceResponse toResponse(MoldMaintenanceEntity entity) {
        return MoldMaintenanceResponse.builder()
                .maintenanceId(entity.getMaintenanceId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .moldId(entity.getMold().getMoldId())
                .moldCode(entity.getMold().getMoldCode())
                .moldName(entity.getMold().getMoldName())
                .maintenanceNo(entity.getMaintenanceNo())
                .maintenanceType(entity.getMaintenanceType())
                .maintenanceDate(entity.getMaintenanceDate())
                .shotCountBefore(entity.getShotCountBefore())
                .shotCountAfter(entity.getShotCountAfter())
                .shotCountReset(entity.getShotCountReset())
                .maintenanceContent(entity.getMaintenanceContent())
                .partsReplaced(entity.getPartsReplaced())
                .findings(entity.getFindings())
                .correctiveAction(entity.getCorrectiveAction())
                .partsCost(entity.getPartsCost())
                .laborCost(entity.getLaborCost())
                .totalCost(entity.getTotalCost())
                .laborHours(entity.getLaborHours())
                .maintenanceResult(entity.getMaintenanceResult())
                .technicianUserId(entity.getTechnicianUser() != null ? entity.getTechnicianUser().getUserId() : null)
                .technicianUsername(entity.getTechnicianUser() != null ? entity.getTechnicianUser().getUsername() : null)
                .technicianName(entity.getTechnicianName())
                .nextMaintenanceDate(entity.getNextMaintenanceDate())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
