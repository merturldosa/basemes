package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Find by tenant ID with pagination
     */
    Page<UserEntity> findByTenant_TenantId(String tenantId, Pageable pageable);

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

    /**
     * Find first active user by role code within a tenant
     * Used for approval line approver resolution
     */
    @Query("SELECT ur.user FROM UserRoleEntity ur " +
           "WHERE ur.role.tenant.tenantId = :tenantId " +
           "AND ur.role.roleCode = :roleCode " +
           "AND ur.user.status = 'active' " +
           "ORDER BY ur.user.userId ASC")
    List<UserEntity> findActiveUsersByRoleCode(@Param("tenantId") String tenantId, @Param("roleCode") String roleCode);

    /**
     * Find first active user by department name within a tenant
     * Used for approval line approver resolution
     */
    @Query("SELECT e.user FROM EmployeeEntity e " +
           "WHERE e.tenant.tenantId = :tenantId " +
           "AND e.department.departmentName = :departmentName " +
           "AND e.user IS NOT NULL " +
           "AND e.isActive = true " +
           "ORDER BY e.employeeId ASC")
    List<UserEntity> findActiveUsersByDepartmentName(@Param("tenantId") String tenantId, @Param("departmentName") String departmentName);

    /**
     * Find first active user by position within a tenant
     * Used for approval line approver resolution
     */
    @Query("SELECT e.user FROM EmployeeEntity e " +
           "WHERE e.tenant.tenantId = :tenantId " +
           "AND e.position = :position " +
           "AND e.user IS NOT NULL " +
           "AND e.isActive = true " +
           "ORDER BY e.employeeId ASC")
    List<UserEntity> findActiveUsersByPosition(@Param("tenantId") String tenantId, @Param("position") String position);
}
