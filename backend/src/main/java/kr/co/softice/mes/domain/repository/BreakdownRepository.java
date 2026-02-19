package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.BreakdownEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Breakdown Repository
 * 고장 관리 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface BreakdownRepository extends JpaRepository<BreakdownEntity, Long> {

    /**
     * Get all breakdowns by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT b FROM BreakdownEntity b " +
           "JOIN FETCH b.tenant " +
           "JOIN FETCH b.equipment " +
           "LEFT JOIN FETCH b.downtime " +
           "LEFT JOIN FETCH b.reportedByUser " +
           "LEFT JOIN FETCH b.assignedUser " +
           "LEFT JOIN FETCH b.closedByUser " +
           "WHERE b.tenant.tenantId = :tenantId " +
           "ORDER BY b.reportedAt DESC")
    List<BreakdownEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get breakdown by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT b FROM BreakdownEntity b " +
           "JOIN FETCH b.tenant " +
           "JOIN FETCH b.equipment " +
           "LEFT JOIN FETCH b.downtime " +
           "LEFT JOIN FETCH b.reportedByUser " +
           "LEFT JOIN FETCH b.assignedUser " +
           "LEFT JOIN FETCH b.closedByUser " +
           "WHERE b.breakdownId = :breakdownId")
    Optional<BreakdownEntity> findByIdWithAllRelations(@Param("breakdownId") Long breakdownId);

    /**
     * Get breakdowns by status
     */
    @Query("SELECT b FROM BreakdownEntity b " +
           "JOIN FETCH b.tenant " +
           "JOIN FETCH b.equipment " +
           "LEFT JOIN FETCH b.downtime " +
           "LEFT JOIN FETCH b.reportedByUser " +
           "LEFT JOIN FETCH b.assignedUser " +
           "LEFT JOIN FETCH b.closedByUser " +
           "WHERE b.tenant.tenantId = :tenantId " +
           "AND b.status = :status " +
           "ORDER BY b.reportedAt DESC")
    List<BreakdownEntity> findByStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Get breakdowns by equipment
     */
    @Query("SELECT b FROM BreakdownEntity b " +
           "JOIN FETCH b.tenant " +
           "JOIN FETCH b.equipment " +
           "LEFT JOIN FETCH b.downtime " +
           "LEFT JOIN FETCH b.reportedByUser " +
           "LEFT JOIN FETCH b.assignedUser " +
           "LEFT JOIN FETCH b.closedByUser " +
           "WHERE b.equipment.equipmentId = :equipmentId " +
           "ORDER BY b.reportedAt DESC")
    List<BreakdownEntity> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * Check if breakdown number exists for tenant
     */
    boolean existsByTenant_TenantIdAndBreakdownNo(String tenantId, String breakdownNo);
}
