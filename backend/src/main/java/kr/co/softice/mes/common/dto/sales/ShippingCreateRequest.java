package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Shipping Create Request DTO
 * 출하 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingCreateRequest {

    private String shippingNo;  // Optional: 자동 생성 가능 (SH-YYYYMMDD-0001)

    @NotNull(message = "Shipping date is required")
    private LocalDateTime shippingDate;

    private Long salesOrderId;  // Optional: Link to sales order

    private Long customerId;  // Optional: Customer

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotBlank(message = "Shipping type is required")
    @Builder.Default
    private String shippingType = "SALES";  // SALES, RETURN, TRANSFER, OTHER

    private Long shipperUserId;

    private String deliveryAddress;

    private String trackingNumber;

    private String carrierName;

    private String remarks;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ShippingItemRequest> items;
}
