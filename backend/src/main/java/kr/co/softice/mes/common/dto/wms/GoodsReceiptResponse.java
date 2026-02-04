package kr.co.softice.mes.common.dto.wms;

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
 * 헤더 정보 + 공급업체 + 창고 + 항목 리스트
 * 합계: totalQuantity, totalAmount
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptResponse {

    // Header
    private Long goodsReceiptId;
    private String tenantId;
    private String tenantName;
    private String receiptNo;
    private LocalDateTime receiptDate;
    private String receiptType;  // PURCHASE, RETURN, TRANSFER, OTHER
    private String receiptStatus;  // PENDING, INSPECTING, COMPLETED, REJECTED, CANCELLED

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

    // Receiver
    private Long receiverUserId;
    private String receiverUserName;
    private String receiverName;

    // Totals
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;

    // Items
    private List<GoodsReceiptItemResponse> items;

    // Additional
    private String remarks;
    private Boolean isActive;

    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
