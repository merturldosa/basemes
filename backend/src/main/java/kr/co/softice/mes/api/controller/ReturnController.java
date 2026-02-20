package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.ReturnEntity;
import kr.co.softice.mes.domain.service.ReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Return Controller
 * 반품 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@Tag(name = "Return", description = "반품 관리 API")
public class ReturnController {

    private final ReturnService returnService;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER', 'USER')")
    @Operation(summary = "반품 목록 조회", description = "모든 반품을 조회합니다.")
    public ResponseEntity<List<ReturnEntity>> getAllReturns() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all returns for tenant: {}", tenantId);
        List<ReturnEntity> returns = returnService.findAllByTenant(tenantId);
        return ResponseEntity.ok(returns);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{returnId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER', 'USER')")
    @Operation(summary = "반품 상세 조회", description = "ID로 반품을 조회합니다.")
    public ResponseEntity<ReturnEntity> getReturnById(@PathVariable Long returnId) {
        log.info("Getting return by ID: {}", returnId);
        ReturnEntity returnEntity = returnService.findById(returnId);
        return ResponseEntity.ok(returnEntity);
    }

    @Transactional(readOnly = true)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER', 'USER')")
    @Operation(summary = "상태별 반품 조회", description = "특정 상태의 반품을 조회합니다.")
    public ResponseEntity<List<ReturnEntity>> getReturnsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting returns by status: {} for tenant: {}", status, tenantId);
        List<ReturnEntity> returns = returnService.findByStatus(tenantId, status);
        return ResponseEntity.ok(returns);
    }

    @Transactional(readOnly = true)
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER', 'USER')")
    @Operation(summary = "유형별 반품 조회", description = "특정 유형의 반품을 조회합니다.")
    public ResponseEntity<List<ReturnEntity>> getReturnsByType(@PathVariable String type) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting returns by type: {} for tenant: {}", type, tenantId);
        List<ReturnEntity> returns = returnService.findByType(tenantId, type);
        return ResponseEntity.ok(returns);
    }

    @Transactional(readOnly = true)
    @GetMapping("/requiring-inspection")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'QUALITY_MANAGER')")
    @Operation(summary = "검사 필요 반품 조회", description = "검사가 필요한 반품을 조회합니다.")
    public ResponseEntity<List<ReturnEntity>> getReturnsRequiringInspection() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting returns requiring inspection for tenant: {}", tenantId);
        List<ReturnEntity> returns = returnService.findRequiringInspection(tenantId);
        return ResponseEntity.ok(returns);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "반품 등록", description = "새로운 반품을 등록합니다.")
    public ResponseEntity<ReturnEntity> createReturn(@RequestBody ReturnEntity returnEntity) {
        log.info("Creating return");
        ReturnEntity created = returnService.createReturn(returnEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{returnId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 승인", description = "반품을 승인합니다.")
    public ResponseEntity<ReturnEntity> approveReturn(
            @PathVariable Long returnId,
            @RequestParam Long approverId,
            @RequestParam String approverName) {
        log.info("Approving return ID: {} by: {}", returnId, approverName);
        ReturnEntity approved = returnService.approve(returnId, approverId, approverName);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{returnId}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 입고 확인", description = "반품 입고를 확인합니다.")
    public ResponseEntity<ReturnEntity> receiveReturn(@PathVariable Long returnId) {
        log.info("Receiving return ID: {}", returnId);
        ReturnEntity received = returnService.receive(returnId);
        return ResponseEntity.ok(received);
    }

    @PostMapping("/{returnId}/inspect")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "반품 검사 시작", description = "반품 검사를 시작합니다.")
    public ResponseEntity<ReturnEntity> startInspection(@PathVariable Long returnId) {
        log.info("Starting inspection for return ID: {}", returnId);
        ReturnEntity inspecting = returnService.startInspection(returnId);
        return ResponseEntity.ok(inspecting);
    }

    @PostMapping("/{returnId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 완료", description = "반품 처리를 완료합니다.")
    public ResponseEntity<ReturnEntity> completeReturn(@PathVariable Long returnId) {
        log.info("Completing return ID: {}", returnId);
        ReturnEntity completed = returnService.complete(returnId);
        return ResponseEntity.ok(completed);
    }

    @PostMapping("/{returnId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 반려", description = "반품을 반려합니다.")
    public ResponseEntity<ReturnEntity> rejectReturn(
            @PathVariable Long returnId,
            @RequestParam String reason) {
        log.info("Rejecting return ID: {}", returnId);
        ReturnEntity rejected = returnService.reject(returnId, reason);
        return ResponseEntity.ok(rejected);
    }

    @PostMapping("/{returnId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "반품 취소", description = "반품을 취소합니다.")
    public ResponseEntity<ReturnEntity> cancelReturn(
            @PathVariable Long returnId,
            @RequestParam String reason) {
        log.info("Cancelling return ID: {}", returnId);
        ReturnEntity cancelled = returnService.cancel(returnId, reason);
        return ResponseEntity.ok(cancelled);
    }

    @DeleteMapping("/{returnId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "반품 삭제", description = "반품을 삭제합니다.")
    public ResponseEntity<Void> deleteReturn(@PathVariable Long returnId) {
        log.info("Deleting return ID: {}", returnId);
        returnService.deleteReturn(returnId);
        return ResponseEntity.ok().build();
    }
}
