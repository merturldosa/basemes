package kr.co.softice.mes.common.dto.product;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Request DTO
 * 제품 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "제품 코드는 필수입니다")
    @Size(max = 50, message = "제품 코드는 50자를 초과할 수 없습니다")
    private String productCode;

    @NotBlank(message = "제품명은 필수입니다")
    @Size(max = 200, message = "제품명은 200자를 초과할 수 없습니다")
    private String productName;

    @Size(max = 50, message = "제품 유형은 50자를 초과할 수 없습니다")
    private String productType;  // 완제품, 반제품, 원자재

    private String specification;

    @NotBlank(message = "단위는 필수입니다")
    @Size(max = 20, message = "단위는 20자를 초과할 수 없습니다")
    @Builder.Default
    private String unit = "EA";  // EA, KG, L 등

    private Integer standardCycleTime;  // 표준 사이클 타임 (초)

    private String description;

    @Builder.Default
    private Boolean isActive = true;
}
