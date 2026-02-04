package kr.co.softice.mes.common.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Shipping Item Response DTO
 * 출하 상세 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingItemResponse {

    private Long shippingItemId;
    private Long shippingId;

    // Sales Order Item
    private Long salesOrderItemId;

    // Product
    private Long productId;
    private String productCode;
    private String productName;

    // Quantities
    private BigDecimal orderedQuantity;
    private BigDecimal shippedQuantity;

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
