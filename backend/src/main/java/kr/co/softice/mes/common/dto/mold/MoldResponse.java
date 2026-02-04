package kr.co.softice.mes.common.dto.mold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mold Response DTO
 * 금형 응답 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldResponse {

    private Long moldId;
    private String tenantId;
    private String tenantName;

    private String moldCode;
    private String moldName;
    private String moldType;
    private String moldGrade;
    private Integer cavityCount;

    private Long currentShotCount;
    private Long maxShotCount;
    private Long maintenanceShotInterval;
    private Long lastMaintenanceShot;

    private Long siteId;
    private String siteCode;
    private String siteName;

    private Long departmentId;
    private String departmentCode;
    private String departmentName;

    private String manufacturer;
    private String modelName;
    private String serialNo;
    private String material;
    private BigDecimal weight;
    private String dimensions;

    private LocalDate manufactureDate;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private LocalDate firstUseDate;
    private String warrantyPeriod;
    private LocalDate warrantyExpiryDate;

    private String status;
    private String location;

    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
