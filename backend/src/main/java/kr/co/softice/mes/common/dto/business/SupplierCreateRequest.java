package kr.co.softice.mes.common.dto.business;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Supplier Create Request DTO
 * 공급업체 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCreateRequest {

    @NotBlank(message = "Supplier code is required")
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    @NotBlank(message = "Supplier type is required")
    @Builder.Default
    private String supplierType = "MATERIAL";  // MATERIAL, SERVICE, EQUIPMENT, BOTH

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

    @Builder.Default
    private String currency = "KRW";

    private String taxType;  // TAXABLE, EXEMPT, ZERO_RATE

    @Builder.Default
    private Integer leadTimeDays = 0;

    private BigDecimal minOrderAmount;

    @Builder.Default
    private Boolean isActive = true;

    private String rating;  // EXCELLENT, GOOD, AVERAGE, POOR
    private String remarks;
}
