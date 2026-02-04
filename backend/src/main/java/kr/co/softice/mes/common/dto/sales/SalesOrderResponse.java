package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sales Order Response DTO
 * 판매 주문 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponse {

    private Long salesOrderId;
    private String tenantId;
    private String orderNo;
    private LocalDateTime orderDate;

    // Customer
    private Long customerId;
    private String customerCode;
    private String customerName;

    // Delivery
    private LocalDateTime requestedDeliveryDate;
    private String deliveryAddress;

    // Payment
    private String paymentTerms;
    private String currency;

    // Status
    private String status;

    // Totals
    private BigDecimal totalAmount;

    // User
    private Long salesUserId;
    private String salesUserName;

    // Additional
    private String remarks;

    // Items
    private List<SalesOrderItemResponse> items;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
