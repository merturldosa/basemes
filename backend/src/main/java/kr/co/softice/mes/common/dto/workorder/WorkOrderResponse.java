package kr.co.softice.mes.common.dto.workorder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Work Order Response DTO
 * 작업 지시 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderResponse {

    private Long workOrderId;
    private String workOrderNo;
    private String status;

    // Product info
    private Long productId;
    private String productCode;
    private String productName;

    // Process info
    private Long processId;
    private String processCode;
    private String processName;

    // Assigned user info
    private Long assignedUserId;
    private String assignedUserName;

    // Quantities
    private BigDecimal plannedQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;

    // Dates
    private LocalDateTime plannedStartDate;
    private LocalDateTime plannedEndDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;

    private String priority;
    private String tenantId;
    private String tenantName;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
