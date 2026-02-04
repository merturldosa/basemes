package kr.co.softice.mes.common.dto.inventory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Transaction Create Request DTO
 * 재고 이동 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionCreateRequest {

    @NotBlank(message = "Transaction number is required")
    private String transactionNo;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;  // IN_RECEIVE, IN_PRODUCTION, IN_RETURN, OUT_ISSUE, OUT_SCRAP, MOVE, ADJUST

    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long lotId;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    // For MOVE transactions
    private Long fromWarehouseId;
    private Long toWarehouseId;

    // Optional references
    private Long workOrderId;
    private Long qualityInspectionId;

    @NotNull(message = "Transaction user ID is required")
    private Long transactionUserId;

    private String referenceNo;

    private String remarks;
}
