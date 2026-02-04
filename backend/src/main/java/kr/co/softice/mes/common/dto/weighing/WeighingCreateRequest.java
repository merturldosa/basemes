package kr.co.softice.mes.common.dto.weighing;

import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Weighing Create Request DTO
 * Request data for creating a new weighing record
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeighingCreateRequest {

    /**
     * Weighing date/time
     * If not provided, current timestamp will be used
     */
    private LocalDateTime weighingDate;

    /**
     * Weighing type
     * Required: INCOMING, OUTGOING, PRODUCTION, SAMPLING
     */
    @NotBlank(message = "Weighing type is required")
    @Pattern(regexp = "INCOMING|OUTGOING|PRODUCTION|SAMPLING",
             message = "Weighing type must be INCOMING, OUTGOING, PRODUCTION, or SAMPLING")
    private String weighingType;

    /**
     * Reference type (polymorphic)
     * Optional: MATERIAL_REQUEST, WORK_ORDER, GOODS_RECEIPT, SHIPPING, QUALITY_INSPECTION
     */
    @Pattern(regexp = "MATERIAL_REQUEST|WORK_ORDER|GOODS_RECEIPT|SHIPPING|QUALITY_INSPECTION",
             message = "Invalid reference type")
    private String referenceType;

    /**
     * Reference ID
     * ID of the referenced document
     */
    private Long referenceId;

    /**
     * Product ID
     * Required: Product being weighed
     */
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    /**
     * Lot ID
     * Optional: Lot/Batch number
     */
    private Long lotId;

    /**
     * Tare weight (container weight)
     * Required: Must be non-negative
     */
    @NotNull(message = "Tare weight is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tare weight must be non-negative")
    private BigDecimal tareWeight;

    /**
     * Gross weight (total weight including container)
     * Required: Must be greater than or equal to tare weight
     */
    @NotNull(message = "Gross weight is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Gross weight must be non-negative")
    private BigDecimal grossWeight;

    /**
     * Expected weight (from source document)
     * Optional: Used for variance calculation
     */
    private BigDecimal expectedWeight;

    /**
     * Unit of measurement
     * Default: kg
     */
    @NotBlank(message = "Unit is required")
    @Builder.Default
    private String unit = "kg";

    /**
     * Scale ID
     * Optional: Equipment used for weighing
     */
    private Long scaleId;

    /**
     * Scale name
     * Optional: Equipment name
     */
    private String scaleName;

    /**
     * Operator user ID
     * Required: User who performed the weighing
     */
    @NotNull(message = "Operator user ID is required")
    @Positive(message = "Operator user ID must be positive")
    private Long operatorUserId;

    /**
     * Tolerance percentage
     * Default: 2.0 (2%)
     * Acceptable variance percentage
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Tolerance percentage must be non-negative")
    @DecimalMax(value = "100.0", inclusive = true, message = "Tolerance percentage cannot exceed 100")
    @Builder.Default
    private BigDecimal tolerancePercentage = new BigDecimal("2.0");

    /**
     * Remarks
     * Optional: Additional notes
     */
    private String remarks;

    /**
     * Environmental temperature (°C)
     * Optional: For GMP compliance
     */
    private BigDecimal temperature;

    /**
     * Environmental humidity (%)
     * Optional: For GMP compliance
     */
    @DecimalMin(value = "0.0", message = "Humidity must be non-negative")
    @DecimalMax(value = "100.0", message = "Humidity cannot exceed 100")
    private BigDecimal humidity;

    /**
     * Validate that gross weight is greater than or equal to tare weight
     */
    @AssertTrue(message = "Gross weight must be greater than or equal to tare weight")
    public boolean isGrossWeightValid() {
        if (grossWeight == null || tareWeight == null) {
            return true; // Let @NotNull handle null validation
        }
        return grossWeight.compareTo(tareWeight) >= 0;
    }
}
