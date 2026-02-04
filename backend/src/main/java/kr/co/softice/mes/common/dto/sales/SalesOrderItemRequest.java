package kr.co.softice.mes.common.dto.sales;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sales Order Item Request DTO
 * 판매 주문 상세 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItemRequest {

    @NotNull(message = "Line number is required")
    private Integer lineNo;

    // Product or Material (one of them required)
    private Long productId;
    private Long materialId;

    @NotNull(message = "Ordered quantity is required")
    private BigDecimal orderedQuantity;

    @NotNull(message = "Unit is required")
    private String unit;

    private BigDecimal unitPrice;
    private LocalDateTime requestedDate;
    private String remarks;
}
