package kr.co.softice.mes.common.dto.permission;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Update Request DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateRequest {

    @Size(max = 200, message = "권한명은 최대 200자까지 가능합니다")
    private String permissionName;

    @Size(max = 50, message = "모듈명은 최대 50자까지 가능합니다")
    private String module;

    @Size(max = 500, message = "설명은 최대 500자까지 가능합니다")
    private String description;
}
