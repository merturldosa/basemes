package kr.co.softice.mes.common.dto.common;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Site Request DTO
 * 사업장 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteRequest {

    @NotBlank(message = "Site code is required")
    private String siteCode;

    @NotBlank(message = "Site name is required")
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

    private String siteType;  // FACTORY, WAREHOUSE, OFFICE, RD_CENTER
    private String remarks;
}
