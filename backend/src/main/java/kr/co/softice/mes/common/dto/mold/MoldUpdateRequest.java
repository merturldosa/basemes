package kr.co.softice.mes.common.dto.mold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mold Update Request DTO
 * 금형 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldUpdateRequest {

    private String moldName;
    private String moldType;
    private String moldGrade;
    private Integer cavityCount;

    private Long maxShotCount;
    private Long maintenanceShotInterval;

    private String manufacturer;
    private String modelName;
    private String serialNo;
    private String material;
    private BigDecimal weight;

    private String status;
    private String location;
    private String remarks;
}
