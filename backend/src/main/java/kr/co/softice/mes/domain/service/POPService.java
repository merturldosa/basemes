package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.pop.*;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final WorkProgressRepository workProgressRepository;
    private final PauseResumeRepository pauseResumeRepository;
    private final DefectRepository defectRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final WorkResultRepository workResultRepository;
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
            return workOrderRepository.findByTenant_TenantIdAndAssignedUser_UserId(tenantId, operatorId)
                .stream()
                .filter(wo -> "READY".equals(wo.getStatus()) || "IN_PROGRESS".equals(wo.getStatus()))
                .collect(Collectors.toList());
        } else {
            // Return all ready and in-progress work orders
            return workOrderRepository.findByTenant_TenantIdWithAllRelations(tenantId)
                .stream()
                .filter(wo -> "READY".equals(wo.getStatus()) || "IN_PROGRESS".equals(wo.getStatus()))
                .collect(Collectors.toList());
        }
    }

    /**
     * Start work order
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @param operatorId Operator user ID
     * @return Created work progress response
     */
    public WorkProgressResponse startWorkOrder(String tenantId, Long workOrderId, Long operatorId) {
        // 1. Get work order
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        // 2. Validate tenant
        if (!tenantId.equals(workOrder.getTenant().getTenantId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Work order belongs to different tenant");
        }

        // 3. Check if already started
        if ("IN_PROGRESS".equals(workOrder.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "Work order is already in progress");
        }

        if ("COMPLETED".equals(workOrder.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "Work order is already completed");
        }

        // 4. Get operator
        UserEntity operator = userRepository.findById(operatorId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 5. Update work order status
        workOrder.setStatus("IN_PROGRESS");
        workOrder.setActualStartDate(LocalDateTime.now());
        if (workOrder.getAssignedUser() == null) {
            workOrder.setAssignedUser(operator);
        }
        workOrderRepository.save(workOrder);

        // 6. Create work progress record
        WorkProgressEntity progress = WorkProgressEntity.builder()
            .tenant(workOrder.getTenant())
            .workOrder(workOrder)
            .operator(operator)
            .recordDate(LocalDate.now())
            .startTime(LocalTime.now())
            .producedQuantity(BigDecimal.ZERO)
            .goodQuantity(BigDecimal.ZERO)
            .defectQuantity(BigDecimal.ZERO)
            .status("IN_PROGRESS")
            .pauseCount(0)
            .totalPauseDuration(0)
            .isActive(true)
            .build();

        WorkProgressEntity saved = workProgressRepository.save(progress);

        // 7. Broadcast real-time update
        broadcastWorkOrderUpdate(tenantId, workOrder);

        log.info("Work order started: {} by operator {}", workOrderId, operatorId);

        return convertToWorkProgressResponse(saved);
    }

    /**
     * Record work progress (production quantity)
     *
     * @param tenantId Tenant ID
     * @param request Work progress record request
     * @return Updated work progress response
     */
    public WorkProgressResponse recordProgress(String tenantId, WorkProgressRecordRequest request) {
        // 1. Get work progress
        WorkProgressEntity progress = workProgressRepository.findById(request.getProgressId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Work progress not found"));

        // 2. Validate tenant
        if (!tenantId.equals(progress.getTenant().getTenantId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Work progress belongs to different tenant");
        }

        // 3. Validate status
        if ("COMPLETED".equals(progress.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "Work progress is already completed");
        }

        // 4. Update produced quantity
        BigDecimal newProducedQuantity = progress.getProducedQuantity().add(request.getQuantity());
        progress.setProducedQuantity(newProducedQuantity);

        // Assume good quantity = produced quantity - defect quantity
        progress.setGoodQuantity(newProducedQuantity.subtract(progress.getDefectQuantity()));

        if (request.getNotes() != null) {
            progress.setWorkNotes(request.getNotes());
        }

        WorkProgressEntity saved = workProgressRepository.save(progress);

        // 5. Update work order aggregate quantities
        WorkOrderEntity workOrder = progress.getWorkOrder();
        workOrder.setActualQuantity(newProducedQuantity);
        workOrder.setGoodQuantity(progress.getGoodQuantity());
        workOrderRepository.save(workOrder);

        // 6. Broadcast real-time update
        broadcastWorkProgressUpdate(tenantId, saved);

        log.info("Work progress recorded: {} units for progress {}", request.getQuantity(), request.getProgressId());

        return convertToWorkProgressResponse(saved);
    }

    /**
     * Record defect
     *
     * @param tenantId Tenant ID
     * @param request Defect record request
     * @return Defect entity
     */
    public DefectEntity recordDefect(String tenantId, DefectRecordRequest request) {
        // 1. Get work progress
        WorkProgressEntity progress = workProgressRepository.findById(request.getProgressId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Work progress not found"));

        // 2. Validate tenant
        if (!tenantId.equals(progress.getTenant().getTenantId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Work progress belongs to different tenant");
        }

        // 3. Generate defect number
        String defectNo = generateDefectNo(tenantId);

        // 4. Create defect record
        DefectEntity defect = DefectEntity.builder()
            .tenant(progress.getTenant())
            .defectNo(defectNo)
            .defectDate(LocalDateTime.now())
            .sourceType("PRODUCTION")
            .workOrder(progress.getWorkOrder())
            .product(progress.getWorkOrder().getProduct())
            .productCode(progress.getWorkOrder().getProduct().getProductCode())
            .defectType(request.getDefectType())
            .defectQuantity(request.getDefectQuantity())
            .defectDescription(request.getDefectReason())
            .defectLocation(request.getDefectLocation())
            .severity(request.getSeverity() != null ? request.getSeverity() : "MINOR")
            .status("REPORTED")
            .reporterUser(progress.getOperator())
            .reporterName(progress.getOperator().getUsername())
            .remarks(request.getNotes())
            .isActive(true)
            .build();

        DefectEntity saved = defectRepository.save(defect);

        // 5. Update work progress defect quantity
        BigDecimal newDefectQuantity = progress.getDefectQuantity().add(request.getDefectQuantity());
        progress.setDefectQuantity(newDefectQuantity);
        progress.setGoodQuantity(progress.getProducedQuantity().subtract(newDefectQuantity));
        workProgressRepository.save(progress);

        // 6. Update work order aggregate defect quantity
        WorkOrderEntity workOrder = progress.getWorkOrder();
        workOrder.setDefectQuantity(newDefectQuantity);
        workOrder.setGoodQuantity(progress.getGoodQuantity());
        workOrderRepository.save(workOrder);

        // 7. Broadcast real-time update
        broadcastDefectUpdate(tenantId, saved);

        log.info("Defect recorded: {} units of type {} for progress {}",
            request.getDefectQuantity(), request.getDefectType(), request.getProgressId());

        return saved;
    }

    /**
     * Pause work
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @param request Pause work request
     * @return Updated work progress response
     */
    public WorkProgressResponse pauseWork(String tenantId, Long workOrderId, PauseWorkRequest request) {
        // 1. Get active work progress for this work order
        WorkProgressEntity progress = workProgressRepository.findByWorkOrder_WorkOrderIdAndIsActiveTrue(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "No active work progress found"));

        // 2. Validate tenant
        if (!tenantId.equals(progress.getTenant().getTenantId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Validate status
        if ("PAUSED".equals(progress.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "Work is already paused");
        }

        // 4. Update progress status
        progress.setStatus("PAUSED");
        workProgressRepository.save(progress);

        // 5. Create pause record
        PauseResumeEntity pauseResume = PauseResumeEntity.builder()
            .tenant(progress.getTenant())
            .workProgress(progress)
            .pauseTime(LocalDateTime.now())
            .pauseReason(request.getPauseReason())
            .pauseType(request.getPauseType() != null ? request.getPauseType() : "OTHER")
            .requiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : false)
            .build();

        pauseResumeRepository.save(pauseResume);

        // 6. Increment pause count
        progress.setPauseCount(progress.getPauseCount() + 1);
        WorkProgressEntity saved = workProgressRepository.save(progress);

        // 7. Broadcast update
        broadcastWorkProgressUpdate(tenantId, saved);

        log.info("Work paused for work order {}: {}", workOrderId, request.getPauseReason());

        return convertToWorkProgressResponse(saved);
    }

    /**
     * Resume work
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @return Updated work progress response
     */
    public WorkProgressResponse resumeWork(String tenantId, Long workOrderId) {
        // 1. Get active work progress
        WorkProgressEntity progress = workProgressRepository.findByWorkOrder_WorkOrderIdAndIsActiveTrue(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "No active work progress found"));

        // 2. Validate tenant
        if (!tenantId.equals(progress.getTenant().getTenantId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Validate status
        if (!"PAUSED".equals(progress.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "Work is not paused");
        }

        // 4. Find active pause (not resumed yet)
        PauseResumeEntity pauseResume = pauseResumeRepository.findByWorkProgress_ProgressIdAndResumeTimeIsNull(progress.getProgressId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "No active pause found"));

        // 5. Update pause record
        LocalDateTime resumeTime = LocalDateTime.now();
        pauseResume.setResumeTime(resumeTime);

        // Calculate duration in minutes
        long durationMinutes = Duration.between(pauseResume.getPauseTime(), resumeTime).toMinutes();
        pauseResume.setDurationMinutes((int) durationMinutes);
        pauseResumeRepository.save(pauseResume);

        // 6. Update progress status and total pause duration
        progress.setStatus("IN_PROGRESS");
        progress.setTotalPauseDuration(progress.getTotalPauseDuration() + (int) durationMinutes);
        WorkProgressEntity saved = workProgressRepository.save(progress);

        // 7. Broadcast update
        broadcastWorkProgressUpdate(tenantId, saved);

        log.info("Work resumed for work order {}, pause duration: {} minutes", workOrderId, durationMinutes);

        return convertToWorkProgressResponse(saved);
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

        // 2. Validate tenant
        if (!tenantId.equals(workOrder.getTenant().getTenantId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Validate status
        if ("COMPLETED".equals(workOrder.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "Work order is already completed");
        }

        // 4. Get active work progress
        WorkProgressEntity progress = workProgressRepository.findByWorkOrder_WorkOrderIdAndIsActiveTrue(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "No active work progress found"));

        // 5. Complete work progress
        progress.setStatus("COMPLETED");
        progress.setEndTime(LocalTime.now());
        progress.setIsActive(false);
        workProgressRepository.save(progress);

        // 6. Create work result record
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(progress.getRecordDate(), progress.getStartTime());
        long workDuration = Duration.between(startDateTime, now).toMinutes();

        WorkResultEntity workResult = WorkResultEntity.builder()
            .workOrder(workOrder)
            .tenant(workOrder.getTenant())
            .resultDate(now)
            .quantity(progress.getProducedQuantity())
            .goodQuantity(progress.getGoodQuantity())
            .defectQuantity(progress.getDefectQuantity())
            .workStartTime(startDateTime)
            .workEndTime(now)
            .workDuration((int) workDuration)
            .worker(progress.getOperator())
            .workerName(progress.getOperator().getUsername())
            .remarks(remarks)
            .build();

        workResultRepository.save(workResult);

        // 7. Update work order status
        workOrder.setStatus("COMPLETED");
        workOrder.setActualEndDate(now);
        workOrder.setRemarks(remarks);
        WorkOrderEntity saved = workOrderRepository.save(workOrder);

        // 8. Broadcast update
        broadcastWorkOrderUpdate(tenantId, saved);

        log.info("Work order completed: {} with remarks: {}", workOrderId, remarks);

        return saved;
    }

    /**
     * Get work progress by work order
     *
     * @param tenantId Tenant ID
     * @param workOrderId Work order ID
     * @return Work progress response
     */
    @Transactional(readOnly = true)
    public WorkProgressResponse getWorkProgress(String tenantId, Long workOrderId) {
        WorkProgressEntity progress = workProgressRepository.findByWorkOrder_WorkOrderIdAndIsActiveTrue(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "No active work progress found"));

        if (!tenantId.equals(progress.getTenant().getTenantId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return convertToWorkProgressResponse(progress);
    }

    /**
     * Get today's production statistics
     *
     * @param tenantId Tenant ID
     * @param operatorId Optional operator ID filter
     * @return Production statistics
     */
    @Transactional(readOnly = true)
    public ProductionStatisticsResponse getTodayStatistics(String tenantId, Long operatorId) {
        LocalDate today = LocalDate.now();

        List<WorkProgressEntity> todayProgress;
        if (operatorId != null) {
            todayProgress = workProgressRepository.findByOperator_UserIdAndRecordDate(operatorId, today);
        } else {
            todayProgress = workProgressRepository.findByTenant_TenantIdAndRecordDate(tenantId, today);
        }

        // Calculate statistics
        BigDecimal totalProduced = todayProgress.stream()
            .map(WorkProgressEntity::getProducedQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGood = todayProgress.stream()
            .map(WorkProgressEntity::getGoodQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDefects = todayProgress.stream()
            .map(WorkProgressEntity::getDefectQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedOrders = todayProgress.stream()
            .filter(wp -> "COMPLETED".equals(wp.getStatus()))
            .map(WorkProgressEntity::getWorkOrder)
            .distinct()
            .count();

        long inProgressOrders = todayProgress.stream()
            .filter(wp -> "IN_PROGRESS".equals(wp.getStatus()) || "PAUSED".equals(wp.getStatus()))
            .map(WorkProgressEntity::getWorkOrder)
            .distinct()
            .count();

        // Calculate rates
        double defectRate = totalProduced.compareTo(BigDecimal.ZERO) > 0
            ? totalDefects.divide(totalProduced, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        double yieldRate = totalProduced.compareTo(BigDecimal.ZERO) > 0
            ? totalGood.divide(totalProduced, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        // Calculate time metrics
        int totalWorkMinutes = todayProgress.stream()
            .mapToInt(wp -> {
                if (wp.getEndTime() != null) {
                    return (int) Duration.between(wp.getStartTime(), wp.getEndTime()).toMinutes();
                } else {
                    return (int) Duration.between(wp.getStartTime(), LocalTime.now()).toMinutes();
                }
            })
            .sum();

        int totalPauseMinutes = todayProgress.stream()
            .mapToInt(WorkProgressEntity::getTotalPauseDuration)
            .sum();

        double efficiency = totalWorkMinutes > 0
            ? ((double) (totalWorkMinutes - totalPauseMinutes) / totalWorkMinutes) * 100
            : 0.0;

        ProductionStatisticsResponse.ProductionStatisticsResponseBuilder builder = ProductionStatisticsResponse.builder()
            .date(today)
            .tenantId(tenantId)
            .totalProduced(totalProduced)
            .totalGood(totalGood)
            .totalDefects(totalDefects)
            .completedWorkOrders(completedOrders)
            .inProgressWorkOrders(inProgressOrders)
            .defectRate(defectRate)
            .yieldRate(yieldRate)
            .totalWorkMinutes(totalWorkMinutes)
            .totalPauseMinutes(totalPauseMinutes)
            .efficiency(efficiency);

        if (operatorId != null) {
            UserEntity operator = userRepository.findById(operatorId).orElse(null);
            if (operator != null) {
                builder.operatorUserId(operatorId);
                builder.operatorUserName(operator.getUsername());
            }
        }

        return builder.build();
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
        switch (type.toUpperCase()) {
            case "WORK_ORDER":
                return workOrderRepository.findByTenant_TenantIdAndWorkOrderNo(tenantId, barcode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND,
                        "Work order not found: " + barcode));

            case "MATERIAL":
            case "PRODUCT":
            case "LOT":
                throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Scan type not yet implemented: " + type);

            default:
                throw new BusinessException(ErrorCode.INVALID_OPERATION,
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

    private void broadcastWorkProgressUpdate(String tenantId, WorkProgressEntity progress) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/work-progress/" + tenantId,
                convertToWorkProgressResponse(progress)
            );
        } catch (Exception e) {
            log.error("Failed to broadcast work progress update: {}", e.getMessage());
        }
    }

    private void broadcastDefectUpdate(String tenantId, DefectEntity defect) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/defects/" + tenantId,
                defect
            );
        } catch (Exception e) {
            log.error("Failed to broadcast defect update: {}", e.getMessage());
        }
    }

    private WorkProgressResponse convertToWorkProgressResponse(WorkProgressEntity entity) {
        WorkOrderEntity workOrder = entity.getWorkOrder();

        BigDecimal plannedQuantity = workOrder.getPlannedQuantity();
        double completionRate = plannedQuantity.compareTo(BigDecimal.ZERO) > 0
            ? entity.getProducedQuantity().divide(plannedQuantity, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        double defectRate = entity.getProducedQuantity().compareTo(BigDecimal.ZERO) > 0
            ? entity.getDefectQuantity().divide(entity.getProducedQuantity(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        return WorkProgressResponse.builder()
            .progressId(entity.getProgressId())
            .tenantId(entity.getTenant().getTenantId())
            .workOrderId(workOrder.getWorkOrderId())
            .workOrderNo(workOrder.getWorkOrderNo())
            .productName(workOrder.getProduct().getProductName())
            .productCode(workOrder.getProduct().getProductCode())
            .processName(workOrder.getProcess().getProcessName())
            .operatorUserId(entity.getOperator().getUserId())
            .operatorUserName(entity.getOperator().getUsername())
            .recordDate(entity.getRecordDate())
            .startTime(entity.getStartTime())
            .endTime(entity.getEndTime())
            .producedQuantity(entity.getProducedQuantity())
            .goodQuantity(entity.getGoodQuantity())
            .defectQuantity(entity.getDefectQuantity())
            .plannedQuantity(plannedQuantity)
            .completionRate(completionRate)
            .defectRate(defectRate)
            .status(entity.getStatus())
            .pauseCount(entity.getPauseCount())
            .totalPauseDuration(entity.getTotalPauseDuration())
            .workNotes(entity.getWorkNotes())
            .isActive(entity.getIsActive())
            .equipmentId(entity.getEquipment() != null ? entity.getEquipment().getEquipmentId() : null)
            .equipmentName(entity.getEquipment() != null ? entity.getEquipment().getEquipmentName() : null)
            .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
            .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)
            .build();
    }

    private String generateDefectNo(String tenantId) {
        String prefix = "DEF-" + LocalDate.now().toString().replace("-", "") + "-";
        long count = defectRepository.countByTenant_TenantIdAndDefectNoStartingWith(tenantId, prefix);
        return prefix + String.format("%04d", count + 1);
    }
}
