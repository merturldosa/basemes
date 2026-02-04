package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * POP Service
 * Point of Production - Field operations service
 * Handles real-time work order execution, progress tracking, and defect recording
 * @author Moon Myung-seop
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class POPService {

    private final WorkOrderRepository workOrderRepository;
    // Note: WorkProgressRepository, DefectRepository need to be created
    // private final WorkProgressRepository workProgressRepository;
    // private final DefectRepository defectRepository;
    private final InventoryService inventoryService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Get active work orders for operator
     *
     * @param tenantId Tenant ID
     * @param operatorId Optional operator ID filter
     * @return List of active work orders
     */
    @Transactional(readOnly = true)
    public List<WorkOrderEntity> getActiveWorkOrders(String tenantId, Long operatorId) {
        if (operatorId != null) {
            // Return work orders assigned to specific operator
            return workOrderRepository.findByTenantIdAndOperatorUserId(tenantId, operatorId);
        } else {
            // Return all ready and in-progress work orders
            return workOrderRepository.findByTenantIdWithRelations(tenantId)
                .stream()
                .filter(wo -> "READY".equals(wo.getStatus()) || "IN_PROGRESS".equals(wo.getStatus()))
                .toList();
        }
    }

    /**
     * Start work order
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @param operatorId Operator user ID
     * @return Created work progress entity
     */
    public WorkProgressEntity startWorkOrder(String tenantId, Long workOrderId, Long operatorId) {
        // 1. Get work order
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        // 2. Validate status
        if (!"READY".equals(workOrder.getStatus())) {
            throw new BusinessException(ErrorCode.WORK_ORDER_ALREADY_STARTED,
                "Work order is already started or completed");
        }

        // 3. Update work order status
        workOrder.setStatus("IN_PROGRESS");
        workOrder.setActualStartDate(LocalDateTime.now());
        workOrder.setOperatorUserId(operatorId);
        workOrderRepository.save(workOrder);

        // 4. Create work progress record
        // Note: This requires WorkProgressEntity and WorkProgressRepository
        // WorkProgressEntity progress = WorkProgressEntity.builder()
        //     .tenant(workOrder.getTenant())
        //     .workOrder(workOrder)
        //     .operatorUserId(operatorId)
        //     .startTime(LocalDateTime.now())
        //     .producedQuantity(0)
        //     .defectQuantity(0)
        //     .status("IN_PROGRESS")
        //     .build();
        //
        // WorkProgressEntity saved = workProgressRepository.save(progress);

        // 5. Broadcast real-time update
        broadcastWorkOrderUpdate(tenantId, workOrder);

        log.info("Work order started: {} by operator {}", workOrderId, operatorId);

        // Temporary: Return mock progress entity
        return createMockWorkProgress(workOrder, operatorId);
    }

    /**
     * Record work progress (production quantity)
     *
     * @param tenantId Tenant ID
     * @param progressId Work progress ID
     * @param quantity Quantity produced
     * @param operatorId Operator user ID
     * @return Updated work progress entity
     */
    public WorkProgressEntity recordProgress(String tenantId, Long progressId, Integer quantity, Long operatorId) {
        // Note: This requires WorkProgressRepository
        // 1. Get work progress
        // WorkProgressEntity progress = workProgressRepository.findById(progressId)
        //     .orElseThrow(() -> new BusinessException(ErrorCode.WORK_PROGRESS_NOT_FOUND));
        //
        // 2. Update produced quantity
        // int newQuantity = progress.getProducedQuantity() + quantity;
        // progress.setProducedQuantity(newQuantity);
        // progress.setLastUpdateTime(LocalDateTime.now());
        //
        // WorkProgressEntity saved = workProgressRepository.save(progress);
        //
        // 3. Update work order produced quantity
        // WorkOrderEntity workOrder = progress.getWorkOrder();
        // workOrder.setProducedQuantity(newQuantity);
        // workOrderRepository.save(workOrder);
        //
        // 4. Broadcast real-time update
        // broadcastWorkProgressUpdate(tenantId, saved);

        log.info("Work progress recorded: {} units for progress {}", quantity, progressId);

        // Temporary: Return mock progress entity
        return createMockWorkProgress(null, operatorId);
    }

    /**
     * Record defect
     *
     * @param tenantId Tenant ID
     * @param progressId Work progress ID
     * @param quantity Defect quantity
     * @param defectType Type of defect
     * @param reason Defect reason
     * @param operatorId Operator user ID
     * @return Defect entity
     */
    public DefectEntity recordDefect(String tenantId, Long progressId, Integer quantity,
                                      String defectType, String reason, Long operatorId) {
        // Note: This requires DefectRepository and DefectEntity
        // 1. Get work progress
        // WorkProgressEntity progress = workProgressRepository.findById(progressId)
        //     .orElseThrow(() -> new BusinessException(ErrorCode.WORK_PROGRESS_NOT_FOUND));
        //
        // 2. Create defect record
        // DefectEntity defect = DefectEntity.builder()
        //     .tenant(progress.getTenant())
        //     .workOrder(progress.getWorkOrder())
        //     .defectType(defectType)
        //     .defectQuantity(quantity)
        //     .defectReason(reason)
        //     .detectedDate(LocalDateTime.now())
        //     .detectedBy(operatorId)
        //     .status("DETECTED")
        //     .build();
        //
        // DefectEntity saved = defectRepository.save(defect);
        //
        // 3. Update work progress defect quantity
        // int newDefectQuantity = progress.getDefectQuantity() + quantity;
        // progress.setDefectQuantity(newDefectQuantity);
        // workProgressRepository.save(progress);
        //
        // 4. Broadcast real-time update
        // broadcastDefectUpdate(tenantId, saved);

        log.info("Defect recorded: {} units of type {} for progress {}", quantity, defectType, progressId);

        // Temporary: Return mock defect entity
        return createMockDefect(quantity, defectType, reason);
    }

    /**
     * Pause work
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @param reason Pause reason
     * @return Updated work progress entity
     */
    public WorkProgressEntity pauseWork(String tenantId, Long workOrderId, String reason) {
        // Note: Requires WorkProgressRepository
        // WorkProgressEntity progress = getCurrentProgress(workOrderId);
        // progress.setStatus("PAUSED");
        // progress.setPauseTime(LocalDateTime.now());
        // progress.setPauseReason(reason);
        // WorkProgressEntity saved = workProgressRepository.save(progress);
        // broadcastWorkProgressUpdate(tenantId, saved);

        log.info("Work paused for work order {}: {}", workOrderId, reason);

        return createMockWorkProgress(null, null);
    }

    /**
     * Resume work
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @return Updated work progress entity
     */
    public WorkProgressEntity resumeWork(String tenantId, Long workOrderId) {
        // Note: Requires WorkProgressRepository
        // WorkProgressEntity progress = getCurrentProgress(workOrderId);
        // progress.setStatus("IN_PROGRESS");
        // progress.setResumeTime(LocalDateTime.now());
        // WorkProgressEntity saved = workProgressRepository.save(progress);
        // broadcastWorkProgressUpdate(tenantId, saved);

        log.info("Work resumed for work order {}", workOrderId);

        return createMockWorkProgress(null, null);
    }

    /**
     * Complete work order
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @param remarks Completion remarks
     * @return Completed work order
     */
    public WorkOrderEntity completeWorkOrder(String tenantId, Long workOrderId, String remarks) {
        // 1. Get work order
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        // 2. Update work order status
        workOrder.setStatus("COMPLETED");
        workOrder.setActualEndDate(LocalDateTime.now());
        WorkOrderEntity saved = workOrderRepository.save(workOrder);

        // 3. Complete progress (requires WorkProgressRepository)
        // WorkProgressEntity progress = getCurrentProgress(workOrderId);
        // progress.setStatus("COMPLETED");
        // progress.setEndTime(LocalDateTime.now());
        // workProgressRepository.save(progress);

        // 4. Update inventory (finished goods)
        // inventoryService.recordProduction(...)

        // 5. Broadcast real-time update
        broadcastWorkOrderUpdate(tenantId, saved);

        log.info("Work order completed: {} with remarks: {}", workOrderId, remarks);

        return saved;
    }

    /**
     * Get work progress by work order
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @return Work progress entity
     */
    @Transactional(readOnly = true)
    public WorkProgressEntity getWorkProgress(String tenantId, Long workOrderId) {
        // Note: Requires WorkProgressRepository
        // return workProgressRepository.findByWorkOrderId(workOrderId)
        //     .orElseThrow(() -> new BusinessException(ErrorCode.WORK_PROGRESS_NOT_FOUND));

        log.info("Getting work progress for work order {}", workOrderId);

        return createMockWorkProgress(null, null);
    }

    /**
     * Get today's production statistics
     *
     * @param tenantId Tenant ID
     * @param operatorId Optional operator ID filter
     * @return Production statistics
     */
    @Transactional(readOnly = true)
    public ProductionStatistics getTodayStatistics(String tenantId, Long operatorId) {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Note: This requires WorkProgressRepository
        // List<WorkProgressEntity> todayProgress = ...
        // Calculate statistics from work progress records

        log.info("Getting today's statistics for tenant {} and operator {}", tenantId, operatorId);

        // Temporary: Return mock statistics
        return ProductionStatistics.builder()
            .date(LocalDate.now())
            .totalProduced(0)
            .totalDefects(0)
            .completedOrders(0L)
            .defectRate(0.0)
            .build();
    }

    /**
     * Scan barcode
     *
     * @param tenantId Tenant ID
     * @param barcode Barcode string
     * @param type Scan type (WORK_ORDER, MATERIAL, PRODUCT, LOT)
     * @return Scanned entity
     */
    @Transactional(readOnly = true)
    public Object scanBarcode(String tenantId, String barcode, String type) {
        switch (type) {
            case "WORK_ORDER":
                return workOrderRepository.findByWorkOrderNo(barcode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND,
                        "Work order not found: " + barcode));

            case "MATERIAL":
            case "PRODUCT":
            case "LOT":
                // Note: Requires respective repositories
                throw new BusinessException(ErrorCode.NOT_IMPLEMENTED,
                    "Scan type not yet implemented: " + type);

            default:
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Invalid scan type: " + type);
        }
    }

    // Helper methods

    private void broadcastWorkOrderUpdate(String tenantId, WorkOrderEntity workOrder) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/work-orders/" + tenantId,
                workOrder
            );
        } catch (Exception e) {
            log.error("Failed to broadcast work order update: {}", e.getMessage());
        }
    }

    // Mock methods (temporary until entities are created)

    private WorkProgressEntity createMockWorkProgress(WorkOrderEntity workOrder, Long operatorId) {
        // This is a temporary mock - replace with actual entity when WorkProgressEntity is created
        log.warn("Using mock WorkProgressEntity - actual implementation required");
        return null; // Placeholder
    }

    private DefectEntity createMockDefect(Integer quantity, String defectType, String reason) {
        // This is a temporary mock - replace with actual entity when DefectEntity is created
        log.warn("Using mock DefectEntity - actual implementation required");
        return null; // Placeholder
    }

    /**
     * Production Statistics DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionStatistics {
        private LocalDate date;
        private Integer totalProduced;
        private Integer totalDefects;
        private Long completedOrders;
        private Double defectRate;
    }

    /**
     * Work Progress Entity (Placeholder)
     * TODO: Move to domain/entity package
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkProgressEntity {
        private Long progressId;
        private Long workOrderId;
        private Long operatorUserId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer producedQuantity;
        private Integer defectQuantity;
        private String status;
    }
}
