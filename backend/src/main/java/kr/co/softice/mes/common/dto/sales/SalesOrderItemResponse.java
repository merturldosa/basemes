package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sales Order Item Response DTO
 * 판매 주문 상세 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItemResponse {

    private Long salesOrderItemId;
    private Integer lineNo;

    // Product or Material
    private Long productId;
    private String productCode;
    private String productName;
    private Long materialId;
    private String materialCode;
    private String materialName;

    // Quantity
    private BigDecimal orderedQuantity;
    private BigDecimal deliveredQuantity;
    private String unit;

    // Price
    private BigDecimal unitPrice;
    private BigDecimal amount;

    // Delivery
    private LocalDateTime requestedDate;

    // Additional
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
