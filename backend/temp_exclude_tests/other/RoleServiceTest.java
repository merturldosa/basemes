package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.PermissionEntity;
import kr.co.softice.mes.domain.entity.RoleEntity;
import kr.co.softice.mes.domain.entity.RolePermissionEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.PermissionRepository;
import kr.co.softice.mes.domain.repository.RolePermissionRepository;
import kr.co.softice.mes.domain.repository.RoleRepository;
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
 * Role Service Test
 * 역할 관리 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("역할 서비스 테스트")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RoleService roleService;

    private TenantEntity testTenant;
    private RoleEntity testRole;
    private PermissionEntity testPermission;
    private RolePermissionEntity testRolePermission;
    private Long roleId;
    private Long permissionId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";
        roleId = 1L;
        permissionId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testRole = new RoleEntity();
        testRole.setRoleId(roleId);
        testRole.setTenant(testTenant);
        testRole.setRoleCode("ROLE001");
        testRole.setRoleName("Test Role");
        testRole.setIsActive(true);

        testPermission = new PermissionEntity();
        testPermission.setPermissionId(permissionId);
        testPermission.setPermissionCode("PERM001");
        testPermission.setPermissionName("Test Permission");

        testRolePermission = RolePermissionEntity.builder()
                .role(testRole)
                .permission(testPermission)
                .build();
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("역할 ID로 조회 - 성공")
    void testFindById_Success() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(testRole));

        Optional<RoleEntity> result = roleService.findById(roleId);

        assertThat(result).isPresent();
        assertThat(result.get().getRoleCode()).isEqualTo("ROLE001");
        verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("테넌트와 역할 코드로 조회 - 성공")
    void testFindByTenantAndRoleCode_Success() {
        when(roleRepository.findByTenant_TenantIdAndRoleCode(tenantId, "ROLE001"))
                .thenReturn(Optional.of(testRole));

        Optional<RoleEntity> result = roleService.findByTenantAndRoleCode(tenantId, "ROLE001");

        assertThat(result).isPresent();
        assertThat(result.get().getRoleCode()).isEqualTo("ROLE001");
        verify(roleRepository).findByTenant_TenantIdAndRoleCode(tenantId, "ROLE001");
    }

    @Test
    @DisplayName("테넌트별 역할 조회 - 성공")
    void testFindByTenant_Success() {
        List<RoleEntity> roles = Arrays.asList(testRole);
        when(roleRepository.findByTenantIdWithTenant(tenantId))
                .thenReturn(roles);

        List<RoleEntity> result = roleService.findByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(roleRepository).findByTenantIdWithTenant(tenantId);
    }

    @Test
    @DisplayName("활성 역할 조회 - 성공")
    void testFindActiveRolesByTenant_Success() {
        List<RoleEntity> roles = Arrays.asList(testRole);
        when(roleRepository.findByTenantIdAndIsActiveWithTenant(tenantId, true))
                .thenReturn(roles);

        List<RoleEntity> result = roleService.findActiveRolesByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(roleRepository).findByTenantIdAndIsActiveWithTenant(tenantId, true);
    }

    @Test
    @DisplayName("역할별 권한 조회 - 성공")
    void testFindPermissionsByRole_Success() {
        List<PermissionEntity> permissions = Arrays.asList(testPermission);
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(testRole));
        when(rolePermissionRepository.findPermissionsByRole(testRole))
                .thenReturn(permissions);

        List<PermissionEntity> result = roleService.findPermissionsByRole(roleId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(rolePermissionRepository).findPermissionsByRole(testRole);
    }

    @Test
    @DisplayName("역할별 권한 조회 - 실패 (역할 없음)")
    void testFindPermissionsByRole_Fail_RoleNotFound() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.findPermissionsByRole(roleId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("역할 생성 - 성공")
    void testCreateRole_Success() {
        RoleEntity newRole = new RoleEntity();
        newRole.setTenant(testTenant);
        newRole.setRoleCode("ROLE999");
        newRole.setRoleName("New Role");

        when(roleRepository.existsByTenantAndRoleCode(testTenant, "ROLE999"))
                .thenReturn(false);
        when(roleRepository.save(any(RoleEntity.class)))
                .thenReturn(newRole);

        RoleEntity result = roleService.createRole(newRole);

        assertThat(result).isNotNull();
        verify(roleRepository).save(newRole);
    }

    @Test
    @DisplayName("역할 생성 - 실패 (중복 코드)")
    void testCreateRole_Fail_DuplicateCode() {
        RoleEntity newRole = new RoleEntity();
        newRole.setTenant(testTenant);
        newRole.setRoleCode("ROLE001");

        when(roleRepository.existsByTenantAndRoleCode(testTenant, "ROLE001"))
                .thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(newRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role code already exists");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("역할 수정 - 성공")
    void testUpdateRole_Success() {
        testRole.setRoleName("Updated Role");

        when(roleRepository.existsById(roleId))
                .thenReturn(true);
        when(roleRepository.save(any(RoleEntity.class)))
                .thenReturn(testRole);

        RoleEntity result = roleService.updateRole(testRole);

        assertThat(result).isNotNull();
        verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("역할 수정 - 실패 (존재하지 않음)")
    void testUpdateRole_Fail_NotFound() {
        when(roleRepository.existsById(roleId))
                .thenReturn(false);

        assertThatThrownBy(() -> roleService.updateRole(testRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("역할 삭제 - 성공")
    void testDeleteRole_Success() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(testRole));

        roleService.deleteRole(roleId);

        verify(rolePermissionRepository).deleteByRole(testRole);
        verify(roleRepository).deleteById(roleId);
    }

    @Test
    @DisplayName("역할 삭제 - 실패 (존재하지 않음)")
    void testDeleteRole_Fail_NotFound() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.deleteRole(roleId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
    }

    // === 권한 할당 테스트 ===

    @Test
    @DisplayName("권한 할당 - 성공")
    void testAssignPermission_Success() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(testPermission));
        when(rolePermissionRepository.existsByRoleAndPermission(testRole, testPermission))
                .thenReturn(false);
        when(rolePermissionRepository.save(any(RolePermissionEntity.class)))
                .thenReturn(testRolePermission);

        RolePermissionEntity result = roleService.assignPermission(roleId, permissionId);

        assertThat(result).isNotNull();
        verify(rolePermissionRepository).save(any(RolePermissionEntity.class));
    }

    @Test
    @DisplayName("권한 할당 - 실패 (역할 없음)")
    void testAssignPermission_Fail_RoleNotFound() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignPermission(roleId, permissionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("권한 할당 - 실패 (권한 없음)")
    void testAssignPermission_Fail_PermissionNotFound() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignPermission(roleId, permissionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission not found");
    }

    @Test
    @DisplayName("권한 할당 - 실패 (이미 할당됨)")
    void testAssignPermission_Fail_AlreadyAssigned() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(testPermission));
        when(rolePermissionRepository.existsByRoleAndPermission(testRole, testPermission))
                .thenReturn(true);

        assertThatThrownBy(() -> roleService.assignPermission(roleId, permissionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission already assigned");
    }

    // === 권한 제거 테스트 ===

    @Test
    @DisplayName("권한 제거 - 성공")
    void testRemovePermission_Success() {
        List<RolePermissionEntity> rolePermissions = Arrays.asList(testRolePermission);
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(testPermission));
        when(rolePermissionRepository.findByRole(testRole))
                .thenReturn(rolePermissions);

        roleService.removePermission(roleId, permissionId);

        verify(rolePermissionRepository).delete(testRolePermission);
    }

    @Test
    @DisplayName("권한 제거 - 실패 (역할 없음)")
    void testRemovePermission_Fail_RoleNotFound() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.removePermission(roleId, permissionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("권한 제거 - 실패 (권한 없음)")
    void testRemovePermission_Fail_PermissionNotFound() {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.removePermission(roleId, permissionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission not found");
    }
}
