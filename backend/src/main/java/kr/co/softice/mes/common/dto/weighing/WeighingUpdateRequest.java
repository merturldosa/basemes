package kr.co.softice.mes.common.dto.weighing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Weighing Update Request DTO
 * 칭량 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeighingUpdateRequest {

    private LocalDateTime weighingDate;
    private String weighingType;  // INCOMING, OUTGOING, PRODUCTION, SAMPLING

    private String referenceType;
    private Long referenceId;

    private Long productId;
    private Long lotId;

    @DecimalMin(value = "0.0", message = "Tare weight must be non-negative")
    private BigDecimal tareWeight;

    @DecimalMin(value = "0.0", message = "Gross weight must be non-negative")
    private BigDecimal grossWeight;

    private BigDecimal expectedWeight;
    private String unit;

    private Long scaleId;
    private String scaleName;

    private BigDecimal tolerancePercentage;

    private String remarks;
    private String attachments;

    private BigDecimal temperature;
    private BigDecimal humidity;
}
