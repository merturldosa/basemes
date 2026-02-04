package kr.co.softice.mes.common.dto.warehouse;

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

    private Long shippingId;
    private String tenantId;
    private String shippingNo;
    private LocalDateTime shippingDate;

    // Sales Order
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

    // Type & Status
    private String shippingType;
    private String shippingStatus;

    // Totals
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;

    // Shipper
    private Long shipperUserId;
    private String shipperName;

    // Delivery Info
    private String deliveryAddress;
    private String trackingNumber;
    private String carrierName;

    // Additional
    private String remarks;
    private Boolean isActive;

    // Items
    private List<ShippingItemResponse> items;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
