package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.annotation.Audited;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.theme.ThemeCreateRequest;
import kr.co.softice.mes.common.dto.theme.ThemeResponse;
import kr.co.softice.mes.common.dto.theme.ThemeUpdateRequest;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.ThemeEntity;
import kr.co.softice.mes.domain.service.ThemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Theme Controller
 * 테마 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@Tag(name = "Theme Management", description = "테마 관리 API")
public class ThemeController {

    private final ThemeService themeService;

    /**
     * 테마 관리 권한 체크 (softice tenant만 가능)
     */
    private void checkThemeManagementPermission() {
        String tenantId = TenantContext.getCurrentTenant();
        if (!"softice".equals(tenantId)) {
            log.warn("Unauthorized theme management attempt by tenant: {}", tenantId);
            throw new BusinessException(ErrorCode.FORBIDDEN, "테마 관리는 개발사(SoftIce)만 접근 가능합니다.");
        }
    }

    /**
     * 테마 목록 조회
     * GET /api/themes
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "테마 목록 조회", description = "모든 테마 목록 조회")
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> getThemes() {
        log.info("Getting all themes");

        List<ThemeResponse> themes = themeService.findAll().stream()
                .map(this::toThemeResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("테마 목록 조회 성공", themes));
    }

    /**
     * 활성 테마 목록 조회
     * GET /api/themes/active
     */
    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 테마 목록 조회", description = "활성 상태의 테마만 조회")
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> getActiveThemes() {
        log.info("Getting active themes");

        List<ThemeResponse> themes = themeService.findActiveThemes().stream()
                .map(this::toThemeResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("활성 테마 목록 조회 성공", themes));
    }

    /**
     * 기본 테마 조회
     * GET /api/themes/default
     */
    @Transactional(readOnly = true)
    @GetMapping("/default")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기본 테마 조회", description = "시스템 기본 테마 조회")
    public ResponseEntity<ApiResponse<ThemeResponse>> getDefaultTheme() {
        log.info("Getting default theme");

        ThemeEntity theme = themeService.getDefaultTheme();

        return ResponseEntity.ok(ApiResponse.success("기본 테마 조회 성공", toThemeResponse(theme)));
    }

