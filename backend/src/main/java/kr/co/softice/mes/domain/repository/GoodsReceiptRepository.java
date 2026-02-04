package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.GoodsReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Goods Receipt Repository
 * 입하 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceiptEntity, Long> {

    /**
     * Find all goods receipts by tenant with all relations
     */
    @Query("SELECT DISTINCT gr FROM GoodsReceiptEntity gr " +
           "JOIN FETCH gr.tenant " +
           "LEFT JOIN FETCH gr.purchaseOrder po " +
           "LEFT JOIN FETCH po.tenant " +
           "LEFT JOIN FETCH gr.supplier s " +
           "LEFT JOIN FETCH s.tenant " +
           "JOIN FETCH gr.warehouse w " +
           "JOIN FETCH w.tenant " +
           "LEFT JOIN FETCH gr.receiver " +
           "LEFT JOIN FETCH gr.items gri " +
           "LEFT JOIN FETCH gri.purchaseOrderItem " +
           "LEFT JOIN FETCH gri.product p " +
           "LEFT JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH gri.qualityInspection " +
           "WHERE gr.tenant.tenantId = :tenantId " +
           "ORDER BY gr.receiptDate DESC")
    List<GoodsReceiptEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find goods receipt by ID with all relations
     */
    @Query("SELECT DISTINCT gr FROM GoodsReceiptEntity gr " +
           "JOIN FETCH gr.tenant " +
           "LEFT JOIN FETCH gr.purchaseOrder po " +
           "LEFT JOIN FETCH po.tenant " +
           "LEFT JOIN FETCH gr.supplier s " +
           "LEFT JOIN FETCH s.tenant " +
           "JOIN FETCH gr.warehouse w " +
           "JOIN FETCH w.tenant " +
           "LEFT JOIN FETCH gr.receiver " +
           "LEFT JOIN FETCH gr.items gri " +
           "LEFT JOIN FETCH gri.purchaseOrderItem " +
           "LEFT JOIN FETCH gri.product p " +
           "LEFT JOIN FETCH p.tenant " +
           "LEFT JOIN FETCH gri.qualityInspection " +
           "WHERE gr.goodsReceiptId = :goodsReceiptId")
    Optional<GoodsReceiptEntity> findByIdWithAllRelations(@Param("goodsReceiptId") Long goodsReceiptId);

    /**
     * Find goods receipts by status
     */
    @Query("SELECT DISTINCT gr FROM GoodsReceiptEntity gr " +
           "JOIN FETCH gr.tenant " +
           "LEFT JOIN FETCH gr.purchaseOrder " +
           "LEFT JOIN FETCH gr.supplier " +
           "JOIN FETCH gr.warehouse " +
           "LEFT JOIN FETCH gr.receiver " +
           "WHERE gr.tenant.tenantId = :tenantId " +
           "AND gr.receiptStatus = :status " +
           "ORDER BY gr.receiptDate DESC")
    List<GoodsReceiptEntity> findByTenantIdAndStatus(@Param("tenantId") String tenantId,
                                                      @Param("status") String status);

    /**
     * Find goods receipts by purchase order
     */
    @Query("SELECT DISTINCT gr FROM GoodsReceiptEntity gr " +
           "JOIN FETCH gr.tenant " +
           "LEFT JOIN FETCH gr.purchaseOrder " +
           "LEFT JOIN FETCH gr.supplier " +
           "JOIN FETCH gr.warehouse " +
           "LEFT JOIN FETCH gr.receiver " +
           "WHERE gr.tenant.tenantId = :tenantId " +
           "AND gr.purchaseOrder.purchaseOrderId = :purchaseOrderId " +
           "ORDER BY gr.receiptDate DESC")
    List<GoodsReceiptEntity> findByTenantIdAndPurchaseOrderId(@Param("tenantId") String tenantId,
                                                                @Param("purchaseOrderId") Long purchaseOrderId);

    /**
     * Find goods receipts by warehouse
     */
    @Query("SELECT DISTINCT gr FROM GoodsReceiptEntity gr " +
           "JOIN FETCH gr.tenant " +
           "LEFT JOIN FETCH gr.purchaseOrder " +
           "LEFT JOIN FETCH gr.supplier " +
           "JOIN FETCH gr.warehouse " +
           "LEFT JOIN FETCH gr.receiver " +
           "WHERE gr.tenant.tenantId = :tenantId " +
           "AND gr.warehouse.warehouseId = :warehouseId " +
           "ORDER BY gr.receiptDate DESC")
    List<GoodsReceiptEntity> findByTenantIdAndWarehouseId(@Param("tenantId") String tenantId,
                                                           @Param("warehouseId") Long warehouseId);

    /**
     * Find goods receipts by date range
     */
    @Query("SELECT DISTINCT gr FROM GoodsReceiptEntity gr " +
           "JOIN FETCH gr.tenant " +
           "LEFT JOIN FETCH gr.purchaseOrder " +
           "LEFT JOIN FETCH gr.supplier " +
           "JOIN FETCH gr.warehouse " +
           "LEFT JOIN FETCH gr.receiver " +
           "WHERE gr.tenant.tenantId = :tenantId " +
           "AND gr.receiptDate BETWEEN :startDate AND :endDate " +
           "ORDER BY gr.receiptDate DESC")
    List<GoodsReceiptEntity> findByTenantIdAndDateRange(@Param("tenantId") String tenantId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Check if receipt no exists
     */
    boolean existsByTenant_TenantIdAndReceiptNo(String tenantId, String receiptNo);
}
