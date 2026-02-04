package kr.co.softice.mes.common.dto.sales;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Delivery Item Request DTO
 * 출하 상세 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryItemRequest {

    @NotNull(message = "Line number is required")
    private Integer lineNo;

    @NotNull(message = "Sales order item is required")
    private Long salesOrderItemId;

    // Product or Material (one of them required)
    private Long productId;
    private Long materialId;

    @NotNull(message = "Delivered quantity is required")
    private BigDecimal deliveredQuantity;

    @NotNull(message = "Unit is required")
    private String unit;

    // LOT
    private Long lotId;

    // Location
    private String location;

    // Additional
    private String remarks;
}
