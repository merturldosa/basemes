package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.DuplicateEntityException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.domain.entity.ThemeEntity;
import kr.co.softice.mes.domain.repository.ThemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Theme Service Test
 * 테마 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("테마 서비스 테스트")
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    private ThemeEntity testTheme;

    @BeforeEach
    void setUp() {
        testTheme = ThemeEntity.builder()
                .themeId(1L)
                .themeCode("DEFAULT")
                .themeName("기본 테마")
                .industryType("GENERAL")
                .description("범용 제조업 기본 테마")
                .isDefault(true)
                .status("active")
                .colorScheme(new HashMap<>())
                .enabledModules(new HashMap<>())
                .build();
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("ID로 테마 조회 - 성공")
    void testFindById_Success() {
        when(themeRepository.findById(1L))
                .thenReturn(Optional.of(testTheme));

        Optional<ThemeEntity> result = themeService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getThemeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ID로 테마 조회 - 없음")
    void testFindById_NotFound() {
        when(themeRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<ThemeEntity> result = themeService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("테마 코드로 조회 - 성공")
    void testFindByThemeCode_Success() {
        when(themeRepository.findByThemeCode("DEFAULT"))
                .thenReturn(Optional.of(testTheme));

        Optional<ThemeEntity> result = themeService.findByThemeCode("DEFAULT");

        assertThat(result).isPresent();
        assertThat(result.get().getThemeCode()).isEqualTo("DEFAULT");
    }

    @Test
    @DisplayName("모든 테마 조회 - 성공")
    void testFindAll_Success() {
        List<ThemeEntity> themes = Arrays.asList(testTheme);
        when(themeRepository.findAllThemes())
                .thenReturn(themes);

        List<ThemeEntity> result = themeService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("활성 테마 목록 조회 - 성공")
    void testFindActiveThemes_Success() {
        List<ThemeEntity> themes = Arrays.asList(testTheme);
        when(themeRepository.findActiveThemes())
                .thenReturn(themes);

        List<ThemeEntity> result = themeService.findActiveThemes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("active");
    }

    @Test
    @DisplayName("산업 유형별 테마 조회 - 성공")
    void testFindByIndustryType_Success() {
        List<ThemeEntity> themes = Arrays.asList(testTheme);
        when(themeRepository.findByIndustryType("GENERAL"))
                .thenReturn(themes);

        List<ThemeEntity> result = themeService.findByIndustryType("GENERAL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIndustryType()).isEqualTo("GENERAL");
    }

    @Test
    @DisplayName("기본 테마 조회 - 성공")
    void testGetDefaultTheme_Success() {
        when(themeRepository.findDefaultTheme())
                .thenReturn(Optional.of(testTheme));

        ThemeEntity result = themeService.getDefaultTheme();

        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 테마 조회 - 실패 (없음)")
    void testGetDefaultTheme_Fail_NotFound() {
        when(themeRepository.findDefaultTheme())
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.getDefaultTheme())
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("테마 생성 - 성공")
    void testCreateTheme_Success() {
        ThemeEntity newTheme = ThemeEntity.builder()
                .themeCode("CHEMICAL")
                .themeName("화학 제조업")
                .isDefault(false)
                .build();

        when(themeRepository.existsByThemeCode("CHEMICAL"))
                .thenReturn(false);
        when(themeRepository.save(newTheme))
                .thenReturn(newTheme);

        ThemeEntity result = themeService.createTheme(newTheme);

        assertThat(result).isNotNull();
        verify(themeRepository).save(newTheme);
    }

    @Test
    @DisplayName("테마 생성 - 실패 (중복 코드)")
    void testCreateTheme_Fail_DuplicateCode() {
        when(themeRepository.existsByThemeCode("DEFAULT"))
                .thenReturn(true);

        assertThatThrownBy(() -> themeService.createTheme(testTheme))
                .isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    @DisplayName("테마 생성 - 기본 테마 설정 시 기존 해제")
    void testCreateTheme_SetAsDefault() {
        ThemeEntity newDefaultTheme = ThemeEntity.builder()
                .themeCode("NEW_DEFAULT")
                .themeName("새 기본 테마")
                .isDefault(true)
                .build();

        when(themeRepository.existsByThemeCode("NEW_DEFAULT"))
                .thenReturn(false);
        when(themeRepository.findAllThemes())
                .thenReturn(Arrays.asList(testTheme));
        when(themeRepository.save(any(ThemeEntity.class)))
                .thenReturn(newDefaultTheme);

        themeService.createTheme(newDefaultTheme);

        verify(themeRepository, times(2)).save(any(ThemeEntity.class));
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("테마 수정 - 성공")
    void testUpdateTheme_Success() {
        when(themeRepository.save(testTheme))
                .thenReturn(testTheme);

        ThemeEntity result = themeService.updateTheme(testTheme);

        assertThat(result).isNotNull();
        verify(themeRepository).save(testTheme);
    }

    @Test
    @DisplayName("테마 수정 - 기본 테마로 변경 시 기존 해제")
    void testUpdateTheme_SetAsDefault() {
        testTheme.setIsDefault(true);
        when(themeRepository.findAllThemes())
                .thenReturn(Arrays.asList(testTheme));
        when(themeRepository.save(any(ThemeEntity.class)))
                .thenReturn(testTheme);

        themeService.updateTheme(testTheme);

        verify(themeRepository, atLeast(1)).save(any(ThemeEntity.class));
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("테마 삭제 - 성공")
    void testDeleteTheme_Success() {
        ThemeEntity nonDefaultTheme = ThemeEntity.builder()
                .themeId(2L)
                .themeCode("CUSTOM")
                .isDefault(false)
                .build();

        when(themeRepository.findById(2L))
                .thenReturn(Optional.of(nonDefaultTheme));

        themeService.deleteTheme(2L);

        verify(themeRepository).delete(nonDefaultTheme);
    }

    @Test
    @DisplayName("테마 삭제 - 실패 (테마 없음)")
    void testDeleteTheme_Fail_NotFound() {
        when(themeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.deleteTheme(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("테마 삭제 - 실패 (기본 테마)")
    void testDeleteTheme_Fail_DefaultTheme() {
        when(themeRepository.findById(1L))
                .thenReturn(Optional.of(testTheme));

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("기본 테마는 삭제할 수 없습니다");
    }

    // === 활성화/비활성화 테스트 ===

    @Test
    @DisplayName("테마 활성화 - 성공")
    void testActivateTheme_Success() {
        testTheme.setStatus("inactive");
        when(themeRepository.findById(1L))
                .thenReturn(Optional.of(testTheme));
        when(themeRepository.save(any(ThemeEntity.class)))
                .thenReturn(testTheme);

        ThemeEntity result = themeService.activateTheme(1L);

        assertThat(result).isNotNull();
        verify(themeRepository).save(argThat(theme ->
                "active".equals(theme.getStatus())));
    }

    @Test
    @DisplayName("테마 활성화 - 실패 (테마 없음)")
    void testActivateTheme_Fail_NotFound() {
        when(themeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.activateTheme(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("테마 비활성화 - 성공")
    void testDeactivateTheme_Success() {
        ThemeEntity nonDefaultTheme = ThemeEntity.builder()
                .themeId(2L)
                .isDefault(false)
                .status("active")
                .build();

        when(themeRepository.findById(2L))
                .thenReturn(Optional.of(nonDefaultTheme));
        when(themeRepository.save(any(ThemeEntity.class)))
                .thenReturn(nonDefaultTheme);

        ThemeEntity result = themeService.deactivateTheme(2L);

        assertThat(result).isNotNull();
        verify(themeRepository).save(argThat(theme ->
                "inactive".equals(theme.getStatus())));
    }

    @Test
    @DisplayName("테마 비활성화 - 실패 (기본 테마)")
    void testDeactivateTheme_Fail_DefaultTheme() {
        when(themeRepository.findById(1L))
                .thenReturn(Optional.of(testTheme));

        assertThatThrownBy(() -> themeService.deactivateTheme(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("기본 테마는 비활성화할 수 없습니다");
    }

    // === 기본 테마 설정 테스트 ===

    @Test
    @DisplayName("기본 테마 설정 - 성공")
    void testSetDefaultTheme_Success() {
        ThemeEntity newDefaultTheme = ThemeEntity.builder()
                .themeId(2L)
                .themeCode("CHEMICAL")
                .isDefault(false)
                .status("inactive")
                .build();

        when(themeRepository.findById(2L))
                .thenReturn(Optional.of(newDefaultTheme));
        when(themeRepository.findAllThemes())
                .thenReturn(Arrays.asList(testTheme, newDefaultTheme));
        when(themeRepository.save(any(ThemeEntity.class)))
                .thenReturn(newDefaultTheme);

        ThemeEntity result = themeService.setDefaultTheme(2L);

        assertThat(result).isNotNull();
        verify(themeRepository).save(argThat(theme ->
                theme.getIsDefault() == true && "active".equals(theme.getStatus())));
    }

    @Test
    @DisplayName("기본 테마 설정 - 실패 (테마 없음)")
    void testSetDefaultTheme_Fail_NotFound() {
        when(themeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.setDefaultTheme(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 프리셋 테마 초기화 테스트 ===

    @Test
    @DisplayName("프리셋 테마 초기화 - 성공 (모든 테마 생성)")
    void testInitializePresetThemes_Success() {
        when(themeRepository.existsByThemeCode(anyString()))
                .thenReturn(false);
        when(themeRepository.save(any(ThemeEntity.class)))
                .thenReturn(testTheme);

        themeService.initializePresetThemes();

        verify(themeRepository, times(5)).save(any(ThemeEntity.class));
    }

    @Test
    @DisplayName("프리셋 테마 초기화 - 이미 존재하는 테마 스킵")
    void testInitializePresetThemes_SkipExisting() {
        when(themeRepository.existsByThemeCode("CHEMICAL"))
                .thenReturn(true);
        when(themeRepository.existsByThemeCode("ELECTRONICS"))
                .thenReturn(true);
        when(themeRepository.existsByThemeCode("MEDICAL"))
                .thenReturn(false);
        when(themeRepository.existsByThemeCode("FOOD"))
                .thenReturn(false);
        when(themeRepository.existsByThemeCode("DEFAULT"))
                .thenReturn(false);
        when(themeRepository.save(any(ThemeEntity.class)))
                .thenReturn(testTheme);

        themeService.initializePresetThemes();

        verify(themeRepository, times(3)).save(any(ThemeEntity.class));
    }

    @Test
    @DisplayName("프리셋 테마 초기화 - 모든 테마 존재 (저장 없음)")
    void testInitializePresetThemes_AllExist() {
        when(themeRepository.existsByThemeCode(anyString()))
                .thenReturn(true);

        themeService.initializePresetThemes();

        verify(themeRepository, never()).save(any(ThemeEntity.class));
    }
}
