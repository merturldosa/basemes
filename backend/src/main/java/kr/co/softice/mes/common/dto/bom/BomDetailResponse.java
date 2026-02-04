package kr.co.softice.mes.common.dto.bom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BOM Detail Response DTO
 * BOM 상세 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomDetailResponse {

    private Long bomDetailId;
    private Long bomId;
    private Integer sequence;
    private Long materialProductId;
    private String materialProductCode;
    private String materialProductName;
    private Long processId;
    private String processCode;
    private String processName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal usageRate;
    private BigDecimal scrapRate;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
