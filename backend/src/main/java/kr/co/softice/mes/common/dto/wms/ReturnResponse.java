package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Return Response DTO
 * 반품 응답 DTO
 *
 * 헤더 정보 + 신청자 + 창고 + 승인자 + 항목 리스트
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {

    // Header
    private Long returnId;
    private String tenantId;
    private String tenantName;
    private String returnNo;
    private LocalDateTime returnDate;
    private String returnType;   // DEFECTIVE, EXCESS, WRONG_DELIVERY, OTHER
    private String returnStatus; // PENDING, APPROVED, REJECTED, RECEIVED, INSPECTING, COMPLETED, CANCELLED

    // References
    private Long materialRequestId;
    private String materialRequestNo;
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
    private LocalDateTime receivedDate;
    private LocalDateTime completedDate;

    // Totals
    private BigDecimal totalReturnQuantity;
    private BigDecimal totalReceivedQuantity;
    private BigDecimal totalPassedQuantity;
    private BigDecimal totalFailedQuantity;

    // Items
    private List<ReturnItemResponse> items;

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
