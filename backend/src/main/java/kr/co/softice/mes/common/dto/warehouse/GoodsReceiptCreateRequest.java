package kr.co.softice.mes.common.dto.warehouse;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @NotBlank(message = "Receipt number is required")
    private String receiptNo;

    @NotNull(message = "Receipt date is required")
    private LocalDateTime receiptDate;

    private Long purchaseOrderId;
    private Long supplierId;

    @NotNull(message = "Warehouse is required")
    private Long warehouseId;

    @NotBlank(message = "Receipt type is required")
    private String receiptType;

    private String receiptStatus;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private Long receiverUserId;
    private String receiverName;
    private String remarks;

    private List<GoodsReceiptItemRequest> items;
}
