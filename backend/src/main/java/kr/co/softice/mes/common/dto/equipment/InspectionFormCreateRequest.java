package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Inspection Form Create Request DTO
 * 점검 양식 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionFormCreateRequest {

    @NotBlank(message = "양식 코드는 필수입니다.")
    private String formCode;

    @NotBlank(message = "양식명은 필수입니다.")
    private String formName;

    private String description;
    private String equipmentType;
    private String inspectionType;

    private List<InspectionFormFieldDTO> fields;
}
