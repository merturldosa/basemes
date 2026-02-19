package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.InspectionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Inspection Plan Repository
 * 점검 계획 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface InspectionPlanRepository extends JpaRepository<InspectionPlanEntity, Long> {

    /**
     * Get all inspection plans by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT p FROM InspectionPlanEntity p " +
           "JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH p.equipment " +
           "LEFT JOIN FETCH p.form " +
           "LEFT JOIN FETCH p.assignedUser " +
           "WHERE p.tenant.tenantId = :tenantId " +
           "ORDER BY p.planCode")
    List<InspectionPlanEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get inspection plan by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT p FROM InspectionPlanEntity p " +
           "JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH p.equipment " +
           "LEFT JOIN FETCH p.form " +
           "LEFT JOIN FETCH p.assignedUser " +
           "WHERE p.planId = :planId")
    Optional<InspectionPlanEntity> findByIdWithAllRelations(@Param("planId") Long planId);

    /**
     * Get due inspection plans by tenant and due date
     */
    @Query("SELECT p FROM InspectionPlanEntity p " +
           "JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH p.equipment " +
           "LEFT JOIN FETCH p.form " +
           "LEFT JOIN FETCH p.assignedUser " +
           "WHERE p.tenant.tenantId = :tenantId " +
           "AND p.nextDueDate <= :dueDate " +
           "AND p.status = 'ACTIVE'")
    List<InspectionPlanEntity> findDuePlans(@Param("tenantId") String tenantId, @Param("dueDate") LocalDate dueDate);

    /**
     * Check if plan code exists for tenant
     */
    boolean existsByTenant_TenantIdAndPlanCode(String tenantId, String planCode);
}
