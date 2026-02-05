package kr.co.softice.mes.common.dto.pop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Defect Record Request DTO
 * 불량 기록 요청
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectRecordRequest {

    @NotNull(message = "Progress ID is required")
    private Long progressId;

    @NotNull(message = "Defect quantity is required")
    @Positive(message = "Defect quantity must be positive")
    private BigDecimal defectQuantity;

    @NotBlank(message = "Defect type is required")
    private String defectType; // 외관불량, 치수불량, 기능불량, etc.

    private String defectReason;

    private String defectLocation;

    private String severity; // CRITICAL, MAJOR, MINOR

    private String notes;
}
