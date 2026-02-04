package kr.co.softice.mes.common.dto.defect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Defect Response DTO
 * 불량 응답
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectResponse {

    private Long defectId;
    private String tenantId;
    private String tenantName;
    private String defectNo;
    private LocalDateTime defectDate;

    // Source
    private String sourceType;
    private Long workOrderId;
    private String workOrderNo;
    private Long workResultId;
    private Long goodsReceiptId;
    private String goodsReceiptNo;
    private Long shippingId;
    private String shippingNo;
    private Long qualityInspectionId;
    private String qualityInspectionNo;

    // Product
    private Long productId;
    private String productCode;
    private String productName;

    // Defect Details
    private String defectType;
    private String defectCategory;
    private String defectLocation;
    private String defectDescription;
    private BigDecimal defectQuantity;
    private String lotNo;
    private String severity;

    // Status & Action
    private String status;
    private Long responsibleDepartmentId;
    private String responsibleDepartmentName;
    private Long responsibleUserId;
    private String responsibleUserName;
    private String rootCause;
    private String correctiveAction;
    private String preventiveAction;
    private LocalDateTime actionDate;

    // Reporter
    private Long reporterUserId;
    private String reporterName;

    // Cost
    private BigDecimal defectCost;

    // Additional
    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
