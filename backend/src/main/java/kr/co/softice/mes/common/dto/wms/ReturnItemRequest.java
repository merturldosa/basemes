package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Return Item Request DTO
 * 반품 항목 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Return quantity is required")
    @Positive(message = "Return quantity must be positive")
    private BigDecimal returnQuantity;

    private String originalLotNo;  // 원래 불출된 LOT (Optional)

    private String returnReason;   // 반품 사유

    private String remarks;
}
