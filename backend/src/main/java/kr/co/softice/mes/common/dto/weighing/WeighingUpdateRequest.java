package kr.co.softice.mes.common.dto.weighing;

import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Weighing Update Request DTO
 * Request data for updating an existing weighing record
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeighingUpdateRequest {

    /**
     * Weighing date/time
     */
    private LocalDateTime weighingDate;

    /**
     * Tare weight (container weight)
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Tare weight must be non-negative")
    private BigDecimal tareWeight;

    /**
     * Gross weight (total weight including container)
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Gross weight must be non-negative")
    private BigDecimal grossWeight;

    /**
     * Expected weight (from source document)
     */
    private BigDecimal expectedWeight;

    /**
     * Scale ID
     */
    private Long scaleId;

    /**
     * Scale name
     */
    private String scaleName;

    /**
     * Tolerance percentage
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Tolerance percentage must be non-negative")
    @DecimalMax(value = "100.0", inclusive = true, message = "Tolerance percentage cannot exceed 100")
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
