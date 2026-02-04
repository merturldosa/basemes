package kr.co.softice.mes.common.dto.sales;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Delivery Update Request DTO
 * 출하 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryUpdateRequest {

    @NotNull(message = "Delivery date is required")
    private LocalDateTime deliveryDate;

    // Quality Check
    private String qualityCheckStatus;
    private Long inspectorUserId;
    private LocalDateTime inspectionDate;

    // Shipment
    private String shippingMethod;
    private String trackingNo;
    private String carrier;

    // Additional
    private String remarks;
}
