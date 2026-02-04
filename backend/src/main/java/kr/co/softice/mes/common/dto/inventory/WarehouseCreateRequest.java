package kr.co.softice.mes.common.dto.inventory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Warehouse Create Request DTO
 * 창고 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseCreateRequest {

    @NotBlank(message = "Warehouse code is required")
    private String warehouseCode;

    @NotBlank(message = "Warehouse name is required")
    private String warehouseName;

    @NotBlank(message = "Warehouse type is required")
    private String warehouseType;  // RAW_MATERIAL, WORK_IN_PROCESS, FINISHED_GOODS, QUARANTINE, SCRAP

    private String location;

    @NotNull(message = "Manager user ID is required")
    private Long managerUserId;

    private String contactNumber;

    private Integer capacity;

    private String unit;

    @Builder.Default
    private Boolean isActive = true;

    private String remarks;
}
