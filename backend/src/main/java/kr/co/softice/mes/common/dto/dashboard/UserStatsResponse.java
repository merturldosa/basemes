package kr.co.softice.mes.common.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Statistics Response DTO
 * 사용자 상태별 통계
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    /**
     * 상태 (active, inactive, locked)
     */
    private String status;

    /**
     * 해당 상태의 사용자 수
     */
    private Long count;

    /**
     * 표시 이름
     */
    private String displayName;
}
