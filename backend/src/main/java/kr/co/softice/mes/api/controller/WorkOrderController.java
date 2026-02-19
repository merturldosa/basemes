package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.workorder.WorkOrderCreateRequest;
import kr.co.softice.mes.common.dto.workorder.WorkOrderResponse;
import kr.co.softice.mes.common.dto.workorder.WorkOrderUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WorkOrder Controller
 * 작업 지시 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@Tag(name = "Work Order Management", description = "작업 지시 관리 API")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final ProcessRepository processRepository;
    private final UserRepository userRepository;

    /**
     * 작업 지시 목록 조회
     * GET /api/work-orders
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 지시 목록 조회", description = "테넌트의 모든 작업 지시 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<WorkOrderResponse>>> getWorkOrders() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work orders for tenant: {}", tenantId);

        List<WorkOrderResponse> workOrders = workOrderService.findByTenant(tenantId).stream()
                .map(this::toWorkOrderResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 지시 목록 조회 성공", workOrders));
    }

    /**
     * 작업 지시 상세 조회
     * GET /api/work-orders/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 지시 상세 조회", description = "작업 지시 ID로 상세 정보 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getWorkOrder(@PathVariable Long id) {
        log.info("Getting work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("작업 지시 조회 성공", toWorkOrderResponse(workOrder)));
    }

    /**
     * 상태별 작업 지시 조회
     * GET /api/work-orders/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 작업 지시 조회", description = "특정 상태의 작업 지시 목록 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<WorkOrderResponse>>> getWorkOrdersByStatus(
            @PathVariable String status) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work orders by status: {} for tenant: {}", status, tenantId);

        List<WorkOrderResponse> workOrders = workOrderService.findByStatus(tenantId, status).stream()
                .map(this::toWorkOrderResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 지시 목록 조회 성공", workOrders));
    }

    /**
     * 기간별 작업 지시 조회
     * GET /api/work-orders/date-range
     */
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기간별 작업 지시 조회", description = "시작일과 종료일 사이의 작업 지시 조회")
    public ResponseEntity<ApiResponse<List<WorkOrderResponse>>> getWorkOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work orders by date range: {} to {} for tenant: {}", startDate, endDate, tenantId);

        List<WorkOrderResponse> workOrders = workOrderService.findByDateRange(tenantId, startDate, endDate).stream()
                .map(this::toWorkOrderResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 지시 목록 조회 성공", workOrders));
    }

    /**
     * 작업 지시 생성
     * POST /api/work-orders
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "작업 지시 생성", description = "신규 작업 지시 등록")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> createWorkOrder(
            @Valid @RequestBody WorkOrderCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating work order: {} for tenant: {}", request.getWorkOrderNo(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        ProcessEntity process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROCESS_NOT_FOUND));

        WorkOrderEntity workOrder = WorkOrderEntity.builder()
                .tenant(tenant)
                .workOrderNo(request.getWorkOrderNo())
                .product(product)
                .process(process)
                .plannedQuantity(request.getPlannedQuantity())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .priority(convertPriorityToInteger(request.getPriority()))
                .status("PENDING")
                .build();

        if (request.getAssignedUserId() != null) {
            UserEntity user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
            workOrder.setAssignedUser(user);
        }

        if (request.getRemarks() != null) {
            workOrder.setRemarks(request.getRemarks());
        }

        WorkOrderEntity created = workOrderService.createWorkOrder(workOrder);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("작업 지시 생성 성공", toWorkOrderResponse(created)));
    }

    /**
     * 작업 지시 수정
     * PUT /api/work-orders/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "작업 지시 수정", description = "작업 지시 정보 수정")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> updateWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderUpdateRequest request) {

        log.info("Updating work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        if (request.getPlannedQuantity() != null) {
            workOrder.setPlannedQuantity(request.getPlannedQuantity());
        }
        if (request.getPlannedStartDate() != null) {
            workOrder.setPlannedStartDate(request.getPlannedStartDate());
        }
        if (request.getPlannedEndDate() != null) {
            workOrder.setPlannedEndDate(request.getPlannedEndDate());
        }
        if (request.getPriority() != null) {
            workOrder.setPriority(convertPriorityToInteger(request.getPriority()));
        }
        if (request.getRemarks() != null) {
            workOrder.setRemarks(request.getRemarks());
        }
        if (request.getAssignedUserId() != null) {
            UserEntity user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
            workOrder.setAssignedUser(user);
        }

        WorkOrderEntity updated = workOrderService.updateWorkOrder(workOrder);

        return ResponseEntity.ok(ApiResponse.success("작업 지시 수정 성공", toWorkOrderResponse(updated)));
    }

    /**
     * 작업 지시 삭제
     * DELETE /api/work-orders/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "작업 지시 삭제", description = "작업 지시 완전 삭제 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkOrder(@PathVariable Long id) {
        log.info("Deleting work order: {}", id);

        workOrderService.deleteWorkOrder(id);

        return ResponseEntity.ok(ApiResponse.success("작업 지시 삭제 성공", null));
    }

    /**
     * 작업 준비 완료
     * POST /api/work-orders/{id}/ready
     */
    @PostMapping("/{id}/ready")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'OPERATOR')")
    @Operation(summary = "작업 준비 완료", description = "작업 지시를 준비 완료 상태로 변경")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> readyWorkOrder(@PathVariable Long id) {
        log.info("Ready work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.readyWorkOrder(id);

        return ResponseEntity.ok(ApiResponse.success("작업 준비 완료", toWorkOrderResponse(workOrder)));
    }

    /**
     * 작업 시작
     * POST /api/work-orders/{id}/start
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'OPERATOR')")
    @Operation(summary = "작업 시작", description = "작업 지시를 시작하고 실제 시작 시간 기록")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> startWorkOrder(@PathVariable Long id) {
        log.info("Starting work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.startWorkOrder(id);

        return ResponseEntity.ok(ApiResponse.success("작업 시작 성공", toWorkOrderResponse(workOrder)));
    }

    /**
     * 작업 완료
     * POST /api/work-orders/{id}/complete
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'OPERATOR')")
    @Operation(summary = "작업 완료", description = "작업 지시를 완료하고 실제 종료 시간 기록")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> completeWorkOrder(@PathVariable Long id) {
        log.info("Completing work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.completeWorkOrder(id);

        return ResponseEntity.ok(ApiResponse.success("작업 완료 성공", toWorkOrderResponse(workOrder)));
    }

    /**
     * 작업 취소
     * POST /api/work-orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "작업 취소", description = "작업 지시를 취소 상태로 변경")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> cancelWorkOrder(@PathVariable Long id) {
        log.info("Cancelling work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.cancelWorkOrder(id);

        return ResponseEntity.ok(ApiResponse.success("작업 취소 성공", toWorkOrderResponse(workOrder)));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private WorkOrderResponse toWorkOrderResponse(WorkOrderEntity entity) {
        return WorkOrderResponse.builder()
                .workOrderId(entity.getWorkOrderId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .workOrderNo(entity.getWorkOrderNo())
                .productId(entity.getProduct() != null ? entity.getProduct().getProductId() : null)
                .productCode(entity.getProduct() != null ? entity.getProduct().getProductCode() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getProductName() : null)
                .processId(entity.getProcess() != null ? entity.getProcess().getProcessId() : null)
                .processCode(entity.getProcess() != null ? entity.getProcess().getProcessCode() : null)
                .processName(entity.getProcess() != null ? entity.getProcess().getProcessName() : null)
                .assignedUserId(entity.getAssignedUser() != null ? entity.getAssignedUser().getUserId() : null)
                .assignedUserName(entity.getAssignedUser() != null ? entity.getAssignedUser().getFullName() : null)
                .plannedQuantity(entity.getPlannedQuantity())
                .actualQuantity(entity.getActualQuantity())
                .goodQuantity(entity.getGoodQuantity())
                .defectQuantity(entity.getDefectQuantity())
                .plannedStartDate(entity.getPlannedStartDate())
                .plannedEndDate(entity.getPlannedEndDate())
                .actualStartDate(entity.getActualStartDate())
                .actualEndDate(entity.getActualEndDate())
                .status(entity.getStatus())
                .priority(entity.getPriority() != null ? entity.getPriority().toString() : null)
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 우선순위 문자열을 Integer로 변환
     * HIGH -> 1, MEDIUM -> 5, LOW -> 10, null -> 5 (default)
     */
    private Integer convertPriorityToInteger(String priority) {
        if (priority == null) {
            return 5; // default: MEDIUM
        }

        switch (priority.toUpperCase()) {
            case "HIGH":
                return 1;
            case "MEDIUM":
                return 5;
            case "LOW":
                return 10;
            default:
                return 5; // default: MEDIUM
        }
    }
}
