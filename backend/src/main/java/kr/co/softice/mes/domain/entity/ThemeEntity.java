package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Theme Entity
 * UI 테마 설정
 *
 * @author Moon Myung-seop
 */
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Entity
@Table(name = "SD_Themes", indexes = {
        @Index(name = "idx_theme_code", columnList = "theme_code", unique = true),
        @Index(name = "idx_theme_industry", columnList = "industry_type"),
        @Index(name = "idx_theme_default", columnList = "is_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_id")
    private Long themeId;

    /**
     * 테마 코드 (고유 식별자)
     * 예: CHEMICAL, ELECTRONICS, MEDICAL, FOOD, DEFAULT
     */
    @Column(name = "theme_code", nullable = false, unique = true, length = 50)
    private String themeCode;

    /**
     * 테마명
     */
    @Column(name = "theme_name", nullable = false, length = 100)
    private String themeName;

    /**
     * 산업 유형
     * CHEMICAL: 화학 제조
     * ELECTRONICS: 전자/전기 제조
     * MEDICAL: 의료기기 제조
     * FOOD: 식품/음료 제조
     * GENERAL: 일반 제조
     */
    @Column(name = "industry_type", length = 50)
    private String industryType;

    /**
     * 테마 설명
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 기본 테마 여부
     */
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * 활성화 상태
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    /**
     * 색상 설정 (JSON)
     * {
     *   "primary": "#1976d2",
     *   "secondary": "#dc004e",
     *   "success": "#4caf50",
     *   "warning": "#ff9800",
     *   "error": "#f44336",
     *   "info": "#2196f3",
     *   "background": "#fafafa",
     *   "surface": "#ffffff",
     *   "text": {
     *     "primary": "#000000",
     *     "secondary": "#666666"
     *   }
     * }
     */
    @Type(type = "jsonb")
    @Column(name = "color_scheme", columnDefinition = "jsonb")
    private Map<String, Object> colorScheme;

    /**
     * 타이포그래피 설정 (JSON)
     * {
     *   "fontFamily": "Roboto, sans-serif",
     *   "fontSize": {
     *     "small": "12px",
     *     "medium": "14px",
     *     "large": "16px",
     *     "h1": "32px",
     *     "h2": "28px"
     *   },
     *   "fontWeight": {
     *     "light": 300,
     *     "regular": 400,
     *     "medium": 500,
     *     "bold": 700
     *   }
     * }
     */
    @Type(type = "jsonb")
    @Column(name = "typography", columnDefinition = "jsonb")
    private Map<String, Object> typography;

    /**
     * 레이아웃 설정 (JSON)
     * {
     *   "sidebarWidth": 240,
     *   "headerHeight": 64,
     *   "spacing": 8,
     *   "borderRadius": 4,
     *   "defaultDashboard": "production"
     * }
     */
    @Type(type = "jsonb")
    @Column(name = "layout", columnDefinition = "jsonb")
    private Map<String, Object> layout;

    /**
     * 컴포넌트 스타일 (JSON)
     * {
     *   "button": {
     *     "borderRadius": 4,
     *     "textTransform": "none"
     *   },
     *   "card": {
     *     "elevation": 2
     *   },
     *   "table": {
     *     "stripe": true
     *   }
     * }
     */
    @Type(type = "jsonb")
    @Column(name = "components", columnDefinition = "jsonb")
    private Map<String, Object> components;

    /**
     * 산업별 모듈 활성화 (JSON)
     * {
     *   "qualityControl": true,
     *   "lotTracking": true,
     *   "chemicalInventory": true,
     *   "safetyCompliance": true
     * }
     */
    @Type(type = "jsonb")
    @Column(name = "enabled_modules", columnDefinition = "jsonb")
    private Map<String, Object> enabledModules;

    /**
     * 추가 설정 (JSON)
     * 로고 URL, 파비콘, 기타 커스텀 설정
     */
    @Type(type = "jsonb")
    @Column(name = "additional_config", columnDefinition = "jsonb")
    private Map<String, Object> additionalConfig;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
