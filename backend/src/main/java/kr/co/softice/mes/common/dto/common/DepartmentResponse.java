package kr.co.softice.mes.common.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Department Response DTO
 * 부서 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private Long departmentId;
    private String tenantId;
    private String departmentCode;
    private String departmentName;

    private Long siteId;
    private String siteCode;
    private String siteName;

    private Long parentDepartmentId;
    private String parentDepartmentName;
    private Integer depthLevel;
    private Integer sortOrder;

    private Long managerUserId;
    private String managerUsername;

    private String departmentType;
    private Boolean isActive;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
