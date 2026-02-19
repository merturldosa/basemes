package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Inspection Form Update Request DTO
 * 점검 양식 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionFormUpdateRequest {

    private String formName;
    private String description;
    private String equipmentType;
    private String inspectionType;

    private List<InspectionFormFieldDTO> fields;
}
