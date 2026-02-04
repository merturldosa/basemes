package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.AlarmHistoryEntity;
import kr.co.softice.mes.domain.entity.AlarmTemplateEntity;
import kr.co.softice.mes.domain.repository.AlarmHistoryRepository;
import kr.co.softice.mes.domain.repository.AlarmTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Alarm Service
 * 알람 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmTemplateRepository templateRepository;
    private final AlarmHistoryRepository historyRepository;

    // ==================== Template Management ====================

    /**
     * Find all templates
     */
    @Transactional(readOnly = true)
    public List<AlarmTemplateEntity> findAllTemplates(String tenantId) {
        return templateRepository.findAllByTenantId(tenantId);
    }

    /**
     * Find template by event type
     */
    @Transactional(readOnly = true)
    public Optional<AlarmTemplateEntity> findTemplateByEventType(String tenantId, String eventType) {
        return templateRepository.findByTenantIdAndEventType(tenantId, eventType);
    }

    // ==================== Alarm Sending ====================

    /**
     * Send alarm
     */
    @Transactional
    public AlarmHistoryEntity sendAlarm(
            String tenantId,
            String eventType,
            Long recipientUserId,
            String recipientName,
            Map<String, String> variables,
            String referenceType,
            Long referenceId,
            String referenceNo
    ) {
        log.info("Sending alarm: eventType={}, recipient={}", eventType, recipientUserId);

        // Find template
        Optional<AlarmTemplateEntity> templateOpt = findTemplateByEventType(tenantId, eventType);
        if (!templateOpt.isPresent()) {
            log.warn("No template found for event type: {}", eventType);
            return null;
        }

        AlarmTemplateEntity template = templateOpt.get();

        // Render message
        String title = template.renderTitle(variables);
        String message = template.renderMessage(variables);

        // Create alarm history
        AlarmHistoryEntity alarm = AlarmHistoryEntity.builder()
                .recipientUserId(recipientUserId)
                .recipientName(recipientName)
                .alarmType(template.getAlarmType())
                .eventType(template.getEventType())
                .priority(template.getPriority())
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .referenceNo(referenceNo)
                .sentViaEmail(template.isEmailEnabled())
                .sentViaSms(template.isSmsEnabled())
                .sentViaPush(template.isPushEnabled())
                .sentViaSystem(template.isSystemEnabled())
                .sendStatus("PENDING")
                .build();

        alarm.setTenant(template.getTenant());
        alarm.setTemplate(template);

        AlarmHistoryEntity saved = historyRepository.save(alarm);

        // Send via channels (in real implementation, integrate with email/SMS/push services)
        sendViaChannels(saved, template);

        return saved;
    }

    /**
     * Send via channels
     */
    private void sendViaChannels(AlarmHistoryEntity alarm, AlarmTemplateEntity template) {
        try {
            // TODO: Integrate with actual email/SMS/push services
            // For now, just mark as sent

            if (template.isEmailEnabled()) {
                sendViaEmail(alarm);
            }

            if (template.isSmsEnabled()) {
                sendViaSms(alarm);
            }

            if (template.isPushEnabled()) {
                sendViaPush(alarm);
            }

            // System notification is always sent
            alarm.markAsSent();
            historyRepository.save(alarm);

        } catch (Exception e) {
            log.error("Failed to send alarm", e);
            alarm.markAsFailed(e.getMessage());
            historyRepository.save(alarm);
        }
    }

    /**
     * Send via email
     */
    private void sendViaEmail(AlarmHistoryEntity alarm) {
        // TODO: Integrate with email service
        log.info("Sending email to: {}", alarm.getRecipientEmail());
    }

    /**
     * Send via SMS
     */
    private void sendViaSms(AlarmHistoryEntity alarm) {
        // TODO: Integrate with SMS service
        log.info("Sending SMS to: {}", alarm.getRecipientPhone());
    }

    /**
     * Send via push notification
     */
    private void sendViaPush(AlarmHistoryEntity alarm) {
        // TODO: Integrate with push notification service
        log.info("Sending push notification to user: {}", alarm.getRecipientUserId());
    }

    // ==================== Alarm History Management ====================

    /**
     * Find alarms by recipient
     */
    @Transactional(readOnly = true)
    public List<AlarmHistoryEntity> findAlarmsByRecipient(String tenantId, Long userId) {
        return historyRepository.findByRecipient(tenantId, userId);
    }

    /**
     * Find unread alarms by recipient
     */
    @Transactional(readOnly = true)
    public List<AlarmHistoryEntity> findUnreadAlarms(String tenantId, Long userId) {
        return historyRepository.findUnreadByRecipient(tenantId, userId);
    }

    /**
     * Find recent alarms (last 7 days)
     */
    @Transactional(readOnly = true)
    public List<AlarmHistoryEntity> findRecentAlarms(String tenantId, Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return historyRepository.findRecentByRecipient(tenantId, userId, since);
    }

    /**
     * Count unread alarms
     */
    @Transactional(readOnly = true)
    public Long countUnreadAlarms(String tenantId, Long userId) {
        return historyRepository.countUnreadByRecipient(tenantId, userId);
    }

    /**
     * Mark alarm as read
     */
    @Transactional
    public void markAsRead(Long alarmId) {
        Optional<AlarmHistoryEntity> alarmOpt = historyRepository.findById(alarmId);
        if (alarmOpt.isPresent()) {
            AlarmHistoryEntity alarm = alarmOpt.get();
            alarm.markAsRead();
            historyRepository.save(alarm);
        }
    }

    /**
     * Mark all alarms as read for user
     */
    @Transactional
    public void markAllAsRead(String tenantId, Long userId) {
        List<AlarmHistoryEntity> unreadAlarms = findUnreadAlarms(tenantId, userId);
        for (AlarmHistoryEntity alarm : unreadAlarms) {
            alarm.markAsRead();
            historyRepository.save(alarm);
        }
    }

    /**
     * Delete old alarms (retention policy)
     */
    @Transactional
    public void deleteOldAlarms(String tenantId, int retentionDays) {
        LocalDateTime before = LocalDateTime.now().minusDays(retentionDays);
        historyRepository.deleteOldAlarms(tenantId, before);
        log.info("Deleted alarms older than {} days for tenant: {}", retentionDays, tenantId);
    }

    // ==================== Statistics ====================

    /**
     * Get alarm statistics
     */
    @Transactional(readOnly = true)
    public AlarmStatistics getStatistics(String tenantId, Long userId) {
        Long unread = countUnreadAlarms(tenantId, userId);
        Long total = (long) findAlarmsByRecipient(tenantId, userId).size();

        Long approvals = historyRepository.countByTypeAndRecipient(tenantId, userId, "APPROVAL");
        Long quality = historyRepository.countByTypeAndRecipient(tenantId, userId, "QUALITY");
        Long production = historyRepository.countByTypeAndRecipient(tenantId, userId, "PRODUCTION");
        Long inventory = historyRepository.countByTypeAndRecipient(tenantId, userId, "INVENTORY");

        return new AlarmStatistics(unread, total, approvals, quality, production, inventory);
    }

    /**
     * Alarm statistics inner class
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class AlarmStatistics {
        private Long unreadCount;
        private Long totalCount;
        private Long approvalCount;
        private Long qualityCount;
        private Long productionCount;
        private Long inventoryCount;

        public Long getReadCount() {
            return totalCount - unreadCount;
        }

        public double getReadRate() {
            if (totalCount == 0) return 0.0;
            return (double) getReadCount() / totalCount * 100;
        }
    }
}
