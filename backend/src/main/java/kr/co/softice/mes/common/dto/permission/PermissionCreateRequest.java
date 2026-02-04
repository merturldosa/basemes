package kr.co.softice.mes.common.dto.permission;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Create Request DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCreateRequest {

    @NotBlank(message = "권한 코드는 필수입니다")
    @Size(max = 100, message = "권한 코드는 최대 100자까지 가능합니다")
    private String permissionCode;

    @NotBlank(message = "권한명은 필수입니다")
    @Size(max = 200, message = "권한명은 최대 200자까지 가능합니다")
    private String permissionName;

    @NotBlank(message = "모듈명은 필수입니다")
    @Size(max = 50, message = "모듈명은 최대 50자까지 가능합니다")
    private String module;

    @Size(max = 500, message = "설명은 최대 500자까지 가능합니다")
    private String description;
}
