package kr.co.softice.mes.common.dto.quality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Quality Standard Response DTO
 * 품질 기준 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityStandardResponse {

    private Long qualityStandardId;

    // Product Information
    private Long productId;
    private String productCode;
    private String productName;

    // Standard Identification
    private String standardCode;
    private String standardName;
    private String standardVersion;

    // Inspection Configuration
    private String inspectionType;  // INCOMING, IN_PROCESS, OUTGOING, FINAL
    private String inspectionMethod;

    // Quality Criteria
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private BigDecimal targetValue;
    private BigDecimal toleranceValue;
    private String unit;

    // Measurement
    private String measurementItem;
    private String measurementEquipment;

    // Sampling
    private String samplingMethod;
    private Integer sampleSize;

    // Status and Validity
    private Boolean isActive;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;

    // Additional Information
    private String remarks;

    // Tenant Information
    private String tenantId;
    private String tenantName;

    // Audit Information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
