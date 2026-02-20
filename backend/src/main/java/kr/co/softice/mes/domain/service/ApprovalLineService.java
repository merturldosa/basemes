package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.ApprovalLineEntity;
import kr.co.softice.mes.domain.entity.DepartmentEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.ApprovalLineRepository;
import kr.co.softice.mes.domain.repository.DepartmentRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Approval Line Service
 * 결재라인 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ApprovalLineService {

    private final ApprovalLineRepository approvalLineRepository;
    private final TenantRepository tenantRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Get all approval lines by tenant
     */
    public List<ApprovalLineEntity> getAllApprovalLinesByTenant(String tenantId) {
        log.info("Getting all approval lines for tenant: {}", tenantId);
        return approvalLineRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get approval line by ID
     */
    public ApprovalLineEntity getApprovalLineById(Long approvalLineId) {
        log.info("Getting approval line by ID: {}", approvalLineId);
        return approvalLineRepository.findByIdWithAllRelations(approvalLineId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_LINE_NOT_FOUND));
    }

    /**
     * Get active approval lines by tenant
     */
    public List<ApprovalLineEntity> getActiveApprovalLines(String tenantId) {
        log.info("Getting active approval lines for tenant: {}", tenantId);
        return approvalLineRepository.findActiveApprovalLinesByTenantId(tenantId);
    }

    /**
     * Get approval lines by document type
     */
    public List<ApprovalLineEntity> getApprovalLinesByDocumentType(String tenantId, String documentType) {
        log.info("Getting approval lines by document type: {} for tenant: {}", documentType, tenantId);
        return approvalLineRepository.findByTenantIdAndDocumentType(tenantId, documentType);
    }

    /**
     * Get approval lines by department
     */
    public List<ApprovalLineEntity> getApprovalLinesByDepartment(String tenantId, Long departmentId) {
        log.info("Getting approval lines by department: {} for tenant: {}", departmentId, tenantId);
        return approvalLineRepository.findByTenantIdAndDepartmentId(tenantId, departmentId);
    }

    /**
     * Get default approval line by document type
     */
    public ApprovalLineEntity getDefaultApprovalLine(String tenantId, String documentType) {
        log.info("Getting default approval line for document type: {} and tenant: {}", documentType, tenantId);
        return approvalLineRepository.findDefaultByTenantIdAndDocumentType(tenantId, documentType)
                .orElse(null);
    }

    /**
     * Create approval line
     */
    @Transactional
    public ApprovalLineEntity createApprovalLine(ApprovalLineEntity approvalLine) {
        log.info("Creating approval line: {}", approvalLine.getLineCode());

        // Check duplicate
        if (approvalLineRepository.existsByTenant_TenantIdAndLineCode(
                approvalLine.getTenant().getTenantId(), approvalLine.getLineCode())) {
            throw new BusinessException(ErrorCode.APPROVAL_LINE_ALREADY_EXISTS);
        }

        // Validate tenant
        TenantEntity tenant = tenantRepository.findById(approvalLine.getTenant().getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        approvalLine.setTenant(tenant);

        // Validate department if provided
        if (approvalLine.getDepartment() != null && approvalLine.getDepartment().getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(approvalLine.getDepartment().getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            approvalLine.setDepartment(department);
        }

        // Set default values
        if (approvalLine.getIsActive() == null) {
            approvalLine.setIsActive(true);
        }
        if (approvalLine.getIsDefault() == null) {
            approvalLine.setIsDefault(false);
        }
        if (approvalLine.getPriority() == null) {
            approvalLine.setPriority(0);
        }

        // If setting as default, unset other defaults for same document type
        if (approvalLine.getIsDefault()) {
            ApprovalLineEntity currentDefault = getDefaultApprovalLine(
                    tenant.getTenantId(), approvalLine.getDocumentType());
            if (currentDefault != null) {
                currentDefault.setIsDefault(false);
                approvalLineRepository.save(currentDefault);
            }
        }

        ApprovalLineEntity savedApprovalLine = approvalLineRepository.save(approvalLine);
        log.info("Approval line created successfully: {}", savedApprovalLine.getLineCode());

        return getApprovalLineById(savedApprovalLine.getApprovalLineId());
    }

    /**
     * Update approval line
     */
    @Transactional
    public ApprovalLineEntity updateApprovalLine(Long approvalLineId, ApprovalLineEntity updatedApprovalLine) {
        log.info("Updating approval line ID: {}", approvalLineId);

        ApprovalLineEntity existingApprovalLine = approvalLineRepository.findById(approvalLineId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_LINE_NOT_FOUND));

        // Update fields
        existingApprovalLine.setLineName(updatedApprovalLine.getLineName());
        existingApprovalLine.setDocumentType(updatedApprovalLine.getDocumentType());
        existingApprovalLine.setApprovalSteps(updatedApprovalLine.getApprovalSteps());
        existingApprovalLine.setConditions(updatedApprovalLine.getConditions());
        existingApprovalLine.setPriority(updatedApprovalLine.getPriority());
        existingApprovalLine.setRemarks(updatedApprovalLine.getRemarks());

        // Update department if provided
        if (updatedApprovalLine.getDepartment() != null && updatedApprovalLine.getDepartment().getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(updatedApprovalLine.getDepartment().getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            existingApprovalLine.setDepartment(department);
        }

        // Handle default flag change
        if (updatedApprovalLine.getIsDefault() != null &&
            !updatedApprovalLine.getIsDefault().equals(existingApprovalLine.getIsDefault())) {
            if (updatedApprovalLine.getIsDefault()) {
                // Unset other defaults for same document type
                ApprovalLineEntity currentDefault = getDefaultApprovalLine(
                        existingApprovalLine.getTenant().getTenantId(),
                        existingApprovalLine.getDocumentType());
                if (currentDefault != null && !currentDefault.getApprovalLineId().equals(approvalLineId)) {
                    currentDefault.setIsDefault(false);
                    approvalLineRepository.save(currentDefault);
                }
            }
            existingApprovalLine.setIsDefault(updatedApprovalLine.getIsDefault());
        }

        approvalLineRepository.save(existingApprovalLine);
        log.info("Approval line updated successfully: {}", existingApprovalLine.getLineCode());

        return getApprovalLineById(approvalLineId);
    }

    /**
     * Delete approval line
     */
    @Transactional
    public void deleteApprovalLine(Long approvalLineId) {
        log.info("Deleting approval line ID: {}", approvalLineId);

        ApprovalLineEntity approvalLine = approvalLineRepository.findById(approvalLineId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_LINE_NOT_FOUND));

        approvalLineRepository.delete(approvalLine);
        log.info("Approval line deleted successfully: {}", approvalLine.getLineCode());
    }

    /**
     * Toggle approval line active status
     */
    @Transactional
    public ApprovalLineEntity toggleActive(Long approvalLineId) {
        log.info("Toggling active status for approval line ID: {}", approvalLineId);

        ApprovalLineEntity approvalLine = approvalLineRepository.findById(approvalLineId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_LINE_NOT_FOUND));

        approvalLine.setIsActive(!approvalLine.getIsActive());
        approvalLineRepository.save(approvalLine);

        log.info("Approval line active status toggled to: {}", approvalLine.getIsActive());
        return getApprovalLineById(approvalLineId);
    }
}
