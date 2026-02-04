package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shipping Item Request DTO
 * 출하 항목 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingItemRequest {

    private Long salesOrderItemId;  // Optional: Link to sales order item

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Shipped quantity is required")
    @DecimalMin(value = "0.001", message = "Shipped quantity must be positive")
    private BigDecimal shippedQuantity;

    private BigDecimal orderedQuantity;  // For reference

    private BigDecimal unitPrice;

    private String lotNo;

    private LocalDate expiryDate;

    private String inspectionStatus;  // NOT_REQUIRED, PENDING, PASS, FAIL

    private Long qualityInspectionId;  // Link to OQC inspection

    private String remarks;
}
