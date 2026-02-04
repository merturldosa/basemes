package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * LOT 선택 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotSelectionRequest {

    /**
     * 창고 ID
     */
    @NotNull(message = "창고 ID는 필수입니다")
    private Long warehouseId;

    /**
     * 제품 ID
     */
    @NotNull(message = "제품 ID는 필수입니다")
    private Long productId;

    /**
     * 필요 수량
     */
    @NotNull(message = "필요 수량은 필수입니다")
    @Positive(message = "필요 수량은 양수여야 합니다")
    private BigDecimal requiredQuantity;

    /**
     * LOT ID (특정 LOT 선택 시)
     */
    private Long lotId;

    /**
     * 작업지시 ID (참조용)
     */
    private Long workOrderId;

    /**
     * 판매주문 ID (참조용)
     */
    private Long salesOrderId;

    /**
     * 비고
     */
    private String remarks;
}
