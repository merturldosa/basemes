package kr.co.softice.mes.common.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Response DTO
 * 재고 현황 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long inventoryId;
    private String tenantId;
    private String tenantName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private String productCode;
    private String productName;
    private Long lotId;
    private String lotNo;
    private BigDecimal availableQuantity;
    private BigDecimal reservedQuantity;
    private String unit;
    private String location;
    private LocalDateTime lastTransactionDate;
    private String lastTransactionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
