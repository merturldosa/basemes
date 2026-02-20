package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.skill.EmployeeSkillCreateRequest;
import kr.co.softice.mes.common.dto.skill.EmployeeSkillResponse;
import kr.co.softice.mes.common.dto.skill.EmployeeSkillUpdateRequest;
import kr.co.softice.mes.domain.entity.EmployeeEntity;
import kr.co.softice.mes.domain.entity.EmployeeSkillEntity;
import kr.co.softice.mes.domain.entity.SkillMatrixEntity;
import kr.co.softice.mes.domain.service.EmployeeSkillService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Employee Skill Controller
 * 사원 스킬 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/employee-skills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Skill", description = "사원 스킬 관리 API")
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사원 스킬 목록 조회", description = "모든 사원 스킬을 조회합니다.")
    public ResponseEntity<List<EmployeeSkillResponse>> getAllEmployeeSkills() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all employee skills for tenant: {}", tenantId);

        List<EmployeeSkillEntity> employeeSkills = employeeSkillService.getAllEmployeeSkills(tenantId);
        List<EmployeeSkillResponse> response = employeeSkills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{employeeSkillId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사원 스킬 상세 조회", description = "ID로 사원 스킬을 조회합니다.")
    public ResponseEntity<EmployeeSkillResponse> getEmployeeSkillById(@PathVariable Long employeeSkillId) {
        log.info("Getting employee skill by ID: {}", employeeSkillId);

        EmployeeSkillEntity employeeSkill = employeeSkillService.getEmployeeSkillById(employeeSkillId);
        EmployeeSkillResponse response = toResponse(employeeSkill);

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사원별 스킬 조회", description = "특정 사원의 스킬을 조회합니다.")
    public ResponseEntity<List<EmployeeSkillResponse>> getSkillsByEmployee(@PathVariable Long employeeId) {
        log.info("Getting skills for employee ID: {}", employeeId);

        List<EmployeeSkillEntity> employeeSkills = employeeSkillService.getSkillsByEmployee(employeeId);
        List<EmployeeSkillResponse> response = employeeSkills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/skill/{skillId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "스킬별 사원 조회", description = "특정 스킬을 보유한 사원을 조회합니다.")
    public ResponseEntity<List<EmployeeSkillResponse>> getEmployeesBySkill(@PathVariable Long skillId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting employees with skill ID: {} for tenant: {}", skillId, tenantId);

        List<EmployeeSkillEntity> employeeSkills = employeeSkillService.getEmployeesBySkill(tenantId, skillId);
        List<EmployeeSkillResponse> response = employeeSkills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/skill/{skillId}/level/{minLevel}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "스킬 및 레벨별 사원 조회", description = "특정 스킬과 최소 레벨을 만족하는 사원을 조회합니다.")
    public ResponseEntity<List<EmployeeSkillResponse>> getEmployeesBySkillAndLevel(
            @PathVariable Long skillId,
            @PathVariable Integer minLevel) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting employees with skill ID: {} and min level: {} for tenant: {}", skillId, minLevel, tenantId);

        List<EmployeeSkillEntity> employeeSkills = employeeSkillService.getEmployeesBySkillAndLevel(tenantId, skillId, minLevel);
        List<EmployeeSkillResponse> response = employeeSkills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/expiring-certifications")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "만료 예정 자격증 조회", description = "지정한 날짜까지 만료되는 자격증을 조회합니다.")
    public ResponseEntity<List<EmployeeSkillResponse>> getExpiringCertifications(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting expiring certifications before: {} for tenant: {}", expiryDate, tenantId);

        List<EmployeeSkillEntity> employeeSkills = employeeSkillService.getExpiringCertifications(tenantId, expiryDate);
        List<EmployeeSkillResponse> response = employeeSkills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/pending-assessments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "평가 예정 스킬 조회", description = "지정한 날짜까지 평가가 필요한 스킬을 조회합니다.")
    public ResponseEntity<List<EmployeeSkillResponse>> getPendingAssessments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assessmentDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting pending assessments before: {} for tenant: {}", assessmentDate, tenantId);

        List<EmployeeSkillEntity> employeeSkills = employeeSkillService.getPendingAssessments(tenantId, assessmentDate);
        List<EmployeeSkillResponse> response = employeeSkills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "사원 스킬 등록", description = "사원의 스킬을 등록합니다.")
    public ResponseEntity<EmployeeSkillResponse> createEmployeeSkill(@Valid @RequestBody EmployeeSkillCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating employee skill for tenant: {}", tenantId);

        EmployeeSkillEntity employeeSkill = toEntity(request);
        EmployeeSkillEntity created = employeeSkillService.createEmployeeSkill(tenantId, employeeSkill);
        EmployeeSkillResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{employeeSkillId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "사원 스킬 수정", description = "사원 스킬 정보를 수정합니다.")
    public ResponseEntity<EmployeeSkillResponse> updateEmployeeSkill(
            @PathVariable Long employeeSkillId,
            @Valid @RequestBody EmployeeSkillUpdateRequest request) {
        log.info("Updating employee skill ID: {}", employeeSkillId);

        EmployeeSkillEntity updateData = toEntity(request);
        EmployeeSkillEntity updated = employeeSkillService.updateEmployeeSkill(employeeSkillId, updateData);
        EmployeeSkillResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{employeeSkillId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사원 스킬 삭제", description = "사원 스킬을 삭제합니다.")
    public ResponseEntity<Void> deleteEmployeeSkill(@PathVariable Long employeeSkillId) {
        log.info("Deleting employee skill ID: {}", employeeSkillId);

        employeeSkillService.deleteEmployeeSkill(employeeSkillId);

        return ResponseEntity.ok().build();
    }

    private EmployeeSkillEntity toEntity(EmployeeSkillCreateRequest request) {
        EmployeeSkillEntity.EmployeeSkillEntityBuilder builder = EmployeeSkillEntity.builder()
                .skillLevel(request.getSkillLevel())
                .skillLevelNumeric(request.getSkillLevelNumeric())
                .acquisitionDate(request.getAcquisitionDate())
                .expiryDate(request.getExpiryDate())
                .lastAssessmentDate(request.getLastAssessmentDate())
                .nextAssessmentDate(request.getNextAssessmentDate())
                .certificationNo(request.getCertificationNo())
                .issuingAuthority(request.getIssuingAuthority())
                .assessorName(request.getAssessorName())
                .assessmentScore(request.getAssessmentScore())
                .assessmentResult(request.getAssessmentResult())
                .remarks(request.getRemarks());

        if (request.getEmployeeId() != null) {
            builder.employee(EmployeeEntity.builder().employeeId(request.getEmployeeId()).build());
        }
        if (request.getSkillId() != null) {
            builder.skill(SkillMatrixEntity.builder().skillId(request.getSkillId()).build());
        }

        return builder.build();
    }

    private EmployeeSkillEntity toEntity(EmployeeSkillUpdateRequest request) {
        return EmployeeSkillEntity.builder()
                .skillLevel(request.getSkillLevel())
                .skillLevelNumeric(request.getSkillLevelNumeric())
                .acquisitionDate(request.getAcquisitionDate())
                .expiryDate(request.getExpiryDate())
                .lastAssessmentDate(request.getLastAssessmentDate())
                .nextAssessmentDate(request.getNextAssessmentDate())
                .certificationNo(request.getCertificationNo())
                .issuingAuthority(request.getIssuingAuthority())
                .assessorName(request.getAssessorName())
                .assessmentScore(request.getAssessmentScore())
                .assessmentResult(request.getAssessmentResult())
                .remarks(request.getRemarks())
                .build();
    }

    private EmployeeSkillResponse toResponse(EmployeeSkillEntity entity) {
        return EmployeeSkillResponse.builder()
                .employeeSkillId(entity.getEmployeeSkillId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .employeeId(entity.getEmployee().getEmployeeId())
                .employeeNo(entity.getEmployee().getEmployeeNo())
                .employeeName(entity.getEmployee().getFullName())
                .skillId(entity.getSkill().getSkillId())
                .skillCode(entity.getSkill().getSkillCode())
                .skillName(entity.getSkill().getSkillName())
                .skillCategory(entity.getSkill().getSkillCategory())
                .skillLevel(entity.getSkillLevel())
                .skillLevelNumeric(entity.getSkillLevelNumeric())
                .acquisitionDate(entity.getAcquisitionDate())
                .expiryDate(entity.getExpiryDate())
                .lastAssessmentDate(entity.getLastAssessmentDate())
                .nextAssessmentDate(entity.getNextAssessmentDate())
                .certificationNo(entity.getCertificationNo())
                .issuingAuthority(entity.getIssuingAuthority())
                .assessorName(entity.getAssessorName())
                .assessmentScore(entity.getAssessmentScore())
                .assessmentResult(entity.getAssessmentResult())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
