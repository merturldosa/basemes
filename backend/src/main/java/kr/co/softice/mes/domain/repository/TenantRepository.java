package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Tenant Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, String> {

    /**
     * Find by tenant name
     */
    Optional<TenantEntity> findByTenantName(String tenantName);

    /**
     * Find by company name
     */
    Optional<TenantEntity> findByCompanyName(String companyName);

    /**
     * Find by industry type
     */
    java.util.List<TenantEntity> findByIndustryType(String industryType);

    /**
     * Find by status
     */
    java.util.List<TenantEntity> findByStatus(String status);
}
