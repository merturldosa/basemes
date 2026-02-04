package kr.co.softice.mes.common.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Purchase Order Item Response DTO
 * 구매 주문 항목 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponse {

    private Long purchaseOrderItemId;
    private Integer lineNo;

    // Material
    private Long materialId;
    private String materialCode;
    private String materialName;

    // Quantity
    private BigDecimal orderedQuantity;
    private BigDecimal receivedQuantity;
    private String unit;

    // Price
    private BigDecimal unitPrice;
    private BigDecimal amount;

    // Delivery
    private LocalDateTime requiredDate;

    // Reference
    private Long purchaseRequestId;
    private String purchaseRequestNo;

    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
