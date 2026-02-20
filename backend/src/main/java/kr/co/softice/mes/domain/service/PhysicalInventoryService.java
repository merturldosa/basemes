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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Physical Inventory Service
 * 실사 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhysicalInventoryService {

    private final PhysicalInventoryRepository physicalInventoryRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final InventoryTransactionService inventoryTransactionService;

    /**
     * 실사 계획 생성
     * 창고의 현재 재고를 기준으로 실사 항목 자동 생성
     *
     * @param tenantId 테넌트 ID
     * @param warehouseId 창고 ID
     * @param inventoryDate 실사 일자
     * @param plannedByUserId 계획자 ID
     * @return 생성된 실사 계획
     */
    @Transactional
    public PhysicalInventoryEntity createPhysicalInventory(
            String tenantId,
            Long warehouseId,
            LocalDateTime inventoryDate,
            Long plannedByUserId,
            String remarks) {

        log.info("Creating physical inventory - Tenant: {}, Warehouse: {}, Date: {}",
                tenantId, warehouseId, inventoryDate);

        // 테넌트 조회
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));

        // 창고 조회
        WarehouseEntity warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));

        // 실사 번호 자동 생성
        String inventoryNo = generateInventoryNo(tenantId);

        // 실사 계획 생성
        PhysicalInventoryEntity physicalInventory = PhysicalInventoryEntity.builder()
                .tenant(tenant)
                .inventoryNo(inventoryNo)
                .inventoryDate(inventoryDate)
                .warehouse(warehouse)
                .inventoryStatus(PhysicalInventoryEntity.InventoryStatus.PLANNED.name())
                .plannedByUserId(plannedByUserId)
                .remarks(remarks)
                .build();

        // 창고의 현재 재고 조회
        List<InventoryEntity> currentInventories = inventoryRepository
                .findByTenantIdAndWarehouseIdWithAllRelations(tenantId, warehouseId);

        log.info("Found {} inventory items in warehouse", currentInventories.size());

        // 실사 항목 생성
        for (InventoryEntity inventory : currentInventories) {
            // 재고가 있는 항목만 실사 대상
            if (inventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0 ||
                inventory.getReservedQuantity().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal systemQuantity = inventory.getAvailableQuantity()
                        .add(inventory.getReservedQuantity());

                PhysicalInventoryItemEntity item = PhysicalInventoryItemEntity.builder()
                        .product(inventory.getProduct())
                        .lot(inventory.getLot())
                        .location(String.join("-",
                                inventory.getZone() != null ? inventory.getZone() : "",
                                inventory.getRack() != null ? inventory.getRack() : "",
                                inventory.getShelf() != null ? inventory.getShelf() : "",
                                inventory.getBin() != null ? inventory.getBin() : "").replaceAll("^-+|-+$", ""))
                        .systemQuantity(systemQuantity)
                        .adjustmentStatus(PhysicalInventoryItemEntity.AdjustmentStatus.NOT_REQUIRED.name())
                        .build();

                physicalInventory.addItem(item);
            }
        }

        PhysicalInventoryEntity saved = physicalInventoryRepository.save(physicalInventory);

        log.info("Physical inventory created - ID: {}, No: {}, Items: {}",
                saved.getPhysicalInventoryId(), saved.getInventoryNo(), saved.getItems().size());

        return saved;
    }

    /**
     * 실사 수량 입력
     *
     * @param tenantId 테넌트 ID
     * @param physicalInventoryId 실사 ID
     * @param itemId 실사 항목 ID
     * @param countedQuantity 실사 수량
     * @param countedByUserId 실사자 ID
     * @return 업데이트된 실사 계획
     */
    @Transactional
    public PhysicalInventoryEntity updateCountedQuantity(
            String tenantId,
            Long physicalInventoryId,
            Long itemId,
            BigDecimal countedQuantity,
            Long countedByUserId) {

        log.info("Updating counted quantity - Physical Inventory: {}, Item: {}, Quantity: {}",
                physicalInventoryId, itemId, countedQuantity);

        // 실사 조회
        PhysicalInventoryEntity physicalInventory = physicalInventoryRepository
                .findByIdWithAllRelations(physicalInventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 테넌트 검증
        if (!physicalInventory.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 상태 검증 (PLANNED 또는 IN_PROGRESS만 수정 가능)
        if (!physicalInventory.getInventoryStatus().equals(PhysicalInventoryEntity.InventoryStatus.PLANNED.name()) &&
            !physicalInventory.getInventoryStatus().equals(PhysicalInventoryEntity.InventoryStatus.IN_PROGRESS.name())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 실사 항목 찾기
        PhysicalInventoryItemEntity item = physicalInventory.getItems().stream()
                .filter(i -> i.getPhysicalInventoryItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 실사 수량 업데이트
        item.setCountedQuantity(countedQuantity);
        item.setCountedByUserId(countedByUserId);
        item.setCountedAt(LocalDateTime.now());
        item.calculateDifference();

        // 조정 상태 업데이트
        if (item.isAdjustmentRequired()) {
            item.setAdjustmentStatus(PhysicalInventoryItemEntity.AdjustmentStatus.PENDING.name());
        } else {
            item.setAdjustmentStatus(PhysicalInventoryItemEntity.AdjustmentStatus.NOT_REQUIRED.name());
        }

        // 실사 상태 업데이트 (IN_PROGRESS)
        if (physicalInventory.getInventoryStatus().equals(PhysicalInventoryEntity.InventoryStatus.PLANNED.name())) {
            physicalInventory.setInventoryStatus(PhysicalInventoryEntity.InventoryStatus.IN_PROGRESS.name());
        }

        PhysicalInventoryEntity saved = physicalInventoryRepository.save(physicalInventory);

        log.info("Counted quantity updated - Item: {}, Difference: {}",
                itemId, item.getDifferenceQuantity());

        return saved;
    }

    /**
     * 실사 완료
     * 모든 항목의 실사가 완료되었는지 확인
     *
     * @param tenantId 테넌트 ID
     * @param physicalInventoryId 실사 ID
     * @return 완료된 실사 계획
     */
    @Transactional
    public PhysicalInventoryEntity completePhysicalInventory(
            String tenantId,
            Long physicalInventoryId) {

        log.info("Completing physical inventory - ID: {}", physicalInventoryId);

        // 실사 조회
        PhysicalInventoryEntity physicalInventory = physicalInventoryRepository
                .findByIdWithAllRelations(physicalInventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 테넌트 검증
        if (!physicalInventory.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 상태 검증
        if (!physicalInventory.getInventoryStatus().equals(PhysicalInventoryEntity.InventoryStatus.IN_PROGRESS.name())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 모든 항목 실사 완료 확인
        boolean allCounted = physicalInventory.getItems().stream()
                .allMatch(item -> item.getCountedQuantity() != null);

        if (!allCounted) {
            log.warn("Not all items have been counted");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 실사 완료
        physicalInventory.setInventoryStatus(PhysicalInventoryEntity.InventoryStatus.COMPLETED.name());

        PhysicalInventoryEntity saved = physicalInventoryRepository.save(physicalInventory);

        // 차이 분석 로깅
        long adjustmentRequired = saved.getItems().stream()
                .filter(PhysicalInventoryItemEntity::isAdjustmentRequired)
                .count();

        log.info("Physical inventory completed - ID: {}, Items requiring adjustment: {}",
                physicalInventoryId, adjustmentRequired);

        return saved;
    }

    /**
     * 재고 조정 승인
     * 실사 차이에 대한 재고 조정 트랜잭션 생성
     *
     * @param tenantId 테넌트 ID
     * @param physicalInventoryId 실사 ID
     * @param itemId 실사 항목 ID
     * @param approverId 승인자 ID
     * @return 업데이트된 실사 계획
     */
    @Transactional
    public PhysicalInventoryEntity approveAdjustment(
            String tenantId,
            Long physicalInventoryId,
            Long itemId,
            Long approverId) {

        log.info("Approving adjustment - Physical Inventory: {}, Item: {}, Approver: {}",
                physicalInventoryId, itemId, approverId);

        // 실사 조회
        PhysicalInventoryEntity physicalInventory = physicalInventoryRepository
                .findByIdWithAllRelations(physicalInventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 테넌트 검증
        if (!physicalInventory.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 실사 항목 찾기
        PhysicalInventoryItemEntity item = physicalInventory.getItems().stream()
                .filter(i -> i.getPhysicalInventoryItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 조정 상태 검증
        if (!item.getAdjustmentStatus().equals(PhysicalInventoryItemEntity.AdjustmentStatus.PENDING.name())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 재고 조정 트랜잭션 생성
        InventoryTransactionEntity adjustmentTransaction = createAdjustmentTransaction(
                tenantId,
                physicalInventory,
                item,
                approverId
        );

        // 조정 트랜잭션 즉시 승인
        adjustmentTransaction = inventoryTransactionService.approveTransaction(
                adjustmentTransaction.getTransactionId(),
                approverId
        );

        // 조정 상태 업데이트
        item.setAdjustmentStatus(PhysicalInventoryItemEntity.AdjustmentStatus.APPROVED.name());
        item.setAdjustmentTransaction(adjustmentTransaction);

        PhysicalInventoryEntity saved = physicalInventoryRepository.save(physicalInventory);

        log.info("Adjustment approved - Item: {}, Transaction: {}",
                itemId, adjustmentTransaction.getTransactionId());

        return saved;
    }

    /**
     * 재고 조정 거부
     *
     * @param tenantId 테넌트 ID
     * @param physicalInventoryId 실사 ID
     * @param itemId 실사 항목 ID
     * @param approverId 승인자 ID
     * @param reason 거부 사유
     * @return 업데이트된 실사 계획
     */
    @Transactional
    public PhysicalInventoryEntity rejectAdjustment(
            String tenantId,
            Long physicalInventoryId,
            Long itemId,
            Long approverId,
            String reason) {

        log.info("Rejecting adjustment - Physical Inventory: {}, Item: {}, Reason: {}",
                physicalInventoryId, itemId, reason);

        // 실사 조회
        PhysicalInventoryEntity physicalInventory = physicalInventoryRepository
                .findByIdWithAllRelations(physicalInventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 테넌트 검증
        if (!physicalInventory.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 실사 항목 찾기
        PhysicalInventoryItemEntity item = physicalInventory.getItems().stream()
                .filter(i -> i.getPhysicalInventoryItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 조정 상태 업데이트
        item.setAdjustmentStatus(PhysicalInventoryItemEntity.AdjustmentStatus.REJECTED.name());
        item.setRemarks(reason);

        PhysicalInventoryEntity saved = physicalInventoryRepository.save(physicalInventory);

        log.info("Adjustment rejected - Item: {}", itemId);

        return saved;
    }

    /**
     * 실사 번호 자동 생성
     * 형식: PI-YYYYMMDD-0001
     */
    private String generateInventoryNo(String tenantId) {
        LocalDate today = LocalDate.now();
        String prefix = "PI-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";

        long count = physicalInventoryRepository.countByTenantIdAndInventoryNoPrefix(tenantId, prefix);
        String sequence = String.format("%04d", count + 1);

        return prefix + sequence;
    }

    /**
     * 재고 조정 트랜잭션 생성
     */
    private InventoryTransactionEntity createAdjustmentTransaction(
            String tenantId,
            PhysicalInventoryEntity physicalInventory,
            PhysicalInventoryItemEntity item,
            Long approverId) {

        String transactionNo = "ADJ-" + physicalInventory.getInventoryNo() + "-" +
                               item.getPhysicalInventoryItemId();

        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
                .transactionNo(transactionNo)
                .transactionDate(LocalDateTime.now())
                .transactionType("ADJUST")
                .warehouse(physicalInventory.getWarehouse())
                .product(item.getProduct())
                .lot(item.getLot())
                .quantity(item.getDifferenceQuantity().abs())
                .unit(item.getProduct().getUnit())
                .transactionUser(userRepository.findById(approverId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .referenceNo(physicalInventory.getInventoryNo())
                .remarks(String.format("실사 조정 - 시스템: %s, 실사: %s, 차이: %s",
                        item.getSystemQuantity(),
                        item.getCountedQuantity(),
                        item.getDifferenceQuantity()))
                .approvalStatus("PENDING")
                .build();

        return inventoryTransactionService.createTransactionPending(transaction);
    }

    /**
     * 실사 목록 조회
     */
    public List<PhysicalInventoryEntity> getPhysicalInventories(String tenantId) {
        return physicalInventoryRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * 실사 상세 조회
     */
    public PhysicalInventoryEntity getPhysicalInventory(String tenantId, Long physicalInventoryId) {
        PhysicalInventoryEntity physicalInventory = physicalInventoryRepository
                .findByIdWithAllRelations(physicalInventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (!physicalInventory.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return physicalInventory;
    }
}
