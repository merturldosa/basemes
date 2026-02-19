package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inspection Form Service
 * 점검 양식 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InspectionFormService {

    private final InspectionFormRepository inspectionFormRepository;
    private final TenantRepository tenantRepository;

    /**
     * Get all inspection forms for tenant
     */
    public List<InspectionFormEntity> getAllForms(String tenantId) {
        log.info("Getting all inspection forms for tenant: {}", tenantId);
        return inspectionFormRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get active inspection forms for tenant
     */
    public List<InspectionFormEntity> getActiveForms(String tenantId) {
        log.info("Getting active inspection forms for tenant: {}", tenantId);
        return inspectionFormRepository.findActiveByTenantId(tenantId);
    }

    /**
     * Get inspection form by ID
     */
    public InspectionFormEntity getFormById(Long formId) {
        log.info("Getting inspection form by ID: {}", formId);
        return inspectionFormRepository.findByIdWithAllRelations(formId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_FORM_NOT_FOUND));
    }

    /**
     * Create inspection form
     */
    @Transactional
    public InspectionFormEntity createForm(String tenantId, InspectionFormEntity form) {
        log.info("Creating inspection form: {} for tenant: {}", form.getFormCode(), tenantId);

        // Check duplicate
        if (inspectionFormRepository.existsByTenant_TenantIdAndFormCode(tenantId, form.getFormCode())) {
            throw new BusinessException(ErrorCode.INSPECTION_FORM_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        form.setTenant(tenant);

        // Set defaults
        if (form.getIsActive() == null) {
            form.setIsActive(true);
        }

        // Set form reference for each field
        if (form.getFields() != null) {
            for (InspectionFormFieldEntity field : form.getFields()) {
                field.setForm(form);
            }
        }

        InspectionFormEntity saved = inspectionFormRepository.save(form);
        log.info("Inspection form created successfully: {}", saved.getFormCode());
        return saved;
    }

    /**
     * Update inspection form
     */
    @Transactional
    public InspectionFormEntity updateForm(Long formId, InspectionFormEntity updateData) {
        log.info("Updating inspection form ID: {}", formId);

        InspectionFormEntity existing = inspectionFormRepository.findByIdWithAllRelations(formId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_FORM_NOT_FOUND));

        // Update non-null fields
        if (updateData.getFormName() != null) {
            existing.setFormName(updateData.getFormName());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getEquipmentType() != null) {
            existing.setEquipmentType(updateData.getEquipmentType());
        }
        if (updateData.getInspectionType() != null) {
            existing.setInspectionType(updateData.getInspectionType());
        }

        // Update fields if provided
        if (updateData.getFields() != null) {
            existing.getFields().clear();
            for (InspectionFormFieldEntity field : updateData.getFields()) {
                field.setForm(existing);
                existing.getFields().add(field);
            }
        }

        InspectionFormEntity updated = inspectionFormRepository.save(existing);
        log.info("Inspection form updated successfully: {}", updated.getFormCode());
        return updated;
    }

    /**
     * Delete inspection form
     */
    @Transactional
    public void deleteForm(Long formId) {
        log.info("Deleting inspection form ID: {}", formId);

        InspectionFormEntity form = inspectionFormRepository.findById(formId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_FORM_NOT_FOUND));

        inspectionFormRepository.delete(form);
        log.info("Inspection form deleted successfully: {}", form.getFormCode());
    }
}
