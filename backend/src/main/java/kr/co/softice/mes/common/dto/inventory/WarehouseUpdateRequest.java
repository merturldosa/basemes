package kr.co.softice.mes.common.dto.inventory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Warehouse Update Request DTO
 * 창고 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseUpdateRequest {

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotBlank(message = "Warehouse name is required")
    private String warehouseName;

    @NotBlank(message = "Warehouse type is required")
    private String warehouseType;

    private String location;

    @NotNull(message = "Manager user ID is required")
    private Long managerUserId;

    private String contactNumber;

    private Integer capacity;

    private String unit;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private String remarks;
}
