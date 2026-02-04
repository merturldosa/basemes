package kr.co.softice.mes.common.dto.sales;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sales Order Update Request DTO
 * 판매 주문 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderUpdateRequest {

    @NotNull(message = "Order date is required")
    private LocalDateTime orderDate;

    @NotNull(message = "Customer is required")
    private Long customerId;

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
