package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Work Order Service
 * 작업 지시 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkResultRepository workResultRepository;
    private final BomRepository bomRepository;
    private final InventoryService inventoryService;
    private final MaterialRequestService materialRequestService;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    /**
     * Find all work orders by tenant ID
     */
    public List<WorkOrderEntity> findByTenant(String tenantId) {
        return workOrderRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find work orders by tenant and status
     */
    public List<WorkOrderEntity> findByTenantAndStatus(String tenantId, String status) {
        return workOrderRepository.findByTenantIdAndStatusWithAllRelations(tenantId, status);
    }

    /**
     * Find work orders by date range
     */
    public List<WorkOrderEntity> findByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return workOrderRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Find work order by ID
     */
    public Optional<WorkOrderEntity> findById(Long workOrderId) {
        return workOrderRepository.findById(workOrderId);
    }

    /**
     * Find work order by ID with all relationships eagerly loaded
     */
    public Optional<WorkOrderEntity> findByIdWithAllRelations(Long workOrderId) {
        return workOrderRepository.findByIdWithAllRelations(workOrderId);
    }

    /**
     * Find work order by work order number
     */
    public Optional<WorkOrderEntity> findByWorkOrderNo(String tenantId, String workOrderNo) {
        return workOrderRepository.findByTenant_TenantIdAndWorkOrderNo(tenantId, workOrderNo);
    }

    /**
     * Create new work order
     */
    @Transactional
    public WorkOrderEntity createWorkOrder(WorkOrderEntity workOrder) {
        log.info("Creating work order: {} for tenant: {}",
            workOrder.getWorkOrderNo(), workOrder.getTenant().getTenantId());

        // Check duplicate
        if (workOrderRepository.existsByTenantAndWorkOrderNo(workOrder.getTenant(), workOrder.getWorkOrderNo())) {
            throw new IllegalArgumentException("Work order number already exists: " + workOrder.getWorkOrderNo());
        }

        // Set default values
        if (workOrder.getActualQuantity() == null) {
            workOrder.setActualQuantity(BigDecimal.ZERO);
        }
        if (workOrder.getGoodQuantity() == null) {
            workOrder.setGoodQuantity(BigDecimal.ZERO);
        }
        if (workOrder.getDefectQuantity() == null) {
            workOrder.setDefectQuantity(BigDecimal.ZERO);
        }

        return workOrderRepository.save(workOrder);
    }

    /**
     * Update work order
     */
    @Transactional
    public WorkOrderEntity updateWorkOrder(WorkOrderEntity workOrder) {
        log.info("Updating work order: {}", workOrder.getWorkOrderId());

        if (!workOrderRepository.existsById(workOrder.getWorkOrderId())) {
            throw new IllegalArgumentException("Work order not found: " + workOrder.getWorkOrderId());
        }

        return workOrderRepository.save(workOrder);
    }

    /**
     * Delete work order
     */
    @Transactional
    public void deleteWorkOrder(Long workOrderId) {
        log.info("Deleting work order: {}", workOrderId);
        workOrderRepository.deleteById(workOrderId);
    }

    /**
     * Start work order (상태: READY -> IN_PROGRESS)
     */
    @Transactional
    public WorkOrderEntity startWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        if (!"READY".equals(workOrder.getStatus()) && !"PENDING".equals(workOrder.getStatus())) {
            throw new IllegalStateException("Work order must be in READY or PENDING status to start");
        }

        workOrder.setStatus("IN_PROGRESS");
        workOrder.setActualStartDate(LocalDateTime.now());

        log.info("Work order {} started", workOrder.getWorkOrderNo());
        return workOrderRepository.save(workOrder);
    }

    /**
     * Complete work order (상태: IN_PROGRESS -> COMPLETED)
     */
    @Transactional
    public WorkOrderEntity completeWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        if (!"IN_PROGRESS".equals(workOrder.getStatus())) {
            throw new IllegalStateException("Work order must be in IN_PROGRESS status to complete");
        }

        workOrder.setStatus("COMPLETED");
        workOrder.setActualEndDate(LocalDateTime.now());

        log.info("Work order {} completed", workOrder.getWorkOrderNo());
        return workOrderRepository.save(workOrder);
    }

    /**
     * Cancel work order
     */
    @Transactional
    public WorkOrderEntity cancelWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        if ("COMPLETED".equals(workOrder.getStatus()) || "CANCELLED".equals(workOrder.getStatus())) {
            throw new IllegalStateException("Cannot cancel a completed or already cancelled work order");
        }

        workOrder.setStatus("CANCELLED");

        log.info("Work order {} cancelled", workOrder.getWorkOrderNo());
        return workOrderRepository.save(workOrder);
    }

    /**
     * Recalculate work order aggregates from work results
     * 작업 실적으로부터 집계 재계산
     */
    @Transactional
    public void recalculateAggregates(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        List<WorkResultEntity> results = workResultRepository.findByWorkOrder(workOrder);

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalGood = BigDecimal.ZERO;
        BigDecimal totalDefect = BigDecimal.ZERO;

        for (WorkResultEntity result : results) {
            totalQuantity = totalQuantity.add(result.getQuantity());
            totalGood = totalGood.add(result.getGoodQuantity());
            totalDefect = totalDefect.add(result.getDefectQuantity());
        }

        workOrder.setActualQuantity(totalQuantity);
        workOrder.setGoodQuantity(totalGood);
        workOrder.setDefectQuantity(totalDefect);

        workOrderRepository.save(workOrder);
        log.info("Recalculated aggregates for work order {}: quantity={}, good={}, defect={}",
            workOrder.getWorkOrderNo(), totalQuantity, totalGood, totalDefect);
    }

    /**
     * Count work orders by tenant and status
     */
    public long countByTenantAndStatus(String tenantId, String status) {
        return workOrderRepository.countByTenant_TenantIdAndStatus(tenantId, status);
    }

    // ================== Material Management Integration ==================

    /**
     * Calculate material requirements based on BOM
     * BOM 기반 자재 소요량 계산
     *
     * @param workOrderId Work order ID
     * @return Map of material product ID to required quantity
     */
    public Map<Long, MaterialRequirement> calculateMaterialRequirements(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(workOrderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        // Find active BOM for the product
        List<BomEntity> boms = bomRepository.findByTenantIdAndProductIdWithAllRelations(
            workOrder.getTenant().getTenantId(),
            workOrder.getProduct().getProductId()
        );

        BomEntity activeBom = boms.stream()
            .filter(BomEntity::getIsActive)
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.BOM_NOT_FOUND));

        Map<Long, MaterialRequirement> requirements = new HashMap<>();

        for (BomDetailEntity detail : activeBom.getDetails()) {
            BigDecimal baseQuantity = detail.getQuantity();
            BigDecimal usageRate = detail.getUsageRate() != null ? detail.getUsageRate() : BigDecimal.ONE;
            BigDecimal scrapRate = detail.getScrapRate() != null ? detail.getScrapRate() : BigDecimal.ZERO;

            // Calculate required quantity with scrap consideration
            // Required = (Base Quantity * Production Quantity) * Usage Rate * (1 + Scrap Rate)
            BigDecimal requiredQty = baseQuantity
                .multiply(workOrder.getTargetQuantity())
                .multiply(usageRate)
                .multiply(BigDecimal.ONE.add(scrapRate))
                .setScale(3, RoundingMode.HALF_UP);

            MaterialRequirement requirement = MaterialRequirement.builder()
                .materialProduct(detail.getMaterialProduct())
                .requiredQuantity(requiredQty)
                .unit(detail.getUnit())
                .bomDetail(detail)
                .build();

            requirements.put(detail.getMaterialProduct().getProductId(), requirement);
        }

        log.info("Calculated material requirements for work order {}: {} materials",
            workOrder.getWorkOrderNo(), requirements.size());

        return requirements;
    }

    /**
     * Reserve materials for work order
     * 작업 지시에 필요한 자재 예약
     *
     * @param workOrderId Work order ID
     * @param warehouseId Warehouse to reserve from
     * @return List of reserved inventory IDs
     */
    @Transactional
    public List<Long> reserveMaterials(Long workOrderId, Long warehouseId) {
        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(workOrderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        if (!"PENDING".equals(workOrder.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        Map<Long, MaterialRequirement> requirements = calculateMaterialRequirements(workOrderId);
        List<Long> reservedInventoryIds = new ArrayList<>();

        for (MaterialRequirement requirement : requirements.values()) {
            try {
                InventoryEntity reserved = inventoryService.reserveInventory(
                    workOrder.getTenant().getTenantId(),
                    warehouseId,
                    requirement.getMaterialProduct().getProductId(),
                    null, // LOT ID (FIFO will be applied in inventory service)
                    requirement.getRequiredQuantity()
                );

                reservedInventoryIds.add(reserved.getInventoryId());

                log.info("Reserved {} {} of {} for work order {}",
                    requirement.getRequiredQuantity(),
                    requirement.getUnit(),
                    requirement.getMaterialProduct().getProductCode(),
                    workOrder.getWorkOrderNo());

            } catch (Exception e) {
                // Rollback all reservations if any fails
                log.error("Failed to reserve materials for work order {}: {}",
                    workOrder.getWorkOrderNo(), e.getMessage());
                throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
            }
        }

        // Update work order status to READY
        workOrder.setStatus("READY");
        workOrderRepository.save(workOrder);

        log.info("Reserved all materials for work order {}: {} items",
            workOrder.getWorkOrderNo(), reservedInventoryIds.size());

        return reservedInventoryIds;
    }

    /**
     * Create material requests for work order
     * 작업 지시에 필요한 자재 출고 요청 생성
     *
     * @param workOrderId Work order ID
     * @param warehouseId Source warehouse ID
     * @param requesterUserId Requester user ID
     * @return Created material request
     */
    @Transactional
    public MaterialRequestEntity createMaterialRequests(Long workOrderId, Long warehouseId, Long requesterUserId) {
        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(workOrderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        WarehouseEntity warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        UserEntity requester = userRepository.findById(requesterUserId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        Map<Long, MaterialRequirement> requirements = calculateMaterialRequirements(workOrderId);

        // Create material request header
        String requestNo = generateMaterialRequestNo(workOrder.getTenant().getTenantId());

        MaterialRequestEntity request = MaterialRequestEntity.builder()
            .tenant(workOrder.getTenant())
            .requestNo(requestNo)
            .requestDate(LocalDateTime.now())
            .workOrder(workOrder)
            .workOrderNo(workOrder.getWorkOrderNo())
            .requester(requester)
            .requesterName(requester.getUsername())
            .warehouse(warehouse)
            .requestStatus("PENDING")
            .priority("NORMAL")
            .requiredDate(workOrder.getPlannedStartDate().toLocalDate())
            .isActive(true)
            .build();

        // Create material request items
        for (MaterialRequirement requirement : requirements.values()) {
            MaterialRequestItemEntity item = MaterialRequestItemEntity.builder()
                .materialRequest(request)
                .product(requirement.getMaterialProduct())
                .productCode(requirement.getMaterialProduct().getProductCode())
                .productName(requirement.getMaterialProduct().getProductName())
                .requestedQuantity(requirement.getRequiredQuantity())
                .unit(requirement.getUnit())
                .itemStatus("PENDING")
                .build();

            request.addItem(item);
        }

        MaterialRequestEntity created = materialRequestService.createMaterialRequest(request);

        log.info("Created material request {} for work order {} with {} items",
            created.getRequestNo(), workOrder.getWorkOrderNo(), created.getItems().size());

        return created;
    }

    /**
     * Release reserved materials (cancel work order)
     * 예약된 자재 해제 (작업 지시 취소 시)
     *
     * @param workOrderId Work order ID
     * @param warehouseId Warehouse ID
     */
    @Transactional
    public void releaseMaterials(Long workOrderId, Long warehouseId) {
        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(workOrderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));

        Map<Long, MaterialRequirement> requirements = calculateMaterialRequirements(workOrderId);

        for (MaterialRequirement requirement : requirements.values()) {
            try {
                inventoryService.releaseReservedInventory(
                    workOrder.getTenant().getTenantId(),
                    warehouseId,
                    requirement.getMaterialProduct().getProductId(),
                    null, // LOT ID
                    requirement.getRequiredQuantity()
                );

                log.info("Released {} {} of {} for cancelled work order {}",
                    requirement.getRequiredQuantity(),
                    requirement.getUnit(),
                    requirement.getMaterialProduct().getProductCode(),
                    workOrder.getWorkOrderNo());

            } catch (Exception e) {
                log.error("Failed to release material reservation: {}", e.getMessage());
                // Continue with other materials even if one fails
            }
        }

        log.info("Released all material reservations for work order {}", workOrder.getWorkOrderNo());
    }

    /**
     * Generate material request number: MR-YYYYMMDD-0001
     */
    private String generateMaterialRequestNo(String tenantId) {
        String datePrefix = "MR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // This is a simplified version - actual implementation should query existing requests
        return datePrefix + "-" + String.format("%04d", (int)(Math.random() * 9999) + 1);
    }

    // ================== Inner Classes ==================

    /**
     * Material Requirement DTO
     * 자재 소요 정보
     */
    @lombok.Data
    @lombok.Builder
    public static class MaterialRequirement {
        private ProductEntity materialProduct;
        private BigDecimal requiredQuantity;
        private String unit;
        private BomDetailEntity bomDetail;
    }
}
