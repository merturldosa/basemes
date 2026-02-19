package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inspection Plan Response DTO
 * 점검 계획 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionPlanResponse {

    private Long planId;
    private String tenantId;
    private String tenantName;

    private String planCode;
    private String planName;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private Long formId;
    private String formName;

    private String inspectionType;
    private Integer cycleDays;

    private Long assignedUserId;
    private String assignedUserName;

    private LocalDate lastExecutionDate;
    private LocalDate nextDueDate;

    private String status;
    private String remarks;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
