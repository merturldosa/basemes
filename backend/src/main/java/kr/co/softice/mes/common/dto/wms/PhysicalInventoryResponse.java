package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 실사 계획 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryResponse {

    /**
     * 실사 ID
     */
    private Long physicalInventoryId;

    /**
     * 실사 번호
     */
    private String inventoryNo;

    /**
     * 실사 일자
     */
    private LocalDateTime inventoryDate;

    /**
     * 창고 ID
     */
    private Long warehouseId;

    /**
     * 창고 코드
     */
    private String warehouseCode;

    /**
     * 창고명
     */
    private String warehouseName;

    /**
     * 실사 상태
     */
    private String inventoryStatus;

    /**
     * 계획자 ID
     */
    private Long plannedByUserId;

    /**
     * 승인자 ID
     */
    private Long approvedByUserId;

    /**
     * 승인 일자
     */
    private LocalDateTime approvalDate;

    /**
     * 비고
     */
    private String remarks;

    /**
     * 실사 항목 목록
     */
    private List<PhysicalInventoryItemResponse> items;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;

    /**
     * 통계 정보
     */
    private Statistics statistics;

    /**
     * 통계 정보 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Integer totalItems;              // 전체 항목 수
        private Integer countedItems;            // 실사 완료 항목 수
        private Integer itemsRequiringAdjustment; // 조정 필요 항목 수
        private Integer approvedAdjustments;     // 승인된 조정 수
        private Integer rejectedAdjustments;     // 거부된 조정 수
    }
}
