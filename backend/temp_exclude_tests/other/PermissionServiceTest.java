package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.PermissionEntity;
import kr.co.softice.mes.domain.repository.PermissionRepository;
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
 * Permission Service Test
 * 권한 관리 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("권한 서비스 테스트")
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private PermissionEntity testPermission;
    private Long permissionId;

    @BeforeEach
    void setUp() {
        permissionId = 1L;

        testPermission = new PermissionEntity();
        testPermission.setPermissionId(permissionId);
        testPermission.setPermissionCode("PERM001");
        testPermission.setPermissionName("Test Permission");
        testPermission.setModule("PRODUCTION");
        testPermission.setStatus("active");
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("권한 ID로 조회 - 성공")
    void testFindById_Success() {
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(testPermission));

        Optional<PermissionEntity> result = permissionService.findById(permissionId);

        assertThat(result).isPresent();
        assertThat(result.get().getPermissionCode()).isEqualTo("PERM001");
        verify(permissionRepository).findById(permissionId);
    }

    @Test
    @DisplayName("권한 코드로 조회 - 성공")
    void testFindByPermissionCode_Success() {
        when(permissionRepository.findByPermissionCode("PERM001"))
                .thenReturn(Optional.of(testPermission));

        Optional<PermissionEntity> result = permissionService.findByPermissionCode("PERM001");

        assertThat(result).isPresent();
        assertThat(result.get().getPermissionCode()).isEqualTo("PERM001");
        verify(permissionRepository).findByPermissionCode("PERM001");
    }

    @Test
    @DisplayName("모든 권한 조회 - 성공")
    void testFindAll_Success() {
        List<PermissionEntity> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findAll())
                .thenReturn(permissions);

        List<PermissionEntity> result = permissionService.findAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(permissionRepository).findAll();
    }

    @Test
    @DisplayName("모듈별 권한 조회 - 성공")
    void testFindByModule_Success() {
        List<PermissionEntity> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findByModule("PRODUCTION"))
                .thenReturn(permissions);

        List<PermissionEntity> result = permissionService.findByModule("PRODUCTION");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModule()).isEqualTo("PRODUCTION");
        verify(permissionRepository).findByModule("PRODUCTION");
    }

    @Test
    @DisplayName("활성 권한 조회 - 성공")
    void testFindActivePermissions_Success() {
        List<PermissionEntity> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findByStatus("active"))
                .thenReturn(permissions);

        List<PermissionEntity> result = permissionService.findActivePermissions();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("active");
        verify(permissionRepository).findByStatus("active");
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("권한 생성 - 성공")
    void testCreatePermission_Success() {
        PermissionEntity newPermission = new PermissionEntity();
        newPermission.setPermissionCode("PERM999");
        newPermission.setPermissionName("New Permission");

        when(permissionRepository.existsByPermissionCode("PERM999"))
                .thenReturn(false);
        when(permissionRepository.save(any(PermissionEntity.class)))
                .thenReturn(newPermission);

        PermissionEntity result = permissionService.createPermission(newPermission);

        assertThat(result).isNotNull();
        verify(permissionRepository).save(newPermission);
    }

    @Test
    @DisplayName("권한 생성 - 실패 (중복 코드)")
    void testCreatePermission_Fail_DuplicateCode() {
        PermissionEntity newPermission = new PermissionEntity();
        newPermission.setPermissionCode("PERM001");

        when(permissionRepository.existsByPermissionCode("PERM001"))
                .thenReturn(true);

        assertThatThrownBy(() -> permissionService.createPermission(newPermission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission code already exists");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("권한 수정 - 성공")
    void testUpdatePermission_Success() {
        testPermission.setPermissionName("Updated Permission");

        when(permissionRepository.existsById(permissionId))
                .thenReturn(true);
        when(permissionRepository.save(any(PermissionEntity.class)))
                .thenReturn(testPermission);

        PermissionEntity result = permissionService.updatePermission(testPermission);

        assertThat(result).isNotNull();
        verify(permissionRepository).save(testPermission);
    }

    @Test
    @DisplayName("권한 수정 - 실패 (존재하지 않음)")
    void testUpdatePermission_Fail_NotFound() {
        when(permissionRepository.existsById(permissionId))
                .thenReturn(false);

        assertThatThrownBy(() -> permissionService.updatePermission(testPermission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("권한 삭제 - 성공")
    void testDeletePermission_Success() {
        permissionService.deletePermission(permissionId);

        verify(permissionRepository).deleteById(permissionId);
    }

    // === 활성화/비활성화 테스트 ===

    @Test
    @DisplayName("권한 활성화 - 성공")
    void testActivatePermission_Success() {
        testPermission.setStatus("inactive");

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(testPermission));
        when(permissionRepository.save(any(PermissionEntity.class)))
                .thenAnswer(invocation -> {
                    PermissionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("active");
                    return saved;
                });

        PermissionEntity result = permissionService.activatePermission(permissionId);

        assertThat(result).isNotNull();
        verify(permissionRepository).save(testPermission);
    }

    @Test
    @DisplayName("권한 활성화 - 실패 (존재하지 않음)")
    void testActivatePermission_Fail_NotFound() {
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.activatePermission(permissionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission not found");
    }

    @Test
    @DisplayName("권한 비활성화 - 성공")
    void testDeactivatePermission_Success() {
        testPermission.setStatus("active");

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(testPermission));
        when(permissionRepository.save(any(PermissionEntity.class)))
                .thenAnswer(invocation -> {
                    PermissionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("inactive");
                    return saved;
                });

        PermissionEntity result = permissionService.deactivatePermission(permissionId);

        assertThat(result).isNotNull();
        verify(permissionRepository).save(testPermission);
    }

    @Test
    @DisplayName("권한 비활성화 - 실패 (존재하지 않음)")
    void testDeactivatePermission_Fail_NotFound() {
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.deactivatePermission(permissionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission not found");
    }
}
