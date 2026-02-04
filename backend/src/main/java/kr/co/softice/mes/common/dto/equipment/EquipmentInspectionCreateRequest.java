package kr.co.softice.mes.common.dto.equipment;

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
 * Equipment Inspection Create Request DTO
 * 설비 점검 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentInspectionCreateRequest {

    @NotNull(message = "설비 ID는 필수입니다.")
    private Long equipmentId;

    @NotBlank(message = "점검 번호는 필수입니다.")
    private String inspectionNo;

    @NotBlank(message = "점검 유형은 필수입니다.")
    private String inspectionType; // DAILY, PERIODIC, PREVENTIVE, CORRECTIVE, BREAKDOWN

    @NotNull(message = "점검 일시는 필수입니다.")
    private LocalDateTime inspectionDate;

    @NotBlank(message = "점검 결과는 필수입니다.")
    private String inspectionResult; // PASS, FAIL, CONDITIONAL

    private Long inspectorUserId;
    private String inspectorName;

    private Long responsibleUserId;
    private String responsibleUserName;

    private String findings;
    private Boolean abnormalityDetected;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private String correctiveAction;
    private LocalDateTime correctiveActionDate;

    private String partsReplaced;
    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private Integer laborHours;

    private LocalDate nextInspectionDate;
    private String nextInspectionType;

    private String remarks;
}
