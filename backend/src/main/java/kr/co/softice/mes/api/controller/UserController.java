package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.PageResponse;
import kr.co.softice.mes.common.dto.user.ChangePasswordRequest;
import kr.co.softice.mes.common.dto.user.UserCreateRequest;
import kr.co.softice.mes.common.dto.user.UserResponse;
import kr.co.softice.mes.common.dto.user.UserUpdateRequest;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 * 사용자 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;
    private final TenantRepository tenantRepository;

    /**
     * 사용자 목록 조회 (페이징)
     * GET /api/users?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER')")
    @Operation(summary = "사용자 목록 조회", description = "페이징 처리된 사용자 목록 조회")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting users for tenant: {}", tenantId);

        // Sort 파라미터 파싱
        Sort.Direction direction = sort.length > 1 && "desc".equalsIgnoreCase(sort[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        // TODO: Repository에 페이징 메서드 추가 필요
        // Page<UserEntity> userPage = userRepository.findByTenant_TenantId(tenantId, pageable);

        // 임시로 전체 조회 후 수동 페이징
        var users = userService.findByTenant(tenantId).stream()
                .map(this::toUserResponse)
                .collect(java.util.stream.Collectors.toList());

        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .content(users)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(users.size())
                .totalPages((int) Math.ceil((double) users.size() / size))
                .first(page == 0)
                .last(true)
                .empty(users.isEmpty())
                .build();

        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 성공", pageResponse));
    }

    /**
     * 사용자 상세 조회
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER') or #id == authentication.principal.userId")
    @Operation(summary = "사용자 상세 조회", description = "사용자 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        log.info("Getting user: {}", id);

        UserEntity user = userService.findById(id)
                .orElseThrow(() -> new kr.co.softice.mes.common.exception.EntityNotFoundException(
                        kr.co.softice.mes.common.exception.ErrorCode.USER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("사용자 조회 성공", toUserResponse(user)));
    }

    /**
     * 사용자 생성
     * POST /api/users
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER')")
    @Operation(summary = "사용자 생성", description = "신규 사용자 등록")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating user: {} for tenant: {}", request.getUsername(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new kr.co.softice.mes.common.exception.EntityNotFoundException(
                        kr.co.softice.mes.common.exception.ErrorCode.TENANT_NOT_FOUND));

        UserEntity user = UserEntity.builder()
                .tenant(tenant)
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .preferredLanguage(request.getPreferredLanguage() != null
                        ? request.getPreferredLanguage() : "ko")
                .status("active")
                .build();

        UserEntity createdUser = userService.createUser(user, request.getPassword());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("사용자 생성 성공", toUserResponse(createdUser)));
    }

    /**
     * 사용자 수정
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER') or #id == authentication.principal.userId")
    @Operation(summary = "사용자 수정", description = "사용자 정보 수정")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        log.info("Updating user: {}", id);

        UserEntity user = userService.findById(id)
                .orElseThrow(() -> new kr.co.softice.mes.common.exception.EntityNotFoundException(
                        kr.co.softice.mes.common.exception.ErrorCode.USER_NOT_FOUND));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }

        UserEntity updatedUser = userService.updateUser(user);

        return ResponseEntity.ok(ApiResponse.success("사용자 수정 성공", toUserResponse(updatedUser)));
    }

    /**
     * 비밀번호 변경
     * PUT /api/users/{id}/password
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("#id == authentication.principal.userId")
    @Operation(summary = "비밀번호 변경", description = "사용자 비밀번호 변경")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Changing password for user: {}", id);

        userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 성공", null));
    }

    /**
     * 사용자 활성화
     * PUT /api/users/{id}/activate
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER')")
    @Operation(summary = "사용자 활성화", description = "비활성 사용자를 활성화")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        log.info("Activating user: {}", id);

        UserEntity user = userService.activateUser(id);

        return ResponseEntity.ok(ApiResponse.success("사용자 활성화 성공", toUserResponse(user)));
    }

    /**
     * 사용자 비활성화
     * PUT /api/users/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER')")
    @Operation(summary = "사용자 비활성화", description = "활성 사용자를 비활성화")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user: {}", id);

        UserEntity user = userService.deactivateUser(id);

        return ResponseEntity.ok(ApiResponse.success("사용자 비활성화 성공", toUserResponse(user)));
    }

    /**
     * 사용자 삭제
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 삭제", description = "사용자 완전 삭제 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success("사용자 삭제 성공", null));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private UserResponse toUserResponse(UserEntity user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .preferredLanguage(user.getPreferredLanguage())
                .tenantId(user.getTenant().getTenantId())
                .tenantName(user.getTenant().getTenantName())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
