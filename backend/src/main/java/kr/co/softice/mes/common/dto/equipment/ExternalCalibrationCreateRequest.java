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
 * External Calibration Create Request DTO
 * 외부 검교정 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalCalibrationCreateRequest {

    @NotBlank(message = "검교정번호는 필수입니다.")
    private String calibrationNo;

    @NotNull(message = "계측기 ID는 필수입니다.")
    private Long gaugeId;

    @NotNull(message = "요청일자는 필수입니다.")
    private LocalDate requestedDate;

    private String calibrationVendor;
    private BigDecimal cost;
    private String remarks;
}
