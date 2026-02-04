package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.DuplicateEntityException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.ThemeEntity;
import kr.co.softice.mes.domain.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Theme Service
 * 테마 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;

    /**
     * ID로 테마 조회
     */
    public Optional<ThemeEntity> findById(Long themeId) {
        return themeRepository.findById(themeId);
    }

    /**
     * 테마 코드로 조회
     */
    public Optional<ThemeEntity> findByThemeCode(String themeCode) {
        return themeRepository.findByThemeCode(themeCode);
    }

    /**
     * 모든 테마 조회
     */
    public List<ThemeEntity> findAll() {
        return themeRepository.findAllThemes();
    }

    /**
     * 활성 테마 목록 조회
     */
    public List<ThemeEntity> findActiveThemes() {
        return themeRepository.findActiveThemes();
    }

    /**
     * 산업 유형별 테마 조회
     */
    public List<ThemeEntity> findByIndustryType(String industryType) {
        return themeRepository.findByIndustryType(industryType);
    }

    /**
     * 기본 테마 조회
     */
    public ThemeEntity getDefaultTheme() {
        return themeRepository.findDefaultTheme()
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.THEME_NOT_FOUND));
    }

    /**
     * 테마 생성
     */
    @Transactional
    public ThemeEntity createTheme(ThemeEntity theme) {
        log.info("Creating theme: {}", theme.getThemeCode());

        // 중복 코드 확인
        if (themeRepository.existsByThemeCode(theme.getThemeCode())) {
            throw new DuplicateEntityException(ErrorCode.THEME_ALREADY_EXISTS);
        }

        // 기본 테마로 설정 시, 기존 기본 테마 해제
        if (Boolean.TRUE.equals(theme.getIsDefault())) {
            resetDefaultThemes();
        }

        return themeRepository.save(theme);
    }

    /**
     * 테마 수정
     */
    @Transactional
    public ThemeEntity updateTheme(ThemeEntity theme) {
        log.info("Updating theme: {}", theme.getThemeId());

        // 기본 테마로 설정 시, 기존 기본 테마 해제
        if (Boolean.TRUE.equals(theme.getIsDefault())) {
            resetDefaultThemes();
        }

        return themeRepository.save(theme);
    }

    /**
     * 테마 삭제
     */
    @Transactional
    public void deleteTheme(Long themeId) {
        log.info("Deleting theme: {}", themeId);

        ThemeEntity theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.THEME_NOT_FOUND));

        // 기본 테마는 삭제 불가
        if (Boolean.TRUE.equals(theme.getIsDefault())) {
            throw new IllegalStateException("기본 테마는 삭제할 수 없습니다");
        }

        themeRepository.delete(theme);
    }

    /**
     * 테마 활성화
     */
    @Transactional
    public ThemeEntity activateTheme(Long themeId) {
        log.info("Activating theme: {}", themeId);

        ThemeEntity theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.THEME_NOT_FOUND));

        theme.setStatus("active");
        return themeRepository.save(theme);
    }

    /**
     * 테마 비활성화
     */
    @Transactional
    public ThemeEntity deactivateTheme(Long themeId) {
        log.info("Deactivating theme: {}", themeId);

        ThemeEntity theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.THEME_NOT_FOUND));

        // 기본 테마는 비활성화 불가
        if (Boolean.TRUE.equals(theme.getIsDefault())) {
            throw new IllegalStateException("기본 테마는 비활성화할 수 없습니다");
        }

        theme.setStatus("inactive");
        return themeRepository.save(theme);
    }

    /**
     * 기본 테마 설정
     */
    @Transactional
    public ThemeEntity setDefaultTheme(Long themeId) {
        log.info("Setting default theme: {}", themeId);

        ThemeEntity theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.THEME_NOT_FOUND));

        // 기존 기본 테마 해제
        resetDefaultThemes();

        // 새 기본 테마 설정 및 활성화
        theme.setIsDefault(true);
        theme.setStatus("active");

        return themeRepository.save(theme);
    }

    /**
     * 산업별 프리셋 테마 초기화
     */
    @Transactional
    public void initializePresetThemes() {
        log.info("Initializing preset themes");

        // 화학 제조 테마
        if (!themeRepository.existsByThemeCode("CHEMICAL")) {
            ThemeEntity chemicalTheme = createChemicalTheme();
            themeRepository.save(chemicalTheme);
        }

        // 전자 제조 테마
        if (!themeRepository.existsByThemeCode("ELECTRONICS")) {
            ThemeEntity electronicsTheme = createElectronicsTheme();
            themeRepository.save(electronicsTheme);
        }

        // 의료기기 제조 테마
        if (!themeRepository.existsByThemeCode("MEDICAL")) {
            ThemeEntity medicalTheme = createMedicalTheme();
            themeRepository.save(medicalTheme);
        }

        // 식품 제조 테마
        if (!themeRepository.existsByThemeCode("FOOD")) {
            ThemeEntity foodTheme = createFoodTheme();
            themeRepository.save(foodTheme);
        }

        // 기본 테마
        if (!themeRepository.existsByThemeCode("DEFAULT")) {
            ThemeEntity defaultTheme = createDefaultTheme();
            themeRepository.save(defaultTheme);
        }

        log.info("Preset themes initialized successfully");
    }

    /**
     * 기존 기본 테마 모두 해제
     */
    private void resetDefaultThemes() {
        List<ThemeEntity> themes = themeRepository.findAllThemes();
        for (ThemeEntity t : themes) {
            if (Boolean.TRUE.equals(t.getIsDefault())) {
                t.setIsDefault(false);
                themeRepository.save(t);
            }
        }
    }

    /**
     * 화학 제조 테마 생성
     */
    private ThemeEntity createChemicalTheme() {
        Map<String, Object> colorScheme = new HashMap<>();
        colorScheme.put("primary", "#0d47a1"); // Deep Blue
        colorScheme.put("secondary", "#1976d2"); // Blue
        colorScheme.put("success", "#388e3c");
        colorScheme.put("warning", "#f57c00");
        colorScheme.put("error", "#d32f2f");

        Map<String, Object> enabledModules = new HashMap<>();
        enabledModules.put("chemicalInventory", true);
        enabledModules.put("safetyCompliance", true);
        enabledModules.put("batchProcessing", true);
        enabledModules.put("qualityControl", true);

        return ThemeEntity.builder()
                .themeCode("CHEMICAL")
                .themeName("화학 제조업")
                .industryType("CHEMICAL")
                .description("화학 제조업에 최적화된 테마")
                .isDefault(false)
                .status("active")
                .colorScheme(colorScheme)
                .enabledModules(enabledModules)
                .build();
    }

    /**
     * 전자 제조 테마 생성
     */
    private ThemeEntity createElectronicsTheme() {
        Map<String, Object> colorScheme = new HashMap<>();
        colorScheme.put("primary", "#1565c0"); // Tech Blue
        colorScheme.put("secondary", "#00838f"); // Cyan
        colorScheme.put("success", "#2e7d32");
        colorScheme.put("warning", "#ef6c00");
        colorScheme.put("error", "#c62828");

        Map<String, Object> enabledModules = new HashMap<>();
        enabledModules.put("pcbTracking", true);
        enabledModules.put("componentInventory", true);
        enabledModules.put("testAutomation", true);
        enabledModules.put("qualityControl", true);

        return ThemeEntity.builder()
                .themeCode("ELECTRONICS")
                .themeName("전자/전기 제조업")
                .industryType("ELECTRONICS")
                .description("전자/전기 제조업에 최적화된 테마")
                .isDefault(false)
                .status("active")
                .colorScheme(colorScheme)
                .enabledModules(enabledModules)
                .build();
    }

    /**
     * 의료기기 제조 테마 생성
     */
    private ThemeEntity createMedicalTheme() {
        Map<String, Object> colorScheme = new HashMap<>();
        colorScheme.put("primary", "#00695c"); // Medical Teal
        colorScheme.put("secondary", "#0097a7"); // Cyan
        colorScheme.put("success", "#2e7d32");
        colorScheme.put("warning", "#f57c00");
        colorScheme.put("error", "#c62828");

        Map<String, Object> enabledModules = new HashMap<>();
        enabledModules.put("regulatoryCompliance", true);
        enabledModules.put("sterilization", true);
        enabledModules.put("lotTracking", true);
        enabledModules.put("qualityControl", true);

        return ThemeEntity.builder()
                .themeCode("MEDICAL")
                .themeName("의료기기 제조업")
                .industryType("MEDICAL")
                .description("의료기기 제조업에 최적화된 테마")
                .isDefault(false)
                .status("active")
                .colorScheme(colorScheme)
                .enabledModules(enabledModules)
                .build();
    }

    /**
     * 식품 제조 테마 생성
     */
    private ThemeEntity createFoodTheme() {
        Map<String, Object> colorScheme = new HashMap<>();
        colorScheme.put("primary", "#558b2f"); // Green
        colorScheme.put("secondary", "#689f38"); // Light Green
        colorScheme.put("success", "#388e3c");
        colorScheme.put("warning", "#f57c00");
        colorScheme.put("error", "#d32f2f");

        Map<String, Object> enabledModules = new HashMap<>();
        enabledModules.put("foodSafety", true);
        enabledModules.put("expiryTracking", true);
        enabledModules.put("temperatureMonitoring", true);
        enabledModules.put("qualityControl", true);

        return ThemeEntity.builder()
                .themeCode("FOOD")
                .themeName("식품/음료 제조업")
                .industryType("FOOD")
                .description("식품/음료 제조업에 최적화된 테마")
                .isDefault(false)
                .status("active")
                .colorScheme(colorScheme)
                .enabledModules(enabledModules)
                .build();
    }

    /**
     * 기본 테마 생성
     */
    private ThemeEntity createDefaultTheme() {
        Map<String, Object> colorScheme = new HashMap<>();
        colorScheme.put("primary", "#1976d2"); // Material Blue
        colorScheme.put("secondary", "#dc004e"); // Material Pink
        colorScheme.put("success", "#4caf50");
        colorScheme.put("warning", "#ff9800");
        colorScheme.put("error", "#f44336");

        Map<String, Object> enabledModules = new HashMap<>();
        enabledModules.put("production", true);
        enabledModules.put("inventory", true);
        enabledModules.put("qualityControl", true);

        return ThemeEntity.builder()
                .themeCode("DEFAULT")
                .themeName("기본 테마")
                .industryType("GENERAL")
                .description("범용 제조업 기본 테마")
                .isDefault(true)
                .status("active")
                .colorScheme(colorScheme)
                .enabledModules(enabledModules)
                .build();
    }
}
