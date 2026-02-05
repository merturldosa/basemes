package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.domain.entity.SkillMatrixEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.SkillMatrixRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Skill Matrix Service Test
 * 스킬 매트릭스 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("스킬 매트릭스 서비스 테스트")
class SkillMatrixServiceTest {

    @Mock
    private SkillMatrixRepository skillMatrixRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private SkillMatrixService skillMatrixService;

    private TenantEntity testTenant;
    private SkillMatrixEntity testSkill;
    private Long skillId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";
        skillId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testSkill = new SkillMatrixEntity();
        testSkill.setSkillId(skillId);
        testSkill.setTenant(testTenant);
        testSkill.setSkillCode("SKILL001");
        testSkill.setSkillName("Welding");
        testSkill.setSkillCategory("PRODUCTION");
        testSkill.setCertificationRequired(true);
        testSkill.setIsActive(true);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("모든 스킬 조회 - 성공")
    void testGetAllSkills_Success() {
        List<SkillMatrixEntity> skills = Arrays.asList(testSkill);
        when(skillMatrixRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(skills);

        List<SkillMatrixEntity> result = skillMatrixService.getAllSkills(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(skillMatrixRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("스킬 ID로 조회 - 성공")
    void testGetSkillById_Success() {
        when(skillMatrixRepository.findByIdWithAllRelations(skillId))
                .thenReturn(Optional.of(testSkill));

        SkillMatrixEntity result = skillMatrixService.getSkillById(skillId);

        assertThat(result).isNotNull();
        assertThat(result.getSkillCode()).isEqualTo("SKILL001");
        verify(skillMatrixRepository).findByIdWithAllRelations(skillId);
    }

    @Test
    @DisplayName("스킬 ID로 조회 - 실패 (존재하지 않음)")
    void testGetSkillById_Fail_NotFound() {
        when(skillMatrixRepository.findByIdWithAllRelations(skillId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillMatrixService.getSkillById(skillId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("활성 스킬 조회 - 성공")
    void testGetActiveSkills_Success() {
        List<SkillMatrixEntity> skills = Arrays.asList(testSkill);
        when(skillMatrixRepository.findActiveSkillsByTenantId(tenantId))
                .thenReturn(skills);

        List<SkillMatrixEntity> result = skillMatrixService.getActiveSkills(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(skillMatrixRepository).findActiveSkillsByTenantId(tenantId);
    }

    @Test
    @DisplayName("카테고리별 스킬 조회 - 성공")
    void testGetSkillsByCategory_Success() {
        List<SkillMatrixEntity> skills = Arrays.asList(testSkill);
        when(skillMatrixRepository.findByTenantIdAndSkillCategory(tenantId, "PRODUCTION"))
                .thenReturn(skills);

        List<SkillMatrixEntity> result = skillMatrixService.getSkillsByCategory(tenantId, "PRODUCTION");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(skillMatrixRepository).findByTenantIdAndSkillCategory(tenantId, "PRODUCTION");
    }

    @Test
    @DisplayName("자격증 필요 스킬 조회 - 성공")
    void testGetSkillsRequiringCertification_Success() {
        List<SkillMatrixEntity> skills = Arrays.asList(testSkill);
        when(skillMatrixRepository.findSkillsRequiringCertification(tenantId))
                .thenReturn(skills);

        List<SkillMatrixEntity> result = skillMatrixService.getSkillsRequiringCertification(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCertificationRequired()).isTrue();
        verify(skillMatrixRepository).findSkillsRequiringCertification(tenantId);
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("스킬 생성 - 성공")
    void testCreateSkill_Success() {
        SkillMatrixEntity newSkill = new SkillMatrixEntity();
        newSkill.setSkillCode("SKILL999");
        newSkill.setSkillName("New Skill");

        when(skillMatrixRepository.existsByTenant_TenantIdAndSkillCode(tenantId, "SKILL999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(skillMatrixRepository.save(any(SkillMatrixEntity.class)))
                .thenAnswer(invocation -> {
                    SkillMatrixEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue(); // Default value
                    assertThat(saved.getCertificationRequired()).isFalse(); // Default value
                    return saved;
                });

        SkillMatrixEntity result = skillMatrixService.createSkill(tenantId, newSkill);

        assertThat(result).isNotNull();
        verify(skillMatrixRepository).save(any(SkillMatrixEntity.class));
    }

    @Test
    @DisplayName("스킬 생성 - 실패 (중복 코드)")
    void testCreateSkill_Fail_DuplicateCode() {
        SkillMatrixEntity newSkill = new SkillMatrixEntity();
        newSkill.setSkillCode("SKILL001");

        when(skillMatrixRepository.existsByTenant_TenantIdAndSkillCode(tenantId, "SKILL001"))
                .thenReturn(true);

        assertThatThrownBy(() -> skillMatrixService.createSkill(tenantId, newSkill))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("스킬 생성 - 실패 (테넌트 없음)")
    void testCreateSkill_Fail_TenantNotFound() {
        SkillMatrixEntity newSkill = new SkillMatrixEntity();
        newSkill.setSkillCode("SKILL999");

        when(skillMatrixRepository.existsByTenant_TenantIdAndSkillCode(tenantId, "SKILL999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillMatrixService.createSkill(tenantId, newSkill))
                .isInstanceOf(BusinessException.class);
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("스킬 수정 - 성공")
    void testUpdateSkill_Success() {
        SkillMatrixEntity updateData = new SkillMatrixEntity();
        updateData.setSkillName("Updated Skill");
        updateData.setSkillCategory("QUALITY");

        when(skillMatrixRepository.findByIdWithAllRelations(skillId))
                .thenReturn(Optional.of(testSkill));
        when(skillMatrixRepository.save(any(SkillMatrixEntity.class)))
                .thenReturn(testSkill);

        SkillMatrixEntity result = skillMatrixService.updateSkill(skillId, updateData);

        assertThat(result).isNotNull();
        verify(skillMatrixRepository).save(testSkill);
    }

    @Test
    @DisplayName("스킬 수정 - 실패 (존재하지 않음)")
    void testUpdateSkill_Fail_NotFound() {
        SkillMatrixEntity updateData = new SkillMatrixEntity();
        updateData.setSkillName("Updated Skill");

        when(skillMatrixRepository.findByIdWithAllRelations(skillId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillMatrixService.updateSkill(skillId, updateData))
                .isInstanceOf(BusinessException.class);
    }

    // === 활성화/비활성화 테스트 ===

    @Test
    @DisplayName("스킬 활성화 - 성공")
    void testActivate_Success() {
        testSkill.setIsActive(false);

        when(skillMatrixRepository.findByIdWithAllRelations(skillId))
                .thenReturn(Optional.of(testSkill));
        when(skillMatrixRepository.save(any(SkillMatrixEntity.class)))
                .thenAnswer(invocation -> {
                    SkillMatrixEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });

        SkillMatrixEntity result = skillMatrixService.activate(skillId);

        assertThat(result).isNotNull();
        verify(skillMatrixRepository).save(testSkill);
    }

    @Test
    @DisplayName("스킬 활성화 - 실패 (존재하지 않음)")
    void testActivate_Fail_NotFound() {
        when(skillMatrixRepository.findByIdWithAllRelations(skillId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillMatrixService.activate(skillId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("스킬 비활성화 - 성공")
    void testDeactivate_Success() {
        testSkill.setIsActive(true);

        when(skillMatrixRepository.findByIdWithAllRelations(skillId))
                .thenReturn(Optional.of(testSkill));
        when(skillMatrixRepository.save(any(SkillMatrixEntity.class)))
                .thenAnswer(invocation -> {
                    SkillMatrixEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });

        SkillMatrixEntity result = skillMatrixService.deactivate(skillId);

        assertThat(result).isNotNull();
        verify(skillMatrixRepository).save(testSkill);
    }

    @Test
    @DisplayName("스킬 비활성화 - 실패 (존재하지 않음)")
    void testDeactivate_Fail_NotFound() {
        when(skillMatrixRepository.findByIdWithAllRelations(skillId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillMatrixService.deactivate(skillId))
                .isInstanceOf(BusinessException.class);
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("스킬 삭제 - 성공")
    void testDeleteSkill_Success() {
        when(skillMatrixRepository.findById(skillId))
                .thenReturn(Optional.of(testSkill));

        skillMatrixService.deleteSkill(skillId);

        verify(skillMatrixRepository).delete(testSkill);
    }

    @Test
    @DisplayName("스킬 삭제 - 실패 (존재하지 않음)")
    void testDeleteSkill_Fail_NotFound() {
        when(skillMatrixRepository.findById(skillId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillMatrixService.deleteSkill(skillId))
                .isInstanceOf(BusinessException.class);
    }
}
