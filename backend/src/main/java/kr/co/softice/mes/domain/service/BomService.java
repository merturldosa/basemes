package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.BomDetailEntity;
import kr.co.softice.mes.domain.entity.BomEntity;
import kr.co.softice.mes.domain.repository.BomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BOM Service
 * BOM 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BomService {

    private final BomRepository bomRepository;

    public List<BomEntity> findByTenant(String tenantId) {
        return bomRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public List<BomEntity> findByTenantAndProduct(String tenantId, Long productId) {
        return bomRepository.findByTenantIdAndProductIdWithAllRelations(tenantId, productId);
    }

    public List<BomEntity> findActiveByTenant(String tenantId) {
        return bomRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    public Optional<BomEntity> findById(Long bomId) {
        return bomRepository.findByIdWithAllRelations(bomId);
    }

    public Optional<BomEntity> findByBomCodeAndVersion(String tenantId, String bomCode, String version) {
        return bomRepository.findByTenant_TenantIdAndBomCodeAndVersion(tenantId, bomCode, version);
    }

    @Transactional
    public BomEntity createBom(BomEntity bom) {
        log.info("Creating BOM: {} version: {} for product: {}",
            bom.getBomCode(), bom.getVersion(), bom.getProduct().getProductCode());

        if (bomRepository.existsByTenantAndBomCodeAndVersion(
            bom.getTenant(), bom.getBomCode(), bom.getVersion())) {
            throw new BusinessException(ErrorCode.BOM_ALREADY_EXISTS);
        }

        // Set sequence numbers for details if not set
        int sequence = 1;
        for (BomDetailEntity detail : bom.getDetails()) {
            if (detail.getSequence() == null) {
                detail.setSequence(sequence++);
            }
            detail.setBom(bom);
        }

        BomEntity saved = bomRepository.save(bom);
        return bomRepository.findByIdWithAllRelations(saved.getBomId()).orElse(saved);
    }

    @Transactional
    public BomEntity updateBom(BomEntity bom) {
        log.info("Updating BOM: {}", bom.getBomId());

        // Set sequence numbers for new details
        int sequence = 1;
        List<BomDetailEntity> newDetails = new ArrayList<>(bom.getDetails());

        // Clear existing details and add new ones
        bom.clearDetails();

        for (BomDetailEntity detail : newDetails) {
            if (detail.getSequence() == null) {
                detail.setSequence(sequence++);
            }
            bom.addDetail(detail);
        }

        BomEntity updated = bomRepository.save(bom);
        return bomRepository.findByIdWithAllRelations(updated.getBomId()).orElse(updated);
    }

    @Transactional
    public void deleteBom(Long bomId) {
        log.info("Deleting BOM: {}", bomId);
        bomRepository.deleteById(bomId);
    }

    @Transactional
    public BomEntity toggleActive(Long bomId) {
        BomEntity bom = bomRepository.findById(bomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BOM_NOT_FOUND));

        log.info("Toggling BOM {} active status from {} to {}",
            bom.getBomCode(), bom.getIsActive(), !bom.getIsActive());

        bom.setIsActive(!bom.getIsActive());
        BomEntity updated = bomRepository.save(bom);
        return bomRepository.findByIdWithAllRelations(updated.getBomId()).orElse(updated);
    }

    /**
     * Copy BOM to a new version
     * BOM을 새 버전으로 복사
     */
    @Transactional
    public BomEntity copyBom(Long sourceBomId, String newVersion) {
        BomEntity sourceBom = bomRepository.findByIdWithAllRelations(sourceBomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BOM_NOT_FOUND));

        log.info("Copying BOM {} from version {} to version {}",
            sourceBom.getBomCode(), sourceBom.getVersion(), newVersion);

        // Check if target version already exists
        if (bomRepository.existsByTenantAndBomCodeAndVersion(
            sourceBom.getTenant(), sourceBom.getBomCode(), newVersion)) {
            throw new BusinessException(ErrorCode.BOM_VERSION_ALREADY_EXISTS);
        }

        // Create new BOM with new version
        BomEntity newBom = BomEntity.builder()
            .tenant(sourceBom.getTenant())
            .product(sourceBom.getProduct())
            .bomCode(sourceBom.getBomCode())
            .bomName(sourceBom.getBomName())
            .version(newVersion)
            .effectiveDate(sourceBom.getEffectiveDate())
            .expiryDate(sourceBom.getExpiryDate())
            .isActive(true)
            .remarks(sourceBom.getRemarks())
            .build();

        // Copy details
        for (BomDetailEntity sourceDetail : sourceBom.getDetails()) {
            BomDetailEntity newDetail = BomDetailEntity.builder()
                .sequence(sourceDetail.getSequence())
                .materialProduct(sourceDetail.getMaterialProduct())
                .process(sourceDetail.getProcess())
                .quantity(sourceDetail.getQuantity())
                .unit(sourceDetail.getUnit())
                .usageRate(sourceDetail.getUsageRate())
                .scrapRate(sourceDetail.getScrapRate())
                .remarks(sourceDetail.getRemarks())
                .build();
            newBom.addDetail(newDetail);
        }

        BomEntity saved = bomRepository.save(newBom);
        return bomRepository.findByIdWithAllRelations(saved.getBomId()).orElse(saved);
    }
}
