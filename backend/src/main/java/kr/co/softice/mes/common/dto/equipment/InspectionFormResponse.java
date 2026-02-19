package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inspection Form Response DTO
 * 점검 양식 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionFormResponse {

    private Long formId;
    private String tenantId;
    private String tenantName;

    private String formCode;
    private String formName;
    private String description;
    private String equipmentType;
    private String inspectionType;

    private List<InspectionFormFieldDTO> fields;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
