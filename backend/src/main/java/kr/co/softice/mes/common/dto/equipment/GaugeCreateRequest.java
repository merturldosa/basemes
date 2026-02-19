package kr.co.softice.mes.common.dto.equipment;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Gauge Create Request DTO
 * 계측기 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaugeCreateRequest {

    @NotBlank(message = "계측기 코드는 필수입니다.")
    private String gaugeCode;

    @NotBlank(message = "계측기명은 필수입니다.")
    private String gaugeName;

    private String gaugeType;
    private String manufacturer;
    private String modelName;
    private String serialNo;

    private Long equipmentId;
    private Long departmentId;

    private String location;
    private String measurementRange;
    private String accuracy;

    private Integer calibrationCycleDays;
    private LocalDate lastCalibrationDate;
    private LocalDate nextCalibrationDate;
    private String calibrationStatus;

    private String status;
    private String remarks;
}
