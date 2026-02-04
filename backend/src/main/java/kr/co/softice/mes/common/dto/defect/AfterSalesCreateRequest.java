package kr.co.softice.mes.common.dto.defect;

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
 * After Sales Create Request DTO
 * A/S 생성 요청
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterSalesCreateRequest {

    @NotBlank(message = "A/S 번호는 필수입니다")
    @Size(max = 50, message = "A/S 번호는 50자 이하여야 합니다")
    private String asNo;

    @NotNull(message = "접수 일시는 필수입니다")
    private LocalDateTime receiptDate;

    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;

    @Size(max = 100, message = "연락처명은 100자 이하여야 합니다")
    private String contactPerson;

    @Size(max = 50, message = "연락 전화는 50자 이하여야 합니다")
    private String contactPhone;

    @Size(max = 100, message = "연락 이메일은 100자 이하여야 합니다")
    private String contactEmail;

    @NotNull(message = "제품 ID는 필수입니다")
    private Long productId;

    @Size(max = 100, message = "시리얼 번호는 100자 이하여야 합니다")
    private String serialNo;

    @Size(max = 100, message = "LOT 번호는 100자 이하여야 합니다")
    private String lotNo;

    // Optional references
    private Long salesOrderId;
    private Long shippingId;
    private LocalDate purchaseDate;

    @Size(max = 30, message = "보증 상태는 30자 이하여야 합니다")
    private String warrantyStatus; // IN_WARRANTY, OUT_OF_WARRANTY, EXTENDED

    @Size(max = 50, message = "문제 분류는 50자 이하여야 합니다")
    private String issueCategory; // DEFECT, BREAKDOWN, INSTALLATION, USAGE, OTHER

    @NotBlank(message = "문제 설명은 필수입니다")
    private String issueDescription;

    private String symptom;

    @Size(max = 30, message = "서비스 유형은 30자 이하여야 합니다")
    private String serviceType; // REPAIR, REPLACEMENT, REFUND, TECHNICAL_SUPPORT

    @Builder.Default
    @Size(max = 30, message = "서비스 상태는 30자 이하여야 합니다")
    private String serviceStatus = "RECEIVED"; // RECEIVED, IN_PROGRESS, COMPLETED, CLOSED, CANCELLED

    @Size(max = 30, message = "우선순위는 30자 이하여야 합니다")
    private String priority; // URGENT, HIGH, NORMAL, LOW

    private Long assignedEngineerId;

    @Builder.Default
    private BigDecimal serviceCost = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal partsCost = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal chargeToCustomer = BigDecimal.ZERO;

    private String remarks;
}
