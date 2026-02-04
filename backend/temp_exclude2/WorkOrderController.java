package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.workorder.WorkOrderCreateRequest;
import kr.co.softice.mes.common.dto.workorder.WorkOrderResponse;
import kr.co.softice.mes.common.dto.workorder.WorkOrderUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.ProcessEntity;
import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.repository.ProcessRepository;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import kr.co.softice.mes.domain.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Work Order Controller
 * 작업 지시 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/work-orders")
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
    public ResponseEntity<ApiResponse<List<WorkOrderResponse>>> getWorkOrders() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work orders for tenant: {}", tenantId);

        List<WorkOrderResponse> workOrders = workOrderService.findByTenant(tenantId).stream()
                .map(this::toWorkOrderResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 지시 목록 조회 성공", workOrders));
    }

    /**
     * 상태별 작업 지시 목록 조회
     * GET /api/work-orders/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "상태별 작업 지시 조회", description = "특정 상태의 작업 지시 조회")
    public ResponseEntity<ApiResponse<List<WorkOrderResponse>>> getWorkOrdersByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work orders with status {} for tenant: {}", status, tenantId);

        List<WorkOrderResponse> workOrders = workOrderService.findByTenantAndStatus(tenantId, status).stream()
                .map(this::toWorkOrderResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 지시 목록 조회 성공", workOrders));
    }

    /**
     * 날짜 범위별 작업 지시 조회
     * GET /api/work-orders/date-range?startDate=...&endDate=...
     */
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "날짜 범위별 작업 지시 조회", description = "시작일~종료일 범위의 작업 지시 조회")
    public ResponseEntity<ApiResponse<List<WorkOrderResponse>>> getWorkOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work orders from {} to {} for tenant: {}", startDate, endDate, tenantId);

        List<WorkOrderResponse> workOrders = workOrderService.findByDateRange(tenantId, startDate, endDate).stream()
                .map(this::toWorkOrderResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 지시 목록 조회 성공", workOrders));
    }

    /**
     * 작업 지시 상세 조회
     * GET /api/work-orders/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 지시 상세 조회", description = "작업 지시 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getWorkOrder(@PathVariable Long id) {
        log.info("Getting work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.findByIdWithAllRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("작업 지시 조회 성공", toWorkOrderResponse(workOrder)));
    }

    /**
     * 작업 지시 번호로 조회
     * GET /api/work-orders/no/{workOrderNo}
     */
    @GetMapping("/no/{workOrderNo}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 지시 번호로 조회", description = "작업 지시 번호로 조회")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getWorkOrderByNo(@PathVariable String workOrderNo) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work order by no: {} for tenant: {}", workOrderNo, tenantId);

        WorkOrderEntity workOrder = workOrderService.findByWorkOrderNo(tenantId, workOrderNo)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("작업 지시 조회 성공", toWorkOrderResponse(workOrder)));
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

        UserEntity assignedUser = null;
        if (request.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        }

        WorkOrderEntity workOrder = WorkOrderEntity.builder()
                .tenant(tenant)
                .workOrderNo(request.getWorkOrderNo())
                .product(product)
                .process(process)
                .assignedUser(assignedUser)
                .status("PENDING")
                .plannedQuantity(request.getPlannedQuantity())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .priority(request.getPriority() != null ? Integer.parseInt(request.getPriority()) : 5)
                .remarks(request.getRemarks())
                .build();

        WorkOrderEntity createdWorkOrder = workOrderService.createWorkOrder(workOrder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("작업 지시 생성 성공", toWorkOrderResponse(createdWorkOrder)));
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

        if (request.getProductId() != null) {
            ProductEntity product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
            workOrder.setProduct(product);
        }

        if (request.getProcessId() != null) {
            ProcessEntity process = processRepository.findById(request.getProcessId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROCESS_NOT_FOUND));
            workOrder.setProcess(process);
        }

        if (request.getAssignedUserId() != null) {
            UserEntity user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
            workOrder.setAssignedUser(user);
        }

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
            workOrder.setPriority(Integer.parseInt(request.getPriority()));
        }
        if (request.getRemarks() != null) {
            workOrder.setRemarks(request.getRemarks());
        }

        WorkOrderEntity updatedWorkOrder = workOrderService.updateWorkOrder(workOrder);

        return ResponseEntity.ok(ApiResponse.success("작업 지시 수정 성공", toWorkOrderResponse(updatedWorkOrder)));
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
     * 작업 지시 시작 (상태: READY/PENDING -> IN_PROGRESS)
     * POST /api/work-orders/{id}/start
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'OPERATOR')")
    @Operation(summary = "작업 지시 시작", description = "작업 지시를 진행 중 상태로 변경")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> startWorkOrder(@PathVariable Long id) {
        log.info("Starting work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.startWorkOrder(id);

        return ResponseEntity.ok(ApiResponse.success("작업 지시 시작 성공", toWorkOrderResponse(workOrder)));
    }

    /**
     * 작업 지시 완료 (상태: IN_PROGRESS -> COMPLETED)
     * POST /api/work-orders/{id}/complete
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "작업 지시 완료", description = "작업 지시를 완료 상태로 변경")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> completeWorkOrder(@PathVariable Long id) {
        log.info("Completing work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.completeWorkOrder(id);

        return ResponseEntity.ok(ApiResponse.success("작업 지시 완료 성공", toWorkOrderResponse(workOrder)));
    }

    /**
     * 작업 지시 취소
     * POST /api/work-orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "작업 지시 취소", description = "작업 지시를 취소 상태로 변경")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> cancelWorkOrder(@PathVariable Long id) {
        log.info("Cancelling work order: {}", id);

        WorkOrderEntity workOrder = workOrderService.cancelWorkOrder(id);

        return ResponseEntity.ok(ApiResponse.success("작업 지시 취소 성공", toWorkOrderResponse(workOrder)));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private WorkOrderResponse toWorkOrderResponse(WorkOrderEntity workOrder) {
        return WorkOrderResponse.builder()
                .workOrderId(workOrder.getWorkOrderId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .status(workOrder.getStatus())
                // Product
                .productId(workOrder.getProduct().getProductId())
                .productCode(workOrder.getProduct().getProductCode())
                .productName(workOrder.getProduct().getProductName())
                // Process
                .processId(workOrder.getProcess().getProcessId())
                .processCode(workOrder.getProcess().getProcessCode())
                .processName(workOrder.getProcess().getProcessName())
                // Assigned user (may be null)
                .assignedUserId(workOrder.getAssignedUser() != null ? workOrder.getAssignedUser().getUserId() : null)
                .assignedUserName(workOrder.getAssignedUser() != null ? workOrder.getAssignedUser().getFullName() : null)
                // Quantities
                .plannedQuantity(workOrder.getPlannedQuantity())
                .actualQuantity(workOrder.getActualQuantity())
                .goodQuantity(workOrder.getGoodQuantity())
                .defectQuantity(workOrder.getDefectQuantity())
                // Dates
                .plannedStartDate(workOrder.getPlannedStartDate())
                .plannedEndDate(workOrder.getPlannedEndDate())
                .actualStartDate(workOrder.getActualStartDate())
                .actualEndDate(workOrder.getActualEndDate())
                // Others
                .priority(workOrder.getPriority() != null ? workOrder.getPriority().toString() : null)
                .tenantId(workOrder.getTenant().getTenantId())
                .tenantName(workOrder.getTenant().getTenantName())
                .remarks(workOrder.getRemarks())
                .createdAt(workOrder.getCreatedAt())
                .updatedAt(workOrder.getUpdatedAt())
                .build();
    }
}
