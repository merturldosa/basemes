package kr.co.softice.mes.common.dto.defect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * After Sales Response DTO
 * A/S 응답
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterSalesResponse {

    private Long afterSalesId;
    private String tenantId;
    private String tenantName;
    private String asNo;
    private LocalDateTime receiptDate;

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
    private String serialNo;
    private String lotNo;

    // Sales Info
    private Long salesOrderId;
    private String salesOrderNo;
    private Long shippingId;
    private LocalDate purchaseDate;
    private String warrantyStatus;

    // Issue Details
    private String issueCategory;
    private String issueDescription;
    private String symptom;

    // Service
    private String serviceType;
    private String serviceStatus;
    private String priority;

    // Assignment
    private Long assignedEngineerId;
    private String assignedEngineerName;
    private LocalDateTime assignedDate;

    // Service Details
    private String diagnosis;
    private String serviceAction;
    private String partsReplaced;
    private LocalDateTime serviceStartDate;
    private LocalDateTime serviceEndDate;

    // Cost
    private BigDecimal serviceCost;
    private BigDecimal partsCost;
    private BigDecimal totalCost;
    private BigDecimal chargeToCustomer;

    // Resolution
    private String resolutionDescription;
    private String customerSatisfaction;

    // Additional
    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
