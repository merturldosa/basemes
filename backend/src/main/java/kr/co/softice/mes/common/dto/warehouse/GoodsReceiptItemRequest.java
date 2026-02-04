package kr.co.softice.mes.common.dto.warehouse;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Goods Receipt Item Request DTO
 * 입하 상세 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemRequest {

    private Long purchaseOrderItemId;

    @NotNull(message = "Product is required")
    private Long productId;

    private String productCode;
    private String productName;
    private BigDecimal orderedQuantity;

    @NotNull(message = "Received quantity is required")
    private BigDecimal receivedQuantity;

    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private String lotNo;
    private LocalDate expiryDate;
    private String inspectionStatus;
    private Long qualityInspectionId;
    private String remarks;
}
