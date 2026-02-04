package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.CommonCodeDetailEntity;
import kr.co.softice.mes.domain.entity.CommonCodeGroupEntity;
import kr.co.softice.mes.domain.repository.CommonCodeDetailRepository;
import kr.co.softice.mes.domain.repository.CommonCodeGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Common Code Service
 * 공통 코드 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {

    private final CommonCodeGroupRepository codeGroupRepository;
    private final CommonCodeDetailRepository codeDetailRepository;

    // ================== Code Group Methods ==================

    /**
     * Find all code groups by tenant
     */
    public List<CommonCodeGroupEntity> findAllCodeGroups(String tenantId) {
        return codeGroupRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find active code groups by tenant
     */
    public List<CommonCodeGroupEntity> findActiveCodeGroups(String tenantId) {
        return codeGroupRepository.findByTenant_TenantIdAndIsActiveTrue(tenantId);
    }

    /**
     * Find code group by ID
     */
    public Optional<CommonCodeGroupEntity> findCodeGroupById(Long codeGroupId) {
        return codeGroupRepository.findByIdWithAllRelations(codeGroupId);
    }

    /**
     * Find code group by code group name
     */
    public Optional<CommonCodeGroupEntity> findCodeGroupByCode(String tenantId, String codeGroup) {
        return codeGroupRepository.findByTenantIdAndCodeGroupWithDetails(tenantId, codeGroup);
    }

    /**
     * Create code group
     */
    @Transactional
    public CommonCodeGroupEntity createCodeGroup(CommonCodeGroupEntity codeGroup) {
        log.info("Creating code group: {} for tenant: {}",
            codeGroup.getCodeGroup(), codeGroup.getTenant().getTenantId());

        // Check duplicate
        if (codeGroupRepository.existsByTenantAndCodeGroup(codeGroup.getTenant(), codeGroup.getCodeGroup())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                "Code group already exists: " + codeGroup.getCodeGroup());
        }

        CommonCodeGroupEntity saved = codeGroupRepository.save(codeGroup);
        log.info("Code group created: {}", saved.getCodeGroupId());

        return saved;
    }

    /**
     * Update code group
     */
    @Transactional
    public CommonCodeGroupEntity updateCodeGroup(CommonCodeGroupEntity codeGroup) {
        log.info("Updating code group: {}", codeGroup.getCodeGroupId());

        CommonCodeGroupEntity existing = codeGroupRepository.findById(codeGroup.getCodeGroupId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // System code groups cannot change code_group value
        if (existing.getIsSystem() && !existing.getCodeGroup().equals(codeGroup.getCodeGroup())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                "Cannot change code group value for system codes");
        }

        existing.setCodeGroupName(codeGroup.getCodeGroupName());
        existing.setDescription(codeGroup.getDescription());
        existing.setDisplayOrder(codeGroup.getDisplayOrder());
        existing.setIsActive(codeGroup.getIsActive());

        CommonCodeGroupEntity updated = codeGroupRepository.save(existing);
        log.info("Code group updated: {}", updated.getCodeGroupId());

        return codeGroupRepository.findByIdWithAllRelations(updated.getCodeGroupId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Delete code group
     */
    @Transactional
    public void deleteCodeGroup(Long codeGroupId) {
        log.info("Deleting code group: {}", codeGroupId);

        CommonCodeGroupEntity codeGroup = codeGroupRepository.findById(codeGroupId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Cannot delete system code groups
        if (codeGroup.getIsSystem()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                "Cannot delete system code group");
        }

        codeGroupRepository.deleteById(codeGroupId);
        log.info("Code group deleted: {}", codeGroupId);
    }

    // ================== Code Detail Methods ==================

    /**
     * Find all code details by code group ID
     */
    public List<CommonCodeDetailEntity> findCodeDetailsByGroupId(Long codeGroupId) {
        return codeDetailRepository.findByCodeGroupIdWithAllRelations(codeGroupId);
    }

    /**
     * Find active code details by code group ID
     */
    public List<CommonCodeDetailEntity> findActiveCodeDetailsByGroupId(Long codeGroupId) {
        return codeDetailRepository.findActiveCodesByCodeGroupIdOrdered(codeGroupId);
    }

    /**
     * Find code details by tenant and code group name
     */
    public List<CommonCodeDetailEntity> findCodeDetailsByCodeGroup(String tenantId, String codeGroup) {
        return codeDetailRepository.findByTenantIdAndCodeGroup(tenantId, codeGroup);
    }

    /**
     * Find code detail by ID
     */
    public Optional<CommonCodeDetailEntity> findCodeDetailById(Long codeDetailId) {
        return codeDetailRepository.findById(codeDetailId);
    }

    /**
     * Find code detail by code group and code
     */
    public Optional<CommonCodeDetailEntity> findCodeDetail(String tenantId, String codeGroup, String code) {
        return codeDetailRepository.findByTenantIdAndCodeGroupAndCode(tenantId, codeGroup, code);
    }

    /**
     * Create code detail
     */
    @Transactional
    public CommonCodeDetailEntity createCodeDetail(CommonCodeDetailEntity codeDetail) {
        log.info("Creating code detail: {} in group: {}",
            codeDetail.getCode(), codeDetail.getCodeGroup().getCodeGroupId());

        // Check duplicate
        if (codeDetailRepository.existsByCodeGroupAndCode(codeDetail.getCodeGroup(), codeDetail.getCode())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                "Code already exists in this group: " + codeDetail.getCode());
        }

        CommonCodeDetailEntity saved = codeDetailRepository.save(codeDetail);
        log.info("Code detail created: {}", saved.getCodeDetailId());

        return saved;
    }

    /**
     * Update code detail
     */
    @Transactional
    public CommonCodeDetailEntity updateCodeDetail(CommonCodeDetailEntity codeDetail) {
        log.info("Updating code detail: {}", codeDetail.getCodeDetailId());

        CommonCodeDetailEntity existing = codeDetailRepository.findById(codeDetail.getCodeDetailId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        existing.setCodeName(codeDetail.getCodeName());
        existing.setDescription(codeDetail.getDescription());
        existing.setDisplayOrder(codeDetail.getDisplayOrder());
        existing.setIsDefault(codeDetail.getIsDefault());
        existing.setIsActive(codeDetail.getIsActive());
        existing.setValue1(codeDetail.getValue1());
        existing.setValue2(codeDetail.getValue2());
        existing.setValue3(codeDetail.getValue3());
        existing.setValue4(codeDetail.getValue4());
        existing.setValue5(codeDetail.getValue5());
        existing.setColorCode(codeDetail.getColorCode());
        existing.setIconName(codeDetail.getIconName());

        CommonCodeDetailEntity updated = codeDetailRepository.save(existing);
        log.info("Code detail updated: {}", updated.getCodeDetailId());

        return updated;
    }

    /**
     * Delete code detail
     */
    @Transactional
    public void deleteCodeDetail(Long codeDetailId) {
        log.info("Deleting code detail: {}", codeDetailId);

        CommonCodeDetailEntity codeDetail = codeDetailRepository.findById(codeDetailId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if code group is system code
        if (codeDetail.getCodeGroup().getIsSystem()) {
            log.warn("Attempting to delete code from system code group: {}",
                codeDetail.getCodeGroup().getCodeGroup());
            // Optionally throw exception or just log warning
        }

        codeDetailRepository.deleteById(codeDetailId);
        log.info("Code detail deleted: {}", codeDetailId);
    }

    // ================== Utility Methods ==================

    /**
     * Get all codes as Map (codeGroup -> List<CodeDetail>)
     */
    public Map<String, List<CommonCodeDetailEntity>> getAllCodesAsMap(String tenantId) {
        List<CommonCodeGroupEntity> groups = codeGroupRepository.findByTenantIdWithAllRelations(tenantId);

        return groups.stream()
            .collect(Collectors.toMap(
                CommonCodeGroupEntity::getCodeGroup,
                group -> group.getDetails().stream()
                    .filter(CommonCodeDetailEntity::getIsActive)
                    .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                    .collect(Collectors.toList())
            ));
    }

    /**
     * Get code name by code
     */
    public String getCodeName(String tenantId, String codeGroup, String code) {
        return codeDetailRepository.findByTenantIdAndCodeGroupAndCode(tenantId, codeGroup, code)
            .map(CommonCodeDetailEntity::getCodeName)
            .orElse(code);
    }

    /**
     * Get default code in code group
     */
    public Optional<CommonCodeDetailEntity> getDefaultCode(Long codeGroupId) {
        return codeDetailRepository.findByCodeGroup_CodeGroupIdAndIsDefaultTrue(codeGroupId);
    }

    /**
     * Count code details by code group
     */
    public long countCodeDetails(Long codeGroupId) {
        return codeDetailRepository.countByCodeGroup_CodeGroupId(codeGroupId);
    }

    /**
     * Validate code exists
     */
    public boolean validateCode(String tenantId, String codeGroup, String code) {
        return codeDetailRepository.findByTenantIdAndCodeGroupAndCode(tenantId, codeGroup, code)
            .filter(CommonCodeDetailEntity::getIsActive)
            .isPresent();
    }
}
