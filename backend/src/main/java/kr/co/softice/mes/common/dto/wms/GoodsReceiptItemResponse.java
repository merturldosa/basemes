package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Goods Receipt Item Response DTO
 * 입하 항목 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemResponse {

    private Long goodsReceiptItemId;
    private Long purchaseOrderItemId;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal orderedQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private String lotNo;
    private LocalDate expiryDate;
    private String inspectionStatus;  // NOT_REQUIRED, PENDING, PASS, FAIL
    private Long qualityInspectionId;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
