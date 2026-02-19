package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.permission.PermissionResponse;
import kr.co.softice.mes.common.dto.role.AssignPermissionRequest;
import kr.co.softice.mes.common.dto.role.RoleCreateRequest;
import kr.co.softice.mes.common.dto.role.RoleResponse;
import kr.co.softice.mes.common.dto.role.RoleUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.PermissionEntity;
import kr.co.softice.mes.domain.entity.RoleEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.PermissionRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role Controller
 * 역할 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "역할 관리 API")
public class RoleController {

    private final RoleService roleService;
    private final TenantRepository tenantRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 역할 목록 조회
     * GET /api/roles
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "역할 목록 조회", description = "테넌트의 모든 역할 조회")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting roles for tenant: {}", tenantId);

        List<RoleResponse> roles = roleService.findByTenant(tenantId).stream()
                .map(this::toRoleResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("역할 목록 조회 성공", roles));
    }

    /**
     * 활성 역할 목록 조회
     * GET /api/roles/active
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 역할 목록 조회", description = "활성 상태의 역할만 조회")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getActiveRoles() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active roles for tenant: {}", tenantId);

        List<RoleResponse> roles = roleService.findActiveRolesByTenant(tenantId).stream()
                .map(this::toRoleResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("활성 역할 목록 조회 성공", roles));
    }

    /**
     * 역할 상세 조회
     * GET /api/roles/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "역할 상세 조회", description = "역할 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable Long id) {
        log.info("Getting role: {}", id);

        RoleEntity role = roleService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("역할 조회 성공", toRoleResponse(role)));
    }

    /**
     * 역할 생성
     * POST /api/roles
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "역할 생성", description = "신규 역할 등록")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating role: {} for tenant: {}", request.getRoleCode(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        RoleEntity role = RoleEntity.builder()
                .tenant(tenant)
                .roleCode(request.getRoleCode())
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .config(request.getConfig())
                .isActive(true)
                .build();

        RoleEntity createdRole = roleService.createRole(role);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("역할 생성 성공", toRoleResponse(createdRole)));
    }

    /**
     * 역할 수정
     * PUT /api/roles/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "역할 수정", description = "역할 정보 수정")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {

        log.info("Updating role: {}", id);

        RoleEntity role = roleService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        if (request.getRoleName() != null) {
            role.setRoleName(request.getRoleName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getConfig() != null) {
            role.setConfig(request.getConfig());
        }

        RoleEntity updatedRole = roleService.updateRole(role);

        return ResponseEntity.ok(ApiResponse.success("역할 수정 성공", toRoleResponse(updatedRole)));
    }

    /**
     * 역할 삭제
     * DELETE /api/roles/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "역할 삭제", description = "역할 완전 삭제 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        log.info("Deleting role: {}", id);

        roleService.deleteRole(id);

        return ResponseEntity.ok(ApiResponse.success("역할 삭제 성공", null));
    }

    /**
     * 역할에 권한 할당
     * POST /api/roles/{id}/permissions
     */
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "역할에 권한 할당", description = "역할에 새로운 권한 추가")
    public ResponseEntity<ApiResponse<Void>> assignPermission(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionRequest request) {

        log.info("Assigning permission {} to role {}", request.getPermissionId(), id);

        roleService.assignPermission(id, request.getPermissionId());

        return ResponseEntity.ok(ApiResponse.success("권한 할당 성공", null));
    }

    /**
     * 역할에서 권한 제거
     * DELETE /api/roles/{id}/permissions/{permissionId}
     */
    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "역할에서 권한 제거", description = "역할에서 특정 권한 제거")
    public ResponseEntity<ApiResponse<Void>> removePermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {

        log.info("Removing permission {} from role {}", permissionId, id);

        roleService.removePermission(id, permissionId);

        return ResponseEntity.ok(ApiResponse.success("권한 제거 성공", null));
    }

    /**
     * 역할의 권한 목록 조회
     * GET /api/roles/{id}/permissions
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "역할의 권한 목록", description = "역할에 할당된 모든 권한 조회")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(
            @PathVariable Long id) {

        log.info("Getting permissions for role: {}", id);

        List<PermissionResponse> permissions = roleService.findPermissionsByRole(id).stream()
                .map(this::toPermissionResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("역할 권한 목록 조회 성공", permissions));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private RoleResponse toRoleResponse(RoleEntity role) {
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .status(role.getIsActive() ? "ACTIVE" : "INACTIVE")
                .tenantId(role.getTenant().getTenantId())
                .tenantName(role.getTenant().getTenantName())
                .config(role.getConfig())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    /**
     * Permission Entity를 Response DTO로 변환
     */
    private PermissionResponse toPermissionResponse(PermissionEntity permission) {
        return PermissionResponse.builder()
                .permissionId(permission.getPermissionId())
                .permissionCode(permission.getPermissionCode())
                .permissionName(permission.getPermissionName())
                .module(permission.getModule())
                .description(permission.getDescription())
                .status(permission.getStatus())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
