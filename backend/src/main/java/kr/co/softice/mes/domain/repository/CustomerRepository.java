package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.CustomerEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Customer Repository
 * 고객 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByTenantAndCustomerCode(TenantEntity tenant, String customerCode);
    Optional<CustomerEntity> findByTenant_TenantIdAndCustomerCode(String tenantId, String customerCode);
    List<CustomerEntity> findByTenant_TenantId(String tenantId);
    List<CustomerEntity> findByTenant_TenantIdAndCustomerType(String tenantId, String customerType);
    List<CustomerEntity> findByTenant_TenantIdAndIsActive(String tenantId, Boolean isActive);
    boolean existsByTenantAndCustomerCode(TenantEntity tenant, String customerCode);

    @Query("SELECT c FROM CustomerEntity c " +
           "JOIN FETCH c.tenant " +
           "WHERE c.tenant.tenantId = :tenantId " +
           "ORDER BY c.customerCode ASC")
    List<CustomerEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM CustomerEntity c " +
           "JOIN FETCH c.tenant " +
           "WHERE c.customerId = :customerId")
    Optional<CustomerEntity> findByIdWithAllRelations(@Param("customerId") Long customerId);

    @Query("SELECT c FROM CustomerEntity c " +
           "JOIN FETCH c.tenant " +
           "WHERE c.tenant.tenantId = :tenantId AND c.isActive = :isActive " +
           "ORDER BY c.customerCode ASC")
    List<CustomerEntity> findByTenantIdAndIsActiveWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("isActive") Boolean isActive
    );

    @Query("SELECT c FROM CustomerEntity c " +
           "JOIN FETCH c.tenant " +
           "WHERE c.tenant.tenantId = :tenantId AND c.customerType = :customerType " +
           "ORDER BY c.customerCode ASC")
    List<CustomerEntity> findByTenantIdAndCustomerTypeWithAllRelations(
        @Param("tenantId") String tenantId,
        @Param("customerType") String customerType
    );
}
