package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.auth.LoginRequest;
import kr.co.softice.mes.common.dto.auth.LoginResponse;
import kr.co.softice.mes.common.dto.auth.TokenRefreshResponse;
import kr.co.softice.mes.common.security.JwtTokenProvider;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.common.security.UserPrincipal;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service
 * 인증 관련 비즈니스 로직
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {} in tenant: {}",
                loginRequest.getUsername(), loginRequest.getTenantId());

        // Tenant Context 설정
        TenantContext.setCurrentTenant(loginRequest.getTenantId());

        try {
            // 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 토큰 생성
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            // 사용자 정보 조회
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            UserEntity user = userRepository.findById(userPrincipal.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 마지막 로그인 시간 업데이트
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("Login successful for user: {} in tenant: {}",
                    loginRequest.getUsername(), loginRequest.getTenantId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration / 1000) // seconds
                    .user(LoginResponse.UserInfo.builder()
                            .userId(user.getUserId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .tenantId(user.getTenant().getTenantId())
                            .tenantName(user.getTenant().getTenantName())
                            .build())
                    .build();
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 토큰 갱신
     */
    public TokenRefreshResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        // Refresh Token 유효성 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // User ID 추출
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        String tenantId = tokenProvider.getTenantIdFromToken(refreshToken);

        // Tenant Context 설정
        TenantContext.setCurrentTenant(tenantId);

        try {
            // 사용자 조회
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 사용자 활성 상태 확인
            if (!"active".equals(user.getStatus())) {
                throw new RuntimeException("User is not active");
            }

            // 새로운 Access Token 생성을 위한 Authentication 객체 생성
            // 실제 환경에서는 CustomUserDetailsService를 통해 UserDetails를 로드해야 함
            UserPrincipal userPrincipal = UserPrincipal.builder()
                    .userId(user.getUserId())
                    .tenantId(user.getTenant().getTenantId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities(null) // 실제로는 roles를 조회해야 함
                    .enabled(true)
                    .build();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userPrincipal.getAuthorities());

            // 새로운 Access Token 생성
            String newAccessToken = tokenProvider.generateAccessToken(authentication);

            log.info("Token refresh successful for user: {}", userId);

            return TokenRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration / 1000)
                    .build();
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 로그아웃
     */
    public void logout() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
        log.info("User logged out");
    }
}
