package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Material Request Repository
 * 불출 신청 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequestEntity, Long> {

    /**
     * Find by tenant and request no
     */
    Optional<MaterialRequestEntity> findByTenant_TenantIdAndRequestNo(String tenantId, String requestNo);

    /**
     * Check if request no exists
     */
    boolean existsByTenant_TenantIdAndRequestNo(String tenantId, String requestNo);

    /**
     * Find by tenant ID
     */
    List<MaterialRequestEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and status
     */
    List<MaterialRequestEntity> findByTenant_TenantIdAndRequestStatus(String tenantId, String requestStatus);

    /**
     * Find by tenant and work order
     */
    List<MaterialRequestEntity> findByTenant_TenantIdAndWorkOrder_WorkOrderId(String tenantId, Long workOrderId);

    /**
     * Find by tenant and requester
     */
    List<MaterialRequestEntity> findByTenant_TenantIdAndRequester_UserId(String tenantId, Long requesterId);

    /**
     * Find by tenant and warehouse
     */
    List<MaterialRequestEntity> findByTenant_TenantIdAndWarehouse_WarehouseId(String tenantId, Long warehouseId);

    /**
     * Find by tenant and required date
     */
    List<MaterialRequestEntity> findByTenant_TenantIdAndRequiredDate(String tenantId, LocalDate requiredDate);

    /**
     * Find by tenant and priority
     */
    List<MaterialRequestEntity> findByTenant_TenantIdAndPriority(String tenantId, String priority);

    /**
     * Find by tenant and date range
     */
    List<MaterialRequestEntity> findByTenant_TenantIdAndRequestDateBetween(
        String tenantId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all material requests by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT mr FROM MaterialRequestEntity mr " +
           "JOIN FETCH mr.tenant " +
           "JOIN FETCH mr.requester " +
           "JOIN FETCH mr.warehouse " +
           "LEFT JOIN FETCH mr.workOrder " +
           "LEFT JOIN FETCH mr.approver " +
           "WHERE mr.tenant.tenantId = :tenantId " +
           "ORDER BY mr.requestDate DESC")
    List<MaterialRequestEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find by ID with all relationships eagerly loaded
     */
    @Query("SELECT mr FROM MaterialRequestEntity mr " +
           "JOIN FETCH mr.tenant " +
           "JOIN FETCH mr.requester " +
           "JOIN FETCH mr.warehouse " +
           "LEFT JOIN FETCH mr.workOrder " +
           "LEFT JOIN FETCH mr.approver " +
           "LEFT JOIN FETCH mr.items " +
           "WHERE mr.materialRequestId = :id")
    Optional<MaterialRequestEntity> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * Find by tenant and status with relationships
     */
    @Query("SELECT mr FROM MaterialRequestEntity mr " +
           "JOIN FETCH mr.tenant " +
           "JOIN FETCH mr.requester " +
           "JOIN FETCH mr.warehouse " +
           "LEFT JOIN FETCH mr.workOrder " +
           "LEFT JOIN FETCH mr.approver " +
           "WHERE mr.tenant.tenantId = :tenantId " +
           "AND mr.requestStatus = :status " +
           "ORDER BY mr.requestDate DESC")
    List<MaterialRequestEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Find by work order ID with relationships
     */
    @Query("SELECT mr FROM MaterialRequestEntity mr " +
           "JOIN FETCH mr.tenant " +
           "JOIN FETCH mr.requester " +
           "JOIN FETCH mr.warehouse " +
           "LEFT JOIN FETCH mr.approver " +
           "WHERE mr.workOrder.workOrderId = :workOrderId " +
           "ORDER BY mr.requestDate DESC")
    List<MaterialRequestEntity> findByWorkOrderIdWithRelations(@Param("workOrderId") Long workOrderId);

    /**
     * Find pending requests by warehouse
     */
    @Query("SELECT mr FROM MaterialRequestEntity mr " +
           "JOIN FETCH mr.tenant " +
           "JOIN FETCH mr.requester " +
           "JOIN FETCH mr.warehouse " +
           "LEFT JOIN FETCH mr.workOrder " +
           "WHERE mr.warehouse.warehouseId = :warehouseId " +
           "AND mr.requestStatus IN ('PENDING', 'APPROVED') " +
           "ORDER BY mr.priority DESC, mr.requiredDate ASC")
    List<MaterialRequestEntity> findPendingRequestsByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * Find urgent requests
     */
    @Query("SELECT mr FROM MaterialRequestEntity mr " +
           "JOIN FETCH mr.tenant " +
           "JOIN FETCH mr.requester " +
           "JOIN FETCH mr.warehouse " +
           "LEFT JOIN FETCH mr.workOrder " +
           "WHERE mr.tenant.tenantId = :tenantId " +
           "AND mr.priority = 'URGENT' " +
           "AND mr.requestStatus IN ('PENDING', 'APPROVED') " +
           "ORDER BY mr.requestDate ASC")
    List<MaterialRequestEntity> findUrgentRequests(@Param("tenantId") String tenantId);
}
