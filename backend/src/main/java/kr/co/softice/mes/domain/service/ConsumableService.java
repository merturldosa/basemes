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
import java.util.List;

/**
 * Consumable Service
 * 소모품 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ConsumableService {

    private final ConsumableRepository consumableRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentRepository equipmentRepository;

    /**
     * Get all consumables for tenant
     */
    public List<ConsumableEntity> getAllConsumables(String tenantId) {
        log.info("Getting all consumables for tenant: {}", tenantId);
        return consumableRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get consumable by ID
     */
    public ConsumableEntity getConsumableById(Long consumableId) {
        log.info("Getting consumable by ID: {}", consumableId);
        return consumableRepository.findByIdWithAllRelations(consumableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSUMABLE_NOT_FOUND));
    }

    /**
     * Get consumables with low stock (currentStock <= minimumStock)
     */
    public List<ConsumableEntity> getLowStock(String tenantId) {
        log.info("Getting low stock consumables for tenant: {}", tenantId);
        return consumableRepository.findLowStock(tenantId);
    }

    /**
     * Create consumable
     */
    @Transactional
    public ConsumableEntity createConsumable(String tenantId, ConsumableEntity consumable, Long equipmentId) {
        log.info("Creating consumable: {} for tenant: {}", consumable.getConsumableCode(), tenantId);

        // Check duplicate
        if (consumableRepository.existsByTenant_TenantIdAndConsumableCode(tenantId, consumable.getConsumableCode())) {
            throw new BusinessException(ErrorCode.CONSUMABLE_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        consumable.setTenant(tenant);

        // Set equipment (optional)
        if (equipmentId != null) {
            EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
            consumable.setEquipment(equipment);
        }

        // Set defaults
        if (consumable.getIsActive() == null) {
            consumable.setIsActive(true);
        }
        if (consumable.getStatus() == null) {
            consumable.setStatus("ACTIVE");
        }
        if (consumable.getCurrentStock() == null) {
            consumable.setCurrentStock(BigDecimal.ZERO);
        }
        if (consumable.getMinimumStock() == null) {
            consumable.setMinimumStock(BigDecimal.ZERO);
        }

        ConsumableEntity saved = consumableRepository.save(consumable);
        log.info("Consumable created successfully: {}", saved.getConsumableCode());
        return saved;
    }

    /**
     * Update consumable
     */
    @Transactional
    public ConsumableEntity updateConsumable(Long consumableId, ConsumableEntity updateData, Long equipmentId) {
        log.info("Updating consumable ID: {}", consumableId);

        ConsumableEntity existing = consumableRepository.findByIdWithAllRelations(consumableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSUMABLE_NOT_FOUND));

        // Update fields
        if (updateData.getConsumableName() != null) {
            existing.setConsumableName(updateData.getConsumableName());
        }
        if (updateData.getCategory() != null) {
            existing.setCategory(updateData.getCategory());
        }
        if (updateData.getUnit() != null) {
            existing.setUnit(updateData.getUnit());
        }
        if (updateData.getCurrentStock() != null) {
            existing.setCurrentStock(updateData.getCurrentStock());
        }
        if (updateData.getMinimumStock() != null) {
            existing.setMinimumStock(updateData.getMinimumStock());
        }
        if (updateData.getMaximumStock() != null) {
            existing.setMaximumStock(updateData.getMaximumStock());
        }
        if (updateData.getUnitPrice() != null) {
            existing.setUnitPrice(updateData.getUnitPrice());
        }
        if (updateData.getSupplier() != null) {
            existing.setSupplier(updateData.getSupplier());
        }
        if (updateData.getLeadTimeDays() != null) {
            existing.setLeadTimeDays(updateData.getLeadTimeDays());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Update equipment (optional)
        if (equipmentId != null) {
            EquipmentEntity equipment = equipmentRepository.findByIdWithAllRelations(equipmentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
            existing.setEquipment(equipment);
        }

        ConsumableEntity updated = consumableRepository.save(existing);
        log.info("Consumable updated successfully: {}", updated.getConsumableCode());
        return updated;
    }

    /**
     * Adjust stock quantity
     * If adjustmentQuantity > 0, set lastReplenishedDate = today
     */
    @Transactional
    public ConsumableEntity adjustStock(Long consumableId, BigDecimal adjustmentQuantity) {
        log.info("Adjusting stock for consumable ID: {} by {}", consumableId, adjustmentQuantity);

        ConsumableEntity existing = consumableRepository.findByIdWithAllRelations(consumableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSUMABLE_NOT_FOUND));

        // KEY LOGIC: add adjustmentQuantity to currentStock
        BigDecimal currentStock = existing.getCurrentStock() != null ? existing.getCurrentStock() : BigDecimal.ZERO;
        existing.setCurrentStock(currentStock.add(adjustmentQuantity));

        // If adjustmentQuantity > 0, set lastReplenishedDate
        if (adjustmentQuantity.compareTo(BigDecimal.ZERO) > 0) {
            existing.setLastReplenishedDate(LocalDate.now());
        }

        ConsumableEntity updated = consumableRepository.save(existing);
        log.info("Stock adjusted successfully for consumable: {}, new stock: {}", updated.getConsumableCode(), updated.getCurrentStock());
        return updated;
    }

    /**
     * Delete consumable
     */
    @Transactional
    public void deleteConsumable(Long consumableId) {
        log.info("Deleting consumable ID: {}", consumableId);

        ConsumableEntity consumable = consumableRepository.findById(consumableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSUMABLE_NOT_FOUND));

        consumableRepository.delete(consumable);
        log.info("Consumable deleted successfully: {}", consumable.getConsumableCode());
    }
}
