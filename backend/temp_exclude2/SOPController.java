package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.SOPService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SOP (Standard Operating Procedure) Controller
 * 표준 작업 절차 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/sops")
@RequiredArgsConstructor
@Tag(name = "SOP Management", description = "표준 작업 절차 관리 API")
public class SOPController {

    private final SOPService sopService;
    private final TenantRepository tenantRepository;

    // ==================== SOP CRUD APIs ====================

    /**
     * SOP 목록 조회
     * GET /api/sops
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "SOP 목록 조회", description = "테넌트의 모든 SOP 조회")
    public ResponseEntity<ApiResponse<List<SOPEntity>>> getSOPs() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting SOPs for tenant: {}", tenantId);

        List<SOPEntity> sops = sopService.findAllSOPs(tenantId);

        return ResponseEntity.ok(ApiResponse.success("SOP 목록 조회 성공", sops));
    }

    /**
     * 활성 SOP 목록 조회
     * GET /api/sops/active
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 SOP 목록", description = "활성 상태의 SOP만 조회")
    public ResponseEntity<ApiResponse<List<SOPEntity>>> getActiveSOPs() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active SOPs for tenant: {}", tenantId);

        List<SOPEntity> sops = sopService.findActiveSOPs(tenantId);

        return ResponseEntity.ok(ApiResponse.success("활성 SOP 목록 조회 성공", sops));
    }

    /**
     * 승인된 SOP 목록 조회
     * GET /api/sops/approved
     */
    @GetMapping("/approved")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "승인된 SOP 목록", description = "승인 완료된 SOP만 조회")
    public ResponseEntity<ApiResponse<List<SOPEntity>>> getApprovedSOPs() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting approved SOPs for tenant: {}", tenantId);

        List<SOPEntity> sops = sopService.findApprovedSOPs(tenantId);

        return ResponseEntity.ok(ApiResponse.success("승인된 SOP 목록 조회 성공", sops));
    }

    /**
     * SOP 상세 조회
     * GET /api/sops/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "SOP 상세 조회", description = "SOP 상세 정보 조회 (단계 포함)")
    public ResponseEntity<ApiResponse<SOPEntity>> getSOP(@PathVariable Long id) {
        log.info("Getting SOP: {}", id);

        SOPEntity sop = sopService.findSOPById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("SOP 조회 성공", sop));
    }

    /**
     * 유형별 SOP 조회
     * GET /api/sops/type/{sopType}
     */
    @GetMapping("/type/{sopType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "유형별 SOP 조회", description = "특정 유형의 SOP 조회")
    public ResponseEntity<ApiResponse<List<SOPEntity>>> getSOPsByType(@PathVariable String sopType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting SOPs by type: {} for tenant: {}", sopType, tenantId);

        List<SOPEntity> sops = sopService.findSOPsByType(tenantId, sopType);

        return ResponseEntity.ok(ApiResponse.success("유형별 SOP 조회 성공", sops));
    }

    /**
     * 카테고리별 SOP 조회
     * GET /api/sops/category/{category}
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "카테고리별 SOP 조회", description = "특정 카테고리의 SOP 조회")
    public ResponseEntity<ApiResponse<List<SOPEntity>>> getSOPsByCategory(@PathVariable String category) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting SOPs by category: {} for tenant: {}", category, tenantId);

        List<SOPEntity> sops = sopService.findSOPsByCategory(tenantId, category);

        return ResponseEntity.ok(ApiResponse.success("카테고리별 SOP 조회 성공", sops));
    }

    /**
     * 대상 공정별 SOP 조회
     * GET /api/sops/process/{targetProcess}
     */
    @GetMapping("/process/{targetProcess}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "대상 공정별 SOP 조회", description = "특정 공정의 SOP 조회")
    public ResponseEntity<ApiResponse<List<SOPEntity>>> getSOPsByProcess(@PathVariable String targetProcess) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting SOPs by process: {} for tenant: {}", targetProcess, tenantId);

        List<SOPEntity> sops = sopService.findSOPsByTargetProcess(tenantId, targetProcess);

        return ResponseEntity.ok(ApiResponse.success("대상 공정별 SOP 조회 성공", sops));
    }

    /**
     * 검토 필요 SOP 조회
     * GET /api/sops/requiring-review
     */
    @GetMapping("/requiring-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "검토 필요 SOP 조회", description = "검토 기한이 지난 SOP 조회")
    public ResponseEntity<ApiResponse<List<SOPEntity>>> getSOPsRequiringReview() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting SOPs requiring review for tenant: {}", tenantId);

        List<SOPEntity> sops = sopService.findSOPsRequiringReview(tenantId);

        return ResponseEntity.ok(ApiResponse.success("검토 필요 SOP 조회 성공", sops));
    }

    /**
     * 승인 대기 SOP 조회
     * GET /api/sops/pending-approval
     */
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "승인 대기 SOP 조회", description = "승인 대기 중인 SOP 조회")
    public ResponseEntity<ApiResponse<List<SOPEntity>>> getSOPsPendingApproval() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting SOPs pending approval for tenant: {}", tenantId);

        List<SOPEntity> sops = sopService.findSOPsPendingApproval(tenantId);

        return ResponseEntity.ok(ApiResponse.success("승인 대기 SOP 조회 성공", sops));
    }

    /**
     * SOP 생성
     * POST /api/sops
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "SOP 생성", description = "새로운 SOP 생성")
    public ResponseEntity<ApiResponse<SOPEntity>> createSOP(@RequestBody SOPEntity sop) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating SOP: {} for tenant: {}", sop.getSopCode(), tenantId);

        // Set tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));
        sop.setTenant(tenant);

        SOPEntity created = sopService.createSOP(sop);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SOP 생성 성공", created));
    }

    /**
     * SOP 수정
     * PUT /api/sops/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "SOP 수정", description = "SOP 정보 수정")
    public ResponseEntity<ApiResponse<SOPEntity>> updateSOP(
            @PathVariable Long id,
            @RequestBody SOPEntity sop) {
        log.info("Updating SOP: {}", id);

        sop.setSopId(id);
        SOPEntity updated = sopService.updateSOP(sop);

        return ResponseEntity.ok(ApiResponse.success("SOP 수정 성공", updated));
    }

    /**
     * SOP 삭제 (비활성화)
     * DELETE /api/sops/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "SOP 삭제", description = "SOP 비활성화")
    public ResponseEntity<ApiResponse<Void>> deleteSOP(@PathVariable Long id) {
        log.info("Deleting SOP: {}", id);

        sopService.deleteSOP(id);

        return ResponseEntity.ok(ApiResponse.success("SOP 삭제 성공", null));
    }

    // ==================== SOP Approval APIs ====================

    /**
     * SOP 승인 요청
     * POST /api/sops/{id}/submit
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "SOP 승인 요청", description = "SOP 승인 요청 제출")
    public ResponseEntity<ApiResponse<SOPEntity>> submitForApproval(@PathVariable Long id) {
        log.info("Submitting SOP for approval: {}", id);

        SOPEntity sop = sopService.submitForApproval(id);

        return ResponseEntity.ok(ApiResponse.success("SOP 승인 요청 성공", sop));
    }

    /**
     * SOP 승인
     * POST /api/sops/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "SOP 승인", description = "SOP 승인 처리")
    public ResponseEntity<ApiResponse<SOPEntity>> approveSOP(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        log.info("Approving SOP: {} by user: {}", id, approverId);

        SOPEntity sop = sopService.approveSOP(id, approverId);

        return ResponseEntity.ok(ApiResponse.success("SOP 승인 성공", sop));
    }

    /**
     * SOP 반려
     * POST /api/sops/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "SOP 반려", description = "SOP 승인 반려")
    public ResponseEntity<ApiResponse<SOPEntity>> rejectSOP(@PathVariable Long id) {
        log.info("Rejecting SOP: {}", id);

        SOPEntity sop = sopService.rejectSOP(id);

        return ResponseEntity.ok(ApiResponse.success("SOP 반려 성공", sop));
    }

    /**
     * SOP 폐기
     * POST /api/sops/{id}/obsolete
     */
    @PostMapping("/{id}/obsolete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "SOP 폐기", description = "SOP 폐기 처리")
    public ResponseEntity<ApiResponse<SOPEntity>> markObsolete(@PathVariable Long id) {
        log.info("Marking SOP as obsolete: {}", id);

        SOPEntity sop = sopService.markObsolete(id);

        return ResponseEntity.ok(ApiResponse.success("SOP 폐기 성공", sop));
    }

    // ==================== SOP Step APIs ====================

    /**
     * SOP 단계 추가
     * POST /api/sops/{sopId}/steps
     */
    @PostMapping("/{sopId}/steps")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "SOP 단계 추가", description = "SOP에 새 단계 추가")
    public ResponseEntity<ApiResponse<SOPStepEntity>> addStep(
            @PathVariable Long sopId,
            @RequestBody SOPStepEntity step) {
        log.info("Adding step to SOP: {}", sopId);

        SOPStepEntity created = sopService.addStep(sopId, step);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SOP 단계 추가 성공", created));
    }

    /**
     * SOP 단계 수정
     * PUT /api/sops/steps/{stepId}
     */
    @PutMapping("/steps/{stepId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "SOP 단계 수정", description = "SOP 단계 정보 수정")
    public ResponseEntity<ApiResponse<SOPStepEntity>> updateStep(
            @PathVariable Long stepId,
            @RequestBody SOPStepEntity step) {
        log.info("Updating SOP step: {}", stepId);

        step.setSopStepId(stepId);
        SOPStepEntity updated = sopService.updateStep(step);

        return ResponseEntity.ok(ApiResponse.success("SOP 단계 수정 성공", updated));
    }

    /**
     * SOP 단계 삭제
     * DELETE /api/sops/steps/{stepId}
     */
    @DeleteMapping("/steps/{stepId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "SOP 단계 삭제", description = "SOP 단계 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteStep(@PathVariable Long stepId) {
        log.info("Deleting SOP step: {}", stepId);

        sopService.deleteStep(stepId);

        return ResponseEntity.ok(ApiResponse.success("SOP 단계 삭제 성공", null));
    }

    // ==================== SOP Execution APIs ====================

    /**
     * SOP 실행 시작
     * POST /api/sops/{sopId}/executions
     */
    @PostMapping("/{sopId}/executions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "SOP 실행 시작", description = "SOP 실행 기록 생성 및 시작")
    public ResponseEntity<ApiResponse<SOPExecutionEntity>> startExecution(
            @PathVariable Long sopId,
            @RequestBody ExecutionStartRequest request) {
        log.info("Starting SOP execution for SOP: {}", sopId);

        SOPExecutionEntity execution = sopService.startExecution(
                sopId,
                request.getExecutorId(),
                request.getReferenceType(),
                request.getReferenceId(),
                request.getReferenceNo()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SOP 실행 시작 성공", execution));
    }

    /**
     * 실행 단계 시작
     * POST /api/sops/executions/{executionId}/steps/{stepId}/start
     */
    @PostMapping("/executions/{executionId}/steps/{stepId}/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "실행 단계 시작", description = "특정 단계 실행 시작")
    public ResponseEntity<ApiResponse<SOPExecutionStepEntity>> startExecutionStep(
            @PathVariable Long executionId,
            @PathVariable Long stepId) {
        log.info("Starting execution step: {} for execution: {}", stepId, executionId);

        SOPExecutionStepEntity executionStep = sopService.startExecutionStep(executionId, stepId);

        return ResponseEntity.ok(ApiResponse.success("실행 단계 시작 성공", executionStep));
    }

    /**
     * 실행 단계 완료
     * POST /api/sops/executions/steps/{executionStepId}/complete
     */
    @PostMapping("/executions/steps/{executionStepId}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "실행 단계 완료", description = "특정 단계 실행 완료")
    public ResponseEntity<ApiResponse<SOPExecutionStepEntity>> completeExecutionStep(
            @PathVariable Long executionStepId,
            @RequestBody StepCompleteRequest request) {
        log.info("Completing execution step: {}", executionStepId);

        SOPExecutionStepEntity executionStep = sopService.completeExecutionStep(
                executionStepId,
                request.getResultValue(),
                request.getChecklistResults()
        );

        return ResponseEntity.ok(ApiResponse.success("실행 단계 완료 성공", executionStep));
    }

    /**
     * SOP 실행 완료
     * POST /api/sops/executions/{executionId}/complete
     */
    @PostMapping("/executions/{executionId}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "SOP 실행 완료", description = "SOP 실행 완료 처리")
    public ResponseEntity<ApiResponse<SOPExecutionEntity>> completeExecution(
            @PathVariable Long executionId) {
        log.info("Completing SOP execution: {}", executionId);

        SOPExecutionEntity execution = sopService.completeExecution(executionId);

        return ResponseEntity.ok(ApiResponse.success("SOP 실행 완료 성공", execution));
    }

    /**
     * SOP 실행 취소
     * POST /api/sops/executions/{executionId}/cancel
     */
    @PostMapping("/executions/{executionId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "SOP 실행 취소", description = "SOP 실행 취소")
    public ResponseEntity<ApiResponse<SOPExecutionEntity>> cancelExecution(
            @PathVariable Long executionId,
            @RequestParam String reason) {
        log.info("Cancelling SOP execution: {}", executionId);

        SOPExecutionEntity execution = sopService.cancelExecution(executionId, reason);

        return ResponseEntity.ok(ApiResponse.success("SOP 실행 취소 성공", execution));
    }

    // ==================== Request DTOs ====================

    @Data
    public static class ExecutionStartRequest {
        private Long executorId;
        private String referenceType;
        private Long referenceId;
        private String referenceNo;
    }

    @Data
    public static class StepCompleteRequest {
        private String resultValue;
        private String checklistResults;
    }
}
