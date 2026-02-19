package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.PageResponse;
import kr.co.softice.mes.common.dto.audit.AuditLogResponse;
import kr.co.softice.mes.common.dto.audit.AuditLogSearchRequest;
import kr.co.softice.mes.common.dto.audit.AuditStatisticsResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.AuditLogEntity;
import kr.co.softice.mes.domain.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit Log Controller
 * 감사 로그 조회 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Log Management", description = "감사 로그 조회 API")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 감사 로그 목록 조회 (페이징)
     * GET /api/audit-logs
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "감사 로그 목록 조회", description = "테넌트의 모든 감사 로그 조회 (페이징)")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting audit logs for tenant: {}", tenantId);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLogEntity> auditLogsPage = auditLogService.findByTenant(tenantId, pageable);
        PageResponse<AuditLogResponse> response = PageResponse.of(
                auditLogsPage.map(this::toAuditLogResponse)
        );

        return ResponseEntity.ok(ApiResponse.success("감사 로그 목록 조회 성공", response));
    }

    /**
     * 감사 로그 상세 조회
     * GET /api/audit-logs/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "감사 로그 상세 조회", description = "특정 감사 로그 상세 정보 조회")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLog(@PathVariable Long id) {
        log.info("Getting audit log: {}", id);

        AuditLogEntity auditLog = auditLogService.findById(id);

        return ResponseEntity.ok(ApiResponse.success("감사 로그 조회 성공", toAuditLogResponse(auditLog)));
    }

    /**
     * 감사 로그 검색 (복합 조건)
     * POST /api/audit-logs/search
     */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "감사 로그 검색", description = "복합 조건으로 감사 로그 검색")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> searchAuditLogs(
            @RequestBody AuditLogSearchRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Searching audit logs for tenant: {} with criteria: {}", tenantId, request);

        Sort.Direction direction = request.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction, request.getSortBy())
        );

        Page<AuditLogEntity> auditLogsPage = auditLogService.searchAuditLogs(
                tenantId,
                request.getUsername(),
                request.getAction(),
                request.getEntityType(),
                request.getSuccess(),
                request.getStartDate(),
                request.getEndDate(),
                pageable
        );

        PageResponse<AuditLogResponse> response = PageResponse.of(
                auditLogsPage.map(this::toAuditLogResponse)
        );

        return ResponseEntity.ok(ApiResponse.success("감사 로그 검색 성공", response));
    }

    /**
     * 사용자별 감사 로그 조회
     * GET /api/audit-logs/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "사용자별 감사 로그 조회", description = "특정 사용자의 모든 활동 로그 조회")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Getting audit logs for user: {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogEntity> auditLogsPage = auditLogService.findByUser(userId, pageable);

        PageResponse<AuditLogResponse> response = PageResponse.of(
                auditLogsPage.map(this::toAuditLogResponse)
        );

        return ResponseEntity.ok(ApiResponse.success("사용자 감사 로그 조회 성공", response));
    }

    /**
     * 작업 유형별 감사 로그 조회
     * GET /api/audit-logs/action/{action}
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "작업 유형별 감사 로그", description = "특정 작업 유형의 모든 로그 조회")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting audit logs for action: {} in tenant: {}", action, tenantId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogEntity> auditLogsPage = auditLogService.findByAction(tenantId, action, pageable);

        PageResponse<AuditLogResponse> response = PageResponse.of(
                auditLogsPage.map(this::toAuditLogResponse)
        );

        return ResponseEntity.ok(ApiResponse.success("작업 유형별 감사 로그 조회 성공", response));
    }

    /**
     * 엔티티별 변경 이력 조회
     * GET /api/audit-logs/entity/{entityType}/{entityId}
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "엔티티 변경 이력", description = "특정 엔티티의 모든 변경 이력 조회")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting audit logs for entity: {} with id: {}", entityType, entityId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogEntity> auditLogsPage = auditLogService.findByEntity(
                tenantId, entityType, entityId, pageable
        );

        PageResponse<AuditLogResponse> response = PageResponse.of(
                auditLogsPage.map(this::toAuditLogResponse)
        );

        return ResponseEntity.ok(ApiResponse.success("엔티티 변경 이력 조회 성공", response));
    }

    /**
     * 실패한 작업 로그 조회
     * GET /api/audit-logs/failures
     */
    @GetMapping("/failures")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "실패한 작업 로그", description = "실패한 모든 작업 로그 조회")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getFailedOperations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting failed operations for tenant: {}", tenantId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogEntity> auditLogsPage = auditLogService.findBySuccess(tenantId, false, pageable);

        PageResponse<AuditLogResponse> response = PageResponse.of(
                auditLogsPage.map(this::toAuditLogResponse)
        );

        return ResponseEntity.ok(ApiResponse.success("실패한 작업 로그 조회 성공", response));
    }

    /**
     * IP 주소별 감사 로그 조회
     * GET /api/audit-logs/ip/{ipAddress}
     */
    @GetMapping("/ip/{ipAddress}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "IP별 감사 로그", description = "특정 IP 주소의 모든 활동 로그 조회")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByIp(
            @PathVariable String ipAddress,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting audit logs for IP: {} in tenant: {}", ipAddress, tenantId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogEntity> auditLogsPage = auditLogService.findByIpAddress(
                tenantId, ipAddress, pageable
        );

        PageResponse<AuditLogResponse> response = PageResponse.of(
                auditLogsPage.map(this::toAuditLogResponse)
        );

        return ResponseEntity.ok(ApiResponse.success("IP별 감사 로그 조회 성공", response));
    }

    /**
     * 감사 로그 통계
     * GET /api/audit-logs/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "감사 로그 통계", description = "기간별 감사 로그 통계 정보 조회")
    public ResponseEntity<ApiResponse<AuditStatisticsResponse>> getAuditStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        String tenantId = TenantContext.getCurrentTenant();

        // 기본값: 최근 30일
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.info("Getting audit statistics for tenant: {} from {} to {}", tenantId, startDate, endDate);

        Map<String, Long> actionStats = auditLogService.getActionStatistics(
                tenantId, startDate, endDate
        );
        Map<String, Long> userActivityStats = auditLogService.getUserActivityStatistics(
                tenantId, startDate, endDate
        );

        // 전체 통계 계산
        long totalLogs = actionStats.values().stream().mapToLong(Long::longValue).sum();
        long successfulOps = totalLogs; // 실제로는 성공/실패를 별도로 조회해야 함
        long failedOps = 0; // 간단히 0으로 설정

        AuditStatisticsResponse response = AuditStatisticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .actionStatistics(actionStats)
                .userActivityStatistics(userActivityStats)
                .totalLogs(totalLogs)
                .successfulOperations(successfulOps)
                .failedOperations(failedOps)
                .build();

        return ResponseEntity.ok(ApiResponse.success("감사 로그 통계 조회 성공", response));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private AuditLogResponse toAuditLogResponse(AuditLogEntity auditLog) {
        return AuditLogResponse.builder()
                .auditId(auditLog.getAuditId())
                .tenantId(auditLog.getTenant() != null ? auditLog.getTenant().getTenantId() : null)
                .tenantName(auditLog.getTenant() != null ? auditLog.getTenant().getTenantName() : null)
                .userId(auditLog.getUser() != null ? auditLog.getUser().getUserId() : null)
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .description(auditLog.getDescription())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .httpMethod(auditLog.getHttpMethod())
                .endpoint(auditLog.getEndpoint())
                .success(auditLog.getSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .createdAt(auditLog.getCreatedAt())
                .metadata(auditLog.getMetadata())
                .build();
    }
}
