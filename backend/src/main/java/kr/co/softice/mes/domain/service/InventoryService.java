package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.InventoryRepository;
import kr.co.softice.mes.domain.repository.LotRepository;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Inventory Service
 * 재고 현황 서비스
 *
 * 핵심 기능:
 * - 재고 조회 및 관리
 * - 재고 예약/해제 (작업 지시용)
 * - 재고 잔액 자동 계산
 * - 저재고 알림
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final LotRepository lotRepository;

    public List<InventoryEntity> findByTenant(String tenantId) {
        return inventoryRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public List<InventoryEntity> findByTenantAndWarehouse(String tenantId, Long warehouseId) {
        return inventoryRepository.findByTenantIdAndWarehouseIdWithAllRelations(tenantId, warehouseId);
    }

    public List<InventoryEntity> findByTenantAndProduct(String tenantId, Long productId) {
        return inventoryRepository.findByTenant_TenantIdAndProduct_ProductId(tenantId, productId);
    }

    public Optional<InventoryEntity> findById(Long inventoryId) {
        return inventoryRepository.findByIdWithAllRelations(inventoryId);
    }

    public Optional<InventoryEntity> findByLocation(String tenantId, Long warehouseId, Long productId, Long lotId) {
        return inventoryRepository.findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
            tenantId, warehouseId, productId, lotId);
    }

    @Transactional
    public InventoryEntity updateInventory(InventoryEntity inventory) {
        log.info("Updating inventory: {} for product: {} in warehouse: {}",
            inventory.getInventoryId(),
            inventory.getProduct().getProductCode(),
            inventory.getWarehouse().getWarehouseCode());

        InventoryEntity updated = inventoryRepository.save(inventory);
        return inventoryRepository.findByIdWithAllRelations(updated.getInventoryId()).orElse(updated);
    }

    @Transactional
    public void deleteInventory(Long inventoryId) {
        log.info("Deleting inventory: {}", inventoryId);
        inventoryRepository.deleteById(inventoryId);
    }

    /**
     * Reserve inventory for work order
     * 작업 지시를 위한 재고 예약
     *
     * 가용 재고(available_quantity)를 예약 재고(reserved_quantity)로 이동
     */
    @Transactional
    public InventoryEntity reserveInventory(String tenantId, Long warehouseId, Long productId,
                                           Long lotId, BigDecimal quantity) {
        log.info("Reserving inventory: warehouse={}, product={}, lot={}, quantity={}",
            warehouseId, productId, lotId, quantity);

        // Find inventory record
        Optional<InventoryEntity> inventoryOpt;
        if (lotId != null) {
            inventoryOpt = inventoryRepository
                .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                    tenantId, warehouseId, productId, lotId);
        } else {
            // Find first available inventory for this product in warehouse
            List<InventoryEntity> inventories = inventoryRepository
                .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductId(tenantId, warehouseId, productId);

            inventoryOpt = inventories.stream()
                .filter(inv -> inv.getAvailableQuantity().compareTo(quantity) >= 0)
                .findFirst();
        }

        InventoryEntity inventory = inventoryOpt
            .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY, "Insufficient inventory available"));

        // Check if sufficient quantity available
        if (inventory.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY,
                String.format("Insufficient inventory: available=%s, requested=%s",
                    inventory.getAvailableQuantity(), quantity));
        }

        // Move from available to reserved
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(quantity));
        inventory.setReservedQuantity(inventory.getReservedQuantity().add(quantity));
        inventory.setLastTransactionDate(LocalDateTime.now());
        inventory.setLastTransactionType("RESERVE");

        InventoryEntity updated = inventoryRepository.save(inventory);
        log.info("Reserved {} units of product {} in warehouse {}",
            quantity, inventory.getProduct().getProductCode(), inventory.getWarehouse().getWarehouseCode());

        return inventoryRepository.findByIdWithAllRelations(updated.getInventoryId()).orElse(updated);
    }

    /**
     * Release reserved inventory
     * 예약 재고 해제
     *
     * 예약 재고(reserved_quantity)를 가용 재고(available_quantity)로 되돌림
     */
    @Transactional
    public InventoryEntity releaseReservedInventory(String tenantId, Long warehouseId,
                                                    Long productId, Long lotId, BigDecimal quantity) {
        log.info("Releasing reserved inventory: warehouse={}, product={}, lot={}, quantity={}",
            warehouseId, productId, lotId, quantity);

        // Find inventory record
        Optional<InventoryEntity> inventoryOpt = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenantId, warehouseId, productId, lotId);

        InventoryEntity inventory = inventoryOpt
            .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND, "Inventory record not found"));

        // Check if sufficient reserved quantity
        if (inventory.getReservedQuantity().compareTo(quantity) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY,
                String.format("Insufficient reserved inventory: reserved=%s, requested=%s",
                    inventory.getReservedQuantity(), quantity));
        }

        // Move from reserved to available
        inventory.setReservedQuantity(inventory.getReservedQuantity().subtract(quantity));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantity));
        inventory.setLastTransactionDate(LocalDateTime.now());
        inventory.setLastTransactionType("RELEASE");

        InventoryEntity updated = inventoryRepository.save(inventory);
        log.info("Released {} units of product {} in warehouse {}",
            quantity, inventory.getProduct().getProductCode(), inventory.getWarehouse().getWarehouseCode());

        return inventoryRepository.findByIdWithAllRelations(updated.getInventoryId()).orElse(updated);
    }

    /**
     * Update inventory balance
     * 재고 잔액 자동 계산 및 업데이트
     *
     * GoodsReceiptService, ShippingService 등에서 호출
     */
    @Transactional
    public InventoryEntity updateInventoryBalance(WarehouseEntity warehouse, ProductEntity product,
                                                  LotEntity lot, BigDecimal quantityChange,
                                                  String transactionType) {
        log.info("Updating inventory balance: warehouse={}, product={}, lot={}, quantity={}, type={}",
            warehouse.getWarehouseCode(), product.getProductCode(),
            lot != null ? lot.getLotNo() : "N/A", quantityChange, transactionType);

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
                inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantityChange));
                break;

            case "OUT_ISSUE":
            case "OUT_SCRAP":
                // Deduct from available first, then reserved if needed
                BigDecimal remaining = quantityChange;
                if (inventory.getAvailableQuantity().compareTo(remaining) >= 0) {
                    inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(remaining));
                } else {
                    remaining = remaining.subtract(inventory.getAvailableQuantity());
                    inventory.setAvailableQuantity(BigDecimal.ZERO);
                    inventory.setReservedQuantity(inventory.getReservedQuantity().subtract(remaining));
                }
                break;

            case "ADJUST":
                // Direct adjustment (can be positive or negative)
                inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantityChange));
                break;

            default:
                log.warn("Unknown transaction type: {}", transactionType);
        }

        // Update last transaction info
        inventory.setLastTransactionDate(LocalDateTime.now());
        inventory.setLastTransactionType(transactionType);

        InventoryEntity saved = inventoryRepository.save(inventory);
        log.info("Updated inventory: available={}, reserved={}",
            saved.getAvailableQuantity(), saved.getReservedQuantity());

        return saved;
    }

    /**
     * Find or create inventory record
     * 재고 레코드 찾기 또는 생성
     */
    @Transactional
    public InventoryEntity findOrCreateInventory(TenantEntity tenant, WarehouseEntity warehouse,
                                                ProductEntity product, LotEntity lot) {
        Optional<InventoryEntity> existing = inventoryRepository
            .findByTenant_TenantIdAndWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                tenant.getTenantId(),
                warehouse.getWarehouseId(),
                product.getProductId(),
                lot != null ? lot.getLotId() : null);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new inventory record
        InventoryEntity inventory = InventoryEntity.builder()
            .tenant(tenant)
            .warehouse(warehouse)
            .product(product)
            .lot(lot)
            .availableQuantity(BigDecimal.ZERO)
            .reservedQuantity(BigDecimal.ZERO)
            .unit(product.getUnit())
            .build();

        return inventoryRepository.save(inventory);
    }

    /**
     * Calculate low stock items
     * 저재고 제품 조회
     *
     * Note: Currently checks if total inventory (available + reserved) < 100
     * In future, this should check against product's safety_stock field
     */
    public List<InventoryEntity> calculateLowStock(String tenantId, BigDecimal threshold) {
        log.info("Calculating low stock items for tenant: {}, threshold: {}", tenantId, threshold);

        return inventoryRepository.findByTenantIdWithAllRelations(tenantId).stream()
            .filter(inventory -> {
                BigDecimal total = inventory.getAvailableQuantity().add(inventory.getReservedQuantity());
                return total.compareTo(threshold) < 0;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get total inventory quantity (available + reserved)
     * 총 재고 수량 계산
     */
    public BigDecimal getTotalQuantity(InventoryEntity inventory) {
        return inventory.getAvailableQuantity().add(inventory.getReservedQuantity());
    }
}
