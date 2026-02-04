package kr.co.softice.mes.common.dto.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employee Skill Update Request DTO
 * 사원 스킬 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillUpdateRequest {

    private String skillLevel;
    private Integer skillLevelNumeric;

    private LocalDate acquisitionDate;
    private LocalDate expiryDate;
    private LocalDate lastAssessmentDate;
    private LocalDate nextAssessmentDate;

    private String certificationNo;
    private String issuingAuthority;

    private String assessorName;
    private BigDecimal assessmentScore;
    private String assessmentResult;

    private String remarks;
}
