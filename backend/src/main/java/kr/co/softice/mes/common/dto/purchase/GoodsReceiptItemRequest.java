package kr.co.softice.mes.common.dto.purchase;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Goods Receipt Item Request DTO
 * 입하 항목 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemRequest {

    @NotNull(message = "라인 번호는 필수입니다")
    private Integer lineNo;

    @NotNull(message = "구매 주문 항목 ID는 필수입니다")
    private Long purchaseOrderItemId;

    @NotNull(message = "자재 ID는 필수입니다")
    private Long materialId;

    @NotNull(message = "입하 수량은 필수입니다")
    private BigDecimal receivedQuantity;

    private BigDecimal acceptedQuantity;

    private BigDecimal rejectedQuantity;

    @NotNull(message = "단위는 필수입니다")
    private String unit;

    private Long lotId;

    private String location;

    private String remarks;
}
