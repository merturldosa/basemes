package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.SupplierEntity;
import kr.co.softice.mes.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Supplier Service
 * 공급업체 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<SupplierEntity> findByTenant(String tenantId) {
        return supplierRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public List<SupplierEntity> findActiveByTenant(String tenantId) {
        return supplierRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    public List<SupplierEntity> findByTenantAndType(String tenantId, String supplierType) {
        return supplierRepository.findByTenantIdAndSupplierTypeWithAllRelations(tenantId, supplierType);
    }

    public List<SupplierEntity> findByTenantAndRating(String tenantId, String rating) {
        return supplierRepository.findByTenantIdAndRatingWithAllRelations(tenantId, rating);
    }

    public Optional<SupplierEntity> findById(Long supplierId) {
        return supplierRepository.findByIdWithAllRelations(supplierId);
    }

    public Optional<SupplierEntity> findBySupplierCode(String tenantId, String supplierCode) {
        return supplierRepository.findByTenant_TenantIdAndSupplierCode(tenantId, supplierCode);
    }

    @Transactional
    public SupplierEntity createSupplier(SupplierEntity supplier) {
        log.info("Creating supplier: {} for tenant: {}",
            supplier.getSupplierCode(), supplier.getTenant().getTenantId());

        if (supplierRepository.existsByTenantAndSupplierCode(
            supplier.getTenant(), supplier.getSupplierCode())) {
            throw new IllegalArgumentException("Supplier code already exists: " + supplier.getSupplierCode());
        }

        SupplierEntity saved = supplierRepository.save(supplier);
        return supplierRepository.findByIdWithAllRelations(saved.getSupplierId()).orElse(saved);
    }

    @Transactional
    public SupplierEntity updateSupplier(SupplierEntity supplier) {
        log.info("Updating supplier: {}", supplier.getSupplierId());
        SupplierEntity updated = supplierRepository.save(supplier);
        return supplierRepository.findByIdWithAllRelations(updated.getSupplierId()).orElse(updated);
    }

    @Transactional
    public void deleteSupplier(Long supplierId) {
        log.info("Deleting supplier: {}", supplierId);
        supplierRepository.deleteById(supplierId);
    }

    @Transactional
    public SupplierEntity toggleActive(Long supplierId) {
        SupplierEntity supplier = supplierRepository.findById(supplierId)
            .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));

        log.info("Toggling supplier {} active status from {} to {}",
            supplier.getSupplierCode(), supplier.getIsActive(), !supplier.getIsActive());

        supplier.setIsActive(!supplier.getIsActive());
        SupplierEntity updated = supplierRepository.save(supplier);
        return supplierRepository.findByIdWithAllRelations(updated.getSupplierId()).orElse(updated);
    }
}
