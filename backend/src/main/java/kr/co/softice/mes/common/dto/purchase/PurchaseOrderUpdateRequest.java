package kr.co.softice.mes.common.dto.purchase;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Purchase Order Update Request DTO
 * 구매 주문 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderUpdateRequest {

    private LocalDateTime expectedDeliveryDate;

    private String deliveryAddress;

    private String paymentTerms;

    private String currency;

    private String remarks;

    @Valid
    private List<PurchaseOrderItemRequest> items;
}
