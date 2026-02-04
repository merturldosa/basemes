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
import java.time.LocalDateTime;

/**
 * Quality Inspection Create Request DTO
 * 품질 검사 생성 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionCreateRequest {

    @NotNull(message = "품질 기준 ID는 필수입니다")
    private Long qualityStandardId;

    // Optional references
    private Long workOrderId;
    private Long workResultId;

    @NotNull(message = "제품 ID는 필수입니다")
    private Long productId;

    @NotBlank(message = "검사 번호는 필수입니다")
    @Size(max = 50, message = "검사 번호는 50자 이하여야 합니다")
    private String inspectionNo;

    @NotNull(message = "검사 일시는 필수입니다")
    private LocalDateTime inspectionDate;

    @NotBlank(message = "검사 유형은 필수입니다")
    @Size(max = 20, message = "검사 유형은 20자 이하여야 합니다")
    private String inspectionType;  // INCOMING, IN_PROCESS, OUTGOING, FINAL

    @NotNull(message = "검사자 ID는 필수입니다")
    private Long inspectorUserId;

    @NotNull(message = "검사 수량은 필수입니다")
    @Builder.Default
    private BigDecimal inspectedQuantity = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal passedQuantity = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal failedQuantity = BigDecimal.ZERO;

    private BigDecimal measuredValue;

    @Size(max = 20, message = "측정 단위는 20자 이하여야 합니다")
    private String measurementUnit;

    @Size(max = 20, message = "검사 결과는 20자 이하여야 합니다")
    private String inspectionResult;  // PASS, FAIL, CONDITIONAL (Optional - auto-determined if not provided)

    @Size(max = 100, message = "불량 유형은 100자 이하여야 합니다")
    private String defectType;

    @Size(max = 1000, message = "불량 사유는 1000자 이하여야 합니다")
    private String defectReason;

    @Size(max = 200, message = "불량 위치는 200자 이하여야 합니다")
    private String defectLocation;

    @Size(max = 1000, message = "시정 조치는 1000자 이하여야 합니다")
    private String correctiveAction;

    private LocalDate correctiveActionDate;

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
