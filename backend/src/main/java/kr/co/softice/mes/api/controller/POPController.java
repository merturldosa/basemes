package kr.co.softice.mes.api.controller;

import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.pop.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.DefectEntity;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.service.POPService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * POP Controller
 * Point of Production - Field operations API
 * Provides real-time work order execution and tracking for field workers
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/pop")
@RequiredArgsConstructor
public class POPController {

    private final POPService popService;

    /**
     * Get active work orders for operator
     * GET /api/pop/work-orders/active
     *
     * @param operatorId Optional operator ID filter
     * @return List of active work orders
     */
    @Transactional(readOnly = true)
    @GetMapping("/work-orders/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<WorkOrderEntity>>> getActiveWorkOrders(
            @RequestParam(required = false) Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        List<WorkOrderEntity> workOrders = popService.getActiveWorkOrders(tenantId, operatorId);

        return ResponseEntity.ok(ApiResponse.success(workOrders));
    }

    /**
     * Start work order
     * POST /api/pop/work-orders/{id}/start
     *
     * @param id Work order ID
     * @param operatorId Operator user ID
     * @return Work progress response
     */
    @PostMapping("/work-orders/{id}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> startWorkOrder(
            @PathVariable Long id,
            @RequestParam Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressResponse response = popService.startWorkOrder(tenantId, id, operatorId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Record work progress (production quantity)
     * POST /api/pop/work-progress/record
     *
     * @param request Work progress record request
     * @return Updated work progress
     */
    @PostMapping("/work-progress/record")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> recordProgress(
            @Valid @RequestBody WorkProgressRecordRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressResponse response = popService.recordProgress(tenantId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Record defect
     * POST /api/pop/work-progress/defect
     *
     * @param request Defect record request
     * @return Defect entity
     */
    @PostMapping("/work-progress/defect")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DefectEntity>> recordDefect(
            @Valid @RequestBody DefectRecordRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        DefectEntity defect = popService.recordDefect(tenantId, request);

        return ResponseEntity.ok(ApiResponse.success(defect));
    }

    /**
     * Pause work
     * POST /api/pop/work-orders/{id}/pause
     *
     * @param id Work order ID
     * @param request Pause work request
     * @return Updated work progress
     */
    @PostMapping("/work-orders/{id}/pause")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> pauseWork(
            @PathVariable Long id,
            @Valid @RequestBody PauseWorkRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressResponse response = popService.pauseWork(tenantId, id, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Resume work
     * POST /api/pop/work-orders/{id}/resume
     *
     * @param id Work order ID
     * @return Updated work progress
     */
    @PostMapping("/work-orders/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> resumeWork(@PathVariable Long id) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressResponse response = popService.resumeWork(tenantId, id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Complete work order
     * POST /api/pop/work-orders/{id}/complete
     *
     * @param id Work order ID
     * @param remarks Optional completion remarks
     * @return Completed work order
     */
    @PostMapping("/work-orders/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkOrderEntity>> completeWorkOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkOrderEntity workOrder = popService.completeWorkOrder(tenantId, id, remarks);

        return ResponseEntity.ok(ApiResponse.success(workOrder));
    }

    /**
     * Get work progress by work order
     * GET /api/pop/work-orders/{id}/progress
     *
     * @param id Work order ID
     * @return Work progress response
     */
    @Transactional(readOnly = true)
    @GetMapping("/work-orders/{id}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> getWorkProgress(@PathVariable Long id) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressResponse response = popService.getWorkProgress(tenantId, id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get today's production statistics
     * GET /api/pop/statistics/today
     *
     * @param operatorId Optional operator ID filter
     * @return Production statistics
     */
    @Transactional(readOnly = true)
    @GetMapping("/statistics/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductionStatisticsResponse>> getTodayStatistics(
            @RequestParam(required = false) Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        ProductionStatisticsResponse stats = popService.getTodayStatistics(tenantId, operatorId);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Scan barcode (work order, material, product, lot)
     * POST /api/pop/scan
     *
     * @param barcode Barcode string
     * @param type Scan type (WORK_ORDER, MATERIAL, PRODUCT, LOT)
     * @return Scan result
     */
    @PostMapping("/scan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> scanBarcode(
            @RequestParam String barcode,
            @RequestParam String type) {

        String tenantId = TenantContext.getCurrentTenant();
        Object result = popService.scanBarcode(tenantId, barcode, type);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
