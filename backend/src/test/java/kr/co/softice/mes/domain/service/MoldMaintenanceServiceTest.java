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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Mold Maintenance Service Test
 */
@ExtendWith(MockitoExtension.class)
class MoldMaintenanceServiceTest {

    @Mock
    private MoldMaintenanceRepository maintenanceRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private MoldRepository moldRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MoldMaintenanceService maintenanceService;

    private TenantEntity testTenant;
    private MoldEntity testMold;
    private UserEntity testTechnician;
    private MoldMaintenanceEntity testMaintenance;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("Test Tenant");

        // 테스트 금형
        testMold = new MoldEntity();
        testMold.setMoldId(1L);
        testMold.setMoldCode("MOLD-001");
        testMold.setMoldName("Test Mold");
        testMold.setCurrentShotCount(50000L);
        testMold.setLastMaintenanceShot(0L);

        // 테스트 기술자
        testTechnician = new UserEntity();
        testTechnician.setUserId(1L);
        testTechnician.setUsername("technician01");
        testTechnician.setFullName("기술자 이름");

        // 테스트 보전
        testMaintenance = new MoldMaintenanceEntity();
        testMaintenance.setMaintenanceId(1L);
        testMaintenance.setMaintenanceNo("MNT-2026-001");
        testMaintenance.setMaintenanceType("PERIODIC");
        testMaintenance.setMaintenanceContent("정기 보전");
        testMaintenance.setMaintenanceDate(LocalDateTime.now());
        testMaintenance.setShotCountBefore(50000L);
        testMaintenance.setShotCountAfter(50000L);
        testMaintenance.setPartsCost(new BigDecimal("100000"));
        testMaintenance.setLaborCost(new BigDecimal("50000"));
        testMaintenance.setShotCountReset(false);
        testMaintenance.setMaintenanceResult("COMPLETED");
        testMaintenance.setIsActive(true);
        testMaintenance.setTenant(testTenant);
        testMaintenance.setMold(testMold);
        testMaintenance.setTechnicianUser(testTechnician);
        testMaintenance.setTechnicianName("기술자 이름");
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("보전 조회 - 전체 조회 성공")
    void testGetAllMaintenances_Success() {
        // Given
        String tenantId = "TEST001";
        List<MoldMaintenanceEntity> expectedList = Arrays.asList(testMaintenance);

        when(maintenanceRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<MoldMaintenanceEntity> result = maintenanceService.getAllMaintenances(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaintenanceNo()).isEqualTo("MNT-2026-001");
        verify(maintenanceRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("보전 조회 - ID로 조회 성공")
    void testGetMaintenanceById_Success() {
        // Given
        Long maintenanceId = 1L;

        when(maintenanceRepository.findByIdWithAllRelations(maintenanceId))
                .thenReturn(Optional.of(testMaintenance));

        // When
        MoldMaintenanceEntity result = maintenanceService.getMaintenanceById(maintenanceId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMaintenanceId()).isEqualTo(maintenanceId);
        verify(maintenanceRepository, times(1)).findByIdWithAllRelations(maintenanceId);
    }

    @Test
    @DisplayName("보전 조회 - ID로 조회 실패 (없음)")
    void testGetMaintenanceById_Fail_NotFound() {
        // Given
        Long maintenanceId = 999L;

        when(maintenanceRepository.findByIdWithAllRelations(maintenanceId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> maintenanceService.getMaintenanceById(maintenanceId))
                .isInstanceOf(BusinessException.class);
        verify(maintenanceRepository, times(1)).findByIdWithAllRelations(maintenanceId);
    }

    @Test
    @DisplayName("보전 조회 - 금형별 조회 성공")
    void testGetMaintenancesByMold_Success() {
        // Given
        Long moldId = 1L;
        List<MoldMaintenanceEntity> expectedList = Arrays.asList(testMaintenance);

        when(maintenanceRepository.findByMoldId(moldId))
                .thenReturn(expectedList);

        // When
        List<MoldMaintenanceEntity> result = maintenanceService.getMaintenancesByMold(moldId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(maintenanceRepository, times(1)).findByMoldId(moldId);
    }

    @Test
    @DisplayName("보전 조회 - 타입별 조회 성공")
    void testGetMaintenancesByType_Success() {
        // Given
        String tenantId = "TEST001";
        String maintenanceType = "PERIODIC";
        List<MoldMaintenanceEntity> expectedList = Arrays.asList(testMaintenance);

        when(maintenanceRepository.findByTenantIdAndMaintenanceType(tenantId, maintenanceType))
                .thenReturn(expectedList);

        // When
        List<MoldMaintenanceEntity> result = maintenanceService.getMaintenancesByType(tenantId, maintenanceType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaintenanceType()).isEqualTo("PERIODIC");
        verify(maintenanceRepository, times(1)).findByTenantIdAndMaintenanceType(tenantId, maintenanceType);
    }

    @Test
    @DisplayName("보전 조회 - 기간별 조회 성공")
    void testGetMaintenancesByDateRange_Success() {
        // Given
        String tenantId = "TEST001";
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        List<MoldMaintenanceEntity> expectedList = Arrays.asList(testMaintenance);

        when(maintenanceRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate))
                .thenReturn(expectedList);

        // When
        List<MoldMaintenanceEntity> result = maintenanceService.getMaintenancesByDateRange(tenantId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(maintenanceRepository, times(1)).findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("보전 생성 - 성공 (샷 카운트 유지)")
    void testCreateMaintenance_Success_NoReset() {
        // Given
        String tenantId = "TEST001";
        MoldMaintenanceEntity newMaintenance = new MoldMaintenanceEntity();
        newMaintenance.setMaintenanceNo("MNT-2026-002");
        newMaintenance.setMaintenanceType("CORRECTIVE");
        newMaintenance.setShotCountReset(false);

        MoldEntity mold = new MoldEntity();
        mold.setMoldId(1L);
        newMaintenance.setMold(mold);

        UserEntity technician = new UserEntity();
        technician.setUserId(1L);
        newMaintenance.setTechnicianUser(technician);

        when(maintenanceRepository.existsByTenant_TenantIdAndMaintenanceNo(tenantId, "MNT-2026-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(moldRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testMold));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testTechnician));
        when(maintenanceRepository.save(any(MoldMaintenanceEntity.class)))
                .thenAnswer(invocation -> {
                    MoldMaintenanceEntity saved = invocation.getArgument(0);
                    saved.setMaintenanceId(2L);
                    assertThat(saved.getShotCountBefore()).isEqualTo(50000L);
                    assertThat(saved.getPartsCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getLaborCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getShotCountReset()).isFalse();
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> {
                    MoldEntity saved = invocation.getArgument(0);
                    assertThat(saved.getLastMaintenanceShot()).isEqualTo(50000L);
                    return saved;
                });

        // When
        MoldMaintenanceEntity result = maintenanceService.createMaintenance(tenantId, newMaintenance);

        // Then
        assertThat(result).isNotNull();
        verify(maintenanceRepository, times(1)).save(any(MoldMaintenanceEntity.class));
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    @Test
    @DisplayName("보전 생성 - 성공 (샷 카운트 리셋)")
    void testCreateMaintenance_Success_WithReset() {
        // Given
        String tenantId = "TEST001";
        MoldMaintenanceEntity newMaintenance = new MoldMaintenanceEntity();
        newMaintenance.setMaintenanceNo("MNT-2026-003");
        newMaintenance.setMaintenanceType("OVERHAUL");
        newMaintenance.setShotCountReset(true);

        MoldEntity mold = new MoldEntity();
        mold.setMoldId(1L);
        newMaintenance.setMold(mold);

        when(maintenanceRepository.existsByTenant_TenantIdAndMaintenanceNo(tenantId, "MNT-2026-003"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(moldRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testMold));
        when(maintenanceRepository.save(any(MoldMaintenanceEntity.class)))
                .thenAnswer(invocation -> {
                    MoldMaintenanceEntity saved = invocation.getArgument(0);
                    saved.setMaintenanceId(3L);
                    return saved;
                });
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> {
                    MoldEntity saved = invocation.getArgument(0);
                    assertThat(saved.getLastMaintenanceShot()).isEqualTo(0L);
                    return saved;
                });

        // When
        MoldMaintenanceEntity result = maintenanceService.createMaintenance(tenantId, newMaintenance);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getShotCountAfter()).isEqualTo(0L);
        verify(maintenanceRepository, times(1)).save(any(MoldMaintenanceEntity.class));
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    @Test
    @DisplayName("보전 생성 - 실패 (보전 번호 중복)")
    void testCreateMaintenance_Fail_DuplicateNo() {
        // Given
        String tenantId = "TEST001";
        MoldMaintenanceEntity newMaintenance = new MoldMaintenanceEntity();
        newMaintenance.setMaintenanceNo("MNT-2026-001");

        when(maintenanceRepository.existsByTenant_TenantIdAndMaintenanceNo(tenantId, "MNT-2026-001"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> maintenanceService.createMaintenance(tenantId, newMaintenance))
                .isInstanceOf(BusinessException.class);
        verify(maintenanceRepository, never()).save(any(MoldMaintenanceEntity.class));
    }

    @Test
    @DisplayName("보전 생성 - 기본값 설정 확인")
    void testCreateMaintenance_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        MoldMaintenanceEntity newMaintenance = new MoldMaintenanceEntity();
        newMaintenance.setMaintenanceNo("MNT-2026-004");
        // 기본값을 설정하지 않음

        MoldEntity mold = new MoldEntity();
        mold.setMoldId(1L);
        newMaintenance.setMold(mold);

        when(maintenanceRepository.existsByTenant_TenantIdAndMaintenanceNo(tenantId, "MNT-2026-004"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(moldRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testMold));
        when(maintenanceRepository.save(any(MoldMaintenanceEntity.class)))
                .thenAnswer(invocation -> {
                    MoldMaintenanceEntity saved = invocation.getArgument(0);
                    assertThat(saved.getPartsCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getLaborCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getShotCountReset()).isFalse();
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });
        when(moldRepository.save(any(MoldEntity.class)))
                .thenReturn(testMold);

        // When
        maintenanceService.createMaintenance(tenantId, newMaintenance);

        // Then
        verify(maintenanceRepository, times(1)).save(any(MoldMaintenanceEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("보전 수정 - 성공")
    void testUpdateMaintenance_Success() {
        // Given
        Long maintenanceId = 1L;
        MoldMaintenanceEntity updateData = new MoldMaintenanceEntity();
        updateData.setMaintenanceContent("Updated content");
        updateData.setPartsReplaced("New parts");
        updateData.setMaintenanceResult("COMPLETED");

        when(maintenanceRepository.findByIdWithAllRelations(maintenanceId))
                .thenReturn(Optional.of(testMaintenance));
        when(maintenanceRepository.save(any(MoldMaintenanceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MoldMaintenanceEntity result = maintenanceService.updateMaintenance(maintenanceId, updateData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMaintenanceContent()).isEqualTo("Updated content");
        assertThat(result.getPartsReplaced()).isEqualTo("New parts");
        assertThat(result.getMaintenanceResult()).isEqualTo("COMPLETED");
        verify(maintenanceRepository, times(1)).save(any(MoldMaintenanceEntity.class));
    }

    @Test
    @DisplayName("보전 수정 - 실패 (없음)")
    void testUpdateMaintenance_Fail_NotFound() {
        // Given
        Long maintenanceId = 999L;
        MoldMaintenanceEntity updateData = new MoldMaintenanceEntity();

        when(maintenanceRepository.findByIdWithAllRelations(maintenanceId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> maintenanceService.updateMaintenance(maintenanceId, updateData))
                .isInstanceOf(BusinessException.class);
        verify(maintenanceRepository, never()).save(any(MoldMaintenanceEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("보전 삭제 - 성공")
    void testDeleteMaintenance_Success() {
        // Given
        Long maintenanceId = 1L;

        when(maintenanceRepository.findById(maintenanceId))
                .thenReturn(Optional.of(testMaintenance));
        doNothing().when(maintenanceRepository).delete(testMaintenance);

        // When
        maintenanceService.deleteMaintenance(maintenanceId);

        // Then
        verify(maintenanceRepository, times(1)).findById(maintenanceId);
        verify(maintenanceRepository, times(1)).delete(testMaintenance);
    }

    @Test
    @DisplayName("보전 삭제 - 실패 (없음)")
    void testDeleteMaintenance_Fail_NotFound() {
        // Given
        Long maintenanceId = 999L;

        when(maintenanceRepository.findById(maintenanceId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> maintenanceService.deleteMaintenance(maintenanceId))
                .isInstanceOf(BusinessException.class);
        verify(maintenanceRepository, never()).delete(any(MoldMaintenanceEntity.class));
    }
}
