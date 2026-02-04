package kr.co.softice.mes.common.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private Integer lineNo;

    // Purchase Order Item
    private Long purchaseOrderItemId;

    // Material
    private Long materialId;
    private String materialCode;
    private String materialName;

    // Quantity
    private BigDecimal receivedQuantity;
    private BigDecimal acceptedQuantity;
    private BigDecimal rejectedQuantity;
    private String unit;

    // LOT
    private Long lotId;
    private String lotNo;

    // Location
    private String location;

    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
