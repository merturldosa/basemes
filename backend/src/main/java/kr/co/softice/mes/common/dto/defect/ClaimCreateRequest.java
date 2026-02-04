package kr.co.softice.mes.common.dto.defect;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Claim Create Request DTO
 * 클레임 생성 요청
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimCreateRequest {

    @NotBlank(message = "클레임 번호는 필수입니다")
    @Size(max = 50, message = "클레임 번호는 50자 이하여야 합니다")
    private String claimNo;

    @NotNull(message = "클레임 일시는 필수입니다")
    private LocalDateTime claimDate;

    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;

    @Size(max = 100, message = "연락처명은 100자 이하여야 합니다")
    private String contactPerson;

    @Size(max = 50, message = "연락 전화는 50자 이하여야 합니다")
    private String contactPhone;

    @Size(max = 100, message = "연락 이메일은 100자 이하여야 합니다")
    private String contactEmail;

    // Optional product
    private Long productId;

    @Size(max = 100, message = "LOT 번호는 100자 이하여야 합니다")
    private String lotNo;

    // Optional sales references
    private Long salesOrderId;
    private Long shippingId;

    @Size(max = 50, message = "클레임 유형은 50자 이하여야 합니다")
    private String claimType; // QUALITY, DELIVERY, QUANTITY, PACKAGING, DOCUMENTATION, SERVICE, PRICE, OTHER

    @Size(max = 50, message = "클레임 분류는 50자 이하여야 합니다")
    private String claimCategory; // DEFECT, DAMAGE, SHORTAGE, DELAY, MISMATCH, etc

    @NotBlank(message = "클레임 설명은 필수입니다")
    private String claimDescription;

    @Builder.Default
    private BigDecimal claimedQuantity = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal claimedAmount = BigDecimal.ZERO;

    @Size(max = 30, message = "심각도는 30자 이하여야 합니다")
    private String severity; // CRITICAL, MAJOR, MINOR

    @Size(max = 30, message = "우선순위는 30자 이하여야 합니다")
    private String priority; // URGENT, HIGH, NORMAL, LOW

    @Builder.Default
    @Size(max = 30, message = "상태는 30자 이하여야 합니다")
    private String status = "RECEIVED"; // RECEIVED, INVESTIGATING, IN_PROGRESS, RESOLVED, CLOSED, REJECTED

    private Long responsibleDepartmentId;
    private Long responsibleUserId;

    private String remarks;
}
