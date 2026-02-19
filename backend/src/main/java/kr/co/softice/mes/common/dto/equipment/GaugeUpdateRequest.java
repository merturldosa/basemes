package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Gauge Update Request DTO
 * 계측기 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaugeUpdateRequest {

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
