package kr.co.softice.mes.common.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Response DTO
 * 제품 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long productId;
    private String productCode;
    private String productName;
    private String productType;
    private String specification;
    private String unit;
    private BigDecimal standardCycleTime;
    private Boolean isActive;
    private String tenantId;
    private String tenantName;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
