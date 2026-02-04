package kr.co.softice.mes.common.dto.common;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Department Request DTO
 * 부서 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "Department code is required")
    private String departmentCode;

    @NotBlank(message = "Department name is required")
    private String departmentName;

    private Long siteId;
    private Long parentDepartmentId;
    private Long managerUserId;

    private String departmentType;  // PRODUCTION, QUALITY, WAREHOUSE, PURCHASING, SALES, RD, ADMIN
    private Integer sortOrder;
    private String remarks;
}
