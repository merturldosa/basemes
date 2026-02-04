package kr.co.softice.mes.common.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String receiptNo;
    private LocalDateTime receiptDate;

    // Purchase Order
    private Long purchaseOrderId;
    private String purchaseOrderNo;

    // Supplier
    private Long supplierId;
    private String supplierCode;
    private String supplierName;

    // Warehouse
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    // Type & Status
    private String receiptType;
    private String receiptStatus;

    // Totals
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;

    // Receiver
    private Long receiverUserId;
    private String receiverName;

    // Additional
    private String remarks;
    private Boolean isActive;

    // Items
    private List<GoodsReceiptItemResponse> items;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
