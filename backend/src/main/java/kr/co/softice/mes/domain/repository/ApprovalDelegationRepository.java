package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ApprovalDelegationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Approval Delegation Repository
 * 결재 위임 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ApprovalDelegationRepository extends JpaRepository<ApprovalDelegationEntity, Long> {

    /**
     * Find all delegations by tenant ID
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "ORDER BY d.startDate DESC")
    List<ApprovalDelegationEntity> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find active delegations by tenant ID
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.isActive = true " +
            "ORDER BY d.startDate DESC")
    List<ApprovalDelegationEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find delegations by delegator
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.delegatorId = :delegatorId " +
            "ORDER BY d.startDate DESC")
    List<ApprovalDelegationEntity> findByTenantIdAndDelegatorId(
            @Param("tenantId") String tenantId,
            @Param("delegatorId") Long delegatorId);

    /**
     * Find delegations by delegate
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.delegateId = :delegateId " +
            "ORDER BY d.startDate DESC")
    List<ApprovalDelegationEntity> findByTenantIdAndDelegateId(
            @Param("tenantId") String tenantId,
            @Param("delegateId") Long delegateId);

    /**
     * Find effective delegation for delegator on date
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.delegatorId = :delegatorId " +
            "AND d.isActive = true " +
            "AND :date BETWEEN d.startDate AND d.endDate " +
            "ORDER BY d.startDate DESC")
    List<ApprovalDelegationEntity> findEffectiveDelegationForDelegator(
            @Param("tenantId") String tenantId,
            @Param("delegatorId") Long delegatorId,
            @Param("date") LocalDate date);

    /**
     * Find current effective delegations
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.isActive = true " +
            "AND CURRENT_DATE BETWEEN d.startDate AND d.endDate " +
            "ORDER BY d.startDate DESC")
    List<ApprovalDelegationEntity> findCurrentEffectiveDelegations(@Param("tenantId") String tenantId);

    /**
     * Find upcoming delegations
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.isActive = true " +
            "AND d.startDate > CURRENT_DATE " +
            "ORDER BY d.startDate ASC")
    List<ApprovalDelegationEntity> findUpcomingDelegations(@Param("tenantId") String tenantId);

    /**
     * Find expired delegations
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.endDate < CURRENT_DATE " +
            "ORDER BY d.endDate DESC")
    List<ApprovalDelegationEntity> findExpiredDelegations(@Param("tenantId") String tenantId);

    /**
     * Check if user has active delegation on date
     */
    @Query("SELECT COUNT(d) > 0 FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.delegatorId = :delegatorId " +
            "AND d.isActive = true " +
            "AND :date BETWEEN d.startDate AND d.endDate")
    boolean hasActiveDelegation(
            @Param("tenantId") String tenantId,
            @Param("delegatorId") Long delegatorId,
            @Param("date") LocalDate date);

    /**
     * Find overlapping delegations
     */
    @Query("SELECT d FROM ApprovalDelegationEntity d " +
            "WHERE d.tenant.tenantId = :tenantId " +
            "AND d.delegatorId = :delegatorId " +
            "AND d.isActive = true " +
            "AND (" +
            "  (d.startDate BETWEEN :startDate AND :endDate) " +
            "  OR (d.endDate BETWEEN :startDate AND :endDate) " +
            "  OR (:startDate BETWEEN d.startDate AND d.endDate) " +
            "  OR (:endDate BETWEEN d.startDate AND d.endDate)" +
            ")")
    List<ApprovalDelegationEntity> findOverlappingDelegations(
            @Param("tenantId") String tenantId,
            @Param("delegatorId") Long delegatorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
