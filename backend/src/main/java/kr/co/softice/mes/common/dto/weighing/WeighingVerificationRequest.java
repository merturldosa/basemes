package kr.co.softice.mes.common.dto.weighing;

import lombok.*;

import javax.validation.constraints.*;

/**
 * Weighing Verification Request DTO
 * Request data for verifying or rejecting a weighing record (GMP dual verification)
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeighingVerificationRequest {

    /**
     * Verifier user ID
     * Required: User who is verifying the weighing
     */
    @NotNull(message = "Verifier user ID is required")
    @Positive(message = "Verifier user ID must be positive")
    private Long verifierUserId;

    /**
     * Verification action
     * Required: VERIFY or REJECT
     */
    @NotBlank(message = "Action is required")
    @Pattern(regexp = "VERIFY|REJECT", message = "Action must be VERIFY or REJECT")
    private String action;

    /**
     * Verification remarks
     * Optional: Additional notes for verification or rejection reason
     */
    private String remarks;
}
