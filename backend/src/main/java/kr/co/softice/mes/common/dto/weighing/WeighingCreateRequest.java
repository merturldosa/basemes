package kr.co.softice.mes.common.dto.weighing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Weighing Create Request DTO
 * 칭량 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeighingCreateRequest {

    private String weighingNo;  // Optional: 자동 생성 가능 (WG-YYYYMMDD-0001)

    @NotNull(message = "Weighing date is required")
    private LocalDateTime weighingDate;

    @NotBlank(message = "Weighing type is required")
    private String weighingType;  // INCOMING, OUTGOING, PRODUCTION, SAMPLING

    private String referenceType;  // MATERIAL_REQUEST, WORK_ORDER, GOODS_RECEIPT, SHIPPING, QUALITY_INSPECTION
    private Long referenceId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long lotId;  // Optional: Lot tracking

    @NotNull(message = "Tare weight is required")
    @DecimalMin(value = "0.0", message = "Tare weight must be non-negative")
    private BigDecimal tareWeight;

    @NotNull(message = "Gross weight is required")
    @DecimalMin(value = "0.0", message = "Gross weight must be non-negative")
    private BigDecimal grossWeight;

    // Net weight will be calculated automatically (gross - tare)
    // No need to provide from client

    private BigDecimal expectedWeight;  // Optional: For variance calculation

    @NotBlank(message = "Unit is required")
    @Builder.Default
    private String unit = "kg";

    private Long scaleId;  // Optional: Equipment tracking
    private String scaleName;

    @NotNull(message = "Operator user ID is required")
    private Long operatorUserId;

    @Builder.Default
    private BigDecimal tolerancePercentage = new BigDecimal("2.0");  // Default 2%

    private String remarks;
    private String attachments;  // JSON string for file attachments

    // Environmental conditions (GMP)
    private BigDecimal temperature;
    private BigDecimal humidity;
}
