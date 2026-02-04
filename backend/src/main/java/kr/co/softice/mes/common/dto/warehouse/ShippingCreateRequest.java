package kr.co.softice.mes.common.dto.warehouse;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @NotBlank(message = "Shipping number is required")
    private String shippingNo;

    @NotNull(message = "Shipping date is required")
    private LocalDateTime shippingDate;

    private Long salesOrderId;
    private Long customerId;

    @NotNull(message = "Warehouse is required")
    private Long warehouseId;

    @NotBlank(message = "Shipping type is required")
    private String shippingType;

    private String shippingStatus;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private Long shipperUserId;
    private String shipperName;
    private String deliveryAddress;
    private String trackingNumber;
    private String carrierName;
    private String remarks;

    private List<ShippingItemRequest> items;
}
