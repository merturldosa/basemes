package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.MaterialEntity;
import kr.co.softice.mes.domain.entity.SupplierEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.MaterialRepository;
import kr.co.softice.mes.domain.repository.SupplierRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Material Service
 * 자재 마스터 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final TenantRepository tenantRepository;
    private final SupplierRepository supplierRepository;

    /**
     * 테넌트별 모든 자재 조회
     */
    public List<MaterialEntity> getAllMaterials(String tenantId) {
        log.info("Fetching all materials for tenant: {}", tenantId);
        return materialRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * 자재 ID로 조회
     */
    public MaterialEntity getMaterialById(Long materialId) {
        log.info("Fetching material by ID: {}", materialId);
        return materialRepository.findByIdWithAllRelations(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + materialId));
    }

    /**
     * 테넌트별 활성 자재 조회
     */
    public List<MaterialEntity> getActiveMaterials(String tenantId) {
        log.info("Fetching active materials for tenant: {}", tenantId);
        return materialRepository.findActiveByTenantId(tenantId);
    }

    /**
     * 테넌트 및 자재 유형별 조회
     */
    public List<MaterialEntity> getMaterialsByType(String tenantId, String materialType) {
        log.info("Fetching materials by type {} for tenant: {}", materialType, tenantId);
        return materialRepository.findByTenantIdAndType(tenantId, materialType);
    }

    /**
     * 테넌트 및 공급업체별 조회
     */
    public List<MaterialEntity> getMaterialsBySupplier(String tenantId, Long supplierId) {
        log.info("Fetching materials by supplier {} for tenant: {}", supplierId, tenantId);
        return materialRepository.findByTenantIdAndSupplierId(tenantId, supplierId);
    }

    /**
     * 자재 생성
     */
    @Transactional
    public MaterialEntity createMaterial(String tenantId, MaterialEntity material) {
        log.info("Creating material: {} for tenant: {}", material.getMaterialCode(), tenantId);

        // Check if material code already exists
        if (materialRepository.existsByTenant_TenantIdAndMaterialCode(tenantId, material.getMaterialCode())) {
            throw new IllegalArgumentException("Material code already exists: " + material.getMaterialCode());
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        material.setTenant(tenant);

        // Set supplier if provided
        if (material.getSupplier() != null && material.getSupplier().getSupplierId() != null) {
            SupplierEntity supplier = supplierRepository.findById(material.getSupplier().getSupplierId())
                    .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + material.getSupplier().getSupplierId()));
            material.setSupplier(supplier);
        }

        // Set default values
        if (material.getIsActive() == null) {
            material.setIsActive(true);
        }
        if (material.getLotManaged() == null) {
            material.setLotManaged(false);
        }

        MaterialEntity saved = materialRepository.save(material);
        log.info("Material created successfully: {}", saved.getMaterialId());

        return materialRepository.findByIdWithAllRelations(saved.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve created material"));
    }

    /**
     * 자재 수정
     */
    @Transactional
    public MaterialEntity updateMaterial(Long materialId, MaterialEntity updatedMaterial) {
        log.info("Updating material: {}", materialId);

        MaterialEntity existingMaterial = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + materialId));

        // Update fields
        existingMaterial.setMaterialName(updatedMaterial.getMaterialName());
        existingMaterial.setMaterialType(updatedMaterial.getMaterialType());
        existingMaterial.setSpecification(updatedMaterial.getSpecification());
        existingMaterial.setModel(updatedMaterial.getModel());
        existingMaterial.setUnit(updatedMaterial.getUnit());
        existingMaterial.setStandardPrice(updatedMaterial.getStandardPrice());
        existingMaterial.setCurrentPrice(updatedMaterial.getCurrentPrice());
        existingMaterial.setCurrency(updatedMaterial.getCurrency());
        existingMaterial.setLeadTimeDays(updatedMaterial.getLeadTimeDays());
        existingMaterial.setMinStockQuantity(updatedMaterial.getMinStockQuantity());
        existingMaterial.setMaxStockQuantity(updatedMaterial.getMaxStockQuantity());
        existingMaterial.setSafetyStockQuantity(updatedMaterial.getSafetyStockQuantity());
        existingMaterial.setReorderPoint(updatedMaterial.getReorderPoint());
        existingMaterial.setStorageLocation(updatedMaterial.getStorageLocation());
        existingMaterial.setLotManaged(updatedMaterial.getLotManaged());
        existingMaterial.setShelfLifeDays(updatedMaterial.getShelfLifeDays());
        existingMaterial.setIsActive(updatedMaterial.getIsActive());
        existingMaterial.setRemarks(updatedMaterial.getRemarks());

        // Update supplier if provided
        if (updatedMaterial.getSupplier() != null && updatedMaterial.getSupplier().getSupplierId() != null) {
            SupplierEntity supplier = supplierRepository.findById(updatedMaterial.getSupplier().getSupplierId())
                    .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + updatedMaterial.getSupplier().getSupplierId()));
            existingMaterial.setSupplier(supplier);
        } else {
            existingMaterial.setSupplier(null);
        }

        MaterialEntity saved = materialRepository.save(existingMaterial);
        log.info("Material updated successfully: {}", saved.getMaterialId());

        return materialRepository.findByIdWithAllRelations(saved.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve updated material"));
    }

    /**
     * 자재 삭제
     */
    @Transactional
    public void deleteMaterial(Long materialId) {
        log.info("Deleting material: {}", materialId);

        if (!materialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Material not found: " + materialId);
        }

        materialRepository.deleteById(materialId);
        log.info("Material deleted successfully: {}", materialId);
    }

    /**
     * 자재 활성화/비활성화 토글
     */
    @Transactional
    public MaterialEntity toggleActive(Long materialId) {
        log.info("Toggling active status for material: {}", materialId);

        MaterialEntity material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + materialId));

        material.setIsActive(!material.getIsActive());
        MaterialEntity saved = materialRepository.save(material);

        log.info("Material active status toggled: {} -> {}", materialId, saved.getIsActive());

        return materialRepository.findByIdWithAllRelations(saved.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve updated material"));
    }
}
