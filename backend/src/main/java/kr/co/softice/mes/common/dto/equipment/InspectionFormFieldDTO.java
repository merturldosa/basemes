package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inspection Form Field DTO
 * 점검 양식 필드 DTO (요청/응답 공용)
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionFormFieldDTO {

    private Long fieldId; // null for new fields

    private String fieldName;
    private String fieldType; // TEXT, NUMBER, BOOLEAN, SELECT
    private Integer fieldOrder;
    private Boolean isRequired;
    private String options;
    private String unit;
    private BigDecimal minValue;
    private BigDecimal maxValue;
}
