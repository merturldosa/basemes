package kr.co.softice.mes.common.dto.purchase;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Purchase Order Item Request DTO
 * 구매 주문 항목 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemRequest {

    @NotNull(message = "라인 번호는 필수입니다")
    private Integer lineNo;

    @NotNull(message = "자재 ID는 필수입니다")
    private Long materialId;

    @NotNull(message = "주문 수량은 필수입니다")
    private BigDecimal orderedQuantity;

    @NotNull(message = "단위는 필수입니다")
    private String unit;

    private BigDecimal unitPrice;

    private LocalDateTime requiredDate;

    private Long purchaseRequestId;

    private String remarks;
}
