package kr.co.softice.mes.common.dto.defect;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * After Sales Update Request DTO
 * A/S 수정 요청
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterSalesUpdateRequest {

    @Size(max = 50, message = "문제 분류는 50자 이하여야 합니다")
    private String issueCategory;

    private String issueDescription;
    private String symptom;

    @Size(max = 30, message = "서비스 유형은 30자 이하여야 합니다")
    private String serviceType;

    @Size(max = 30, message = "서비스 상태는 30자 이하여야 합니다")
    private String serviceStatus;

    @Size(max = 30, message = "우선순위는 30자 이하여야 합니다")
    private String priority;

    private String diagnosis;
    private String serviceAction;
    private String partsReplaced;

    private BigDecimal serviceCost;
    private BigDecimal partsCost;
    private BigDecimal chargeToCustomer;

    private String resolutionDescription;

    @Size(max = 30, message = "고객 만족도는 30자 이하여야 합니다")
    private String customerSatisfaction; // VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED

    private String remarks;
}
