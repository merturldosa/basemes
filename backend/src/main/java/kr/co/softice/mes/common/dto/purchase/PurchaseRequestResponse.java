package kr.co.softice.mes.common.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Purchase Request Response DTO
 * 구매 요청 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestResponse {

    private Long purchaseRequestId;
    private String tenantId;
    private String tenantName;
    private String requestNo;
    private LocalDateTime requestDate;

    // Requester
    private Long requesterUserId;
    private String requesterUsername;
    private String requesterFullName;
    private String department;

    // Material
    private Long materialId;
    private String materialCode;
    private String materialName;
    private BigDecimal requestedQuantity;
    private String unit;
    private LocalDateTime requiredDate;
    private String purpose;

    // Approval
    private String status;
    private Long approverUserId;
    private String approverUsername;
    private String approverFullName;
    private LocalDateTime approvalDate;
    private String approvalComment;

    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
