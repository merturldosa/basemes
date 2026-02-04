package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Equipment Operation Create Request DTO
 * 설비 가동 이력 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentOperationCreateRequest {

    @NotNull(message = "설비 ID는 필수입니다.")
    private Long equipmentId;

    @NotNull(message = "가동 일자는 필수입니다.")
    private LocalDate operationDate;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal operationHours;

    private Long workOrderId;
    private Long workResultId;

    private Long operatorUserId;
    private String operatorName;

    private BigDecimal productionQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;

    private String operationStatus; // RUNNING, STOPPED, PAUSED, COMPLETED

    private String stopReason;
    private Integer stopDurationMinutes;

    private BigDecimal cycleTime;

    private String remarks;
}
