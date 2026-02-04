package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisposalResponse {

    private Long disposalId;
    private String tenantId;
    private String tenantName;
    private String disposalNo;
    private LocalDateTime disposalDate;
    private String disposalType;
    private String disposalStatus;
    private Long workOrderId;
    private String workOrderNo;
    private Long requesterUserId;
    private String requesterUserName;
    private String requesterName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long approverUserId;
    private String approverUserName;
    private String approverName;
    private LocalDateTime approvedDate;
    private Long processorUserId;
    private String processorUserName;
    private String processorName;
    private LocalDateTime processedDate;
    private LocalDateTime completedDate;
    private String disposalMethod;
    private String disposalLocation;
    private BigDecimal totalDisposalQuantity;
    private List<DisposalItemResponse> items;
    private String remarks;
    private String rejectionReason;
    private String cancellationReason;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
