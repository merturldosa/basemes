package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    private Long purchaseOrderItemId;  // Optional: 구매 주문 항목 ID

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Received quantity is required")
    @Positive(message = "Received quantity must be positive")
    private BigDecimal receivedQuantity;

    private String lotNo;  // LOT 번호 (자동 생성 또는 수동 입력)

    private LocalDate expiryDate;  // 유효기한

    @Builder.Default
    private String inspectionStatus = "NOT_REQUIRED";  // NOT_REQUIRED, PENDING, PASS, FAIL

    private String remarks;
}
