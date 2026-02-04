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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisposalCreateRequest {

    private String disposalNo;

    @NotNull(message = "Disposal date is required")
    private LocalDateTime disposalDate;

    @NotBlank(message = "Disposal type is required")
    private String disposalType; // DEFECTIVE, EXPIRED, DAMAGED, OBSOLETE, OTHER

    private Long workOrderId;

    @NotNull(message = "Requester user ID is required")
    private Long requesterUserId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<DisposalItemRequest> items;

    private String remarks;
}
