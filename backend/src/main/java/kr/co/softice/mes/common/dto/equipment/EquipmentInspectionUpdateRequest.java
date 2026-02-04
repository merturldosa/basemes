package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Equipment Inspection Update Request DTO
 * 설비 점검 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentInspectionUpdateRequest {

    private String inspectionResult;

    private String findings;
    private Boolean abnormalityDetected;
    private String severity;

    private String correctiveAction;
    private LocalDateTime correctiveActionDate;

    private String partsReplaced;
    private BigDecimal partsCost;
    private BigDecimal laborCost;

    private String remarks;
}
