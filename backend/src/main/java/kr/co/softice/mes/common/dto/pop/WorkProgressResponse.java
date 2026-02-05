package kr.co.softice.mes.common.dto.pop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Work Progress Response DTO
 * 작업 진행 응답
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkProgressResponse {

    private Long progressId;
    private String tenantId;

    // Work Order
    private Long workOrderId;
    private String workOrderNo;
    private String productName;
    private String productCode;
    private String processName;

    // Operator
    private Long operatorUserId;
    private String operatorUserName;

    // Work Date and Time
    private LocalDate recordDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Production Quantities
    private BigDecimal producedQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;
    private BigDecimal plannedQuantity;

    // Calculated fields
    private Double completionRate; // (producedQuantity / plannedQuantity) * 100
    private Double defectRate; // (defectQuantity / producedQuantity) * 100

    // Work Status
    private String status; // IN_PROGRESS, PAUSED, COMPLETED

    // Pause Statistics
    private Integer pauseCount;
    private Integer totalPauseDuration; // in minutes

    // Work Notes
    private String workNotes;

    // Active Status
    private Boolean isActive;

    // Equipment
    private Long equipmentId;
    private String equipmentName;

    // Audit
    private String createdAt;
    private String updatedAt;
}
