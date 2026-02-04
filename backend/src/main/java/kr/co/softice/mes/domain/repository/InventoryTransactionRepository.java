package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.InventoryTransactionEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Inventory Transaction Repository
 * 재고 이동 내역 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransactionEntity, Long> {

    Optional<InventoryTransactionEntity> findByTenantAndTransactionNo(TenantEntity tenant, String transactionNo);
    Optional<InventoryTransactionEntity> findByTenant_TenantIdAndTransactionNo(String tenantId, String transactionNo);
    List<InventoryTransactionEntity> findByTenant_TenantId(String tenantId);
    List<InventoryTransactionEntity> findByTenant_TenantIdAndTransactionType(String tenantId, String transactionType);
    List<InventoryTransactionEntity> findByTenant_TenantIdAndApprovalStatus(String tenantId, String approvalStatus);
    List<InventoryTransactionEntity> findByTenant_TenantIdAndTransactionDateBetween(String tenantId, LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByTenantAndTransactionNo(TenantEntity tenant, String transactionNo);

    @Query("SELECT t FROM InventoryTransactionEntity t " +
           "WHERE t.tenant.tenantId = :tenantId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<InventoryTransactionEntity> findByTenantIdAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM InventoryTransactionEntity t " +
           "JOIN FETCH t.tenant " +
           "JOIN FETCH t.warehouse " +
           "JOIN FETCH t.product " +
           "LEFT JOIN FETCH t.lot " +
           "LEFT JOIN FETCH t.fromWarehouse " +
           "LEFT JOIN FETCH t.toWarehouse " +
           "LEFT JOIN FETCH t.workOrder " +
           "LEFT JOIN FETCH t.qualityInspection " +
           "JOIN FETCH t.transactionUser " +
           "LEFT JOIN FETCH t.approvedBy " +
           "WHERE t.tenant.tenantId = :tenantId " +
           "ORDER BY t.transactionDate DESC")
    List<InventoryTransactionEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT t FROM InventoryTransactionEntity t " +
           "JOIN FETCH t.tenant " +
           "JOIN FETCH t.warehouse " +
           "JOIN FETCH t.product " +
           "LEFT JOIN FETCH t.lot " +
           "LEFT JOIN FETCH t.fromWarehouse " +
           "LEFT JOIN FETCH t.toWarehouse " +
           "LEFT JOIN FETCH t.workOrder " +
           "LEFT JOIN FETCH t.qualityInspection " +
           "JOIN FETCH t.transactionUser " +
           "LEFT JOIN FETCH t.approvedBy " +
           "WHERE t.transactionId = :transactionId")
    Optional<InventoryTransactionEntity> findByIdWithAllRelations(@Param("transactionId") Long transactionId);
}
