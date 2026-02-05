package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.AlarmHistoryEntity;
import kr.co.softice.mes.domain.entity.AlarmTemplateEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.AlarmHistoryRepository;
import kr.co.softice.mes.domain.repository.AlarmTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Alarm Service Test
 * 알람 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("알람 서비스 테스트")
class AlarmServiceTest {

    @Mock
    private AlarmHistoryRepository historyRepository;

    @Mock
    private AlarmTemplateRepository templateRepository;

    @InjectMocks
    private AlarmService alarmService;

    private TenantEntity testTenant;
    private AlarmTemplateEntity testTemplate;
    private AlarmHistoryEntity testAlarm;
    private String tenantId;
    private Long recipientUserId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";
        recipientUserId = 123L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);

        testTemplate = AlarmTemplateEntity.builder()
                .eventType("WORK_ORDER_CREATED")
                .alarmType("PRODUCTION")
                .priority("MEDIUM")
                .titleTemplate("Work Order {{woNumber}} Created")
                .messageTemplate("Work Order {{woNumber}} has been created")
                .enableEmail(true)
                .enableSms(false)
                .enablePush(true)
                .enableSystem(true)
                .build();
        testTemplate.setTenant(testTenant);

        testAlarm = AlarmHistoryEntity.builder()
                .recipientUserId(recipientUserId)
                .recipientName("Test User")
                .alarmType("PRODUCTION")
                .sendStatus("SENT")
                .build();
    }

    // === 템플릿 조회 테스트 ===

    @Test
    @DisplayName("모든 템플릿 조회 - 성공")
    void testFindAllTemplates_Success() {
        List<AlarmTemplateEntity> templates = Arrays.asList(testTemplate);
        when(templateRepository.findAllByTenantId(tenantId))
                .thenReturn(templates);

        List<AlarmTemplateEntity> result = alarmService.findAllTemplates(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("이벤트 타입으로 템플릿 조회 - 성공")
    void testFindTemplateByEventType_Success() {
        when(templateRepository.findByTenantIdAndEventType(tenantId, "WORK_ORDER_CREATED"))
                .thenReturn(Optional.of(testTemplate));

        Optional<AlarmTemplateEntity> result = alarmService.findTemplateByEventType(tenantId, "WORK_ORDER_CREATED");

        assertThat(result).isPresent();
    }

    // === 알람 전송 테스트 ===

    @Test
    @DisplayName("알람 전송 - 성공")
    void testSendAlarm_Success() {
        Map<String, String> variables = new HashMap<>();
        variables.put("woNumber", "WO001");

        when(templateRepository.findByTenantIdAndEventType(tenantId, "WORK_ORDER_CREATED"))
                .thenReturn(Optional.of(testTemplate));
        when(historyRepository.save(any(AlarmHistoryEntity.class)))
                .thenReturn(testAlarm);

        AlarmHistoryEntity result = alarmService.sendAlarm(
                tenantId, "WORK_ORDER_CREATED", recipientUserId, "Test User",
                variables, "WORK_ORDER", 1L, "WO001");

        assertThat(result).isNotNull();
        verify(historyRepository, atLeast(1)).save(any(AlarmHistoryEntity.class));
    }

    @Test
    @DisplayName("알람 전송 - 실패 (템플릿 없음)")
    void testSendAlarm_Fail_TemplateNotFound() {
        Map<String, String> variables = new HashMap<>();
        when(templateRepository.findByTenantIdAndEventType(tenantId, "INVALID"))
                .thenReturn(Optional.empty());

        AlarmHistoryEntity result = alarmService.sendAlarm(
                tenantId, "INVALID", recipientUserId, "Test User",
                variables, "WORK_ORDER", 1L, "WO001");

        assertThat(result).isNull();
    }

    // === 알람 조회 테스트 ===

    @Test
    @DisplayName("수신자별 알람 조회 - 성공")
    void testFindAlarmsByRecipient_Success() {
        List<AlarmHistoryEntity> alarms = Arrays.asList(testAlarm);
        when(historyRepository.findByRecipient(tenantId, recipientUserId))
                .thenReturn(alarms);

        List<AlarmHistoryEntity> result = alarmService.findAlarmsByRecipient(tenantId, recipientUserId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("읽지 않은 알람 조회 - 성공")
    void testFindUnreadAlarms_Success() {
        List<AlarmHistoryEntity> alarms = Arrays.asList(testAlarm);
        when(historyRepository.findUnreadByRecipient(tenantId, recipientUserId))
                .thenReturn(alarms);

        List<AlarmHistoryEntity> result = alarmService.findUnreadAlarms(tenantId, recipientUserId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("최근 알람 조회 - 성공")
    void testFindRecentAlarms_Success() {
        List<AlarmHistoryEntity> alarms = Arrays.asList(testAlarm);
        when(historyRepository.findRecentByRecipient(eq(tenantId), eq(recipientUserId), any(LocalDateTime.class)))
                .thenReturn(alarms);

        List<AlarmHistoryEntity> result = alarmService.findRecentAlarms(tenantId, recipientUserId);

        assertThat(result).hasSize(1);
    }

    // === 알람 읽음 처리 테스트 ===

    @Test
    @DisplayName("알람 읽음 처리 - 성공")
    void testMarkAsRead_Success() {
        when(historyRepository.findById(1L))
                .thenReturn(Optional.of(testAlarm));

        alarmService.markAsRead(1L);

        verify(historyRepository).save(testAlarm);
    }

    @Test
    @DisplayName("모든 알람 읽음 처리 - 성공")
    void testMarkAllAsRead_Success() {
        List<AlarmHistoryEntity> unreadAlarms = Arrays.asList(testAlarm);
        when(historyRepository.findUnreadByRecipient(tenantId, recipientUserId))
                .thenReturn(unreadAlarms);

        alarmService.markAllAsRead(tenantId, recipientUserId);

        verify(historyRepository, atLeastOnce()).save(any(AlarmHistoryEntity.class));
    }

    // === 알람 삭제 테스트 ===

    @Test
    @DisplayName("오래된 알람 삭제 - 성공")
    void testDeleteOldAlarms_Success() {
        alarmService.deleteOldAlarms(tenantId, 30);

        verify(historyRepository).deleteOldAlarms(eq(tenantId), any(LocalDateTime.class));
    }

    // === 통계 테스트 ===

    @Test
    @DisplayName("읽지 않은 알람 수 조회 - 성공")
    void testCountUnreadAlarms_Success() {
        when(historyRepository.countUnreadByRecipient(tenantId, recipientUserId))
                .thenReturn(5L);

        Long result = alarmService.countUnreadAlarms(tenantId, recipientUserId);

        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("알람 통계 조회 - 성공")
    void testGetStatistics_Success() {
        when(historyRepository.countUnreadByRecipient(tenantId, recipientUserId))
                .thenReturn(5L);
        when(historyRepository.findByRecipient(tenantId, recipientUserId))
                .thenReturn(Arrays.asList(testAlarm, testAlarm, testAlarm));
        when(historyRepository.countByTypeAndRecipient(tenantId, recipientUserId, "APPROVAL"))
                .thenReturn(1L);
        when(historyRepository.countByTypeAndRecipient(tenantId, recipientUserId, "QUALITY"))
                .thenReturn(2L);
        when(historyRepository.countByTypeAndRecipient(tenantId, recipientUserId, "PRODUCTION"))
                .thenReturn(3L);
        when(historyRepository.countByTypeAndRecipient(tenantId, recipientUserId, "INVENTORY"))
                .thenReturn(4L);

        AlarmService.AlarmStatistics result = alarmService.getStatistics(tenantId, recipientUserId);

        assertThat(result).isNotNull();
        assertThat(result.getUnreadCount()).isEqualTo(5L);
    }
}
