package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 실사 계획 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryCreateRequest {

    /**
     * 창고 ID
     */
    @NotNull(message = "창고 ID는 필수입니다")
    private Long warehouseId;

    /**
     * 실사 일자
     */
    @NotNull(message = "실사 일자는 필수입니다")
    private LocalDateTime inventoryDate;

    /**
     * 계획자 ID
     */
    @NotNull(message = "계획자 ID는 필수입니다")
    private Long plannedByUserId;

    /**
     * 비고
     */
    private String remarks;
}
