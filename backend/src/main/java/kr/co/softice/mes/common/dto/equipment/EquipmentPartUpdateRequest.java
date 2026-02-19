package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equipment Part Update Request DTO
 * 설비 부품 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentPartUpdateRequest {

    private Long equipmentId;
    private String partName;
    private String partType;

    private String manufacturer;
    private String modelName;
    private String serialNo;

    private LocalDate installationDate;
    private Integer expectedLifeDays;

    private BigDecimal unitPrice;

    private String status;
    private String remarks;
}
