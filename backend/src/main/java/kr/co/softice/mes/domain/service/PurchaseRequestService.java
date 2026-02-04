package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.MaterialEntity;
import kr.co.softice.mes.domain.entity.PurchaseRequestEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.MaterialRepository;
import kr.co.softice.mes.domain.repository.PurchaseRequestRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Purchase Request Service
 * 구매 요청 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;

    /**
     * 테넌트별 모든 구매 요청 조회
     */
    public List<PurchaseRequestEntity> getAllPurchaseRequests(String tenantId) {
        log.info("Fetching all purchase requests for tenant: {}", tenantId);
        return purchaseRequestRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * 구매 요청 ID로 조회
     */
    public PurchaseRequestEntity getPurchaseRequestById(Long purchaseRequestId) {
        log.info("Fetching purchase request by ID: {}", purchaseRequestId);
        return purchaseRequestRepository.findByIdWithAllRelations(purchaseRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase request not found: " + purchaseRequestId));
    }

    /**
     * 테넌트 및 상태별 조회
     */
    public List<PurchaseRequestEntity> getPurchaseRequestsByStatus(String tenantId, String status) {
        log.info("Fetching purchase requests by status {} for tenant: {}", status, tenantId);
        return purchaseRequestRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * 구매 요청 생성
     */
    @Transactional
    public PurchaseRequestEntity createPurchaseRequest(String tenantId, PurchaseRequestEntity purchaseRequest) {
        log.info("Creating purchase request: {} for tenant: {}", purchaseRequest.getRequestNo(), tenantId);

        // Check if request number already exists
        if (purchaseRequestRepository.existsByTenant_TenantIdAndRequestNo(tenantId, purchaseRequest.getRequestNo())) {
            throw new IllegalArgumentException("Purchase request number already exists: " + purchaseRequest.getRequestNo());
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        purchaseRequest.setTenant(tenant);

        // Set requester
        if (purchaseRequest.getRequester() != null && purchaseRequest.getRequester().getUserId() != null) {
            UserEntity requester = userRepository.findById(purchaseRequest.getRequester().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Requester not found: " + purchaseRequest.getRequester().getUserId()));
            purchaseRequest.setRequester(requester);
        }

        // Set material
        if (purchaseRequest.getMaterial() != null && purchaseRequest.getMaterial().getMaterialId() != null) {
            MaterialEntity material = materialRepository.findById(purchaseRequest.getMaterial().getMaterialId())
                    .orElseThrow(() -> new IllegalArgumentException("Material not found: " + purchaseRequest.getMaterial().getMaterialId()));
            purchaseRequest.setMaterial(material);
        }

        // Set default values
        if (purchaseRequest.getStatus() == null) {
            purchaseRequest.setStatus("PENDING");
        }
        if (purchaseRequest.getRequestDate() == null) {
            purchaseRequest.setRequestDate(LocalDateTime.now());
        }

        PurchaseRequestEntity saved = purchaseRequestRepository.save(purchaseRequest);
        log.info("Purchase request created successfully: {}", saved.getPurchaseRequestId());

        return purchaseRequestRepository.findByIdWithAllRelations(saved.getPurchaseRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve created purchase request"));
    }

    /**
     * 구매 요청 승인
     */
    @Transactional
    public PurchaseRequestEntity approvePurchaseRequest(Long purchaseRequestId, Long approverUserId, String approvalComment) {
        log.info("Approving purchase request: {}", purchaseRequestId);

        PurchaseRequestEntity purchaseRequest = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase request not found: " + purchaseRequestId));

        if (!"PENDING".equals(purchaseRequest.getStatus())) {
            throw new IllegalArgumentException("Only pending purchase requests can be approved");
        }

        // Set approver
        UserEntity approver = userRepository.findById(approverUserId)
                .orElseThrow(() -> new IllegalArgumentException("Approver not found: " + approverUserId));

        purchaseRequest.setStatus("APPROVED");
        purchaseRequest.setApprover(approver);
        purchaseRequest.setApprovalDate(LocalDateTime.now());
        purchaseRequest.setApprovalComment(approvalComment);

        PurchaseRequestEntity saved = purchaseRequestRepository.save(purchaseRequest);
        log.info("Purchase request approved successfully: {}", saved.getPurchaseRequestId());

        return purchaseRequestRepository.findByIdWithAllRelations(saved.getPurchaseRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve approved purchase request"));
    }

    /**
     * 구매 요청 거절
     */
    @Transactional
    public PurchaseRequestEntity rejectPurchaseRequest(Long purchaseRequestId, Long approverUserId, String approvalComment) {
        log.info("Rejecting purchase request: {}", purchaseRequestId);

        PurchaseRequestEntity purchaseRequest = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase request not found: " + purchaseRequestId));

        if (!"PENDING".equals(purchaseRequest.getStatus())) {
            throw new IllegalArgumentException("Only pending purchase requests can be rejected");
        }

        // Set approver
        UserEntity approver = userRepository.findById(approverUserId)
                .orElseThrow(() -> new IllegalArgumentException("Approver not found: " + approverUserId));

        purchaseRequest.setStatus("REJECTED");
        purchaseRequest.setApprover(approver);
        purchaseRequest.setApprovalDate(LocalDateTime.now());
        purchaseRequest.setApprovalComment(approvalComment);

        PurchaseRequestEntity saved = purchaseRequestRepository.save(purchaseRequest);
        log.info("Purchase request rejected successfully: {}", saved.getPurchaseRequestId());

        return purchaseRequestRepository.findByIdWithAllRelations(saved.getPurchaseRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve rejected purchase request"));
    }

    /**
     * 구매 요청 삭제
     */
    @Transactional
    public void deletePurchaseRequest(Long purchaseRequestId) {
        log.info("Deleting purchase request: {}", purchaseRequestId);

        PurchaseRequestEntity purchaseRequest = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase request not found: " + purchaseRequestId));

        if ("ORDERED".equals(purchaseRequest.getStatus())) {
            throw new IllegalArgumentException("Cannot delete purchase request that has been ordered");
        }

        purchaseRequestRepository.deleteById(purchaseRequestId);
        log.info("Purchase request deleted successfully: {}", purchaseRequestId);
    }
}
