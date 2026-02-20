package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.EquipmentOperationCreateRequest;
import kr.co.softice.mes.common.dto.equipment.EquipmentOperationResponse;
import kr.co.softice.mes.common.dto.equipment.EquipmentOperationUpdateRequest;
import kr.co.softice.mes.domain.entity.EquipmentOperationEntity;
import kr.co.softice.mes.domain.service.EquipmentOperationService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Equipment Operation Controller
 * 설비 가동 이력 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/equipment-operations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment Operation", description = "설비 가동 이력 관리 API")
public class EquipmentOperationController {

    private final EquipmentOperationService operationService;

    /**
     * Get all operations
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비 가동 이력 목록 조회", description = "모든 설비 가동 이력을 조회합니다.")
    public ResponseEntity<List<EquipmentOperationResponse>> getAllOperations() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all operations for tenant: {}", tenantId);

        List<EquipmentOperationEntity> operations = operationService.getAllOperations(tenantId);
        List<EquipmentOperationResponse> response = operations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get operation by ID
     */
    @Transactional(readOnly = true)
    @GetMapping("/{operationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비 가동 이력 상세 조회", description = "ID로 설비 가동 이력을 조회합니다.")
    public ResponseEntity<EquipmentOperationResponse> getOperationById(@PathVariable Long operationId) {
        log.info("Getting operation by ID: {}", operationId);

        EquipmentOperationEntity operation = operationService.getOperationById(operationId);
        EquipmentOperationResponse response = toResponse(operation);

        return ResponseEntity.ok(response);
    }

    /**
     * Get operations by equipment
     */
    @Transactional(readOnly = true)
    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "설비별 가동 이력 조회", description = "특정 설비의 가동 이력을 조회합니다.")
    public ResponseEntity<List<EquipmentOperationResponse>> getOperationsByEquipment(@PathVariable Long equipmentId) {
        log.info("Getting operations for equipment ID: {}", equipmentId);

        List<EquipmentOperationEntity> operations = operationService.getOperationsByEquipment(equipmentId);
        List<EquipmentOperationResponse> response = operations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get operations by date range
     */
    @Transactional(readOnly = true)
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기간별 가동 이력 조회", description = "특정 기간의 가동 이력을 조회합니다.")
    public ResponseEntity<List<EquipmentOperationResponse>> getOperationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting operations for tenant: {} from {} to {}", tenantId, startDate, endDate);

        List<EquipmentOperationEntity> operations = operationService.getOperationsByDateRange(tenantId, startDate, endDate);
        List<EquipmentOperationResponse> response = operations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get operations by status
     */
    @Transactional(readOnly = true)
    @GetMapping("/status/{operationStatus}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 가동 이력 조회", description = "특정 상태의 가동 이력을 조회합니다.")
    public ResponseEntity<List<EquipmentOperationResponse>> getOperationsByStatus(@PathVariable String operationStatus) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting operations by status: {} for tenant: {}", operationStatus, tenantId);

        List<EquipmentOperationEntity> operations = operationService.getOperationsByStatus(tenantId, operationStatus);
        List<EquipmentOperationResponse> response = operations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create operation
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "설비 가동 이력 등록", description = "새로운 설비 가동 이력을 등록합니다.")
    public ResponseEntity<EquipmentOperationResponse> createOperation(@Valid @RequestBody EquipmentOperationCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating operation for equipment ID: {} for tenant: {}", request.getEquipmentId(), tenantId);

        EquipmentOperationEntity operation = toEntity(request);
        EquipmentOperationEntity created = operationService.createOperation(tenantId, operation);
        EquipmentOperationResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update operation
     */
    @PutMapping("/{operationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "설비 가동 이력 수정", description = "설비 가동 이력 정보를 수정합니다.")
    public ResponseEntity<EquipmentOperationResponse> updateOperation(
            @PathVariable Long operationId,
            @Valid @RequestBody EquipmentOperationUpdateRequest request) {
        log.info("Updating operation ID: {}", operationId);

        EquipmentOperationEntity updateData = toEntity(request);
        EquipmentOperationEntity updated = operationService.updateOperation(operationId, updateData);
        EquipmentOperationResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Complete operation (calculate OEE)
     */
    @PatchMapping("/{operationId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER', 'OPERATOR')")
    @Operation(summary = "설비 가동 완료", description = "설비 가동을 완료하고 OEE를 계산합니다.")
    public ResponseEntity<EquipmentOperationResponse> completeOperation(@PathVariable Long operationId) {
        log.info("Completing operation ID: {}", operationId);

        EquipmentOperationEntity completed = operationService.completeOperation(operationId);
        EquipmentOperationResponse response = toResponse(completed);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete operation
     */
    @DeleteMapping("/{operationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "설비 가동 이력 삭제", description = "설비 가동 이력을 삭제합니다.")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long operationId) {
        log.info("Deleting operation ID: {}", operationId);

        operationService.deleteOperation(operationId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert EquipmentOperationCreateRequest to Entity
     */
    private EquipmentOperationEntity toEntity(EquipmentOperationCreateRequest request) {
        return EquipmentOperationEntity.builder()
                .operationDate(request.getOperationDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .operationHours(request.getOperationHours())
                .operatorName(request.getOperatorName())
                .productionQuantity(request.getProductionQuantity())
                .goodQuantity(request.getGoodQuantity())
                .defectQuantity(request.getDefectQuantity())
                .operationStatus(request.getOperationStatus())
                .stopReason(request.getStopReason())
                .stopDurationMinutes(request.getStopDurationMinutes())
                .cycleTime(request.getCycleTime())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert EquipmentOperationUpdateRequest to Entity
     */
    private EquipmentOperationEntity toEntity(EquipmentOperationUpdateRequest request) {
        return EquipmentOperationEntity.builder()
                .endTime(request.getEndTime())
                .productionQuantity(request.getProductionQuantity())
                .goodQuantity(request.getGoodQuantity())
                .defectQuantity(request.getDefectQuantity())
                .operationStatus(request.getOperationStatus())
                .stopReason(request.getStopReason())
                .stopDurationMinutes(request.getStopDurationMinutes())
                .cycleTime(request.getCycleTime())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to EquipmentOperationResponse
     */
    private EquipmentOperationResponse toResponse(EquipmentOperationEntity entity) {
        return EquipmentOperationResponse.builder()
                .operationId(entity.getOperationId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .equipmentId(entity.getEquipment().getEquipmentId())
                .equipmentCode(entity.getEquipment().getEquipmentCode())
                .equipmentName(entity.getEquipment().getEquipmentName())
                .operationDate(entity.getOperationDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .operationHours(entity.getOperationHours())
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderNo() : null)
                .workResultId(entity.getWorkResult() != null ? entity.getWorkResult().getWorkResultId() : null)
                .operatorUserId(entity.getOperatorUser() != null ? entity.getOperatorUser().getUserId() : null)
                .operatorName(entity.getOperatorName())
                .productionQuantity(entity.getProductionQuantity())
                .goodQuantity(entity.getGoodQuantity())
                .defectQuantity(entity.getDefectQuantity())
                .operationStatus(entity.getOperationStatus())
                .stopReason(entity.getStopReason())
                .stopDurationMinutes(entity.getStopDurationMinutes())
                .cycleTime(entity.getCycleTime())
                .utilizationRate(entity.getUtilizationRate())
                .performanceRate(entity.getPerformanceRate())
                .qualityRate(entity.getQualityRate())
                .oee(entity.getOee())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
