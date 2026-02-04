package kr.co.softice.mes.common.dto.skill;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employee Skill Create Request DTO
 * 사원 스킬 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillCreateRequest {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long employeeId;

    @NotNull(message = "스킬 ID는 필수입니다.")
    private Long skillId;

    private String skillLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT, MASTER
    private Integer skillLevelNumeric; // 1~5

    private LocalDate acquisitionDate;
    private LocalDate expiryDate;
    private LocalDate lastAssessmentDate;
    private LocalDate nextAssessmentDate;

    private String certificationNo;
    private String issuingAuthority;

    private String assessorName;
    private BigDecimal assessmentScore;
    private String assessmentResult; // PASS, FAIL, CONDITIONAL

    private String remarks;
}
