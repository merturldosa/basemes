package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisposalItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long lotId;

    @NotNull(message = "Disposal quantity is required")
    @Positive(message = "Disposal quantity must be positive")
    private BigDecimal disposalQuantity;

    private String defectType;
    private String defectDescription;
    private LocalDate expiryDate;
    private String remarks;
}
