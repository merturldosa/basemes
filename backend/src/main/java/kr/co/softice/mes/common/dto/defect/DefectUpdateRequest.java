package kr.co.softice.mes.common.dto.defect;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Defect Update Request DTO
 * 불량 수정 요청
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectUpdateRequest {

    @Size(max = 50, message = "불량 유형은 50자 이하여야 합니다")
    private String defectType;

    @Size(max = 50, message = "불량 분류는 50자 이하여야 합니다")
    private String defectCategory;

    @Size(max = 200, message = "불량 위치는 200자 이하여야 합니다")
    private String defectLocation;

    private String defectDescription;

    private BigDecimal defectQuantity;

    @Size(max = 30, message = "심각도는 30자 이하여야 합니다")
    private String severity;

    @Size(max = 30, message = "상태는 30자 이하여야 합니다")
    private String status;

    private String rootCause;
    private String correctiveAction;
    private String preventiveAction;

    private BigDecimal defectCost;

    private String remarks;
}
