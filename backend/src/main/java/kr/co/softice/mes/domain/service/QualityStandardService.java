package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.QualityStandardEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.QualityStandardRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Quality Standard Service
 * 품질 기준 마스터 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualityStandardService {

    private final QualityStandardRepository qualityStandardRepository;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;

    /**
     * Find all quality standards by tenant ID
     */
    public List<QualityStandardEntity> findByTenant(String tenantId) {
        return qualityStandardRepository.findByTenantIdWithRelations(tenantId);
    }

    /**
     * Find active quality standards by tenant ID
     */
    public List<QualityStandardEntity> findActiveByTenant(String tenantId) {
        return qualityStandardRepository.findByTenantIdAndIsActiveWithRelations(tenantId, true);
    }

    /**
     * Find quality standard by ID
     */
    public Optional<QualityStandardEntity> findById(Long qualityStandardId) {
        return qualityStandardRepository.findByIdWithRelations(qualityStandardId);
    }

    /**
     * Find quality standards by product ID
     */
    public List<QualityStandardEntity> findByProductId(Long productId) {
        return qualityStandardRepository.findByProductIdWithRelations(productId);
    }

    /**
     * Find quality standards by inspection type
     */
    public List<QualityStandardEntity> findByInspectionType(String tenantId, String inspectionType) {
        return qualityStandardRepository.findByTenantIdAndInspectionTypeWithRelations(tenantId, inspectionType);
    }

    /**
     * Create new quality standard
     */
    @Transactional
    public QualityStandardEntity createQualityStandard(QualityStandardEntity qualityStandard) {
        log.info("Creating quality standard: {} for tenant: {}",
            qualityStandard.getStandardCode(), qualityStandard.getTenant().getTenantId());

        // Check duplicate
        if (qualityStandardRepository.existsByTenantAndStandardCodeAndStandardVersion(
                qualityStandard.getTenant(),
                qualityStandard.getStandardCode(),
                qualityStandard.getStandardVersion())) {
            throw new IllegalArgumentException("Quality standard code already exists: " +
                qualityStandard.getStandardCode() + " v" + qualityStandard.getStandardVersion());
        }

        return qualityStandardRepository.save(qualityStandard);
    }

    /**
     * Update quality standard
     */
    @Transactional
    public QualityStandardEntity updateQualityStandard(QualityStandardEntity qualityStandard) {
        log.info("Updating quality standard: {}", qualityStandard.getQualityStandardId());

        if (!qualityStandardRepository.existsById(qualityStandard.getQualityStandardId())) {
            throw new IllegalArgumentException("Quality standard not found: " +
                qualityStandard.getQualityStandardId());
        }

        return qualityStandardRepository.save(qualityStandard);
    }

    /**
     * Delete quality standard
     */
    @Transactional
    public void deleteQualityStandard(Long qualityStandardId) {
        log.info("Deleting quality standard: {}", qualityStandardId);
        qualityStandardRepository.deleteById(qualityStandardId);
    }

    /**
     * Activate quality standard
     */
    @Transactional
    public QualityStandardEntity activateQualityStandard(Long qualityStandardId) {
        log.info("Activating quality standard: {}", qualityStandardId);
        QualityStandardEntity qualityStandard = qualityStandardRepository.findById(qualityStandardId)
            .orElseThrow(() -> new IllegalArgumentException("Quality standard not found: " + qualityStandardId));

        qualityStandard.setIsActive(true);
        return qualityStandardRepository.save(qualityStandard);
    }

    /**
     * Deactivate quality standard
     */
    @Transactional
    public QualityStandardEntity deactivateQualityStandard(Long qualityStandardId) {
        log.info("Deactivating quality standard: {}", qualityStandardId);
        QualityStandardEntity qualityStandard = qualityStandardRepository.findById(qualityStandardId)
            .orElseThrow(() -> new IllegalArgumentException("Quality standard not found: " + qualityStandardId));

        qualityStandard.setIsActive(false);
        return qualityStandardRepository.save(qualityStandard);
    }

    /**
     * Count quality standards by tenant
     */
    public long countByTenant(String tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return qualityStandardRepository.countByTenant(tenant);
    }
}
