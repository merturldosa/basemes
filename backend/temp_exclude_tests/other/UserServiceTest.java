package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.DuplicateEntityException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * User Service Test
 * 사용자 관리 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 서비스 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private TenantEntity testTenant;
    private UserEntity testUser;
    private Long userId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";
        userId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testUser = new UserEntity();
        testUser.setUserId(userId);
        testUser.setTenant(testTenant);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encodedPassword");
        testUser.setStatus("active");
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("사용자 ID로 조회 - 성공")
    void testFindById_Success() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));

        Optional<UserEntity> result = userService.findById(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("테넌트와 사용자명으로 조회 - 성공")
    void testFindByTenantAndUsername_Success() {
        when(userRepository.findByTenant_TenantIdAndUsername(tenantId, "testuser"))
                .thenReturn(Optional.of(testUser));

        Optional<UserEntity> result = userService.findByTenantAndUsername(tenantId, "testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository).findByTenant_TenantIdAndUsername(tenantId, "testuser");
    }

    @Test
    @DisplayName("이메일로 조회 - 성공")
    void testFindByEmail_Success() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        Optional<UserEntity> result = userService.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("테넌트별 사용자 조회 - 성공")
    void testFindByTenant_Success() {
        List<UserEntity> users = Arrays.asList(testUser);
        when(userRepository.findByTenantIdWithTenant(tenantId))
                .thenReturn(users);

        List<UserEntity> result = userService.findByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(userRepository).findByTenantIdWithTenant(tenantId);
    }

    @Test
    @DisplayName("활성 사용자 조회 - 성공")
    void testFindActiveUsersByTenant_Success() {
        List<UserEntity> users = Arrays.asList(testUser);
        when(userRepository.findByTenantIdAndStatusWithTenant(tenantId, "active"))
                .thenReturn(users);

        List<UserEntity> result = userService.findActiveUsersByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(userRepository).findByTenantIdAndStatusWithTenant(tenantId, "active");
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("사용자 생성 - 성공")
    void testCreateUser_Success() {
        UserEntity newUser = new UserEntity();
        newUser.setTenant(testTenant);
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");

        when(userRepository.existsByTenantAndUsername(testTenant, "newuser"))
                .thenReturn(false);
        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword123");
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(newUser);

        UserEntity result = userService.createUser(newUser, "password123");

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("사용자 생성 - 실패 (중복 사용자명)")
    void testCreateUser_Fail_DuplicateUsername() {
        UserEntity newUser = new UserEntity();
        newUser.setTenant(testTenant);
        newUser.setUsername("testuser");
        newUser.setEmail("new@example.com");

        when(userRepository.existsByTenantAndUsername(testTenant, "testuser"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(newUser, "password123"))
                .isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    @DisplayName("사용자 생성 - 실패 (중복 이메일)")
    void testCreateUser_Fail_DuplicateEmail() {
        UserEntity newUser = new UserEntity();
        newUser.setTenant(testTenant);
        newUser.setUsername("newuser");
        newUser.setEmail("test@example.com");

        when(userRepository.existsByTenantAndUsername(testTenant, "newuser"))
                .thenReturn(false);
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(newUser, "password123"))
                .isInstanceOf(DuplicateEntityException.class);
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("사용자 수정 - 성공")
    void testUpdateUser_Success() {
        testUser.setEmail("updated@example.com");

        when(userRepository.existsById(userId))
                .thenReturn(true);
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(testUser);

        UserEntity result = userService.updateUser(testUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("사용자 수정 - 실패 (존재하지 않음)")
    void testUpdateUser_Fail_NotFound() {
        when(userRepository.existsById(userId))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.updateUser(testUser))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 패스워드 관리 테스트 ===

    @Test
    @DisplayName("패스워드 변경 - 성공")
    void testChangePassword_Success() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "encodedPassword"))
                .thenReturn(true);
        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(testUser);

        userService.changePassword(userId, "currentPassword", "newPassword");

        verify(passwordEncoder).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("패스워드 변경 - 실패 (사용자 없음)")
    void testChangePassword_Fail_UserNotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(userId, "currentPassword", "newPassword"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("패스워드 변경 - 실패 (현재 패스워드 불일치)")
    void testChangePassword_Fail_PasswordMismatch() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(userId, "wrongPassword", "newPassword"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("패스워드 재설정 - 성공")
    void testResetPassword_Success() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(testUser);

        userService.resetPassword(userId, "newPassword");

        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("패스워드 재설정 - 실패 (사용자 없음)")
    void testResetPassword_Fail_UserNotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword(userId, "newPassword"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 로그인 시간 업데이트 테스트 ===

    @Test
    @DisplayName("마지막 로그인 시간 업데이트 - 성공")
    void testUpdateLastLogin_Success() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(testUser);

        userService.updateLastLogin(userId);

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("마지막 로그인 시간 업데이트 - 실패 (사용자 없음)")
    void testUpdateLastLogin_Fail_UserNotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateLastLogin(userId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 활성화/비활성화 테스트 ===

    @Test
    @DisplayName("사용자 활성화 - 성공")
    void testActivateUser_Success() {
        testUser.setStatus("inactive");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("active");
                    return saved;
                });

        UserEntity result = userService.activateUser(userId);

        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("사용자 활성화 - 실패 (존재하지 않음)")
    void testActivateUser_Fail_NotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.activateUser(userId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 비활성화 - 성공")
    void testDeactivateUser_Success() {
        testUser.setStatus("active");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("inactive");
                    return saved;
                });

        UserEntity result = userService.deactivateUser(userId);

        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("사용자 비활성화 - 실패 (존재하지 않음)")
    void testDeactivateUser_Fail_NotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(userId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("사용자 삭제 - 성공")
    void testDeleteUser_Success() {
        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }
}
