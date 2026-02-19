package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Find by tenant and username
     */
    Optional<UserEntity> findByTenantAndUsername(TenantEntity tenant, String username);

    /**
     * Find by tenant ID and username
     */
    Optional<UserEntity> findByTenant_TenantIdAndUsername(String tenantId, String username);

    /**
     * Find by username (without tenant filter)
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Find by email
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Find by tenant
     */
    List<UserEntity> findByTenant(TenantEntity tenant);

    /**
     * Find by tenant ID
     */
    List<UserEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and status
     */
    List<UserEntity> findByTenantAndStatus(TenantEntity tenant, String status);

    /**
     * Check if username exists for tenant
     */
    boolean existsByTenantAndUsername(TenantEntity tenant, String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Count users by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Count users by tenant and status
     */
    long countByTenantAndStatus(TenantEntity tenant, String status);

    /**
     * Count users by tenant and last login after specific time
     */
    long countByTenantAndLastLoginAtAfter(TenantEntity tenant, LocalDateTime time);

    /**
     * Find by tenant and last login between dates
     */
    List<UserEntity> findByTenantAndLastLoginAtBetween(TenantEntity tenant, LocalDateTime start, LocalDateTime end);

    /**
     * Find all users by tenant ID with tenant eagerly loaded
     */
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.tenant WHERE u.tenant.tenantId = :tenantId")
    List<UserEntity> findByTenantIdWithTenant(@Param("tenantId") String tenantId);

    /**
     * Find users by tenant ID and status with tenant eagerly loaded
     */
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.tenant WHERE u.tenant.tenantId = :tenantId AND u.status = :status")
    List<UserEntity> findByTenantIdAndStatusWithTenant(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Find user by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.tenant WHERE u.userId = :userId")
    Optional<UserEntity> findByIdWithAllRelations(@Param("userId") Long userId);
}
