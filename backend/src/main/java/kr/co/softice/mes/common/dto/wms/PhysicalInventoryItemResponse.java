package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 실사 항목 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryItemResponse {

    /**
     * 실사 항목 ID
     */
    private Long physicalInventoryItemId;

    /**
     * 제품 ID
     */
    private Long productId;

    /**
     * 제품 코드
     */
    private String productCode;

    /**
     * 제품명
     */
    private String productName;

    /**
     * LOT ID
     */
    private Long lotId;

    /**
     * LOT 번호
     */
    private String lotNo;

    /**
     * 유효기간
     */
    private LocalDate expiryDate;

    /**
     * 위치
     */
    private String location;

    /**
     * 시스템 재고 수량
     */
    private BigDecimal systemQuantity;

    /**
     * 실사 수량
     */
    private BigDecimal countedQuantity;

    /**
     * 차이 수량
     */
    private BigDecimal differenceQuantity;

    /**
     * 조정 상태
     */
    private String adjustmentStatus;

    /**
     * 조정 트랜잭션 ID
     */
    private Long adjustmentTransactionId;

    /**
     * 실사자 ID
     */
    private Long countedByUserId;

    /**
     * 실사 일시
     */
    private LocalDateTime countedAt;

    /**
     * 비고
     */
    private String remarks;

    /**
     * 단위
     */
    private String unit;
}
