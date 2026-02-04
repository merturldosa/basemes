package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Alarm Setting Entity
 * 알람 설정 엔티티 (사용자별 알람 수신 설정)
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "alarm_settings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_alarm_setting_user_type",
                columnNames = {"tenant_id", "user_id", "alarm_type"})
    },
    indexes = {
        @Index(name = "idx_alarm_setting_tenant", columnList = "tenant_id"),
        @Index(name = "idx_alarm_setting_user", columnList = "user_id"),
        @Index(name = "idx_alarm_setting_type", columnList = "alarm_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmSettingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Alarm Type
    @Column(name = "alarm_type", nullable = false, length = 50)
    private String alarmType;

    // Channel Settings
    @Column(name = "enable_email")
    @Builder.Default
    private Boolean enableEmail = false;

    @Column(name = "enable_sms")
    @Builder.Default
    private Boolean enableSms = false;

    @Column(name = "enable_push")
    @Builder.Default
    private Boolean enablePush = true;

    @Column(name = "enable_system")
    @Builder.Default
    private Boolean enableSystem = true;

    // Quiet Hours
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(name = "enable_quiet_hours")
    @Builder.Default
    private Boolean enableQuietHours = false;

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Check if email is enabled
     */
    public boolean isEmailEnabled() {
        return enableEmail != null && enableEmail && isActive;
    }

    /**
     * Check if SMS is enabled
     */
    public boolean isSmsEnabled() {
        return enableSms != null && enableSms && isActive;
    }

    /**
     * Check if push is enabled
     */
    public boolean isPushEnabled() {
        return enablePush != null && enablePush && isActive;
    }

    /**
     * Check if system notification is enabled
     */
    public boolean isSystemEnabled() {
        return enableSystem != null && enableSystem && isActive;
    }

    /**
     * Check if quiet hours is enabled
     */
    public boolean isQuietHoursEnabled() {
        return enableQuietHours != null && enableQuietHours &&
                quietHoursStart != null && quietHoursEnd != null;
    }

    /**
     * Check if current time is in quiet hours
     */
    public boolean isInQuietHours() {
        if (!isQuietHoursEnabled()) {
            return false;
        }

        LocalTime now = LocalTime.now();

        // Handle quiet hours crossing midnight
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            // Normal case: 22:00 - 08:00 next day
            return !now.isBefore(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Crossing midnight case: 22:00 - 02:00 (same day representation)
            return !now.isBefore(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }

    /**
     * Check if notification should be sent via any channel
     */
    public boolean shouldSendNotification() {
        if (!isActive) {
            return false;
        }
        if (isInQuietHours()) {
            return false;
        }
        return isEmailEnabled() || isSmsEnabled() || isPushEnabled() || isSystemEnabled();
    }

    /**
     * Get enabled channels
     */
    public java.util.List<String> getEnabledChannels() {
        java.util.List<String> channels = new java.util.ArrayList<>();
        if (isEmailEnabled() && !isInQuietHours()) channels.add("EMAIL");
        if (isSmsEnabled() && !isInQuietHours()) channels.add("SMS");
        if (isPushEnabled() && !isInQuietHours()) channels.add("PUSH");
        if (isSystemEnabled()) channels.add("SYSTEM");
        return channels;
    }
}
