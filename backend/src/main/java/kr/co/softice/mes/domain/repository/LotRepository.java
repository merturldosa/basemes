package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lot Repository
 * LOT 관리 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface LotRepository extends JpaRepository<LotEntity, Long> {

    Optional<LotEntity> findByTenantAndLotNo(TenantEntity tenant, String lotNo);
    Optional<LotEntity> findByTenant_TenantIdAndLotNo(String tenantId, String lotNo);
    List<LotEntity> findByTenant_TenantId(String tenantId);
    List<LotEntity> findByTenant_TenantIdAndQualityStatus(String tenantId, String qualityStatus);
    List<LotEntity> findByTenant_TenantIdAndProduct_ProductId(String tenantId, Long productId);
    List<LotEntity> findByTenant_TenantIdAndProduct_ProductIdOrderByCreatedAtAsc(String tenantId, Long productId);
    boolean existsByTenantAndLotNo(TenantEntity tenant, String lotNo);
    List<LotEntity> findByTenant_TenantIdAndExpiryDateBeforeAndIsActiveTrue(String tenantId, LocalDate expiryDate);

    @Query("SELECT l FROM LotEntity l " +
           "JOIN FETCH l.tenant " +
           "JOIN FETCH l.product " +
           "LEFT JOIN FETCH l.workOrder " +
           "WHERE l.tenant.tenantId = :tenantId " +
           "ORDER BY l.lotNo ASC")
    List<LotEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    @Query("SELECT l FROM LotEntity l " +
           "JOIN FETCH l.tenant " +
           "JOIN FETCH l.product " +
           "LEFT JOIN FETCH l.workOrder " +
           "WHERE l.lotId = :lotId")
    Optional<LotEntity> findByIdWithAllRelations(@Param("lotId") Long lotId);
}
