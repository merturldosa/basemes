package kr.co.softice.mes.common.dto.product;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product Create Request DTO
 * 제품 생성 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "제품 코드는 필수입니다")
    @Size(max = 50, message = "제품 코드는 50자 이하여야 합니다")
    private String productCode;

    @NotBlank(message = "제품명은 필수입니다")
    @Size(max = 200, message = "제품명은 200자 이하여야 합니다")
    private String productName;

    @Size(max = 50, message = "제품 유형은 50자 이하여야 합니다")
    private String productType;

    @Size(max = 500, message = "규격은 500자 이하여야 합니다")
    private String specification;

    @NotBlank(message = "단위는 필수입니다")
    @Size(max = 20, message = "단위는 20자 이하여야 합니다")
    private String unit;

    private BigDecimal standardCycleTime;  // 표준 사이클 타임 (분)

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
