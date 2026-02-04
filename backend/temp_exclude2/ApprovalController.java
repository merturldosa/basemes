package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.ApprovalDelegationEntity;
import kr.co.softice.mes.domain.entity.ApprovalInstanceEntity;
import kr.co.softice.mes.domain.entity.ApprovalLineTemplateEntity;
import kr.co.softice.mes.domain.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Approval Controller
 * 결재 관리 컨트롤러
 *
 * @author Moon Myung-seop
 */
@Tag(name = "Approval", description = "결재 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    // ==================== Template Management ====================

    /**
     * Get all approval templates
     */
    @Operation(summary = "결재 라인 템플릿 목록 조회")
    @GetMapping("/templates")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<ApprovalLineTemplateEntity>>> getAllTemplates(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            List<ApprovalLineTemplateEntity> templates = approvalService.findAllTemplates(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ApprovalLineTemplateEntity>>builder()
                            .success(true)
                            .data(templates)
                            .message("결재 라인 템플릿 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<ApprovalLineTemplateEntity>>builder()
                            .success(false)
                            .message("결재 라인 템플릿 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get template by document type
     */
    @Operation(summary = "문서 타입별 결재 라인 템플릿 조회")
    @GetMapping("/templates/document-type/{documentType}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<ApprovalLineTemplateEntity>>> getTemplatesByDocumentType(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "문서 타입", required = true)
            @PathVariable String documentType) {
        try {
            List<ApprovalLineTemplateEntity> templates = approvalService.findTemplatesByDocumentType(tenantId, documentType);
            return ResponseEntity.ok(
                    ApiResponse.<List<ApprovalLineTemplateEntity>>builder()
                            .success(true)
                            .data(templates)
                            .message("문서 타입별 결재 라인 템플릿 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get templates by document type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<ApprovalLineTemplateEntity>>builder()
                            .success(false)
                            .message("결재 라인 템플릿 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Create template
     */
    @Operation(summary = "결재 라인 템플릿 생성")
    @PostMapping("/templates")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalLineTemplateEntity>> createTemplate(
            @Valid @RequestBody ApprovalLineTemplateEntity template) {
        try {
            ApprovalLineTemplateEntity created = approvalService.createTemplate(template);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<ApprovalLineTemplateEntity>builder()
                            .success(true)
                            .data(created)
                            .message("결재 라인 템플릿 생성 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create template", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ApprovalLineTemplateEntity>builder()
                            .success(false)
                            .message("결재 라인 템플릿 생성 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Update template
     */
    @Operation(summary = "결재 라인 템플릿 수정")
    @PutMapping("/templates/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalLineTemplateEntity>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalLineTemplateEntity template) {
        try {
            template.setTemplateId(id);
            ApprovalLineTemplateEntity updated = approvalService.updateTemplate(template);
            return ResponseEntity.ok(
                    ApiResponse.<ApprovalLineTemplateEntity>builder()
                            .success(true)
                            .data(updated)
                            .message("결재 라인 템플릿 수정 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to update template", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ApprovalLineTemplateEntity>builder()
                            .success(false)
                            .message("결재 라인 템플릿 수정 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    // ==================== Approval Instance Management ====================

    /**
     * Create approval instance
     */
    @Operation(summary = "결재 인스턴스 생성")
    @PostMapping("/instances")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<ApprovalInstanceEntity>> createInstance(
            @Valid @RequestBody CreateApprovalRequest request) {
        try {
            ApprovalInstanceEntity instance = approvalService.createApprovalInstance(
                    request.getTenantId(),
                    request.getDocumentType(),
                    request.getDocumentId(),
                    request.getDocumentNo(),
                    request.getDocumentTitle(),
                    request.getDocumentAmount(),
                    request.getRequesterId(),
                    request.getRequesterName(),
                    request.getRequesterDepartment(),
                    request.getRequestComment()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<ApprovalInstanceEntity>builder()
                            .success(true)
                            .data(instance)
                            .message("결재 인스턴스 생성 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create approval instance", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ApprovalInstanceEntity>builder()
                            .success(false)
                            .message("결재 인스턴스 생성 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Approve step
     */
    @Operation(summary = "결재 단계 승인")
    @PostMapping("/instances/{instanceId}/steps/{stepId}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Void>> approveStep(
            @PathVariable Long instanceId,
            @PathVariable Long stepId,
            @RequestBody ApprovalActionRequest request) {
        try {
            approvalService.approveStep(instanceId, stepId, request.getApproverId(), request.getComment());
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("결재 승인 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to approve step", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("결재 승인 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Reject step
     */
    @Operation(summary = "결재 단계 반려")
    @PostMapping("/instances/{instanceId}/steps/{stepId}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Void>> rejectStep(
            @PathVariable Long instanceId,
            @PathVariable Long stepId,
            @RequestBody ApprovalActionRequest request) {
        try {
            approvalService.rejectStep(instanceId, stepId, request.getApproverId(), request.getReason());
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("결재 반려 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to reject step", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("결재 반려 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get pending approvals for user
     */
    @Operation(summary = "사용자의 대기 중인 결재 목록 조회")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<ApprovalInstanceEntity>>> getPendingApprovals(
            @RequestParam String tenantId,
            @RequestParam Long userId) {
        try {
            List<ApprovalInstanceEntity> instances = approvalService.findPendingApprovalsForUser(tenantId, userId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ApprovalInstanceEntity>>builder()
                            .success(true)
                            .data(instances)
                            .message("대기 중인 결재 목록 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get pending approvals", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<ApprovalInstanceEntity>>builder()
                            .success(false)
                            .message("대기 중인 결재 목록 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get approval statistics
     */
    @Operation(summary = "결재 통계 조회")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ApprovalService.ApprovalStatistics>> getStatistics(
            @RequestParam String tenantId) {
        try {
            ApprovalService.ApprovalStatistics stats = approvalService.getStatistics(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<ApprovalService.ApprovalStatistics>builder()
                            .success(true)
                            .data(stats)
                            .message("결재 통계 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<ApprovalService.ApprovalStatistics>builder()
                            .success(false)
                            .message("결재 통계 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    // ==================== Delegation Management ====================

    /**
     * Create delegation
     */
    @Operation(summary = "결재 위임 생성")
    @PostMapping("/delegations")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<ApprovalDelegationEntity>> createDelegation(
            @Valid @RequestBody ApprovalDelegationEntity delegation) {
        try {
            ApprovalDelegationEntity created = approvalService.createDelegation(delegation);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<ApprovalDelegationEntity>builder()
                            .success(true)
                            .data(created)
                            .message("결재 위임 생성 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create delegation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ApprovalDelegationEntity>builder()
                            .success(false)
                            .message("결재 위임 생성 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get current delegations
     */
    @Operation(summary = "현재 유효한 결재 위임 목록 조회")
    @GetMapping("/delegations/current")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<ApprovalDelegationEntity>>> getCurrentDelegations(
            @RequestParam String tenantId) {
        try {
            List<ApprovalDelegationEntity> delegations = approvalService.findCurrentDelegations(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ApprovalDelegationEntity>>builder()
                            .success(true)
                            .data(delegations)
                            .message("현재 유효한 결재 위임 목록 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get current delegations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<ApprovalDelegationEntity>>builder()
                            .success(false)
                            .message("결재 위임 목록 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    // ==================== Request DTOs ====================

    @lombok.Data
    public static class CreateApprovalRequest {
        private String tenantId;
        private String documentType;
        private Long documentId;
        private String documentNo;
        private String documentTitle;
        private BigDecimal documentAmount;
        private Long requesterId;
        private String requesterName;
        private String requesterDepartment;
        private String requestComment;
    }

    @lombok.Data
    public static class ApprovalActionRequest {
        private Long approverId;
        private String comment;
        private String reason;
    }
}
