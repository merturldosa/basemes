package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Equipment Operation Response DTO
 * 설비 가동 이력 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentOperationResponse {

    private Long operationId;
    private String tenantId;
    private String tenantName;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private LocalDate operationDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal operationHours;

    private Long workOrderId;
    private String workOrderNo;

    private Long workResultId;

    private Long operatorUserId;
    private String operatorName;

    private BigDecimal productionQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;

    private String operationStatus;

    private String stopReason;
    private Integer stopDurationMinutes;

    private BigDecimal cycleTime;

    private BigDecimal utilizationRate;
    private BigDecimal performanceRate;
    private BigDecimal qualityRate;
    private BigDecimal oee;

    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
