package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.EquipmentOperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Equipment Operation Repository
 * 설비 가동 이력 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface EquipmentOperationRepository extends JpaRepository<EquipmentOperationEntity, Long> {

    /**
     * Get all operations by tenant with all relations (JOIN FETCH)
     */
    @Query("SELECT o FROM EquipmentOperationEntity o " +
           "JOIN FETCH o.tenant " +
           "JOIN FETCH o.equipment " +
           "LEFT JOIN FETCH o.workOrder " +
           "LEFT JOIN FETCH o.workResult " +
           "LEFT JOIN FETCH o.operatorUser " +
           "WHERE o.tenant.tenantId = :tenantId " +
           "ORDER BY o.operationDate DESC, o.startTime DESC")
    List<EquipmentOperationEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Get operation by ID with all relations (JOIN FETCH)
     */
    @Query("SELECT o FROM EquipmentOperationEntity o " +
           "JOIN FETCH o.tenant " +
           "JOIN FETCH o.equipment " +
           "LEFT JOIN FETCH o.workOrder " +
           "LEFT JOIN FETCH o.workResult " +
           "LEFT JOIN FETCH o.operatorUser " +
           "WHERE o.operationId = :operationId")
    Optional<EquipmentOperationEntity> findByIdWithAllRelations(@Param("operationId") Long operationId);

    /**
     * Get operations by equipment
     */
    @Query("SELECT o FROM EquipmentOperationEntity o " +
           "JOIN FETCH o.tenant " +
           "JOIN FETCH o.equipment " +
           "LEFT JOIN FETCH o.workOrder " +
           "LEFT JOIN FETCH o.workResult " +
           "LEFT JOIN FETCH o.operatorUser " +
           "WHERE o.equipment.equipmentId = :equipmentId " +
           "ORDER BY o.operationDate DESC, o.startTime DESC")
    List<EquipmentOperationEntity> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * Get operations by tenant and date range
     */
    @Query("SELECT o FROM EquipmentOperationEntity o " +
           "JOIN FETCH o.tenant " +
           "JOIN FETCH o.equipment " +
           "LEFT JOIN FETCH o.workOrder " +
           "LEFT JOIN FETCH o.workResult " +
           "LEFT JOIN FETCH o.operatorUser " +
           "WHERE o.tenant.tenantId = :tenantId " +
           "AND o.operationDate BETWEEN :startDate AND :endDate " +
           "ORDER BY o.operationDate DESC, o.startTime DESC")
    List<EquipmentOperationEntity> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get operations by tenant and status
     */
    @Query("SELECT o FROM EquipmentOperationEntity o " +
           "JOIN FETCH o.tenant " +
           "JOIN FETCH o.equipment " +
           "LEFT JOIN FETCH o.workOrder " +
           "LEFT JOIN FETCH o.workResult " +
           "LEFT JOIN FETCH o.operatorUser " +
           "WHERE o.tenant.tenantId = :tenantId " +
           "AND o.operationStatus = :operationStatus " +
           "ORDER BY o.operationDate DESC, o.startTime DESC")
    List<EquipmentOperationEntity> findByTenantIdAndOperationStatus(@Param("tenantId") String tenantId, @Param("operationStatus") String operationStatus);
}
