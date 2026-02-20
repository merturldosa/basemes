package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.AlarmHistoryEntity;
import kr.co.softice.mes.domain.entity.AlarmTemplateEntity;
import kr.co.softice.mes.domain.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Alarm Controller
 * 알람 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
@Tag(name = "Alarm", description = "알람 관리 API")
public class AlarmController {

    private final AlarmService alarmService;

    // ==================== Templates ====================

    @Transactional(readOnly = true)
    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Operation(summary = "알람 템플릿 목록 조회", description = "모든 알람 템플릿을 조회합니다.")
    public ResponseEntity<List<AlarmTemplateEntity>> getAllTemplates() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all alarm templates for tenant: {}", tenantId);
        List<AlarmTemplateEntity> templates = alarmService.findAllTemplates(tenantId);
        return ResponseEntity.ok(templates);
    }

    @Transactional(readOnly = true)
    @GetMapping("/templates/{eventType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Operation(summary = "이벤트 유형별 템플릿 조회", description = "이벤트 유형으로 알람 템플릿을 조회합니다.")
    public ResponseEntity<AlarmTemplateEntity> getTemplateByEventType(@PathVariable String eventType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting alarm template for event type: {} in tenant: {}", eventType, tenantId);
        return alarmService.findTemplateByEventType(tenantId, eventType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Alarm History ====================

    @Transactional(readOnly = true)
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자 알람 목록 조회", description = "특정 사용자의 알람을 조회합니다.")
    public ResponseEntity<List<AlarmHistoryEntity>> getAlarmsByUser(@PathVariable Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting alarms for user: {} in tenant: {}", userId, tenantId);
        List<AlarmHistoryEntity> alarms = alarmService.findAlarmsByRecipient(tenantId, userId);
        return ResponseEntity.ok(alarms);
    }

    @Transactional(readOnly = true)
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "읽지 않은 알람 조회", description = "사용자의 읽지 않은 알람을 조회합니다.")
    public ResponseEntity<List<AlarmHistoryEntity>> getUnreadAlarms(@PathVariable Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting unread alarms for user: {} in tenant: {}", userId, tenantId);
        List<AlarmHistoryEntity> alarms = alarmService.findUnreadAlarms(tenantId, userId);
        return ResponseEntity.ok(alarms);
    }

    @Transactional(readOnly = true)
    @GetMapping("/user/{userId}/recent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "최근 알람 조회", description = "사용자의 최근 7일 알람을 조회합니다.")
    public ResponseEntity<List<AlarmHistoryEntity>> getRecentAlarms(@PathVariable Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting recent alarms for user: {} in tenant: {}", userId, tenantId);
        List<AlarmHistoryEntity> alarms = alarmService.findRecentAlarms(tenantId, userId);
        return ResponseEntity.ok(alarms);
    }

    @Transactional(readOnly = true)
    @GetMapping("/user/{userId}/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "읽지 않은 알람 수 조회", description = "사용자의 읽지 않은 알람 수를 조회합니다.")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        Long count = alarmService.countUnreadAlarms(tenantId, userId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{alarmId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "알람 읽음 처리", description = "알람을 읽음 상태로 변경합니다.")
    public ResponseEntity<Void> markAsRead(@PathVariable Long alarmId) {
        log.info("Marking alarm as read: {}", alarmId);
        alarmService.markAsRead(alarmId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/user/{userId}/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "전체 읽음 처리", description = "사용자의 모든 알람을 읽음 상태로 변경합니다.")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Marking all alarms as read for user: {} in tenant: {}", userId, tenantId);
        alarmService.markAllAsRead(tenantId, userId);
        return ResponseEntity.ok().build();
    }

    // ==================== Alarm Sending ====================

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Operation(summary = "알람 발송", description = "알람을 발송합니다.")
    public ResponseEntity<AlarmHistoryEntity> sendAlarm(@RequestBody Map<String, Object> request) {
        String tenantId = TenantContext.getCurrentTenant();
        String eventType = (String) request.get("eventType");
        Long recipientUserId = Long.valueOf(request.get("recipientUserId").toString());
        String recipientName = (String) request.get("recipientName");
        @SuppressWarnings("unchecked")
        Map<String, String> variables = (Map<String, String>) request.get("variables");
        String referenceType = (String) request.get("referenceType");
        Long referenceId = request.get("referenceId") != null ? Long.valueOf(request.get("referenceId").toString()) : null;
        String referenceNo = (String) request.get("referenceNo");

        log.info("Sending alarm: eventType={}, recipient={}", eventType, recipientUserId);
        AlarmHistoryEntity alarm = alarmService.sendAlarm(
                tenantId, eventType, recipientUserId, recipientName,
                variables, referenceType, referenceId, referenceNo);

        if (alarm == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(alarm);
    }

    // ==================== Statistics ====================

    @Transactional(readOnly = true)
    @GetMapping("/statistics/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "알람 통계 조회", description = "사용자의 알람 통계를 조회합니다.")
    public ResponseEntity<AlarmService.AlarmStatistics> getStatistics(@PathVariable Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting alarm statistics for user: {} in tenant: {}", userId, tenantId);
        AlarmService.AlarmStatistics stats = alarmService.getStatistics(tenantId, userId);
        return ResponseEntity.ok(stats);
    }

    // ==================== Maintenance ====================

    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "오래된 알람 삭제", description = "보존 기간이 지난 알람을 삭제합니다.")
    public ResponseEntity<Void> deleteOldAlarms(@RequestParam(defaultValue = "90") int retentionDays) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Cleaning up alarms older than {} days for tenant: {}", retentionDays, tenantId);
        alarmService.deleteOldAlarms(tenantId, retentionDays);
        return ResponseEntity.ok().build();
    }
}
