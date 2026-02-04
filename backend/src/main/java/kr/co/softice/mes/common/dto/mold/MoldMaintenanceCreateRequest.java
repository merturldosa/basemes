package kr.co.softice.mes.common.dto.mold;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mold Maintenance Create Request DTO
 * 금형 보전 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldMaintenanceCreateRequest {

    @NotNull(message = "금형 ID는 필수입니다.")
    private Long moldId;

    @NotBlank(message = "보전 번호는 필수입니다.")
    private String maintenanceNo;

    @NotBlank(message = "보전 유형은 필수입니다.")
    private String maintenanceType; // DAILY_CHECK, PERIODIC, SHOT_BASED, EMERGENCY_REPAIR, OVERHAUL

    @NotNull(message = "보전 일시는 필수입니다.")
    private LocalDateTime maintenanceDate;

    private Long shotCountBefore;
    private Boolean shotCountReset; // true인 경우 보전 후 Shot 수 리셋
    private Long shotCountAfter;

    private String maintenanceContent;
    private String partsReplaced;
    private String findings;
    private String correctiveAction;

    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private Integer laborHours;

    private String maintenanceResult; // COMPLETED, PARTIAL, FAILED

    private Long technicianUserId;
    private String technicianName;

    private LocalDate nextMaintenanceDate;
    private String remarks;
}
