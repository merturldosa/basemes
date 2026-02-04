package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Purchase Order Repository
 * 구매 주문 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {

    /**
     * 테넌트별 모든 구매 주문 조회 (JOIN FETCH)
     */
    @Query("SELECT DISTINCT po FROM PurchaseOrderEntity po " +
           "JOIN FETCH po.tenant " +
           "JOIN FETCH po.supplier " +
           "JOIN FETCH po.buyer " +
           "LEFT JOIN FETCH po.items poi " +
           "LEFT JOIN FETCH poi.material " +
           "WHERE po.tenant.tenantId = :tenantId " +
           "ORDER BY po.orderDate DESC")
    List<PurchaseOrderEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * 구매 주문 ID로 조회 (JOIN FETCH)
     */
    @Query("SELECT DISTINCT po FROM PurchaseOrderEntity po " +
           "JOIN FETCH po.tenant " +
           "JOIN FETCH po.supplier " +
           "JOIN FETCH po.buyer " +
           "LEFT JOIN FETCH po.items poi " +
           "LEFT JOIN FETCH poi.material m " +
           "LEFT JOIN FETCH m.supplier " +
           "LEFT JOIN FETCH poi.purchaseRequest pr " +
           "LEFT JOIN FETCH pr.requester " +
           "WHERE po.purchaseOrderId = :purchaseOrderId")
    Optional<PurchaseOrderEntity> findByIdWithAllRelations(@Param("purchaseOrderId") Long purchaseOrderId);

    /**
     * 테넌트 및 상태별 조회
     */
    @Query("SELECT DISTINCT po FROM PurchaseOrderEntity po " +
           "JOIN FETCH po.tenant " +
           "JOIN FETCH po.supplier " +
           "JOIN FETCH po.buyer " +
           "LEFT JOIN FETCH po.items " +
           "WHERE po.tenant.tenantId = :tenantId " +
           "AND po.status = :status " +
           "ORDER BY po.orderDate DESC")
    List<PurchaseOrderEntity> findByTenantIdAndStatus(
        @Param("tenantId") String tenantId,
        @Param("status") String status
    );

    /**
     * 테넌트 및 공급업체별 조회
     */
    @Query("SELECT DISTINCT po FROM PurchaseOrderEntity po " +
           "JOIN FETCH po.tenant " +
           "JOIN FETCH po.supplier " +
           "JOIN FETCH po.buyer " +
           "LEFT JOIN FETCH po.items " +
           "WHERE po.tenant.tenantId = :tenantId " +
           "AND po.supplier.supplierId = :supplierId " +
           "ORDER BY po.orderDate DESC")
    List<PurchaseOrderEntity> findByTenantIdAndSupplierId(
        @Param("tenantId") String tenantId,
        @Param("supplierId") Long supplierId
    );

    /**
     * 테넌트 및 주문 번호로 존재 여부 확인
     */
    boolean existsByTenant_TenantIdAndOrderNo(String tenantId, String orderNo);
}
