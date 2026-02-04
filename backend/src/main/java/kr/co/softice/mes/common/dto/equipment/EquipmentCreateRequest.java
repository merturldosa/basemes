package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equipment Create Request DTO
 * 설비 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentCreateRequest {

    @NotBlank(message = "설비 코드는 필수입니다.")
    private String equipmentCode;

    @NotBlank(message = "설비명은 필수입니다.")
    private String equipmentName;

    @NotBlank(message = "설비 유형은 필수입니다.")
    private String equipmentType; // MACHINE, MOLD, TOOL, FACILITY, VEHICLE, OTHER

    private String equipmentCategory;

    private Long siteId;
    private Long departmentId;

    private String manufacturer;
    private String modelName;
    private String serialNo;
    private String location;

    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;

    private String capacity;
    private BigDecimal powerRating;
    private BigDecimal weight;

    @NotBlank(message = "상태는 필수입니다.")
    private String status; // OPERATIONAL, STOPPED, MAINTENANCE, BREAKDOWN, RETIRED

    private Integer maintenanceCycleDays;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;

    private BigDecimal standardCycleTime;
    private BigDecimal actualOeeTarget;

    private String warrantyPeriod;
    private LocalDate warrantyExpiryDate;

    private String remarks;
}
