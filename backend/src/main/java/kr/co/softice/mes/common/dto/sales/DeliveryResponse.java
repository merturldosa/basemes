package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Delivery Response DTO
 * 출하 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {

    private Long deliveryId;
    private String tenantId;
    private String deliveryNo;
    private LocalDateTime deliveryDate;

    // Sales Order
    private Long salesOrderId;
    private String salesOrderNo;

    // Customer (from Sales Order)
    private Long customerId;
    private String customerName;

    // Warehouse
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    // Quality Check
    private String qualityCheckStatus;
    private Long inspectorUserId;
    private String inspectorName;
    private LocalDateTime inspectionDate;

    // Shipment
    private String shippingMethod;
    private String trackingNo;
    private String carrier;

    // Status
    private String status;

    // User
    private Long shipperUserId;
    private String shipperName;

    // Additional
    private String remarks;

    // Items
    private List<DeliveryItemResponse> items;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
