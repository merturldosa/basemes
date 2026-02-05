package kr.co.softice.mes.api.controller;

import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.POPService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
     * @return Work progress entity
     */
    @PostMapping("/work-orders/{id}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<POPService.WorkProgressEntity>> startWorkOrder(
            @PathVariable Long id,
            @RequestParam Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        POPService.WorkProgressEntity progress = popService.startWorkOrder(tenantId, id, operatorId);

        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * Record work progress (production quantity)
     * POST /api/pop/work-progress/record
     *
     * @param progressId Work progress ID
     * @param quantity Quantity produced
     * @param operatorId Operator user ID
     * @return Updated work progress
     */
    @PostMapping("/work-progress/record")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<POPService.WorkProgressEntity>> recordProgress(
            @RequestParam Long progressId,
            @RequestParam Integer quantity,
            @RequestParam Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        POPService.WorkProgressEntity progress = popService.recordProgress(tenantId, progressId, quantity, operatorId);

        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * Record defect
     * POST /api/pop/work-progress/defect
     *
     * @param progressId Work progress ID
     * @param quantity Defect quantity
     * @param defectType Type of defect
     * @param reason Defect reason
     * @param operatorId Operator user ID
     * @return Defect entity
     */
    @PostMapping("/work-progress/defect")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DefectEntity>> recordDefect(
            @RequestParam Long progressId,
            @RequestParam Integer quantity,
            @RequestParam String defectType,
            @RequestParam(required = false) String reason,
            @RequestParam Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        DefectEntity defect = popService.recordDefect(tenantId, progressId, quantity, defectType, reason, operatorId);

        return ResponseEntity.ok(ApiResponse.success(defect));
    }

    /**
     * Pause work
     * POST /api/pop/work-orders/{id}/pause
     *
     * @param id Work order ID
     * @param reason Pause reason
     * @return Updated work progress
     */
    @PostMapping("/work-orders/{id}/pause")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<POPService.WorkProgressEntity>> pauseWork(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        String tenantId = TenantContext.getCurrentTenant();
        POPService.WorkProgressEntity progress = popService.pauseWork(tenantId, id, reason);

        return ResponseEntity.ok(ApiResponse.success(progress));
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
    public ResponseEntity<ApiResponse<POPService.WorkProgressEntity>> resumeWork(@PathVariable Long id) {

        String tenantId = TenantContext.getCurrentTenant();
        POPService.WorkProgressEntity progress = popService.resumeWork(tenantId, id);

        return ResponseEntity.ok(ApiResponse.success(progress));
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
     * @return Work progress entity
     */
    @GetMapping("/work-orders/{id}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<POPService.WorkProgressEntity>> getWorkProgress(@PathVariable Long id) {

        String tenantId = TenantContext.getCurrentTenant();
        POPService.WorkProgressEntity progress = popService.getWorkProgress(tenantId, id);

        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * Get today's production statistics
     * GET /api/pop/statistics/today
     *
     * @param operatorId Optional operator ID filter
     * @return Production statistics
     */
    @GetMapping("/statistics/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<POPService.ProductionStatistics>> getTodayStatistics(
            @RequestParam(required = false) Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        POPService.ProductionStatistics stats = popService.getTodayStatistics(tenantId, operatorId);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Scan barcode (work order, material, product)
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
