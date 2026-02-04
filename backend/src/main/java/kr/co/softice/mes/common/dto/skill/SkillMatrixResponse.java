package kr.co.softice.mes.common.dto.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Skill Matrix Response DTO
 * 스킬 매트릭스 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixResponse {

    private Long skillId;
    private String tenantId;
    private String tenantName;
    private String skillCode;
    private String skillName;
    private String skillCategory;
    private String skillLevelDefinition;
    private String description;
    private Boolean certificationRequired;
    private String certificationName;
    private Integer validityPeriodMonths;
    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
