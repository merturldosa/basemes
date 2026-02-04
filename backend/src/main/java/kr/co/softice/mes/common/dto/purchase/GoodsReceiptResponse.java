package kr.co.softice.mes.common.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Goods Receipt Response DTO
 * 입하 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptResponse {

    private Long goodsReceiptId;
    private String tenantId;
    private String tenantName;
    private String receiptNo;
    private LocalDateTime receiptDate;

    // Purchase Order
    private Long purchaseOrderId;
    private String purchaseOrderNo;
    private Long supplierId;
    private String supplierName;

    // Warehouse
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    // Inspection
    private String inspectionStatus;
    private Long inspectorUserId;
    private String inspectorUsername;
    private String inspectorFullName;
    private LocalDateTime inspectionDate;

    // Status
    private String status;

    // Receiver
    private Long receiverUserId;
    private String receiverUsername;
    private String receiverFullName;

    private String remarks;

    // Items
    private List<GoodsReceiptItemResponse> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
