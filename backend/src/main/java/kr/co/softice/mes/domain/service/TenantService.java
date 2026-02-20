package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Tenant Service
 * 테넌트(회사/사업장) 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

    /**
     * Find tenant by ID
     */
    public Optional<TenantEntity> findById(String tenantId) {
        log.debug("Finding tenant by ID: {}", tenantId);
        return tenantRepository.findById(tenantId);
    }

    /**
     * Find tenant by name
     */
    public Optional<TenantEntity> findByTenantName(String tenantName) {
        log.debug("Finding tenant by name: {}", tenantName);
        return tenantRepository.findByTenantName(tenantName);
    }

    /**
     * Find all tenants
     */
    public List<TenantEntity> findAll() {
        log.debug("Finding all tenants");
        return tenantRepository.findAll();
    }

    /**
     * Find tenants by industry type
     */
    public List<TenantEntity> findByIndustryType(String industryType) {
        log.debug("Finding tenants by industry type: {}", industryType);
        return tenantRepository.findByIndustryType(industryType);
    }

    /**
     * Find active tenants
     */
    public List<TenantEntity> findActiveTenants() {
        log.debug("Finding active tenants");
        return tenantRepository.findByStatus("active");
    }

    /**
     * Create new tenant
     */
    @Transactional
    public TenantEntity createTenant(TenantEntity tenant) {
        log.info("Creating new tenant: {}", tenant.getTenantId());

        // Check if tenant ID already exists
        if (tenantRepository.existsById(tenant.getTenantId())) {
            throw new BusinessException(ErrorCode.TENANT_ALREADY_EXISTS);
        }

        return tenantRepository.save(tenant);
    }

    /**
     * Update tenant
     */
    @Transactional
    public TenantEntity updateTenant(TenantEntity tenant) {
        log.info("Updating tenant: {}", tenant.getTenantId());

        if (!tenantRepository.existsById(tenant.getTenantId())) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }

        return tenantRepository.save(tenant);
    }

    /**
     * Delete tenant
     */
    @Transactional
    public void deleteTenant(String tenantId) {
        log.info("Deleting tenant: {}", tenantId);
        tenantRepository.deleteById(tenantId);
    }

    /**
     * Activate tenant
     */
    @Transactional
    public TenantEntity activateTenant(String tenantId) {
        log.info("Activating tenant: {}", tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));

        tenant.setStatus("active");
        return tenantRepository.save(tenant);
    }

    /**
     * Deactivate tenant
     */
    @Transactional
    public TenantEntity deactivateTenant(String tenantId) {
        log.info("Deactivating tenant: {}", tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));

        tenant.setStatus("inactive");
        return tenantRepository.save(tenant);
    }
}
