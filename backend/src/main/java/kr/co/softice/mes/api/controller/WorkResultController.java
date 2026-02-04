package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.workresult.WorkResultCreateRequest;
import kr.co.softice.mes.common.dto.workresult.WorkResultResponse;
import kr.co.softice.mes.common.dto.workresult.WorkResultUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.WorkResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final TenantRepository tenantRepository;
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

        List<WorkResultResponse> results = workResultService.findByTenant(tenantId).stream()
                .map(this::toWorkResultResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 실적 목록 조회 성공", results));
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
     * 작업 지시별 작업 실적 조회
     * GET /api/work-results/work-order/{workOrderId}
     */
    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 지시별 작업 실적 조회", description = "특정 작업 지시의 모든 작업 실적 조회")
    public ResponseEntity<ApiResponse<List<WorkResultResponse>>> getWorkResultsByWorkOrder(
            @PathVariable Long workOrderId) {

        log.info("Getting work results for work order: {}", workOrderId);

        List<WorkResultResponse> results = workResultService.findByWorkOrder(workOrderId).stream()
                .map(this::toWorkResultResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 실적 목록 조회 성공", results));
    }

    /**
     * 기간별 작업 실적 조회
     * GET /api/work-results/date-range
     */
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기간별 작업 실적 조회", description = "시작일과 종료일 사이의 작업 실적 조회")
    public ResponseEntity<ApiResponse<List<WorkResultResponse>>> getWorkResultsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting work results by date range: {} to {} for tenant: {}", startDate, endDate, tenantId);

        List<WorkResultResponse> results = workResultService.findByDateRange(tenantId, startDate, endDate).stream()
                .map(this::toWorkResultResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 실적 목록 조회 성공", results));
    }

    /**
     * 작업자별 작업 실적 조회
     * GET /api/work-results/worker/{userId}
     */
    @GetMapping("/worker/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업자별 작업 실적 조회", description = "특정 작업자의 모든 작업 실적 조회")
    public ResponseEntity<ApiResponse<List<WorkResultResponse>>> getWorkResultsByWorker(
            @PathVariable Long userId) {

        log.info("Getting work results for worker: {}", userId);

        List<WorkResultResponse> results = workResultService.findByWorker(userId).stream()
                .map(this::toWorkResultResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 실적 목록 조회 성공", results));
    }

    /**
     * 작업 실적 생성
     * POST /api/work-results
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'OPERATOR')")
    @Operation(summary = "작업 실적 생성", description = "신규 작업 실적 등록")
    public ResponseEntity<ApiResponse<WorkResultResponse>> createWorkResult(
            @Valid @RequestBody WorkResultCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating work result for work order: {} in tenant: {}", request.getWorkOrderId(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        WorkOrderEntity workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        // 작업 시간 계산 (분 단위)
        Integer workDuration = request.getWorkDuration();
        if (workDuration == null) {
            Duration duration = Duration.between(request.getWorkStartTime(), request.getWorkEndTime());
            workDuration = (int) duration.toMinutes();
        }

        WorkResultEntity.WorkResultEntityBuilder builder = WorkResultEntity.builder()
                .tenant(tenant)
                .workOrder(workOrder)
                .resultDate(request.getResultDate())
                .quantity(request.getQuantity())
                .goodQuantity(request.getGoodQuantity())
                .defectQuantity(request.getDefectQuantity())
                .workStartTime(request.getWorkStartTime())
                .workEndTime(request.getWorkEndTime())
                .workDuration(workDuration)
                .workerName(request.getWorkerName())
                .defectReason(request.getDefectReason())
                .remarks(request.getRemarks());

        // 작업자 설정
        if (request.getWorkerId() != null) {
            UserEntity worker = userRepository.findById(request.getWorkerId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
            builder.worker(worker);
        }

        WorkResultEntity workResult = builder.build();
        WorkResultEntity created = workResultService.createWorkResult(workResult);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("작업 실적 생성 성공", toWorkResultResponse(created)));
    }

    /**
     * 작업 실적 수정
     * PUT /api/work-results/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'OPERATOR')")
    @Operation(summary = "작업 실적 수정", description = "작업 실적 정보 수정")
    public ResponseEntity<ApiResponse<WorkResultResponse>> updateWorkResult(
            @PathVariable Long id,
            @Valid @RequestBody WorkResultUpdateRequest request) {

        log.info("Updating work result: {}", id);

        WorkResultEntity workResult = workResultService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_RESULT_NOT_FOUND));

        // 수정 가능한 필드 업데이트
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
        } else if (request.getWorkStartTime() != null || request.getWorkEndTime() != null) {
            // 시작/종료 시간이 변경되면 작업 시간 재계산
            Duration duration = Duration.between(workResult.getWorkStartTime(), workResult.getWorkEndTime());
            workResult.setWorkDuration((int) duration.toMinutes());
        }
        if (request.getWorkerName() != null) {
            workResult.setWorkerName(request.getWorkerName());
        }
        if (request.getWorkerId() != null) {
            UserEntity worker = userRepository.findById(request.getWorkerId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
            workResult.setWorker(worker);
        }
        if (request.getDefectReason() != null) {
            workResult.setDefectReason(request.getDefectReason());
        }
        if (request.getRemarks() != null) {
            workResult.setRemarks(request.getRemarks());
        }

        WorkResultEntity updated = workResultService.updateWorkResult(workResult);

        return ResponseEntity.ok(ApiResponse.success("작업 실적 수정 성공", toWorkResultResponse(updated)));
    }

    /**
     * 작업 실적 삭제
     * DELETE /api/work-results/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "작업 실적 삭제", description = "작업 실적 완전 삭제 (관리자/생산관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkResult(@PathVariable Long id) {
        log.info("Deleting work result: {}", id);

        workResultService.deleteWorkResult(id);

        return ResponseEntity.ok(ApiResponse.success("작업 실적 삭제 성공", null));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private WorkResultResponse toWorkResultResponse(WorkResultEntity entity) {
        return WorkResultResponse.builder()
                .workResultId(entity.getWorkResultId())
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderNo() : null)
                .tenantId(entity.getTenant() != null ? entity.getTenant().getTenantId() : null)
                .tenantName(entity.getTenant() != null ? entity.getTenant().getTenantName() : null)
                .resultDate(entity.getResultDate())
                .quantity(entity.getQuantity())
                .goodQuantity(entity.getGoodQuantity())
                .defectQuantity(entity.getDefectQuantity())
                .workStartTime(entity.getWorkStartTime())
                .workEndTime(entity.getWorkEndTime())
                .workDuration(entity.getWorkDuration())
                .workerId(entity.getWorker() != null ? entity.getWorker().getUserId() : null)
                .workerName(entity.getWorkerName())
                .defectReason(entity.getDefectReason())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
