package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.AlarmHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alarm History Repository
 * 알람 이력 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface AlarmHistoryRepository extends JpaRepository<AlarmHistoryEntity, Long> {

    /**
     * Find all alarms by recipient
     */
    @Query("SELECT a FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.recipientUserId = :userId " +
            "ORDER BY a.createdAt DESC")
    List<AlarmHistoryEntity> findByRecipient(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId);

    /**
     * Find unread alarms by recipient
     */
    @Query("SELECT a FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.recipientUserId = :userId " +
            "AND a.isRead = false " +
            "AND a.sendStatus = 'SENT' " +
            "ORDER BY a.createdAt DESC")
    List<AlarmHistoryEntity> findUnreadByRecipient(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId);

    /**
     * Find recent alarms by recipient (last 7 days)
     */
    @Query("SELECT a FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.recipientUserId = :userId " +
            "AND a.createdAt >= :since " +
            "ORDER BY a.createdAt DESC")
    List<AlarmHistoryEntity> findRecentByRecipient(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    /**
     * Find alarms by type
     */
    @Query("SELECT a FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.alarmType = :alarmType " +
            "ORDER BY a.createdAt DESC")
    List<AlarmHistoryEntity> findByAlarmType(
            @Param("tenantId") String tenantId,
            @Param("alarmType") String alarmType);

    /**
     * Find alarms by reference
     */
    @Query("SELECT a FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.referenceType = :referenceType " +
            "AND a.referenceId = :referenceId " +
            "ORDER BY a.createdAt DESC")
    List<AlarmHistoryEntity> findByReference(
            @Param("tenantId") String tenantId,
            @Param("referenceType") String referenceType,
            @Param("referenceId") Long referenceId);

    /**
     * Count unread alarms by recipient
     */
    @Query("SELECT COUNT(a) FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.recipientUserId = :userId " +
            "AND a.isRead = false " +
            "AND a.sendStatus = 'SENT'")
    Long countUnreadByRecipient(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId);

    /**
     * Count alarms by type and recipient
     */
    @Query("SELECT COUNT(a) FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.recipientUserId = :userId " +
            "AND a.alarmType = :alarmType")
    Long countByTypeAndRecipient(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId,
            @Param("alarmType") String alarmType);

    /**
     * Find failed alarms
     */
    @Query("SELECT a FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.sendStatus = 'FAILED' " +
            "ORDER BY a.createdAt DESC")
    List<AlarmHistoryEntity> findFailedAlarms(@Param("tenantId") String tenantId);

    /**
     * Delete old alarms (retention policy)
     */
    @Query("DELETE FROM AlarmHistoryEntity a " +
            "WHERE a.tenant.tenantId = :tenantId " +
            "AND a.createdAt < :before")
    void deleteOldAlarms(
            @Param("tenantId") String tenantId,
            @Param("before") LocalDateTime before);
}
