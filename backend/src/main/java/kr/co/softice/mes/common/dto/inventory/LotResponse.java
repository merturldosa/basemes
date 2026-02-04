package kr.co.softice.mes.common.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lot Response DTO
 * LOT 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotResponse {

    private Long lotId;
    private String tenantId;
    private String tenantName;
    private String lotNo;
    private Long productId;
    private String productCode;
    private String productName;
    private Long workOrderId;
    private String workOrderNo;
    private BigDecimal initialQuantity;
    private BigDecimal currentQuantity;
    private String unit;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private String qualityStatus;
    private String supplier;
    private String supplierLotNo;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
