package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Material Handover Response DTO
 * 자재 인수인계 응답 DTO
 *
 * 창고에서 생산으로 자재 인수인계 기록
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialHandoverResponse {

    // Header
    private Long materialHandoverId;
    private String tenantId;
    private String tenantName;
    private String handoverNo;
    private LocalDateTime handoverDate;
    private String handoverStatus;  // PENDING, CONFIRMED, REJECTED

    // Material Request Reference
    private Long materialRequestId;
    private String materialRequestNo;
    private Long materialRequestItemId;

    // Inventory Transaction Reference
    private Long inventoryTransactionId;
    private String transactionNo;

    // Product and LOT
    private Long productId;
    private String productCode;
    private String productName;
    private Long lotId;
    private String lotNo;
    private String lotQualityStatus;
    private BigDecimal quantity;
    private String unit;

    // Issuer (출고자 - 창고 담당)
    private Long issuerUserId;
    private String issuerUserName;
    private String issuerName;
    private String issueLocation;

    // Receiver (인수자 - 생산 담당)
    private Long receiverUserId;
    private String receiverUserName;
    private String receiverName;
    private String receiveLocation;
    private LocalDateTime receivedDate;

    // Confirmation
    private String confirmationRemarks;

    // Additional
    private String remarks;

    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
