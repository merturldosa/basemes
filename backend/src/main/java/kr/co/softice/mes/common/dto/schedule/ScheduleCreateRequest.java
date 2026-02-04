package kr.co.softice.mes.common.dto.schedule;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Schedule Create Request DTO
 * 생산 일정 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRequest {

    @NotNull(message = "Work Order ID is required")
    private Long workOrderId;

    @NotNull(message = "Routing Step ID is required")
    private Long routingStepId;

    @NotNull(message = "Sequence order is required")
    @Min(value = 1, message = "Sequence order must be at least 1")
    private Integer sequenceOrder;

    @NotNull(message = "Planned start time is required")
    private LocalDateTime plannedStartTime;

    @NotNull(message = "Planned end time is required")
    private LocalDateTime plannedEndTime;

    @NotNull(message = "Planned duration is required")
    @Min(value = 1, message = "Planned duration must be at least 1 minute")
    private Integer plannedDuration;

    private Long assignedEquipmentId;

    @Min(value = 0, message = "Assigned workers must be non-negative")
    @Builder.Default
    private Integer assignedWorkers = 1;

    private Long assignedUserId;

    private String remarks;
}
