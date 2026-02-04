package kr.co.softice.mes.common.dto.mold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mold Maintenance Response DTO
 * 금형 보전 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldMaintenanceResponse {

    private Long maintenanceId;
    private String tenantId;
    private String tenantName;

    private Long moldId;
    private String moldCode;
    private String moldName;

    private String maintenanceNo;
    private String maintenanceType;
    private LocalDateTime maintenanceDate;

    private Long shotCountBefore;
    private Long shotCountAfter;
    private Boolean shotCountReset;

    private String maintenanceContent;
    private String partsReplaced;
    private String findings;
    private String correctiveAction;

    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private BigDecimal totalCost;
    private Integer laborHours;

    private String maintenanceResult;

    private Long technicianUserId;
    private String technicianUsername;
    private String technicianName;

    private LocalDate nextMaintenanceDate;

    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
