package kr.co.softice.mes.common.dto.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Supplier Response DTO
 * 공급업체 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private Long supplierId;
    private String tenantId;
    private String tenantName;
    private String supplierCode;
    private String supplierName;
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
    private Boolean isActive;
    private String rating;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
