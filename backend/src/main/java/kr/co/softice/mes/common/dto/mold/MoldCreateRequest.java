package kr.co.softice.mes.common.dto.mold;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mold Create Request DTO
 * 금형 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldCreateRequest {

    @NotBlank(message = "금형 코드는 필수입니다.")
    private String moldCode;

    @NotBlank(message = "금형명은 필수입니다.")
    private String moldName;

    @NotBlank(message = "금형 유형은 필수입니다.")
    private String moldType; // INJECTION, PRESS, DIE_CASTING, FORGING, OTHER

    private String moldGrade; // A, B, C, S
    private Integer cavityCount;

    private Long maxShotCount;
    private Long maintenanceShotInterval;

    private Long siteId;
    private Long departmentId;

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

    @NotBlank(message = "상태는 필수입니다.")
    private String status; // AVAILABLE, IN_USE, MAINTENANCE, BREAKDOWN, RETIRED

    private String location;
    private String remarks;
}
