package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Consumable Create Request DTO
 * 소모품 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableCreateRequest {

    @NotBlank(message = "소모품 코드는 필수입니다.")
    private String consumableCode;

    @NotBlank(message = "소모품명은 필수입니다.")
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
