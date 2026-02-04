package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.AlarmTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Alarm Template Repository
 * 알람 템플릿 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface AlarmTemplateRepository extends JpaRepository<AlarmTemplateEntity, Long> {

    /**
     * Find all templates by tenant ID
     */
    @Query("SELECT t FROM AlarmTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "ORDER BY t.alarmType, t.eventType")
    List<AlarmTemplateEntity> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find active templates by tenant ID
     */
    @Query("SELECT t FROM AlarmTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.isActive = true " +
            "ORDER BY t.alarmType, t.eventType")
    List<AlarmTemplateEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find template by code
     */
    @Query("SELECT t FROM AlarmTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.templateCode = :templateCode")
    Optional<AlarmTemplateEntity> findByTenantIdAndTemplateCode(
            @Param("tenantId") String tenantId,
            @Param("templateCode") String templateCode);

    /**
     * Find template by event type
     */
    @Query("SELECT t FROM AlarmTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.eventType = :eventType " +
            "AND t.isActive = true")
    Optional<AlarmTemplateEntity> findByTenantIdAndEventType(
            @Param("tenantId") String tenantId,
            @Param("eventType") String eventType);

    /**
     * Find templates by alarm type
     */
    @Query("SELECT t FROM AlarmTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.alarmType = :alarmType " +
            "AND t.isActive = true " +
            "ORDER BY t.eventType")
    List<AlarmTemplateEntity> findByTenantIdAndAlarmType(
            @Param("tenantId") String tenantId,
            @Param("alarmType") String alarmType);

    /**
     * Check if template code exists
     */
    @Query("SELECT COUNT(t) > 0 FROM AlarmTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.templateCode = :templateCode")
    boolean existsByTenantIdAndTemplateCode(
            @Param("tenantId") String tenantId,
            @Param("templateCode") String templateCode);
}
