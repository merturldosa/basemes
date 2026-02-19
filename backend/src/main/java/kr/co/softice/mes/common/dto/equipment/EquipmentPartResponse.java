package kr.co.softice.mes.common.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Equipment Part Response DTO
 * 설비 부품 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentPartResponse {

    private Long partId;
    private String tenantId;
    private String tenantName;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;

    private String partCode;
    private String partName;
    private String partType;

    private String manufacturer;
    private String modelName;
    private String serialNo;

    private LocalDate installationDate;
    private Integer expectedLifeDays;
    private LocalDate replacementDate;
    private LocalDate nextReplacementDate;
    private Integer replacementCount;

    private BigDecimal unitPrice;

    private String status;
    private String remarks;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
