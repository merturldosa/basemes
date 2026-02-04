package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.domain.entity.AuditLogEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Audit Log Service Test
 * 감사 로그 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("감사 로그 서비스 테스트")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private TenantEntity testTenant;
    private AuditLogEntity testAuditLog;
    private String tenantId;
    private Long userId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";
        userId = 123L;
        pageable = PageRequest.of(0, 10);

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);

        testAuditLog = AuditLogEntity.builder()
                .username("john.doe")
                .action("CREATE")
                .entityType("WorkOrder")
                .entityId("WO001")
                .ipAddress("192.168.1.100")
                .success(true)
                .build();
        testAuditLog.setAuditId(1L);
        testAuditLog.setTenant(testTenant);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("ID로 감사 로그 조회 - 성공")
    void testFindById_Success() {
        when(auditLogRepository.findById(1L))
                .thenReturn(Optional.of(testAuditLog));

        AuditLogEntity result = auditLogService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAction()).isEqualTo("CREATE");
    }

    @Test
    @DisplayName("ID로 감사 로그 조회 - 실패 (존재하지 않음)")
    void testFindById_Fail_NotFound() {
        when(auditLogRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.findById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("테넌트별 감사 로그 조회 - 성공")
    void testFindByTenant_Success() {
        Page<AuditLogEntity> page = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findByTenantId(tenantId, pageable))
                .thenReturn(page);

        Page<AuditLogEntity> result = auditLogService.findByTenant(tenantId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo("CREATE");
    }

    @Test
    @DisplayName("사용자별 감사 로그 조회 - 성공")
    void testFindByUser_Success() {
        Page<AuditLogEntity> page = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findByUserId(userId, pageable))
                .thenReturn(page);

        Page<AuditLogEntity> result = auditLogService.findByUser(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("작업 유형별 감사 로그 조회 - 성공")
    void testFindByAction_Success() {
        Page<AuditLogEntity> page = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findByTenantIdAndAction(tenantId, "CREATE", pageable))
                .thenReturn(page);

        Page<AuditLogEntity> result = auditLogService.findByAction(tenantId, "CREATE", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo("CREATE");
    }

    @Test
    @DisplayName("엔티티별 감사 로그 조회 - 성공")
    void testFindByEntity_Success() {
        Page<AuditLogEntity> page = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findByTenantIdAndEntityTypeAndEntityId(
                tenantId, "WorkOrder", "WO001", pageable))
                .thenReturn(page);

        Page<AuditLogEntity> result = auditLogService.findByEntity(
                tenantId, "WorkOrder", "WO001", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("기간별 감사 로그 조회 - 성공")
    void testFindByDateRange_Success() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        Page<AuditLogEntity> page = new PageImpl<>(Arrays.asList(testAuditLog));

        when(auditLogRepository.findByTenantIdAndDateRange(tenantId, start, end, pageable))
                .thenReturn(page);

        Page<AuditLogEntity> result = auditLogService.findByDateRange(
                tenantId, start, end, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("성공/실패별 감사 로그 조회 - 성공")
    void testFindBySuccess_Success() {
        Page<AuditLogEntity> page = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findByTenantIdAndSuccess(tenantId, true, pageable))
                .thenReturn(page);

        Page<AuditLogEntity> result = auditLogService.findBySuccess(tenantId, true, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSuccess()).isTrue();
    }

    @Test
    @DisplayName("IP 주소별 감사 로그 조회 - 성공")
    void testFindByIpAddress_Success() {
        Page<AuditLogEntity> page = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findByTenantIdAndIpAddress(tenantId, "192.168.1.100", pageable))
                .thenReturn(page);

        Page<AuditLogEntity> result = auditLogService.findByIpAddress(
                tenantId, "192.168.1.100", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // === 복합 검색 테스트 ===

    @Test
    @DisplayName("복합 조건 검색 - 성공")
    void testSearchAuditLogs_Success() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        Page<AuditLogEntity> page = new PageImpl<>(Arrays.asList(testAuditLog));

        when(auditLogRepository.searchAuditLogs(
                tenantId, "john.doe", "CREATE", "WorkOrder", true, start, end, pageable))
                .thenReturn(page);

        Page<AuditLogEntity> result = auditLogService.searchAuditLogs(
                tenantId, "john.doe", "CREATE", "WorkOrder", true, start, end, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // === 통계 테스트 ===

    @Test
    @DisplayName("작업 유형별 통계 - 성공")
    void testGetActionStatistics_Success() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<Object[]> results = Arrays.asList(
                new Object[]{"CREATE", 10L},
                new Object[]{"UPDATE", 5L},
                new Object[]{"DELETE", 2L}
        );

        when(auditLogRepository.countByActionAndDateRange(tenantId, start, end))
                .thenReturn(results);

        Map<String, Long> statistics = auditLogService.getActionStatistics(
                tenantId, start, end);

        assertThat(statistics).hasSize(3);
        assertThat(statistics.get("CREATE")).isEqualTo(10L);
        assertThat(statistics.get("UPDATE")).isEqualTo(5L);
        assertThat(statistics.get("DELETE")).isEqualTo(2L);
    }

    @Test
    @DisplayName("사용자 활동 통계 - 성공")
    void testGetUserActivityStatistics_Success() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<Object[]> results = Arrays.asList(
                new Object[]{"john.doe", 15L},
                new Object[]{"jane.smith", 10L}
        );

        when(auditLogRepository.countByUserAndDateRange(tenantId, start, end))
                .thenReturn(results);

        Map<String, Long> statistics = auditLogService.getUserActivityStatistics(
                tenantId, start, end);

        assertThat(statistics).hasSize(2);
        assertThat(statistics.get("john.doe")).isEqualTo(15L);
        assertThat(statistics.get("jane.smith")).isEqualTo(10L);
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("감사 로그 수동 생성 - 성공")
    void testCreateAuditLog_Success() {
        when(auditLogRepository.save(testAuditLog))
                .thenReturn(testAuditLog);

        AuditLogEntity result = auditLogService.createAuditLog(testAuditLog);

        assertThat(result).isNotNull();
        verify(auditLogRepository).save(testAuditLog);
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("오래된 감사 로그 정리 - 성공")
    void testCleanOldAuditLogs_Success() {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(90);
        Page<AuditLogEntity> oldLogs = new PageImpl<>(Arrays.asList(testAuditLog));

        when(auditLogRepository.findByTenantIdAndDateRange(
                eq(tenantId), any(LocalDateTime.class), eq(beforeDate), any(Pageable.class)))
                .thenReturn(oldLogs);

        auditLogService.cleanOldAuditLogs(tenantId, beforeDate);

        verify(auditLogRepository).deleteAll(anyList());
    }
}
