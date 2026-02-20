package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.ApprovalDelegationEntity;
import kr.co.softice.mes.domain.entity.ApprovalInstanceEntity;
import kr.co.softice.mes.domain.entity.ApprovalLineEntity;
import kr.co.softice.mes.domain.entity.ApprovalLineTemplateEntity;
import kr.co.softice.mes.domain.service.ApprovalLineService;
import kr.co.softice.mes.domain.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Approval Controller
 * 결재 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Approval", description = "결재 관리 API")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ApprovalLineService approvalLineService;

    // ==================== Approval Lines ====================

    @Transactional(readOnly = true)
    @GetMapping("/lines")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재라인 목록 조회", description = "모든 결재라인을 조회합니다.")
    public ResponseEntity<List<ApprovalLineEntity>> getAllApprovalLines() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all approval lines for tenant: {}", tenantId);
        List<ApprovalLineEntity> lines = approvalLineService.getAllApprovalLinesByTenant(tenantId);
        return ResponseEntity.ok(lines);
    }

    @Transactional(readOnly = true)
    @GetMapping("/lines/{approvalLineId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재라인 상세 조회", description = "ID로 결재라인을 조회합니다.")
    public ResponseEntity<ApprovalLineEntity> getApprovalLineById(@PathVariable Long approvalLineId) {
        log.info("Getting approval line by ID: {}", approvalLineId);
        ApprovalLineEntity line = approvalLineService.getApprovalLineById(approvalLineId);
        return ResponseEntity.ok(line);
    }

    @Transactional(readOnly = true)
    @GetMapping("/lines/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 결재라인 조회", description = "활성 상태의 결재라인을 조회합니다.")
    public ResponseEntity<List<ApprovalLineEntity>> getActiveApprovalLines() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active approval lines for tenant: {}", tenantId);
        List<ApprovalLineEntity> lines = approvalLineService.getActiveApprovalLines(tenantId);
        return ResponseEntity.ok(lines);
    }

    @Transactional(readOnly = true)
    @GetMapping("/lines/document-type/{documentType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "문서유형별 결재라인 조회", description = "문서 유형별 결재라인을 조회합니다.")
    public ResponseEntity<List<ApprovalLineEntity>> getApprovalLinesByDocumentType(@PathVariable String documentType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting approval lines by document type: {} for tenant: {}", documentType, tenantId);
        List<ApprovalLineEntity> lines = approvalLineService.getApprovalLinesByDocumentType(tenantId, documentType);
        return ResponseEntity.ok(lines);
    }

    @PostMapping("/lines")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVAL_MANAGER')")
    @Operation(summary = "결재라인 등록", description = "새로운 결재라인을 등록합니다.")
    public ResponseEntity<ApprovalLineEntity> createApprovalLine(@RequestBody ApprovalLineEntity line) {
        log.info("Creating approval line: {}", line.getLineCode());
        ApprovalLineEntity created = approvalLineService.createApprovalLine(line);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/lines/{approvalLineId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVAL_MANAGER')")
    @Operation(summary = "결재라인 수정", description = "결재라인 정보를 수정합니다.")
    public ResponseEntity<ApprovalLineEntity> updateApprovalLine(
            @PathVariable Long approvalLineId,
            @RequestBody ApprovalLineEntity line) {
        log.info("Updating approval line ID: {}", approvalLineId);
        ApprovalLineEntity updated = approvalLineService.updateApprovalLine(approvalLineId, line);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/lines/{approvalLineId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "결재라인 삭제", description = "결재라인을 삭제합니다.")
    public ResponseEntity<Void> deleteApprovalLine(@PathVariable Long approvalLineId) {
        log.info("Deleting approval line ID: {}", approvalLineId);
        approvalLineService.deleteApprovalLine(approvalLineId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/lines/{approvalLineId}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVAL_MANAGER')")
    @Operation(summary = "결재라인 활성/비활성 토글", description = "결재라인 활성 상태를 토글합니다.")
    public ResponseEntity<ApprovalLineEntity> toggleApprovalLineActive(@PathVariable Long approvalLineId) {
        log.info("Toggling active status for approval line ID: {}", approvalLineId);
        ApprovalLineEntity toggled = approvalLineService.toggleActive(approvalLineId);
        return ResponseEntity.ok(toggled);
    }

    // ==================== Approval Templates ====================

    @Transactional(readOnly = true)
    @GetMapping("/templates")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 템플릿 목록 조회", description = "모든 결재 템플릿을 조회합니다.")
    public ResponseEntity<List<ApprovalLineTemplateEntity>> getAllTemplates() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all approval templates for tenant: {}", tenantId);
        List<ApprovalLineTemplateEntity> templates = approvalService.findAllTemplates(tenantId);
        return ResponseEntity.ok(templates);
    }

    @Transactional(readOnly = true)
    @GetMapping("/templates/{templateId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 템플릿 상세 조회", description = "ID로 결재 템플릿을 조회합니다.")
    public ResponseEntity<ApprovalLineTemplateEntity> getTemplateById(@PathVariable Long templateId) {
        log.info("Getting approval template by ID: {}", templateId);
        return approvalService.findTemplateById(templateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVAL_MANAGER')")
    @Operation(summary = "결재 템플릿 등록", description = "새로운 결재 템플릿을 등록합니다.")
    public ResponseEntity<ApprovalLineTemplateEntity> createTemplate(@RequestBody ApprovalLineTemplateEntity template) {
        log.info("Creating approval template: {}", template.getTemplateName());
        ApprovalLineTemplateEntity created = approvalService.createTemplate(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/templates/{templateId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVAL_MANAGER')")
    @Operation(summary = "결재 템플릿 수정", description = "결재 템플릿을 수정합니다.")
    public ResponseEntity<ApprovalLineTemplateEntity> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody ApprovalLineTemplateEntity template) {
        log.info("Updating approval template ID: {}", templateId);
        template.setTemplateId(templateId);
        ApprovalLineTemplateEntity updated = approvalService.updateTemplate(template);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "결재 템플릿 삭제", description = "결재 템플릿을 삭제합니다.")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId) {
        log.info("Deleting approval template ID: {}", templateId);
        approvalService.deleteTemplate(templateId);
        return ResponseEntity.ok().build();
    }

    // ==================== Approval Instances ====================

    @Transactional(readOnly = true)
    @GetMapping("/instances/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "대기 중인 결재 조회", description = "현재 사용자의 대기 중인 결재를 조회합니다.")
    public ResponseEntity<List<ApprovalInstanceEntity>> getPendingApprovals(@RequestParam Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting pending approvals for user: {} in tenant: {}", userId, tenantId);
        List<ApprovalInstanceEntity> instances = approvalService.findPendingApprovalsForUser(tenantId, userId);
        return ResponseEntity.ok(instances);
    }

    @Transactional(readOnly = true)
    @GetMapping("/instances/my-requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 결재 요청 조회", description = "현재 사용자가 요청한 결재를 조회합니다.")
    public ResponseEntity<List<ApprovalInstanceEntity>> getMyRequests(@RequestParam Long requesterId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting approval requests for requester: {} in tenant: {}", requesterId, tenantId);
        List<ApprovalInstanceEntity> instances = approvalService.findInstancesByRequester(tenantId, requesterId);
        return ResponseEntity.ok(instances);
    }

    @PostMapping("/instances/{instanceId}/steps/{stepInstanceId}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 승인", description = "결재 단계를 승인합니다.")
    public ResponseEntity<Void> approveStep(
            @PathVariable Long instanceId,
            @PathVariable Long stepInstanceId,
            @RequestParam Long approverId,
            @RequestParam(required = false) String comment) {
        log.info("Approving step: {} in instance: {} by: {}", stepInstanceId, instanceId, approverId);
        approvalService.approveStep(instanceId, stepInstanceId, approverId, comment);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/instances/{instanceId}/steps/{stepInstanceId}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 반려", description = "결재 단계를 반려합니다.")
    public ResponseEntity<Void> rejectStep(
            @PathVariable Long instanceId,
            @PathVariable Long stepInstanceId,
            @RequestParam Long approverId,
            @RequestParam String reason) {
        log.info("Rejecting step: {} in instance: {} by: {}", stepInstanceId, instanceId, approverId);
        approvalService.rejectStep(instanceId, stepInstanceId, approverId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/instances/{instanceId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 취소", description = "결재 요청을 취소합니다.")
    public ResponseEntity<Void> cancelInstance(
            @PathVariable Long instanceId,
            @RequestParam Long requesterId) {
        log.info("Cancelling instance: {} by requester: {}", instanceId, requesterId);
        approvalService.cancelInstance(instanceId, requesterId);
        return ResponseEntity.ok().build();
    }

    // ==================== Delegation ====================

    @Transactional(readOnly = true)
    @GetMapping("/delegations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 위임 목록 조회", description = "현재 유효한 결재 위임을 조회합니다.")
    public ResponseEntity<List<ApprovalDelegationEntity>> getCurrentDelegations() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting current delegations for tenant: {}", tenantId);
        List<ApprovalDelegationEntity> delegations = approvalService.findCurrentDelegations(tenantId);
        return ResponseEntity.ok(delegations);
    }

    @Transactional(readOnly = true)
    @GetMapping("/delegations/user/{delegatorId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자별 위임 조회", description = "특정 사용자의 결재 위임을 조회합니다.")
    public ResponseEntity<List<ApprovalDelegationEntity>> getDelegationsByDelegator(@PathVariable Long delegatorId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting delegations for delegator: {} in tenant: {}", delegatorId, tenantId);
        List<ApprovalDelegationEntity> delegations = approvalService.findDelegationsByDelegator(tenantId, delegatorId);
        return ResponseEntity.ok(delegations);
    }

    @PostMapping("/delegations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 위임 등록", description = "새로운 결재 위임을 등록합니다.")
    public ResponseEntity<ApprovalDelegationEntity> createDelegation(@RequestBody ApprovalDelegationEntity delegation) {
        log.info("Creating delegation from: {} to: {}", delegation.getDelegatorId(), delegation.getDelegateId());
        ApprovalDelegationEntity created = approvalService.createDelegation(delegation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/delegations/{delegationId}/deactivate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 위임 비활성화", description = "결재 위임을 비활성화합니다.")
    public ResponseEntity<Void> deactivateDelegation(@PathVariable Long delegationId) {
        log.info("Deactivating delegation ID: {}", delegationId);
        approvalService.deactivateDelegation(delegationId);
        return ResponseEntity.ok().build();
    }

    // ==================== Statistics ====================

    @Transactional(readOnly = true)
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결재 통계 조회", description = "결재 통계를 조회합니다.")
    public ResponseEntity<ApprovalService.ApprovalStatistics> getStatistics() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting approval statistics for tenant: {}", tenantId);
        ApprovalService.ApprovalStatistics stats = approvalService.getStatistics(tenantId);
        return ResponseEntity.ok(stats);
    }
}
