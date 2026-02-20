package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.defect.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.ClaimService;
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
 * Claim Controller
 * 클레임 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Claims", description = "클레임 관리 API")
public class ClaimController {

    private final ClaimService claimService;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'CUSTOMER_SERVICE')")
    @Operation(summary = "Get all claims", description = "모든 클레임 조회")
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/claims - tenant: {}", tenantId);

        List<ClaimEntity> claims = claimService.getAllClaims(tenantId);
        List<ClaimResponse> responses = claims.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'CUSTOMER_SERVICE')")
    @Operation(summary = "Get claim by ID", description = "ID로 클레임 조회")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long id) {
        log.info("GET /api/claims/{}", id);

        ClaimEntity claim = claimService.getClaimById(id);
        return ResponseEntity.ok(toResponse(claim));
    }

    @Transactional(readOnly = true)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'CUSTOMER_SERVICE')")
    @Operation(summary = "Get claims by status", description = "상태별 클레임 조회")
    public ResponseEntity<List<ClaimResponse>> getClaimsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/claims/status/{} - tenant: {}", status, tenantId);

        List<ClaimEntity> claims = claimService.getClaimsByStatus(tenantId, status);
        List<ClaimResponse> responses = claims.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @Transactional(readOnly = true)
    @GetMapping("/type/{claimType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'CUSTOMER_SERVICE')")
    @Operation(summary = "Get claims by claim type", description = "유형별 클레임 조회")
    public ResponseEntity<List<ClaimResponse>> getClaimsByClaimType(@PathVariable String claimType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/claims/type/{} - tenant: {}", claimType, tenantId);

        List<ClaimEntity> claims = claimService.getClaimsByClaimType(tenantId, claimType);
        List<ClaimResponse> responses = claims.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'CUSTOMER_SERVICE')")
    @Operation(summary = "Create claim", description = "클레임 생성")
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody ClaimCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/claims - tenant: {}, claimNo: {}", tenantId, request.getClaimNo());

        ClaimEntity entity = toEntity(request);
        ClaimEntity created = claimService.createClaim(tenantId, entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'CUSTOMER_SERVICE')")
    @Operation(summary = "Update claim", description = "클레임 수정")
    public ResponseEntity<ClaimResponse> updateClaim(
            @PathVariable Long id,
            @Valid @RequestBody ClaimUpdateRequest request) {
        log.info("PUT /api/claims/{}", id);

        ClaimEntity updateData = toEntity(request);
        ClaimEntity updated = claimService.updateClaim(id, updateData);

        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping("/{id}/investigate")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "Start investigation", description = "조사 시작")
    public ResponseEntity<ClaimResponse> startInvestigation(@PathVariable Long id) {
        log.info("POST /api/claims/{}/investigate", id);

        ClaimEntity investigated = claimService.startInvestigation(id);
        return ResponseEntity.ok(toResponse(investigated));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "Resolve claim", description = "클레임 해결")
    public ResponseEntity<ClaimResponse> resolveClaim(@PathVariable Long id) {
        log.info("POST /api/claims/{}/resolve", id);

        ClaimEntity resolved = claimService.resolveClaim(id);
        return ResponseEntity.ok(toResponse(resolved));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "Close claim", description = "클레임 종료")
    public ResponseEntity<ClaimResponse> closeClaim(@PathVariable Long id) {
        log.info("POST /api/claims/{}/close", id);

        ClaimEntity closed = claimService.closeClaim(id);
        return ResponseEntity.ok(toResponse(closed));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete claim", description = "클레임 삭제")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long id) {
        log.info("DELETE /api/claims/{}", id);

        claimService.deleteClaim(id);
        return ResponseEntity.ok().build();
    }

    // Helper methods for entity-DTO conversion

    private ClaimResponse toResponse(ClaimEntity entity) {
        return ClaimResponse.builder()
                .claimId(entity.getClaimId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .claimNo(entity.getClaimNo())
                .claimDate(entity.getClaimDate())
                .customerId(entity.getCustomer().getCustomerId())
                .customerCode(entity.getCustomerCode())
                .customerName(entity.getCustomerName())
                .contactPerson(entity.getContactPerson())
                .contactPhone(entity.getContactPhone())
                .contactEmail(entity.getContactEmail())
                .productId(entity.getProduct() != null ? entity.getProduct().getProductId() : null)
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .lotNo(entity.getLotNo())
                .salesOrderId(entity.getSalesOrder() != null ? entity.getSalesOrder().getSalesOrderId() : null)
                .salesOrderNo(entity.getSalesOrderNo())
                .shippingId(entity.getShipping() != null ? entity.getShipping().getShippingId() : null)
                .claimType(entity.getClaimType())
                .claimCategory(entity.getClaimCategory())
                .claimDescription(entity.getClaimDescription())
                .claimedQuantity(entity.getClaimedQuantity())
                .claimedAmount(entity.getClaimedAmount())
                .severity(entity.getSeverity())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .responsibleDepartmentId(entity.getResponsibleDepartment() != null ? entity.getResponsibleDepartment().getDepartmentId() : null)
                .responsibleDepartmentName(entity.getResponsibleDepartment() != null ? entity.getResponsibleDepartment().getDepartmentName() : null)
                .responsibleUserId(entity.getResponsibleUser() != null ? entity.getResponsibleUser().getUserId() : null)
                .responsibleUserName(entity.getResponsibleUser() != null ? entity.getResponsibleUser().getFullName() : null)
                .assignedDate(entity.getAssignedDate())
                .investigationFindings(entity.getInvestigationFindings())
                .rootCauseAnalysis(entity.getRootCauseAnalysis())
                .resolutionType(entity.getResolutionType())
                .resolutionDescription(entity.getResolutionDescription())
                .resolutionAmount(entity.getResolutionAmount())
                .resolutionDate(entity.getResolutionDate())
                .correctiveAction(entity.getCorrectiveAction())
                .preventiveAction(entity.getPreventiveAction())
                .actionCompletionDate(entity.getActionCompletionDate())
                .customerAcceptance(entity.getCustomerAcceptance())
                .customerFeedback(entity.getCustomerFeedback())
                .claimCost(entity.getClaimCost())
                .compensationAmount(entity.getCompensationAmount())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ClaimEntity toEntity(ClaimCreateRequest request) {
        ClaimEntity.ClaimEntityBuilder builder = ClaimEntity.builder()
                .claimNo(request.getClaimNo())
                .claimDate(request.getClaimDate())
                .customer(CustomerEntity.builder().customerId(request.getCustomerId()).build())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .lotNo(request.getLotNo())
                .claimType(request.getClaimType())
                .claimCategory(request.getClaimCategory())
                .claimDescription(request.getClaimDescription())
                .claimedQuantity(request.getClaimedQuantity())
                .claimedAmount(request.getClaimedAmount())
                .severity(request.getSeverity())
                .priority(request.getPriority())
                .status(request.getStatus())
                .remarks(request.getRemarks());

        if (request.getProductId() != null) {
            builder.product(ProductEntity.builder().productId(request.getProductId()).build());
        }
        if (request.getSalesOrderId() != null) {
            builder.salesOrder(SalesOrderEntity.builder().salesOrderId(request.getSalesOrderId()).build());
        }
        if (request.getShippingId() != null) {
            builder.shipping(ShippingEntity.builder().shippingId(request.getShippingId()).build());
        }
        if (request.getResponsibleDepartmentId() != null) {
            builder.responsibleDepartment(DepartmentEntity.builder().departmentId(request.getResponsibleDepartmentId()).build());
        }
        if (request.getResponsibleUserId() != null) {
            builder.responsibleUser(UserEntity.builder().userId(request.getResponsibleUserId()).build());
        }

        return builder.build();
    }

    private ClaimEntity toEntity(ClaimUpdateRequest request) {
        return ClaimEntity.builder()
                .claimType(request.getClaimType())
                .claimCategory(request.getClaimCategory())
                .claimDescription(request.getClaimDescription())
                .claimedQuantity(request.getClaimedQuantity())
                .claimedAmount(request.getClaimedAmount())
                .severity(request.getSeverity())
                .priority(request.getPriority())
                .status(request.getStatus())
                .investigationFindings(request.getInvestigationFindings())
                .rootCauseAnalysis(request.getRootCauseAnalysis())
                .resolutionType(request.getResolutionType())
                .resolutionDescription(request.getResolutionDescription())
                .resolutionAmount(request.getResolutionAmount())
                .correctiveAction(request.getCorrectiveAction())
                .preventiveAction(request.getPreventiveAction())
                .customerAcceptance(request.getCustomerAcceptance())
                .customerFeedback(request.getCustomerFeedback())
                .claimCost(request.getClaimCost())
                .compensationAmount(request.getCompensationAmount())
                .remarks(request.getRemarks())
                .build();
    }
}
