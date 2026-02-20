package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.business.CustomerCreateRequest;
import kr.co.softice.mes.common.dto.business.CustomerResponse;
import kr.co.softice.mes.common.dto.business.CustomerUpdateRequest;
import kr.co.softice.mes.domain.entity.CustomerEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.CustomerService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer Controller
 * 고객 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "고객 관리 API")
public class CustomerController {

    private final CustomerService customerService;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'USER')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        String tenantId = TenantContext.getCurrentTenant();
        List<CustomerEntity> customers = customerService.findByTenant(tenantId);
        return ResponseEntity.ok(customers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'USER')")
    public ResponseEntity<List<CustomerResponse>> getActiveCustomers() {
        String tenantId = TenantContext.getCurrentTenant();
        List<CustomerEntity> customers = customerService.findActiveByTenant(tenantId);
        return ResponseEntity.ok(customers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/type/{customerType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'USER')")
    public ResponseEntity<List<CustomerResponse>> getCustomersByType(@PathVariable String customerType) {
        String tenantId = TenantContext.getCurrentTenant();
        List<CustomerEntity> customers = customerService.findByTenantAndType(tenantId, customerType);
        return ResponseEntity.ok(customers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'USER')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long customerId) {
        return customerService.findById(customerId)
            .map(customer -> ResponseEntity.ok(toResponse(customer)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        CustomerEntity customer = CustomerEntity.builder()
            .tenant(tenant)
            .customerCode(request.getCustomerCode())
            .customerName(request.getCustomerName())
            .customerType(request.getCustomerType())
            .businessNumber(request.getBusinessNumber())
            .representativeName(request.getRepresentativeName())
            .industry(request.getIndustry())
            .address(request.getAddress())
            .postalCode(request.getPostalCode())
            .phoneNumber(request.getPhoneNumber())
            .faxNumber(request.getFaxNumber())
            .email(request.getEmail())
            .website(request.getWebsite())
            .contactPerson(request.getContactPerson())
            .contactPhone(request.getContactPhone())
            .contactEmail(request.getContactEmail())
            .paymentTerms(request.getPaymentTerms())
            .creditLimit(request.getCreditLimit())
            .currency(request.getCurrency())
            .taxType(request.getTaxType())
            .isActive(request.getIsActive())
            .remarks(request.getRemarks())
            .build();

        CustomerEntity created = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<CustomerResponse> updateCustomer(
        @PathVariable Long customerId,
        @Valid @RequestBody CustomerUpdateRequest request) {

        return customerService.findById(customerId)
            .map(customer -> {
                customer.setCustomerName(request.getCustomerName());
                customer.setCustomerType(request.getCustomerType());
                customer.setBusinessNumber(request.getBusinessNumber());
                customer.setRepresentativeName(request.getRepresentativeName());
                customer.setIndustry(request.getIndustry());
                customer.setAddress(request.getAddress());
                customer.setPostalCode(request.getPostalCode());
                customer.setPhoneNumber(request.getPhoneNumber());
                customer.setFaxNumber(request.getFaxNumber());
                customer.setEmail(request.getEmail());
                customer.setWebsite(request.getWebsite());
                customer.setContactPerson(request.getContactPerson());
                customer.setContactPhone(request.getContactPhone());
                customer.setContactEmail(request.getContactEmail());
                customer.setPaymentTerms(request.getPaymentTerms());
                customer.setCreditLimit(request.getCreditLimit());
                customer.setCurrency(request.getCurrency());
                customer.setTaxType(request.getTaxType());
                customer.setIsActive(request.getIsActive());
                customer.setRemarks(request.getRemarks());

                CustomerEntity updated = customerService.updateCustomer(customer);
                return ResponseEntity.ok(toResponse(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{customerId}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<CustomerResponse> toggleActive(@PathVariable Long customerId) {
        CustomerEntity customer = customerService.toggleActive(customerId);
        return ResponseEntity.ok(toResponse(customer));
    }

    private CustomerResponse toResponse(CustomerEntity customer) {
        return CustomerResponse.builder()
            .customerId(customer.getCustomerId())
            .tenantId(customer.getTenant().getTenantId())
            .tenantName(customer.getTenant().getTenantName())
            .customerCode(customer.getCustomerCode())
            .customerName(customer.getCustomerName())
            .customerType(customer.getCustomerType())
            .businessNumber(customer.getBusinessNumber())
            .representativeName(customer.getRepresentativeName())
            .industry(customer.getIndustry())
            .address(customer.getAddress())
            .postalCode(customer.getPostalCode())
            .phoneNumber(customer.getPhoneNumber())
            .faxNumber(customer.getFaxNumber())
            .email(customer.getEmail())
            .website(customer.getWebsite())
            .contactPerson(customer.getContactPerson())
            .contactPhone(customer.getContactPhone())
            .contactEmail(customer.getContactEmail())
            .paymentTerms(customer.getPaymentTerms())
            .creditLimit(customer.getCreditLimit())
            .currency(customer.getCurrency())
            .taxType(customer.getTaxType())
            .isActive(customer.getIsActive())
            .remarks(customer.getRemarks())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }
}
