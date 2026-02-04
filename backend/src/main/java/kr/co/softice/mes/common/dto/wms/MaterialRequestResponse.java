package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Material Request Response DTO
 * 불출 신청 응답 DTO
 *
 * 헤더 정보 + 요청자 + 창고 + 승인자 + 항목 리스트
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestResponse {

    // Header
    private Long materialRequestId;
    private String tenantId;
    private String tenantName;
    private String requestNo;
    private LocalDateTime requestDate;
    private String requestStatus;  // PENDING, APPROVED, REJECTED, ISSUED, COMPLETED, CANCELLED
    private String priority;  // URGENT, HIGH, NORMAL, LOW
    private String purpose;  // PRODUCTION, MAINTENANCE, SAMPLE, OTHER

    // Work Order (Optional)
    private Long workOrderId;
    private String workOrderNo;

    // Requester
    private Long requesterUserId;
    private String requesterUserName;
    private String requesterName;

    // Warehouse
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    // Approver (Optional)
    private Long approverUserId;
    private String approverUserName;
    private String approverName;
    private LocalDateTime approvedDate;

    // Dates
    private LocalDate requiredDate;
    private LocalDateTime issuedDate;
    private LocalDateTime completedDate;

    // Totals
    private BigDecimal totalRequestedQuantity;
    private BigDecimal totalApprovedQuantity;
    private BigDecimal totalIssuedQuantity;

    // Items
    private List<MaterialRequestItemResponse> items;

    // Additional
    private String remarks;
    private String rejectionReason;
    private String cancellationReason;
    private Boolean isActive;

    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
