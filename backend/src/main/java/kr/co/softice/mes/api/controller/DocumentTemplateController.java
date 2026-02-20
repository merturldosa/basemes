package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.DocumentTemplateEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.DocumentTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Document Template Controller
 * 문서 양식 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/document-templates")
@RequiredArgsConstructor
@Tag(name = "Document Template Management", description = "문서 양식 관리 API")
public class DocumentTemplateController {

    private final DocumentTemplateService documentTemplateService;
    private final TenantRepository tenantRepository;

    /**
     * 문서 양식 목록 조회
     * GET /api/document-templates
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "문서 양식 목록 조회", description = "테넌트의 모든 문서 양식 조회")
    public ResponseEntity<ApiResponse<List<DocumentTemplateEntity>>> getTemplates() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting document templates for tenant: {}", tenantId);

        List<DocumentTemplateEntity> templates = documentTemplateService.findAllTemplates(tenantId);

        return ResponseEntity.ok(ApiResponse.success("문서 양식 목록 조회 성공", templates));
    }

    /**
     * 활성 문서 양식 목록 조회
     * GET /api/document-templates/active
     */
    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 문서 양식 목록", description = "활성 상태의 문서 양식만 조회")
    public ResponseEntity<ApiResponse<List<DocumentTemplateEntity>>> getActiveTemplates() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active document templates for tenant: {}", tenantId);

        List<DocumentTemplateEntity> templates = documentTemplateService.findActiveTemplates(tenantId);

