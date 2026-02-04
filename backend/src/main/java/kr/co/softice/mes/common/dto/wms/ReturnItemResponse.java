package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Return Item Response DTO
 * 반품 항목 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemResponse {

    private Long returnItemId;

    // Product
    private Long productId;
    private String productCode;
    private String productName;
    private String productType;
    private String unit;

    // LOT
    private String originalLotNo;  // 원래 불출된 LOT
    private String newLotNo;       // 재입고 시 생성된 새 LOT

    // Quantities
    private BigDecimal returnQuantity;    // 반품 신청 수량
    private BigDecimal receivedQuantity;  // 실제 입고 수량
    private BigDecimal passedQuantity;    // 합격 수량
    private BigDecimal failedQuantity;    // 불합격 수량

    // Quality Inspection
    private String inspectionStatus;  // NOT_REQUIRED, PENDING, PASS, FAIL
    private Long qualityInspectionId;

    // Inventory Transactions
    private Long receiveTransactionId;
    private Long passTransactionId;
    private Long failTransactionId;

    // Return Reason
    private String returnReason;

    // Additional
    private String remarks;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
