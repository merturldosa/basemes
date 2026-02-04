package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shipping Item Response DTO
 * 출하 항목 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingItemResponse {

    // Item
    private Long shippingItemId;

    // Sales Order Item (Optional)
    private Long salesOrderItemId;

    // Product
    private Long productId;
    private String productCode;
    private String productName;

    // Quantities
    private BigDecimal orderedQuantity;
    private BigDecimal shippedQuantity;

    // Pricing
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;

    // Lot/Batch
    private String lotNo;
    private LocalDate expiryDate;

    // Quality Inspection
    private String inspectionStatus;  // NOT_REQUIRED, PENDING, PASS, FAIL
    private Long qualityInspectionId;
    private String inspectionResult;

    // Additional
    private String remarks;
}
