package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.DocumentTemplateEntity;
import kr.co.softice.mes.domain.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Document Template Service
 * 문서 양식 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTemplateService {

    private final DocumentTemplateRepository documentTemplateRepository;

    /**
     * Find all templates by tenant
     */
    @Transactional(readOnly = true)
    public List<DocumentTemplateEntity> findAllTemplates(String tenantId) {
        log.debug("Finding all document templates for tenant: {}", tenantId);
        return documentTemplateRepository.findAllByTenantId(tenantId);
    }

    /**
     * Find active templates
     */
    @Transactional(readOnly = true)
    public List<DocumentTemplateEntity> findActiveTemplates(String tenantId) {
        log.debug("Finding active document templates for tenant: {}", tenantId);
        return documentTemplateRepository.findActiveByTenantId(tenantId);
    }

    /**
     * Find templates by type
     */
    @Transactional(readOnly = true)
    public List<DocumentTemplateEntity> findTemplatesByType(String tenantId, String templateType) {
        log.debug("Finding templates by type: {} for tenant: {}", templateType, tenantId);
        return documentTemplateRepository.findByTenantIdAndTemplateType(tenantId, templateType);
    }

    /**
     * Find templates by category
     */
    @Transactional(readOnly = true)
    public List<DocumentTemplateEntity> findTemplatesByCategory(String tenantId, String category) {
        log.debug("Finding templates by category: {} for tenant: {}", category, tenantId);
        return documentTemplateRepository.findByTenantIdAndCategory(tenantId, category);
    }

    /**
     * Find template by ID
     */
    @Transactional(readOnly = true)
    public Optional<DocumentTemplateEntity> findTemplateById(Long templateId) {
        log.debug("Finding template by ID: {}", templateId);
        return documentTemplateRepository.findById(templateId);
    }

    /**
     * Find latest template by code
     */
    @Transactional(readOnly = true)
    public Optional<DocumentTemplateEntity> findLatestTemplateByCode(String tenantId, String templateCode) {
        log.debug("Finding latest template by code: {} for tenant: {}", templateCode, tenantId);
        return documentTemplateRepository.findLatestByTenantIdAndTemplateCode(tenantId, templateCode);
    }

    /**
     * Find template by code and version
     */
    @Transactional(readOnly = true)
    public Optional<DocumentTemplateEntity> findTemplateByCodeAndVersion(
            String tenantId, String templateCode, String version) {
        log.debug("Finding template by code: {} version: {} for tenant: {}", templateCode, version, tenantId);
        return documentTemplateRepository.findByTenantIdAndTemplateCodeAndVersion(tenantId, templateCode, version);
    }

    /**
     * Find all versions of a template
     */
    @Transactional(readOnly = true)
    public List<DocumentTemplateEntity> findAllVersions(String tenantId, String templateCode) {
        log.debug("Finding all versions of template: {} for tenant: {}", templateCode, tenantId);
        return documentTemplateRepository.findAllVersionsByTenantIdAndTemplateCode(tenantId, templateCode);
    }

    /**
     * Create document template
     */
    @Transactional
    public DocumentTemplateEntity createTemplate(DocumentTemplateEntity template) {
        log.info("Creating document template: {} for tenant: {}",
                template.getTemplateCode(), template.getTenant().getTenantId());

        // Validate template code uniqueness
        String tenantId = template.getTenant().getTenantId();
        if (documentTemplateRepository.existsByTenantIdAndTemplateCode(tenantId, template.getTemplateCode())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Template code already exists: " + template.getTemplateCode());
        }

        // Set as latest version
        template.setIsLatest(true);

        return documentTemplateRepository.save(template);
    }

    /**
     * Update document template
     */
    @Transactional
    public DocumentTemplateEntity updateTemplate(DocumentTemplateEntity template) {
        log.info("Updating document template: {}", template.getTemplateId());

        // Check if template exists
        DocumentTemplateEntity existing = documentTemplateRepository.findById(template.getTemplateId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Update fields
        existing.setTemplateName(template.getTemplateName());
        existing.setDescription(template.getDescription());
        existing.setTemplateType(template.getTemplateType());
        existing.setCategory(template.getCategory());
        existing.setFileName(template.getFileName());
        existing.setFilePath(template.getFilePath());
        existing.setFileType(template.getFileType());
        existing.setFileSize(template.getFileSize());
        existing.setTemplateContent(template.getTemplateContent());
        existing.setDisplayOrder(template.getDisplayOrder());
        existing.setIsActive(template.getIsActive());

        return documentTemplateRepository.save(existing);
    }

    /**
     * Create new version of template
     */
    @Transactional
    public DocumentTemplateEntity createNewVersion(String tenantId, String templateCode, String newVersion) {
        log.info("Creating new version: {} for template: {} in tenant: {}", newVersion, templateCode, tenantId);

        // Find current latest version
        DocumentTemplateEntity currentLatest = documentTemplateRepository
                .findLatestByTenantIdAndTemplateCode(tenantId, templateCode)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Template not found: " + templateCode));

        // Mark current version as not latest
        currentLatest.setIsLatest(false);
        documentTemplateRepository.save(currentLatest);

        // Create new version (copy from current)
        DocumentTemplateEntity newTemplate = DocumentTemplateEntity.builder()
                .tenant(currentLatest.getTenant())
                .templateCode(currentLatest.getTemplateCode())
                .templateName(currentLatest.getTemplateName())
                .description(currentLatest.getDescription())
                .templateType(currentLatest.getTemplateType())
                .category(currentLatest.getCategory())
                .fileName(currentLatest.getFileName())
                .filePath(currentLatest.getFilePath())
                .fileType(currentLatest.getFileType())
                .fileSize(currentLatest.getFileSize())
                .templateContent(currentLatest.getTemplateContent())
                .version(newVersion)
                .isLatest(true)
                .displayOrder(currentLatest.getDisplayOrder())
                .isActive(currentLatest.getIsActive())
                .build();

        return documentTemplateRepository.save(newTemplate);
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        log.info("Deleting document template: {}", templateId);

        DocumentTemplateEntity template = documentTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Soft delete - deactivate
        template.setIsActive(false);
        documentTemplateRepository.save(template);
    }

    /**
     * Activate template
     */
    @Transactional
    public DocumentTemplateEntity activateTemplate(Long templateId) {
        log.info("Activating document template: {}", templateId);

        DocumentTemplateEntity template = documentTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        template.setIsActive(true);
        return documentTemplateRepository.save(template);
    }

    /**
     * Deactivate template
     */
    @Transactional
    public DocumentTemplateEntity deactivateTemplate(Long templateId) {
        log.info("Deactivating document template: {}", templateId);

        DocumentTemplateEntity template = documentTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        template.setIsActive(false);
        return documentTemplateRepository.save(template);
    }
}
