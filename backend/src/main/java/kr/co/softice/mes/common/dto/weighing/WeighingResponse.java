package kr.co.softice.mes.common.dto.weighing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Weighing Response DTO
 * 칭량 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeighingResponse {

    // Header
    private Long weighingId;
    private String tenantId;
    private String tenantName;
    private String weighingNo;
    private LocalDateTime weighingDate;
    private String weighingType;  // INCOMING, OUTGOING, PRODUCTION, SAMPLING

    // Reference
    private String referenceType;
    private Long referenceId;
    private String referenceNo;  // For display purposes

    // Product/Material
    private Long productId;
    private String productCode;
    private String productName;

    // Lot
    private Long lotId;
    private String lotNo;

    // Weight Measurements
    private BigDecimal tareWeight;
    private BigDecimal grossWeight;
    private BigDecimal netWeight;
    private BigDecimal expectedWeight;
    private BigDecimal variance;
    private BigDecimal variancePercentage;
    private String unit;

    // Equipment
    private Long scaleId;
    private String scaleName;

    // Personnel (GMP Dual Verification)
    private Long operatorUserId;
    private String operatorUserName;
    private String operatorName;

    private Long verifierUserId;
    private String verifierUserName;
    private String verifierName;
    private LocalDateTime verificationDate;
    private String verificationStatus;  // PENDING, VERIFIED, REJECTED

    // Tolerance Control
    private Boolean toleranceExceeded;
    private BigDecimal tolerancePercentage;

    // Additional
    private String remarks;
    private String attachments;

    // Environmental Conditions
    private BigDecimal temperature;
    private BigDecimal humidity;

    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
