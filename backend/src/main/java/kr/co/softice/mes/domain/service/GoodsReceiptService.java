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
 * Goods Receipt Service
 * 입하 관리 서비스
 *
 * 핵심 기능:
 * - 입하 생성 (구매 주문 기반)
 * - LOT 자동 생성 및 재고 트랜잭션 연동
 * - 품질 검사 통합 (QMS)
 * - 입하 완료/취소 워크플로우
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final LotRepository lotRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseRepository warehouseRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final QualityStandardRepository qualityStandardRepository;

    /**
     * Find all goods receipts by tenant ID
     */
    public List<GoodsReceiptEntity> findByTenant(String tenantId) {
        return goodsReceiptRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find goods receipt by ID
     */
    public Optional<GoodsReceiptEntity> findById(Long goodsReceiptId) {
        return goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId);
    }

    /**
     * Find goods receipts by status
     */
    public List<GoodsReceiptEntity> findByStatus(String tenantId, String status) {
        return goodsReceiptRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Find goods receipts by purchase order ID
     */
    public List<GoodsReceiptEntity> findByPurchaseOrderId(String tenantId, Long purchaseOrderId) {
        return goodsReceiptRepository.findByTenantIdAndPurchaseOrderId(tenantId, purchaseOrderId);
    }

    /**
     * Find goods receipts by warehouse ID
     */
    public List<GoodsReceiptEntity> findByWarehouseId(String tenantId, Long warehouseId) {
        return goodsReceiptRepository.findByTenantIdAndWarehouseId(tenantId, warehouseId);
    }

    /**
     * Find goods receipts by date range
     */
    public List<GoodsReceiptEntity> findByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return goodsReceiptRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Create new goods receipt
     *
     * 워크플로우:
     * 1. 입하 번호 자동 생성 (GR-YYYYMMDD-0001)
     * 2. 입하 헤더 생성 (PENDING 상태)
     * 3. 각 입하 항목에 대해:
     *    - LOT 레코드 자동 생성 (quality_status=PENDING)
     *    - 재고 트랜잭션 생성 (IN_RECEIVE, approval_status=PENDING)
     *    - 재고 잔액 업데이트 (조건부: 검사 불요 시 즉시)
     * 4. 합계 계산 (totalQuantity, totalAmount)
     */
    @Transactional
    public GoodsReceiptEntity createGoodsReceipt(GoodsReceiptEntity goodsReceipt) {
        log.info("Creating goods receipt for tenant: {}, warehouse: {}",
            goodsReceipt.getTenant().getTenantId(),
            goodsReceipt.getWarehouse().getWarehouseCode());

        // 1. Generate receipt number if not provided
        if (goodsReceipt.getReceiptNo() == null || goodsReceipt.getReceiptNo().isEmpty()) {
            goodsReceipt.setReceiptNo(generateReceiptNo(goodsReceipt.getTenant().getTenantId()));
        }

        // Check duplicate receipt number
        if (goodsReceiptRepository.existsByTenant_TenantIdAndReceiptNo(
                goodsReceipt.getTenant().getTenantId(), goodsReceipt.getReceiptNo())) {
            throw new IllegalArgumentException("Receipt number already exists: " + goodsReceipt.getReceiptNo());
        }

        // 2. Set initial status
        if (goodsReceipt.getReceiptStatus() == null) {
            goodsReceipt.setReceiptStatus("PENDING");
        }

        if (goodsReceipt.getIsActive() == null) {
            goodsReceipt.setIsActive(true);
        }

        // 3. Calculate totals from items
        calculateTotals(goodsReceipt);

        // 4. Save goods receipt header
        GoodsReceiptEntity savedReceipt = goodsReceiptRepository.save(goodsReceipt);

        // 5. Process each item: create LOT, inventory transaction, and update inventory
        for (GoodsReceiptItemEntity item : savedReceipt.getItems()) {
            processGoodsReceiptItem(savedReceipt, item);
        }

        log.info("Created goods receipt: {} with {} items",
            savedReceipt.getReceiptNo(), savedReceipt.getItems().size());

        return goodsReceiptRepository.findByIdWithAllRelations(savedReceipt.getGoodsReceiptId())
            .orElse(savedReceipt);
    }

    /**
     * Update goods receipt
     * Note: Can only update PENDING receipts
     */
    @Transactional
    public GoodsReceiptEntity updateGoodsReceipt(GoodsReceiptEntity goodsReceipt) {
        log.info("Updating goods receipt: {}", goodsReceipt.getGoodsReceiptId());

        GoodsReceiptEntity existing = goodsReceiptRepository.findById(goodsReceipt.getGoodsReceiptId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Goods receipt not found: " + goodsReceipt.getGoodsReceiptId()));

        // Only PENDING receipts can be updated
        if (!"PENDING".equals(existing.getReceiptStatus())) {
            throw new IllegalStateException(
                "Cannot update goods receipt in status: " + existing.getReceiptStatus());
        }

        // Recalculate totals
        calculateTotals(goodsReceipt);

        GoodsReceiptEntity updated = goodsReceiptRepository.save(goodsReceipt);
        return goodsReceiptRepository.findByIdWithAllRelations(updated.getGoodsReceiptId())
            .orElse(updated);
    }

    /**
     * Complete goods receipt
     *
     * 워크플로우:
     * 1. 품질 검사 결과 확인 (inspection_status)
     * 2. 합격품: LOT quality_status → PASSED, 가용 재고로 추가
     * 3. 불합격품: LOT quality_status → FAILED, 격리 창고로 이동
     * 4. 입하 상태 → COMPLETED
     * 5. 구매 주문 항목 수령 수량 업데이트 (선택적)
     */
    @Transactional
    public GoodsReceiptEntity completeGoodsReceipt(Long goodsReceiptId, Long completedByUserId) {
        log.info("Completing goods receipt: {}", goodsReceiptId);

        GoodsReceiptEntity receipt = goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId)
            .orElseThrow(() -> new IllegalArgumentException("Goods receipt not found: " + goodsReceiptId));

        // Validate status
        if (!"PENDING".equals(receipt.getReceiptStatus()) && !"INSPECTING".equals(receipt.getReceiptStatus())) {
            throw new IllegalStateException(
                "Cannot complete goods receipt in status: " + receipt.getReceiptStatus());
        }

        // Process each item based on inspection status
        for (GoodsReceiptItemEntity item : receipt.getItems()) {
            processItemCompletion(receipt, item);
        }

        // Update receipt status
        receipt.setReceiptStatus("COMPLETED");

        GoodsReceiptEntity completed = goodsReceiptRepository.save(receipt);
        log.info("Completed goods receipt: {} with status: {}",
            completed.getReceiptNo(), completed.getReceiptStatus());

        return goodsReceiptRepository.findByIdWithAllRelations(completed.getGoodsReceiptId())
            .orElse(completed);
    }

    /**
     * Cancel goods receipt
     *
     * 워크플로우:
     * 1. 재고 이동 역처리 (보정 트랜잭션 생성)
     * 2. LOT 비활성화
     * 3. 상태 → CANCELLED
     */
    @Transactional
    public GoodsReceiptEntity cancelGoodsReceipt(Long goodsReceiptId, String cancelReason) {
        log.info("Cancelling goods receipt: {}", goodsReceiptId);

        GoodsReceiptEntity receipt = goodsReceiptRepository.findByIdWithAllRelations(goodsReceiptId)
            .orElseThrow(() -> new IllegalArgumentException("Goods receipt not found: " + goodsReceiptId));

        // Cannot cancel completed receipts
        if ("CANCELLED".equals(receipt.getReceiptStatus())) {
            throw new IllegalStateException("Goods receipt already cancelled");
        }

        // Reverse inventory transactions for each item
        for (GoodsReceiptItemEntity item : receipt.getItems()) {
            reverseItemInventory(receipt, item, cancelReason);
        }

        // Update receipt status
        receipt.setReceiptStatus("CANCELLED");
        receipt.setRemarks(cancelReason);

        GoodsReceiptEntity cancelled = goodsReceiptRepository.save(receipt);
        log.info("Cancelled goods receipt: {}", cancelled.getReceiptNo());

        return goodsReceiptRepository.findByIdWithAllRelations(cancelled.getGoodsReceiptId())
            .orElse(cancelled);
    }

    // ================== Private Helper Methods ==================

    /**
     * Generate sequential receipt number: GR-YYYYMMDD-0001
     */
    private String generateReceiptNo(String tenantId) {
        String datePrefix = "GR-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Find existing receipts with the same date prefix
        long count = goodsReceiptRepository.findByTenantIdWithAllRelations(tenantId).stream()
            .filter(gr -> gr.getReceiptNo() != null && gr.getReceiptNo().startsWith(datePrefix))
            .count();

        return String.format("%s-%04d", datePrefix, count + 1);
    }

    /**
     * Calculate total quantity and amount from items
     */
    private void calculateTotals(GoodsReceiptEntity receipt) {
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmt = BigDecimal.ZERO;

        for (GoodsReceiptItemEntity item : receipt.getItems()) {
            if (item.getReceivedQuantity() != null) {
                totalQty = totalQty.add(item.getReceivedQuantity());
            }
            if (item.getLineAmount() != null) {
                totalAmt = totalAmt.add(item.getLineAmount());
            }
        }

        receipt.setTotalQuantity(totalQty);
        receipt.setTotalAmount(totalAmt);
    }

    /**
     * Process individual goods receipt item:
     * - Create LOT record
     * - Create inventory transaction
     * - Update inventory balance (if inspection not required)
     */
    private void processGoodsReceiptItem(GoodsReceiptEntity receipt, GoodsReceiptItemEntity item) {
        log.info("Processing goods receipt item: product={}, quantity={}",
            item.getProductCode(), item.getReceivedQuantity());

        // 1. Create LOT if lotNo provided
        LotEntity lot = null;
        if (item.getLotNo() != null && !item.getLotNo().isEmpty()) {
            lot = createLotForItem(receipt, item);
        }

        // 2. Set default inspection status if not provided
        if (item.getInspectionStatus() == null || item.getInspectionStatus().isEmpty()) {
            item.setInspectionStatus("NOT_REQUIRED");
        }

        // 3. Create inventory transaction (PENDING approval)
        createInventoryTransaction(receipt, item, lot);

        // 4. Update inventory if inspection not required
        if ("NOT_REQUIRED".equals(item.getInspectionStatus())) {
            updateInventoryBalance(receipt.getWarehouse(), item.getProduct(), lot,
                item.getReceivedQuantity(), "IN_RECEIVE");
        }

        // 5. Create IQC request if inspection is required
        if ("PENDING".equals(item.getInspectionStatus())) {
            createIQCRequest(receipt, item, lot);
            receipt.setReceiptStatus("INSPECTING");
        }
    }

    /**
     * Create IQC (Incoming Quality Control) inspection request
     *
     * 입고 품질 검사 의뢰 생성:
     * - inspection_type: INCOMING
     * - 검사 기준서 조회 (제품별)
     * - 검사 수량 = 입고 수량
     * - 검사자는 품질팀 기본 사용자로 설정 (또는 파라미터)
     */
    private void createIQCRequest(GoodsReceiptEntity receipt, GoodsReceiptItemEntity item, LotEntity lot) {
        log.info("Creating IQC request for product: {}, LOT: {}", item.getProductCode(), item.getLotNo());

        // Find quality standard for this product (INCOMING type)
        List<QualityStandardEntity> allStandards = qualityStandardRepository
            .findByTenantAndProduct(receipt.getTenant(), item.getProduct());

        // Filter by inspection type and active status
        List<QualityStandardEntity> standards = allStandards.stream()
            .filter(s -> "INCOMING".equals(s.getInspectionType()))
            .filter(QualityStandardEntity::getIsActive)
            .collect(java.util.stream.Collectors.toList());

        if (standards.isEmpty()) {
            log.warn("No IQC quality standard found for product: {}, skipping IQC request", item.getProductCode());
            return;
        }

        QualityStandardEntity standard = standards.get(0); // Use first active standard

        // Find quality inspector (default: system user or first available user)
        UserEntity inspector = receipt.getReceiver() != null ?
            receipt.getReceiver() :
            userRepository.findById(1L).orElseThrow(() ->
                new IllegalArgumentException("No inspector user found"));

        // Generate inspection number
        String inspectionNo = generateInspectionNo(receipt.getTenant().getTenantId(), "IQC");

        // Create quality inspection record
        QualityInspectionEntity inspection = QualityInspectionEntity.builder()
            .tenant(receipt.getTenant())
            .qualityStandard(standard)
            .product(item.getProduct())
            .inspectionNo(inspectionNo)
            .inspectionDate(LocalDateTime.now())
            .inspectionType("INCOMING")
            .inspector(inspector)
            .inspectedQuantity(item.getReceivedQuantity())
            .passedQuantity(BigDecimal.ZERO) // Will be updated after inspection
            .failedQuantity(BigDecimal.ZERO)
            .inspectionResult("CONDITIONAL") // Pending inspection
            .remarks("IQC request from goods receipt: " + receipt.getReceiptNo())
            .build();

        QualityInspectionEntity savedInspection = qualityInspectionRepository.save(inspection);

        // Link inspection to goods receipt item
        item.setQualityInspection(savedInspection);

        log.info("Created IQC request: {} for goods receipt item: {}", inspectionNo, item.getProductCode());
    }

    /**
     * Generate sequential inspection number: IQC-YYYYMMDD-0001 or OQC-YYYYMMDD-0001
     */
    private String generateInspectionNo(String tenantId, String prefix) {
        String datePrefix = prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Find existing inspections with the same date prefix
        long count = qualityInspectionRepository.findByTenant_TenantId(tenantId).stream()
            .filter(qi -> qi.getInspectionNo() != null && qi.getInspectionNo().startsWith(datePrefix))
            .count();

        return String.format("%s-%04d", datePrefix, count + 1);
    }

    /**
     * Create LOT record for goods receipt item
     */
    private LotEntity createLotForItem(GoodsReceiptEntity receipt, GoodsReceiptItemEntity item) {
        // Check if LOT already exists
        Optional<LotEntity> existingLot = lotRepository.findByTenant_TenantIdAndLotNo(
            receipt.getTenant().getTenantId(), item.getLotNo());

        if (existingLot.isPresent()) {
            log.info("LOT already exists: {}", item.getLotNo());
            return existingLot.get();
        }

        // Create new LOT
        LotEntity lot = LotEntity.builder()
            .tenant(receipt.getTenant())
            .product(item.getProduct())
            .lotNo(item.getLotNo())
            .initialQuantity(item.getReceivedQuantity())
            .currentQuantity(item.getReceivedQuantity())
            .reservedQuantity(BigDecimal.ZERO)
            .unit(item.getProduct().getUnit())
            .expiryDate(item.getExpiryDate())
            .qualityStatus("PENDING".equals(item.getInspectionStatus()) ? "PENDING" : "PASSED")
            .isActive(true)
            .remarks("Created from goods receipt: " + receipt.getReceiptNo())
            .build();

        if (receipt.getSupplier() != null) {
            lot.setSupplierName(receipt.getSupplier().getSupplierName());
        }

        LotEntity saved = lotRepository.save(lot);
        log.info("Created LOT: {} for product: {}", saved.getLotNo(), item.getProductCode());

        return saved;
    }

    /**
     * Create inventory transaction for goods receipt item
     */
    private void createInventoryTransaction(GoodsReceiptEntity receipt,
                                           GoodsReceiptItemEntity item,
                                           LotEntity lot) {
        // Find transaction user
        UserEntity transactionUser = receipt.getReceiver() != null ?
            receipt.getReceiver() :
            userRepository.findById(1L).orElse(null); // Fallback to system user

        String transactionNo = String.format("IN-%s-%03d",
            receipt.getReceiptNo(),
            receipt.getItems().indexOf(item) + 1);

        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
            .tenant(receipt.getTenant())
            .transactionNo(transactionNo)
            .transactionDate(receipt.getReceiptDate())
            .transactionType("IN_RECEIVE")
            .warehouse(receipt.getWarehouse())
            .product(item.getProduct())
            .lot(lot)
            .quantity(item.getReceivedQuantity())
            .unit(item.getProduct().getUnit())
            .transactionUser(transactionUser)
            .approvalStatus("PENDING".equals(item.getInspectionStatus()) ? "PENDING" : "APPROVED")
            .referenceNo(receipt.getReceiptNo())
            .remarks("Goods receipt: " + receipt.getReceiptNo())
            .build();

        // Auto-approve if inspection not required
        if ("NOT_REQUIRED".equals(item.getInspectionStatus())) {
            transaction.setApprovalStatus("APPROVED");
            transaction.setApprovedBy(transactionUser);
            transaction.setApprovedDate(LocalDateTime.now());
        }

        inventoryTransactionRepository.save(transaction);
        log.info("Created inventory transaction: {} for goods receipt item", transactionNo);
    }

    /**
     * Update inventory balance
     */
    private void updateInventoryBalance(WarehouseEntity warehouse,
                                       ProductEntity product,
                                       LotEntity lot,
                                       BigDecimal quantity,
                                       String transactionType) {
        // Find or create inventory record
        Optional<InventoryEntity> existingInventory = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                warehouse.getTenant().getTenantId(),
                warehouse.getWarehouseId(),
                product.getProductId(),
                lot != null ? lot.getLotId() : null);

        InventoryEntity inventory;
        if (existingInventory.isPresent()) {
            inventory = existingInventory.get();
        } else {
            // Create new inventory record
            inventory = InventoryEntity.builder()
                .tenant(warehouse.getTenant())
                .warehouse(warehouse)
                .product(product)
                .lot(lot)
                .availableQuantity(BigDecimal.ZERO)
                .reservedQuantity(BigDecimal.ZERO)
                .unit(product.getUnit())
                .build();
        }

        // Update quantity based on transaction type
        switch (transactionType) {
            case "IN_RECEIVE":
            case "IN_PRODUCTION":
            case "IN_RETURN":
                inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantity));
                break;
            case "OUT_ISSUE":
            case "OUT_SCRAP":
                inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(quantity));
                break;
            default:
                log.warn("Unknown transaction type: {}", transactionType);
        }

        // Update last transaction info
        inventory.setLastTransactionDate(LocalDateTime.now());
        inventory.setLastTransactionType(transactionType);

        inventoryRepository.save(inventory);
        log.info("Updated inventory balance for product: {} in warehouse: {}, available: {}",
            product.getProductCode(), warehouse.getWarehouseCode(), inventory.getAvailableQuantity());
    }

    /**
     * Process item completion based on inspection status
     */
    private void processItemCompletion(GoodsReceiptEntity receipt, GoodsReceiptItemEntity item) {
        // Find LOT for this item
        Optional<LotEntity> lotOpt = lotRepository.findByTenant_TenantIdAndLotNo(
            receipt.getTenant().getTenantId(), item.getLotNo());

        if (!lotOpt.isPresent()) {
            log.warn("LOT not found for item: {}", item.getLotNo());
            return;
        }

        LotEntity lot = lotOpt.get();

        // Process based on inspection status
        switch (item.getInspectionStatus()) {
            case "PASS":
                // Update LOT quality status to PASSED
                lot.setQualityStatus("PASSED");
                lotRepository.save(lot);

                // Update inventory (move from pending to available)
                updateInventoryBalance(receipt.getWarehouse(), item.getProduct(), lot,
                    item.getReceivedQuantity(), "IN_RECEIVE");

                log.info("Item passed inspection: {}, moved to available inventory", item.getProductCode());
                break;

            case "FAIL":
                // Update LOT quality status to FAILED
                lot.setQualityStatus("FAILED");
                lotRepository.save(lot);

                // Move to quarantine warehouse
                moveToQuarantine(receipt, item, lot);

                log.info("Item failed inspection: {}, moved to quarantine", item.getProductCode());
                break;

            case "NOT_REQUIRED":
                // Already processed during creation
                lot.setQualityStatus("PASSED");
                lotRepository.save(lot);
                break;

            case "PENDING":
                log.warn("Item still pending inspection: {}", item.getProductCode());
                break;

            default:
                log.warn("Unknown inspection status: {}", item.getInspectionStatus());
        }
    }

    /**
     * Move rejected items to quarantine warehouse
     */
    private void moveToQuarantine(GoodsReceiptEntity receipt, GoodsReceiptItemEntity item, LotEntity lot) {
        // Find quarantine warehouse
        List<WarehouseEntity> quarantineWarehouses = warehouseRepository
            .findByTenant_TenantIdAndWarehouseType(receipt.getTenant().getTenantId(), "QUARANTINE");

        if (quarantineWarehouses.isEmpty()) {
            log.warn("No quarantine warehouse found, keeping in current warehouse");
            return;
        }

        WarehouseEntity quarantineWarehouse = quarantineWarehouses.get(0);

        // Create inventory in quarantine warehouse
        updateInventoryBalance(quarantineWarehouse, item.getProduct(), lot,
            item.getReceivedQuantity(), "IN_RECEIVE");

        log.info("Moved {} {} to quarantine warehouse: {}",
            item.getReceivedQuantity(), item.getProductCode(), quarantineWarehouse.getWarehouseCode());
    }

    /**
     * Reverse inventory for cancelled goods receipt item
     */
    private void reverseItemInventory(GoodsReceiptEntity receipt, GoodsReceiptItemEntity item, String reason) {
        // Find LOT
        if (item.getLotNo() != null) {
            Optional<LotEntity> lotOpt = lotRepository.findByTenant_TenantIdAndLotNo(
                receipt.getTenant().getTenantId(), item.getLotNo());

            if (lotOpt.isPresent()) {
                LotEntity lot = lotOpt.get();

                // Deactivate LOT
                lot.setIsActive(false);
                lot.setRemarks("Cancelled: " + reason);
                lotRepository.save(lot);

                // Create reversing transaction (subtract quantity)
                updateInventoryBalance(receipt.getWarehouse(), item.getProduct(), lot,
                    item.getReceivedQuantity(), "OUT_ISSUE");

                log.info("Reversed inventory for LOT: {}", lot.getLotNo());
            }
        }
    }
}
