package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Equipment Operation Update Request DTO
 * 설비 가동 이력 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentOperationUpdateRequest {

    private LocalDateTime endTime;

    private BigDecimal productionQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;

    private String operationStatus;

    private String stopReason;
    private Integer stopDurationMinutes;

    private BigDecimal cycleTime;

    private String remarks;
}
