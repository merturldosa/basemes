package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.SkillMatrixEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.SkillMatrixRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Skill Matrix Service
 * 스킬 매트릭스 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SkillMatrixService {

    private final SkillMatrixRepository skillMatrixRepository;
    private final TenantRepository tenantRepository;

    public List<SkillMatrixEntity> getAllSkills(String tenantId) {
        log.info("Getting all skills for tenant: {}", tenantId);
        return skillMatrixRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public SkillMatrixEntity getSkillById(Long skillId) {
        log.info("Getting skill by ID: {}", skillId);
        return skillMatrixRepository.findByIdWithAllRelations(skillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SKILL_NOT_FOUND));
    }

    public List<SkillMatrixEntity> getActiveSkills(String tenantId) {
        log.info("Getting active skills for tenant: {}", tenantId);
        return skillMatrixRepository.findActiveSkillsByTenantId(tenantId);
    }

    public List<SkillMatrixEntity> getSkillsByCategory(String tenantId, String skillCategory) {
        log.info("Getting skills by category: {} for tenant: {}", skillCategory, tenantId);
        return skillMatrixRepository.findByTenantIdAndSkillCategory(tenantId, skillCategory);
    }

    public List<SkillMatrixEntity> getSkillsRequiringCertification(String tenantId) {
        log.info("Getting skills requiring certification for tenant: {}", tenantId);
        return skillMatrixRepository.findSkillsRequiringCertification(tenantId);
    }

    @Transactional
    public SkillMatrixEntity createSkill(String tenantId, SkillMatrixEntity skill) {
        log.info("Creating skill: {} for tenant: {}", skill.getSkillCode(), tenantId);

        if (skillMatrixRepository.existsByTenant_TenantIdAndSkillCode(tenantId, skill.getSkillCode())) {
            throw new BusinessException(ErrorCode.SKILL_ALREADY_EXISTS);
        }

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        skill.setTenant(tenant);

        if (skill.getIsActive() == null) {
            skill.setIsActive(true);
        }
        if (skill.getCertificationRequired() == null) {
            skill.setCertificationRequired(false);
        }

        SkillMatrixEntity saved = skillMatrixRepository.save(skill);
        log.info("Skill created successfully: {}", saved.getSkillCode());
        return saved;
    }

    @Transactional
    public SkillMatrixEntity updateSkill(Long skillId, SkillMatrixEntity updateData) {
        log.info("Updating skill ID: {}", skillId);

        SkillMatrixEntity existing = skillMatrixRepository.findByIdWithAllRelations(skillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SKILL_NOT_FOUND));

        if (updateData.getSkillName() != null) {
            existing.setSkillName(updateData.getSkillName());
        }
        if (updateData.getSkillCategory() != null) {
            existing.setSkillCategory(updateData.getSkillCategory());
        }
        if (updateData.getSkillLevelDefinition() != null) {
            existing.setSkillLevelDefinition(updateData.getSkillLevelDefinition());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getCertificationRequired() != null) {
            existing.setCertificationRequired(updateData.getCertificationRequired());
        }
        if (updateData.getCertificationName() != null) {
            existing.setCertificationName(updateData.getCertificationName());
        }
        if (updateData.getValidityPeriodMonths() != null) {
            existing.setValidityPeriodMonths(updateData.getValidityPeriodMonths());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        SkillMatrixEntity updated = skillMatrixRepository.save(existing);
        log.info("Skill updated successfully: {}", updated.getSkillCode());
        return updated;
    }

    @Transactional
    public SkillMatrixEntity activate(Long skillId) {
        log.info("Activating skill ID: {}", skillId);

        SkillMatrixEntity skill = skillMatrixRepository.findByIdWithAllRelations(skillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SKILL_NOT_FOUND));

        skill.setIsActive(true);
        return skillMatrixRepository.save(skill);
    }

    @Transactional
    public SkillMatrixEntity deactivate(Long skillId) {
        log.info("Deactivating skill ID: {}", skillId);

        SkillMatrixEntity skill = skillMatrixRepository.findByIdWithAllRelations(skillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SKILL_NOT_FOUND));

        skill.setIsActive(false);
        return skillMatrixRepository.save(skill);
    }

    @Transactional
    public void deleteSkill(Long skillId) {
        log.info("Deleting skill ID: {}", skillId);

        SkillMatrixEntity skill = skillMatrixRepository.findById(skillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SKILL_NOT_FOUND));

        skillMatrixRepository.delete(skill);
        log.info("Skill deleted successfully: {}", skill.getSkillCode());
    }
}
