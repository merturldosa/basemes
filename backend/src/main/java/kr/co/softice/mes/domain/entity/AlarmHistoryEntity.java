package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Alarm History Entity
 * 알람 이력 엔티티 (발송된 알람 기록)
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_alarm_history",
    indexes = {
        @Index(name = "idx_sd_alarm_history_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_alarm_history_recipient", columnList = "recipient_user_id"),
        @Index(name = "idx_sd_alarm_history_type", columnList = "alarm_type"),
        @Index(name = "idx_sd_alarm_history_reference", columnList = "reference_type, reference_id"),
        @Index(name = "idx_sd_alarm_history_created", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long alarmId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Template
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private AlarmTemplateEntity template;

    // Recipient
    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_email", length = 200)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 50)
    private String recipientPhone;

    // Alarm Info
    @Column(name = "alarm_type", nullable = false, length = 50)
    private String alarmType;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "NORMAL";

    // Content
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    // Reference
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    // Channels
    @Column(name = "sent_via_email")
    @Builder.Default
    private Boolean sentViaEmail = false;

    @Column(name = "sent_via_sms")
    @Builder.Default
    private Boolean sentViaSms = false;

    @Column(name = "sent_via_push")
    @Builder.Default
    private Boolean sentViaPush = false;

    @Column(name = "sent_via_system")
    @Builder.Default
    private Boolean sentViaSystem = false;

    // Status
    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Send Status
    @Column(name = "send_status", length = 20)
    @Builder.Default
    private String sendStatus = "PENDING";  // PENDING, SENT, FAILED

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failed_reason", columnDefinition = "TEXT")
    private String failedReason;

    /**
     * Check if alarm is read
     */
    public boolean isRead() {
        return isRead != null && isRead;
    }

    /**
     * Check if alarm is sent
     */
    public boolean isSent() {
        return "SENT".equals(sendStatus);
    }

    /**
     * Check if send failed
     */
    public boolean isFailed() {
        return "FAILED".equals(sendStatus);
    }

    /**
     * Check if pending
     */
    public boolean isPending() {
        return "PENDING".equals(sendStatus);
    }

    /**
     * Check if urgent
     */
    public boolean isUrgent() {
        return "URGENT".equals(priority);
    }

    /**
     * Check if high priority
     */
    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "URGENT".equals(priority);
    }

    /**
     * Mark as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Mark as sent
     */
    public void markAsSent() {
        this.sendStatus = "SENT";
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String reason) {
        this.sendStatus = "FAILED";
        this.failedReason = reason;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Get hours since created
     */
    public long getHoursSinceCreated() {
        if (getCreatedAt() == null) {
            return 0;
        }
        return java.time.Duration.between(getCreatedAt(), LocalDateTime.now()).toHours();
    }

    /**
     * Check if alarm is recent (within 24 hours)
     */
    public boolean isRecent() {
        return getHoursSinceCreated() < 24;
    }

    /**
     * Get sent channels as list
     */
    public java.util.List<String> getSentChannels() {
        java.util.List<String> channels = new java.util.ArrayList<>();
        if (sentViaEmail != null && sentViaEmail) channels.add("EMAIL");
        if (sentViaSms != null && sentViaSms) channels.add("SMS");
        if (sentViaPush != null && sentViaPush) channels.add("PUSH");
        if (sentViaSystem != null && sentViaSystem) channels.add("SYSTEM");
        return channels;
    }

    /**
     * Check if sent via any channel
     */
    public boolean isSentViaAnyChannel() {
        return (sentViaEmail != null && sentViaEmail) ||
               (sentViaSms != null && sentViaSms) ||
               (sentViaPush != null && sentViaPush) ||
               (sentViaSystem != null && sentViaSystem);
    }
}
