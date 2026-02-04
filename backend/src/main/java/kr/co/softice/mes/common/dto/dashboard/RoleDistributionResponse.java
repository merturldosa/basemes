package kr.co.softice.mes.common.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Distribution Response DTO
 * 역할별 사용자 분포 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDistributionResponse {
    private String roleCode;     // 역할 코드
    private String roleName;     // 역할 이름
    private Long userCount;      // 해당 역할을 가진 사용자 수
}
