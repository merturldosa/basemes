package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equipment Update Request DTO
 * 설비 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentUpdateRequest {

    private String equipmentName;
    private String equipmentType;
    private String equipmentCategory;

    private Long siteId;
    private Long departmentId;

    private String manufacturer;
    private String modelName;
    private String serialNo;
    private String location;

    private String capacity;
    private BigDecimal powerRating;
    private BigDecimal weight;

    private String status;

    private Integer maintenanceCycleDays;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;

    private BigDecimal standardCycleTime;
    private BigDecimal actualOeeTarget;

    private String warrantyPeriod;
    private LocalDate warrantyExpiryDate;

    private String remarks;
}
