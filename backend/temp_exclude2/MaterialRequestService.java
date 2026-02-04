package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Material Request Service
 * 불출 신청 관리 서비스
 *
 * 핵심 기능:
 * - 불출 신청 생성 (작업 지시 기반 또는 수동)
 * - 승인 워크플로우
 * - 불출 처리 (재고 출고, LOT 선택)
 * - 인수인계 생성
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialRequestService {

    private final MaterialRequestRepository materialRequestRepository;
    private final MaterialHandoverRepository materialHandoverRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final LotRepository lotRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * Find all material requests by tenant
     */
    public List<MaterialRequestEntity> findByTenant(String tenantId) {
        return materialRequestRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find material request by ID
     */
    public Optional<MaterialRequestEntity> findById(Long materialRequestId) {
        return materialRequestRepository.findByIdWithAllRelations(materialRequestId);
    }

    /**
     * Find material requests by status
     */
    public List<MaterialRequestEntity> findByStatus(String tenantId, String status) {
        return materialRequestRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Find material requests by work order
     */
    public List<MaterialRequestEntity> findByWorkOrder(Long workOrderId) {
        return materialRequestRepository.findByWorkOrderIdWithRelations(workOrderId);
    }

    /**
     * Find pending requests by warehouse
     */
    public List<MaterialRequestEntity> findPendingByWarehouse(Long warehouseId) {
        return materialRequestRepository.findPendingRequestsByWarehouse(warehouseId);
    }

    /**
     * Find urgent requests
     */
    public List<MaterialRequestEntity> findUrgentRequests(String tenantId) {
        return materialRequestRepository.findUrgentRequests(tenantId);
    }

    /**
     * Create new material request
     *
     * 워크플로우:
     * 1. 신청 번호 자동 생성 (MR-YYYYMMDD-0001)
     * 2. 신청 헤더 생성 (PENDING 상태)
     * 3. 신청 항목 검증 (재고 가용성 확인)
     * 4. 자동 승인 여부 확인 (설정에 따라)
     */
    @Transactional
    public MaterialRequestEntity createMaterialRequest(MaterialRequestEntity request) {
        log.info("Creating material request for tenant: {}, warehouse: {}",
            request.getTenant().getTenantId(),
            request.getWarehouse().getWarehouseCode());

        // 1. Generate request number if not provided
        if (request.getRequestNo() == null || request.getRequestNo().isEmpty()) {
            request.setRequestNo(generateRequestNo(request.getTenant().getTenantId()));
        }

        // Check duplicate
        if (materialRequestRepository.existsByTenant_TenantIdAndRequestNo(
                request.getTenant().getTenantId(), request.getRequestNo())) {
            throw new IllegalArgumentException("Request number already exists: " + request.getRequestNo());
        }

        // 2. Set initial status
        if (request.getRequestStatus() == null) {
            request.setRequestStatus("PENDING");
        }

        if (request.getIsActive() == null) {
            request.setIsActive(true);
        }

        // 3. Validate items availability
        for (MaterialRequestItemEntity item : request.getItems()) {
            validateItemAvailability(request, item);
        }

        // 4. Save material request
        MaterialRequestEntity saved = materialRequestRepository.save(request);

        log.info("Created material request: {} with {} items",
            saved.getRequestNo(), saved.getItems().size());

        return materialRequestRepository.findByIdWithAllRelations(saved.getMaterialRequestId())
            .orElse(saved);
    }

    /**
     * Approve material request
     *
     * 워크플로우:
     * 1. 상태 검증 (PENDING만 승인 가능)
     * 2. 승인 정보 업데이트
     * 3. 승인 수량 = 신청 수량 (또는 조정)
     * 4. 상태 → APPROVED
     */
    @Transactional
    public MaterialRequestEntity approveMaterialRequest(Long materialRequestId, Long approverId, String remarks) {
        log.info("Approving material request: {} by user: {}", materialRequestId, approverId);

        MaterialRequestEntity request = materialRequestRepository.findByIdWithAllRelations(materialRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Material request not found: " + materialRequestId));

        // Validate status
        if (!"PENDING".equals(request.getRequestStatus())) {
            throw new IllegalStateException("Cannot approve request in status: " + request.getRequestStatus());
        }

        // Find approver
        UserEntity approver = userRepository.findById(approverId)
            .orElseThrow(() -> new IllegalArgumentException("Approver not found: " + approverId));

        // Update approval info
        request.setApprover(approver);
        request.setApproverName(approver.getFullName());
        request.setApprovalDate(LocalDateTime.now());
        request.setApprovalRemarks(remarks);
        request.setRequestStatus("APPROVED");

        // Set approved quantity = requested quantity for all items
        for (MaterialRequestItemEntity item : request.getItems()) {
            if (item.getApprovedQuantity() == null) {
                item.setApprovedQuantity(item.getRequestedQuantity());
            }
        }

        MaterialRequestEntity approved = materialRequestRepository.save(request);
        log.info("Approved material request: {}", approved.getRequestNo());

        return materialRequestRepository.findByIdWithAllRelations(approved.getMaterialRequestId())
            .orElse(approved);
    }

    /**
     * Reject material request
     */
    @Transactional
    public MaterialRequestEntity rejectMaterialRequest(Long materialRequestId, Long approverId, String reason) {
        log.info("Rejecting material request: {} by user: {}", materialRequestId, approverId);

        MaterialRequestEntity request = materialRequestRepository.findByIdWithAllRelations(materialRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Material request not found: " + materialRequestId));

        // Validate status
        if (!"PENDING".equals(request.getRequestStatus())) {
            throw new IllegalStateException("Cannot reject request in status: " + request.getRequestStatus());
        }

        // Find approver
        UserEntity approver = userRepository.findById(approverId)
            .orElseThrow(() -> new IllegalArgumentException("Approver not found: " + approverId));

        // Update approval info
        request.setApprover(approver);
        request.setApproverName(approver.getFullName());
        request.setApprovalDate(LocalDateTime.now());
        request.setApprovalRemarks(reason);
        request.setRequestStatus("REJECTED");

        MaterialRequestEntity rejected = materialRequestRepository.save(request);
        log.info("Rejected material request: {}", rejected.getRequestNo());

        return materialRequestRepository.findByIdWithAllRelations(rejected.getMaterialRequestId())
            .orElse(rejected);
    }

    /**
     * Issue materials (불출 처리)
     *
     * 워크플로우:
     * 1. 상태 검증 (APPROVED만 불출 가능)
     * 2. 각 항목에 대해:
     *    - LOT 선택 (FIFO 또는 지정 LOT)
     *    - 재고 트랜잭션 생성 (OUT_ISSUE)
     *    - 재고 차감 (available_quantity)
     *    - 인수인계 기록 생성
     * 3. 상태 → ISSUED
     */
    @Transactional
    public MaterialRequestEntity issueMaterials(Long materialRequestId, Long issuerUserId) {
        log.info("Issuing materials for request: {} by user: {}", materialRequestId, issuerUserId);

        MaterialRequestEntity request = materialRequestRepository.findByIdWithAllRelations(materialRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Material request not found: " + materialRequestId));

        // Validate status
        if (!"APPROVED".equals(request.getRequestStatus())) {
            throw new IllegalStateException("Cannot issue request in status: " + request.getRequestStatus());
        }

        // Find issuer
        UserEntity issuer = userRepository.findById(issuerUserId)
            .orElseThrow(() -> new IllegalArgumentException("Issuer not found: " + issuerUserId));

        // Process each item
        for (MaterialRequestItemEntity item : request.getItems()) {
            if ("COMPLETED".equals(item.getIssueStatus()) || "CANCELLED".equals(item.getIssueStatus())) {
                continue; // Skip already processed items
            }

            processItemIssue(request, item, issuer);
        }

        // Update request status
        request.setRequestStatus("ISSUED");

        MaterialRequestEntity issued = materialRequestRepository.save(request);
        log.info("Issued materials for request: {}", issued.getRequestNo());

        return materialRequestRepository.findByIdWithAllRelations(issued.getMaterialRequestId())
            .orElse(issued);
    }

    /**
     * Complete material request
     */
    @Transactional
    public MaterialRequestEntity completeMaterialRequest(Long materialRequestId) {
        log.info("Completing material request: {}", materialRequestId);

        MaterialRequestEntity request = materialRequestRepository.findByIdWithAllRelations(materialRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Material request not found: " + materialRequestId));

        // Check all items are completed
        boolean allCompleted = request.getItems().stream()
            .allMatch(item -> "COMPLETED".equals(item.getIssueStatus()));

        if (!allCompleted) {
            throw new IllegalStateException("Cannot complete request with incomplete items");
        }

        request.setRequestStatus("COMPLETED");

        MaterialRequestEntity completed = materialRequestRepository.save(request);
        log.info("Completed material request: {}", completed.getRequestNo());

        return materialRequestRepository.findByIdWithAllRelations(completed.getMaterialRequestId())
            .orElse(completed);
    }

    /**
     * Cancel material request
     */
    @Transactional
    public MaterialRequestEntity cancelMaterialRequest(Long materialRequestId, String cancelReason) {
        log.info("Cancelling material request: {}", materialRequestId);

        MaterialRequestEntity request = materialRequestRepository.findByIdWithAllRelations(materialRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Material request not found: " + materialRequestId));

        // Can only cancel PENDING or APPROVED requests
        if (!"PENDING".equals(request.getRequestStatus()) && !"APPROVED".equals(request.getRequestStatus())) {
            throw new IllegalStateException("Cannot cancel request in status: " + request.getRequestStatus());
        }

        request.setRequestStatus("CANCELLED");
        request.setRemarks(cancelReason);

        // Cancel all items
        for (MaterialRequestItemEntity item : request.getItems()) {
            item.setIssueStatus("CANCELLED");
        }

        MaterialRequestEntity cancelled = materialRequestRepository.save(request);
        log.info("Cancelled material request: {}", cancelled.getRequestNo());

        return materialRequestRepository.findByIdWithAllRelations(cancelled.getMaterialRequestId())
            .orElse(cancelled);
    }

    // ================== Private Helper Methods ==================

    /**
     * Generate sequential request number: MR-YYYYMMDD-0001
     */
    private String generateRequestNo(String tenantId) {
        String datePrefix = "MR-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        long count = materialRequestRepository.findByTenant_TenantId(tenantId).stream()
            .filter(mr -> mr.getRequestNo() != null && mr.getRequestNo().startsWith(datePrefix))
            .count();

        return String.format("%s-%04d", datePrefix, count + 1);
    }

    /**
     * Validate item availability in inventory
     */
    private void validateItemAvailability(MaterialRequestEntity request, MaterialRequestItemEntity item) {
        // Find available inventory
        List<InventoryEntity> inventories = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(
                request.getTenant().getTenantId(),
                request.getWarehouse().getWarehouseId(),
                item.getProduct().getProductId()
            );

        BigDecimal totalAvailable = inventories.stream()
            .map(InventoryEntity::getAvailableQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(item.getRequestedQuantity()) < 0) {
            log.warn("Insufficient inventory for product: {}, requested: {}, available: {}",
                item.getProductCode(), item.getRequestedQuantity(), totalAvailable);
            // Don't throw exception, just log warning
        }
    }

    /**
     * Process individual material request item issue
     */
    private void processItemIssue(MaterialRequestEntity request, MaterialRequestItemEntity item, UserEntity issuer) {
        log.info("Processing issue for item: product={}, quantity={}",
            item.getProductCode(), item.getApprovedQuantity());

        // Select LOT (FIFO or specific LOT)
        LotEntity lot = selectLotForIssue(request, item);

        if (lot == null) {
            throw new IllegalStateException("No available LOT for product: " + item.getProductCode());
        }

        // Create inventory transaction
        InventoryTransactionEntity transaction = createIssueTransaction(request, item, lot, issuer);

        // Update inventory balance
        updateInventoryBalance(request.getWarehouse(), item.getProduct(), lot,
            item.getApprovedQuantity(), "OUT_ISSUE");

        // Create handover record
        createHandoverRecord(request, item, lot, transaction, issuer);

        // Update item status
        item.setIssuedQuantity(item.getApprovedQuantity());
        item.setIssueStatus("COMPLETED");
        item.setInventoryTransaction(transaction);
    }

    /**
     * Select LOT for issue using FIFO strategy
     */
    private LotEntity selectLotForIssue(MaterialRequestEntity request, MaterialRequestItemEntity item) {
        // If specific LOT requested, use that
        if (item.getRequestedLotNo() != null && !item.getRequestedLotNo().isEmpty()) {
            return lotRepository.findByTenant_TenantIdAndLotNo(
                request.getTenant().getTenantId(),
                item.getRequestedLotNo()
            ).orElse(null);
        }

        // Otherwise, use FIFO - find oldest LOT with available quantity
        List<LotEntity> lots = lotRepository.findByTenant_TenantIdAndProduct_ProductIdOrderByCreatedAtAsc(
            request.getTenant().getTenantId(),
            item.getProduct().getProductId()
        );

        for (LotEntity lot : lots) {
            if (lot.getCurrentQuantity().compareTo(item.getApprovedQuantity()) >= 0 &&
                "PASSED".equals(lot.getQualityStatus()) &&
                Boolean.TRUE.equals(lot.getIsActive())) {
                return lot;
            }
        }

        return null; // No suitable LOT found
    }

    /**
     * Create inventory transaction for issue
     */
    private InventoryTransactionEntity createIssueTransaction(
            MaterialRequestEntity request,
            MaterialRequestItemEntity item,
            LotEntity lot,
            UserEntity issuer) {

        String transactionNo = String.format("OUT-%s-%03d",
            request.getRequestNo(),
            request.getItems().indexOf(item) + 1);

        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
            .tenant(request.getTenant())
            .transactionNo(transactionNo)
            .transactionDate(LocalDateTime.now())
            .transactionType("OUT_ISSUE")
            .warehouse(request.getWarehouse())
            .product(item.getProduct())
            .lot(lot)
            .quantity(item.getApprovedQuantity())
            .unit(item.getUnit())
            .transactionUser(issuer)
            .approvalStatus("APPROVED")
            .approvedBy(issuer)
            .approvedDate(LocalDateTime.now())
            .referenceNo(request.getRequestNo())
            .remarks("Material issue for request: " + request.getRequestNo())
            .build();

        return inventoryTransactionRepository.save(transaction);
    }

    /**
     * Update inventory balance (subtract issued quantity)
     */
    private void updateInventoryBalance(WarehouseEntity warehouse, ProductEntity product,
                                       LotEntity lot, BigDecimal quantity, String transactionType) {
        // Find inventory record
        Optional<InventoryEntity> existingInventory = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                warehouse.getTenant().getTenantId(),
                warehouse.getWarehouseId(),
                product.getProductId(),
                lot.getLotId()
            );

        if (!existingInventory.isPresent()) {
            throw new IllegalStateException("Inventory not found for LOT: " + lot.getLotNo());
        }

        InventoryEntity inventory = existingInventory.get();

        // Subtract quantity
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(quantity));
        inventory.setLastTransactionDate(LocalDateTime.now());
        inventory.setLastTransactionType(transactionType);

        // Update LOT current quantity
        lot.setCurrentQuantity(lot.getCurrentQuantity().subtract(quantity));

        inventoryRepository.save(inventory);
        lotRepository.save(lot);

        log.info("Updated inventory balance for product: {} in warehouse: {}, available: {}",
            product.getProductCode(), warehouse.getWarehouseCode(), inventory.getAvailableQuantity());
    }

    /**
     * Create handover record
     */
    private void createHandoverRecord(MaterialRequestEntity request, MaterialRequestItemEntity item,
                                     LotEntity lot, InventoryTransactionEntity transaction, UserEntity issuer) {
        String handoverNo = String.format("HO-%s-%03d",
            request.getRequestNo(),
            request.getItems().indexOf(item) + 1);

        MaterialHandoverEntity handover = MaterialHandoverEntity.builder()
            .tenant(request.getTenant())
            .materialRequest(request)
            .materialRequestItem(item)
            .inventoryTransaction(transaction)
            .handoverNo(handoverNo)
            .handoverDate(LocalDateTime.now())
            .product(item.getProduct())
            .lot(lot)
            .lotNo(lot.getLotNo())
            .quantity(item.getApprovedQuantity())
            .unit(item.getUnit())
            .issuer(issuer)
            .issuerName(issuer.getFullName())
            .issueLocation(request.getWarehouse().getWarehouseName())
            .receiver(request.getRequester())
            .receiverName(request.getRequesterName())
            .receiveLocation(request.getRequesterDepartment())
            .handoverStatus("PENDING")
            .remarks("Material handover for request: " + request.getRequestNo())
            .build();

        materialHandoverRepository.save(handover);
        log.info("Created handover record: {}", handoverNo);
    }
}
