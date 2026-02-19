package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.InspectionFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Inspection Form Repository
 * 점검 양식 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface InspectionFormRepository extends JpaRepository<InspectionFormEntity, Long> {

    /**
     * Get all inspection forms by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT f FROM InspectionFormEntity f " +
           "JOIN FETCH f.tenant " +
           "LEFT JOIN FETCH f.fields " +
           "WHERE f.tenant.tenantId = :tenantId " +
           "ORDER BY f.formCode")
    List<InspectionFormEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get inspection form by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT f FROM InspectionFormEntity f " +
           "JOIN FETCH f.tenant " +
           "LEFT JOIN FETCH f.fields " +
           "WHERE f.formId = :formId")
    Optional<InspectionFormEntity> findByIdWithAllRelations(@Param("formId") Long formId);

    /**
     * Get active inspection forms by tenant
     */
    @Query("SELECT f FROM InspectionFormEntity f " +
           "JOIN FETCH f.tenant " +
           "LEFT JOIN FETCH f.fields " +
           "WHERE f.tenant.tenantId = :tenantId " +
           "AND f.isActive = true " +
           "ORDER BY f.formCode")
    List<InspectionFormEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Check if form code exists for tenant
     */
    boolean existsByTenant_TenantIdAndFormCode(String tenantId, String formCode);
}
