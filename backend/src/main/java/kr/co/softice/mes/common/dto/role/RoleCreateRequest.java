package kr.co.softice.mes.common.dto.role;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Role Create Request DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequest {

    @NotBlank(message = "역할 코드는 필수입니다")
    @Size(max = 50, message = "역할 코드는 최대 50자까지 가능합니다")
    private String roleCode;

    @NotBlank(message = "역할명은 필수입니다")
    @Size(max = 100, message = "역할명은 최대 100자까지 가능합니다")
    private String roleName;

    @Size(max = 500, message = "설명은 최대 500자까지 가능합니다")
    private String description;

    private Map<String, Object> config;
}
