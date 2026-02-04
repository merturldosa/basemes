package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.RoleEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * User-Role Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    /**
     * Find by user
     */
    List<UserRoleEntity> findByUser(UserEntity user);

    /**
     * Find by role
     */
    List<UserRoleEntity> findByRole(RoleEntity role);

    /**
     * Find roles by user
     */
    @Query("SELECT ur.role FROM UserRoleEntity ur WHERE ur.user = :user")
    List<RoleEntity> findRolesByUser(@Param("user") UserEntity user);

    /**
     * Find users by role
     */
    @Query("SELECT ur.user FROM UserRoleEntity ur WHERE ur.role = :role")
    List<UserEntity> findUsersByRole(@Param("role") RoleEntity role);

    /**
     * Check if user-role mapping exists
     */
    boolean existsByUserAndRole(UserEntity user, RoleEntity role);

    /**
     * Delete by user
     */
    void deleteByUser(UserEntity user);

    /**
     * Delete by role
     */
    void deleteByRole(RoleEntity role);

    /**
     * Count users by role for a specific tenant
     */
    @Query("SELECT ur.role.roleCode as roleCode, ur.role.roleName as roleName, COUNT(DISTINCT ur.user) as userCount " +
           "FROM UserRoleEntity ur " +
           "WHERE ur.user.tenant = :tenant " +
           "GROUP BY ur.role.roleCode, ur.role.roleName " +
           "ORDER BY COUNT(DISTINCT ur.user) DESC")
    List<Object[]> countUsersByRoleForTenant(@Param("tenant") TenantEntity tenant);
}
