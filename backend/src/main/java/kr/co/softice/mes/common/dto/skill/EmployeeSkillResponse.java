package kr.co.softice.mes.common.dto.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee Skill Response DTO
 * 사원 스킬 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillResponse {

    private Long employeeSkillId;
    private String tenantId;
    private String tenantName;

    private Long employeeId;
    private String employeeNo;
    private String employeeName;

    private Long skillId;
    private String skillCode;
    private String skillName;
    private String skillCategory;

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
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
