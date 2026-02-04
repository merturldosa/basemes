package kr.co.softice.mes.common.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Audit Log Search Request DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchRequest {

    /**
     * 사용자명으로 필터링
     */
    private String username;

    /**
     * 작업 유형으로 필터링
     * CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, etc.
     */
    private String action;

    /**
     * 엔티티 타입으로 필터링
     * User, Role, Permission, Order, Product, etc.
     */
    private String entityType;

    /**
     * 엔티티 ID로 필터링
     */
    private String entityId;

    /**
     * 성공/실패로 필터링
     */
    private Boolean success;

    /**
     * IP 주소로 필터링
     */
    private String ipAddress;

    /**
     * 시작 날짜
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    /**
     * 종료 날짜
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    /**
     * 페이지 번호 (0부터 시작)
     */
    @Builder.Default
    private int page = 0;

    /**
     * 페이지 크기
     */
    @Builder.Default
    private int size = 20;

    /**
     * 정렬 필드
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * 정렬 방향 (ASC, DESC)
     */
    @Builder.Default
    private String sortDirection = "DESC";
}
