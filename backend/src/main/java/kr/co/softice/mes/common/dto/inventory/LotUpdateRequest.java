package kr.co.softice.mes.common.dto.inventory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lot Update Request DTO
 * LOT 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotUpdateRequest {

    @NotNull(message = "Lot ID is required")
    private Long lotId;

    @NotNull(message = "Current quantity is required")
    private BigDecimal currentQuantity;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    @NotBlank(message = "Quality status is required")
    private String qualityStatus;

    private String supplier;

    private String supplierLotNo;

    private String remarks;
}
