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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Mold Service Test
 */
@ExtendWith(MockitoExtension.class)
class MoldServiceTest {

    @Mock
    private MoldRepository moldRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private MoldService moldService;

    private TenantEntity testTenant;
    private SiteEntity testSite;
    private DepartmentEntity testDepartment;
    private MoldEntity testMold;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("Test Tenant");

        // 테스트 사이트
        testSite = new SiteEntity();
        testSite.setSiteId(1L);
        testSite.setSiteCode("SITE-001");
        testSite.setSiteName("Test Site");

        // 테스트 부서
        testDepartment = new DepartmentEntity();
        testDepartment.setDepartmentId(1L);
        testDepartment.setDepartmentCode("DEPT-001");
        testDepartment.setDepartmentName("Test Department");

        // 테스트 금형
        testMold = new MoldEntity();
        testMold.setMoldId(1L);
        testMold.setMoldCode("MOLD-001");
        testMold.setMoldName("Test Mold");
        testMold.setMoldType("INJECTION");
        testMold.setMoldGrade("A");
        testMold.setCavityCount(4);
        testMold.setCurrentShotCount(10000L);
        testMold.setLastMaintenanceShot(0L);
        testMold.setMaxShotCount(1000000L);
        testMold.setMaintenanceShotInterval(50000L);
        testMold.setManufacturer("Test Manufacturer");
        testMold.setModelName("TM-100");
        testMold.setSerialNo("SN-001");
        testMold.setMaterial("Steel");
        testMold.setWeight(new BigDecimal("500.00"));
        testMold.setPurchasePrice(new BigDecimal("10000000.00"));
        testMold.setPurchaseDate(LocalDate.now().minusYears(2));
        testMold.setLocation("Warehouse-A");
        testMold.setStatus("AVAILABLE");
        testMold.setIsActive(true);
        testMold.setTenant(testTenant);
        testMold.setSite(testSite);
        testMold.setDepartment(testDepartment);
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("금형 조회 - 전체 조회 성공")
    void testGetAllMolds_Success() {
        // Given
        String tenantId = "TEST001";
        List<MoldEntity> expectedList = Arrays.asList(testMold);

        when(moldRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<MoldEntity> result = moldService.getAllMolds(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMoldCode()).isEqualTo("MOLD-001");
        verify(moldRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("금형 조회 - 활성 금형만 조회")
    void testGetActiveMolds_Success() {
        // Given
        String tenantId = "TEST001";
        List<MoldEntity> expectedList = Arrays.asList(testMold);

        when(moldRepository.findActiveByTenantId(tenantId))
                .thenReturn(expectedList);

        // When
        List<MoldEntity> result = moldService.getActiveMolds(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(moldRepository, times(1)).findActiveByTenantId(tenantId);
    }

    @Test
    @DisplayName("금형 조회 - ID로 조회 성공")
    void testGetMoldById_Success() {
        // Given
        Long moldId = 1L;

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));

        // When
        MoldEntity result = moldService.getMoldById(moldId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMoldId()).isEqualTo(moldId);
        verify(moldRepository, times(1)).findByIdWithAllRelations(moldId);
    }

    @Test
    @DisplayName("금형 조회 - ID로 조회 실패 (없음)")
    void testGetMoldById_Fail_NotFound() {
        // Given
        Long moldId = 999L;

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> moldService.getMoldById(moldId))
                .isInstanceOf(BusinessException.class);
        verify(moldRepository, times(1)).findByIdWithAllRelations(moldId);
    }

    @Test
    @DisplayName("금형 조회 - 상태별 조회 성공")
    void testGetMoldsByStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String status = "AVAILABLE";
        List<MoldEntity> expectedList = Arrays.asList(testMold);

        when(moldRepository.findByTenantIdAndStatus(tenantId, status))
                .thenReturn(expectedList);

        // When
        List<MoldEntity> result = moldService.getMoldsByStatus(tenantId, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("AVAILABLE");
        verify(moldRepository, times(1)).findByTenantIdAndStatus(tenantId, status);
    }

    @Test
    @DisplayName("금형 조회 - 타입별 조회 성공")
    void testGetMoldsByType_Success() {
        // Given
        String tenantId = "TEST001";
        String moldType = "INJECTION";
        List<MoldEntity> expectedList = Arrays.asList(testMold);

        when(moldRepository.findByTenantIdAndMoldType(tenantId, moldType))
                .thenReturn(expectedList);

        // When
        List<MoldEntity> result = moldService.getMoldsByType(tenantId, moldType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMoldType()).isEqualTo("INJECTION");
        verify(moldRepository, times(1)).findByTenantIdAndMoldType(tenantId, moldType);
    }

    @Test
    @DisplayName("금형 조회 - 보수 필요 금형 조회")
    void testGetMoldsRequiringMaintenance_Success() {
        // Given
        String tenantId = "TEST001";
        List<MoldEntity> expectedList = Arrays.asList(testMold);

        when(moldRepository.findMoldsRequiringMaintenance(tenantId))
                .thenReturn(expectedList);

        // When
        List<MoldEntity> result = moldService.getMoldsRequiringMaintenance(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(moldRepository, times(1)).findMoldsRequiringMaintenance(tenantId);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("금형 생성 - 성공")
    void testCreateMold_Success() {
        // Given
        String tenantId = "TEST001";
        MoldEntity newMold = new MoldEntity();
        newMold.setMoldCode("MOLD-002");
        newMold.setMoldName("New Mold");

        SiteEntity site = new SiteEntity();
        site.setSiteId(1L);
        newMold.setSite(site);

        DepartmentEntity department = new DepartmentEntity();
        department.setDepartmentId(1L);
        newMold.setDepartment(department);

        when(moldRepository.existsByTenant_TenantIdAndMoldCode(tenantId, "MOLD-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(siteRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testSite));
        when(departmentRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testDepartment));
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> {
                    MoldEntity saved = invocation.getArgument(0);
                    saved.setMoldId(2L);
                    assertThat(saved.getCurrentShotCount()).isEqualTo(0L);
                    assertThat(saved.getLastMaintenanceShot()).isEqualTo(0L);
                    assertThat(saved.getIsActive()).isTrue();
                    assertThat(saved.getStatus()).isEqualTo("AVAILABLE");
                    return saved;
                });

        // When
        MoldEntity result = moldService.createMold(tenantId, newMold);

        // Then
        assertThat(result).isNotNull();
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    @Test
    @DisplayName("금형 생성 - 실패 (코드 중복)")
    void testCreateMold_Fail_DuplicateCode() {
        // Given
        String tenantId = "TEST001";
        MoldEntity newMold = new MoldEntity();
        newMold.setMoldCode("MOLD-001");

        when(moldRepository.existsByTenant_TenantIdAndMoldCode(tenantId, "MOLD-001"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> moldService.createMold(tenantId, newMold))
                .isInstanceOf(BusinessException.class);
        verify(moldRepository, never()).save(any(MoldEntity.class));
    }

    @Test
    @DisplayName("금형 생성 - 기본값 설정 확인")
    void testCreateMold_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        MoldEntity newMold = new MoldEntity();
        newMold.setMoldCode("MOLD-003");
        newMold.setMoldName("Mold with Defaults");
        // 기본값을 설정하지 않음

        when(moldRepository.existsByTenant_TenantIdAndMoldCode(tenantId, "MOLD-003"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> {
                    MoldEntity saved = invocation.getArgument(0);
                    assertThat(saved.getCurrentShotCount()).isEqualTo(0L);
                    assertThat(saved.getLastMaintenanceShot()).isEqualTo(0L);
                    assertThat(saved.getPurchasePrice()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getWeight()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getIsActive()).isTrue();
                    assertThat(saved.getStatus()).isEqualTo("AVAILABLE");
                    return saved;
                });

        // When
        moldService.createMold(tenantId, newMold);

        // Then
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("금형 수정 - 성공")
    void testUpdateMold_Success() {
        // Given
        Long moldId = 1L;
        MoldEntity updateData = new MoldEntity();
        updateData.setMoldName("Updated Mold Name");
        updateData.setMoldType("COMPRESSION");
        updateData.setLocation("Warehouse-B");

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MoldEntity result = moldService.updateMold(moldId, updateData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMoldName()).isEqualTo("Updated Mold Name");
        assertThat(result.getMoldType()).isEqualTo("COMPRESSION");
        assertThat(result.getLocation()).isEqualTo("Warehouse-B");
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    @Test
    @DisplayName("금형 수정 - 실패 (없음)")
    void testUpdateMold_Fail_NotFound() {
        // Given
        Long moldId = 999L;
        MoldEntity updateData = new MoldEntity();

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> moldService.updateMold(moldId, updateData))
                .isInstanceOf(BusinessException.class);
        verify(moldRepository, never()).save(any(MoldEntity.class));
    }

    // ================== 상태 변경 테스트 ==================

    @Test
    @DisplayName("금형 상태 변경 - 성공")
    void testChangeStatus_Success() {
        // Given
        Long moldId = 1L;
        String newStatus = "MAINTENANCE";

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> {
                    MoldEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo(newStatus);
                    return saved;
                });

        // When
        MoldEntity result = moldService.changeStatus(moldId, newStatus);

        // Then
        assertThat(result).isNotNull();
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    @Test
    @DisplayName("금형 활성화 - 성공")
    void testActivate_Success() {
        // Given
        Long moldId = 1L;
        testMold.setIsActive(false);

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> {
                    MoldEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });

        // When
        MoldEntity result = moldService.activate(moldId);

        // Then
        assertThat(result).isNotNull();
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    @Test
    @DisplayName("금형 비활성화 - 성공")
    void testDeactivate_Success() {
        // Given
        Long moldId = 1L;

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> {
                    MoldEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });

        // When
        MoldEntity result = moldService.deactivate(moldId);

        // Then
        assertThat(result).isNotNull();
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    // ================== 샷 카운트 관리 테스트 ==================

    @Test
    @DisplayName("샷 카운트 리셋 - 성공")
    void testResetShotCount_Success() {
        // Given
        Long moldId = 1L;

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));
        when(moldRepository.save(any(MoldEntity.class)))
                .thenAnswer(invocation -> {
                    MoldEntity saved = invocation.getArgument(0);
                    assertThat(saved.getCurrentShotCount()).isEqualTo(0L);
                    assertThat(saved.getLastMaintenanceShot()).isEqualTo(0L);
                    return saved;
                });

        // When
        MoldEntity result = moldService.resetShotCount(moldId);

        // Then
        assertThat(result).isNotNull();
        verify(moldRepository, times(1)).save(any(MoldEntity.class));
    }

    @Test
    @DisplayName("보수 필요 여부 체크 - 보수 필요")
    void testIsMaintenanceRequired_True() {
        // Given
        Long moldId = 1L;
        testMold.setCurrentShotCount(60000L);
        testMold.setLastMaintenanceShot(0L);
        testMold.setMaintenanceShotInterval(50000L);

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));

        // When
        boolean result = moldService.isMaintenanceRequired(moldId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("보수 필요 여부 체크 - 보수 불필요")
    void testIsMaintenanceRequired_False() {
        // Given
        Long moldId = 1L;
        testMold.setCurrentShotCount(30000L);
        testMold.setLastMaintenanceShot(0L);
        testMold.setMaintenanceShotInterval(50000L);

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));

        // When
        boolean result = moldService.isMaintenanceRequired(moldId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("보수 필요 여부 체크 - 보수 주기 없음")
    void testIsMaintenanceRequired_NoInterval() {
        // Given
        Long moldId = 1L;
        testMold.setMaintenanceShotInterval(null);

        when(moldRepository.findByIdWithAllRelations(moldId))
                .thenReturn(Optional.of(testMold));

        // When
        boolean result = moldService.isMaintenanceRequired(moldId);

        // Then
        assertThat(result).isFalse();
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("금형 삭제 - 성공")
    void testDeleteMold_Success() {
        // Given
        Long moldId = 1L;

        when(moldRepository.findById(moldId))
                .thenReturn(Optional.of(testMold));
        doNothing().when(moldRepository).delete(testMold);

        // When
        moldService.deleteMold(moldId);

        // Then
        verify(moldRepository, times(1)).findById(moldId);
        verify(moldRepository, times(1)).delete(testMold);
    }

    @Test
    @DisplayName("금형 삭제 - 실패 (없음)")
    void testDeleteMold_Fail_NotFound() {
        // Given
        Long moldId = 999L;

        when(moldRepository.findById(moldId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> moldService.deleteMold(moldId))
                .isInstanceOf(BusinessException.class);
        verify(moldRepository, never()).delete(any(MoldEntity.class));
    }
}
