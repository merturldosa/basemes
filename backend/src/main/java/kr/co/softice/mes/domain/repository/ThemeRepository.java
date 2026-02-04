package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Theme Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ThemeRepository extends JpaRepository<ThemeEntity, Long> {

    /**
     * 테마 코드로 조회
     */
    Optional<ThemeEntity> findByThemeCode(String themeCode);

    /**
     * 테마 코드 존재 여부 확인
     */
    boolean existsByThemeCode(String themeCode);

    /**
     * 활성 테마 목록 조회
     */
    @Query("SELECT t FROM ThemeEntity t WHERE t.status = 'active' ORDER BY t.themeName")
    List<ThemeEntity> findActiveThemes();

    /**
     * 산업 유형별 테마 조회
     */
    @Query("SELECT t FROM ThemeEntity t WHERE t.industryType = :industryType AND t.status = 'active' ORDER BY t.themeName")
    List<ThemeEntity> findByIndustryType(@Param("industryType") String industryType);

    /**
     * 기본 테마 조회
     */
    @Query("SELECT t FROM ThemeEntity t WHERE t.isDefault = true AND t.status = 'active'")
    Optional<ThemeEntity> findDefaultTheme();

    /**
     * 모든 테마 조회 (활성/비활성 포함)
     */
    @Query("SELECT t FROM ThemeEntity t ORDER BY t.isDefault DESC, t.themeName")
    List<ThemeEntity> findAllThemes();
}
