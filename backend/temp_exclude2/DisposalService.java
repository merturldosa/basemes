package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Disposal Service
 * 폐기 관리 서비스
 *
 * 핵심 기능:
 * - 폐기 의뢰 생성
 * - 폐기 승인/거부
 * - 폐기 처리 (재고 차감)
 * - 폐기 완료
 *
 * 워크플로우:
 * PENDING → APPROVED → PROCESSED → COMPLETED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisposalService {

    private final DisposalRepository disposalRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;

    /**
     * Find all disposals by tenant
     */
    public List<DisposalEntity> findByTenant(String tenantId) {
        return disposalRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find disposal by ID
     */
    public Optional<DisposalEntity> findById(Long disposalId) {
        return disposalRepository.findByIdWithAllRelations(disposalId);
    }

    /**
     * Find disposals by status
     */
    public List<DisposalEntity> findByStatus(String tenantId, String status) {
        return disposalRepository.findByTenantIdAndStatusWithRelations(tenantId, status);
    }

    /**
     * Find disposals by type
     */
    public List<DisposalEntity> findByType(String tenantId, String type) {
        return disposalRepository.findByTenantIdAndTypeWithRelations(tenantId, type);
    }

    /**
     * Find disposals by warehouse
     */
    public List<DisposalEntity> findByWarehouseId(String tenantId, Long warehouseId) {
        return disposalRepository.findByTenant_TenantIdAndWarehouse_WarehouseId(tenantId, warehouseId);
    }

    /**
     * Find pending disposals by warehouse
     */
    public List<DisposalEntity> findPendingByWarehouse(Long warehouseId) {
        return disposalRepository.findPendingDisposalsByWarehouse(warehouseId);
    }

    /**
     * Find approved disposals requiring processing
     */
    public List<DisposalEntity> findApprovedDisposals(String tenantId) {
        return disposalRepository.findApprovedDisposals(tenantId);
    }

    /**
     * Create disposal (폐기 의뢰 생성)
     */
    @Transactional
    public DisposalEntity createDisposal(DisposalEntity disposal) {
        log.info("Creating disposal for tenant: {}", disposal.getTenant().getTenantId());

        // Generate disposal number
        if (disposal.getDisposalNo() == null || disposal.getDisposalNo().isEmpty()) {
            disposal.setDisposalNo(generateDisposalNo(disposal.getTenant().getTenantId()));
        }

        disposal.setDisposalStatus("PENDING");

        if (disposal.getRequester() != null && disposal.getRequesterName() == null) {
            disposal.setRequesterName(disposal.getRequester().getFullName());
        }

        // Set product codes and names
        for (DisposalItemEntity item : disposal.getItems()) {
            if (item.getProduct() != null) {
                item.setProductCode(item.getProduct().getProductCode());
                item.setProductName(item.getProduct().getProductName());
            }
            if (item.getLot() != null && item.getLotNo() == null) {
                item.setLotNo(item.getLot().getLotNo());
            }
        }

        disposal.calculateTotals();

        DisposalEntity saved = disposalRepository.save(disposal);
        log.info("Created disposal: {}", saved.getDisposalNo());

        return disposalRepository.findByIdWithAllRelations(saved.getDisposalId()).orElse(saved);
    }

    /**
     * Approve disposal
     */
    @Transactional
    public DisposalEntity approveDisposal(Long disposalId, Long approverId) {
        log.info("Approving disposal: {} by user: {}", disposalId, approverId);

        DisposalEntity disposal = disposalRepository.findByIdWithAllRelations(disposalId)
                .orElseThrow(() -> new IllegalArgumentException("Disposal not found: " + disposalId));

        if (!"PENDING".equals(disposal.getDisposalStatus())) {
            throw new IllegalStateException("Cannot approve disposal in status: " + disposal.getDisposalStatus());
        }

        UserEntity approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + approverId));

        disposal.setApprover(approver);
        disposal.setApproverName(approver.getFullName());
        disposal.setApprovedDate(LocalDateTime.now());
        disposal.setDisposalStatus("APPROVED");

        DisposalEntity approved = disposalRepository.save(disposal);
        log.info("Approved disposal: {}", approved.getDisposalNo());

        return disposalRepository.findByIdWithAllRelations(approved.getDisposalId()).orElse(approved);
    }

    /**
     * Reject disposal
     */
    @Transactional
    public DisposalEntity rejectDisposal(Long disposalId, Long approverId, String reason) {
        log.info("Rejecting disposal: {} by user: {}, reason: {}", disposalId, approverId, reason);

        DisposalEntity disposal = disposalRepository.findByIdWithAllRelations(disposalId)
                .orElseThrow(() -> new IllegalArgumentException("Disposal not found: " + disposalId));

        if (!"PENDING".equals(disposal.getDisposalStatus())) {
            throw new IllegalStateException("Cannot reject disposal in status: " + disposal.getDisposalStatus());
        }

        UserEntity approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + approverId));

        disposal.setApprover(approver);
        disposal.setApproverName(approver.getFullName());
        disposal.setApprovedDate(LocalDateTime.now());
        disposal.setRejectionReason(reason);
        disposal.setDisposalStatus("REJECTED");

        DisposalEntity rejected = disposalRepository.save(disposal);
        log.info("Rejected disposal: {}", rejected.getDisposalNo());

        return disposalRepository.findByIdWithAllRelations(rejected.getDisposalId()).orElse(rejected);
    }

    /**
     * Process disposal (재고 차감)
     */
    @Transactional
    public DisposalEntity processDisposal(Long disposalId, Long processorUserId) {
        log.info("Processing disposal: {} by user: {}", disposalId, processorUserId);

        DisposalEntity disposal = disposalRepository.findByIdWithAllRelations(disposalId)
                .orElseThrow(() -> new IllegalArgumentException("Disposal not found: " + disposalId));

        if (!"APPROVED".equals(disposal.getDisposalStatus())) {
            throw new IllegalStateException("Cannot process disposal in status: " + disposal.getDisposalStatus());
        }

        UserEntity processor = userRepository.findById(processorUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + processorUserId));

        // Process each item - deduct from inventory
        for (DisposalItemEntity item : disposal.getItems()) {
            processDisposalItem(disposal, item, processor);
        }

        disposal.setProcessor(processor);
        disposal.setProcessorName(processor.getFullName());
        disposal.setProcessedDate(LocalDateTime.now());
        disposal.setDisposalStatus("PROCESSED");

        DisposalEntity processed = disposalRepository.save(disposal);
        log.info("Processed disposal: {}", processed.getDisposalNo());

        return disposalRepository.findByIdWithAllRelations(processed.getDisposalId()).orElse(processed);
    }

    /**
     * Complete disposal
     */
    @Transactional
    public DisposalEntity completeDisposal(Long disposalId, String method, String location) {
        log.info("Completing disposal: {}", disposalId);

        DisposalEntity disposal = disposalRepository.findByIdWithAllRelations(disposalId)
                .orElseThrow(() -> new IllegalArgumentException("Disposal not found: " + disposalId));

        if (!"PROCESSED".equals(disposal.getDisposalStatus())) {
            throw new IllegalStateException("Cannot complete disposal in status: " + disposal.getDisposalStatus());
        }

        disposal.setCompletedDate(LocalDateTime.now());
        disposal.setDisposalMethod(method);
        disposal.setDisposalLocation(location);
        disposal.setDisposalStatus("COMPLETED");

        DisposalEntity completed = disposalRepository.save(disposal);
        log.info("Completed disposal: {}", completed.getDisposalNo());

        return disposalRepository.findByIdWithAllRelations(completed.getDisposalId()).orElse(completed);
    }

    /**
     * Cancel disposal
     */
    @Transactional
    public DisposalEntity cancelDisposal(Long disposalId, String reason) {
        log.info("Cancelling disposal: {}, reason: {}", disposalId, reason);

        DisposalEntity disposal = disposalRepository.findByIdWithAllRelations(disposalId)
                .orElseThrow(() -> new IllegalArgumentException("Disposal not found: " + disposalId));

        if (!"PENDING".equals(disposal.getDisposalStatus()) &&
            !"APPROVED".equals(disposal.getDisposalStatus())) {
            throw new IllegalStateException("Cannot cancel disposal in status: " + disposal.getDisposalStatus());
        }

        disposal.setCancellationReason(reason);
        disposal.setDisposalStatus("CANCELLED");

        DisposalEntity cancelled = disposalRepository.save(disposal);
        log.info("Cancelled disposal: {}", cancelled.getDisposalNo());

        return disposalRepository.findByIdWithAllRelations(cancelled.getDisposalId()).orElse(cancelled);
    }

    // ================== Private Helper Methods ==================

    private String generateDisposalNo(String tenantId) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "DIS-" + datePrefix + "-";

        long count = disposalRepository.findByTenant_TenantId(tenantId).stream()
                .filter(d -> d.getDisposalNo().startsWith(prefix))
                .count();

        return prefix + String.format("%04d", count + 1);
    }

    private void processDisposalItem(DisposalEntity disposal, DisposalItemEntity item, UserEntity processor) {
        // Set processed quantity
        if (item.getProcessedQuantity() == null) {
            item.setProcessedQuantity(item.getDisposalQuantity());
        }

        // Create disposal transaction
        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
                .tenant(disposal.getTenant())
                .transactionNo(generateTransactionNo(disposal.getTenant().getTenantId()))
                .transactionDate(LocalDateTime.now())
                .transactionType("OUT_DISPOSAL")
                .warehouse(disposal.getWarehouse())
                .product(item.getProduct())
                .lot(item.getLot())
                .quantity(item.getProcessedQuantity().negate()) // Negative for disposal
                .referenceType("DISPOSAL")
                .referenceId(disposal.getDisposalId().toString())
                .approvalStatus("APPROVED")
                .user(processor)
                .remarks("Disposal: " + disposal.getDisposalNo())
                .isActive(true)
                .build();

        InventoryTransactionEntity savedTransaction = inventoryTransactionRepository.save(transaction);
        item.setDisposalTransaction(savedTransaction);

        // Deduct from inventory
        deductInventory(disposal.getWarehouse().getWarehouseId(),
                item.getProduct().getProductId(),
                item.getLot() != null ? item.getLot().getLotId() : null,
                item.getProcessedQuantity());

        // Update LOT status if applicable
        if (item.getLot() != null) {
            LotEntity lot = item.getLot();
            lot.setQuantity(lot.getQuantity().subtract(item.getProcessedQuantity()));
            if (lot.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                lot.setIsActive(false);
            }
            lotRepository.save(lot);
        }

        log.info("Processed disposal item: {} units of {} from warehouse {}, LOT: {}",
                item.getProcessedQuantity(), item.getProductCode(),
                disposal.getWarehouse().getWarehouseCode(),
                item.getLotNo() != null ? item.getLotNo() : "N/A");
    }

    private void deductInventory(Long warehouseId, Long productId, Long lotId, BigDecimal quantity) {
        Optional<InventoryEntity> inventoryOpt;

        if (lotId != null) {
            inventoryOpt = inventoryRepository
                    .findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(warehouseId, productId, lotId);
        } else {
            inventoryOpt = inventoryRepository
                    .findByWarehouse_WarehouseIdAndProduct_ProductId(warehouseId, productId).stream()
                    .findFirst();
        }

        if (inventoryOpt.isPresent()) {
            InventoryEntity inventory = inventoryOpt.get();
            inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(quantity));
            inventory.setLastTransactionDate(LocalDateTime.now());
            inventory.setLastTransactionType("OUT_DISPOSAL");

            if (inventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Insufficient inventory for disposal");
            }

            if (inventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) == 0 &&
                inventory.getReservedQuantity().compareTo(BigDecimal.ZERO) == 0) {
                inventory.setIsActive(false);
            }

            inventoryRepository.save(inventory);
        } else {
            throw new IllegalArgumentException("Inventory not found for disposal");
        }
    }

    private String generateTransactionNo(String tenantId) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "IT-" + datePrefix + "-";

        long count = inventoryTransactionRepository.findByTenant_TenantId(tenantId).stream()
                .filter(t -> t.getTransactionNo().startsWith(prefix))
                .count();

        return prefix + String.format("%04d", count + 1);
    }
}
