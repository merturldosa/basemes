package kr.co.softice.mes.common.dto.weighing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Weighing Verification Request DTO
 * 칭량 검증 요청 DTO (GMP 이중 검증)
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeighingVerificationRequest {

    @NotNull(message = "Verifier user ID is required")
    private Long verifierUserId;

    @NotBlank(message = "Action is required")
    private String action;  // VERIFY, REJECT

    private String remarks;  // Verification notes or rejection reason
}
