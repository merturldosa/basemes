package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.InspectionPlanCreateRequest;
import kr.co.softice.mes.common.dto.equipment.InspectionPlanUpdateRequest;
import kr.co.softice.mes.common.dto.equipment.InspectionPlanResponse;
import kr.co.softice.mes.domain.entity.InspectionPlanEntity;
import kr.co.softice.mes.domain.service.InspectionPlanService;
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
 * Inspection Plan Controller
 * 점검 계획 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/inspection-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "InspectionPlan", description = "점검 계획 API")
public class InspectionPlanController {

    private final InspectionPlanService inspectionPlanService;

    /**
     * Get all inspection plans
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검 계획 목록 조회", description = "모든 점검 계획을 조회합니다.")
    public ResponseEntity<List<InspectionPlanResponse>> getAllPlans() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all inspection plans for tenant: {}", tenantId);

        List<InspectionPlanEntity> plans = inspectionPlanService.getAllPlans(tenantId);
        List<InspectionPlanResponse> response = plans.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspection plan by ID
     */
    @Transactional(readOnly = true)
    @GetMapping("/{planId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검 계획 상세 조회", description = "ID로 점검 계획을 조회합니다.")
    public ResponseEntity<InspectionPlanResponse> getPlanById(@PathVariable Long planId) {
        log.info("Getting inspection plan by ID: {}", planId);

        InspectionPlanEntity plan = inspectionPlanService.getPlanById(planId);
        InspectionPlanResponse response = toResponse(plan);

        return ResponseEntity.ok(response);
    }

    /**
     * Get due inspection plans
     */
    @Transactional(readOnly = true)
    @GetMapping("/due")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "만기 점검 계획 조회", description = "특정 날짜까지 만기인 점검 계획을 조회합니다.")
    public ResponseEntity<List<InspectionPlanResponse>> getDuePlans(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting due inspection plans for tenant: {} by date: {}", tenantId, dueDate);

        List<InspectionPlanEntity> plans = inspectionPlanService.getDuePlans(tenantId, dueDate);
        List<InspectionPlanResponse> response = plans.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create inspection plan
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 계획 등록", description = "새로운 점검 계획을 등록합니다.")
    public ResponseEntity<InspectionPlanResponse> createPlan(@Valid @RequestBody InspectionPlanCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating inspection plan: {} for tenant: {}", request.getPlanCode(), tenantId);

        InspectionPlanEntity plan = toEntity(request);
        InspectionPlanEntity created = inspectionPlanService.createPlan(
                tenantId, plan,
                request.getEquipmentId(),
                request.getFormId(),
                request.getAssignedUserId());
        InspectionPlanResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update inspection plan
     */
    @PutMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 계획 수정", description = "점검 계획 정보를 수정합니다.")
    public ResponseEntity<InspectionPlanResponse> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody InspectionPlanUpdateRequest request) {
        log.info("Updating inspection plan ID: {}", planId);

        InspectionPlanEntity updateData = toEntity(request);
        InspectionPlanEntity updated = inspectionPlanService.updatePlan(
                planId, updateData,
                request.getEquipmentId(),
                request.getFormId(),
                request.getAssignedUserId());
        InspectionPlanResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Execute inspection plan
     */
    @PostMapping("/{planId}/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 계획 실행", description = "점검 계획을 실행 처리합니다.")
    public ResponseEntity<InspectionPlanResponse> executePlan(
            @PathVariable Long planId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate executionDate) {
        log.info("Executing inspection plan ID: {} on date: {}", planId, executionDate);

        InspectionPlanEntity executed = inspectionPlanService.executePlan(planId, executionDate);
        InspectionPlanResponse response = toResponse(executed);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete inspection plan
     */
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 계획 삭제", description = "점검 계획을 삭제합니다.")
    public ResponseEntity<Void> deletePlan(@PathVariable Long planId) {
        log.info("Deleting inspection plan ID: {}", planId);

        inspectionPlanService.deletePlan(planId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert InspectionPlanCreateRequest to Entity
     */
    private InspectionPlanEntity toEntity(InspectionPlanCreateRequest request) {
        return InspectionPlanEntity.builder()
                .planCode(request.getPlanCode())
                .planName(request.getPlanName())
                .inspectionType(request.getInspectionType())
                .cycleDays(request.getCycleDays())
                .nextDueDate(request.getNextDueDate())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert InspectionPlanUpdateRequest to Entity
     */
    private InspectionPlanEntity toEntity(InspectionPlanUpdateRequest request) {
        return InspectionPlanEntity.builder()
                .planName(request.getPlanName())
                .inspectionType(request.getInspectionType())
                .cycleDays(request.getCycleDays())
                .nextDueDate(request.getNextDueDate())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to InspectionPlanResponse
     */
    private InspectionPlanResponse toResponse(InspectionPlanEntity entity) {
        return InspectionPlanResponse.builder()
                .planId(entity.getPlanId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .planCode(entity.getPlanCode())
                .planName(entity.getPlanName())
                .equipmentId(entity.getEquipment() != null ? entity.getEquipment().getEquipmentId() : null)
                .equipmentCode(entity.getEquipment() != null ? entity.getEquipment().getEquipmentCode() : null)
                .equipmentName(entity.getEquipment() != null ? entity.getEquipment().getEquipmentName() : null)
                .formId(entity.getForm() != null ? entity.getForm().getFormId() : null)
                .formName(entity.getForm() != null ? entity.getForm().getFormName() : null)
                .inspectionType(entity.getInspectionType())
                .cycleDays(entity.getCycleDays())
                .assignedUserId(entity.getAssignedUser() != null ? entity.getAssignedUser().getUserId() : null)
                .assignedUserName(entity.getAssignedUser() != null ? entity.getAssignedUser().getFullName() : null)
                .lastExecutionDate(entity.getLastExecutionDate())
                .nextDueDate(entity.getNextDueDate())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
