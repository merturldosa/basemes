package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Permission Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    /**
     * Find by permission code
     */
    Optional<PermissionEntity> findByPermissionCode(String permissionCode);

    /**
     * Find by module
     */
    List<PermissionEntity> findByModule(String module);

    /**
     * Find by status
     */
    List<PermissionEntity> findByStatus(String status);

    /**
     * Check if permission code exists
     */
    boolean existsByPermissionCode(String permissionCode);
}
