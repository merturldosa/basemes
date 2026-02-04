package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.mold.MoldProductionHistoryCreateRequest;
import kr.co.softice.mes.common.dto.mold.MoldProductionHistoryResponse;
import kr.co.softice.mes.common.dto.mold.MoldProductionHistoryUpdateRequest;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.MoldProductionHistoryService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mold Production History Controller
 * 금형 생산 이력 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/mold-production-histories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mold Production History", description = "금형 생산 이력 관리 API")
public class MoldProductionHistoryController {

    private final MoldProductionHistoryService historyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "금형 생산 이력 목록 조회", description = "모든 금형 생산 이력을 조회합니다.")
    public ResponseEntity<List<MoldProductionHistoryResponse>> getAllHistories() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all mold production histories for tenant: {}", tenantId);

        List<MoldProductionHistoryEntity> histories = historyService.getAllHistories(tenantId);
        List<MoldProductionHistoryResponse> response = histories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{historyId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "금형 생산 이력 상세 조회", description = "ID로 금형 생산 이력을 조회합니다.")
    public ResponseEntity<MoldProductionHistoryResponse> getHistoryById(@PathVariable Long historyId) {
        log.info("Getting mold production history by ID: {}", historyId);

        MoldProductionHistoryEntity history = historyService.getHistoryById(historyId);
        MoldProductionHistoryResponse response = toResponse(history);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mold/{moldId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "금형별 생산 이력 조회", description = "특정 금형의 생산 이력을 조회합니다.")
    public ResponseEntity<List<MoldProductionHistoryResponse>> getHistoriesByMold(@PathVariable Long moldId) {
        log.info("Getting production histories for mold ID: {}", moldId);

        List<MoldProductionHistoryEntity> histories = historyService.getHistoriesByMold(moldId);
        List<MoldProductionHistoryResponse> response = histories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기간별 생산 이력 조회", description = "특정 기간의 생산 이력을 조회합니다.")
    public ResponseEntity<List<MoldProductionHistoryResponse>> getHistoriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting production histories for tenant: {} from {} to {}", tenantId, startDate, endDate);

        List<MoldProductionHistoryEntity> histories = historyService.getHistoriesByDateRange(tenantId, startDate, endDate);
        List<MoldProductionHistoryResponse> response = histories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업지시별 생산 이력 조회", description = "특정 작업지시의 생산 이력을 조회합니다.")
    public ResponseEntity<List<MoldProductionHistoryResponse>> getHistoriesByWorkOrder(@PathVariable Long workOrderId) {
        log.info("Getting production histories for work order ID: {}", workOrderId);

        List<MoldProductionHistoryEntity> histories = historyService.getHistoriesByWorkOrder(workOrderId);
        List<MoldProductionHistoryResponse> response = histories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'OPERATOR')")
    @Operation(summary = "금형 생산 이력 등록", description = "새로운 금형 생산 이력을 등록합니다.")
    public ResponseEntity<MoldProductionHistoryResponse> createHistory(@Valid @RequestBody MoldProductionHistoryCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating mold production history for tenant: {}", tenantId);

        MoldProductionHistoryEntity history = toEntity(request);
        MoldProductionHistoryEntity created = historyService.createHistory(tenantId, history);
        MoldProductionHistoryResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{historyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "금형 생산 이력 수정", description = "금형 생산 이력을 수정합니다.")
    public ResponseEntity<MoldProductionHistoryResponse> updateHistory(
            @PathVariable Long historyId,
            @Valid @RequestBody MoldProductionHistoryUpdateRequest request) {
        log.info("Updating mold production history ID: {}", historyId);

        MoldProductionHistoryEntity updateData = toEntity(request);
        MoldProductionHistoryEntity updated = historyService.updateHistory(historyId, updateData);
        MoldProductionHistoryResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{historyId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "금형 생산 이력 삭제", description = "금형 생산 이력을 삭제합니다.")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long historyId) {
        log.info("Deleting mold production history ID: {}", historyId);

        historyService.deleteHistory(historyId);

        return ResponseEntity.ok().build();
    }

    private MoldProductionHistoryEntity toEntity(MoldProductionHistoryCreateRequest request) {
        MoldProductionHistoryEntity.MoldProductionHistoryEntityBuilder builder = MoldProductionHistoryEntity.builder()
                .productionDate(request.getProductionDate())
                .shotCount(request.getShotCount())
                .productionQuantity(request.getProductionQuantity())
                .goodQuantity(request.getGoodQuantity())
                .defectQuantity(request.getDefectQuantity())
                .operatorName(request.getOperatorName())
                .remarks(request.getRemarks());

        if (request.getMoldId() != null) {
            builder.mold(MoldEntity.builder().moldId(request.getMoldId()).build());
        }
        if (request.getWorkOrderId() != null) {
            builder.workOrder(WorkOrderEntity.builder().workOrderId(request.getWorkOrderId()).build());
        }
        if (request.getWorkResultId() != null) {
            builder.workResult(WorkResultEntity.builder().workResultId(request.getWorkResultId()).build());
        }
        if (request.getOperatorUserId() != null) {
            builder.operatorUser(UserEntity.builder().userId(request.getOperatorUserId()).build());
        }

        return builder.build();
    }

    private MoldProductionHistoryEntity toEntity(MoldProductionHistoryUpdateRequest request) {
        return MoldProductionHistoryEntity.builder()
                .productionDate(request.getProductionDate())
                .shotCount(request.getShotCount())
                .productionQuantity(request.getProductionQuantity())
                .goodQuantity(request.getGoodQuantity())
                .defectQuantity(request.getDefectQuantity())
                .operatorName(request.getOperatorName())
                .remarks(request.getRemarks())
                .build();
    }

    private MoldProductionHistoryResponse toResponse(MoldProductionHistoryEntity entity) {
        return MoldProductionHistoryResponse.builder()
                .historyId(entity.getHistoryId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .moldId(entity.getMold().getMoldId())
                .moldCode(entity.getMold().getMoldCode())
                .moldName(entity.getMold().getMoldName())
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderNo() : null)
                .workResultId(entity.getWorkResult() != null ? entity.getWorkResult().getWorkResultId() : null)
                .productionDate(entity.getProductionDate())
                .shotCount(entity.getShotCount())
                .cumulativeShotCount(entity.getCumulativeShotCount())
                .productionQuantity(entity.getProductionQuantity())
                .goodQuantity(entity.getGoodQuantity())
                .defectQuantity(entity.getDefectQuantity())
                .operatorUserId(entity.getOperatorUser() != null ? entity.getOperatorUser().getUserId() : null)
                .operatorUsername(entity.getOperatorUser() != null ? entity.getOperatorUser().getUsername() : null)
                .operatorName(entity.getOperatorName())
                .remarks(entity.getRemarks())
                .isActive(true)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
