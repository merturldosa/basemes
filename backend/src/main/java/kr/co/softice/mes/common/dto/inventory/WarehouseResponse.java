package kr.co.softice.mes.common.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Warehouse Response DTO
 * 창고 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {

    private Long warehouseId;
    private String tenantId;
    private String tenantName;
    private String warehouseCode;
    private String warehouseName;
    private String warehouseType;
    private String location;
    private Long managerUserId;
    private String managerUserName;
    private String contactNumber;
    private Integer capacity;
    private String unit;
    private Boolean isActive;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
