package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equipment Part Create Request DTO
 * 설비 부품 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentPartCreateRequest {

    @NotNull(message = "설비 ID는 필수입니다.")
    private Long equipmentId;

    @NotBlank(message = "부품 코드는 필수입니다.")
    private String partCode;

    @NotBlank(message = "부품명은 필수입니다.")
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
