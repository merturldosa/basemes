package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Consumable Update Request DTO
 * 소모품 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableUpdateRequest {

    private String consumableName;
    private String category;
    private Long equipmentId;

    private String unit;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private BigDecimal maximumStock;

    private BigDecimal unitPrice;
    private String supplier;
    private Integer leadTimeDays;

    private String status;
    private String remarks;
}
