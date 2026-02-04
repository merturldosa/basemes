package kr.co.softice.mes.api.controller;

import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.config.TenantContext;
import kr.co.softice.mes.domain.entity.NotificationEntity;
import kr.co.softice.mes.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification Controller
 * REST API for notification management
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationEntity>>> getUserNotifications(
            @RequestParam Long userId) {

        String tenantId = TenantContext.getCurrentTenant();
        List<NotificationEntity> notifications = notificationService.getUserNotifications(tenantId, userId);

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationEntity>>> getUnreadNotifications(
            @RequestParam Long userId) {

        String tenantId = TenantContext.getCurrentTenant();
        List<NotificationEntity> notifications = notificationService.getUnreadNotifications(tenantId, userId);

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestParam Long userId) {

        String tenantId = TenantContext.getCurrentTenant();
        long count = notificationService.getUnreadCount(tenantId, userId);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationEntity>> markAsRead(
            @PathVariable Long id) {

        NotificationEntity notification = notificationService.markAsRead(id);

        return ResponseEntity.ok(ApiResponse.success(notification));
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestParam Long userId) {

        String tenantId = TenantContext.getCurrentTenant();
        notificationService.markAllAsRead(tenantId, userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id) {

        notificationService.deleteNotification(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
