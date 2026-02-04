package kr.co.softice.mes.common.dto.weighing;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Weighing Response DTO
 * Response data containing complete weighing information
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeighingResponse {

    /**
     * Weighing ID
     */
    private Long weighingId;

    /**
     * Tenant ID
     */
    private String tenantId;

    /**
     * Weighing number (auto-generated)
     * Format: WG-YYYYMMDD-0001
     */
    private String weighingNo;

    /**
     * Weighing date/time
     */
    private LocalDateTime weighingDate;

    /**
     * Weighing type
     * INCOMING, OUTGOING, PRODUCTION, SAMPLING
     */
    private String weighingType;

    /**
     * Reference type (polymorphic)
     * MATERIAL_REQUEST, WORK_ORDER, GOODS_RECEIPT, SHIPPING, QUALITY_INSPECTION
     */
    private String referenceType;

    /**
     * Reference ID
     */
    private Long referenceId;

    /**
     * Product ID
     */
    private Long productId;

    /**
     * Product code
     */
    private String productCode;

    /**
     * Product name
     */
    private String productName;

    /**
     * Lot ID
     */
    private Long lotId;

    /**
     * Lot number
     */
    private String lotNo;

    /**
     * Tare weight (container weight)
     */
    private BigDecimal tareWeight;

    /**
     * Gross weight (total weight)
     */
    private BigDecimal grossWeight;

    /**
     * Net weight (gross - tare)
     */
    private BigDecimal netWeight;

    /**
     * Expected weight
     */
    private BigDecimal expectedWeight;

    /**
     * Variance (net - expected)
     */
    private BigDecimal variance;

    /**
     * Variance percentage
     */
    private BigDecimal variancePercentage;

    /**
     * Unit of measurement
     */
    private String unit;

    /**
     * Scale ID
     */
    private Long scaleId;

    /**
     * Scale name
     */
    private String scaleName;

    /**
     * Operator user ID
     */
    private Long operatorUserId;

    /**
     * Operator username
     */
    private String operatorUsername;

    /**
     * Operator name
     */
    private String operatorName;

    /**
     * Verifier user ID
     */
    private Long verifierUserId;

    /**
     * Verifier username
     */
    private String verifierUsername;

    /**
     * Verifier name
     */
    private String verifierName;

    /**
     * Verification date
     */
    private LocalDateTime verificationDate;

    /**
     * Verification status
     * PENDING, VERIFIED, REJECTED
     */
    private String verificationStatus;

    /**
     * Tolerance exceeded flag
     */
    private Boolean toleranceExceeded;

    /**
     * Tolerance percentage
     */
    private BigDecimal tolerancePercentage;

    /**
     * Remarks
     */
    private String remarks;

    /**
     * Environmental temperature (°C)
     */
    private BigDecimal temperature;

    /**
     * Environmental humidity (%)
     */
    private BigDecimal humidity;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Created by user ID
     */
    private Long createdBy;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Updated by user ID
     */
    private Long updatedBy;
}
