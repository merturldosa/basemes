package kr.co.softice.mes.common.dto.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Skill Matrix Update Request DTO
 * 스킬 매트릭스 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixUpdateRequest {

    private String skillName;
    private String skillCategory;
    private String skillLevelDefinition;
    private String description;
    private Boolean certificationRequired;
    private String certificationName;
    private Integer validityPeriodMonths;
    private String remarks;
}
