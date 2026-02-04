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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EquipmentInspectionService Unit Test
 *
 * 테스트 대상:
 * - 설비 점검 CRUD
 * - 총 비용 계산
 * - 다음 점검일 계산
 * - 점검 결과에 따른 설비 상태 변경
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentInspectionService 단위 테스트")
class EquipmentInspectionServiceTest {

    @Mock
    private EquipmentInspectionRepository inspectionRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private EquipmentService equipmentService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EquipmentInspectionService inspectionService;

    private TenantEntity testTenant;
    private EquipmentEntity testEquipment;
    private UserEntity testInspector;
    private EquipmentInspectionEntity testInspection;

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
        testEquipment.setMaintenanceCycleDays(90);
        testEquipment.setStatus("OPERATIONAL");

        // 테스트 점검자
        testInspector = new UserEntity();
        testInspector.setUserId(1L);
        testInspector.setUsername("inspector");
        testInspector.setFullName("점검 담당자");

        // 테스트 점검
        testInspection = new EquipmentInspectionEntity();
        testInspection.setInspectionId(1L);
        testInspection.setInspectionNo("INSP-2026-001");
        testInspection.setInspectionType("PERIODIC");
        testInspection.setInspectionDate(LocalDateTime.now());
        testInspection.setInspectionResult("PASS");
        testInspection.setAbnormalityDetected(false);
        testInspection.setPartsCost(new BigDecimal("50000"));
        testInspection.setLaborCost(new BigDecimal("30000"));
        testInspection.setTotalCost(new BigDecimal("80000"));
        testInspection.setIsActive(true);
        testInspection.setTenant(testTenant);
        testInspection.setEquipment(testEquipment);
        testInspection.setInspectorUser(testInspector);
        testInspection.setInspectorName(testInspector.getFullName());
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("점검 조회 - 전체 조회 성공")
    void testGetAllInspections_Success() {
        // Given
        String tenantId = "TEST001";
        List<EquipmentInspectionEntity> expectedList = Arrays.asList(testInspection);

        when(inspectionRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<EquipmentInspectionEntity> result = inspectionService.getAllInspections(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInspectionNo()).isEqualTo("INSP-2026-001");
        verify(inspectionRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("점검 조회 - ID로 조회 성공")
    void testGetInspectionById_Success() {
        // Given
        Long inspectionId = 1L;
        when(inspectionRepository.findByIdWithAllRelations(inspectionId))
                .thenReturn(Optional.of(testInspection));

        // When
        EquipmentInspectionEntity result = inspectionService.getInspectionById(inspectionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getInspectionId()).isEqualTo(inspectionId);
        verify(inspectionRepository, times(1)).findByIdWithAllRelations(inspectionId);
    }

    @Test
    @DisplayName("점검 조회 - ID로 조회 실패 (없음)")
    void testGetInspectionById_Fail_NotFound() {
        // Given
        Long inspectionId = 999L;
        when(inspectionRepository.findByIdWithAllRelations(inspectionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inspectionService.getInspectionById(inspectionId))
                .isInstanceOf(BusinessException.class);

        verify(inspectionRepository, times(1)).findByIdWithAllRelations(inspectionId);
    }

    @Test
    @DisplayName("점검 조회 - 설비별 조회 성공")
    void testGetInspectionsByEquipment_Success() {
        // Given
        Long equipmentId = 1L;
        List<EquipmentInspectionEntity> expectedList = Arrays.asList(testInspection);

        when(inspectionRepository.findByEquipmentId(equipmentId))
                .thenReturn(expectedList);

        // When
        List<EquipmentInspectionEntity> result = inspectionService.getInspectionsByEquipment(equipmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(inspectionRepository, times(1)).findByEquipmentId(equipmentId);
    }

    @Test
    @DisplayName("점검 조회 - 타입별 조회 성공")
    void testGetInspectionsByType_Success() {
        // Given
        String tenantId = "TEST001";
        String inspectionType = "PERIODIC";
        List<EquipmentInspectionEntity> expectedList = Arrays.asList(testInspection);

        when(inspectionRepository.findByTenantIdAndInspectionType(tenantId, inspectionType))
                .thenReturn(expectedList);

        // When
        List<EquipmentInspectionEntity> result = inspectionService.getInspectionsByType(tenantId, inspectionType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInspectionType()).isEqualTo(inspectionType);
        verify(inspectionRepository, times(1)).findByTenantIdAndInspectionType(tenantId, inspectionType);
    }

    @Test
    @DisplayName("점검 조회 - 결과별 조회 성공")
    void testGetInspectionsByResult_Success() {
        // Given
        String tenantId = "TEST001";
        String inspectionResult = "PASS";
        List<EquipmentInspectionEntity> expectedList = Arrays.asList(testInspection);

        when(inspectionRepository.findByTenantIdAndInspectionResult(tenantId, inspectionResult))
                .thenReturn(expectedList);

        // When
        List<EquipmentInspectionEntity> result = inspectionService.getInspectionsByResult(tenantId, inspectionResult);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInspectionResult()).isEqualTo(inspectionResult);
        verify(inspectionRepository, times(1)).findByTenantIdAndInspectionResult(tenantId, inspectionResult);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("점검 생성 - 성공 (총 비용 자동 계산)")
    void testCreateInspection_Success() {
        // Given
        String tenantId = "TEST001";

        when(inspectionRepository.existsByTenant_TenantIdAndInspectionNo(tenantId, testInspection.getInspectionNo()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(testEquipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(userRepository.findById(testInspector.getUserId()))
                .thenReturn(Optional.of(testInspector));

        when(inspectionRepository.save(any(EquipmentInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentInspectionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getTenant()).isEqualTo(testTenant);
                    assertThat(saved.getEquipment()).isEqualTo(testEquipment);
                    assertThat(saved.getInspectorName()).isEqualTo(testInspector.getFullName());
                    // 총 비용 자동 계산 확인
                    assertThat(saved.getTotalCost()).isEqualByComparingTo(new BigDecimal("80000"));
                    return saved;
                });

        when(equipmentService.recordMaintenance(anyLong(), any(LocalDate.class)))
                .thenReturn(testEquipment);

        // When
        EquipmentInspectionEntity result = inspectionService.createInspection(tenantId, testInspection);

        // Then
        assertThat(result).isNotNull();
        verify(inspectionRepository, times(1)).save(any(EquipmentInspectionEntity.class));
        verify(equipmentService, times(1)).recordMaintenance(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("점검 생성 - 성공 (기본값 설정)")
    void testCreateInspection_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        EquipmentInspectionEntity minimalInspection = new EquipmentInspectionEntity();
        minimalInspection.setInspectionNo("INSP-2026-002");
        minimalInspection.setInspectionType("DAILY");
        minimalInspection.setInspectionDate(LocalDateTime.now());

        EquipmentEntity equipment = new EquipmentEntity();
        equipment.setEquipmentId(1L);
        minimalInspection.setEquipment(equipment);

        when(inspectionRepository.existsByTenant_TenantIdAndInspectionNo(tenantId, minimalInspection.getInspectionNo()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(equipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(inspectionRepository.save(any(EquipmentInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentInspectionEntity saved = invocation.getArgument(0);
                    // 기본값 검증
                    assertThat(saved.getAbnormalityDetected()).isFalse();
                    assertThat(saved.getPartsCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getLaborCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getTotalCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getIsActive()).isTrue();
                    // 다음 점검일이 자동 설정됨 (DAILY는 내일)
                    assertThat(saved.getNextInspectionDate()).isEqualTo(LocalDate.now().plusDays(1));
                    assertThat(saved.getNextInspectionType()).isEqualTo("DAILY");
                    return saved;
                });

        // When
        EquipmentInspectionEntity result = inspectionService.createInspection(tenantId, minimalInspection);

        // Then
        assertThat(result).isNotNull();
        verify(inspectionRepository, times(1)).save(any(EquipmentInspectionEntity.class));
    }

    @Test
    @DisplayName("점검 생성 - 다음 점검일 계산 (정기점검)")
    void testCreateInspection_NextInspectionDate_Periodic() {
        // Given
        String tenantId = "TEST001";
        testInspection.setInspectionType("PERIODIC");
        testInspection.setNextInspectionDate(null); // 자동 계산되도록

        when(inspectionRepository.existsByTenant_TenantIdAndInspectionNo(tenantId, testInspection.getInspectionNo()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(testEquipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(userRepository.findById(testInspector.getUserId()))
                .thenReturn(Optional.of(testInspector));

        when(inspectionRepository.save(any(EquipmentInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentInspectionEntity saved = invocation.getArgument(0);
                    // 정기점검 -> 설비 유지보수 주기(90일) 후
                    assertThat(saved.getNextInspectionDate()).isEqualTo(LocalDate.now().plusDays(90));
                    assertThat(saved.getNextInspectionType()).isEqualTo("PERIODIC");
                    return saved;
                });

        when(equipmentService.recordMaintenance(anyLong(), any(LocalDate.class)))
                .thenReturn(testEquipment);

        // When
        inspectionService.createInspection(tenantId, testInspection);

        // Then
        verify(inspectionRepository, times(1)).save(any(EquipmentInspectionEntity.class));
    }

    @Test
    @DisplayName("점검 생성 - 실패 (중복 번호)")
    void testCreateInspection_Fail_Duplicate() {
        // Given
        String tenantId = "TEST001";

        when(inspectionRepository.existsByTenant_TenantIdAndInspectionNo(tenantId, testInspection.getInspectionNo()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> inspectionService.createInspection(tenantId, testInspection))
                .isInstanceOf(BusinessException.class);

        verify(inspectionRepository, never()).save(any(EquipmentInspectionEntity.class));
    }

    @Test
    @DisplayName("점검 생성 - FAIL 결과 시 설비 상태 변경 (고장점검)")
    void testCreateInspection_FailResult_BreakdownType() {
        // Given
        String tenantId = "TEST001";
        testInspection.setInspectionType("BREAKDOWN");
        testInspection.setInspectionResult("FAIL");

        when(inspectionRepository.existsByTenant_TenantIdAndInspectionNo(tenantId, testInspection.getInspectionNo()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(testEquipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(userRepository.findById(testInspector.getUserId()))
                .thenReturn(Optional.of(testInspector));

        when(inspectionRepository.save(any(EquipmentInspectionEntity.class)))
                .thenReturn(testInspection);

        when(equipmentService.changeStatus(testEquipment.getEquipmentId(), "BREAKDOWN"))
                .thenReturn(testEquipment);

        // When
        inspectionService.createInspection(tenantId, testInspection);

        // Then
        verify(equipmentService, times(1)).changeStatus(testEquipment.getEquipmentId(), "BREAKDOWN");
    }

    @Test
    @DisplayName("점검 생성 - PASS 결과 시 설비 상태 복구")
    void testCreateInspection_PassResult_StatusRestore() {
        // Given
        String tenantId = "TEST001";
        testEquipment.setStatus("MAINTENANCE"); // 점검 중 상태
        testInspection.setInspectionResult("PASS");

        when(inspectionRepository.existsByTenant_TenantIdAndInspectionNo(tenantId, testInspection.getInspectionNo()))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        when(equipmentRepository.findByIdWithAllRelations(testEquipment.getEquipmentId()))
                .thenReturn(Optional.of(testEquipment));

        when(userRepository.findById(testInspector.getUserId()))
                .thenReturn(Optional.of(testInspector));

        when(inspectionRepository.save(any(EquipmentInspectionEntity.class)))
                .thenReturn(testInspection);

        when(equipmentService.changeStatus(testEquipment.getEquipmentId(), "OPERATIONAL"))
                .thenReturn(testEquipment);

        when(equipmentService.recordMaintenance(anyLong(), any(LocalDate.class)))
                .thenReturn(testEquipment);

        // When
        inspectionService.createInspection(tenantId, testInspection);

        // Then
        verify(equipmentService, times(1)).changeStatus(testEquipment.getEquipmentId(), "OPERATIONAL");
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("점검 수정 - 성공 (총 비용 재계산)")
    void testUpdateInspection_Success() {
        // Given
        Long inspectionId = 1L;
        EquipmentInspectionEntity updateData = new EquipmentInspectionEntity();
        updateData.setPartsCost(new BigDecimal("60000"));
        updateData.setLaborCost(new BigDecimal("40000"));
        updateData.setFindings("수정된 발견사항");

        when(inspectionRepository.findByIdWithAllRelations(inspectionId))
                .thenReturn(Optional.of(testInspection));

        when(inspectionRepository.save(any(EquipmentInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentInspectionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getPartsCost()).isEqualByComparingTo(new BigDecimal("60000"));
                    assertThat(saved.getLaborCost()).isEqualByComparingTo(new BigDecimal("40000"));
                    // 총 비용 재계산 확인
                    assertThat(saved.getTotalCost()).isEqualByComparingTo(new BigDecimal("100000"));
                    assertThat(saved.getFindings()).isEqualTo("수정된 발견사항");
                    return saved;
                });

        // When
        EquipmentInspectionEntity result = inspectionService.updateInspection(inspectionId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(inspectionRepository, times(1)).findByIdWithAllRelations(inspectionId);
        verify(inspectionRepository, times(1)).save(any(EquipmentInspectionEntity.class));
    }

    @Test
    @DisplayName("점검 수정 - 실패 (점검 없음)")
    void testUpdateInspection_Fail_NotFound() {
        // Given
        Long inspectionId = 999L;
        EquipmentInspectionEntity updateData = new EquipmentInspectionEntity();

        when(inspectionRepository.findByIdWithAllRelations(inspectionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inspectionService.updateInspection(inspectionId, updateData))
                .isInstanceOf(BusinessException.class);

        verify(inspectionRepository, never()).save(any(EquipmentInspectionEntity.class));
    }

    // ================== 점검 완료 테스트 ==================

    @Test
    @DisplayName("점검 완료 - 성공")
    void testCompleteInspection_Success() {
        // Given
        Long inspectionId = 1L;

        when(inspectionRepository.findByIdWithAllRelations(inspectionId))
                .thenReturn(Optional.of(testInspection));

        when(inspectionRepository.save(any(EquipmentInspectionEntity.class)))
                .thenAnswer(invocation -> {
                    EquipmentInspectionEntity saved = invocation.getArgument(0);
                    assertThat(saved.getCorrectiveActionDate()).isNotNull();
                    return saved;
                });

        // When
        EquipmentInspectionEntity result = inspectionService.completeInspection(inspectionId);

        // Then
        assertThat(result).isNotNull();
        verify(inspectionRepository, times(1)).findByIdWithAllRelations(inspectionId);
        verify(inspectionRepository, times(1)).save(any(EquipmentInspectionEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("점검 삭제 - 성공")
    void testDeleteInspection_Success() {
        // Given
        Long inspectionId = 1L;

        when(inspectionRepository.findById(inspectionId))
                .thenReturn(Optional.of(testInspection));

        doNothing().when(inspectionRepository).delete(testInspection);

        // When
        inspectionService.deleteInspection(inspectionId);

        // Then
        verify(inspectionRepository, times(1)).findById(inspectionId);
        verify(inspectionRepository, times(1)).delete(testInspection);
    }

    @Test
    @DisplayName("점검 삭제 - 실패 (점검 없음)")
    void testDeleteInspection_Fail_NotFound() {
        // Given
        Long inspectionId = 999L;

        when(inspectionRepository.findById(inspectionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inspectionService.deleteInspection(inspectionId))
                .isInstanceOf(BusinessException.class);

        verify(inspectionRepository, never()).delete(any(EquipmentInspectionEntity.class));
    }
}
