package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.InventoryEntity;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.repository.InventoryRepository;
import kr.co.softice.mes.domain.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LOT 선택 전략 서비스
 * FIFO (First-In-First-Out) 및 FEFO (First-Expired-First-Out) 로직 구현
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotSelectionService {

    private final InventoryRepository inventoryRepository;
    private final LotRepository lotRepository;

    /**
     * FIFO 전략으로 LOT 선택
     * 가장 오래된 LOT부터 순서대로 선택
     *
     * @param tenantId 테넌트 ID
     * @param warehouseId 창고 ID
     * @param productId 제품 ID
     * @param requiredQuantity 필요 수량
     * @return LOT 할당 목록
     */
    public List<LotAllocation> selectLotsByFIFO(
            String tenantId,
            Long warehouseId,
            Long productId,
            BigDecimal requiredQuantity) {

        log.info("FIFO LOT selection - Tenant: {}, Warehouse: {}, Product: {}, Required: {}",
                tenantId, warehouseId, productId, requiredQuantity);

        // 가용 재고 조회 (LOT 생성일 기준 오름차순)
        List<InventoryEntity> availableInventories = inventoryRepository
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                        warehouseId, productId, BigDecimal.ZERO);

        // LOT 생성일 기준 정렬
        List<InventoryEntity> sortedInventories = availableInventories.stream()
                .sorted(Comparator.comparing(inv -> inv.getLot().getCreatedAt()))
                .collect(Collectors.toList());

        return allocateQuantity(sortedInventories, requiredQuantity, "FIFO");
    }

    /**
     * FEFO 전략으로 LOT 선택
     * 유효기간이 가장 빨리 만료되는 LOT부터 선택
     *
     * @param tenantId 테넌트 ID
     * @param warehouseId 창고 ID
     * @param productId 제품 ID
     * @param requiredQuantity 필요 수량
     * @return LOT 할당 목록
     */
    public List<LotAllocation> selectLotsByFEFO(
            String tenantId,
            Long warehouseId,
            Long productId,
            BigDecimal requiredQuantity) {

        log.info("FEFO LOT selection - Tenant: {}, Warehouse: {}, Product: {}, Required: {}",
                tenantId, warehouseId, productId, requiredQuantity);

        // 가용 재고 조회 (유효기간이 있는 것만)
        List<InventoryEntity> availableInventories = inventoryRepository
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndAvailableQuantityGreaterThan(
                        warehouseId, productId, BigDecimal.ZERO);

        // 유효기간 기준 정렬 (null은 제외)
        List<InventoryEntity> sortedInventories = availableInventories.stream()
                .filter(inv -> inv.getLot().getExpiryDate() != null)
                .sorted(Comparator.comparing(inv -> inv.getLot().getExpiryDate()))
                .collect(Collectors.toList());

        // 유효기간이 없는 재고는 뒤에 추가 (LOT 생성일 기준 정렬)
        List<InventoryEntity> noExpiryInventories = availableInventories.stream()
                .filter(inv -> inv.getLot().getExpiryDate() == null)
                .sorted(Comparator.comparing(inv -> inv.getLot().getCreatedAt()))
                .collect(Collectors.toList());

        sortedInventories.addAll(noExpiryInventories);

        return allocateQuantity(sortedInventories, requiredQuantity, "FEFO");
    }

    /**
     * 특정 LOT 지정 선택
     * 사용자가 직접 LOT을 지정하는 경우
     *
     * @param tenantId 테넌트 ID
     * @param warehouseId 창고 ID
     * @param productId 제품 ID
     * @param lotId LOT ID
     * @param requiredQuantity 필요 수량
     * @return LOT 할당 (단일)
     */
    public LotAllocation selectSpecificLot(
            String tenantId,
            Long warehouseId,
            Long productId,
            Long lotId,
            BigDecimal requiredQuantity) {

        log.info("Specific LOT selection - Tenant: {}, LOT: {}, Required: {}",
                tenantId, lotId, requiredQuantity);

        InventoryEntity inventory = inventoryRepository
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndLot_LotId(
                        warehouseId, productId, lotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));

        if (inventory.getAvailableQuantity().compareTo(requiredQuantity) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
        }

        return LotAllocation.builder()
                .lotId(lotId)
                .lotNo(inventory.getLot().getLotNo())
                .allocatedQuantity(requiredQuantity)
                .availableQuantity(inventory.getAvailableQuantity())
                .expiryDate(inventory.getLot().getExpiryDate())
                .build();
    }

    /**
     * 만료 예정 LOT 조회
     * 지정된 일수 내에 유효기간이 만료되는 LOT 목록 반환
     *
     * @param tenantId 테넌트 ID
     * @param daysUntilExpiry 만료까지 남은 일수 (예: 30일)
     * @return 만료 예정 LOT 목록
     */
    public List<LotEntity> findExpiringLots(String tenantId, int daysUntilExpiry) {
        LocalDate expiryThreshold = LocalDate.now().plusDays(daysUntilExpiry);

        return lotRepository.findByTenant_TenantIdAndExpiryDateBeforeAndIsActiveTrue(
                tenantId, expiryThreshold);
    }

    /**
     * 수량 할당 로직
     * 정렬된 재고 목록에서 필요 수량만큼 할당
     *
     * @param sortedInventories 정렬된 재고 목록
     * @param requiredQuantity 필요 수량
     * @param strategy 전략명 (로깅용)
     * @return LOT 할당 목록
     */
    private List<LotAllocation> allocateQuantity(
            List<InventoryEntity> sortedInventories,
            BigDecimal requiredQuantity,
            String strategy) {

        List<LotAllocation> allocations = new ArrayList<>();
        BigDecimal remainingQuantity = requiredQuantity;

        for (InventoryEntity inventory : sortedInventories) {
            if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal availableQty = inventory.getAvailableQuantity();
            BigDecimal allocatedQty = remainingQuantity.min(availableQty);

            LotAllocation allocation = LotAllocation.builder()
                    .lotId(inventory.getLot().getLotId())
                    .lotNo(inventory.getLot().getLotNo())
                    .allocatedQuantity(allocatedQty)
                    .availableQuantity(availableQty)
                    .expiryDate(inventory.getLot().getExpiryDate())
                    .build();

            allocations.add(allocation);
            remainingQuantity = remainingQuantity.subtract(allocatedQty);

            log.debug("{} allocation - LOT: {}, Allocated: {}, Remaining: {}",
                    strategy, inventory.getLot().getLotNo(), allocatedQty, remainingQuantity);
        }

        // 할당 불가능한 경우
        if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            log.error("Insufficient inventory - Required: {}, Allocated: {}, Short: {}",
                    requiredQuantity, requiredQuantity.subtract(remainingQuantity), remainingQuantity);
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
        }

        log.info("{} allocation complete - Total lots: {}, Total quantity: {}",
                strategy, allocations.size(), requiredQuantity);

        return allocations;
    }

    /**
     * LOT 할당 결과 클래스
     */
    @lombok.Getter
    @lombok.Builder
    public static class LotAllocation {
        private Long lotId;
        private String lotNo;
        private BigDecimal allocatedQuantity;
        private BigDecimal availableQuantity;
        private LocalDate expiryDate;
    }
}
