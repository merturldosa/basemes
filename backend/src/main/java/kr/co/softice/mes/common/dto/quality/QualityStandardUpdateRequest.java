package kr.co.softice.mes.common.dto.quality;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Quality Standard Update Request DTO
 * 품질 기준 수정 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityStandardUpdateRequest {

    @NotNull(message = "품질 기준 ID는 필수입니다")
    private Long qualityStandardId;

    @NotNull(message = "제품 ID는 필수입니다")
    private Long productId;

    @NotBlank(message = "품질 기준 이름은 필수입니다")
    @Size(max = 200, message = "품질 기준 이름은 200자 이하여야 합니다")
    private String standardName;

    @NotBlank(message = "검사 유형은 필수입니다")
    @Size(max = 20, message = "검사 유형은 20자 이하여야 합니다")
    private String inspectionType;  // INCOMING, IN_PROCESS, OUTGOING, FINAL

    @Size(max = 100, message = "검사 방법은 100자 이하여야 합니다")
    private String inspectionMethod;

    private BigDecimal minValue;
    private BigDecimal maxValue;
    private BigDecimal targetValue;
    private BigDecimal toleranceValue;

    @Size(max = 20, message = "단위는 20자 이하여야 합니다")
    private String unit;

    @Size(max = 200, message = "측정 항목은 200자 이하여야 합니다")
    private String measurementItem;

    @Size(max = 100, message = "측정 장비는 100자 이하여야 합니다")
    private String measurementEquipment;

    @Size(max = 100, message = "샘플링 방법은 100자 이하여야 합니다")
    private String samplingMethod;

    private Integer sampleSize;

    private Boolean isActive;

    @NotNull(message = "유효 시작일은 필수입니다")
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
