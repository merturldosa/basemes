package kr.co.softice.mes.common.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Transaction Response DTO
 * 재고 이동 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {

    private Long transactionId;
    private String tenantId;
    private String tenantName;
    private String transactionNo;
    private String transactionType;
    private LocalDateTime transactionDate;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private String productCode;
    private String productName;
    private Long lotId;
    private String lotNo;
    private BigDecimal quantity;
    private String unit;
    private Long fromWarehouseId;
    private String fromWarehouseCode;
    private String fromWarehouseName;
    private Long toWarehouseId;
    private String toWarehouseCode;
    private String toWarehouseName;
    private Long workOrderId;
    private String workOrderNo;
    private Long qualityInspectionId;
    private String inspectionNo;
    private Long transactionUserId;
    private String transactionUserName;
    private String approvalStatus;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedDate;
    private String referenceNo;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
