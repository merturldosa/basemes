package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * 실사 수량 입력 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryCountRequest {

    /**
     * 실사 항목 ID
     */
    @NotNull(message = "실사 항목 ID는 필수입니다")
    private Long itemId;

    /**
     * 실사 수량
     */
    @NotNull(message = "실사 수량은 필수입니다")
    @PositiveOrZero(message = "실사 수량은 0 이상이어야 합니다")
    private BigDecimal countedQuantity;

    /**
     * 실사자 ID
     */
    @NotNull(message = "실사자 ID는 필수입니다")
    private Long countedByUserId;
}
