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
import java.util.Optional;

/**
 * Inventory Transaction Service
 * 재고 이동 내역 서비스
 *
 * 핵심 기능:
 * - 재고 트랜잭션 생성 및 관리
 * - 승인 워크플로우 (PENDING → APPROVED/REJECTED)
 * - 재고 잔액 자동 업데이트
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryRepository inventoryRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;

    public List<InventoryTransactionEntity> findByTenant(String tenantId) {
        return inventoryTransactionRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public Optional<InventoryTransactionEntity> findById(Long transactionId) {
        return inventoryTransactionRepository.findByIdWithAllRelations(transactionId);
    }

    public List<InventoryTransactionEntity> findByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return inventoryTransactionRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    public List<InventoryTransactionEntity> findByApprovalStatus(String tenantId, String approvalStatus) {
        return inventoryTransactionRepository.findByTenant_TenantIdAndApprovalStatus(tenantId, approvalStatus);
    }

    @Transactional
    public InventoryTransactionEntity createTransaction(InventoryTransactionEntity transaction) {
        log.info("Creating inventory transaction: {} type: {}",
            transaction.getTransactionNo(), transaction.getTransactionType());

        if (inventoryTransactionRepository.existsByTenantAndTransactionNo(
            transaction.getTenant(), transaction.getTransactionNo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Transaction number already exists: " + transaction.getTransactionNo());
        }

        // Auto-approve for now (can be changed to require approval)
        transaction.setApprovalStatus("APPROVED");
        transaction.setApprovedDate(LocalDateTime.now());

        InventoryTransactionEntity saved = inventoryTransactionRepository.save(transaction);

        // Update inventory based on transaction type
        updateInventory(saved);

        return inventoryTransactionRepository.findByIdWithAllRelations(saved.getTransactionId()).orElse(saved);
    }

    /**
     * Update inventory based on transaction
     */
    private void updateInventory(InventoryTransactionEntity transaction) {
        String type = transaction.getTransactionType();

        if (type.startsWith("IN_")) {
            // Increase inventory
            increaseInventory(transaction);
        } else if (type.startsWith("OUT_")) {
            // Decrease inventory
            decreaseInventory(transaction);
        } else if ("MOVE".equals(type)) {
            // Move between warehouses
            moveInventory(transaction);
        } else if ("ADJUST".equals(type)) {
            // Adjust inventory
            adjustInventory(transaction);
        }
    }

    private void increaseInventory(InventoryTransactionEntity transaction) {
        InventoryEntity inventory = findOrCreateInventory(
            transaction.getTenant(),
            transaction.getWarehouse(),
            transaction.getProduct(),
            transaction.getLot()
        );

        BigDecimal newQuantity = inventory.getAvailableQuantity().add(transaction.getQuantity());
        inventory.setAvailableQuantity(newQuantity);
        inventory.setLastTransactionDate(transaction.getTransactionDate());
        inventory.setLastTransactionType(transaction.getTransactionType());
        inventoryRepository.save(inventory);

        // Update lot quantity if applicable
        if (transaction.getLot() != null) {
            LotEntity lot = transaction.getLot();
            lot.setCurrentQuantity(lot.getCurrentQuantity().add(transaction.getQuantity()));
            lotRepository.save(lot);
        }

        log.info("Increased inventory for product: {} by {}",
            transaction.getProduct().getProductCode(), transaction.getQuantity());
    }

    private void decreaseInventory(InventoryTransactionEntity transaction) {
        InventoryEntity inventory = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                transaction.getTenant().getTenantId(),
                transaction.getWarehouse().getWarehouseId(),
                transaction.getProduct().getProductId(),
                transaction.getLot() != null ? transaction.getLot().getLotId() : null
            )
            .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND, "Inventory not found"));

        BigDecimal newQuantity = inventory.getAvailableQuantity().subtract(transaction.getQuantity());
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY, "Insufficient inventory");
        }

        inventory.setAvailableQuantity(newQuantity);
        inventory.setLastTransactionDate(transaction.getTransactionDate());
        inventory.setLastTransactionType(transaction.getTransactionType());
        inventoryRepository.save(inventory);

        // Update lot quantity if applicable
        if (transaction.getLot() != null) {
            LotEntity lot = transaction.getLot();
            lot.setCurrentQuantity(lot.getCurrentQuantity().subtract(transaction.getQuantity()));
            lotRepository.save(lot);
        }

        log.info("Decreased inventory for product: {} by {}",
            transaction.getProduct().getProductCode(), transaction.getQuantity());
    }

    private void moveInventory(InventoryTransactionEntity transaction) {
        // Decrease from source warehouse
        decreaseInventory(transaction);

        // Increase to destination warehouse
        InventoryEntity toInventory = findOrCreateInventory(
            transaction.getTenant(),
            transaction.getToWarehouse(),
            transaction.getProduct(),
            transaction.getLot()
        );

        BigDecimal newQuantity = toInventory.getAvailableQuantity().add(transaction.getQuantity());
        toInventory.setAvailableQuantity(newQuantity);
        toInventory.setLastTransactionDate(transaction.getTransactionDate());
        toInventory.setLastTransactionType("MOVE_IN");
        inventoryRepository.save(toInventory);

        log.info("Moved inventory for product: {} from warehouse: {} to warehouse: {}",
            transaction.getProduct().getProductCode(),
            transaction.getFromWarehouse().getWarehouseCode(),
            transaction.getToWarehouse().getWarehouseCode());
    }

    private void adjustInventory(InventoryTransactionEntity transaction) {
        InventoryEntity inventory = findOrCreateInventory(
            transaction.getTenant(),
            transaction.getWarehouse(),
            transaction.getProduct(),
            transaction.getLot()
        );

        // Adjust to exact quantity
        inventory.setAvailableQuantity(transaction.getQuantity());
        inventory.setLastTransactionDate(transaction.getTransactionDate());
        inventory.setLastTransactionType(transaction.getTransactionType());
        inventoryRepository.save(inventory);

        log.info("Adjusted inventory for product: {} to {}",
            transaction.getProduct().getProductCode(), transaction.getQuantity());
    }

    private InventoryEntity findOrCreateInventory(TenantEntity tenant, WarehouseEntity warehouse,
                                                  ProductEntity product, LotEntity lot) {
        return inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenant.getTenantId(),
                warehouse.getWarehouseId(),
                product.getProductId(),
                lot != null ? lot.getLotId() : null
            )
            .orElseGet(() -> {
                InventoryEntity newInventory = InventoryEntity.builder()
                    .tenant(tenant)
                    .warehouse(warehouse)
                    .product(product)
                    .lot(lot)
                    .availableQuantity(BigDecimal.ZERO)
                    .reservedQuantity(BigDecimal.ZERO)
                    .unit(product.getUnit())
                    .build();
                return inventoryRepository.save(newInventory);
            });
    }

    // ================== Approval Workflow Methods ==================

    /**
     * Approve transaction
     * 트랜잭션 승인
     *
     * 상태 검증 (PENDING만 승인 가능)
     * 승인 정보 업데이트
     * 재고 잔액 업데이트 실행
     */
    @Transactional
    public InventoryTransactionEntity approveTransaction(Long transactionId, Long approverId) {
        log.info("Approving transaction: {} by user: {}", transactionId, approverId);

        InventoryTransactionEntity transaction = inventoryTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_TRANSACTION_NOT_FOUND, "Transaction not found: " + transactionId));

        // Validate status (only PENDING can be approved)
        if (!"PENDING".equals(transaction.getApprovalStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                "Cannot approve transaction in status: " + transaction.getApprovalStatus());
        }

        // Find approver user
        UserEntity approver = userRepository.findById(approverId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found: " + approverId));

        // Update approval status
        transaction.setApprovalStatus("APPROVED");
        transaction.setApprovedBy(approver);
        transaction.setApprovedDate(LocalDateTime.now());

        InventoryTransactionEntity saved = inventoryTransactionRepository.save(transaction);

        // Execute inventory update now that it's approved
        updateInventory(saved);

        log.info("Approved transaction: {} by user: {}", transactionId, approver.getUsername());

        return inventoryTransactionRepository.findByIdWithAllRelations(saved.getTransactionId()).orElse(saved);
    }

    /**
     * Reject transaction
     * 트랜잭션 거부
     *
     * 상태를 REJECTED로 변경
     * 재고 업데이트 없음
     */
    @Transactional
    public InventoryTransactionEntity rejectTransaction(Long transactionId, Long approverId, String reason) {
        log.info("Rejecting transaction: {} by user: {}, reason: {}",
            transactionId, approverId, reason);

        InventoryTransactionEntity transaction = inventoryTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_TRANSACTION_NOT_FOUND, "Transaction not found: " + transactionId));

        // Validate status (only PENDING can be rejected)
        if (!"PENDING".equals(transaction.getApprovalStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                "Cannot reject transaction in status: " + transaction.getApprovalStatus());
        }

        // Find approver user
        UserEntity approver = userRepository.findById(approverId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found: " + approverId));

        // Update approval status
        transaction.setApprovalStatus("REJECTED");
        transaction.setApprovedBy(approver);
        transaction.setApprovedDate(LocalDateTime.now());
        transaction.setRemarks(transaction.getRemarks() != null ?
            transaction.getRemarks() + " | Rejected: " + reason : "Rejected: " + reason);

        InventoryTransactionEntity saved = inventoryTransactionRepository.save(transaction);

        log.info("Rejected transaction: {} by user: {}", transactionId, approver.getUsername());

        return inventoryTransactionRepository.findByIdWithAllRelations(saved.getTransactionId()).orElse(saved);
    }

    /**
     * Create transaction with pending approval
     * 승인 대기 상태로 트랜잭션 생성
     *
     * 재고 조정(ADJUST) 등 승인이 필요한 트랜잭션에 사용
     */
    @Transactional
    public InventoryTransactionEntity createTransactionPending(InventoryTransactionEntity transaction) {
        log.info("Creating pending transaction: {} type: {}",
            transaction.getTransactionNo(), transaction.getTransactionType());

        if (inventoryTransactionRepository.existsByTenantAndTransactionNo(
            transaction.getTenant(), transaction.getTransactionNo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Transaction number already exists: " + transaction.getTransactionNo());
        }

        // Set to PENDING for approval
        transaction.setApprovalStatus("PENDING");

        InventoryTransactionEntity saved = inventoryTransactionRepository.save(transaction);

        log.info("Created pending transaction: {}, awaiting approval", saved.getTransactionNo());

        return inventoryTransactionRepository.findByIdWithAllRelations(saved.getTransactionId()).orElse(saved);
    }
}
