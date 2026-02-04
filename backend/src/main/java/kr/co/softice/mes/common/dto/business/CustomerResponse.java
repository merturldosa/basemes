package kr.co.softice.mes.common.dto.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer Response DTO
 * 고객 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long customerId;
    private String tenantId;
    private String tenantName;
    private String customerCode;
    private String customerName;
    private String customerType;
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
    private BigDecimal creditLimit;
    private String currency;
    private String taxType;
    private Boolean isActive;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
