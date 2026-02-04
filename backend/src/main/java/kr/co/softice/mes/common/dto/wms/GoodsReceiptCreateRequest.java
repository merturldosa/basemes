package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    private String receiptNo;  // Optional: 자동 생성 가능 (GR-YYYYMMDD-0001)

    @NotNull(message = "Receipt date is required")
    private LocalDateTime receiptDate;

    private Long purchaseOrderId;  // Optional: 구매 주문 ID

    private Long supplierId;  // Optional: 공급업체 ID

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotBlank(message = "Receipt type is required")
    private String receiptType;  // PURCHASE, RETURN, TRANSFER, OTHER

    private Long receiverUserId;  // Optional: 수령자 ID

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GoodsReceiptItemRequest> items;

    private String remarks;
}
