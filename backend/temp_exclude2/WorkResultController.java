package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.workresult.WorkResultCreateRequest;
import kr.co.softice.mes.common.dto.workresult.WorkResultResponse;
import kr.co.softice.mes.common.dto.workresult.WorkResultUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.entity.WorkResultEntity;
import kr.co.softice.mes.domain.repository.UserRepository;
import kr.co.softice.mes.domain.repository.WorkOrderRepository;
import kr.co.softice.mes.domain.service.WorkResultService;
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
 * Work Result Controller
 * 작업 실적 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/work-results")
@RequiredArgsConstructor
@Tag(name = "Work Result Management", description = "작업 실적 관리 API")
public class WorkResultController {

    private final WorkResultService workResultService;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    /**
     * 작업 실적 목록 조회
     * GET /api/work-results
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 실적 목록 조회", description = "테넌트의 모든 작업 실적 조회")
    public ResponseEntity<ApiResponse<List<WorkResultResponse>>> getWorkResults() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work results for tenant: {}", tenantId);

        List<WorkResultResponse> workResults = workResultService.findByTenant(tenantId).stream()
                .map(this::toWorkResultResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 실적 목록 조회 성공", workResults));
    }

    /**
     * 작업 지시별 실적 조회
     * GET /api/work-results/work-order/{workOrderId}
     */
    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 지시별 실적 조회", description = "특정 작업 지시의 모든 실적 조회")
    public ResponseEntity<ApiResponse<List<WorkResultResponse>>> getWorkResultsByWorkOrder(
            @PathVariable Long workOrderId) {

        log.info("Getting work results for work order: {}", workOrderId);

        List<WorkResultResponse> workResults = workResultService.findByWorkOrderId(workOrderId).stream()
                .map(this::toWorkResultResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 실적 목록 조회 성공", workResults));
    }

    /**
     * 날짜 범위별 작업 실적 조회
     * GET /api/work-results/date-range?startDate=...&endDate=...
     */
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "날짜 범위별 작업 실적 조회", description = "시작일~종료일 범위의 작업 실적 조회")
    public ResponseEntity<ApiResponse<List<WorkResultResponse>>> getWorkResultsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work results from {} to {} for tenant: {}", startDate, endDate, tenantId);

        List<WorkResultResponse> workResults = workResultService.findByDateRange(tenantId, startDate, endDate).stream()
                .map(this::toWorkResultResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 실적 목록 조회 성공", workResults));
    }

