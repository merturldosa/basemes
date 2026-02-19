package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Gauge Response DTO
 * 계측기 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaugeResponse {

    private Long gaugeId;
    private String tenantId;
    private String tenantName;

    private String gaugeCode;
    private String gaugeName;
    private String gaugeType;

    private String manufacturer;
    private String modelName;
    private String serialNo;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private Long departmentId;
    private String departmentCode;
    private String departmentName;

    private String location;
    private String measurementRange;
    private String accuracy;

    private Integer calibrationCycleDays;
    private LocalDate lastCalibrationDate;
    private LocalDate nextCalibrationDate;
    private String calibrationStatus;

    private String status;
    private String remarks;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
