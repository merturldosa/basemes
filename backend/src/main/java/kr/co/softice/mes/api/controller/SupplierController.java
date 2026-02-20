package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.business.SupplierCreateRequest;
import kr.co.softice.mes.common.dto.business.SupplierResponse;
import kr.co.softice.mes.common.dto.business.SupplierUpdateRequest;
import kr.co.softice.mes.domain.entity.SupplierEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.SupplierService;
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
 * Supplier Controller
 * 공급업체 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier", description = "공급업체 관리 API")
public class SupplierController {

    private final SupplierService supplierService;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'USER')")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        String tenantId = TenantContext.getCurrentTenant();
        List<SupplierEntity> suppliers = supplierService.findByTenant(tenantId);
        return ResponseEntity.ok(suppliers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'USER')")
    public ResponseEntity<List<SupplierResponse>> getActiveSuppliers() {
        String tenantId = TenantContext.getCurrentTenant();
        List<SupplierEntity> suppliers = supplierService.findActiveByTenant(tenantId);
        return ResponseEntity.ok(suppliers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/type/{supplierType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'USER')")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByType(@PathVariable String supplierType) {
        String tenantId = TenantContext.getCurrentTenant();
        List<SupplierEntity> suppliers = supplierService.findByTenantAndType(tenantId, supplierType);
        return ResponseEntity.ok(suppliers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/rating/{rating}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'USER')")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByRating(@PathVariable String rating) {
        String tenantId = TenantContext.getCurrentTenant();
        List<SupplierEntity> suppliers = supplierService.findByTenantAndRating(tenantId, rating);
        return ResponseEntity.ok(suppliers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'USER')")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long supplierId) {
        return supplierService.findById(supplierId)
            .map(supplier -> ResponseEntity.ok(toResponse(supplier)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        SupplierEntity supplier = SupplierEntity.builder()
            .tenant(tenant)
            .supplierCode(request.getSupplierCode())
            .supplierName(request.getSupplierName())
            .supplierType(request.getSupplierType())
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
            .currency(request.getCurrency())
            .taxType(request.getTaxType())
            .leadTimeDays(request.getLeadTimeDays())
            .minOrderAmount(request.getMinOrderAmount())
            .isActive(request.getIsActive())
            .rating(request.getRating())
            .remarks(request.getRemarks())
            .build();

        SupplierEntity created = supplierService.createSupplier(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    public ResponseEntity<SupplierResponse> updateSupplier(
        @PathVariable Long supplierId,
        @Valid @RequestBody SupplierUpdateRequest request) {

        return supplierService.findById(supplierId)
            .map(supplier -> {
                supplier.setSupplierName(request.getSupplierName());
                supplier.setSupplierType(request.getSupplierType());
                supplier.setBusinessNumber(request.getBusinessNumber());
                supplier.setRepresentativeName(request.getRepresentativeName());
                supplier.setIndustry(request.getIndustry());
                supplier.setAddress(request.getAddress());
                supplier.setPostalCode(request.getPostalCode());
                supplier.setPhoneNumber(request.getPhoneNumber());
                supplier.setFaxNumber(request.getFaxNumber());
                supplier.setEmail(request.getEmail());
                supplier.setWebsite(request.getWebsite());
                supplier.setContactPerson(request.getContactPerson());
                supplier.setContactPhone(request.getContactPhone());
                supplier.setContactEmail(request.getContactEmail());
                supplier.setPaymentTerms(request.getPaymentTerms());
                supplier.setCurrency(request.getCurrency());
                supplier.setTaxType(request.getTaxType());
                supplier.setLeadTimeDays(request.getLeadTimeDays());
                supplier.setMinOrderAmount(request.getMinOrderAmount());
                supplier.setIsActive(request.getIsActive());
                supplier.setRating(request.getRating());
                supplier.setRemarks(request.getRemarks());

                SupplierEntity updated = supplierService.updateSupplier(supplier);
                return ResponseEntity.ok(toResponse(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{supplierId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long supplierId) {
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{supplierId}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER')")
    public ResponseEntity<SupplierResponse> toggleActive(@PathVariable Long supplierId) {
        SupplierEntity supplier = supplierService.toggleActive(supplierId);
        return ResponseEntity.ok(toResponse(supplier));
    }

    private SupplierResponse toResponse(SupplierEntity supplier) {
        return SupplierResponse.builder()
            .supplierId(supplier.getSupplierId())
            .tenantId(supplier.getTenant().getTenantId())
            .tenantName(supplier.getTenant().getTenantName())
            .supplierCode(supplier.getSupplierCode())
            .supplierName(supplier.getSupplierName())
            .supplierType(supplier.getSupplierType())
            .businessNumber(supplier.getBusinessNumber())
            .representativeName(supplier.getRepresentativeName())
            .industry(supplier.getIndustry())
            .address(supplier.getAddress())
            .postalCode(supplier.getPostalCode())
            .phoneNumber(supplier.getPhoneNumber())
            .faxNumber(supplier.getFaxNumber())
            .email(supplier.getEmail())
            .website(supplier.getWebsite())
            .contactPerson(supplier.getContactPerson())
            .contactPhone(supplier.getContactPhone())
            .contactEmail(supplier.getContactEmail())
            .paymentTerms(supplier.getPaymentTerms())
            .currency(supplier.getCurrency())
            .taxType(supplier.getTaxType())
            .leadTimeDays(supplier.getLeadTimeDays())
            .minOrderAmount(supplier.getMinOrderAmount())
            .isActive(supplier.getIsActive())
            .rating(supplier.getRating())
            .remarks(supplier.getRemarks())
            .createdAt(supplier.getCreatedAt())
            .updatedAt(supplier.getUpdatedAt())
            .build();
    }
}
