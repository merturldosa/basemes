package kr.co.softice.mes.common.dto.sales;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sales Order Create Request DTO
 * 판매 주문 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderCreateRequest {

    @NotBlank(message = "Order number is required")
    private String orderNo;

    @NotNull(message = "Order date is required")
    private LocalDateTime orderDate;

    @NotNull(message = "Customer is required")
    private Long customerId;

    @NotNull(message = "Sales user is required")
    private Long salesUserId;

    // Delivery
    private LocalDateTime requestedDeliveryDate;
    private String deliveryAddress;

    // Payment
    private String paymentTerms;
    private String currency;

    // Additional
    private String remarks;

    // Items
    private List<SalesOrderItemRequest> items;
}
