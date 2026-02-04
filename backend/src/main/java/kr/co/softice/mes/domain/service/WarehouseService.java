package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.WarehouseEntity;
import kr.co.softice.mes.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Warehouse Service
 * 창고 마스터 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public List<WarehouseEntity> findByTenant(String tenantId) {
        return warehouseRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public List<WarehouseEntity> findActiveByTenant(String tenantId) {
        return warehouseRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    public Optional<WarehouseEntity> findById(Long warehouseId) {
        return warehouseRepository.findByIdWithAllRelations(warehouseId);
    }

    @Transactional
    public WarehouseEntity createWarehouse(WarehouseEntity warehouse) {
        log.info("Creating warehouse: {} for tenant: {}",
            warehouse.getWarehouseCode(), warehouse.getTenant().getTenantId());

        if (warehouseRepository.existsByTenantAndWarehouseCode(
            warehouse.getTenant(), warehouse.getWarehouseCode())) {
            throw new IllegalArgumentException("Warehouse code already exists: " + warehouse.getWarehouseCode());
        }

        WarehouseEntity saved = warehouseRepository.save(warehouse);
        return warehouseRepository.findByIdWithAllRelations(saved.getWarehouseId()).orElse(saved);
    }

    @Transactional
    public WarehouseEntity updateWarehouse(WarehouseEntity warehouse) {
        log.info("Updating warehouse: {}", warehouse.getWarehouseId());
        WarehouseEntity updated = warehouseRepository.save(warehouse);
        return warehouseRepository.findByIdWithAllRelations(updated.getWarehouseId()).orElse(updated);
    }

    @Transactional
    public void deleteWarehouse(Long warehouseId) {
        log.info("Deleting warehouse: {}", warehouseId);
        warehouseRepository.deleteById(warehouseId);
    }

    @Transactional
    public WarehouseEntity toggleActive(Long warehouseId) {
        WarehouseEntity warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + warehouseId));
        warehouse.setIsActive(!warehouse.getIsActive());
        WarehouseEntity updated = warehouseRepository.save(warehouse);
        return warehouseRepository.findByIdWithAllRelations(updated.getWarehouseId()).orElse(updated);
    }
}
