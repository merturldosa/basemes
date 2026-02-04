package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Equipment Inspection Response DTO
 * 설비 점검 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentInspectionResponse {

    private Long inspectionId;
    private String tenantId;
    private String tenantName;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private String inspectionNo;
    private String inspectionType;
    private LocalDateTime inspectionDate;
    private String inspectionResult;

    private Long inspectorUserId;
    private String inspectorName;

    private Long responsibleUserId;
    private String responsibleUserName;

    private String findings;
    private Boolean abnormalityDetected;
    private String severity;

    private String correctiveAction;
    private LocalDateTime correctiveActionDate;

    private String partsReplaced;
    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private BigDecimal totalCost;
    private Integer laborHours;

    private LocalDate nextInspectionDate;
    private String nextInspectionType;

    private String remarks;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
