package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.SiteEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.SiteRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Site Service
 * 사업장 서비스
 *
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SiteService {

    private final SiteRepository siteRepository;
    private final TenantRepository tenantRepository;

    /**
     * Get all sites by tenant
     */
    public List<SiteEntity> getAllSitesByTenant(String tenantId) {
        log.info("Getting all sites for tenant: {}", tenantId);
        return siteRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get site by ID
     */
    public SiteEntity getSiteById(Long siteId) {
        log.info("Getting site by ID: {}", siteId);
        return siteRepository.findByIdWithAllRelations(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));
    }

    /**
     * Get active sites by tenant
     */
    public List<SiteEntity> getActiveSites(String tenantId) {
        log.info("Getting active sites for tenant: {}", tenantId);
        return siteRepository.findActiveSitesByTenantId(tenantId);
    }

    /**
     * Get sites by type
     */
    public List<SiteEntity> getSitesByType(String tenantId, String siteType) {
        log.info("Getting sites by type: {} for tenant: {}", siteType, tenantId);
        return siteRepository.findByTenantIdAndSiteType(tenantId, siteType);
    }

    /**
     * Create site
     */
    @Transactional
    public SiteEntity createSite(SiteEntity site) {
        log.info("Creating site: {}", site.getSiteCode());

        // Check duplicate
        if (siteRepository.existsByTenant_TenantIdAndSiteCode(
                site.getTenant().getTenantId(), site.getSiteCode())) {
            throw new IllegalArgumentException("Site already exists: " + site.getSiteCode());
        }

        // Validate tenant
        TenantEntity tenant = tenantRepository.findById(site.getTenant().getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + site.getTenant().getTenantId()));
        site.setTenant(tenant);

        // Set default values
        if (site.getIsActive() == null) {
            site.setIsActive(true);
        }

        SiteEntity savedSite = siteRepository.save(site);
        log.info("Site created successfully: {}", savedSite.getSiteCode());

        return getSiteById(savedSite.getSiteId());
    }

    /**
     * Update site
     */
    @Transactional
    public SiteEntity updateSite(Long siteId, SiteEntity updatedSite) {
        log.info("Updating site ID: {}", siteId);

        SiteEntity existingSite = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));

        // Update fields
        existingSite.setSiteName(updatedSite.getSiteName());
        existingSite.setAddress(updatedSite.getAddress());
        existingSite.setPostalCode(updatedSite.getPostalCode());
        existingSite.setCountry(updatedSite.getCountry());
        existingSite.setRegion(updatedSite.getRegion());
        existingSite.setPhone(updatedSite.getPhone());
        existingSite.setFax(updatedSite.getFax());
        existingSite.setEmail(updatedSite.getEmail());
        existingSite.setManagerName(updatedSite.getManagerName());
        existingSite.setManagerPhone(updatedSite.getManagerPhone());
        existingSite.setManagerEmail(updatedSite.getManagerEmail());
        existingSite.setSiteType(updatedSite.getSiteType());
        existingSite.setRemarks(updatedSite.getRemarks());

        siteRepository.save(existingSite);
        log.info("Site updated successfully: {}", existingSite.getSiteCode());

        return getSiteById(siteId);
    }

    /**
     * Delete site
     */
    @Transactional
    public void deleteSite(Long siteId) {
        log.info("Deleting site ID: {}", siteId);

        SiteEntity site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));

        siteRepository.delete(site);
        log.info("Site deleted successfully: {}", site.getSiteCode());
    }

    /**
     * Toggle site active status
     */
    @Transactional
    public SiteEntity toggleActive(Long siteId) {
        log.info("Toggling active status for site ID: {}", siteId);

        SiteEntity site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));

        site.setIsActive(!site.getIsActive());
        siteRepository.save(site);

        log.info("Site active status toggled to: {}", site.getIsActive());
        return getSiteById(siteId);
    }
}
