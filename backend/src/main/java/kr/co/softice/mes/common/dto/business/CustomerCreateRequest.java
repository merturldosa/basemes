package kr.co.softice.mes.common.dto.business;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Customer Create Request DTO
 * 고객 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer type is required")
    @Builder.Default
    private String customerType = "DOMESTIC";  // DOMESTIC, OVERSEAS, BOTH

    private String businessNumber;
    private String representativeName;
    private String industry;
    private String address;
    private String postalCode;
    private String phoneNumber;
    private String faxNumber;
    private String email;
    private String website;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String paymentTerms;  // NET30, NET60, COD, ADVANCE
    private BigDecimal creditLimit;

    @Builder.Default
    private String currency = "KRW";

    private String taxType;  // TAXABLE, EXEMPT, ZERO_RATE

    @Builder.Default
    private Boolean isActive = true;

    private String remarks;
}
