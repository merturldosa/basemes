package kr.co.softice.mes.common.dto.purchase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Purchase Request Create Request DTO
 * 구매 요청 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestCreateRequest {

    @NotBlank(message = "요청 번호는 필수입니다")
    private String requestNo;

    @NotNull(message = "요청자 ID는 필수입니다")
    private Long requesterUserId;

    private String department;

    @NotNull(message = "자재 ID는 필수입니다")
    private Long materialId;

    @NotNull(message = "요청 수량은 필수입니다")
    private BigDecimal requestedQuantity;

    private LocalDateTime requiredDate;

    private String purpose;

    private String remarks;
}
