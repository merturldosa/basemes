package kr.co.softice.mes.common.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Schedule Response DTO
 * 생산 일정 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private Long scheduleId;
    private String tenantId;
    private String tenantName;

    // WorkOrder 정보
    private Long workOrderId;
    private String workOrderNo;
    private Long productId;
    private String productCode;
    private String productName;

    // Routing Step 정보
    private Long routingStepId;
    private Integer sequenceOrder;
    private Long processId;
    private String processCode;
    private String processName;

    // 계획 일정
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private Integer plannedDuration;  // 분

    // 실제 일정
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Integer actualDuration;  // 분

    // 리소스
    private Long assignedEquipmentId;
    private String assignedEquipmentCode;
    private String assignedEquipmentName;
    private Integer assignedWorkers;
    private Long assignedUserId;
    private String assignedUserName;

    // 상태
    private String status;
    private BigDecimal progressRate;

    // 지연
    private Boolean isDelayed;
    private Integer delayMinutes;
    private String delayReason;

    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
