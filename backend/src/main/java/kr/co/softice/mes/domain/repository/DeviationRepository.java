package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.DeviationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Deviation Repository
 * 이탈 관리 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface DeviationRepository extends JpaRepository<DeviationEntity, Long> {

    /**
     * Get all deviations by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT d FROM DeviationEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.detectedByUser " +
           "LEFT JOIN FETCH d.resolvedByUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "ORDER BY d.detectedAt DESC")
    List<DeviationEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get deviation by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT d FROM DeviationEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.detectedByUser " +
           "LEFT JOIN FETCH d.resolvedByUser " +
           "WHERE d.deviationId = :deviationId")
    Optional<DeviationEntity> findByIdWithAllRelations(@Param("deviationId") Long deviationId);

    /**
     * Get deviations by tenant and status with all relations (JOIN FETCH)
     */
    @Query("SELECT d FROM DeviationEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.detectedByUser " +
           "LEFT JOIN FETCH d.resolvedByUser " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.status = :status " +
           "ORDER BY d.detectedAt DESC")
    List<DeviationEntity> findByStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Get deviations by equipment ID with all relations (JOIN FETCH)
     */
    @Query("SELECT d FROM DeviationEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.equipment " +
           "LEFT JOIN FETCH d.detectedByUser " +
           "LEFT JOIN FETCH d.resolvedByUser " +
           "WHERE d.equipment.equipmentId = :equipmentId " +
           "ORDER BY d.detectedAt DESC")
    List<DeviationEntity> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * Check if deviation number exists for tenant
     */
    boolean existsByTenant_TenantIdAndDeviationNo(String tenantId, String deviationNo);
}
