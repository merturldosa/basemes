package kr.co.softice.mes.common.dto.defect;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Claim Update Request DTO
 * 클레임 수정 요청
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimUpdateRequest {

    @Size(max = 50, message = "클레임 유형은 50자 이하여야 합니다")
    private String claimType;

    @Size(max = 50, message = "클레임 분류는 50자 이하여야 합니다")
    private String claimCategory;

    private String claimDescription;

    private BigDecimal claimedQuantity;
    private BigDecimal claimedAmount;

    @Size(max = 30, message = "심각도는 30자 이하여야 합니다")
    private String severity;

    @Size(max = 30, message = "우선순위는 30자 이하여야 합니다")
    private String priority;

    @Size(max = 30, message = "상태는 30자 이하여야 합니다")
    private String status;

    private String investigationFindings;
    private String rootCauseAnalysis;

    @Size(max = 50, message = "해결 유형은 50자 이하여야 합니다")
    private String resolutionType; // REPLACEMENT, REFUND, DISCOUNT, REWORK, APOLOGY, NO_ACTION

    private String resolutionDescription;
    private BigDecimal resolutionAmount;

    private String correctiveAction;
    private String preventiveAction;

    @Size(max = 30, message = "고객 수용은 30자 이하여야 합니다")
    private String customerAcceptance; // ACCEPTED, PARTIALLY_ACCEPTED, REJECTED, PENDING

    private String customerFeedback;

    private BigDecimal claimCost;
    private BigDecimal compensationAmount;

    private String remarks;
}
