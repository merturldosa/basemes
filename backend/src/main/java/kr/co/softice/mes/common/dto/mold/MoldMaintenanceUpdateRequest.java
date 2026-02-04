package kr.co.softice.mes.common.dto.mold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mold Maintenance Update Request DTO
 * 금형 보전 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldMaintenanceUpdateRequest {

    private String maintenanceType;
    private LocalDateTime maintenanceDate;

    private Long shotCountBefore;
    private Boolean shotCountReset;
    private Long shotCountAfter;

    private String maintenanceContent;
    private String partsReplaced;
    private String findings;
    private String correctiveAction;

    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private Integer laborHours;

    private String maintenanceResult;

    private Long technicianUserId;
    private String technicianName;

    private LocalDate nextMaintenanceDate;
    private String remarks;
}
