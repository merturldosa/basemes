package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.skill.SkillMatrixCreateRequest;
import kr.co.softice.mes.common.dto.skill.SkillMatrixResponse;
import kr.co.softice.mes.common.dto.skill.SkillMatrixUpdateRequest;
import kr.co.softice.mes.domain.entity.SkillMatrixEntity;
import kr.co.softice.mes.domain.service.SkillMatrixService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Skill Matrix Controller
 * 스킬 매트릭스 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/skill-matrix")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Skill Matrix", description = "스킬 매트릭스 관리 API")
public class SkillMatrixController {

    private final SkillMatrixService skillMatrixService;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "스킬 목록 조회", description = "모든 스킬을 조회합니다.")
    public ResponseEntity<List<SkillMatrixResponse>> getAllSkills() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all skills for tenant: {}", tenantId);

        List<SkillMatrixEntity> skills = skillMatrixService.getAllSkills(tenantId);
        List<SkillMatrixResponse> response = skills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 스킬 목록 조회", description = "활성 상태의 스킬을 조회합니다.")
    public ResponseEntity<List<SkillMatrixResponse>> getActiveSkills() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active skills for tenant: {}", tenantId);

        List<SkillMatrixEntity> skills = skillMatrixService.getActiveSkills(tenantId);
        List<SkillMatrixResponse> response = skills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{skillId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "스킬 상세 조회", description = "ID로 스킬을 조회합니다.")
    public ResponseEntity<SkillMatrixResponse> getSkillById(@PathVariable Long skillId) {
        log.info("Getting skill by ID: {}", skillId);

        SkillMatrixEntity skill = skillMatrixService.getSkillById(skillId);
        SkillMatrixResponse response = toResponse(skill);

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "분류별 스킬 조회", description = "특정 분류의 스킬을 조회합니다.")
    public ResponseEntity<List<SkillMatrixResponse>> getSkillsByCategory(@PathVariable String category) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting skills by category: {} for tenant: {}", category, tenantId);

        List<SkillMatrixEntity> skills = skillMatrixService.getSkillsByCategory(tenantId, category);
        List<SkillMatrixResponse> response = skills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/certification-required")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "자격증 필요 스킬 조회", description = "자격증이 필요한 스킬을 조회합니다.")
    public ResponseEntity<List<SkillMatrixResponse>> getSkillsRequiringCertification() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting skills requiring certification for tenant: {}", tenantId);

        List<SkillMatrixEntity> skills = skillMatrixService.getSkillsRequiringCertification(tenantId);
        List<SkillMatrixResponse> response = skills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "스킬 등록", description = "새로운 스킬을 등록합니다.")
    public ResponseEntity<SkillMatrixResponse> createSkill(@Valid @RequestBody SkillMatrixCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating skill: {} for tenant: {}", request.getSkillCode(), tenantId);

        SkillMatrixEntity skill = toEntity(request);
        SkillMatrixEntity created = skillMatrixService.createSkill(tenantId, skill);
        SkillMatrixResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{skillId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "스킬 수정", description = "스킬 정보를 수정합니다.")
    public ResponseEntity<SkillMatrixResponse> updateSkill(
            @PathVariable Long skillId,
            @Valid @RequestBody SkillMatrixUpdateRequest request) {
        log.info("Updating skill ID: {}", skillId);

        SkillMatrixEntity updateData = toEntity(request);
        SkillMatrixEntity updated = skillMatrixService.updateSkill(skillId, updateData);
        SkillMatrixResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{skillId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "스킬 활성화", description = "스킬을 활성화합니다.")
    public ResponseEntity<SkillMatrixResponse> activate(@PathVariable Long skillId) {
        log.info("Activating skill ID: {}", skillId);

        SkillMatrixEntity activated = skillMatrixService.activate(skillId);
        SkillMatrixResponse response = toResponse(activated);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{skillId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "스킬 비활성화", description = "스킬을 비활성화합니다.")
    public ResponseEntity<SkillMatrixResponse> deactivate(@PathVariable Long skillId) {
        log.info("Deactivating skill ID: {}", skillId);

        SkillMatrixEntity deactivated = skillMatrixService.deactivate(skillId);
        SkillMatrixResponse response = toResponse(deactivated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "스킬 삭제", description = "스킬을 삭제합니다.")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long skillId) {
        log.info("Deleting skill ID: {}", skillId);

        skillMatrixService.deleteSkill(skillId);

        return ResponseEntity.ok().build();
    }

    private SkillMatrixEntity toEntity(SkillMatrixCreateRequest request) {
        return SkillMatrixEntity.builder()
                .skillCode(request.getSkillCode())
                .skillName(request.getSkillName())
                .skillCategory(request.getSkillCategory())
                .skillLevelDefinition(request.getSkillLevelDefinition())
                .description(request.getDescription())
                .certificationRequired(request.getCertificationRequired())
                .certificationName(request.getCertificationName())
                .validityPeriodMonths(request.getValidityPeriodMonths())
                .remarks(request.getRemarks())
                .build();
    }

    private SkillMatrixEntity toEntity(SkillMatrixUpdateRequest request) {
        return SkillMatrixEntity.builder()
                .skillName(request.getSkillName())
                .skillCategory(request.getSkillCategory())
                .skillLevelDefinition(request.getSkillLevelDefinition())
                .description(request.getDescription())
                .certificationRequired(request.getCertificationRequired())
                .certificationName(request.getCertificationName())
                .validityPeriodMonths(request.getValidityPeriodMonths())
                .remarks(request.getRemarks())
                .build();
    }

    private SkillMatrixResponse toResponse(SkillMatrixEntity entity) {
        return SkillMatrixResponse.builder()
                .skillId(entity.getSkillId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .skillCode(entity.getSkillCode())
                .skillName(entity.getSkillName())
                .skillCategory(entity.getSkillCategory())
                .skillLevelDefinition(entity.getSkillLevelDefinition())
                .description(entity.getDescription())
                .certificationRequired(entity.getCertificationRequired())
                .certificationName(entity.getCertificationName())
                .validityPeriodMonths(entity.getValidityPeriodMonths())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
