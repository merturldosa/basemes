package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Material Request Create Request DTO
 * 불출 신청 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestCreateRequest {

    private String requestNo;  // Optional: 자동 생성 가능 (MR-YYYYMMDD-0001)

    @NotNull(message = "Request date is required")
    private LocalDateTime requestDate;

    private Long workOrderId;  // Optional: 작업 지시 ID (생산용)

    @NotNull(message = "Requester user ID is required")
    private Long requesterUserId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Required date is required")
    private LocalDate requiredDate;

    @NotBlank(message = "Priority is required")
    private String priority;  // URGENT, HIGH, NORMAL, LOW

    private String purpose;  // PRODUCTION, MAINTENANCE, SAMPLE, OTHER

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<MaterialRequestItemRequest> items;

    private String remarks;
}
