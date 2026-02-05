package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.CodeEntity;
import kr.co.softice.mes.domain.entity.CodeGroupEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.CodeGroupRepository;
import kr.co.softice.mes.domain.repository.CodeRepository;
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
 * Code Service Test
 * 공통 코드 관리 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("공통 코드 서비스 테스트")
class CodeServiceTest {

    @Mock
    private CodeGroupRepository codeGroupRepository;

    @Mock
    private CodeRepository codeRepository;

    @InjectMocks
    private CodeService codeService;

    private TenantEntity testTenant;
    private CodeGroupEntity testCodeGroup;
    private CodeEntity testCode;
    private Long groupId;
    private Long codeId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        groupId = 1L;
        codeId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testCodeGroup = new CodeGroupEntity();
        testCodeGroup.setGroupId(groupId);
        testCodeGroup.setTenant(testTenant);
        testCodeGroup.setGroupCode("GRP001");
        testCodeGroup.setGroupName("Test Group");

        testCode = new CodeEntity();
        testCode.setCodeId(codeId);
        testCode.setCodeGroup(testCodeGroup);
        testCode.setCode("CODE001");
        testCode.setCodeName("Test Code");
        testCode.setDisplayOrder(1);
        testCode.setStatus("active");
    }

    // === Code Group 테스트 ===

    @Test
    @DisplayName("코드 그룹 ID로 조회 - 성공")
    void testFindCodeGroupById_Success() {
        when(codeGroupRepository.findById(groupId))
                .thenReturn(Optional.of(testCodeGroup));

        Optional<CodeGroupEntity> result = codeService.findCodeGroupById(groupId);

        assertThat(result).isPresent();
        assertThat(result.get().getGroupCode()).isEqualTo("GRP001");
        verify(codeGroupRepository).findById(groupId);
    }

    @Test
    @DisplayName("테넌트와 코드로 코드 그룹 조회 - 성공")
    void testFindCodeGroupByTenantAndCode_Success() {
        when(codeGroupRepository.findByTenant_TenantIdAndGroupCode(tenantId, "GRP001"))
                .thenReturn(Optional.of(testCodeGroup));

        Optional<CodeGroupEntity> result = codeService.findCodeGroupByTenantAndCode(tenantId, "GRP001");

        assertThat(result).isPresent();
        assertThat(result.get().getGroupCode()).isEqualTo("GRP001");
        verify(codeGroupRepository).findByTenant_TenantIdAndGroupCode(tenantId, "GRP001");
    }

    @Test
    @DisplayName("테넌트별 코드 그룹 조회 - 성공")
    void testFindCodeGroupsByTenant_Success() {
        List<CodeGroupEntity> groups = Arrays.asList(testCodeGroup);
        when(codeGroupRepository.findByTenant_TenantId(tenantId))
                .thenReturn(groups);

        List<CodeGroupEntity> result = codeService.findCodeGroupsByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroupCode()).isEqualTo("GRP001");
        verify(codeGroupRepository).findByTenant_TenantId(tenantId);
    }

    @Test
    @DisplayName("코드 그룹 생성 - 성공")
    void testCreateCodeGroup_Success() {
        CodeGroupEntity newGroup = new CodeGroupEntity();
        newGroup.setTenant(testTenant);
        newGroup.setGroupCode("GRP999");
        newGroup.setGroupName("New Group");

        when(codeGroupRepository.existsByTenantAndGroupCode(testTenant, "GRP999"))
                .thenReturn(false);
        when(codeGroupRepository.save(any(CodeGroupEntity.class)))
                .thenReturn(newGroup);

        CodeGroupEntity result = codeService.createCodeGroup(newGroup);

        assertThat(result).isNotNull();
        verify(codeGroupRepository).save(newGroup);
    }

    @Test
    @DisplayName("코드 그룹 생성 - 실패 (중복 코드)")
    void testCreateCodeGroup_Fail_DuplicateCode() {
        CodeGroupEntity newGroup = new CodeGroupEntity();
        newGroup.setTenant(testTenant);
        newGroup.setGroupCode("GRP001");

        when(codeGroupRepository.existsByTenantAndGroupCode(testTenant, "GRP001"))
                .thenReturn(true);

        assertThatThrownBy(() -> codeService.createCodeGroup(newGroup))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Group code already exists");
    }

    @Test
    @DisplayName("코드 그룹 수정 - 성공")
    void testUpdateCodeGroup_Success() {
        testCodeGroup.setGroupName("Updated Group");

        when(codeGroupRepository.existsById(groupId))
                .thenReturn(true);
        when(codeGroupRepository.save(any(CodeGroupEntity.class)))
                .thenReturn(testCodeGroup);

        CodeGroupEntity result = codeService.updateCodeGroup(testCodeGroup);

        assertThat(result).isNotNull();
        verify(codeGroupRepository).save(testCodeGroup);
    }

    @Test
    @DisplayName("코드 그룹 수정 - 실패 (존재하지 않음)")
    void testUpdateCodeGroup_Fail_NotFound() {
        when(codeGroupRepository.existsById(groupId))
                .thenReturn(false);

        assertThatThrownBy(() -> codeService.updateCodeGroup(testCodeGroup))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Code group not found");
    }

    @Test
    @DisplayName("코드 그룹 삭제 - 성공")
    void testDeleteCodeGroup_Success() {
        codeService.deleteCodeGroup(groupId);

        verify(codeGroupRepository).deleteById(groupId);
    }

    // === Code 테스트 ===

    @Test
    @DisplayName("코드 ID로 조회 - 성공")
    void testFindCodeById_Success() {
        when(codeRepository.findById(codeId))
                .thenReturn(Optional.of(testCode));

        Optional<CodeEntity> result = codeService.findCodeById(codeId);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("CODE001");
        verify(codeRepository).findById(codeId);
    }

    @Test
    @DisplayName("그룹과 코드로 조회 - 성공")
    void testFindCodeByGroupAndCode_Success() {
        when(codeGroupRepository.findById(groupId))
                .thenReturn(Optional.of(testCodeGroup));
        when(codeRepository.findByCodeGroupAndCode(testCodeGroup, "CODE001"))
                .thenReturn(Optional.of(testCode));

        Optional<CodeEntity> result = codeService.findCodeByGroupAndCode(groupId, "CODE001");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("CODE001");
        verify(codeRepository).findByCodeGroupAndCode(testCodeGroup, "CODE001");
    }

    @Test
    @DisplayName("그룹과 코드로 조회 - 실패 (그룹 없음)")
    void testFindCodeByGroupAndCode_Fail_GroupNotFound() {
        when(codeGroupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codeService.findCodeByGroupAndCode(groupId, "CODE001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Code group not found");
    }

    @Test
    @DisplayName("그룹별 코드 조회 - 성공")
    void testFindCodesByGroup_Success() {
        List<CodeEntity> codes = Arrays.asList(testCode);
        when(codeGroupRepository.findById(groupId))
                .thenReturn(Optional.of(testCodeGroup));
        when(codeRepository.findByCodeGroupOrderByDisplayOrderAsc(testCodeGroup))
                .thenReturn(codes);

        List<CodeEntity> result = codeService.findCodesByGroup(groupId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("CODE001");
        verify(codeRepository).findByCodeGroupOrderByDisplayOrderAsc(testCodeGroup);
    }

    @Test
    @DisplayName("그룹별 코드 조회 - 실패 (그룹 없음)")
    void testFindCodesByGroup_Fail_GroupNotFound() {
        when(codeGroupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codeService.findCodesByGroup(groupId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Code group not found");
    }

    @Test
    @DisplayName("그룹별 활성 코드 조회 - 성공")
    void testFindActiveCodesByGroup_Success() {
        List<CodeEntity> codes = Arrays.asList(testCode);
        when(codeGroupRepository.findById(groupId))
                .thenReturn(Optional.of(testCodeGroup));
        when(codeRepository.findByCodeGroupAndStatusOrderByDisplayOrderAsc(testCodeGroup, "active"))
                .thenReturn(codes);

        List<CodeEntity> result = codeService.findActiveCodesByGroup(groupId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("active");
        verify(codeRepository).findByCodeGroupAndStatusOrderByDisplayOrderAsc(testCodeGroup, "active");
    }

    @Test
    @DisplayName("코드 생성 - 성공")
    void testCreateCode_Success() {
        CodeEntity newCode = new CodeEntity();
        newCode.setCodeGroup(testCodeGroup);
        newCode.setCode("CODE999");
        newCode.setCodeName("New Code");

        when(codeRepository.existsByCodeGroupAndCode(testCodeGroup, "CODE999"))
                .thenReturn(false);
        when(codeRepository.save(any(CodeEntity.class)))
                .thenReturn(newCode);

        CodeEntity result = codeService.createCode(newCode);

        assertThat(result).isNotNull();
        verify(codeRepository).save(newCode);
    }

    @Test
    @DisplayName("코드 생성 - 실패 (중복 코드)")
    void testCreateCode_Fail_DuplicateCode() {
        CodeEntity newCode = new CodeEntity();
        newCode.setCodeGroup(testCodeGroup);
        newCode.setCode("CODE001");

        when(codeRepository.existsByCodeGroupAndCode(testCodeGroup, "CODE001"))
                .thenReturn(true);

        assertThatThrownBy(() -> codeService.createCode(newCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Code already exists");
    }

    @Test
    @DisplayName("코드 수정 - 성공")
    void testUpdateCode_Success() {
        testCode.setCodeName("Updated Code");

        when(codeRepository.existsById(codeId))
                .thenReturn(true);
        when(codeRepository.save(any(CodeEntity.class)))
                .thenReturn(testCode);

        CodeEntity result = codeService.updateCode(testCode);

        assertThat(result).isNotNull();
        verify(codeRepository).save(testCode);
    }

    @Test
    @DisplayName("코드 수정 - 실패 (존재하지 않음)")
    void testUpdateCode_Fail_NotFound() {
        when(codeRepository.existsById(codeId))
                .thenReturn(false);

        assertThatThrownBy(() -> codeService.updateCode(testCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Code not found");
    }

    @Test
    @DisplayName("코드 삭제 - 성공")
    void testDeleteCode_Success() {
        codeService.deleteCode(codeId);

        verify(codeRepository).deleteById(codeId);
    }
}
