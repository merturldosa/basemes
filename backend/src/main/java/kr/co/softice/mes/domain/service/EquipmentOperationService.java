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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Equipment Operation Service
 * 설비 가동 이력 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EquipmentOperationService {

    private final EquipmentOperationRepository operationRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkResultRepository workResultRepository;
    private final UserRepository userRepository;

    /**
     * Get all operations for tenant
     */
    public List<EquipmentOperationEntity> getAllOperations(String tenantId) {
        log.info("Getting all operations for tenant: {}", tenantId);
        return operationRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get operation by ID
     */
    public EquipmentOperationEntity getOperationById(Long operationId) {
        log.info("Getting operation by ID: {}", operationId);
        return operationRepository.findByIdWithAllRelations(operationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_OPERATION_NOT_FOUND));
    }

    /**
     * Get operations by equipment
     */
    public List<EquipmentOperationEntity> getOperationsByEquipment(Long equipmentId) {
        log.info("Getting operations for equipment ID: {}", equipmentId);
        return operationRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Get operations by date range
     */
    public List<EquipmentOperationEntity> getOperationsByDateRange(String tenantId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting operations for tenant: {} from {} to {}", tenantId, startDate, endDate);
        return operationRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Get operations by status
     */
    public List<EquipmentOperationEntity> getOperationsByStatus(String tenantId, String operationStatus) {
        log.info("Getting operations for tenant: {} with status: {}", tenantId, operationStatus);
        return operationRepository.findByTenantIdAndOperationStatus(tenantId, operationStatus);
    }

    /**
     * Create operation
     */
    @Transactional
    public EquipmentOperationEntity createOperation(String tenantId, EquipmentOperationEntity operation) {
        log.info("Creating operation for tenant: {}", tenantId);

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        operation.setTenant(tenant);

        // Get equipment
        EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(operation.getEquipment().getEquipmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
        operation.setEquipment(equipment);

        // Set optional relations
        if (operation.getWorkOrder() != null && operation.getWorkOrder().getWorkOrderId() != null) {
            WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(operation.getWorkOrder().getWorkOrderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));
            operation.setWorkOrder(workOrder);
        }

        if (operation.getWorkResult() != null && operation.getWorkResult().getWorkResultId() != null) {
            WorkResultEntity workResult = workResultRepository.findById(operation.getWorkResult().getWorkResultId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_RESULT_NOT_FOUND));
            operation.setWorkResult(workResult);
        }

        if (operation.getOperatorUser() != null && operation.getOperatorUser().getUserId() != null) {
            UserEntity operator = userRepository.findById(operation.getOperatorUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            operation.setOperatorUser(operator);
            operation.setOperatorName(operator.getFullName());
        }

        // Set defaults
        if (operation.getProductionQuantity() == null) {
            operation.setProductionQuantity(BigDecimal.ZERO);
        }
        if (operation.getGoodQuantity() == null) {
            operation.setGoodQuantity(BigDecimal.ZERO);
        }
        if (operation.getDefectQuantity() == null) {
            operation.setDefectQuantity(BigDecimal.ZERO);
        }
        if (operation.getOperationStatus() == null) {
            operation.setOperationStatus("RUNNING");
        }

        EquipmentOperationEntity saved = operationRepository.save(operation);
        log.info("Operation created successfully for equipment: {}", equipment.getEquipmentCode());
        return saved;
    }

    /**
     * Complete operation (calculate OEE)
     */
    @Transactional
    public EquipmentOperationEntity completeOperation(Long operationId) {
        log.info("Completing operation ID: {}", operationId);

        EquipmentOperationEntity operation = operationRepository.findByIdWithAllRelations(operationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_OPERATION_NOT_FOUND));

        // Set end time if not set
        if (operation.getEndTime() == null) {
            operation.setEndTime(LocalDateTime.now());
        }

        operation.setOperationStatus("COMPLETED");

        // Calculate OEE
        calculateOEE(operation);

        EquipmentOperationEntity updated = operationRepository.save(operation);
        log.info("Operation completed successfully with OEE: {}", updated.getOee());
        return updated;
    }

    /**
     * Calculate OEE (Overall Equipment Effectiveness)
     * OEE = Availability × Performance × Quality
     */
    private void calculateOEE(EquipmentOperationEntity operation) {
        try {
            // Quality Rate = (Good Quantity / Production Quantity) × 100
            BigDecimal qualityRate = BigDecimal.ZERO;
            if (operation.getProductionQuantity() != null && operation.getProductionQuantity().compareTo(BigDecimal.ZERO) > 0) {
                qualityRate = operation.getGoodQuantity()
                        .divide(operation.getProductionQuantity(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                operation.setQualityRate(qualityRate.setScale(2, RoundingMode.HALF_UP));
            }

            // Utilization Rate (Availability) = ((Operation Hours - Stop Duration) / Operation Hours) × 100
            BigDecimal utilizationRate = BigDecimal.ZERO;
            if (operation.getOperationHours() != null && operation.getOperationHours().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal stopHours = BigDecimal.ZERO;
                if (operation.getStopDurationMinutes() != null) {
                    stopHours = BigDecimal.valueOf(operation.getStopDurationMinutes()).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
                }
                BigDecimal effectiveHours = operation.getOperationHours().subtract(stopHours);
                utilizationRate = effectiveHours
                        .divide(operation.getOperationHours(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                operation.setUtilizationRate(utilizationRate.setScale(2, RoundingMode.HALF_UP));
            }

            // Performance Rate = (Standard Cycle Time / Actual Cycle Time) × 100
            BigDecimal performanceRate = BigDecimal.valueOf(100); // Default to 100%
            EquipmentEntity equipment = operation.getEquipment();
            if (equipment.getStandardCycleTime() != null &&
                equipment.getStandardCycleTime().compareTo(BigDecimal.ZERO) > 0 &&
                operation.getCycleTime() != null &&
                operation.getCycleTime().compareTo(BigDecimal.ZERO) > 0) {

                performanceRate = equipment.getStandardCycleTime()
                        .divide(operation.getCycleTime(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                // Cap at 100%
                if (performanceRate.compareTo(BigDecimal.valueOf(100)) > 0) {
                    performanceRate = BigDecimal.valueOf(100);
                }
                operation.setPerformanceRate(performanceRate.setScale(2, RoundingMode.HALF_UP));
            }

            // OEE = Utilization Rate × Performance Rate × Quality Rate / 10000
            BigDecimal oee = utilizationRate
                    .multiply(performanceRate)
                    .multiply(qualityRate)
                    .divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
            operation.setOee(oee);

            log.info("OEE calculated: Utilization={}%, Performance={}%, Quality={}%, OEE={}%",
                    utilizationRate, performanceRate, qualityRate, oee);

        } catch (Exception e) {
            log.error("Error calculating OEE: {}", e.getMessage());
            operation.setOee(BigDecimal.ZERO);
        }
    }

    /**
     * Update operation
     */
    @Transactional
    public EquipmentOperationEntity updateOperation(Long operationId, EquipmentOperationEntity updateData) {
        log.info("Updating operation ID: {}", operationId);

        EquipmentOperationEntity existing = operationRepository.findByIdWithAllRelations(operationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_OPERATION_NOT_FOUND));

        // Update fields
        if (updateData.getEndTime() != null) {
            existing.setEndTime(updateData.getEndTime());
        }
        if (updateData.getProductionQuantity() != null) {
            existing.setProductionQuantity(updateData.getProductionQuantity());
        }
        if (updateData.getGoodQuantity() != null) {
            existing.setGoodQuantity(updateData.getGoodQuantity());
        }
        if (updateData.getDefectQuantity() != null) {
            existing.setDefectQuantity(updateData.getDefectQuantity());
        }
        if (updateData.getStopReason() != null) {
            existing.setStopReason(updateData.getStopReason());
        }
        if (updateData.getStopDurationMinutes() != null) {
            existing.setStopDurationMinutes(updateData.getStopDurationMinutes());
        }
        if (updateData.getCycleTime() != null) {
            existing.setCycleTime(updateData.getCycleTime());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Recalculate OEE if completed
        if ("COMPLETED".equals(existing.getOperationStatus())) {
            calculateOEE(existing);
        }

        EquipmentOperationEntity updated = operationRepository.save(existing);
        log.info("Operation updated successfully");
        return updated;
    }

    /**
     * Delete operation
     */
    @Transactional
    public void deleteOperation(Long operationId) {
        log.info("Deleting operation ID: {}", operationId);

        EquipmentOperationEntity operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_OPERATION_NOT_FOUND));

        operationRepository.delete(operation);
        log.info("Operation deleted successfully");
    }
}
