package kr.co.softice.mes.common.dto.purchase;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Purchase Order Create Request DTO
 * 구매 주문 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderCreateRequest {

    @NotBlank(message = "주문 번호는 필수입니다")
    private String orderNo;

    @NotNull(message = "공급업체 ID는 필수입니다")
    private Long supplierId;

    @NotNull(message = "구매 담당자 ID는 필수입니다")
    private Long buyerUserId;

    private LocalDateTime expectedDeliveryDate;

    private String deliveryAddress;

    private String paymentTerms;

    private String currency;

    private String remarks;

    @Valid
    private List<PurchaseOrderItemRequest> items;
}
