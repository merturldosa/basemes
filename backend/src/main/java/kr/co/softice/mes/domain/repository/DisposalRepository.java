package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.DisposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Disposal Repository
 * 폐기 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface DisposalRepository extends JpaRepository<DisposalEntity, Long> {

    /**
     * Find by tenant and disposal no
     */
    Optional<DisposalEntity> findByTenant_TenantIdAndDisposalNo(String tenantId, String disposalNo);

    /**
     * Check if disposal no exists
     */
    boolean existsByTenant_TenantIdAndDisposalNo(String tenantId, String disposalNo);

    /**
     * Find by tenant ID
     */
    List<DisposalEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and status
     */
    List<DisposalEntity> findByTenant_TenantIdAndDisposalStatus(String tenantId, String disposalStatus);

    /**
     * Find by tenant and type
     */
    List<DisposalEntity> findByTenant_TenantIdAndDisposalType(String tenantId, String disposalType);

    /**
     * Find by tenant and work order
     */
    List<DisposalEntity> findByTenant_TenantIdAndWorkOrder_WorkOrderId(String tenantId, Long workOrderId);

    /**
     * Find by tenant and requester
     */
    List<DisposalEntity> findByTenant_TenantIdAndRequester_UserId(String tenantId, Long requesterId);

    /**
     * Find by tenant and warehouse
     */
    List<DisposalEntity> findByTenant_TenantIdAndWarehouse_WarehouseId(String tenantId, Long warehouseId);

    /**
     * Find by tenant and date range
     */
    List<DisposalEntity> findByTenant_TenantIdAndDisposalDateBetween(
        String tenantId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all disposals by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT d FROM DisposalEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.requester " +
           "JOIN FETCH d.warehouse " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.approver " +
           "LEFT JOIN FETCH d.processor " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "ORDER BY d.disposalDate DESC")
    List<DisposalEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find by ID with all relationships eagerly loaded
     */
    @Query("SELECT d FROM DisposalEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.requester " +
           "JOIN FETCH d.warehouse " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.approver " +
           "LEFT JOIN FETCH d.processor " +
           "LEFT JOIN FETCH d.items " +
           "WHERE d.disposalId = :id")
    Optional<DisposalEntity> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * Find by tenant and status with relationships
     */
    @Query("SELECT d FROM DisposalEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.requester " +
           "JOIN FETCH d.warehouse " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.approver " +
           "LEFT JOIN FETCH d.processor " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.disposalStatus = :status " +
           "ORDER BY d.disposalDate DESC")
    List<DisposalEntity> findByTenantIdAndStatusWithRelations(
        @Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * Find by tenant and type with relationships
     */
    @Query("SELECT d FROM DisposalEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.requester " +
           "JOIN FETCH d.warehouse " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.approver " +
           "LEFT JOIN FETCH d.processor " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.disposalType = :type " +
           "ORDER BY d.disposalDate DESC")
    List<DisposalEntity> findByTenantIdAndTypeWithRelations(
        @Param("tenantId") String tenantId, @Param("type") String type);

    /**
     * Find pending disposals by warehouse
     */
    @Query("SELECT d FROM DisposalEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.requester " +
           "JOIN FETCH d.warehouse " +
           "LEFT JOIN FETCH d.workOrder " +
           "WHERE d.warehouse.warehouseId = :warehouseId " +
           "AND d.disposalStatus IN ('PENDING', 'APPROVED') " +
           "ORDER BY d.disposalDate ASC")
    List<DisposalEntity> findPendingDisposalsByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * Find approved disposals requiring processing
     */
    @Query("SELECT d FROM DisposalEntity d " +
           "JOIN FETCH d.tenant " +
           "JOIN FETCH d.requester " +
           "JOIN FETCH d.warehouse " +
           "LEFT JOIN FETCH d.workOrder " +
           "LEFT JOIN FETCH d.approver " +
           "WHERE d.tenant.tenantId = :tenantId " +
           "AND d.disposalStatus = 'APPROVED' " +
           "ORDER BY d.disposalDate ASC")
    List<DisposalEntity> findApprovedDisposals(@Param("tenantId") String tenantId);
}
