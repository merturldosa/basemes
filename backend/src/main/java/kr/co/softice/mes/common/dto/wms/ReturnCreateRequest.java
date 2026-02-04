package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Return Create Request DTO
 * 반품 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnCreateRequest {

    private String returnNo;  // Optional: 자동 생성 가능 (RT-YYYYMMDD-0001)

    @NotNull(message = "Return date is required")
    private LocalDateTime returnDate;

    @NotBlank(message = "Return type is required")
    private String returnType;  // DEFECTIVE, EXCESS, WRONG_DELIVERY, OTHER

    private Long materialRequestId;  // Optional: 원본 불출 신청

    private Long workOrderId;  // Optional: 관련 작업 지시

    @NotNull(message = "Requester user ID is required")
    private Long requesterUserId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ReturnItemRequest> items;

    private String remarks;
}
