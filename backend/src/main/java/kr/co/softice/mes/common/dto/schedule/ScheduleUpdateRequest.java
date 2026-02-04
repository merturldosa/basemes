package kr.co.softice.mes.common.dto.schedule;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Schedule Update Request DTO
 * 생산 일정 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleUpdateRequest {

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private Integer plannedDuration;

    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    private Long assignedEquipmentId;
    private Integer assignedWorkers;
    private Long assignedUserId;

    private String status;

    @Min(value = 0, message = "Progress rate must be between 0 and 100")
    @Max(value = 100, message = "Progress rate must be between 0 and 100")
    private BigDecimal progressRate;

    private String delayReason;
    private String remarks;
}
