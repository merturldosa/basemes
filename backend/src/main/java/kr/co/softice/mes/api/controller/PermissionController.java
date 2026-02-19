package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.permission.PermissionCreateRequest;
import kr.co.softice.mes.common.dto.permission.PermissionResponse;
import kr.co.softice.mes.common.dto.permission.PermissionUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.PermissionEntity;
import kr.co.softice.mes.domain.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Permission Controller
 * 권한 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "권한 관리 API")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 전체 권한 목록 조회
     * GET /api/permissions
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "권한 목록 조회", description = "시스템의 모든 권한 조회")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions() {
        log.info("Getting all permissions");

        List<PermissionResponse> permissions = permissionService.findAll().stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("권한 목록 조회 성공", permissions));
    }

    /**
     * 활성 권한 목록 조회
     * GET /api/permissions/active
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 권한 목록 조회", description = "활성 상태의 권한만 조회")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getActivePermissions() {
        log.info("Getting active permissions");

        List<PermissionResponse> permissions = permissionService.findActivePermissions().stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("활성 권한 목록 조회 성공", permissions));
    }

    /**
     * 모듈별 권한 목록 조회
     * GET /api/permissions/module/{module}
     */
    @GetMapping("/module/{module}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "모듈별 권한 조회", description = "특정 모듈의 권한 목록 조회")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByModule(
            @PathVariable String module) {

        log.info("Getting permissions for module: {}", module);

        List<PermissionResponse> permissions = permissionService.findByModule(module).stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("모듈 권한 목록 조회 성공", permissions));
    }

    /**
     * 권한 상세 조회
     * GET /api/permissions/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "권한 상세 조회", description = "권한 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(@PathVariable Long id) {
        log.info("Getting permission: {}", id);

        PermissionEntity permission = permissionService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("권한 조회 성공", toPermissionResponse(permission)));
    }

    /**
     * 권한 생성
     * POST /api/permissions
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "권한 생성", description = "신규 권한 등록 (관리자만 가능)")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionCreateRequest request) {

        log.info("Creating permission: {}", request.getPermissionCode());

        PermissionEntity permission = PermissionEntity.builder()
                .permissionCode(request.getPermissionCode())
                .permissionName(request.getPermissionName())
                .module(request.getModule())
                .description(request.getDescription())
                .status("active")
                .build();

        PermissionEntity createdPermission = permissionService.createPermission(permission);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("권한 생성 성공", toPermissionResponse(createdPermission)));
    }

    /**
     * 권한 수정
     * PUT /api/permissions/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "권한 수정", description = "권한 정보 수정 (관리자만 가능)")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateRequest request) {

        log.info("Updating permission: {}", id);

        PermissionEntity permission = permissionService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND));

        if (request.getPermissionName() != null) {
            permission.setPermissionName(request.getPermissionName());
        }
        if (request.getModule() != null) {
            permission.setModule(request.getModule());
        }
        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }

        PermissionEntity updatedPermission = permissionService.updatePermission(permission);

        return ResponseEntity.ok(ApiResponse.success("권한 수정 성공", toPermissionResponse(updatedPermission)));
    }

    /**
     * 권한 활성화
     * PUT /api/permissions/{id}/activate
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "권한 활성화", description = "비활성 권한을 활성화 (관리자만 가능)")
    public ResponseEntity<ApiResponse<PermissionResponse>> activatePermission(@PathVariable Long id) {
        log.info("Activating permission: {}", id);

        PermissionEntity permission = permissionService.activatePermission(id);

        return ResponseEntity.ok(ApiResponse.success("권한 활성화 성공", toPermissionResponse(permission)));
    }

    /**
     * 권한 비활성화
     * PUT /api/permissions/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "권한 비활성화", description = "활성 권한을 비활성화 (관리자만 가능)")
    public ResponseEntity<ApiResponse<PermissionResponse>> deactivatePermission(@PathVariable Long id) {
        log.info("Deactivating permission: {}", id);

        PermissionEntity permission = permissionService.deactivatePermission(id);

        return ResponseEntity.ok(ApiResponse.success("권한 비활성화 성공", toPermissionResponse(permission)));
    }

    /**
     * 권한 삭제
     * DELETE /api/permissions/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "권한 삭제", description = "권한 완전 삭제 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        log.info("Deleting permission: {}", id);

        permissionService.deletePermission(id);

        return ResponseEntity.ok(ApiResponse.success("권한 삭제 성공", null));
    }

    /**
     * Entity를 Response DTO로 변환
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
