package kr.co.softice.mes.common.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit Log Response DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long auditId;
    private String tenantId;
    private String tenantName;
    private Long userId;
    private String username;
    private String action;
    private String entityType;
    private String entityId;
    private String description;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private String httpMethod;
    private String endpoint;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime createdAt;
    private String metadata;
}
