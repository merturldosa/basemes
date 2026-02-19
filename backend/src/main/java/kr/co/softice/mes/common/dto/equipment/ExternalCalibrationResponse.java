package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * External Calibration Response DTO
 * 외부 검교정 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalCalibrationResponse {

    private Long calibrationId;
    private String tenantId;
    private String tenantName;

    private String calibrationNo;

    private Long gaugeId;
    private String gaugeCode;
    private String gaugeName;

    private String calibrationVendor;

    private LocalDate requestedDate;
    private LocalDate sentDate;
    private LocalDate completedDate;

    private String certificateNo;
    private String certificateUrl;

    private String calibrationResult;
    private BigDecimal cost;
    private LocalDate nextCalibrationDate;

    private String status;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
