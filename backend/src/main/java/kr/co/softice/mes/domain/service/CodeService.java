package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.CodeEntity;
import kr.co.softice.mes.domain.entity.CodeGroupEntity;
import kr.co.softice.mes.domain.repository.CodeGroupRepository;
import kr.co.softice.mes.domain.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Code Service
 * 공통 코드 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeRepository codeRepository;

    // ========== Code Group Methods ==========

    /**
     * Find code group by ID
     */
    public Optional<CodeGroupEntity> findCodeGroupById(Long groupId) {
        log.debug("Finding code group by ID: {}", groupId);
        return codeGroupRepository.findById(groupId);
    }

    /**
     * Find code group by tenant and code
     */
    public Optional<CodeGroupEntity> findCodeGroupByTenantAndCode(String tenantId, String groupCode) {
        log.debug("Finding code group by tenant: {} and code: {}", tenantId, groupCode);
        return codeGroupRepository.findByTenant_TenantIdAndGroupCode(tenantId, groupCode);
    }

    /**
     * Find code groups by tenant
     */
    public List<CodeGroupEntity> findCodeGroupsByTenant(String tenantId) {
        log.debug("Finding code groups by tenant: {}", tenantId);
        return codeGroupRepository.findByTenant_TenantId(tenantId);
    }

    /**
     * Create code group
     */
    @Transactional
    public CodeGroupEntity createCodeGroup(CodeGroupEntity codeGroup) {
        log.info("Creating code group: {} for tenant: {}",
                codeGroup.getGroupCode(), codeGroup.getTenant().getTenantId());

        // Check if group code already exists for tenant
        if (codeGroupRepository.existsByTenantAndGroupCode(codeGroup.getTenant(), codeGroup.getGroupCode())) {
            throw new BusinessException(ErrorCode.CODE_GROUP_ALREADY_EXISTS);
        }

        return codeGroupRepository.save(codeGroup);
    }

    /**
     * Update code group
     */
    @Transactional
    public CodeGroupEntity updateCodeGroup(CodeGroupEntity codeGroup) {
        log.info("Updating code group: {}", codeGroup.getGroupId());

        if (!codeGroupRepository.existsById(codeGroup.getGroupId())) {
            throw new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND);
        }

        return codeGroupRepository.save(codeGroup);
    }

    /**
     * Delete code group
     */
    @Transactional
    public void deleteCodeGroup(Long groupId) {
        log.info("Deleting code group: {}", groupId);
        codeGroupRepository.deleteById(groupId);
    }

    // ========== Code Methods ==========

    /**
     * Find code by ID
     */
    public Optional<CodeEntity> findCodeById(Long codeId) {
        log.debug("Finding code by ID: {}", codeId);
        return codeRepository.findById(codeId);
    }

    /**
     * Find code by group and code
     */
    public Optional<CodeEntity> findCodeByGroupAndCode(Long groupId, String code) {
        log.debug("Finding code by group: {} and code: {}", groupId, code);

        CodeGroupEntity codeGroup = codeGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));

        return codeRepository.findByCodeGroupAndCode(codeGroup, code);
    }

    /**
     * Find codes by group
     */
    public List<CodeEntity> findCodesByGroup(Long groupId) {
        log.debug("Finding codes by group: {}", groupId);

        CodeGroupEntity codeGroup = codeGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));

        return codeRepository.findByCodeGroupOrderByDisplayOrderAsc(codeGroup);
    }

    /**
     * Find active codes by group
     */
    public List<CodeEntity> findActiveCodesByGroup(Long groupId) {
        log.debug("Finding active codes by group: {}", groupId);

        CodeGroupEntity codeGroup = codeGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));

        return codeRepository.findByCodeGroupAndStatusOrderByDisplayOrderAsc(codeGroup, "active");
    }

    /**
     * Create code
     */
    @Transactional
    public CodeEntity createCode(CodeEntity code) {
        log.info("Creating code: {} for group: {}",
                code.getCode(), code.getCodeGroup().getGroupId());

        // Check if code already exists for group
        if (codeRepository.existsByCodeGroupAndCode(code.getCodeGroup(), code.getCode())) {
            throw new BusinessException(ErrorCode.CODE_ALREADY_EXISTS);
        }

        return codeRepository.save(code);
    }

    /**
     * Update code
     */
    @Transactional
    public CodeEntity updateCode(CodeEntity code) {
        log.info("Updating code: {}", code.getCodeId());

        if (!codeRepository.existsById(code.getCodeId())) {
            throw new BusinessException(ErrorCode.CODE_NOT_FOUND);
        }

        return codeRepository.save(code);
    }

    /**
     * Delete code
     */
    @Transactional
    public void deleteCode(Long codeId) {
        log.info("Deleting code: {}", codeId);
        codeRepository.deleteById(codeId);
    }
}
