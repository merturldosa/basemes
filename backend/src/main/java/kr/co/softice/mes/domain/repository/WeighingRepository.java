package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.WeighingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Weighing Repository
 * 칭량 기록 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface WeighingRepository extends JpaRepository<WeighingEntity, Long> {

    /**
     * Find by tenant and weighing no
     */
    Optional<WeighingEntity> findByTenant_TenantIdAndWeighingNo(String tenantId, String weighingNo);

    /**
     * Check if weighing no exists
     */
    boolean existsByTenant_TenantIdAndWeighingNo(String tenantId, String weighingNo);

    /**
     * Find by tenant ID
     */
    List<WeighingEntity> findByTenant_TenantId(String tenantId);

    /**
     * Find by tenant and weighing type
     */
    List<WeighingEntity> findByTenant_TenantIdAndWeighingType(String tenantId, String weighingType);

    /**
     * Find by tenant and verification status
     */
    List<WeighingEntity> findByTenant_TenantIdAndVerificationStatus(String tenantId, String verificationStatus);

    /**
     * Find by tenant and reference (polymorphic)
     */
    List<WeighingEntity> findByTenant_TenantIdAndReferenceTypeAndReferenceId(
        String tenantId, String referenceType, Long referenceId);

    /**
     * Find by tenant and product
     */
    List<WeighingEntity> findByTenant_TenantIdAndProduct_ProductId(String tenantId, Long productId);

    /**
     * Find by tenant and lot
     */
    List<WeighingEntity> findByTenant_TenantIdAndLot_LotId(String tenantId, Long lotId);

    /**
     * Find by tenant and operator
     */
    List<WeighingEntity> findByTenant_TenantIdAndOperator_UserId(String tenantId, Long operatorId);

    /**
     * Find by tenant and date range
     */
    List<WeighingEntity> findByTenant_TenantIdAndWeighingDateBetween(
        String tenantId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all weighings by tenant ID with all relationships eagerly loaded
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "LEFT JOIN FETCH w.verifier " +
           "WHERE w.tenant.tenantId = :tenantId " +
           "ORDER BY w.weighingDate DESC")
    List<WeighingEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find by ID with all relationships eagerly loaded
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "LEFT JOIN FETCH w.verifier " +
           "WHERE w.weighingId = :id")
    Optional<WeighingEntity> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * Find by reference type and ID with relationships
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "LEFT JOIN FETCH w.verifier " +
           "WHERE w.referenceType = :referenceType " +
           "AND w.referenceId = :referenceId " +
           "ORDER BY w.weighingDate DESC")
    List<WeighingEntity> findByReferenceTypeAndReferenceIdWithRelations(
        @Param("referenceType") String referenceType,
        @Param("referenceId") Long referenceId);

    /**
     * Find by product ID and date range with relationships
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "LEFT JOIN FETCH w.verifier " +
           "WHERE w.product.productId = :productId " +
           "AND w.weighingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY w.weighingDate DESC")
    List<WeighingEntity> findByProductIdAndDateRangeWithRelations(
        @Param("productId") Long productId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Find by verification status and tenant with relationships
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "LEFT JOIN FETCH w.verifier " +
           "WHERE w.tenant.tenantId = :tenantId " +
           "AND w.verificationStatus = :status " +
           "ORDER BY w.weighingDate DESC")
    List<WeighingEntity> findByVerificationStatusAndTenantWithRelations(
        @Param("tenantId") String tenantId,
        @Param("status") String status);

    /**
     * Find tolerance exceeded weighings
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "LEFT JOIN FETCH w.verifier " +
           "WHERE w.tenant.tenantId = :tenantId " +
           "AND w.toleranceExceeded = true " +
           "ORDER BY w.weighingDate DESC")
    List<WeighingEntity> findToleranceExceededWeighings(@Param("tenantId") String tenantId);

    /**
     * Find pending verification weighings
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "WHERE w.tenant.tenantId = :tenantId " +
           "AND w.verificationStatus = 'PENDING' " +
           "ORDER BY w.weighingDate ASC")
    List<WeighingEntity> findPendingVerificationWeighings(@Param("tenantId") String tenantId);

    /**
     * Find weighings by type and date range
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "LEFT JOIN FETCH w.verifier " +
           "WHERE w.tenant.tenantId = :tenantId " +
           "AND w.weighingType = :weighingType " +
           "AND w.weighingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY w.weighingDate DESC")
    List<WeighingEntity> findByTypeAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("weighingType") String weighingType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Find unverified tolerance exceeded weighings (requires immediate attention)
     */
    @Query("SELECT w FROM WeighingEntity w " +
           "JOIN FETCH w.tenant " +
           "JOIN FETCH w.product " +
           "JOIN FETCH w.operator " +
           "LEFT JOIN FETCH w.lot " +
           "WHERE w.tenant.tenantId = :tenantId " +
           "AND w.toleranceExceeded = true " +
           "AND w.verificationStatus = 'PENDING' " +
           "ORDER BY w.weighingDate ASC")
    List<WeighingEntity> findUnverifiedToleranceExceeded(@Param("tenantId") String tenantId);
}
