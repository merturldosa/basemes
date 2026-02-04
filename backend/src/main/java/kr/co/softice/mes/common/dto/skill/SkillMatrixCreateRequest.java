package kr.co.softice.mes.common.dto.skill;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Skill Matrix Create Request DTO
 * 스킬 매트릭스 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixCreateRequest {

    @NotBlank(message = "스킬 코드는 필수입니다.")
    private String skillCode;

    @NotBlank(message = "스킬명은 필수입니다.")
    private String skillName;

    @NotBlank(message = "스킬 분류는 필수입니다.")
    private String skillCategory; // TECHNICAL, OPERATIONAL, QUALITY, SAFETY, MANAGEMENT

    private String skillLevelDefinition;
    private String description;
    private Boolean certificationRequired;
    private String certificationName;
    private Integer validityPeriodMonths;
    private String remarks;
}
