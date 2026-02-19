package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inspection Action Response DTO
 * 점검 조치 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionActionResponse {

    private Long actionId;
    private String tenantId;
    private String tenantName;

    private Long inspectionId;
    private String inspectionNo;

    private String actionType;
    private String description;

    private Long assignedUserId;
    private String assignedUserName;

    private LocalDate dueDate;
    private LocalDate completedDate;

    private String status;
    private String result;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
