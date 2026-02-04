package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Claim Service
 * 클레임 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ShippingRepository shippingRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /**
     * Get all claims for tenant
     */
    public List<ClaimEntity> getAllClaims(String tenantId) {
        log.info("Getting all claims for tenant: {}", tenantId);
        return claimRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get claim by ID
     */
    public ClaimEntity getClaimById(Long claimId) {
        log.info("Getting claim by ID: {}", claimId);
        return claimRepository.findByIdWithAllRelations(claimId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));
    }

    /**
     * Get claims by status
     */
    public List<ClaimEntity> getClaimsByStatus(String tenantId, String status) {
        log.info("Getting claims for tenant: {} with status: {}", tenantId, status);
        return claimRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get claims by claim type
     */
    public List<ClaimEntity> getClaimsByClaimType(String tenantId, String claimType) {
        log.info("Getting claims for tenant: {} with claim type: {}", tenantId, claimType);
        return claimRepository.findByTenantIdAndClaimType(tenantId, claimType);
    }

    /**
     * Create claim
     */
    @Transactional
    public ClaimEntity createClaim(String tenantId, ClaimEntity claim) {
        log.info("Creating claim: {} for tenant: {}", claim.getClaimNo(), tenantId);

        // Check duplicate
        if (claimRepository.existsByTenant_TenantIdAndClaimNo(tenantId, claim.getClaimNo())) {
            throw new BusinessException(ErrorCode.CLAIM_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        claim.setTenant(tenant);

        // Get customer
        CustomerEntity customer = customerRepository.findByIdWithAllRelations(claim.getCustomer().getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        claim.setCustomer(customer);
        claim.setCustomerCode(customer.getCustomerCode());
        claim.setCustomerName(customer.getCustomerName());

        // Set optional product
        if (claim.getProduct() != null && claim.getProduct().getProductId() != null) {
            ProductEntity product = productRepository.findById(claim.getProduct().getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            claim.setProduct(product);
            claim.setProductCode(product.getProductCode());
            claim.setProductName(product.getProductName());
        }

        // Set optional relations
        if (claim.getSalesOrder() != null && claim.getSalesOrder().getSalesOrderId() != null) {
            SalesOrderEntity salesOrder = salesOrderRepository.findByIdWithAllRelations(claim.getSalesOrder().getSalesOrderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ORDER_NOT_FOUND));
            claim.setSalesOrder(salesOrder);
            claim.setSalesOrderNo(salesOrder.getOrderNo());
        }

        if (claim.getShipping() != null && claim.getShipping().getShippingId() != null) {
            ShippingEntity shipping = shippingRepository.findByIdWithAllRelations(claim.getShipping().getShippingId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPING_NOT_FOUND));
            claim.setShipping(shipping);
        }

        if (claim.getResponsibleDepartment() != null && claim.getResponsibleDepartment().getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findByIdWithAllRelations(claim.getResponsibleDepartment().getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            claim.setResponsibleDepartment(department);
        }

        if (claim.getResponsibleUser() != null && claim.getResponsibleUser().getUserId() != null) {
            UserEntity responsibleUser = userRepository.findById(claim.getResponsibleUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            claim.setResponsibleUser(responsibleUser);
            if (claim.getAssignedDate() == null) {
                claim.setAssignedDate(LocalDateTime.now());
            }
        }

        // Set defaults
        if (claim.getClaimedQuantity() == null) {
            claim.setClaimedQuantity(BigDecimal.ZERO);
        }
        if (claim.getClaimedAmount() == null) {
            claim.setClaimedAmount(BigDecimal.ZERO);
        }
        if (claim.getResolutionAmount() == null) {
            claim.setResolutionAmount(BigDecimal.ZERO);
        }
        if (claim.getClaimCost() == null) {
            claim.setClaimCost(BigDecimal.ZERO);
        }
        if (claim.getCompensationAmount() == null) {
            claim.setCompensationAmount(BigDecimal.ZERO);
        }
        if (claim.getIsActive() == null) {
            claim.setIsActive(true);
        }
        if (claim.getStatus() == null) {
            claim.setStatus("RECEIVED");
        }

        ClaimEntity saved = claimRepository.save(claim);
        log.info("Claim created successfully: {}", saved.getClaimNo());
        return saved;
    }

    /**
     * Update claim
     */
    @Transactional
    public ClaimEntity updateClaim(Long claimId, ClaimEntity updateData) {
        log.info("Updating claim ID: {}", claimId);

        ClaimEntity existing = claimRepository.findByIdWithAllRelations(claimId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));

        // Update fields
        if (updateData.getClaimType() != null) {
            existing.setClaimType(updateData.getClaimType());
        }
        if (updateData.getClaimCategory() != null) {
            existing.setClaimCategory(updateData.getClaimCategory());
        }
        if (updateData.getClaimDescription() != null) {
            existing.setClaimDescription(updateData.getClaimDescription());
        }
        if (updateData.getClaimedQuantity() != null) {
            existing.setClaimedQuantity(updateData.getClaimedQuantity());
        }
        if (updateData.getClaimedAmount() != null) {
            existing.setClaimedAmount(updateData.getClaimedAmount());
        }
        if (updateData.getSeverity() != null) {
            existing.setSeverity(updateData.getSeverity());
        }
        if (updateData.getPriority() != null) {
            existing.setPriority(updateData.getPriority());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getInvestigationFindings() != null) {
            existing.setInvestigationFindings(updateData.getInvestigationFindings());
        }
        if (updateData.getRootCauseAnalysis() != null) {
            existing.setRootCauseAnalysis(updateData.getRootCauseAnalysis());
        }
        if (updateData.getResolutionType() != null) {
            existing.setResolutionType(updateData.getResolutionType());
        }
        if (updateData.getResolutionDescription() != null) {
            existing.setResolutionDescription(updateData.getResolutionDescription());
        }
        if (updateData.getResolutionAmount() != null) {
            existing.setResolutionAmount(updateData.getResolutionAmount());
        }
        if (updateData.getCorrectiveAction() != null) {
            existing.setCorrectiveAction(updateData.getCorrectiveAction());
        }
        if (updateData.getPreventiveAction() != null) {
            existing.setPreventiveAction(updateData.getPreventiveAction());
        }
        if (updateData.getCustomerAcceptance() != null) {
            existing.setCustomerAcceptance(updateData.getCustomerAcceptance());
        }
        if (updateData.getCustomerFeedback() != null) {
            existing.setCustomerFeedback(updateData.getCustomerFeedback());
        }
        if (updateData.getClaimCost() != null) {
            existing.setClaimCost(updateData.getClaimCost());
        }
        if (updateData.getCompensationAmount() != null) {
            existing.setCompensationAmount(updateData.getCompensationAmount());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        ClaimEntity updated = claimRepository.save(existing);
        log.info("Claim updated successfully: {}", updated.getClaimNo());
        return updated;
    }

    /**
     * Start investigation
     */
    @Transactional
    public ClaimEntity startInvestigation(Long claimId) {
        log.info("Starting investigation for claim ID: {}", claimId);

        ClaimEntity claim = claimRepository.findByIdWithAllRelations(claimId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));

        claim.setStatus("INVESTIGATING");

        ClaimEntity updated = claimRepository.save(claim);
        log.info("Investigation started for claim: {}", updated.getClaimNo());
        return updated;
    }

    /**
     * Resolve claim
     */
    @Transactional
    public ClaimEntity resolveClaim(Long claimId) {
        log.info("Resolving claim ID: {}", claimId);

        ClaimEntity claim = claimRepository.findByIdWithAllRelations(claimId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));

        claim.setStatus("RESOLVED");
        if (claim.getResolutionDate() == null) {
            claim.setResolutionDate(LocalDateTime.now());
        }

        ClaimEntity updated = claimRepository.save(claim);
        log.info("Claim resolved: {}", updated.getClaimNo());
        return updated;
    }

    /**
     * Close claim
     */
    @Transactional
    public ClaimEntity closeClaim(Long claimId) {
        log.info("Closing claim ID: {}", claimId);

        ClaimEntity claim = claimRepository.findByIdWithAllRelations(claimId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));

        claim.setStatus("CLOSED");
        if (claim.getActionCompletionDate() == null) {
            claim.setActionCompletionDate(LocalDateTime.now());
        }

        ClaimEntity updated = claimRepository.save(claim);
        log.info("Claim closed: {}", updated.getClaimNo());
        return updated;
    }

    /**
     * Delete claim
     */
    @Transactional
    public void deleteClaim(Long claimId) {
        log.info("Deleting claim ID: {}", claimId);

        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));

        claimRepository.delete(claim);
        log.info("Claim deleted successfully: {}", claim.getClaimNo());
    }
}
