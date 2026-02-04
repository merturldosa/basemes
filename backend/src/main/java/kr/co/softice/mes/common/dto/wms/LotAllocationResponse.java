package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LOT 할당 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotAllocationResponse {

    /**
     * LOT ID
     */
    private Long lotId;

    /**
     * LOT 번호
     */
    private String lotNo;

    /**
     * 할당된 수량
     */
    private BigDecimal allocatedQuantity;

    /**
     * 가용 수량
     */
    private BigDecimal availableQuantity;

    /**
     * 유효기간
     */
    private LocalDate expiryDate;

    /**
     * 품질 상태
     */
    private String qualityStatus;

    /**
     * 제품 코드 (추가 정보)
     */
    private String productCode;

    /**
     * 제품명 (추가 정보)
     */
    private String productName;

    /**
     * 창고 코드 (추가 정보)
     */
    private String warehouseCode;

    /**
     * 창고명 (추가 정보)
     */
    private String warehouseName;
}
