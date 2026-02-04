package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Return Repository
 * 반품 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ReturnRepository extends JpaRepository<ReturnEntity, Long> {

    /**
     * Find by tenant and return no
     */
    Optional<ReturnEntity> findByTenant_TenantIdAndReturnNo(String tenantId, String returnNo);

    /**
     * Check if return no exists
     */
    boolean existsByTenant_TenantIdAndReturnNo(String tenantId, String returnNo);

    /**
     * Find by tenant ID
     */
    List<ReturnEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and status
     */
    List<ReturnEntity> findByTenant_TenantIdAndReturnStatus(String tenantId, String returnStatus);

    /**
     * Find by tenant and type
     */
    List<ReturnEntity> findByTenant_TenantIdAndReturnType(String tenantId, String returnType);

    /**
     * Find by tenant and material request
     */
    List<ReturnEntity> findByTenant_TenantIdAndMaterialRequest_MaterialRequestId(
        String tenantId, Long materialRequestId);

    /**
     * Find by tenant and work order
     */
    List<ReturnEntity> findByTenant_TenantIdAndWorkOrder_WorkOrderId(String tenantId, Long workOrderId);

    /**
     * Find by tenant and requester
     */
    List<ReturnEntity> findByTenant_TenantIdAndRequester_UserId(String tenantId, Long requesterId);

    /**
     * Find by tenant and warehouse
     */
    List<ReturnEntity> findByTenant_TenantIdAndWarehouse_WarehouseId(String tenantId, Long warehouseId);

    /**
     * Find by tenant and date range
     */
    List<ReturnEntity> findByTenant_TenantIdAndReturnDateBetween(
        String tenantId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all returns by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT r FROM ReturnEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.requester " +
           "JOIN FETCH r.warehouse " +
           "LEFT JOIN FETCH r.materialRequest " +
           "LEFT JOIN FETCH r.workOrder " +
           "LEFT JOIN FETCH r.approver " +
           "WHERE r.tenant.tenantId = :tenantId " +
           "ORDER BY r.returnDate DESC")
    List<ReturnEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find by ID with all relationships eagerly loaded
     */
    @Query("SELECT r FROM ReturnEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.requester " +
           "JOIN FETCH r.warehouse " +
           "LEFT JOIN FETCH r.materialRequest " +
           "LEFT JOIN FETCH r.workOrder " +
           "LEFT JOIN FETCH r.approver " +
           "LEFT JOIN FETCH r.items " +
           "WHERE r.returnId = :id")
    Optional<ReturnEntity> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * Find by tenant and status with relationships
     */
    @Query("SELECT r FROM ReturnEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.requester " +
           "JOIN FETCH r.warehouse " +
           "LEFT JOIN FETCH r.materialRequest " +
           "LEFT JOIN FETCH r.workOrder " +
           "LEFT JOIN FETCH r.approver " +
           "WHERE r.tenant.tenantId = :tenantId " +
           "AND r.returnStatus = :status " +
           "ORDER BY r.returnDate DESC")
    List<ReturnEntity> findByTenantIdAndStatusWithRelations(
        @Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Find by tenant and type with relationships
     */
    @Query("SELECT r FROM ReturnEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.requester " +
           "JOIN FETCH r.warehouse " +
           "LEFT JOIN FETCH r.materialRequest " +
           "LEFT JOIN FETCH r.workOrder " +
           "LEFT JOIN FETCH r.approver " +
           "WHERE r.tenant.tenantId = :tenantId " +
           "AND r.returnType = :type " +
           "ORDER BY r.returnDate DESC")
    List<ReturnEntity> findByTenantIdAndTypeWithRelations(
        @Param("tenantId") String tenantId, @Param("type") String type);

    /**
     * Find pending returns by warehouse
     */
    @Query("SELECT r FROM ReturnEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.requester " +
           "JOIN FETCH r.warehouse " +
           "LEFT JOIN FETCH r.materialRequest " +
           "LEFT JOIN FETCH r.workOrder " +
           "WHERE r.warehouse.warehouseId = :warehouseId " +
           "AND r.returnStatus IN ('PENDING', 'APPROVED', 'RECEIVED') " +
           "ORDER BY r.returnDate ASC")
    List<ReturnEntity> findPendingReturnsByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * Find returns requiring inspection
     */
    @Query("SELECT r FROM ReturnEntity r " +
           "JOIN FETCH r.tenant " +
           "JOIN FETCH r.requester " +
           "JOIN FETCH r.warehouse " +
           "LEFT JOIN FETCH r.materialRequest " +
           "LEFT JOIN FETCH r.workOrder " +
           "WHERE r.tenant.tenantId = :tenantId " +
           "AND r.returnStatus IN ('RECEIVED', 'INSPECTING') " +
           "ORDER BY r.returnDate ASC")
    List<ReturnEntity> findReturnsRequiringInspection(@Param("tenantId") String tenantId);
}
