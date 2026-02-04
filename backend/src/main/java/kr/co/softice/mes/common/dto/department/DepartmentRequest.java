package kr.co.softice.mes.common.dto.department;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "부서 코드는 필수입니다")
    @Size(max = 20, message = "부서 코드는 20자를 초과할 수 없습니다")
    private String departmentCode;

    @NotBlank(message = "부서명은 필수입니다")
    @Size(max = 100, message = "부서명은 100자를 초과할 수 없습니다")
    private String departmentName;

    private Long parentDepartmentId;

    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    private String description;

    private Integer sortOrder;

    @Builder.Default
    private Boolean isActive = true;
}
