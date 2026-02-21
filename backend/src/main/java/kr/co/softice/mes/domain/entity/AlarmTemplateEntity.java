package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Alarm Template Entity
 * 알람 템플릿 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_alarm_templates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_alarm_template_code", columnNames = {"tenant_id", "template_code"})
    },
    indexes = {
        @Index(name = "idx_sd_alarm_template_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_alarm_template_type", columnList = "alarm_type"),
        @Index(name = "idx_sd_alarm_template_event", columnList = "event_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmTemplateEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    // Alarm Type
    @Column(name = "alarm_type", nullable = false, length = 50)
    private String alarmType;  // SYSTEM, APPROVAL, QUALITY, PRODUCTION, INVENTORY, DELIVERY

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;  // APPROVAL_REQUEST, QUALITY_FAILED, STOCK_LOW, etc.

    // Message Template
    @Column(name = "title_template", nullable = false, columnDefinition = "TEXT")
    private String titleTemplate;

    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

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

    // Priority
    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "NORMAL";  // LOW, NORMAL, HIGH, URGENT

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Check if email is enabled
     */
    public boolean isEmailEnabled() {
        return enableEmail != null && enableEmail;
    }

    /**
     * Check if SMS is enabled
     */
    public boolean isSmsEnabled() {
        return enableSms != null && enableSms;
    }

    /**
     * Check if push is enabled
     */
    public boolean isPushEnabled() {
        return enablePush != null && enablePush;
    }

    /**
     * Check if system notification is enabled
     */
    public boolean isSystemEnabled() {
        return enableSystem != null && enableSystem;
    }

    /**
     * Check if priority is urgent
     */
    public boolean isUrgent() {
        return "URGENT".equals(priority);
    }

    /**
     * Check if priority is high
     */
    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "URGENT".equals(priority);
    }

    /**
     * Replace template variables with actual values
     */
    public String renderTitle(java.util.Map<String, String> variables) {
        String result = titleTemplate;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * Replace template variables with actual values
     */
    public String renderMessage(java.util.Map<String, String> variables) {
        String result = messageTemplate;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
