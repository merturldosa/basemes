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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EquipmentService Unit Test
 *
 * 테스트 대상:
 * - 설비 CRUD
 * - 상태 변경
 * - 유지보수 기록
 * - 활성화/비활성화
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentService 단위 테스트")
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    private TenantEntity testTenant;
    private SiteEntity testSite;
    private DepartmentEntity testDepartment;
    private EquipmentEntity testEquipment;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        // 테스트 사이트
        testSite = new SiteEntity();
        testSite.setSiteId(1L);
        testSite.setSiteCode("SITE-001");
        testSite.setSiteName("본사");

        // 테스트 부서
        testDepartment = new DepartmentEntity();
        testDepartment.setDepartmentId(1L);
        testDepartment.setDepartmentCode("DEPT-001");
        testDepartment.setDepartmentName("생산부");

        // 테스트 설비
        testEquipment = new EquipmentEntity();
        testEquipment.setEquipmentId(1L);
        testEquipment.setEquipmentCode("EQ-001");
        testEquipment.setEquipmentName("사출기 1호");
        testEquipment.setEquipmentType("INJECTION");
        testEquipment.setEquipmentCategory("MOLDING");
        testEquipment.setManufacturer("ABC기계");
        testEquipment.setModelName("INJ-2000");
        testEquipment.setSerialNo("SN-123456");
        testEquipment.setLocation("1공장-A동");
        testEquipment.setCapacity("200톤");
        testEquipment.setPowerRating(new BigDecimal("50"));
        testEquipment.setWeight(new BigDecimal("5000"));
        testEquipment.setPurchasePrice(new BigDecimal("100000000"));
        testEquipment.setStatus("OPERATIONAL");
        testEquipment.setMaintenanceCycleDays(90);
        testEquipment.setLastMaintenanceDate(LocalDate.now().minusDays(30));
        testEquipment.setNextMaintenanceDate(LocalDate.now().plusDays(60));
        testEquipment.setIsActive(true);
        testEquipment.setTenant(testTenant);
        testEquipment.setSite(testSite);
        testEquipment.setDepartment(testDepartment);
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("설비 조회 - 전체 조회 성공")
    void testGetAllEquipments_Success() {
        // Given
        String tenantId = "TEST001";
        List<EquipmentEntity> expectedList = Arrays.asList(testEquipment);

        when(equipmentRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<EquipmentEntity> result = equipmentService.getAllEquipments(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEquipmentCode()).isEqualTo("EQ-001");
        verify(equipmentRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("설비 조회 - 활성 설비만 조회 성공")
    void testGetActiveEquipments_Success() {
        // Given
        String tenantId = "TEST001";
        List<EquipmentEntity> expectedList = Arrays.asList(testEquipment);

        when(equipmentRepository.findActiveByTenantId(tenantId))
                .thenReturn(expectedList);

        // When
        List<EquipmentEntity> result = equipmentService.getActiveEquipments(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(equipmentRepository, times(1)).findActiveByTenantId(tenantId);
    }

    @Test
    @DisplayName("설비 조회 - ID로 조회 성공")
    void testGetEquipmentById_Success() {
        // Given
        Long equipmentId = 1L;
        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.of(testEquipment));

        // When
        EquipmentEntity result = equipmentService.getEquipmentById(equipmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEquipmentId()).isEqualTo(equipmentId);
        verify(equipmentRepository, times(1)).findByIdWithAllRelations(equipmentId);
    }

    @Test
    @DisplayName("설비 조회 - ID로 조회 실패 (없음)")
    void testGetEquipmentById_Fail_NotFound() {
        // Given
        Long equipmentId = 999L;
        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> equipmentService.getEquipmentById(equipmentId))
                .isInstanceOf(BusinessException.class);

        verify(equipmentRepository, times(1)).findByIdWithAllRelations(equipmentId);
    }

    @Test
    @DisplayName("설비 조회 - 상태별 조회 성공")
    void testGetEquipmentsByStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String status = "OPERATIONAL";
        List<EquipmentEntity> expectedList = Arrays.asList(testEquipment);

        when(equipmentRepository.findByTenantIdAndStatus(tenantId, status))
                .thenReturn(expectedList);

        // When
        List<EquipmentEntity> result = equipmentService.getEquipmentsByStatus(tenantId, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);
        verify(equipmentRepository, times(1)).findByTenantIdAndStatus(tenantId, status);
    }

    @Test
    @DisplayName("설비 조회 - 타입별 조회 성공")
    void testGetEquipmentsByType_Success() {
        // Given
        String tenantId = "TEST001";
        String equipmentType = "INJECTION";
        List<EquipmentEntity> expectedList = Arrays.asList(testEquipment);

        when(equipmentRepository.findByTenantIdAndEquipmentType(tenantId, equipmentType))
                .thenReturn(expectedList);

        // When
        List<EquipmentEntity> result = equipmentService.getEquipmentsByType(tenantId, equipmentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEquipmentType()).isEqualTo(equipmentType);
        verify(equipmentRepository, times(1)).findByTenantIdAndEquipmentType(tenantId, equipmentType);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("설비 생성 - 성공 (전체 필드)")
    void testCreateEquipment_Success() {
        // Given
        String tenantId = "TEST001";

        when(equipmentRepository.existsByTenant_TenantIdAndEquipmentCode(tenantId, testEquipment.getEquipmentCode()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(siteRepository.findByIdWithAllRelations(testSite.getSiteId()))
                .thenReturn(Optional.of(testSite));

        when(departmentRepository.findByIdWithAllRelations(testDepartment.getDepartmentId()))
                .thenReturn(Optional.of(testDepartment));

        when(equipmentRepository.save(any(EquipmentEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentEntity saved = invocation.getArgument(0);
                    assertThat(saved.getTenant()).isEqualTo(testTenant);
                    assertThat(saved.getSite()).isEqualTo(testSite);
                    assertThat(saved.getDepartment()).isEqualTo(testDepartment);
                    assertThat(saved.getNextMaintenanceDate()).isNotNull();
                    return saved;
                });

        // When
        EquipmentEntity result = equipmentService.createEquipment(tenantId, testEquipment);

        // Then
        assertThat(result).isNotNull();
        verify(equipmentRepository, times(1)).save(any(EquipmentEntity.class));
    }

    @Test
    @DisplayName("설비 생성 - 성공 (기본값 설정)")
    void testCreateEquipment_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        EquipmentEntity minimalEquipment = new EquipmentEntity();
        minimalEquipment.setEquipmentCode("EQ-002");
        minimalEquipment.setEquipmentName("테스트 설비");

        when(equipmentRepository.existsByTenant_TenantIdAndEquipmentCode(tenantId, minimalEquipment.getEquipmentCode()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.save(any(EquipmentEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentEntity saved = invocation.getArgument(0);
                    // 기본값 검증
                    assertThat(saved.getPurchasePrice()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getPowerRating()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getWeight()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getIsActive()).isTrue();
                    assertThat(saved.getStatus()).isEqualTo("OPERATIONAL");
                    return saved;
                });

        // When
        EquipmentEntity result = equipmentService.createEquipment(tenantId, minimalEquipment);

        // Then
        assertThat(result).isNotNull();
        verify(equipmentRepository, times(1)).save(any(EquipmentEntity.class));
    }

    @Test
    @DisplayName("설비 생성 - 실패 (중복 코드)")
    void testCreateEquipment_Fail_Duplicate() {
        // Given
        String tenantId = "TEST001";

        when(equipmentRepository.existsByTenant_TenantIdAndEquipmentCode(tenantId, testEquipment.getEquipmentCode()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> equipmentService.createEquipment(tenantId, testEquipment))
                .isInstanceOf(BusinessException.class);

        verify(equipmentRepository, never()).save(any(EquipmentEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("설비 수정 - 성공")
    void testUpdateEquipment_Success() {
        // Given
        Long equipmentId = 1L;
        EquipmentEntity updateData = new EquipmentEntity();
        updateData.setEquipmentName("수정된 설비명");
        updateData.setStatus("MAINTENANCE");
        updateData.setRemarks("정기 점검 중");

        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.of(testEquipment));

        when(equipmentRepository.save(any(EquipmentEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentEntity saved = invocation.getArgument(0);
                    assertThat(saved.getEquipmentName()).isEqualTo("수정된 설비명");
                    assertThat(saved.getStatus()).isEqualTo("MAINTENANCE");
                    assertThat(saved.getRemarks()).isEqualTo("정기 점검 중");
                    return saved;
                });

        // When
        EquipmentEntity result = equipmentService.updateEquipment(equipmentId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(equipmentRepository, times(1)).findByIdWithAllRelations(equipmentId);
        verify(equipmentRepository, times(1)).save(any(EquipmentEntity.class));
    }

    @Test
    @DisplayName("설비 수정 - 실패 (설비 없음)")
    void testUpdateEquipment_Fail_NotFound() {
        // Given
        Long equipmentId = 999L;
        EquipmentEntity updateData = new EquipmentEntity();

        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> equipmentService.updateEquipment(equipmentId, updateData))
                .isInstanceOf(BusinessException.class);

        verify(equipmentRepository, never()).save(any(EquipmentEntity.class));
    }

    @Test
    @DisplayName("설비 수정 - 유지보수 주기 변경 시 날짜 재계산")
    void testUpdateEquipment_RecalculateMaintenanceDate() {
        // Given
        Long equipmentId = 1L;
        EquipmentEntity updateData = new EquipmentEntity();
        updateData.setMaintenanceCycleDays(120);  // 90일 -> 120일

        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.of(testEquipment));

        when(equipmentRepository.save(any(EquipmentEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentEntity saved = invocation.getArgument(0);
                    assertThat(saved.getMaintenanceCycleDays()).isEqualTo(120);
                    assertThat(saved.getNextMaintenanceDate()).isNotNull();
                    return saved;
                });

        // When
        EquipmentEntity result = equipmentService.updateEquipment(equipmentId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(equipmentRepository, times(1)).save(any(EquipmentEntity.class));
    }

    // ================== 상태 변경 테스트 ==================

    @Test
    @DisplayName("설비 상태 변경 - 성공")
    void testChangeStatus_Success() {
        // Given
        Long equipmentId = 1L;
        String newStatus = "MAINTENANCE";

        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.of(testEquipment));

        when(equipmentRepository.save(any(EquipmentEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo(newStatus);
                    return saved;
                });

        // When
        EquipmentEntity result = equipmentService.changeStatus(equipmentId, newStatus);

        // Then
        assertThat(result).isNotNull();
        verify(equipmentRepository, times(1)).findByIdWithAllRelations(equipmentId);
        verify(equipmentRepository, times(1)).save(any(EquipmentEntity.class));
    }

    // ================== 유지보수 기록 테스트 ==================

    @Test
    @DisplayName("유지보수 기록 - 성공 (다음 날짜 자동 계산)")
    void testRecordMaintenance_Success() {
        // Given
        Long equipmentId = 1L;
        LocalDate maintenanceDate = LocalDate.now();

        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.of(testEquipment));

        when(equipmentRepository.save(any(EquipmentEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentEntity saved = invocation.getArgument(0);
                    assertThat(saved.getLastMaintenanceDate()).isEqualTo(maintenanceDate);
                    assertThat(saved.getNextMaintenanceDate()).isEqualTo(maintenanceDate.plusDays(90));
                    return saved;
                });

        // When
        EquipmentEntity result = equipmentService.recordMaintenance(equipmentId, maintenanceDate);

        // Then
        assertThat(result).isNotNull();
        verify(equipmentRepository, times(1)).findByIdWithAllRelations(equipmentId);
        verify(equipmentRepository, times(1)).save(any(EquipmentEntity.class));
    }

    // ================== 활성화/비활성화 테스트 ==================

    @Test
    @DisplayName("설비 활성화 - 성공")
    void testActivate_Success() {
        // Given
        Long equipmentId = 1L;
        testEquipment.setIsActive(false);

        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.of(testEquipment));

        when(equipmentRepository.save(any(EquipmentEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });

        // When
        EquipmentEntity result = equipmentService.activate(equipmentId);

        // Then
        assertThat(result).isNotNull();
        verify(equipmentRepository, times(1)).findByIdWithAllRelations(equipmentId);
        verify(equipmentRepository, times(1)).save(any(EquipmentEntity.class));
    }

    @Test
    @DisplayName("설비 비활성화 - 성공")
    void testDeactivate_Success() {
        // Given
        Long equipmentId = 1L;
        testEquipment.setIsActive(true);

        when(equipmentRepository.findByIdWithAllRelations(equipmentId))
                .thenReturn(Optional.of(testEquipment));

        when(equipmentRepository.save(any(EquipmentEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });

        // When
        EquipmentEntity result = equipmentService.deactivate(equipmentId);

        // Then
        assertThat(result).isNotNull();
        verify(equipmentRepository, times(1)).findByIdWithAllRelations(equipmentId);
        verify(equipmentRepository, times(1)).save(any(EquipmentEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("설비 삭제 - 성공")
    void testDeleteEquipment_Success() {
        // Given
        Long equipmentId = 1L;

        when(equipmentRepository.findById(equipmentId))
                .thenReturn(Optional.of(testEquipment));

        doNothing().when(equipmentRepository).delete(testEquipment);

        // When
        equipmentService.deleteEquipment(equipmentId);

        // Then
        verify(equipmentRepository, times(1)).findById(equipmentId);
        verify(equipmentRepository, times(1)).delete(testEquipment);
    }

    @Test
    @DisplayName("설비 삭제 - 실패 (설비 없음)")
    void testDeleteEquipment_Fail_NotFound() {
        // Given
        Long equipmentId = 999L;

        when(equipmentRepository.findById(equipmentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> equipmentService.deleteEquipment(equipmentId))
                .isInstanceOf(BusinessException.class);

        verify(equipmentRepository, never()).delete(any(EquipmentEntity.class));
    }
}
