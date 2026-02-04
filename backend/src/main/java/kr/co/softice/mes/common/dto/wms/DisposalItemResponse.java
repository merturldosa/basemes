package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisposalItemResponse {

    private Long disposalItemId;
    private Long productId;
    private String productCode;
    private String productName;
    private String productType;
    private String unit;
    private Long lotId;
    private String lotNo;
    private BigDecimal disposalQuantity;
    private BigDecimal processedQuantity;
    private Long disposalTransactionId;
    private String defectType;
    private String defectDescription;
    private LocalDate expiryDate;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
