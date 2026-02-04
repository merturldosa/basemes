package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.NotificationEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.NotificationRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification Service
 * Handles notification creation, persistence, and real-time broadcasting
 * @author Moon Myung-seop
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create and broadcast notification to specific user
     */
    public NotificationEntity createUserNotification(
            String tenantId,
            Long userId,
            String type,
            String category,
            String title,
            String message,
            String referenceType,
            Long referenceId,
            String priority) {

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        NotificationEntity notification = NotificationEntity.builder()
                .tenant(tenant)
                .user(user)
                .notificationType(type)
                .category(category)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .priority(priority != null ? priority : "NORMAL")
                .isRead(false)
                .build();

        // Set expiration time based on priority
        if ("LOW".equals(priority)) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));
        } else if ("NORMAL".equals(priority)) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));
        }
        // HIGH and URGENT never expire

        NotificationEntity saved = notificationRepository.save(notification);

        // Broadcast to user via WebSocket
        broadcastToUser(tenantId, userId, saved);

        log.info("Created notification for user {} in tenant {}: {}", userId, tenantId, title);

        return saved;
    }

    /**
     * Create and broadcast notification to all users (broadcast)
     */
    public NotificationEntity createBroadcastNotification(
            String tenantId,
            String type,
            String category,
            String title,
            String message,
            String priority) {

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));

        NotificationEntity notification = NotificationEntity.builder()
                .tenant(tenant)
                .user(null)  // Broadcast to all users
                .notificationType(type)
                .category(category)
                .title(title)
                .message(message)
                .priority(priority != null ? priority : "NORMAL")
                .isRead(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        // Broadcast to all users in tenant via WebSocket
        broadcastToTenant(tenantId, saved);

        log.info("Created broadcast notification in tenant {}: {}", tenantId, title);

        return saved;
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationEntity> getUserNotifications(String tenantId, Long userId) {
        return notificationRepository.findByTenantIdAndUserId(tenantId, userId, LocalDateTime.now());
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationEntity> getUnreadNotifications(String tenantId, Long userId) {
        return notificationRepository.findUnreadByTenantIdAndUserId(tenantId, userId, LocalDateTime.now());
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(String tenantId, Long userId) {
        return notificationRepository.countUnreadByTenantIdAndUserId(tenantId, userId, LocalDateTime.now());
    }

    /**
     * Mark notification as read
     */
    public NotificationEntity markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(String tenantId, Long userId) {
        List<NotificationEntity> unread = getUnreadNotifications(tenantId, userId);
        unread.forEach(NotificationEntity::markAsRead);
        notificationRepository.saveAll(unread);
    }

    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Delete expired notifications (scheduled task)
     */
    @Scheduled(cron = "0 0 2 * * ?")  // Run daily at 2 AM
    public void deleteExpiredNotifications() {
        notificationRepository.deleteExpired(LocalDateTime.now());
        log.info("Deleted expired notifications");
    }

    /**
     * Broadcast notification to specific user via WebSocket
     */
    private void broadcastToUser(String tenantId, Long userId, NotificationEntity notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
        } catch (Exception e) {
            log.error("Failed to broadcast notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Broadcast notification to all users in tenant via WebSocket
     */
    private void broadcastToTenant(String tenantId, NotificationEntity notification) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + tenantId,
                    notification
            );
        } catch (Exception e) {
            log.error("Failed to broadcast notification to tenant {}: {}", tenantId, e.getMessage());
        }
    }

    /**
     * Helper methods for common notification types
     */

    public void notifyQualityFailure(String tenantId, Long userId, Long inspectionId, String productName) {
        createUserNotification(
                tenantId,
                userId,
                "ERROR",
                "QUALITY",
                "품질 검사 불합격",
                String.format("제품 '%s'의 품질 검사가 불합격 처리되었습니다.", productName),
                "QUALITY_INSPECTION",
                inspectionId,
                "HIGH"
        );
    }

    public void notifyInventoryShortage(String tenantId, String productName, int currentStock, int safetyStock) {
        createBroadcastNotification(
                tenantId,
                "WARNING",
                "INVENTORY",
                "재고 부족 경고",
                String.format("제품 '%s'의 재고가 부족합니다. (현재: %d, 안전: %d)", productName, currentStock, safetyStock),
                "HIGH"
        );
    }

    public void notifyEquipmentDowntime(String tenantId, Long userId, Long equipmentId, String equipmentName) {
        createUserNotification(
                tenantId,
                userId,
                "ERROR",
                "EQUIPMENT",
                "설비 다운타임 발생",
                String.format("설비 '%s'에서 다운타임이 발생했습니다.", equipmentName),
                "EQUIPMENT",
                equipmentId,
                "URGENT"
        );
    }

    public void notifyWorkOrderComplete(String tenantId, Long userId, Long workOrderId, String workOrderNo) {
        createUserNotification(
                tenantId,
                userId,
                "SUCCESS",
                "PRODUCTION",
                "작업지시 완료",
                String.format("작업지시 '%s'가 완료되었습니다.", workOrderNo),
                "WORK_ORDER",
                workOrderId,
                "NORMAL"
        );
    }
}
