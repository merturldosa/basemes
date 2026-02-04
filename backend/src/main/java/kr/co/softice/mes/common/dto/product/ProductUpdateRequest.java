package kr.co.softice.mes.common.dto.product;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product Update Request DTO
 * 제품 수정 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @Size(max = 200, message = "제품명은 200자 이하여야 합니다")
    private String productName;

    @Size(max = 50, message = "제품 유형은 50자 이하여야 합니다")
    private String productType;

    @Size(max = 500, message = "규격은 500자 이하여야 합니다")
    private String specification;

    @Size(max = 20, message = "단위는 20자 이하여야 합니다")
    private String unit;

    private BigDecimal standardCycleTime;

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
