package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Shipping Response DTO
 * 출하 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingResponse {

    // Header
    private Long shippingId;
    private String tenantId;
    private String tenantName;
    private String shippingNo;
    private LocalDateTime shippingDate;
    private String shippingType;  // SALES, RETURN, TRANSFER, OTHER
    private String shippingStatus;  // PENDING, INSPECTING, SHIPPED, CANCELLED

    // Sales Order (Optional)
    private Long salesOrderId;
    private String salesOrderNo;

    // Customer
    private Long customerId;
    private String customerCode;
    private String customerName;

    // Warehouse
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    // Shipper
    private Long shipperUserId;
    private String shipperUserName;
    private String shipperName;

    // Delivery Information
    private String deliveryAddress;
    private String trackingNumber;
    private String carrierName;

    // Totals
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;

    // Items
    private List<ShippingItemResponse> items;

    // Additional
    private String remarks;
    private Boolean isActive;

    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
