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
 * Material Handover Repository
 * 자재 인수인계 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface MaterialHandoverRepository extends JpaRepository<MaterialHandoverEntity, Long> {

    /**
     * Find by tenant and handover no
     */
    Optional<MaterialHandoverEntity> findByTenant_TenantIdAndHandoverNo(String tenantId, String handoverNo);

    /**
     * Check if handover no exists
     */
    boolean existsByTenant_TenantIdAndHandoverNo(String tenantId, String handoverNo);

    /**
     * Find by tenant ID
     */
    List<MaterialHandoverEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and material request
     */
    List<MaterialHandoverEntity> findByTenant_TenantIdAndMaterialRequest_MaterialRequestId(
        String tenantId, Long materialRequestId);

    /**
     * Find by tenant and issuer
     */
    List<MaterialHandoverEntity> findByTenant_TenantIdAndIssuer_UserId(String tenantId, Long issuerId);

    /**
     * Find by tenant and receiver
     */
    List<MaterialHandoverEntity> findByTenant_TenantIdAndReceiver_UserId(String tenantId, Long receiverId);

    /**
     * Find by tenant and status
     */
    List<MaterialHandoverEntity> findByTenant_TenantIdAndHandoverStatus(String tenantId, String handoverStatus);

    /**
     * Find by tenant and date range
     */
    List<MaterialHandoverEntity> findByTenant_TenantIdAndHandoverDateBetween(
        String tenantId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all material handovers by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT mh FROM MaterialHandoverEntity mh " +
           "JOIN FETCH mh.tenant " +
           "JOIN FETCH mh.materialRequest mr " +
           "JOIN FETCH mh.materialRequestItem " +
           "JOIN FETCH mh.inventoryTransaction " +
           "JOIN FETCH mh.product " +
           "LEFT JOIN FETCH mh.lot " +
           "JOIN FETCH mh.issuer " +
           "JOIN FETCH mh.receiver " +
           "WHERE mh.tenant.tenantId = :tenantId " +
           "ORDER BY mh.handoverDate DESC")
    List<MaterialHandoverEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find by ID with all relationships eagerly loaded
     */
    @Query("SELECT mh FROM MaterialHandoverEntity mh " +
           "JOIN FETCH mh.tenant " +
           "JOIN FETCH mh.materialRequest " +
           "JOIN FETCH mh.materialRequestItem " +
           "JOIN FETCH mh.inventoryTransaction " +
           "JOIN FETCH mh.product " +
           "LEFT JOIN FETCH mh.lot " +
           "JOIN FETCH mh.issuer " +
           "JOIN FETCH mh.receiver " +
           "WHERE mh.materialHandoverId = :id")
    Optional<MaterialHandoverEntity> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * Find by material request ID with relationships
     */
    @Query("SELECT mh FROM MaterialHandoverEntity mh " +
           "JOIN FETCH mh.tenant " +
           "JOIN FETCH mh.materialRequest " +
           "JOIN FETCH mh.materialRequestItem " +
           "JOIN FETCH mh.inventoryTransaction " +
           "JOIN FETCH mh.product " +
           "LEFT JOIN FETCH mh.lot " +
           "JOIN FETCH mh.issuer " +
           "JOIN FETCH mh.receiver " +
           "WHERE mh.materialRequest.materialRequestId = :materialRequestId " +
           "ORDER BY mh.handoverDate DESC")
    List<MaterialHandoverEntity> findByMaterialRequestIdWithRelations(@Param("materialRequestId") Long materialRequestId);

    /**
     * Find pending handovers by receiver
     */
    @Query("SELECT mh FROM MaterialHandoverEntity mh " +
           "JOIN FETCH mh.tenant " +
           "JOIN FETCH mh.materialRequest " +
           "JOIN FETCH mh.materialRequestItem " +
           "JOIN FETCH mh.inventoryTransaction " +
           "JOIN FETCH mh.product " +
           "LEFT JOIN FETCH mh.lot " +
           "JOIN FETCH mh.issuer " +
           "JOIN FETCH mh.receiver " +
           "WHERE mh.receiver.userId = :receiverId " +
           "AND mh.handoverStatus = 'PENDING' " +
           "ORDER BY mh.handoverDate ASC")
    List<MaterialHandoverEntity> findPendingHandoversByReceiver(@Param("receiverId") Long receiverId);

    /**
     * Find handovers by status with relationships
     */
    @Query("SELECT mh FROM MaterialHandoverEntity mh " +
           "JOIN FETCH mh.tenant " +
           "JOIN FETCH mh.materialRequest " +
           "JOIN FETCH mh.materialRequestItem " +
           "JOIN FETCH mh.inventoryTransaction " +
           "JOIN FETCH mh.product " +
           "LEFT JOIN FETCH mh.lot " +
           "JOIN FETCH mh.issuer " +
           "JOIN FETCH mh.receiver " +
           "WHERE mh.tenant.tenantId = :tenantId " +
           "AND mh.handoverStatus = :status " +
           "ORDER BY mh.handoverDate DESC")
    List<MaterialHandoverEntity> findByTenantIdAndStatusWithRelations(
        @Param("tenantId") String tenantId, @Param("status") String status);
}
