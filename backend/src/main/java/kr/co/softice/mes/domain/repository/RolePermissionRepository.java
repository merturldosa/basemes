package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.PermissionEntity;
import kr.co.softice.mes.domain.entity.RoleEntity;
import kr.co.softice.mes.domain.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Role-Permission Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {

    /**
     * Find by role
     */
    List<RolePermissionEntity> findByRole(RoleEntity role);

    /**
     * Find by permission
     */
    List<RolePermissionEntity> findByPermission(PermissionEntity permission);

    /**
     * Find permissions by role
     */
    @Query("SELECT rp.permission FROM RolePermissionEntity rp WHERE rp.role = :role")
    List<PermissionEntity> findPermissionsByRole(@Param("role") RoleEntity role);

    /**
     * Find roles by permission
     */
    @Query("SELECT rp.role FROM RolePermissionEntity rp WHERE rp.permission = :permission")
    List<RoleEntity> findRolesByPermission(@Param("permission") PermissionEntity permission);

    /**
     * Check if role-permission mapping exists
     */
    boolean existsByRoleAndPermission(RoleEntity role, PermissionEntity permission);

    /**
     * Delete by role
     */
    void deleteByRole(RoleEntity role);

    /**
     * Delete by permission
     */
    void deleteByPermission(PermissionEntity permission);
}