    /**
     * 산업별 테마 목록 조회
     * GET /api/themes/industry/{industryType}
     */
    @Transactional(readOnly = true)
    @GetMapping("/industry/{industryType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "산업별 테마 조회", description = "특정 산업에 최적화된 테마 목록 조회")
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> getThemesByIndustry(
            @PathVariable String industryType) {

        log.info("Getting themes for industry: {}", industryType);

        List<ThemeResponse> themes = themeService.findByIndustryType(industryType).stream()
                .map(this::toThemeResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("산업별 테마 목록 조회 성공", themes));
    }

    /**
     * 테마 상세 조회 (by code)
     * GET /api/themes/code/{themeCode}
     */
    @Transactional(readOnly = true)
    @GetMapping("/code/{themeCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "테마 조회 (코드)", description = "테마 코드로 상세 정보 조회")
    public ResponseEntity<ApiResponse<ThemeResponse>> getThemeByCode(@PathVariable String themeCode) {
        log.info("Getting theme by code: {}", themeCode);

        ThemeEntity theme = themeService.findByThemeCode(themeCode)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.THEME_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("테마 조회 성공", toThemeResponse(theme)));
    }

    /**
     * 테마 상세 조회 (by ID)
     * GET /api/themes/{id}
     */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "테마 조회 (ID)", description = "테마 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<ThemeResponse>> getTheme(@PathVariable Long id) {
        log.info("Getting theme: {}", id);

        ThemeEntity theme = themeService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.THEME_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("테마 조회 성공", toThemeResponse(theme)));
    }

    /**
     * 테마 생성
     * POST /api/themes
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = "CREATE", entityType = "Theme", description = "테마 생성")
    @Operation(summary = "테마 생성", description = "새로운 테마 등록 (개발사만 가능)")
    public ResponseEntity<ApiResponse<ThemeResponse>> createTheme(
            @Valid @RequestBody ThemeCreateRequest request) {

        checkThemeManagementPermission();
        log.info("Creating theme: {}", request.getThemeCode());

        ThemeEntity theme = ThemeEntity.builder()
                .themeCode(request.getThemeCode())
                .themeName(request.getThemeName())
                .industryType(request.getIndustryType())
                .description(request.getDescription())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .status("active")
                .colorScheme(request.getColorScheme())
                .typography(request.getTypography())
                .layout(request.getLayout())
                .components(request.getComponents())
                .enabledModules(request.getEnabledModules())
                .additionalConfig(request.getAdditionalConfig())
                .build();

        ThemeEntity createdTheme = themeService.createTheme(theme);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("테마 생성 성공", toThemeResponse(createdTheme)));
    }

    /**
     * 테마 수정
     * PUT /api/themes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = "UPDATE", entityType = "Theme", description = "테마 수정")
    @Operation(summary = "테마 수정", description = "테마 정보 수정 (개발사만 가능)")
    public ResponseEntity<ApiResponse<ThemeResponse>> updateTheme(
            @PathVariable Long id,
            @Valid @RequestBody ThemeUpdateRequest request) {

        checkThemeManagementPermission();
        log.info("Updating theme: {}", id);

        ThemeEntity theme = themeService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.THEME_NOT_FOUND));

        if (request.getThemeName() != null) {
            theme.setThemeName(request.getThemeName());
        }
        if (request.getIndustryType() != null) {
            theme.setIndustryType(request.getIndustryType());
        }
        if (request.getDescription() != null) {
            theme.setDescription(request.getDescription());
        }
        if (request.getColorScheme() != null) {
            theme.setColorScheme(request.getColorScheme());
        }
        if (request.getTypography() != null) {
            theme.setTypography(request.getTypography());
        }
        if (request.getLayout() != null) {
            theme.setLayout(request.getLayout());
        }
        if (request.getComponents() != null) {
            theme.setComponents(request.getComponents());
        }
        if (request.getEnabledModules() != null) {
            theme.setEnabledModules(request.getEnabledModules());
        }
        if (request.getAdditionalConfig() != null) {
            theme.setAdditionalConfig(request.getAdditionalConfig());
        }

        ThemeEntity updatedTheme = themeService.updateTheme(theme);

        return ResponseEntity.ok(ApiResponse.success("테마 수정 성공", toThemeResponse(updatedTheme)));
    }

    /**
     * 테마 삭제
     * DELETE /api/themes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = "DELETE", entityType = "Theme", description = "테마 삭제")
    @Operation(summary = "테마 삭제", description = "테마 완전 삭제 (개발사만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteTheme(@PathVariable Long id) {
        checkThemeManagementPermission();
        log.info("Deleting theme: {}", id);

        themeService.deleteTheme(id);

        return ResponseEntity.ok(ApiResponse.success("테마 삭제 성공", null));
    }

    /**
     * 테마 활성화
     * PUT /api/themes/{id}/activate
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = "ACTIVATE", entityType = "Theme", description = "테마 활성화")
    @Operation(summary = "테마 활성화", description = "비활성 테마를 활성화 (관리자만 가능)")
    public ResponseEntity<ApiResponse<ThemeResponse>> activateTheme(@PathVariable Long id) {
        log.info("Activating theme: {}", id);

        ThemeEntity theme = themeService.activateTheme(id);

        return ResponseEntity.ok(ApiResponse.success("테마 활성화 성공", toThemeResponse(theme)));
    }

    /**
     * 테마 비활성화
     * PUT /api/themes/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = "DEACTIVATE", entityType = "Theme", description = "테마 비활성화")
    @Operation(summary = "테마 비활성화", description = "활성 테마를 비활성화 (관리자만 가능)")
    public ResponseEntity<ApiResponse<ThemeResponse>> deactivateTheme(@PathVariable Long id) {
        log.info("Deactivating theme: {}", id);

        ThemeEntity theme = themeService.deactivateTheme(id);

        return ResponseEntity.ok(ApiResponse.success("테마 비활성화 성공", toThemeResponse(theme)));
    }

    /**
     * 기본 테마 설정
     * PUT /api/themes/{id}/set-default
     */
    @PutMapping("/{id}/set-default")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = "SET_DEFAULT", entityType = "Theme", description = "기본 테마 설정")
    @Operation(summary = "기본 테마 설정", description = "테마를 시스템 기본 테마로 설정 (관리자만 가능)")
    public ResponseEntity<ApiResponse<ThemeResponse>> setDefaultTheme(@PathVariable Long id) {
        log.info("Setting default theme: {}", id);

        ThemeEntity theme = themeService.setDefaultTheme(id);

        return ResponseEntity.ok(ApiResponse.success("기본 테마 설정 성공", toThemeResponse(theme)));
    }

    /**
     * 프리셋 테마 초기화
     * POST /api/themes/initialize-presets
     */
    @PostMapping("/initialize-presets")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = "INITIALIZE_PRESETS", entityType = "Theme", description = "프리셋 테마 초기화")
    @Operation(summary = "프리셋 테마 초기화", description = "산업별 기본 테마 프리셋 초기화 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> initializePresetThemes() {
        log.info("Initializing preset themes");

        themeService.initializePresetThemes();

        return ResponseEntity.ok(ApiResponse.success("프리셋 테마 초기화 성공", null));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private ThemeResponse toThemeResponse(ThemeEntity theme) {
        return ThemeResponse.builder()
                .themeId(theme.getThemeId())
                .themeCode(theme.getThemeCode())
                .themeName(theme.getThemeName())
                .industryType(theme.getIndustryType())
                .description(theme.getDescription())
                .isDefault(theme.getIsDefault())
                .status(theme.getStatus())
                .colorScheme(theme.getColorScheme())
                .typography(theme.getTypography())
                .layout(theme.getLayout())
                .components(theme.getComponents())
                .enabledModules(theme.getEnabledModules())
                .additionalConfig(theme.getAdditionalConfig())
                .createdAt(theme.getCreatedAt())
                .updatedAt(theme.getUpdatedAt())
                .build();
    }
}
