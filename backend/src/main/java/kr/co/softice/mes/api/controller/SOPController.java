package kr.co.softice.mes.api.controller;

import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.sop.SOPSimplifiedResponse;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.service.SOPOperatorService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * SOP Controller
 * 표준 작업 절차 관리 API (Operator endpoints)
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/sop")
@RequiredArgsConstructor
public class SOPController {

    private final SOPOperatorService sopOperatorService;

    // ==================== Operator APIs ====================

    /**
     * Get SOPs for work order
     * GET /api/sop/operator/work-order/{workOrderId}
     *
     * @param workOrderId Work order ID
     * @return List of simplified SOPs
     */
    @GetMapping("/operator/work-order/{workOrderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SOPSimplifiedResponse>>> getWorkOrderSOPs(
            @PathVariable Long workOrderId) {

        log.info("Getting SOPs for work order: {}", workOrderId);

        List<SOPSimplifiedResponse> sops = sopOperatorService.getWorkOrderSOPs(workOrderId);

        return ResponseEntity.ok(ApiResponse.success(sops));
    }

    /**
     * Start SOP execution
     * POST /api/sop/operator/execution/start
     *
     * @param request Start SOP execution request
     * @return Started SOP execution
     */
    @PostMapping("/operator/execution/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SOPSimplifiedResponse>> startSOPExecution(
            @Valid @RequestBody StartSOPExecutionRequest request) {

        log.info("Starting SOP execution: sopId={}, workOrderId={}, operatorId={}",
            request.getSopId(), request.getWorkOrderId(), request.getOperatorId());

        SOPSimplifiedResponse response = sopOperatorService.startSOPExecution(
            request.getSopId(),
            request.getWorkOrderId(),
            request.getOperatorId()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Complete SOP step
     * PUT /api/sop/operator/execution/{executionId}/step/{stepId}/complete
     *
     * @param executionId SOP execution ID
     * @param stepId Execution step ID
     * @param request Complete step request
     * @return Success response
     */
    @PutMapping("/operator/execution/{executionId}/step/{stepId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> completeStep(
            @PathVariable Long executionId,
            @PathVariable Long stepId,
            @Valid @RequestBody CompleteStepRequest request) {

        log.info("Completing step: executionId={}, stepId={}, passed={}",
            executionId, stepId, request.getPassed());

        sopOperatorService.completeStep(stepId, request.getPassed(), request.getNotes());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Complete SOP execution
     * POST /api/sop/operator/execution/{executionId}/complete
     *
     * @param executionId SOP execution ID
     * @param request Complete SOP execution request
     * @return Completed SOP execution
     */
    @PostMapping("/operator/execution/{executionId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SOPSimplifiedResponse>> completeSOPExecution(
            @PathVariable Long executionId,
            @Valid @RequestBody CompleteSOPExecutionRequest request) {

        log.info("Completing SOP execution: {}", executionId);

        SOPSimplifiedResponse response = sopOperatorService.completeSOPExecution(
            executionId,
            request.getRemarks()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== Request DTOs ====================

    @Data
    public static class StartSOPExecutionRequest {
        private Long sopId;
        private Long workOrderId;
        private Long operatorId;
    }

    @Data
    public static class CompleteStepRequest {
        private Boolean passed;
        private String notes;
    }

    @Data
    public static class CompleteSOPExecutionRequest {
        private String remarks;
    }
}
