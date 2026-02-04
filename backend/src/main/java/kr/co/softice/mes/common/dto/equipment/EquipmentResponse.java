package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Equipment Response DTO
 * 설비 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponse {

    private Long equipmentId;
    private String tenantId;
    private String tenantName;

    private String equipmentCode;
    private String equipmentName;
    private String equipmentType;
    private String equipmentCategory;

    private Long siteId;
    private String siteCode;
    private String siteName;

    private Long departmentId;
    private String departmentCode;
    private String departmentName;

    private String manufacturer;
    private String modelName;
    private String serialNo;
    private String location;

    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;

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

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
