package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.PermissionEntity;
import kr.co.softice.mes.domain.entity.RoleEntity;
import kr.co.softice.mes.domain.entity.RolePermissionEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Role Service
 * 역할 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * Find role by ID
     */
    public Optional<RoleEntity> findById(Long roleId) {
        log.debug("Finding role by ID: {}", roleId);
        return roleRepository.findById(roleId);
    }

    /**
     * Find role by tenant and role code
     */
    public Optional<RoleEntity> findByTenantAndRoleCode(String tenantId, String roleCode) {
        log.debug("Finding role by tenant: {} and code: {}", tenantId, roleCode);
        return roleRepository.findByTenant_TenantIdAndRoleCode(tenantId, roleCode);
    }

    /**
     * Find roles by tenant
     */
    public List<RoleEntity> findByTenant(String tenantId) {
        log.debug("Finding roles by tenant: {}", tenantId);
        return roleRepository.findByTenantIdWithTenant(tenantId);
    }

    /**
     * Find active roles by tenant
     */
    public List<RoleEntity> findActiveRolesByTenant(String tenantId) {
        log.debug("Finding active roles by tenant: {}", tenantId);
        return roleRepository.findByTenantIdAndIsActiveWithTenant(tenantId, true);
    }

    /**
     * Find permissions by role
     */
    public List<PermissionEntity> findPermissionsByRole(Long roleId) {
        log.debug("Finding permissions for role: {}", roleId);

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        return rolePermissionRepository.findPermissionsByRole(role);
    }

    /**
     * Create new role
     */
    @Transactional
    public RoleEntity createRole(RoleEntity role) {
        log.info("Creating new role: {} for tenant: {}", role.getRoleCode(), role.getTenant().getTenantId());

        // Check if role code already exists for tenant
        if (roleRepository.existsByTenantAndRoleCode(role.getTenant(), role.getRoleCode())) {
            throw new IllegalArgumentException("Role code already exists: " + role.getRoleCode());
        }

        return roleRepository.save(role);
    }

    /**
     * Update role
     */
    @Transactional
    public RoleEntity updateRole(RoleEntity role) {
        log.info("Updating role: {}", role.getRoleId());

        if (!roleRepository.existsById(role.getRoleId())) {
            throw new IllegalArgumentException("Role not found: " + role.getRoleId());
        }

        return roleRepository.save(role);
    }

    /**
     * Delete role
     */
    @Transactional
    public void deleteRole(Long roleId) {
        log.info("Deleting role: {}", roleId);

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        // Delete all role-permission mappings first
        rolePermissionRepository.deleteByRole(role);

        // Delete role
        roleRepository.deleteById(roleId);
    }

    /**
     * Assign permission to role
     */
    @Transactional
    public RolePermissionEntity assignPermission(Long roleId, Long permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        PermissionEntity permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        // Check if mapping already exists
        if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            throw new IllegalArgumentException("Permission already assigned to role");
        }

        RolePermissionEntity rolePermission = RolePermissionEntity.builder()
                .role(role)
                .permission(permission)
                .build();

        return rolePermissionRepository.save(rolePermission);
    }

    /**
     * Remove permission from role
     */
    @Transactional
    public void removePermission(Long roleId, Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        PermissionEntity permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        List<RolePermissionEntity> rolePermissions = rolePermissionRepository.findByRole(role);
        rolePermissions.stream()
                .filter(rp -> rp.getPermission().equals(permission))
                .findFirst()
                .ifPresent(rolePermissionRepository::delete);
    }
}
