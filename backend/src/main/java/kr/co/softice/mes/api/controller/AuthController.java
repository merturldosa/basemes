package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.auth.LoginRequest;
import kr.co.softice.mes.common.dto.auth.LoginResponse;
import kr.co.softice.mes.common.dto.auth.TokenRefreshRequest;
import kr.co.softice.mes.common.dto.auth.TokenRefreshResponse;
import kr.co.softice.mes.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 인증 관련 엔드포인트
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 및 JWT 토큰 발급")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request for user: {} in tenant: {}",
                loginRequest.getUsername(), loginRequest.getTenantId());

        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    /**
     * 토큰 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token 발급")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request");

        try {
            TokenRefreshResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request");

        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * 현재 사용자 정보 조회
     * GET /api/auth/me
     */
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보", description = "인증된 사용자의 정보 조회")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser() {
        // TODO: SecurityContext에서 현재 사용자 정보 추출하여 반환
        return ResponseEntity.ok(ApiResponse.success("User info retrieved", null));
    }
}
