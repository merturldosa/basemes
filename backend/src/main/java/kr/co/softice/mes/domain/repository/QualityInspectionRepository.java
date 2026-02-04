package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Quality Inspection Repository
 * 품질 검사 기록 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface QualityInspectionRepository extends JpaRepository<QualityInspectionEntity, Long> {

    /**
     * Find by tenant and inspection no
     */
    Optional<QualityInspectionEntity> findByTenantAndInspectionNo(TenantEntity tenant, String inspectionNo);

    /**
     * Find by tenant ID
     */
    List<QualityInspectionEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by quality standard
     */
    List<QualityInspectionEntity> findByQualityStandard(QualityStandardEntity qualityStandard);

    /**
     * Find by work order
     */
    List<QualityInspectionEntity> findByWorkOrder(WorkOrderEntity workOrder);

    /**
     * Find by work result
     */
    List<QualityInspectionEntity> findByWorkResult(WorkResultEntity workResult);

    /**
     * Find by product
     */
    List<QualityInspectionEntity> findByProduct(ProductEntity product);

    /**
     * Find by tenant and inspection type
     */
    List<QualityInspectionEntity> findByTenantAndInspectionType(TenantEntity tenant, String inspectionType);

    /**
     * Find by tenant and inspection result
     */
    List<QualityInspectionEntity> findByTenantAndInspectionResult(TenantEntity tenant, String inspectionResult);

    /**
     * Find by tenant and date range
     */
    List<QualityInspectionEntity> findByTenantAndInspectionDateBetween(
        TenantEntity tenant, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find by inspector
     */
    List<QualityInspectionEntity> findByInspector(UserEntity inspector);

    /**
     * Check if inspection no exists for tenant
     */
    boolean existsByTenantAndInspectionNo(TenantEntity tenant, String inspectionNo);

    /**
     * Count inspections by tenant
     */
    long countByTenant(TenantEntity tenant);

    /**
     * Count inspections by tenant and result
     */
    long countByTenantAndInspectionResult(TenantEntity tenant, String inspectionResult);

    /**
     * Find all quality inspections by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT qi FROM QualityInspectionEntity qi " +
           "JOIN FETCH qi.tenant " +
           "JOIN FETCH qi.qualityStandard qs " +
           "JOIN FETCH qs.product " +
           "JOIN FETCH qi.product " +
           "JOIN FETCH qi.inspector " +
           "LEFT JOIN FETCH qi.workOrder " +
           "LEFT JOIN FETCH qi.workResult " +
           "WHERE qi.tenant.tenantId = :tenantId")
    List<QualityInspectionEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find by ID with all relationships eagerly loaded
     */
    @Query("SELECT qi FROM QualityInspectionEntity qi " +
           "JOIN FETCH qi.tenant " +
           "JOIN FETCH qi.qualityStandard qs " +
           "JOIN FETCH qs.product " +
           "JOIN FETCH qi.product " +
           "JOIN FETCH qi.inspector " +
           "LEFT JOIN FETCH qi.workOrder " +
           "LEFT JOIN FETCH qi.workResult " +
           "WHERE qi.qualityInspectionId = :id")
    Optional<QualityInspectionEntity> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * Find by work order ID with relationships eagerly loaded
     */
    @Query("SELECT qi FROM QualityInspectionEntity qi " +
           "JOIN FETCH qi.tenant " +
           "JOIN FETCH qi.qualityStandard qs " +
           "JOIN FETCH qs.product " +
           "JOIN FETCH qi.product " +
           "JOIN FETCH qi.inspector " +
           "WHERE qi.workOrder.workOrderId = :workOrderId")
    List<QualityInspectionEntity> findByWorkOrderIdWithRelations(@Param("workOrderId") Long workOrderId);

    /**
     * Find by inspection result with relationships eagerly loaded
     */
    @Query("SELECT qi FROM QualityInspectionEntity qi " +
           "JOIN FETCH qi.tenant " +
           "JOIN FETCH qi.qualityStandard qs " +
           "JOIN FETCH qs.product " +
           "JOIN FETCH qi.product " +
           "JOIN FETCH qi.inspector " +
           "LEFT JOIN FETCH qi.workOrder " +
           "LEFT JOIN FETCH qi.workResult " +
           "WHERE qi.tenant.tenantId = :tenantId AND qi.inspectionResult = :result")
    List<QualityInspectionEntity> findByTenantIdAndResultWithRelations(
        @Param("tenantId") String tenantId, @Param("result") String result);

    /**
     * Find by inspection type with relationships eagerly loaded
     */
    @Query("SELECT qi FROM QualityInspectionEntity qi " +
           "JOIN FETCH qi.tenant " +
           "JOIN FETCH qi.qualityStandard qs " +
           "JOIN FETCH qs.product " +
           "JOIN FETCH qi.product " +
           "JOIN FETCH qi.inspector " +
           "LEFT JOIN FETCH qi.workOrder " +
           "LEFT JOIN FETCH qi.workResult " +
           "WHERE qi.tenant.tenantId = :tenantId AND qi.inspectionType = :type")
    List<QualityInspectionEntity> findByTenantIdAndTypeWithRelations(
        @Param("tenantId") String tenantId, @Param("type") String type);
}