    /**
     * 작업 실적 상세 조회
     * GET /api/work-results/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 실적 상세 조회", description = "작업 실적 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<WorkResultResponse>> getWorkResult(@PathVariable Long id) {
        log.info("Getting work result: {}", id);

        WorkResultEntity workResult = workResultService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_RESULT_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("작업 실적 조회 성공", toWorkResultResponse(workResult)));
    }

    /**
     * 작업 실적 생성
     * POST /api/work-results
     *
     * 생성 시 자동으로 작업 지시의 집계(actualQuantity, goodQuantity, defectQuantity)가 재계산됩니다.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'OPERATOR')")
    @Operation(summary = "작업 실적 생성", description = "신규 작업 실적 등록 (자동으로 작업 지시 집계 업데이트)")
    public ResponseEntity<ApiResponse<WorkResultResponse>> createWorkResult(
            @Valid @RequestBody WorkResultCreateRequest request) {

        log.info("Creating work result for work order: {}", request.getWorkOrderId());

        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(request.getWorkOrderId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        UserEntity worker = null;
        if (request.getWorkerId() != null) {
            worker = userRepository.findById(request.getWorkerId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        }

        WorkResultEntity workResult = WorkResultEntity.builder()
                .workOrder(workOrder)
                .tenant(workOrder.getTenant())
                .resultDate(request.getResultDate())
                .quantity(request.getQuantity())
                .goodQuantity(request.getGoodQuantity())
                .defectQuantity(request.getDefectQuantity())
                .workStartTime(request.getWorkStartTime())
                .workEndTime(request.getWorkEndTime())
                .workDuration(request.getWorkDuration())  // null이면 Service에서 자동 계산
                .worker(worker)
                .workerName(request.getWorkerName())
                .defectReason(request.getDefectReason())
                .remarks(request.getRemarks())
                .build();

        // Service에서 자동으로 workOrderService.recalculateAggregates() 호출
        WorkResultEntity createdWorkResult = workResultService.createWorkResult(workResult);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("작업 실적 생성 성공", toWorkResultResponse(createdWorkResult)));
    }

    /**
     * 작업 실적 수정
     * PUT /api/work-results/{id}
     *
     * 수정 시 자동으로 작업 지시의 집계가 재계산됩니다.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "작업 실적 수정", description = "작업 실적 정보 수정 (자동으로 작업 지시 집계 업데이트)")
    public ResponseEntity<ApiResponse<WorkResultResponse>> updateWorkResult(
            @PathVariable Long id,
            @Valid @RequestBody WorkResultUpdateRequest request) {

        log.info("Updating work result: {}", id);

        WorkResultEntity workResult = workResultService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_RESULT_NOT_FOUND));

        if (request.getResultDate() != null) {
            workResult.setResultDate(request.getResultDate());
        }
        if (request.getQuantity() != null) {
            workResult.setQuantity(request.getQuantity());
        }
        if (request.getGoodQuantity() != null) {
            workResult.setGoodQuantity(request.getGoodQuantity());
        }
        if (request.getDefectQuantity() != null) {
            workResult.setDefectQuantity(request.getDefectQuantity());
        }
        if (request.getWorkStartTime() != null) {
            workResult.setWorkStartTime(request.getWorkStartTime());
        }
        if (request.getWorkEndTime() != null) {
            workResult.setWorkEndTime(request.getWorkEndTime());
        }
        if (request.getWorkDuration() != null) {
            workResult.setWorkDuration(request.getWorkDuration());
        }
        if (request.getWorkerId() != null) {
            UserEntity worker = userRepository.findById(request.getWorkerId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
            workResult.setWorker(worker);
        }
        if (request.getWorkerName() != null) {
            workResult.setWorkerName(request.getWorkerName());
        }
        if (request.getDefectReason() != null) {
            workResult.setDefectReason(request.getDefectReason());
        }
        if (request.getRemarks() != null) {
            workResult.setRemarks(request.getRemarks());
        }

        // Service에서 자동으로 workOrderService.recalculateAggregates() 호출
        WorkResultEntity updatedWorkResult = workResultService.updateWorkResult(workResult);

        return ResponseEntity.ok(ApiResponse.success("작업 실적 수정 성공", toWorkResultResponse(updatedWorkResult)));
    }

    /**
     * 작업 실적 삭제
     * DELETE /api/work-results/{id}
     *
     * 삭제 시 자동으로 작업 지시의 집계가 재계산됩니다.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "작업 실적 삭제", description = "작업 실적 삭제 (자동으로 작업 지시 집계 업데이트)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkResult(@PathVariable Long id) {
        log.info("Deleting work result: {}", id);

        // Service에서 자동으로 workOrderService.recalculateAggregates() 호출
        workResultService.deleteWorkResult(id);

        return ResponseEntity.ok(ApiResponse.success("작업 실적 삭제 성공", null));
    }

    /**
     * 작업 지시의 실적 개수 조회
     * GET /api/work-results/work-order/{workOrderId}/count
     */
    @GetMapping("/work-order/{workOrderId}/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 지시의 실적 개수", description = "특정 작업 지시의 실적 개수 조회")
    public ResponseEntity<ApiResponse<Long>> countWorkResults(@PathVariable Long workOrderId) {
        log.info("Counting work results for work order: {}", workOrderId);

        long count = workResultService.countByWorkOrder(workOrderId);

        return ResponseEntity.ok(ApiResponse.success("실적 개수 조회 성공", count));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private WorkResultResponse toWorkResultResponse(WorkResultEntity workResult) {
        return WorkResultResponse.builder()
                .workResultId(workResult.getWorkResultId())
                // Work order
                .workOrderId(workResult.getWorkOrder().getWorkOrderId())
                .workOrderNo(workResult.getWorkOrder().getWorkOrderNo())
                // Date
                .resultDate(workResult.getResultDate())
                // Quantities
                .quantity(workResult.getQuantity())
                .goodQuantity(workResult.getGoodQuantity())
                .defectQuantity(workResult.getDefectQuantity())
                // Work time
                .workStartTime(workResult.getWorkStartTime())
                .workEndTime(workResult.getWorkEndTime())
                .workDuration(workResult.getWorkDuration())
                // Worker (may be null)
                .workerId(workResult.getWorker() != null ? workResult.getWorker().getUserId() : null)
                .workerName(workResult.getWorkerName())
                // Others
                .defectReason(workResult.getDefectReason())
                .tenantId(workResult.getTenant().getTenantId())
                .tenantName(workResult.getTenant().getTenantName())
                .remarks(workResult.getRemarks())
                .createdAt(workResult.getCreatedAt())
                .updatedAt(workResult.getUpdatedAt())
                .build();
    }
}
