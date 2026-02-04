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
 * Return Service
 * 반품 관리 서비스
 *
 * 핵심 기능:
 * - 반품 신청 생성
 * - 반품 승인/거부
 * - 반품 입고
 * - 품질 검사 연동
 * - 재고 복원 (합격품 재입고, 불합격품 격리)
 *
 * 워크플로우:
 * PENDING → APPROVED → RECEIVED → INSPECTING → COMPLETED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final WarehouseRepository warehouseRepository;
    private final LotRepository lotRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final QualityStandardRepository qualityStandardRepository;
    private final UserRepository userRepository;

    /**
     * Find all returns by tenant
     */
    public List<ReturnEntity> findByTenant(String tenantId) {
        return returnRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find return by ID
     */
    public Optional<ReturnEntity> findById(Long returnId) {
        return returnRepository.findByIdWithAllRelations(returnId);
    }

    /**
     * Find returns by status
     */
    public List<ReturnEntity> findByStatus(String tenantId, String status) {
        return returnRepository.findByTenantIdAndStatusWithRelations(tenantId, status);
    }

    /**
     * Find returns by type
     */
    public List<ReturnEntity> findByType(String tenantId, String type) {
        return returnRepository.findByTenantIdAndTypeWithRelations(tenantId, type);
    }

    /**
     * Find returns by warehouse
     */
    public List<ReturnEntity> findByWarehouseId(String tenantId, Long warehouseId) {
        return returnRepository.findByTenant_TenantIdAndWarehouse_WarehouseId(tenantId, warehouseId);
    }

    /**
     * Find returns by material request
     */
    public List<ReturnEntity> findByMaterialRequestId(String tenantId, Long materialRequestId) {
        return returnRepository.findByTenant_TenantIdAndMaterialRequest_MaterialRequestId(tenantId, materialRequestId);
    }

    /**
     * Find returns by work order
     */
    public List<ReturnEntity> findByWorkOrderId(String tenantId, Long workOrderId) {
        return returnRepository.findByTenant_TenantIdAndWorkOrder_WorkOrderId(tenantId, workOrderId);
    }

    /**
     * Find pending returns by warehouse
     */
    public List<ReturnEntity> findPendingByWarehouse(Long warehouseId) {
        return returnRepository.findPendingReturnsByWarehouse(warehouseId);
    }

    /**
     * Find returns requiring inspection
     */
    public List<ReturnEntity> findRequiringInspection(String tenantId) {
        return returnRepository.findReturnsRequiringInspection(tenantId);
    }

    /**
     * Create return (반품 신청 생성)
     *
     * 워크플로우:
     * 1. 반품번호 자동 생성 (RT-YYYYMMDD-0001)
     * 2. 상태: PENDING
     * 3. 항목별 검사 상태 설정
     * 4. 합계 계산
     * 5. 저장
     */
    @Transactional
    public ReturnEntity createReturn(ReturnEntity returnEntity) {
        log.info("Creating return for tenant: {}", returnEntity.getTenant().getTenantId());

        // Generate return number if not provided
        if (returnEntity.getReturnNo() == null || returnEntity.getReturnNo().isEmpty()) {
            returnEntity.setReturnNo(generateReturnNo(returnEntity.getTenant().getTenantId()));
        }

        // Set default status
        returnEntity.setReturnStatus("PENDING");

        // Set requester name
        if (returnEntity.getRequester() != null && returnEntity.getRequesterName() == null) {
            returnEntity.setRequesterName(returnEntity.getRequester().getFullName());
        }

        // Set product codes and names for items
        for (ReturnItemEntity item : returnEntity.getItems()) {
            if (item.getProduct() != null) {
                item.setProductCode(item.getProduct().getProductCode());
                item.setProductName(item.getProduct().getProductName());
            }

            // Set default inspection status based on return type
            if (item.getInspectionStatus() == null) {
                if ("DEFECTIVE".equals(returnEntity.getReturnType())) {
                    item.setInspectionStatus("PENDING"); // 불량품은 검사 필요
                } else {
                    item.setInspectionStatus("NOT_REQUIRED"); // 과잉/오배송은 검사 불필요
                }
            }
        }

        // Calculate totals
        returnEntity.calculateTotals();

        ReturnEntity saved = returnRepository.save(returnEntity);
        log.info("Created return: {}", saved.getReturnNo());

        return returnRepository.findByIdWithAllRelations(saved.getReturnId())
                .orElse(saved);
    }

    /**
     * Approve return (반품 승인)
     *
     * 워크플로우:
     * 1. 상태 검증 (PENDING만 승인 가능)
     * 2. 승인 정보 업데이트
     * 3. 상태 → APPROVED
     */
    @Transactional
    public ReturnEntity approveReturn(Long returnId, Long approverId) {
        log.info("Approving return: {} by user: {}", returnId, approverId);

        ReturnEntity returnEntity = returnRepository.findByIdWithAllRelations(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

        // Validate status
        if (!"PENDING".equals(returnEntity.getReturnStatus())) {
            throw new IllegalStateException("Cannot approve return in status: " + returnEntity.getReturnStatus());
        }

        // Update approver
        UserEntity approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + approverId));

        returnEntity.setApprover(approver);
        returnEntity.setApproverName(approver.getFullName());
        returnEntity.setApprovedDate(LocalDateTime.now());
        returnEntity.setReturnStatus("APPROVED");

        ReturnEntity approved = returnRepository.save(returnEntity);
        log.info("Approved return: {}", approved.getReturnNo());

        return returnRepository.findByIdWithAllRelations(approved.getReturnId())
                .orElse(approved);
    }

    /**
     * Reject return (반품 거부)
     *
     * 워크플로우:
     * 1. 상태 검증 (PENDING만 거부 가능)
     * 2. 거부 사유 저장
     * 3. 상태 → REJECTED
     */
    @Transactional
    public ReturnEntity rejectReturn(Long returnId, Long approverId, String reason) {
        log.info("Rejecting return: {} by user: {}, reason: {}", returnId, approverId, reason);

        ReturnEntity returnEntity = returnRepository.findByIdWithAllRelations(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

        // Validate status
        if (!"PENDING".equals(returnEntity.getReturnStatus())) {
            throw new IllegalStateException("Cannot reject return in status: " + returnEntity.getReturnStatus());
        }

        // Update rejection info
        UserEntity approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + approverId));

        returnEntity.setApprover(approver);
        returnEntity.setApproverName(approver.getFullName());
        returnEntity.setApprovedDate(LocalDateTime.now());
        returnEntity.setRejectionReason(reason);
        returnEntity.setReturnStatus("REJECTED");

        ReturnEntity rejected = returnRepository.save(returnEntity);
        log.info("Rejected return: {}", rejected.getReturnNo());

        return returnRepository.findByIdWithAllRelations(rejected.getReturnId())
                .orElse(rejected);
    }

    /**
     * Receive return (반품 입고)
     *
     * 워크플로우:
     * 1. 상태 검증 (APPROVED만 입고 가능)
     * 2. 항목별 입고 처리:
     *    a. 재고 트랜잭션 생성 (IN_RETURN)
     *    b. 검사 필요 시 품질 검사 요청 생성
     * 3. 상태 → RECEIVED (검사 필요 시 INSPECTING)
     * 4. receivedDate 업데이트
     */
    @Transactional
    public ReturnEntity receiveReturn(Long returnId, Long receiverUserId) {
        log.info("Receiving return: {} by user: {}", returnId, receiverUserId);

        ReturnEntity returnEntity = returnRepository.findByIdWithAllRelations(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

        // Validate status
        if (!"APPROVED".equals(returnEntity.getReturnStatus())) {
            throw new IllegalStateException("Cannot receive return in status: " + returnEntity.getReturnStatus());
        }

        UserEntity receiver = userRepository.findById(receiverUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + receiverUserId));

        boolean requiresInspection = false;

        // Process each item
        for (ReturnItemEntity item : returnEntity.getItems()) {
            // Set received quantity (assuming full return for now)
            if (item.getReceivedQuantity() == null) {
                item.setReceivedQuantity(item.getReturnQuantity());
            }

            // Create inventory transaction (IN_RETURN)
            InventoryTransactionEntity transaction = createReturnReceiveTransaction(
                returnEntity, item, receiver);
            item.setReceiveTransaction(transaction);

            // Create quality inspection if needed
            if ("PENDING".equals(item.getInspectionStatus())) {
                createReturnInspection(returnEntity, item);
                requiresInspection = true;
            }
        }

        // Update status
        returnEntity.setReceivedDate(LocalDateTime.now());
        returnEntity.setReturnStatus(requiresInspection ? "INSPECTING" : "RECEIVED");

        // Recalculate totals
        returnEntity.calculateTotals();

        ReturnEntity received = returnRepository.save(returnEntity);
        log.info("Received return: {}, status: {}", received.getReturnNo(), received.getReturnStatus());

        return returnRepository.findByIdWithAllRelations(received.getReturnId())
                .orElse(received);
    }

    /**
     * Complete return (재고 복원 완료)
     *
     * 워크플로우:
     * 1. 상태 검증 (RECEIVED/INSPECTING만 완료 가능)
     * 2. 항목별 재고 복원:
     *    a. 합격품: 원래 창고에 재입고
     *    b. 불합격품: 격리 창고로 이동
     * 3. 상태 → COMPLETED
     * 4. completedDate 업데이트
     */
    @Transactional
    public ReturnEntity completeReturn(Long returnId) {
        log.info("Completing return: {}", returnId);

        ReturnEntity returnEntity = returnRepository.findByIdWithAllRelations(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

        // Validate status
        if (!"RECEIVED".equals(returnEntity.getReturnStatus()) &&
            !"INSPECTING".equals(returnEntity.getReturnStatus())) {
            throw new IllegalStateException("Cannot complete return in status: " + returnEntity.getReturnStatus());
        }

        // Get quarantine warehouse for failed items
        WarehouseEntity quarantineWarehouse = warehouseRepository
                .findByTenant_TenantIdAndWarehouseType(
                    returnEntity.getTenant().getTenantId(), "QUARANTINE")
                .stream()
                .findFirst()
                .orElse(null);

        // Process each item
        for (ReturnItemEntity item : returnEntity.getItems()) {
            if (item.getReceivedQuantity() == null || item.getReceivedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // Determine passed/failed quantities
            if ("PASS".equals(item.getInspectionStatus())) {
                // All passed - restore to original warehouse
                item.setPassedQuantity(item.getReceivedQuantity());
                item.setFailedQuantity(BigDecimal.ZERO);
                restoreInventory(returnEntity, item, item.getPassedQuantity());
            } else if ("FAIL".equals(item.getInspectionStatus())) {
                // All failed - move to quarantine
                item.setPassedQuantity(BigDecimal.ZERO);
                item.setFailedQuantity(item.getReceivedQuantity());
                if (quarantineWarehouse != null) {
                    moveToQuarantine(returnEntity, item, item.getFailedQuantity(), quarantineWarehouse);
                }
            } else {
                // No inspection or not required - restore all
                item.setPassedQuantity(item.getReceivedQuantity());
                item.setFailedQuantity(BigDecimal.ZERO);
                restoreInventory(returnEntity, item, item.getPassedQuantity());
            }
        }

        // Update status
        returnEntity.setCompletedDate(LocalDateTime.now());
        returnEntity.setReturnStatus("COMPLETED");

        // Recalculate totals
        returnEntity.calculateTotals();

        ReturnEntity completed = returnRepository.save(returnEntity);
        log.info("Completed return: {}", completed.getReturnNo());

        return returnRepository.findByIdWithAllRelations(completed.getReturnId())
                .orElse(completed);
    }

    /**
     * Cancel return (반품 취소)
     *
     * 워크플로우:
     * 1. 상태 검증 (PENDING/APPROVED만 취소 가능)
     * 2. 취소 사유 저장
     * 3. 상태 → CANCELLED
     */
    @Transactional
    public ReturnEntity cancelReturn(Long returnId, String reason) {
        log.info("Cancelling return: {}, reason: {}", returnId, reason);

        ReturnEntity returnEntity = returnRepository.findByIdWithAllRelations(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

        // Validate status
        if (!"PENDING".equals(returnEntity.getReturnStatus()) &&
            !"APPROVED".equals(returnEntity.getReturnStatus())) {
            throw new IllegalStateException("Cannot cancel return in status: " + returnEntity.getReturnStatus());
        }

        returnEntity.setCancellationReason(reason);
        returnEntity.setReturnStatus("CANCELLED");

        ReturnEntity cancelled = returnRepository.save(returnEntity);
        log.info("Cancelled return: {}", cancelled.getReturnNo());

        return returnRepository.findByIdWithAllRelations(cancelled.getReturnId())
                .orElse(cancelled);
    }

    // ================== Private Helper Methods ==================

    /**
     * Generate return number: RT-YYYYMMDD-0001
     */
    private String generateReturnNo(String tenantId) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RT-" + datePrefix + "-";

        long count = returnRepository.findByTenant_TenantId(tenantId).stream()
                .filter(r -> r.getReturnNo().startsWith(prefix))
                .count();

        return prefix + String.format("%04d", count + 1);
    }

    /**
     * Create inventory transaction for return receipt
     */
    private InventoryTransactionEntity createReturnReceiveTransaction(
            ReturnEntity returnEntity, ReturnItemEntity item, UserEntity receiver) {

        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
                .tenant(returnEntity.getTenant())
                .transactionNo(generateTransactionNo(returnEntity.getTenant().getTenantId()))
                .transactionDate(LocalDateTime.now())
                .transactionType("IN_RETURN") // 반품 입고
                .warehouse(returnEntity.getWarehouse())
                .product(item.getProduct())
                .quantity(item.getReceivedQuantity())
                .referenceType("RETURN")
                .referenceId(returnEntity.getReturnId().toString())
                .approvalStatus("PENDING")
                .user(receiver)
                .remarks("Return: " + returnEntity.getReturnNo())
                .isActive(true)
                .build();

        return inventoryTransactionRepository.save(transaction);
    }

    /**
     * Create quality inspection for return item
     */
    private void createReturnInspection(ReturnEntity returnEntity, ReturnItemEntity item) {
        QualityInspectionEntity inspection = QualityInspectionEntity.builder()
                .tenant(returnEntity.getTenant())
                .inspectionNo(generateInspectionNo(returnEntity.getTenant().getTenantId()))
                .inspectionDate(LocalDateTime.now())
                .inspectionType("RETURN") // 반품 검사
                .product(item.getProduct())
                .inspectedQuantity(item.getReceivedQuantity())
                .inspectionResult("CONDITIONAL") // 검사 대기
                .remarks("Return inspection for: " + returnEntity.getReturnNo())
                .isActive(true)
                .build();

        QualityInspectionEntity saved = qualityInspectionRepository.save(inspection);
        item.setQualityInspection(saved);
        log.info("Created return inspection: {}", saved.getInspectionNo());
    }

    /**
     * Restore inventory (합격품 재입고)
     */
    private void restoreInventory(ReturnEntity returnEntity, ReturnItemEntity item, BigDecimal quantity) {
        // Create new LOT for returned items
        LotEntity newLot = LotEntity.builder()
                .tenant(returnEntity.getTenant())
                .lotNo(generateLotNo(returnEntity.getTenant().getTenantId()))
                .product(item.getProduct())
                .quantity(quantity)
                .qualityStatus("PASSED")
                .lotType("RETURN")
                .remarks("Returned from: " + returnEntity.getReturnNo())
                .isActive(true)
                .build();

        LotEntity savedLot = lotRepository.save(newLot);
        item.setNewLotNo(savedLot.getLotNo());

        // Create inventory transaction
        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
                .tenant(returnEntity.getTenant())
                .transactionNo(generateTransactionNo(returnEntity.getTenant().getTenantId()))
                .transactionDate(LocalDateTime.now())
                .transactionType("IN_RETURN_RESTORE")
                .warehouse(returnEntity.getWarehouse())
                .product(item.getProduct())
                .lot(savedLot)
                .quantity(quantity)
                .referenceType("RETURN")
                .referenceId(returnEntity.getReturnId().toString())
                .approvalStatus("APPROVED")
                .remarks("Restore inventory from return: " + returnEntity.getReturnNo())
                .isActive(true)
                .build();

        InventoryTransactionEntity savedTransaction = inventoryTransactionRepository.save(transaction);
        item.setPassTransaction(savedTransaction);

        // Update inventory balance
        updateInventoryBalance(returnEntity.getWarehouse().getWarehouseId(),
                item.getProduct().getProductId(), savedLot.getLotId(), quantity);

        log.info("Restored inventory: {} units of {} to warehouse {}, LOT: {}",
                quantity, item.getProductCode(), returnEntity.getWarehouse().getWarehouseCode(), savedLot.getLotNo());
    }

    /**
     * Move to quarantine warehouse (불합격품 격리)
     */
    private void moveToQuarantine(ReturnEntity returnEntity, ReturnItemEntity item,
                                   BigDecimal quantity, WarehouseEntity quarantineWarehouse) {

        // Create new LOT for quarantine
        LotEntity newLot = LotEntity.builder()
                .tenant(returnEntity.getTenant())
                .lotNo(generateLotNo(returnEntity.getTenant().getTenantId()))
                .product(item.getProduct())
                .quantity(quantity)
                .qualityStatus("FAILED")
                .lotType("QUARANTINE")
                .remarks("Return failed inspection: " + returnEntity.getReturnNo())
                .isActive(true)
                .build();

        LotEntity savedLot = lotRepository.save(newLot);

        // Create inventory transaction
        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
                .tenant(returnEntity.getTenant())
                .transactionNo(generateTransactionNo(returnEntity.getTenant().getTenantId()))
                .transactionDate(LocalDateTime.now())
                .transactionType("IN_QUARANTINE")
                .warehouse(quarantineWarehouse)
                .product(item.getProduct())
                .lot(savedLot)
                .quantity(quantity)
                .referenceType("RETURN")
                .referenceId(returnEntity.getReturnId().toString())
                .approvalStatus("APPROVED")
                .remarks("Quarantine from return: " + returnEntity.getReturnNo())
                .isActive(true)
                .build();

        InventoryTransactionEntity savedTransaction = inventoryTransactionRepository.save(transaction);
        item.setFailTransaction(savedTransaction);

        // Update quarantine inventory
        updateInventoryBalance(quarantineWarehouse.getWarehouseId(),
                item.getProduct().getProductId(), savedLot.getLotId(), quantity);

        log.info("Moved to quarantine: {} units of {} to warehouse {}, LOT: {}",
                quantity, item.getProductCode(), quarantineWarehouse.getWarehouseCode(), savedLot.getLotNo());
    }

    /**
     * Update inventory balance
     */
    private void updateInventoryBalance(Long warehouseId, Long productId, Long lotId, BigDecimal quantity) {
        Optional<InventoryEntity> existingInventory = inventoryRepository
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(warehouseId, productId, lotId);

        if (existingInventory.isPresent()) {
            InventoryEntity inventory = existingInventory.get();
            inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantity));
            inventory.setLastTransactionDate(LocalDateTime.now());
            inventory.setLastTransactionType("IN_RETURN");
            inventoryRepository.save(inventory);
        } else {
            // Create new inventory record
            WarehouseEntity warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + warehouseId));
            ProductEntity product = warehouse.getTenant().getProducts().stream()
                    .filter(p -> p.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
            LotEntity lot = lotRepository.findById(lotId)
                    .orElseThrow(() -> new IllegalArgumentException("LOT not found: " + lotId));

            InventoryEntity newInventory = InventoryEntity.builder()
                    .tenant(warehouse.getTenant())
                    .warehouse(warehouse)
                    .product(product)
                    .lot(lot)
                    .availableQuantity(quantity)
                    .reservedQuantity(BigDecimal.ZERO)
                    .lastTransactionDate(LocalDateTime.now())
                    .lastTransactionType("IN_RETURN")
                    .isActive(true)
                    .build();

            inventoryRepository.save(newInventory);
        }
    }

    /**
     * Generate transaction number
     */
    private String generateTransactionNo(String tenantId) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "IT-" + datePrefix + "-";

        long count = inventoryTransactionRepository.findByTenant_TenantId(tenantId).stream()
                .filter(t -> t.getTransactionNo().startsWith(prefix))
                .count();

        return prefix + String.format("%04d", count + 1);
    }

    /**
     * Generate LOT number
     */
    private String generateLotNo(String tenantId) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "LOT-" + datePrefix + "-";

        long count = lotRepository.findByTenant_TenantId(tenantId).stream()
                .filter(l -> l.getLotNo().startsWith(prefix))
                .count();

        return prefix + String.format("%04d", count + 1);
    }

    /**
     * Generate inspection number
     */
    private String generateInspectionNo(String tenantId) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "QI-" + datePrefix + "-";

        long count = qualityInspectionRepository.findByTenant_TenantId(tenantId).stream()
                .filter(qi -> qi.getInspectionNo().startsWith(prefix))
                .count();

        return prefix + String.format("%04d", count + 1);
    }
}
