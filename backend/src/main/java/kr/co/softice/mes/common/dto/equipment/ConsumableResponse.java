package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Consumable Response DTO
 * 소모품 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableResponse {

    private Long consumableId;
    private String tenantId;
    private String tenantName;

    private String consumableCode;
    private String consumableName;
    private String category;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private String unit;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private BigDecimal maximumStock;

    private BigDecimal unitPrice;
    private String supplier;
    private Integer leadTimeDays;
    private LocalDate lastReplenishedDate;

    private String status;
    private String remarks;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
