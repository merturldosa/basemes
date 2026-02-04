package kr.co.softice.mes.common.dto.bom;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BOM Detail Request DTO
 * BOM 상세 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomDetailRequest {

    private Integer sequence;

    @NotNull(message = "Material product ID is required")
    private Long materialProductId;

    private Long processId;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    @NotNull(message = "Unit is required")
    private String unit;

    @Builder.Default
    private BigDecimal usageRate = BigDecimal.valueOf(100.00);

    @Builder.Default
    private BigDecimal scrapRate = BigDecimal.ZERO;

    private String remarks;
}
