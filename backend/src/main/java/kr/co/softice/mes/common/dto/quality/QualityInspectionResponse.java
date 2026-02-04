package kr.co.softice.mes.common.dto.quality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Quality Inspection Response DTO
 * 품질 검사 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionResponse {

    private Long qualityInspectionId;

    // Quality Standard Information
    private Long qualityStandardId;
    private String standardCode;
    private String standardName;

    // Optional Work References
    private Long workOrderId;
    private String workOrderNo;
    private Long workResultId;

    // Product Information
    private Long productId;
    private String productCode;
    private String productName;

    // Inspection Identification
    private String inspectionNo;
    private LocalDateTime inspectionDate;
    private String inspectionType;  // INCOMING, IN_PROCESS, OUTGOING, FINAL

    // Inspector Information
    private Long inspectorUserId;
    private String inspectorUsername;
    private String inspectorName;

    // Quantities
    private BigDecimal inspectedQuantity;
    private BigDecimal passedQuantity;
    private BigDecimal failedQuantity;

    // Measurement
    private BigDecimal measuredValue;
    private String measurementUnit;

    // Result
    private String inspectionResult;  // PASS, FAIL, CONDITIONAL

    // Defect Information
    private String defectType;
    private String defectReason;
    private String defectLocation;

    // Corrective Action
    private String correctiveAction;
    private LocalDate correctiveActionDate;

    // Additional Information
    private String remarks;

    // Tenant Information
    private String tenantId;
    private String tenantName;

    // Audit Information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
