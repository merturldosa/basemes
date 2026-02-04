package kr.co.softice.mes.common.dto.inventory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lot Create Request DTO
 * LOT 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotCreateRequest {

    @NotBlank(message = "Lot number is required")
    private String lotNo;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long workOrderId;

    @NotNull(message = "Initial quantity is required")
    private BigDecimal initialQuantity;

    @NotNull(message = "Current quantity is required")
    private BigDecimal currentQuantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    @NotBlank(message = "Quality status is required")
    @Builder.Default
    private String qualityStatus = "PENDING";  // PENDING, PASSED, FAILED, QUARANTINE

    private String supplier;

    private String supplierLotNo;

    private String remarks;
}
