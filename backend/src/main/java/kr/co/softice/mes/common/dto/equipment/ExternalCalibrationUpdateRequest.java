package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * External Calibration Update Request DTO
 * 외부 검교정 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalCalibrationUpdateRequest {

    private String calibrationVendor;
    private LocalDate sentDate;
    private BigDecimal cost;
    private String remarks;
    private String status;
}
