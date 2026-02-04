package kr.co.softice.mes.common.dto.theme;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Theme Create Request DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeCreateRequest {

    @NotBlank(message = "테마 코드는 필수입니다")
    @Size(max = 50, message = "테마 코드는 최대 50자까지 가능합니다")
    private String themeCode;

    @NotBlank(message = "테마명은 필수입니다")
    @Size(max = 100, message = "테마명은 최대 100자까지 가능합니다")
    private String themeName;

    @Size(max = 50, message = "산업 유형은 최대 50자까지 가능합니다")
    private String industryType;

    @Size(max = 500, message = "설명은 최대 500자까지 가능합니다")
    private String description;

    private Boolean isDefault;

    /**
     * 색상 설정 (JSON)
     */
    private Map<String, Object> colorScheme;

    /**
     * 타이포그래피 설정 (JSON)
     */
    private Map<String, Object> typography;

    /**
     * 레이아웃 설정 (JSON)
     */
    private Map<String, Object> layout;

    /**
     * 컴포넌트 스타일 (JSON)
     */
    private Map<String, Object> components;

    /**
     * 산업별 모듈 활성화 (JSON)
     */
    private Map<String, Object> enabledModules;

    /**
     * 추가 설정 (JSON)
     */
    private Map<String, Object> additionalConfig;
}
