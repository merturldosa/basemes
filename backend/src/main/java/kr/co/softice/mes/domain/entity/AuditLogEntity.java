package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Audit Log Entity
 * 시스템 작업 감사 로그
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(name = "SD_AuditLogs", indexes = {
        @Index(name = "idx_audit_tenant", columnList = "tenant_id"),
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    /**
     * 테넌트 (다중 테넌트 지원)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    /**
     * 작업 수행 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     * 사용자명 (스냅샷 - 사용자 삭제되어도 기록 유지)
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * 작업 유형
     * CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, etc.
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * 작업 대상 엔티티 타입
     * User, Role, Permission, Order, Product, etc.
     */
    @Column(name = "entity_type", length = 100)
    private String entityType;

    /**
     * 작업 대상 엔티티 ID
     */
    @Column(name = "entity_id", length = 100)
    private String entityId;

    /**
     * 작업 설명
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * 변경 전 데이터 (JSON)
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * 변경 후 데이터 (JSON)
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * 클라이언트 IP 주소
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User Agent (브라우저 정보)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 요청 메서드 (GET, POST, PUT, DELETE)
     */
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    /**
     * 요청 엔드포인트
     */
    @Column(name = "endpoint", length = 500)
    private String endpoint;

    /**
     * 작업 성공 여부
     */
    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;

    /**
     * 오류 메시지 (실패 시)
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 작업 수행 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 추가 메타데이터 (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
