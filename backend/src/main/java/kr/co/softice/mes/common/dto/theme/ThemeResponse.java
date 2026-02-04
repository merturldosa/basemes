package kr.co.softice.mes.common.dto.theme;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Theme Response DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeResponse {

    private Long themeId;
    private String themeCode;
    private String themeName;
    private String industryType;
    private String description;
    private Boolean isDefault;
    private String status;
    private Map<String, Object> colorScheme;
    private Map<String, Object> typography;
    private Map<String, Object> layout;
    private Map<String, Object> components;
    private Map<String, Object> enabledModules;
    private Map<String, Object> additionalConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
