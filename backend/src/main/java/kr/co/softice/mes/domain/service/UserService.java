package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.DuplicateEntityException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Service
 * 사용자 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Find user by ID
     */
    public Optional<UserEntity> findById(Long userId) {
        log.debug("Finding user by ID: {}", userId);
        return userRepository.findById(userId);
    }

    /**
     * Find user by tenant and username
     */
    public Optional<UserEntity> findByTenantAndUsername(String tenantId, String username) {
        log.debug("Finding user by tenant: {} and username: {}", tenantId, username);
        return userRepository.findByTenant_TenantIdAndUsername(tenantId, username);
    }

    /**
     * Find user by email
     */
    public Optional<UserEntity> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Find users by tenant
     */
    public List<UserEntity> findByTenant(String tenantId) {
        log.debug("Finding users by tenant: {}", tenantId);
        return userRepository.findByTenantIdWithTenant(tenantId);
    }

    /**
     * Find active users by tenant
     */
    public List<UserEntity> findActiveUsersByTenant(String tenantId) {
        log.debug("Finding active users by tenant: {}", tenantId);
        return userRepository.findByTenantIdAndStatusWithTenant(tenantId, "active");
    }

    /**
     * Create new user
     */
    @Transactional
    public UserEntity createUser(UserEntity user, String rawPassword) {
        log.info("Creating new user: {} for tenant: {}", user.getUsername(), user.getTenant().getTenantId());

        // Check if username already exists for tenant
        if (userRepository.existsByTenantAndUsername(user.getTenant(), user.getUsername())) {
            throw new DuplicateEntityException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEntityException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Encode password
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        return userRepository.save(user);
    }

    /**
     * Update user
     */
    @Transactional
    public UserEntity updateUser(UserEntity user) {
        log.info("Updating user: {}", user.getUserId());

        if (!userRepository.existsById(user.getUserId())) {
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        return userRepository.save(user);
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new EntityNotFoundException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Reset password
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        log.info("Resetting password for user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Update last login time
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        log.debug("Updating last login for user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Activate user
     */
    @Transactional
    public UserEntity activateUser(Long userId) {
        log.info("Activating user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setStatus("active");
        return userRepository.save(user);
    }

    /**
     * Deactivate user
     */
    @Transactional
    public UserEntity deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setStatus("inactive");
        return userRepository.save(user);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);
        userRepository.deleteById(userId);
    }
}
