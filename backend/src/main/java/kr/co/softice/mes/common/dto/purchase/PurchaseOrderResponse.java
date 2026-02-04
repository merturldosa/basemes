package kr.co.softice.mes.common.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Purchase Order Response DTO
 * 구매 주문 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    private Long purchaseOrderId;
    private String tenantId;
    private String tenantName;
    private String orderNo;
    private LocalDateTime orderDate;

    // Supplier
    private Long supplierId;
    private String supplierCode;
    private String supplierName;

    // Delivery
    private LocalDateTime expectedDeliveryDate;
    private String deliveryAddress;

    // Payment
    private String paymentTerms;
    private String currency;

    // Status
    private String status;

    // Totals
    private BigDecimal totalAmount;

    // Buyer
    private Long buyerUserId;
    private String buyerUsername;
    private String buyerFullName;

    private String remarks;

    // Items
    private List<PurchaseOrderItemResponse> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
