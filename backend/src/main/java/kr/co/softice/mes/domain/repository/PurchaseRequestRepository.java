package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.PurchaseRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Purchase Request Repository
 * 구매 요청 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequestEntity, Long> {

    /**
     * 테넌트별 모든 구매 요청 조회 (JOIN FETCH)
     */
    @Query("SELECT pr FROM PurchaseRequestEntity pr " +
           "JOIN FETCH pr.tenant " +
           "JOIN FETCH pr.requester " +
           "JOIN FETCH pr.material m " +
           "LEFT JOIN FETCH m.supplier " +
           "LEFT JOIN FETCH pr.approver " +
           "WHERE pr.tenant.tenantId = :tenantId " +
           "ORDER BY pr.requestDate DESC")
    List<PurchaseRequestEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * 구매 요청 ID로 조회 (JOIN FETCH)
     */
    @Query("SELECT pr FROM PurchaseRequestEntity pr " +
           "JOIN FETCH pr.tenant " +
           "JOIN FETCH pr.requester " +
           "JOIN FETCH pr.material m " +
           "LEFT JOIN FETCH m.supplier " +
           "LEFT JOIN FETCH pr.approver " +
           "WHERE pr.purchaseRequestId = :purchaseRequestId")
    Optional<PurchaseRequestEntity> findByIdWithAllRelations(@Param("purchaseRequestId") Long purchaseRequestId);

    /**
     * 테넌트 및 상태별 조회
     */
    @Query("SELECT pr FROM PurchaseRequestEntity pr " +
           "JOIN FETCH pr.tenant " +
           "JOIN FETCH pr.requester " +
           "JOIN FETCH pr.material m " +
           "LEFT JOIN FETCH m.supplier " +
           "LEFT JOIN FETCH pr.approver " +
           "WHERE pr.tenant.tenantId = :tenantId " +
           "AND pr.status = :status " +
           "ORDER BY pr.requestDate DESC")
    List<PurchaseRequestEntity> findByTenantIdAndStatus(
        @Param("tenantId") String tenantId,
        @Param("status") String status
    );

    /**
     * 테넌트 및 요청 번호로 존재 여부 확인
     */
    boolean existsByTenant_TenantIdAndRequestNo(String tenantId, String requestNo);
}
