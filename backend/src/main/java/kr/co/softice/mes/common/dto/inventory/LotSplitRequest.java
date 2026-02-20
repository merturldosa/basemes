package kr.co.softice.mes.common.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * LOT Split Request DTO
 * LOT 분할 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotSplitRequest {

    @NotNull(message = "분할 수량은 필수입니다.")
    @Positive(message = "분할 수량은 양수여야 합니다.")
    private BigDecimal splitQuantity;

    private String remarks;
}
