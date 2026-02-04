package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Employee Skill Service
 * 사원 스킬 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeSkillService {

    private final EmployeeSkillRepository employeeSkillRepository;
    private final TenantRepository tenantRepository;
    private final EmployeeRepository employeeRepository;
    private final SkillMatrixRepository skillMatrixRepository;

    public List<EmployeeSkillEntity> getAllEmployeeSkills(String tenantId) {
        log.info("Getting all employee skills for tenant: {}", tenantId);
        return employeeSkillRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public EmployeeSkillEntity getEmployeeSkillById(Long employeeSkillId) {
        log.info("Getting employee skill by ID: {}", employeeSkillId);
        return employeeSkillRepository.findByIdWithAllRelations(employeeSkillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_SKILL_NOT_FOUND));
    }

    public List<EmployeeSkillEntity> getSkillsByEmployee(Long employeeId) {
        log.info("Getting skills for employee ID: {}", employeeId);
        return employeeSkillRepository.findByEmployeeId(employeeId);
    }

    public List<EmployeeSkillEntity> getEmployeesBySkill(String tenantId, Long skillId) {
        log.info("Getting employees with skill ID: {} for tenant: {}", skillId, tenantId);
        return employeeSkillRepository.findByTenantIdAndSkillId(tenantId, skillId);
    }

    public List<EmployeeSkillEntity> getEmployeesBySkillAndLevel(String tenantId, Long skillId, Integer minLevel) {
        log.info("Getting employees with skill ID: {} and min level: {} for tenant: {}", skillId, minLevel, tenantId);
        return employeeSkillRepository.findByTenantIdAndSkillIdAndMinLevel(tenantId, skillId, minLevel);
    }

    public List<EmployeeSkillEntity> getExpiringCertifications(String tenantId, LocalDate expiryDate) {
        log.info("Getting expiring certifications before: {} for tenant: {}", expiryDate, tenantId);
        return employeeSkillRepository.findExpiringCertifications(tenantId, expiryDate);
    }

    public List<EmployeeSkillEntity> getPendingAssessments(String tenantId, LocalDate assessmentDate) {
        log.info("Getting pending assessments before: {} for tenant: {}", assessmentDate, tenantId);
        return employeeSkillRepository.findPendingAssessments(tenantId, assessmentDate);
    }

    @Transactional
    public EmployeeSkillEntity createEmployeeSkill(String tenantId, EmployeeSkillEntity employeeSkill) {
        log.info("Creating employee skill for tenant: {}", tenantId);

        // Check duplicate
        if (employeeSkillRepository.existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(
                tenantId, employeeSkill.getEmployee().getEmployeeId(), employeeSkill.getSkill().getSkillId())) {
            throw new BusinessException(ErrorCode.EMPLOYEE_SKILL_ALREADY_EXISTS);
        }

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        employeeSkill.setTenant(tenant);

        EmployeeEntity employee = employeeRepository.findByIdWithAllRelations(employeeSkill.getEmployee().getEmployeeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
        employeeSkill.setEmployee(employee);

        SkillMatrixEntity skill = skillMatrixRepository.findByIdWithAllRelations(employeeSkill.getSkill().getSkillId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SKILL_NOT_FOUND));
        employeeSkill.setSkill(skill);

        // Auto-calculate expiry date if validity period is set
        if (employeeSkill.getAcquisitionDate() != null && skill.getValidityPeriodMonths() != null) {
            employeeSkill.setExpiryDate(employeeSkill.getAcquisitionDate()
                    .plusMonths(skill.getValidityPeriodMonths()));
        }

        // Convert skill level text to numeric
        if (employeeSkill.getSkillLevel() != null && employeeSkill.getSkillLevelNumeric() == null) {
            employeeSkill.setSkillLevelNumeric(convertSkillLevelToNumeric(employeeSkill.getSkillLevel()));
        }

        if (employeeSkill.getIsActive() == null) {
            employeeSkill.setIsActive(true);
        }

        EmployeeSkillEntity saved = employeeSkillRepository.save(employeeSkill);
        log.info("Employee skill created successfully for employee: {}", employee.getEmployeeNo());
        return saved;
    }

    @Transactional
    public EmployeeSkillEntity updateEmployeeSkill(Long employeeSkillId, EmployeeSkillEntity updateData) {
        log.info("Updating employee skill ID: {}", employeeSkillId);

        EmployeeSkillEntity existing = employeeSkillRepository.findByIdWithAllRelations(employeeSkillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_SKILL_NOT_FOUND));

        if (updateData.getSkillLevel() != null) {
            existing.setSkillLevel(updateData.getSkillLevel());
            existing.setSkillLevelNumeric(convertSkillLevelToNumeric(updateData.getSkillLevel()));
        }
        if (updateData.getSkillLevelNumeric() != null) {
            existing.setSkillLevelNumeric(updateData.getSkillLevelNumeric());
        }
        if (updateData.getAcquisitionDate() != null) {
            existing.setAcquisitionDate(updateData.getAcquisitionDate());
            // Recalculate expiry date
            if (existing.getSkill().getValidityPeriodMonths() != null) {
                existing.setExpiryDate(updateData.getAcquisitionDate()
                        .plusMonths(existing.getSkill().getValidityPeriodMonths()));
            }
        }
        if (updateData.getExpiryDate() != null) {
            existing.setExpiryDate(updateData.getExpiryDate());
        }
        if (updateData.getLastAssessmentDate() != null) {
            existing.setLastAssessmentDate(updateData.getLastAssessmentDate());
        }
        if (updateData.getNextAssessmentDate() != null) {
            existing.setNextAssessmentDate(updateData.getNextAssessmentDate());
        }
        if (updateData.getCertificationNo() != null) {
            existing.setCertificationNo(updateData.getCertificationNo());
        }
        if (updateData.getIssuingAuthority() != null) {
            existing.setIssuingAuthority(updateData.getIssuingAuthority());
        }
        if (updateData.getAssessorName() != null) {
            existing.setAssessorName(updateData.getAssessorName());
        }
        if (updateData.getAssessmentScore() != null) {
            existing.setAssessmentScore(updateData.getAssessmentScore());
        }
        if (updateData.getAssessmentResult() != null) {
            existing.setAssessmentResult(updateData.getAssessmentResult());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        EmployeeSkillEntity updated = employeeSkillRepository.save(existing);
        log.info("Employee skill updated successfully");
        return updated;
    }

    @Transactional
    public void deleteEmployeeSkill(Long employeeSkillId) {
        log.info("Deleting employee skill ID: {}", employeeSkillId);

        EmployeeSkillEntity employeeSkill = employeeSkillRepository.findById(employeeSkillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_SKILL_NOT_FOUND));

        employeeSkillRepository.delete(employeeSkill);
        log.info("Employee skill deleted successfully");
    }

    public Long countSkillsByEmployee(Long employeeId) {
        return employeeSkillRepository.countByEmployeeId(employeeId);
    }

    private Integer convertSkillLevelToNumeric(String skillLevel) {
        if (skillLevel == null) return null;
        switch (skillLevel) {
            case "BEGINNER":
                return 1;
            case "INTERMEDIATE":
                return 2;
            case "ADVANCED":
                return 3;
            case "EXPERT":
                return 4;
            case "MASTER":
                return 5;
            default:
                return null;
        }
    }
}
