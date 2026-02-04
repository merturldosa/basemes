package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.AlarmHistoryEntity;
import kr.co.softice.mes.domain.entity.AlarmTemplateEntity;
import kr.co.softice.mes.domain.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Alarm Controller
 * 알람 관리 컨트롤러
 *
 * @author Moon Myung-seop
 */
@Tag(name = "Alarm", description = "알람 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    // ==================== Template Management ====================

    /**
     * Get all alarm templates
     */
    @Operation(summary = "알람 템플릿 목록 조회")
    @GetMapping("/templates")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<AlarmTemplateEntity>>> getAllTemplates(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            List<AlarmTemplateEntity> templates = alarmService.findAllTemplates(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<List<AlarmTemplateEntity>>builder()
                            .success(true)
                            .data(templates)
                            .message("알람 템플릿 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get alarm templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<AlarmTemplateEntity>>builder()
                            .success(false)
                            .message("알람 템플릿 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    // ==================== Alarm History Management ====================

    /**
     * Get alarms for user
     */
    @Operation(summary = "사용자 알람 목록 조회")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<AlarmHistoryEntity>>> getAlarms(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        try {
            List<AlarmHistoryEntity> alarms = alarmService.findAlarmsByRecipient(tenantId, userId);
            return ResponseEntity.ok(
                    ApiResponse.<List<AlarmHistoryEntity>>builder()
                            .success(true)
                            .data(alarms)
                            .message("알람 목록 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get alarms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<AlarmHistoryEntity>>builder()
                            .success(false)
                            .message("알람 목록 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get unread alarms
     */
    @Operation(summary = "읽지 않은 알람 조회")
    @GetMapping("/unread")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<AlarmHistoryEntity>>> getUnreadAlarms(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        try {
            List<AlarmHistoryEntity> alarms = alarmService.findUnreadAlarms(tenantId, userId);
            return ResponseEntity.ok(
                    ApiResponse.<List<AlarmHistoryEntity>>builder()
                            .success(true)
                            .data(alarms)
                            .message("읽지 않은 알람 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get unread alarms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<AlarmHistoryEntity>>builder()
                            .success(false)
                            .message("읽지 않은 알람 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get recent alarms (last 7 days)
     */
    @Operation(summary = "최근 알람 조회 (7일)")
    @GetMapping("/recent")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<AlarmHistoryEntity>>> getRecentAlarms(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        try {
            List<AlarmHistoryEntity> alarms = alarmService.findRecentAlarms(tenantId, userId);
            return ResponseEntity.ok(
                    ApiResponse.<List<AlarmHistoryEntity>>builder()
                            .success(true)
                            .data(alarms)
                            .message("최근 알람 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get recent alarms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<AlarmHistoryEntity>>builder()
                            .success(false)
                            .message("최근 알람 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Count unread alarms
     */
    @Operation(summary = "읽지 않은 알람 수 조회")
    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Long>> countUnreadAlarms(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        try {
            Long count = alarmService.countUnreadAlarms(tenantId, userId);
            return ResponseEntity.ok(
                    ApiResponse.<Long>builder()
                            .success(true)
                            .data(count)
                            .message("읽지 않은 알람 수 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to count unread alarms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Long>builder()
                            .success(false)
                            .message("읽지 않은 알람 수 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Mark alarm as read
     */
    @Operation(summary = "알람 읽음 표시")
    @PutMapping("/{alarmId}/read")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @Parameter(description = "알람 ID", required = true)
            @PathVariable Long alarmId) {
        try {
            alarmService.markAsRead(alarmId);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("알람 읽음 처리 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to mark alarm as read", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("알람 읽음 처리 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Mark all alarms as read
     */
    @Operation(summary = "모든 알람 읽음 표시")
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        try {
            alarmService.markAllAsRead(tenantId, userId);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("모든 알람 읽음 처리 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to mark all alarms as read", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("모든 알람 읽음 처리 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get alarm statistics
     */
    @Operation(summary = "알람 통계 조회")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<AlarmService.AlarmStatistics>> getStatistics(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        try {
            AlarmService.AlarmStatistics stats = alarmService.getStatistics(tenantId, userId);
            return ResponseEntity.ok(
                    ApiResponse.<AlarmService.AlarmStatistics>builder()
                            .success(true)
                            .data(stats)
                            .message("알람 통계 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get alarm statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<AlarmService.AlarmStatistics>builder()
                            .success(false)
                            .message("알람 통계 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }
}
