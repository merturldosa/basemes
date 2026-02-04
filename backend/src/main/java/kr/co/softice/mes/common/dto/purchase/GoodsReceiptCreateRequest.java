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
 * Goods Receipt Create Request DTO
 * 입하 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptCreateRequest {

    @NotBlank(message = "입하 번호는 필수입니다")
    private String receiptNo;

    @NotNull(message = "구매 주문 ID는 필수입니다")
    private Long purchaseOrderId;

    @NotNull(message = "창고 ID는 필수입니다")
    private Long warehouseId;

    @NotNull(message = "입하 담당자 ID는 필수입니다")
    private Long receiverUserId;

    private LocalDateTime receiptDate;

    private String remarks;

    @Valid
    private List<GoodsReceiptItemRequest> items;
}
