package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DowntimeService Unit Test
 *
 * 테스트 대상:
 * - 비가동 CRUD
 * - 비가동 종료
 * - 비가동 해결
 * - 활성화/비활성화
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DowntimeService 단위 테스트")
class DowntimeServiceTest {

    @Mock
    private DowntimeRepository downtimeRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private EquipmentOperationRepository operationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DowntimeService downtimeService;

    private TenantEntity testTenant;
    private EquipmentEntity testEquipment;
    private UserEntity testUser;
    private DowntimeEntity testDowntime;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        // 테스트 설비
        testEquipment = new EquipmentEntity();
        testEquipment.setEquipmentId(1L);
        testEquipment.setEquipmentCode("EQ-001");
        testEquipment.setEquipmentName("사출기 1호");

        // 테스트 사용자
        testUser = new UserEntity();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setFullName("테스트 담당자");

        // 테스트 비가동
        testDowntime = new DowntimeEntity();
        testDowntime.setDowntimeId(1L);
        testDowntime.setDowntimeCode("DT-2026-001");
        testDowntime.setDowntimeType("BREAKDOWN");
        testDowntime.setDowntimeCategory("MECHANICAL");
        testDowntime.setStartTime(LocalDateTime.now().minusHours(2));
        testDowntime.setCause("베어링 고장");
        testDowntime.setCountermeasure("베어링 교체");
        testDowntime.setIsResolved(false);
        testDowntime.setIsActive(true);
        testDowntime.setTenant(testTenant);
        testDowntime.setEquipment(testEquipment);
        testDowntime.setResponsibleUser(testUser);
        testDowntime.setResponsibleName(testUser.getFullName());
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("비가동 조회 - 전체 조회 성공")
    void testGetAllDowntimes_Success() {
        // Given
        String tenantId = "TEST001";
        List<DowntimeEntity> expectedList = Arrays.asList(testDowntime);

        when(downtimeRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<DowntimeEntity> result = downtimeService.getAllDowntimes(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDowntimeCode()).isEqualTo("DT-2026-001");
        verify(downtimeRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("비가동 조회 - ID로 조회 성공")
    void testGetDowntimeById_Success() {
        // Given
        Long downtimeId = 1L;
        when(downtimeRepository.findByIdWithAllRelations(downtimeId))
                .thenReturn(Optional.of(testDowntime));

        // When
        DowntimeEntity result = downtimeService.getDowntimeById(downtimeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDowntimeId()).isEqualTo(downtimeId);
        verify(downtimeRepository, times(1)).findByIdWithAllRelations(downtimeId);
    }

    @Test
    @DisplayName("비가동 조회 - ID로 조회 실패 (없음)")
    void testGetDowntimeById_Fail_NotFound() {
        // Given
        Long downtimeId = 999L;
        when(downtimeRepository.findByIdWithAllRelations(downtimeId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> downtimeService.getDowntimeById(downtimeId))
                .isInstanceOf(BusinessException.class);

        verify(downtimeRepository, times(1)).findByIdWithAllRelations(downtimeId);
    }

    @Test
    @DisplayName("비가동 조회 - 설비별 조회 성공")
    void testGetDowntimesByEquipment_Success() {
        // Given
        Long equipmentId = 1L;
        List<DowntimeEntity> expectedList = Arrays.asList(testDowntime);

        when(downtimeRepository.findByEquipmentId(equipmentId))
                .thenReturn(expectedList);

        // When
        List<DowntimeEntity> result = downtimeService.getDowntimesByEquipment(equipmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(downtimeRepository, times(1)).findByEquipmentId(equipmentId);
    }

    @Test
    @DisplayName("비가동 조회 - 타입별 조회 성공")
    void testGetDowntimesByType_Success() {
        // Given
        String tenantId = "TEST001";
        String downtimeType = "BREAKDOWN";
        List<DowntimeEntity> expectedList = Arrays.asList(testDowntime);

        when(downtimeRepository.findByTenantIdAndDowntimeType(tenantId, downtimeType))
                .thenReturn(expectedList);

        // When
        List<DowntimeEntity> result = downtimeService.getDowntimesByType(tenantId, downtimeType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDowntimeType()).isEqualTo(downtimeType);
        verify(downtimeRepository, times(1)).findByTenantIdAndDowntimeType(tenantId, downtimeType);
    }

    @Test
    @DisplayName("비가동 조회 - 날짜 범위 조회 성공")
    void testGetDowntimesByDateRange_Success() {
        // Given
        String tenantId = "TEST001";
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<DowntimeEntity> expectedList = Arrays.asList(testDowntime);

        when(downtimeRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate))
                .thenReturn(expectedList);

        // When
        List<DowntimeEntity> result = downtimeService.getDowntimesByDateRange(tenantId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(downtimeRepository, times(1)).findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Test
    @DisplayName("비가동 조회 - 미해결 비가동 조회 성공")
    void testGetUnresolvedDowntimes_Success() {
        // Given
        String tenantId = "TEST001";
        List<DowntimeEntity> expectedList = Arrays.asList(testDowntime);

        when(downtimeRepository.findUnresolvedByTenantId(tenantId))
                .thenReturn(expectedList);

        // When
        List<DowntimeEntity> result = downtimeService.getUnresolvedDowntimes(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(downtimeRepository, times(1)).findUnresolvedByTenantId(tenantId);
    }

    @Test
    @DisplayName("비가동 조회 - 진행중 비가동 조회 성공")
    void testGetOngoingDowntimes_Success() {
        // Given
        String tenantId = "TEST001";
        List<DowntimeEntity> expectedList = Arrays.asList(testDowntime);

        when(downtimeRepository.findOngoingByTenantId(tenantId))
                .thenReturn(expectedList);

        // When
        List<DowntimeEntity> result = downtimeService.getOngoingDowntimes(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(downtimeRepository, times(1)).findOngoingByTenantId(tenantId);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("비가동 생성 - 성공")
    void testCreateDowntime_Success() {
        // Given
        String tenantId = "TEST001";

        when(downtimeRepository.existsByTenant_TenantIdAndDowntimeCode(tenantId, testDowntime.getDowntimeCode()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(testEquipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(userRepository.findById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));

        when(downtimeRepository.save(any(DowntimeEntity.class)))
                .thenAnswer(invocation -> {
                    DowntimeEntity saved = invocation.getArgument(0);
                    assertThat(saved.getTenant()).isEqualTo(testTenant);
                    assertThat(saved.getEquipment()).isEqualTo(testEquipment);
                    assertThat(saved.getResponsibleName()).isEqualTo(testUser.getFullName());
                    return saved;
                });

        // When
        DowntimeEntity result = downtimeService.createDowntime(tenantId, testDowntime);

        // Then
        assertThat(result).isNotNull();
        verify(downtimeRepository, times(1)).save(any(DowntimeEntity.class));
    }

    @Test
    @DisplayName("비가동 생성 - 성공 (기본값 설정)")
    void testCreateDowntime_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        DowntimeEntity minimalDowntime = new DowntimeEntity();
        minimalDowntime.setDowntimeCode("DT-2026-002");
        minimalDowntime.setStartTime(LocalDateTime.now());

        EquipmentEntity equipment = new EquipmentEntity();
        equipment.setEquipmentId(1L);
        minimalDowntime.setEquipment(equipment);

        when(downtimeRepository.existsByTenant_TenantIdAndDowntimeCode(tenantId, minimalDowntime.getDowntimeCode()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(equipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(downtimeRepository.save(any(DowntimeEntity.class)))
                .thenAnswer(invocation -> {
                    DowntimeEntity saved = invocation.getArgument(0);
                    // 기본값 검증
                    assertThat(saved.getIsResolved()).isFalse();
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });

        // When
        DowntimeEntity result = downtimeService.createDowntime(tenantId, minimalDowntime);

        // Then
        assertThat(result).isNotNull();
        verify(downtimeRepository, times(1)).save(any(DowntimeEntity.class));
    }

    @Test
    @DisplayName("비가동 생성 - 실패 (중복 코드)")
    void testCreateDowntime_Fail_Duplicate() {
        // Given
        String tenantId = "TEST001";

        when(downtimeRepository.existsByTenant_TenantIdAndDowntimeCode(tenantId, testDowntime.getDowntimeCode()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> downtimeService.createDowntime(tenantId, testDowntime))
                .isInstanceOf(BusinessException.class);

        verify(downtimeRepository, never()).save(any(DowntimeEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("비가동 수정 - 성공")
    void testUpdateDowntime_Success() {
        // Given
        Long downtimeId = 1L;
        DowntimeEntity updateData = new DowntimeEntity();
        updateData.setEndTime(LocalDateTime.now());
        updateData.setCause("수정된 원인");
        updateData.setCountermeasure("수정된 조치");
        updateData.setPreventiveAction("재발 방지 대책");

        when(downtimeRepository.findByIdWithAllRelations(downtimeId))
                .thenReturn(Optional.of(testDowntime));

        when(downtimeRepository.save(any(DowntimeEntity.class)))
                .thenAnswer(invocation -> {
                    DowntimeEntity saved = invocation.getArgument(0);
                    assertThat(saved.getEndTime()).isNotNull();
                    assertThat(saved.getCause()).isEqualTo("수정된 원인");
                    assertThat(saved.getCountermeasure()).isEqualTo("수정된 조치");
                    assertThat(saved.getPreventiveAction()).isEqualTo("재발 방지 대책");
                    return saved;
                });

        // When
        DowntimeEntity result = downtimeService.updateDowntime(downtimeId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(downtimeRepository, times(1)).findByIdWithAllRelations(downtimeId);
        verify(downtimeRepository, times(1)).save(any(DowntimeEntity.class));
    }

    @Test
    @DisplayName("비가동 수정 - 실패 (비가동 없음)")
    void testUpdateDowntime_Fail_NotFound() {
        // Given
        Long downtimeId = 999L;
        DowntimeEntity updateData = new DowntimeEntity();

        when(downtimeRepository.findByIdWithAllRelations(downtimeId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> downtimeService.updateDowntime(downtimeId, updateData))
                .isInstanceOf(BusinessException.class);

        verify(downtimeRepository, never()).save(any(DowntimeEntity.class));
    }

    // ================== 비가동 종료 테스트 ==================

    @Test
    @DisplayName("비가동 종료 - 성공")
    void testEndDowntime_Success() {
        // Given
        Long downtimeId = 1L;

        when(downtimeRepository.findByIdWithAllRelations(downtimeId))
                .thenReturn(Optional.of(testDowntime));

        when(downtimeRepository.save(any(DowntimeEntity.class)))
                .thenAnswer(invocation -> {
                    DowntimeEntity saved = invocation.getArgument(0);
                    assertThat(saved.getEndTime()).isNotNull();
                    return saved;
                });

        // When
        DowntimeEntity result = downtimeService.endDowntime(downtimeId);

        // Then
        assertThat(result).isNotNull();
        verify(downtimeRepository, times(1)).findByIdWithAllRelations(downtimeId);
        verify(downtimeRepository, times(1)).save(any(DowntimeEntity.class));
    }

    // ================== 비가동 해결 테스트 ==================

    @Test
    @DisplayName("비가동 해결 - 성공 (종료 + 해결 표시)")
    void testResolveDowntime_Success() {
        // Given
        Long downtimeId = 1L;

        when(downtimeRepository.findByIdWithAllRelations(downtimeId))
                .thenReturn(Optional.of(testDowntime));

        when(downtimeRepository.save(any(DowntimeEntity.class)))
                .thenAnswer(invocation -> {
                    DowntimeEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsResolved()).isTrue();
                    assertThat(saved.getResolvedAt()).isNotNull();
                    assertThat(saved.getEndTime()).isNotNull();
                    return saved;
                });

        // When
        DowntimeEntity result = downtimeService.resolveDowntime(downtimeId);

        // Then
        assertThat(result).isNotNull();
        verify(downtimeRepository, times(1)).findByIdWithAllRelations(downtimeId);
        verify(downtimeRepository, times(1)).save(any(DowntimeEntity.class));
    }

    // ================== 활성화/비활성화 테스트 ==================

    @Test
    @DisplayName("비가동 활성화 - 성공")
    void testActivate_Success() {
        // Given
        Long downtimeId = 1L;
        testDowntime.setIsActive(false);

        when(downtimeRepository.findByIdWithAllRelations(downtimeId))
                .thenReturn(Optional.of(testDowntime));

        when(downtimeRepository.save(any(DowntimeEntity.class)))
                .thenAnswer(invocation -> {
                    DowntimeEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });

        // When
        DowntimeEntity result = downtimeService.activate(downtimeId);

        // Then
        assertThat(result).isNotNull();
        verify(downtimeRepository, times(1)).findByIdWithAllRelations(downtimeId);
        verify(downtimeRepository, times(1)).save(any(DowntimeEntity.class));
    }

    @Test
    @DisplayName("비가동 비활성화 - 성공")
    void testDeactivate_Success() {
        // Given
        Long downtimeId = 1L;
        testDowntime.setIsActive(true);

        when(downtimeRepository.findByIdWithAllRelations(downtimeId))
                .thenReturn(Optional.of(testDowntime));

        when(downtimeRepository.save(any(DowntimeEntity.class)))
                .thenAnswer(invocation -> {
                    DowntimeEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });

        // When
        DowntimeEntity result = downtimeService.deactivate(downtimeId);

        // Then
        assertThat(result).isNotNull();
        verify(downtimeRepository, times(1)).findByIdWithAllRelations(downtimeId);
        verify(downtimeRepository, times(1)).save(any(DowntimeEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("비가동 삭제 - 성공")
    void testDeleteDowntime_Success() {
        // Given
        Long downtimeId = 1L;

        when(downtimeRepository.findById(downtimeId))
                .thenReturn(Optional.of(testDowntime));

        doNothing().when(downtimeRepository).delete(testDowntime);

        // When
        downtimeService.deleteDowntime(downtimeId);

        // Then
        verify(downtimeRepository, times(1)).findById(downtimeId);
        verify(downtimeRepository, times(1)).delete(testDowntime);
    }

    @Test
    @DisplayName("비가동 삭제 - 실패 (비가동 없음)")
    void testDeleteDowntime_Fail_NotFound() {
        // Given
        Long downtimeId = 999L;

        when(downtimeRepository.findById(downtimeId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> downtimeService.deleteDowntime(downtimeId))
                .isInstanceOf(BusinessException.class);

        verify(downtimeRepository, never()).delete(any(DowntimeEntity.class));
    }
}
