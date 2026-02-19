package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Breakdown Response DTO
 * 고장 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakdownResponse {

    private Long breakdownId;
    private String tenantId;
    private String tenantName;
    private String breakdownNo;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private Long downtimeId;

    private LocalDateTime reportedAt;
    private Long reportedByUserId;
    private String reportedByUserName;

    private String failureType;
    private String severity;
    private String description;

    private Long assignedUserId;
    private String assignedUserName;
    private LocalDateTime assignedAt;

    private LocalDateTime repairStartedAt;
    private LocalDateTime repairCompletedAt;
    private Integer repairDurationMinutes;
    private String repairDescription;
    private String partsUsed;
    private BigDecimal repairCost;

    private String rootCause;
    private String preventiveAction;

    private String status;
    private LocalDateTime closedAt;
    private Long closedByUserId;
    private String closedByUserName;

    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