        return ResponseEntity.ok(ApiResponse.success("활성 문서 양식 목록 조회 성공", templates));
    }

    /**
     * 문서 양식 상세 조회
     * GET /api/document-templates/{id}
     */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "문서 양식 상세 조회", description = "문서 양식 상세 정보 조회")
    public ResponseEntity<ApiResponse<DocumentTemplateEntity>> getTemplate(@PathVariable Long id) {
        log.info("Getting document template: {}", id);

        DocumentTemplateEntity template = documentTemplateService.findTemplateById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("문서 양식 조회 성공", template));
    }

    /**
     * 유형별 문서 양식 조회
     * GET /api/document-templates/type/{templateType}
     */
    @Transactional(readOnly = true)
    @GetMapping("/type/{templateType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "유형별 문서 양식 조회", description = "특정 유형의 문서 양식 조회")
    public ResponseEntity<ApiResponse<List<DocumentTemplateEntity>>> getTemplatesByType(
            @PathVariable String templateType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting templates by type: {} for tenant: {}", templateType, tenantId);

        List<DocumentTemplateEntity> templates = documentTemplateService.findTemplatesByType(tenantId, templateType);

        return ResponseEntity.ok(ApiResponse.success("유형별 문서 양식 조회 성공", templates));
    }

    /**
     * 카테고리별 문서 양식 조회
     * GET /api/document-templates/category/{category}
     */
    @Transactional(readOnly = true)
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "카테고리별 문서 양식 조회", description = "특정 카테고리의 문서 양식 조회")
    public ResponseEntity<ApiResponse<List<DocumentTemplateEntity>>> getTemplatesByCategory(
            @PathVariable String category) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting templates by category: {} for tenant: {}", category, tenantId);

        List<DocumentTemplateEntity> templates = documentTemplateService.findTemplatesByCategory(tenantId, category);

        return ResponseEntity.ok(ApiResponse.success("카테고리별 문서 양식 조회 성공", templates));
    }

    /**
     * 코드로 최신 버전 문서 양식 조회
     * GET /api/document-templates/by-code/{templateCode}
     */
    @Transactional(readOnly = true)
    @GetMapping("/by-code/{templateCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드로 최신 버전 조회", description = "템플릿 코드로 최신 버전 조회")
    public ResponseEntity<ApiResponse<DocumentTemplateEntity>> getLatestTemplateByCode(
            @PathVariable String templateCode) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting latest template by code: {} for tenant: {}", templateCode, tenantId);

        DocumentTemplateEntity template = documentTemplateService.findLatestTemplateByCode(tenantId, templateCode)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("문서 양식 조회 성공", template));
    }

    /**
     * 문서 양식 전체 버전 조회
     * GET /api/document-templates/versions/{templateCode}
     */
    @Transactional(readOnly = true)
    @GetMapping("/versions/{templateCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "문서 양식 전체 버전 조회", description = "특정 템플릿의 모든 버전 조회")
    public ResponseEntity<ApiResponse<List<DocumentTemplateEntity>>> getAllVersions(
            @PathVariable String templateCode) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all versions of template: {} for tenant: {}", templateCode, tenantId);

        List<DocumentTemplateEntity> templates = documentTemplateService.findAllVersions(tenantId, templateCode);

        return ResponseEntity.ok(ApiResponse.success("문서 양식 버전 목록 조회 성공", templates));
    }

    /**
     * 문서 양식 생성
     * POST /api/document-templates
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "문서 양식 생성", description = "새로운 문서 양식 생성")
    public ResponseEntity<ApiResponse<DocumentTemplateEntity>> createTemplate(
            @RequestBody DocumentTemplateEntity template) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating document template: {} for tenant: {}", template.getTemplateCode(), tenantId);

        // Set tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));
        template.setTenant(tenant);

        DocumentTemplateEntity created = documentTemplateService.createTemplate(template);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("문서 양식 생성 성공", created));
    }

    /**
     * 문서 양식 수정
     * PUT /api/document-templates/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "문서 양식 수정", description = "문서 양식 정보 수정")
    public ResponseEntity<ApiResponse<DocumentTemplateEntity>> updateTemplate(
            @PathVariable Long id,
            @RequestBody DocumentTemplateEntity template) {
        log.info("Updating document template: {}", id);

        template.setTemplateId(id);
        DocumentTemplateEntity updated = documentTemplateService.updateTemplate(template);

        return ResponseEntity.ok(ApiResponse.success("문서 양식 수정 성공", updated));
    }

    /**
     * 새 버전 생성
     * POST /api/document-templates/{templateCode}/new-version
     */
    @PostMapping("/{templateCode}/new-version")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "새 버전 생성", description = "문서 양식의 새 버전 생성")
    public ResponseEntity<ApiResponse<DocumentTemplateEntity>> createNewVersion(
            @PathVariable String templateCode,
            @RequestParam String newVersion) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating new version: {} for template: {} in tenant: {}", newVersion, templateCode, tenantId);

        DocumentTemplateEntity newTemplate = documentTemplateService.createNewVersion(tenantId, templateCode, newVersion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("새 버전 생성 성공", newTemplate));
    }

    /**
     * 문서 양식 삭제 (비활성화)
     * DELETE /api/document-templates/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "문서 양식 삭제", description = "문서 양식 비활성화")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("Deleting document template: {}", id);

        documentTemplateService.deleteTemplate(id);

        return ResponseEntity.ok(ApiResponse.success("문서 양식 삭제 성공", null));
    }

    /**
     * 문서 양식 활성화
     * POST /api/document-templates/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "문서 양식 활성화", description = "비활성 문서 양식 활성화")
    public ResponseEntity<ApiResponse<DocumentTemplateEntity>> activateTemplate(@PathVariable Long id) {
        log.info("Activating document template: {}", id);

        DocumentTemplateEntity template = documentTemplateService.activateTemplate(id);

        return ResponseEntity.ok(ApiResponse.success("문서 양식 활성화 성공", template));
    }

    /**
     * 문서 양식 비활성화
     * POST /api/document-templates/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "문서 양식 비활성화", description = "문서 양식 비활성화")
    public ResponseEntity<ApiResponse<DocumentTemplateEntity>> deactivateTemplate(@PathVariable Long id) {
        log.info("Deactivating document template: {}", id);

        DocumentTemplateEntity template = documentTemplateService.deactivateTemplate(id);

        return ResponseEntity.ok(ApiResponse.success("문서 양식 비활성화 성공", template));
    }
}
