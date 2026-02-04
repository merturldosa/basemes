package kr.co.softice.mes.common.dto.defect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Claim Response DTO
 * 클레임 응답
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private Long claimId;
    private String tenantId;
    private String tenantName;
    private String claimNo;
    private LocalDateTime claimDate;

    // Customer
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;

    // Product
    private Long productId;
    private String productCode;
    private String productName;
    private String lotNo;

    // Sales Info
    private Long salesOrderId;
    private String salesOrderNo;
    private Long shippingId;

    // Claim Details
    private String claimType;
    private String claimCategory;
    private String claimDescription;
    private BigDecimal claimedQuantity;
    private BigDecimal claimedAmount;

    // Severity & Priority
    private String severity;
    private String priority;

    // Status
    private String status;

    // Assignment
    private Long responsibleDepartmentId;
    private String responsibleDepartmentName;
    private Long responsibleUserId;
    private String responsibleUserName;
    private LocalDateTime assignedDate;

    // Investigation
    private String investigationFindings;
    private String rootCauseAnalysis;

    // Resolution
    private String resolutionType;
    private String resolutionDescription;
    private BigDecimal resolutionAmount;
    private LocalDateTime resolutionDate;

    // Action Plan
    private String correctiveAction;
    private String preventiveAction;
    private LocalDateTime actionCompletionDate;

    // Customer Response
    private String customerAcceptance;
    private String customerFeedback;

    // Cost
    private BigDecimal claimCost;
    private BigDecimal compensationAmount;

    // Additional
    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
