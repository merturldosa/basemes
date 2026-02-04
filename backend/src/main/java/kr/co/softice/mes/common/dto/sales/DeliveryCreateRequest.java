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
 * Delivery Create Request DTO
 * 출하 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCreateRequest {

    @NotBlank(message = "Delivery number is required")
    private String deliveryNo;

    @NotNull(message = "Delivery date is required")
    private LocalDateTime deliveryDate;

    @NotNull(message = "Sales order is required")
    private Long salesOrderId;

    @NotNull(message = "Warehouse is required")
    private Long warehouseId;

    @NotNull(message = "Shipper is required")
    private Long shipperUserId;

    // Quality Check
    private Long inspectorUserId;

    // Shipment
    private String shippingMethod;
    private String trackingNo;
    private String carrier;

    // Additional
    private String remarks;

    // Items
    private List<DeliveryItemRequest> items;
}
