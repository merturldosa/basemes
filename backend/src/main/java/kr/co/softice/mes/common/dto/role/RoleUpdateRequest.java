package kr.co.softice.mes.common.dto.role;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Role Update Request DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequest {

    @Size(max = 100, message = "역할명은 최대 100자까지 가능합니다")
    private String roleName;

    @Size(max = 500, message = "설명은 최대 500자까지 가능합니다")
    private String description;

    private Map<String, Object> config;
}
