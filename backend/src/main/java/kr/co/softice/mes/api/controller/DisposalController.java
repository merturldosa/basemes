package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.DisposalEntity;
import kr.co.softice.mes.domain.service.DisposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Disposal Controller
 * 폐기 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/disposals")
@RequiredArgsConstructor
@Tag(name = "Disposal", description = "폐기 관리 API")
public class DisposalController {

    private final DisposalService disposalService;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER', 'USER')")
    @Operation(summary = "폐기 목록 조회", description = "모든 폐기를 조회합니다.")
    public ResponseEntity<List<DisposalEntity>> getAllDisposals() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all disposals for tenant: {}", tenantId);
        List<DisposalEntity> disposals = disposalService.findAllByTenant(tenantId);
        return ResponseEntity.ok(disposals);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{disposalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER', 'USER')")
    @Operation(summary = "폐기 상세 조회", description = "ID로 폐기를 조회합니다.")
    public ResponseEntity<DisposalEntity> getDisposalById(@PathVariable Long disposalId) {
        log.info("Getting disposal by ID: {}", disposalId);
        DisposalEntity disposal = disposalService.findById(disposalId);
        return ResponseEntity.ok(disposal);
    }

    @Transactional(readOnly = true)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER', 'USER')")
    @Operation(summary = "상태별 폐기 조회", description = "특정 상태의 폐기를 조회합니다.")
    public ResponseEntity<List<DisposalEntity>> getDisposalsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting disposals by status: {} for tenant: {}", status, tenantId);
        List<DisposalEntity> disposals = disposalService.findByStatus(tenantId, status);
        return ResponseEntity.ok(disposals);
    }

    @Transactional(readOnly = true)
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER', 'USER')")
    @Operation(summary = "유형별 폐기 조회", description = "특정 유형의 폐기를 조회합니다.")
    public ResponseEntity<List<DisposalEntity>> getDisposalsByType(@PathVariable String type) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting disposals by type: {} for tenant: {}", type, tenantId);
        List<DisposalEntity> disposals = disposalService.findByType(tenantId, type);
        return ResponseEntity.ok(disposals);
    }

    @Transactional(readOnly = true)
    @GetMapping("/approved")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "승인된 폐기 조회", description = "처리 대기 중인 승인된 폐기를 조회합니다.")
    public ResponseEntity<List<DisposalEntity>> getApprovedDisposals() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting approved disposals for tenant: {}", tenantId);
        List<DisposalEntity> disposals = disposalService.findApprovedDisposals(tenantId);
        return ResponseEntity.ok(disposals);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "폐기 등록", description = "새로운 폐기를 등록합니다.")
    public ResponseEntity<DisposalEntity> createDisposal(@RequestBody DisposalEntity disposal) {
        log.info("Creating disposal");
        DisposalEntity created = disposalService.createDisposal(disposal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{disposalId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 승인", description = "폐기를 승인합니다.")
    public ResponseEntity<DisposalEntity> approveDisposal(
            @PathVariable Long disposalId,
            @RequestParam Long approverId,
            @RequestParam String approverName) {
        log.info("Approving disposal ID: {} by: {}", disposalId, approverName);
        DisposalEntity approved = disposalService.approve(disposalId, approverId, approverName);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{disposalId}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 처리", description = "폐기를 처리합니다.")
    public ResponseEntity<DisposalEntity> processDisposal(
            @PathVariable Long disposalId,
            @RequestParam Long processorId,
            @RequestParam String processorName,
            @RequestParam(required = false) String disposalMethod,
            @RequestParam(required = false) String disposalLocation) {
        log.info("Processing disposal ID: {} by: {}", disposalId, processorName);
        DisposalEntity processed = disposalService.process(
                disposalId, processorId, processorName, disposalMethod, disposalLocation);
        return ResponseEntity.ok(processed);
    }

    @PostMapping("/{disposalId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 완료", description = "폐기 처리를 완료합니다.")
    public ResponseEntity<DisposalEntity> completeDisposal(@PathVariable Long disposalId) {
        log.info("Completing disposal ID: {}", disposalId);
        DisposalEntity completed = disposalService.complete(disposalId);
        return ResponseEntity.ok(completed);
    }

    @PostMapping("/{disposalId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 반려", description = "폐기를 반려합니다.")
    public ResponseEntity<DisposalEntity> rejectDisposal(
            @PathVariable Long disposalId,
            @RequestParam String reason) {
        log.info("Rejecting disposal ID: {}", disposalId);
        DisposalEntity rejected = disposalService.reject(disposalId, reason);
        return ResponseEntity.ok(rejected);
    }

    @PostMapping("/{disposalId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 취소", description = "폐기를 취소합니다.")
    public ResponseEntity<DisposalEntity> cancelDisposal(
            @PathVariable Long disposalId,
            @RequestParam String reason) {
        log.info("Cancelling disposal ID: {}", disposalId);
        DisposalEntity cancelled = disposalService.cancel(disposalId, reason);
        return ResponseEntity.ok(cancelled);
    }

    @DeleteMapping("/{disposalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "폐기 삭제", description = "폐기를 삭제합니다.")
    public ResponseEntity<Void> deleteDisposal(@PathVariable Long disposalId) {
        log.info("Deleting disposal ID: {}", disposalId);
        disposalService.deleteDisposal(disposalId);
        return ResponseEntity.ok().build();
    }
}
