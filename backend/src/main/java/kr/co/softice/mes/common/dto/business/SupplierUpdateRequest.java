package kr.co.softice.mes.common.dto.business;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Supplier Update Request DTO
 * 공급업체 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierUpdateRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    @NotBlank(message = "Supplier type is required")
    private String supplierType;

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
    private String paymentTerms;
    private String currency;
    private String taxType;
    private Integer leadTimeDays;
    private BigDecimal minOrderAmount;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private String rating;
    private String remarks;
}
