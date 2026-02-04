package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.MoldProductionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Mold Production History Repository
 * 금형 생산 이력 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface MoldProductionHistoryRepository extends JpaRepository<MoldProductionHistoryEntity, Long> {

    /**
     * Get all histories by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT mph FROM MoldProductionHistoryEntity mph " +
           "JOIN FETCH mph.tenant " +
           "JOIN FETCH mph.mold " +
           "LEFT JOIN FETCH mph.workOrder " +
           "LEFT JOIN FETCH mph.workResult " +
           "LEFT JOIN FETCH mph.operatorUser " +
           "WHERE mph.tenant.tenantId = :tenantId " +
           "ORDER BY mph.productionDate DESC, mph.createdAt DESC")
    List<MoldProductionHistoryEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get history by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT mph FROM MoldProductionHistoryEntity mph " +
           "JOIN FETCH mph.tenant " +
           "JOIN FETCH mph.mold " +
           "LEFT JOIN FETCH mph.workOrder " +
           "LEFT JOIN FETCH mph.workResult " +
           "LEFT JOIN FETCH mph.operatorUser " +
           "WHERE mph.historyId = :historyId")
    Optional<MoldProductionHistoryEntity> findByIdWithAllRelations(@Param("historyId") Long historyId);

    /**
     * Get histories by mold
     */
    @Query("SELECT mph FROM MoldProductionHistoryEntity mph " +
           "JOIN FETCH mph.tenant " +
           "JOIN FETCH mph.mold " +
           "LEFT JOIN FETCH mph.workOrder " +
           "LEFT JOIN FETCH mph.workResult " +
           "LEFT JOIN FETCH mph.operatorUser " +
           "WHERE mph.mold.moldId = :moldId " +
           "ORDER BY mph.productionDate DESC, mph.createdAt DESC")
    List<MoldProductionHistoryEntity> findByMoldId(@Param("moldId") Long moldId);

    /**
     * Get histories by date range
     */
    @Query("SELECT mph FROM MoldProductionHistoryEntity mph " +
           "JOIN FETCH mph.tenant " +
           "JOIN FETCH mph.mold " +
           "LEFT JOIN FETCH mph.workOrder " +
           "LEFT JOIN FETCH mph.workResult " +
           "LEFT JOIN FETCH mph.operatorUser " +
           "WHERE mph.tenant.tenantId = :tenantId " +
           "AND mph.productionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY mph.productionDate DESC, mph.createdAt DESC")
    List<MoldProductionHistoryEntity> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get histories by work order
     */
    @Query("SELECT mph FROM MoldProductionHistoryEntity mph " +
           "JOIN FETCH mph.tenant " +
           "JOIN FETCH mph.mold " +
           "LEFT JOIN FETCH mph.workOrder " +
           "LEFT JOIN FETCH mph.workResult " +
           "LEFT JOIN FETCH mph.operatorUser " +
           "WHERE mph.workOrder.workOrderId = :workOrderId " +
           "ORDER BY mph.productionDate DESC, mph.createdAt DESC")
    List<MoldProductionHistoryEntity> findByWorkOrderId(@Param("workOrderId") Long workOrderId);
}
