package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Work Result Service
 * 작업 실적 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkResultService {

    private final WorkResultRepository workResultRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderService workOrderService;
    private final LotRepository lotRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    /**
     * Find all work results by tenant ID
     */
    public List<WorkResultEntity> findByTenant(String tenantId) {
        return workResultRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find work results by work order ID
     */
    public List<WorkResultEntity> findByWorkOrderId(Long workOrderId) {
        return workResultRepository.findByWorkOrderIdWithAllRelations(workOrderId);
    }

    /**
     * Find work results by date range
     */
    public List<WorkResultEntity> findByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return workResultRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Find work result by ID
     */
    public Optional<WorkResultEntity> findById(Long workResultId) {
        return workResultRepository.findById(workResultId);
    }

    /**
     * Create new work result and update work order aggregates
     */
    @Transactional
    public WorkResultEntity createWorkResult(WorkResultEntity workResult) {
        log.info("Creating work result for work order: {}",
            workResult.getWorkOrder().getWorkOrderId());

        // Calculate work duration if not set
        if (workResult.getWorkDuration() == null) {
            Duration duration = Duration.between(
                workResult.getWorkStartTime(),
                workResult.getWorkEndTime()
            );
            workResult.setWorkDuration((int) duration.toMinutes());
        }

        // Set worker name from worker entity if not set
        if (workResult.getWorkerName() == null && workResult.getWorker() != null) {
            workResult.setWorkerName(workResult.getWorker().getFullName());
        }

        // Save work result
        WorkResultEntity saved = workResultRepository.save(workResult);

        // Recalculate work order aggregates
        workOrderService.recalculateAggregates(workResult.getWorkOrder().getWorkOrderId());

        log.info("Work result created: quantity={}, good={}, defect={}",
            saved.getQuantity(), saved.getGoodQuantity(), saved.getDefectQuantity());

        return saved;
    }

    /**
     * Update work result and recalculate work order aggregates
     */
    @Transactional
    public WorkResultEntity updateWorkResult(WorkResultEntity workResult) {
        log.info("Updating work result: {}", workResult.getWorkResultId());

        if (!workResultRepository.existsById(workResult.getWorkResultId())) {
            throw new IllegalArgumentException("Work result not found: " + workResult.getWorkResultId());
        }

        // Recalculate work duration if times changed
        if (workResult.getWorkDuration() == null) {
            Duration duration = Duration.between(
                workResult.getWorkStartTime(),
                workResult.getWorkEndTime()
            );
            workResult.setWorkDuration((int) duration.toMinutes());
        }

        // Save updated result
        WorkResultEntity updated = workResultRepository.save(workResult);

        // Recalculate work order aggregates
        workOrderService.recalculateAggregates(workResult.getWorkOrder().getWorkOrderId());

        return updated;
    }

    /**
     * Delete work result and recalculate work order aggregates
     */
    @Transactional
    public void deleteWorkResult(Long workResultId) {
        log.info("Deleting work result: {}", workResultId);

        WorkResultEntity workResult = workResultRepository.findById(workResultId)
            .orElseThrow(() -> new IllegalArgumentException("Work result not found: " + workResultId));

        Long workOrderId = workResult.getWorkOrder().getWorkOrderId();

        // Delete result
        workResultRepository.deleteById(workResultId);

        // Recalculate work order aggregates
        workOrderService.recalculateAggregates(workOrderId);
    }

    /**
     * Count results by work order
     */
    public long countByWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        return workResultRepository.countByWorkOrder(workOrder);
    }

    // ================== Production Inventory Integration ==================

    /**
     * Record production and create finished goods inventory
     * 생산 실적 등록 및 완제품 입고
     *
     * @param workResult Work result entity
     * @param warehouseId Finished goods warehouse ID
     * @param createLot Whether to create new LOT
     * @return Created work result with LOT information
     */
    @Transactional
    public WorkResultEntity recordProductionWithInventory(
            WorkResultEntity workResult,
            Long warehouseId,
            boolean createLot) {

        log.info("Recording production for work order: {}, quantity: {}, good: {}, defect: {}",
            workResult.getWorkOrder().getWorkOrderId(),
            workResult.getQuantity(),
            workResult.getGoodQuantity(),
            workResult.getDefectQuantity());

        // 1. Create work result
        WorkResultEntity created = createWorkResult(workResult);

        // 2. Create LOT for finished goods (if requested)
        LotEntity lot = null;
        if (createLot && created.getGoodQuantity().compareTo(BigDecimal.ZERO) > 0) {
            lot = createFinishedGoodsLot(created, warehouseId);
        }

        // 3. Update finished goods inventory
        if (created.getGoodQuantity().compareTo(BigDecimal.ZERO) > 0) {
            updateFinishedGoodsInventory(created, warehouseId, lot);
        }

        // 4. Handle defective products (move to scrap or quarantine)
        if (created.getDefectQuantity().compareTo(BigDecimal.ZERO) > 0) {
            handleDefectiveProducts(created);
        }

        log.info("Production recorded successfully with inventory update");
        return created;
    }

    /**
     * Create LOT for finished goods
     * 완제품 LOT 생성
     *
     * @param workResult Work result
     * @param warehouseId Warehouse ID
     * @return Created LOT entity
     */
    private LotEntity createFinishedGoodsLot(WorkResultEntity workResult, Long warehouseId) {
        WarehouseEntity warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        WorkOrderEntity workOrder = workResult.getWorkOrder();
        ProductEntity product = workOrder.getProduct();

        // Generate LOT number: FG-YYYYMMDD-WO{workOrderNo}-{sequence}
        String lotNo = generateFinishedGoodsLotNo(
            workOrder.getTenant().getTenantId(),
            workOrder.getWorkOrderNo()
        );

        LotEntity lot = LotEntity.builder()
            .tenant(workOrder.getTenant())
            .product(product)
            .warehouse(warehouse)
            .lotNo(lotNo)
            .lotType("PRODUCTION")
            .initialQuantity(workResult.getGoodQuantity())
            .currentQuantity(workResult.getGoodQuantity())
            .reservedQuantity(BigDecimal.ZERO)
            .unit(product.getUnit())
            .qualityStatus("PENDING") // Will be updated after OQC
            .productionDate(workResult.getWorkDate().toLocalDate())
            .isActive(true)
            .remarks("Produced from work order: " + workOrder.getWorkOrderNo())
            .build();

        LotEntity savedLot = lotRepository.save(lot);

        log.info("Created finished goods LOT: {} for {} {}",
            savedLot.getLotNo(),
            workResult.getGoodQuantity(),
            product.getProductCode());

        return savedLot;
    }

    /**
     * Update finished goods inventory
     * 완제품 재고 업데이트
     *
     * @param workResult Work result
     * @param warehouseId Warehouse ID
     * @param lot LOT entity (optional)
     */
    private void updateFinishedGoodsInventory(
            WorkResultEntity workResult,
            Long warehouseId,
            LotEntity lot) {

        WarehouseEntity warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        WorkOrderEntity workOrder = workResult.getWorkOrder();
        ProductEntity product = workOrder.getProduct();

        // Find or create inventory record
        Optional<InventoryEntity> existingInventory = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                workOrder.getTenant().getTenantId(),
                warehouseId,
                product.getProductId(),
                lot != null ? lot.getLotId() : null
            );

        InventoryEntity inventory;
        if (existingInventory.isPresent()) {
            inventory = existingInventory.get();
            inventory.setAvailableQuantity(
                inventory.getAvailableQuantity().add(workResult.getGoodQuantity())
            );
        } else {
            inventory = InventoryEntity.builder()
                .tenant(workOrder.getTenant())
                .warehouse(warehouse)
                .product(product)
                .lot(lot)
                .availableQuantity(workResult.getGoodQuantity())
                .reservedQuantity(BigDecimal.ZERO)
                .unit(product.getUnit())
                .build();
        }

        // Update last transaction info
        inventory.setLastTransactionDate(LocalDateTime.now());
        inventory.setLastTransactionType("IN_PRODUCTION");

        inventoryRepository.save(inventory);

        // Create inventory transaction
        createProductionTransaction(workResult, warehouse, product, lot);

        log.info("Updated finished goods inventory: {} {} in warehouse {}",
            workResult.getGoodQuantity(),
            product.getProductCode(),
            warehouse.getWarehouseCode());
    }

    /**
     * Create inventory transaction for production
     * 생산 재고 트랜잭션 생성
     */
    private void createProductionTransaction(
            WorkResultEntity workResult,
            WarehouseEntity warehouse,
            ProductEntity product,
            LotEntity lot) {

        String transactionNo = String.format("PROD-%s-%03d",
            workResult.getWorkOrder().getWorkOrderNo(),
            workResult.getWorkResultId());

        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
            .tenant(workResult.getTenant())
            .transactionNo(transactionNo)
            .transactionDate(workResult.getWorkDate())
            .transactionType("IN_PRODUCTION")
            .warehouse(warehouse)
            .product(product)
            .lot(lot)
            .quantity(workResult.getGoodQuantity())
            .unit(product.getUnit())
            .transactionUser(workResult.getWorker())
            .approvalStatus("APPROVED")
            .referenceNo(workResult.getWorkOrder().getWorkOrderNo())
            .remarks("Production from work result ID: " + workResult.getWorkResultId())
            .build();

        inventoryTransactionRepository.save(transaction);

        log.info("Created production transaction: {}", transactionNo);
    }

    /**
     * Handle defective products
     * 불량품 처리 (격리 창고 또는 스크랩 창고 이동)
     */
    private void handleDefectiveProducts(WorkResultEntity workResult) {
        String tenantId = workResult.getTenant().getTenantId();

        // Find scrap or quarantine warehouse
        List<WarehouseEntity> scrapWarehouses = warehouseRepository
            .findByTenant_TenantIdAndWarehouseType(tenantId, "SCRAP");

        if (scrapWarehouses.isEmpty()) {
            scrapWarehouses = warehouseRepository
                .findByTenant_TenantIdAndWarehouseType(tenantId, "QUARANTINE");
        }

        if (!scrapWarehouses.isEmpty()) {
            WarehouseEntity scrapWarehouse = scrapWarehouses.get(0);
            ProductEntity product = workResult.getWorkOrder().getProduct();

            // Create inventory for defective products
            InventoryEntity defectInventory = InventoryEntity.builder()
                .tenant(workResult.getTenant())
                .warehouse(scrapWarehouse)
                .product(product)
                .availableQuantity(workResult.getDefectQuantity())
                .reservedQuantity(BigDecimal.ZERO)
                .unit(product.getUnit())
                .lastTransactionDate(LocalDateTime.now())
                .lastTransactionType("IN_SCRAP")
                .build();

            inventoryRepository.save(defectInventory);

            log.info("Moved {} defective {} to {} warehouse",
                workResult.getDefectQuantity(),
                product.getProductCode(),
                scrapWarehouse.getWarehouseType());
        } else {
            log.warn("No scrap or quarantine warehouse found for defective products");
        }
    }

    /**
     * Generate finished goods LOT number
     * FG-YYYYMMDD-WO{workOrderNo}-{sequence}
     */
    private String generateFinishedGoodsLotNo(String tenantId, String workOrderNo) {
        String datePrefix = "FG-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String woSuffix = workOrderNo.replaceAll("[^0-9]", ""); // Extract numbers only

        // Find existing LOTs with same prefix
        long count = lotRepository.findByTenantId(tenantId).stream()
            .filter(lot -> lot.getLotNo() != null && lot.getLotNo().startsWith(datePrefix))
            .count();

        return String.format("%s-WO%s-%03d", datePrefix, woSuffix, count + 1);
    }
}
