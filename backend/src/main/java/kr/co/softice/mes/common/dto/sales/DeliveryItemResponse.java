package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Delivery Item Response DTO
 * 출하 상세 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryItemResponse {

    private Long deliveryItemId;
    private Integer lineNo;

    // Sales Order Item
    private Long salesOrderItemId;

    // Product or Material
    private Long productId;
    private String productCode;
    private String productName;
    private Long materialId;
    private String materialCode;
    private String materialName;

    // Quantity
    private BigDecimal deliveredQuantity;
    private String unit;

    // LOT
    private Long lotId;
    private String lotNo;

    // Location
    private String location;

    // Additional
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
