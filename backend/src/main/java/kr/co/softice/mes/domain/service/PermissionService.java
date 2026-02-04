package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.PermissionEntity;
import kr.co.softice.mes.domain.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Permission Service
 * 권한 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    /**
     * Find permission by ID
     */
    public Optional<PermissionEntity> findById(Long permissionId) {
        log.debug("Finding permission by ID: {}", permissionId);
        return permissionRepository.findById(permissionId);
    }

    /**
     * Find permission by code
     */
    public Optional<PermissionEntity> findByPermissionCode(String permissionCode) {
        log.debug("Finding permission by code: {}", permissionCode);
        return permissionRepository.findByPermissionCode(permissionCode);
    }

    /**
     * Find all permissions
     */
    public List<PermissionEntity> findAll() {
        log.debug("Finding all permissions");
        return permissionRepository.findAll();
    }

    /**
     * Find permissions by module
     */
    public List<PermissionEntity> findByModule(String module) {
        log.debug("Finding permissions by module: {}", module);
        return permissionRepository.findByModule(module);
    }

    /**
     * Find active permissions
     */
    public List<PermissionEntity> findActivePermissions() {
        log.debug("Finding active permissions");
        return permissionRepository.findByStatus("active");
    }

    /**
     * Create new permission
     */
    @Transactional
    public PermissionEntity createPermission(PermissionEntity permission) {
        log.info("Creating new permission: {}", permission.getPermissionCode());

        // Check if permission code already exists
        if (permissionRepository.existsByPermissionCode(permission.getPermissionCode())) {
            throw new IllegalArgumentException("Permission code already exists: " + permission.getPermissionCode());
        }

        return permissionRepository.save(permission);
    }

    /**
     * Update permission
     */
    @Transactional
    public PermissionEntity updatePermission(PermissionEntity permission) {
        log.info("Updating permission: {}", permission.getPermissionId());

        if (!permissionRepository.existsById(permission.getPermissionId())) {
            throw new IllegalArgumentException("Permission not found: " + permission.getPermissionId());
        }

        return permissionRepository.save(permission);
    }

    /**
     * Delete permission
     */
    @Transactional
    public void deletePermission(Long permissionId) {
        log.info("Deleting permission: {}", permissionId);
        permissionRepository.deleteById(permissionId);
    }

    /**
     * Activate permission
     */
    @Transactional
    public PermissionEntity activatePermission(Long permissionId) {
        log.info("Activating permission: {}", permissionId);

        PermissionEntity permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        permission.setStatus("active");
        return permissionRepository.save(permission);
    }

    /**
     * Deactivate permission
     */
    @Transactional
    public PermissionEntity deactivatePermission(Long permissionId) {
        log.info("Deactivating permission: {}", permissionId);

        PermissionEntity permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        permission.setStatus("inactive");
        return permissionRepository.save(permission);
    }
}
