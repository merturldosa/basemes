package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Claim Repository
 * 클레임 관리 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface ClaimRepository extends JpaRepository<ClaimEntity, Long> {

    /**
     * Get all claims by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT c FROM ClaimEntity c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH c.customer " +
           "LEFT JOIN FETCH c.product " +
           "LEFT JOIN FETCH c.salesOrder " +
           "LEFT JOIN FETCH c.shipping " +
           "LEFT JOIN FETCH c.responsibleDepartment " +
           "LEFT JOIN FETCH c.responsibleUser " +
           "WHERE c.tenant.tenantId = :tenantId " +
           "ORDER BY c.claimDate DESC")
    List<ClaimEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get claim by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT c FROM ClaimEntity c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH c.customer " +
           "LEFT JOIN FETCH c.product " +
           "LEFT JOIN FETCH c.salesOrder " +
           "LEFT JOIN FETCH c.shipping " +
           "LEFT JOIN FETCH c.responsibleDepartment " +
           "LEFT JOIN FETCH c.responsibleUser " +
           "WHERE c.claimId = :claimId")
    Optional<ClaimEntity> findByIdWithAllRelations(@Param("claimId") Long claimId);

    /**
     * Get claims by tenant and status
     */
    @Query("SELECT c FROM ClaimEntity c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH c.customer " +
           "LEFT JOIN FETCH c.product " +
           "LEFT JOIN FETCH c.salesOrder " +
           "LEFT JOIN FETCH c.shipping " +
           "LEFT JOIN FETCH c.responsibleDepartment " +
           "LEFT JOIN FETCH c.responsibleUser " +
           "WHERE c.tenant.tenantId = :tenantId " +
           "AND c.status = :status " +
           "ORDER BY c.claimDate DESC")
    List<ClaimEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Get claims by tenant and claim type
     */
    @Query("SELECT c FROM ClaimEntity c " +
           "JOIN FETCH c.tenant " +
           "JOIN FETCH c.customer " +
           "LEFT JOIN FETCH c.product " +
           "LEFT JOIN FETCH c.salesOrder " +
           "LEFT JOIN FETCH c.shipping " +
           "LEFT JOIN FETCH c.responsibleDepartment " +
           "LEFT JOIN FETCH c.responsibleUser " +
           "WHERE c.tenant.tenantId = :tenantId " +
           "AND c.claimType = :claimType " +
           "ORDER BY c.claimDate DESC")
    List<ClaimEntity> findByTenantIdAndClaimType(@Param("tenantId") String tenantId, @Param("claimType") String claimType);

    /**
     * Check if claim number exists for tenant
     */
    boolean existsByTenant_TenantIdAndClaimNo(String tenantId, String claimNo);
}
