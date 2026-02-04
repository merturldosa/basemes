package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification Repository
 * @author Moon Myung-seop
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /**
     * Find all notifications for a user (including broadcasts)
     */
    @Query("SELECT n FROM NotificationEntity n " +
           "WHERE n.tenant.tenantId = :tenantId " +
           "AND (n.user.userId = :userId OR n.user IS NULL) " +
           "AND (n.expiresAt IS NULL OR n.expiresAt > :now) " +
           "ORDER BY n.createdAt DESC")
    List<NotificationEntity> findByTenantIdAndUserId(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    /**
     * Find unread notifications for a user
     */
    @Query("SELECT n FROM NotificationEntity n " +
           "WHERE n.tenant.tenantId = :tenantId " +
           "AND (n.user.userId = :userId OR n.user IS NULL) " +
           "AND n.isRead = false " +
           "AND (n.expiresAt IS NULL OR n.expiresAt > :now) " +
           "ORDER BY n.createdAt DESC")
    List<NotificationEntity> findUnreadByTenantIdAndUserId(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    /**
     * Count unread notifications for a user
     */
    @Query("SELECT COUNT(n) FROM NotificationEntity n " +
           "WHERE n.tenant.tenantId = :tenantId " +
           "AND (n.user.userId = :userId OR n.user IS NULL) " +
           "AND n.isRead = false " +
           "AND (n.expiresAt IS NULL OR n.expiresAt > :now)")
    long countUnreadByTenantIdAndUserId(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    /**
     * Find notifications by category
     */
    @Query("SELECT n FROM NotificationEntity n " +
           "WHERE n.tenant.tenantId = :tenantId " +
           "AND n.category = :category " +
           "ORDER BY n.createdAt DESC")
    List<NotificationEntity> findByTenantIdAndCategory(
            @Param("tenantId") String tenantId,
            @Param("category") String category
    );

    /**
     * Delete expired notifications
     */
    @Query("DELETE FROM NotificationEntity n " +
           "WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
