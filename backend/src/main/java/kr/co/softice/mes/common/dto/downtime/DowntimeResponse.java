package kr.co.softice.mes.common.dto.downtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Downtime Response DTO
 * 비가동 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DowntimeResponse {

    private Long downtimeId;
    private String tenantId;
    private String tenantName;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private String downtimeCode;

    private String downtimeType;
    private String downtimeCategory;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;

    private Long workOrderId;
    private String workOrderNo;

    private Long operationId;

    private Long responsibleUserId;
    private String responsibleName;

    private String cause;
    private String countermeasure;
    private String preventiveAction;

    private Boolean isResolved;
    private LocalDateTime resolvedAt;

    private String remarks;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
