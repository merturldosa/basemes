package kr.co.softice.mes.common.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Goods Receipt Item Response DTO
 * 입하 상세 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemResponse {

    private Long goodsReceiptItemId;
    private Long goodsReceiptId;

    // Purchase Order Item
    private Long purchaseOrderItemId;

    // Product
    private Long productId;
    private String productCode;
    private String productName;

    // Quantities
    private BigDecimal orderedQuantity;
    private BigDecimal receivedQuantity;

    // Price
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;

    // LOT
    private String lotNo;
    private LocalDate expiryDate;

    // Inspection
    private String inspectionStatus;
    private Long qualityInspectionId;

    // Additional
    private String remarks;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
