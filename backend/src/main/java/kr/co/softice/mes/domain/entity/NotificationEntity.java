package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification Entity
 * Stores notification history for users
 * @author Moon Myung-seop
 */
@Entity
@Table(name = "si_notifications", schema = "common")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;  // null = broadcast to all users

    @Column(name = "notification_type", length = 50, nullable = false)
    private String notificationType;  // INFO, WARNING, ERROR, SUCCESS

    @Column(name = "category", length = 50)
    private String category;  // PRODUCTION, QUALITY, INVENTORY, EQUIPMENT, SYSTEM

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "reference_type", length = 50)
    private String referenceType;  // WORK_ORDER, QUALITY_INSPECTION, INVENTORY, etc.

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_url", length = 500)
    private String referenceUrl;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "priority", length = 20)
    private String priority = "NORMAL";  // LOW, NORMAL, HIGH, URGENT

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Check if notification is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
