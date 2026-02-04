package kr.co.softice.mes.common.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Site Response DTO
 * 사업장 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteResponse {

    private Long siteId;
    private String tenantId;
    private String siteCode;
    private String siteName;

    private String address;
    private String postalCode;
    private String country;
    private String region;

    private String phone;
    private String fax;
    private String email;

    private String managerName;
    private String managerPhone;
    private String managerEmail;

    private String siteType;
    private Boolean isActive;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
