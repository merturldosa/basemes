package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Material Request Item Response DTO
 * 불출 신청 항목 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestItemResponse {

    private Long materialRequestItemId;

    // Product
    private Long productId;
    private String productCode;
    private String productName;
    private String productType;
    private String unit;

    // Quantities
    private BigDecimal requestedQuantity;
    private BigDecimal approvedQuantity;
    private BigDecimal issuedQuantity;

    // Status
    private String issueStatus;  // PENDING, PARTIAL, COMPLETED, CANCELLED

    // LOT
    private String requestedLotNo;  // 요청한 특정 LOT
    private String issuedLotNo;     // 실제 불출된 LOT

    // Additional
    private String remarks;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
