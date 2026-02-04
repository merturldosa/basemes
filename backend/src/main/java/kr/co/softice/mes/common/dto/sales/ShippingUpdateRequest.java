package kr.co.softice.mes.common.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Shipping Update Request DTO
 * 출하 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingUpdateRequest {

    private LocalDateTime shippingDate;

    private Long customerId;

    private Long warehouseId;

    private String shippingType;  // SALES, RETURN, TRANSFER, OTHER

    private Long shipperUserId;

    private String deliveryAddress;

    private String trackingNumber;

    private String carrierName;

    private String remarks;

    @Valid
    private List<ShippingItemRequest> items;
}
