package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.ApprovalLineEntity;
import kr.co.softice.mes.domain.entity.DepartmentEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.ApprovalLineRepository;
import kr.co.softice.mes.domain.repository.DepartmentRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Approval Line Service Test
 */
@ExtendWith(MockitoExtension.class)
class ApprovalLineServiceTest {

    @Mock
    private ApprovalLineRepository approvalLineRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ApprovalLineService approvalLineService;

    private TenantEntity testTenant;
    private DepartmentEntity testDepartment;
    private ApprovalLineEntity testApprovalLine;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("Test Tenant");

        // 테스트 부서
        testDepartment = new DepartmentEntity();
        testDepartment.setDepartmentId(1L);
        testDepartment.setDepartmentCode("DEPT-001");
        testDepartment.setDepartmentName("Test Department");

        // 테스트 승인 라인
        testApprovalLine = new ApprovalLineEntity();
        testApprovalLine.setApprovalLineId(1L);
        testApprovalLine.setLineCode("AL-001");
        testApprovalLine.setLineName("Standard Approval Line");
        testApprovalLine.setDocumentType("PURCHASE_REQUEST");
        testApprovalLine.setApprovalSteps("Team Leader → Department Manager → Division Head");
        testApprovalLine.setPriority(1);
        testApprovalLine.setIsActive(true);
        testApprovalLine.setIsDefault(true);
        testApprovalLine.setTenant(testTenant);
        testApprovalLine.setDepartment(testDepartment);
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("승인 라인 조회 - 전체 조회 성공")
    void testGetAllApprovalLinesByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<ApprovalLineEntity> expectedList = Arrays.asList(testApprovalLine);

        when(approvalLineRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<ApprovalLineEntity> result = approvalLineService.getAllApprovalLinesByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(approvalLineRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("승인 라인 조회 - ID로 조회 성공")
    void testGetApprovalLineById_Success() {
        // Given
        Long approvalLineId = 1L;

        when(approvalLineRepository.findByIdWithAllRelations(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));

        // When
        ApprovalLineEntity result = approvalLineService.getApprovalLineById(approvalLineId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getApprovalLineId()).isEqualTo(approvalLineId);
        verify(approvalLineRepository, times(1)).findByIdWithAllRelations(approvalLineId);
    }

    @Test
    @DisplayName("승인 라인 조회 - ID로 조회 실패 (없음)")
    void testGetApprovalLineById_Fail_NotFound() {
        // Given
        Long approvalLineId = 999L;

        when(approvalLineRepository.findByIdWithAllRelations(approvalLineId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> approvalLineService.getApprovalLineById(approvalLineId))
                .isInstanceOf(IllegalArgumentException.class);
        verify(approvalLineRepository, times(1)).findByIdWithAllRelations(approvalLineId);
    }

    @Test
    @DisplayName("승인 라인 조회 - 활성 라인 조회 성공")
    void testGetActiveApprovalLines_Success() {
        // Given
        String tenantId = "TEST001";
        List<ApprovalLineEntity> expectedList = Arrays.asList(testApprovalLine);

        when(approvalLineRepository.findActiveApprovalLinesByTenantId(tenantId))
                .thenReturn(expectedList);

        // When
        List<ApprovalLineEntity> result = approvalLineService.getActiveApprovalLines(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(approvalLineRepository, times(1)).findActiveApprovalLinesByTenantId(tenantId);
    }

    @Test
    @DisplayName("승인 라인 조회 - 문서 타입별 조회 성공")
    void testGetApprovalLinesByDocumentType_Success() {
        // Given
        String tenantId = "TEST001";
        String documentType = "PURCHASE_REQUEST";
        List<ApprovalLineEntity> expectedList = Arrays.asList(testApprovalLine);

        when(approvalLineRepository.findByTenantIdAndDocumentType(tenantId, documentType))
                .thenReturn(expectedList);

        // When
        List<ApprovalLineEntity> result = approvalLineService.getApprovalLinesByDocumentType(tenantId, documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocumentType()).isEqualTo(documentType);
        verify(approvalLineRepository, times(1)).findByTenantIdAndDocumentType(tenantId, documentType);
    }

    @Test
    @DisplayName("승인 라인 조회 - 부서별 조회 성공")
    void testGetApprovalLinesByDepartment_Success() {
        // Given
        String tenantId = "TEST001";
        Long departmentId = 1L;
        List<ApprovalLineEntity> expectedList = Arrays.asList(testApprovalLine);

        when(approvalLineRepository.findByTenantIdAndDepartmentId(tenantId, departmentId))
                .thenReturn(expectedList);

        // When
        List<ApprovalLineEntity> result = approvalLineService.getApprovalLinesByDepartment(tenantId, departmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(approvalLineRepository, times(1)).findByTenantIdAndDepartmentId(tenantId, departmentId);
    }

    @Test
    @DisplayName("승인 라인 조회 - 기본 라인 조회 성공")
    void testGetDefaultApprovalLine_Success() {
        // Given
        String tenantId = "TEST001";
        String documentType = "PURCHASE_REQUEST";

        when(approvalLineRepository.findDefaultByTenantIdAndDocumentType(tenantId, documentType))
                .thenReturn(Optional.of(testApprovalLine));

        // When
        ApprovalLineEntity result = approvalLineService.getDefaultApprovalLine(tenantId, documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue();
        verify(approvalLineRepository, times(1)).findDefaultByTenantIdAndDocumentType(tenantId, documentType);
    }

    @Test
    @DisplayName("승인 라인 조회 - 기본 라인 조회 실패 (없음)")
    void testGetDefaultApprovalLine_NotFound() {
        // Given
        String tenantId = "TEST001";
        String documentType = "UNKNOWN_TYPE";

        when(approvalLineRepository.findDefaultByTenantIdAndDocumentType(tenantId, documentType))
                .thenReturn(Optional.empty());

        // When
        ApprovalLineEntity result = approvalLineService.getDefaultApprovalLine(tenantId, documentType);

        // Then
        assertThat(result).isNull();
        verify(approvalLineRepository, times(1)).findDefaultByTenantIdAndDocumentType(tenantId, documentType);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("승인 라인 생성 - 성공 (전체 정보)")
    void testCreateApprovalLine_Success_FullInfo() {
        // Given
        ApprovalLineEntity newLine = new ApprovalLineEntity();
        newLine.setLineCode("AL-002");
        newLine.setLineName("New Approval Line");
        newLine.setDocumentType("PURCHASE_ORDER");
        newLine.setApprovalSteps("Manager → Director");

        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId("TEST001");
        newLine.setTenant(tenant);

        DepartmentEntity department = new DepartmentEntity();
        department.setDepartmentId(1L);
        newLine.setDepartment(department);

        when(approvalLineRepository.existsByTenant_TenantIdAndLineCode("TEST001", "AL-002"))
                .thenReturn(false);
        when(tenantRepository.findById("TEST001"))
                .thenReturn(Optional.of(testTenant));
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(testDepartment));
        when(approvalLineRepository.save(any(ApprovalLineEntity.class)))
                .thenAnswer(invocation -> {
                    ApprovalLineEntity saved = invocation.getArgument(0);
                    saved.setApprovalLineId(2L);
                    assertThat(saved.getIsActive()).isTrue();
                    assertThat(saved.getIsDefault()).isFalse();
                    assertThat(saved.getPriority()).isEqualTo(0);
                    return saved;
                });
        when(approvalLineRepository.findByIdWithAllRelations(2L))
                .thenAnswer(invocation -> {
                    ApprovalLineEntity line = new ApprovalLineEntity();
                    line.setApprovalLineId(2L);
                    line.setLineCode("AL-002");
                    return Optional.of(line);
                });

        // When
        ApprovalLineEntity result = approvalLineService.createApprovalLine(newLine);

        // Then
        assertThat(result).isNotNull();
        verify(approvalLineRepository, times(1)).save(any(ApprovalLineEntity.class));
        verify(approvalLineRepository, times(1)).findByIdWithAllRelations(2L);
    }

    @Test
    @DisplayName("승인 라인 생성 - 성공 (기본값으로 설정)")
    void testCreateApprovalLine_Success_SetAsDefault() {
        // Given
        ApprovalLineEntity newLine = new ApprovalLineEntity();
        newLine.setLineCode("AL-003");
        newLine.setDocumentType("PURCHASE_REQUEST");
        newLine.setIsDefault(true);

        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId("TEST001");
        newLine.setTenant(tenant);

        when(approvalLineRepository.existsByTenant_TenantIdAndLineCode("TEST001", "AL-003"))
                .thenReturn(false);
        when(tenantRepository.findById("TEST001"))
                .thenReturn(Optional.of(testTenant));
        when(approvalLineRepository.findDefaultByTenantIdAndDocumentType("TEST001", "PURCHASE_REQUEST"))
                .thenReturn(Optional.of(testApprovalLine));
        when(approvalLineRepository.save(any(ApprovalLineEntity.class)))
                .thenAnswer(invocation -> {
                    ApprovalLineEntity saved = invocation.getArgument(0);
                    saved.setApprovalLineId(3L);
                    return saved;
                });
        when(approvalLineRepository.findByIdWithAllRelations(3L))
                .thenAnswer(invocation -> {
                    ApprovalLineEntity line = new ApprovalLineEntity();
                    line.setApprovalLineId(3L);
                    line.setLineCode("AL-003");
                    return Optional.of(line);
                });

        // When
        ApprovalLineEntity result = approvalLineService.createApprovalLine(newLine);

        // Then
        assertThat(result).isNotNull();
        verify(approvalLineRepository, times(2)).save(any(ApprovalLineEntity.class)); // Old default + new line
    }

    @Test
    @DisplayName("승인 라인 생성 - 실패 (중복)")
    void testCreateApprovalLine_Fail_Duplicate() {
        // Given
        ApprovalLineEntity newLine = new ApprovalLineEntity();
        newLine.setLineCode("AL-001");

        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId("TEST001");
        newLine.setTenant(tenant);

        when(approvalLineRepository.existsByTenant_TenantIdAndLineCode("TEST001", "AL-001"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> approvalLineService.createApprovalLine(newLine))
                .isInstanceOf(IllegalArgumentException.class);
        verify(approvalLineRepository, never()).save(any(ApprovalLineEntity.class));
    }

    @Test
    @DisplayName("승인 라인 생성 - 실패 (테넌트 없음)")
    void testCreateApprovalLine_Fail_TenantNotFound() {
        // Given
        ApprovalLineEntity newLine = new ApprovalLineEntity();
        newLine.setLineCode("AL-002");

        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId("TEST999");
        newLine.setTenant(tenant);

        when(approvalLineRepository.existsByTenant_TenantIdAndLineCode("TEST999", "AL-002"))
                .thenReturn(false);
        when(tenantRepository.findById("TEST999"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> approvalLineService.createApprovalLine(newLine))
                .isInstanceOf(IllegalArgumentException.class);
        verify(approvalLineRepository, never()).save(any(ApprovalLineEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("승인 라인 수정 - 성공")
    void testUpdateApprovalLine_Success() {
        // Given
        Long approvalLineId = 1L;
        ApprovalLineEntity updateData = new ApprovalLineEntity();
        updateData.setLineName("Updated Approval Line");
        updateData.setDocumentType("PURCHASE_ORDER");
        updateData.setApprovalSteps("New Steps");
        updateData.setPriority(5);
        updateData.setRemarks("Updated remarks");

        when(approvalLineRepository.findById(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));
        when(approvalLineRepository.save(any(ApprovalLineEntity.class)))
                .thenReturn(testApprovalLine);
        when(approvalLineRepository.findByIdWithAllRelations(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));

        // When
        ApprovalLineEntity result = approvalLineService.updateApprovalLine(approvalLineId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(approvalLineRepository, times(1)).save(any(ApprovalLineEntity.class));
        verify(approvalLineRepository, times(1)).findByIdWithAllRelations(approvalLineId);
    }

    @Test
    @DisplayName("승인 라인 수정 - 기본값으로 설정")
    void testUpdateApprovalLine_Success_SetAsDefault() {
        // Given
        Long approvalLineId = 1L;
        testApprovalLine.setIsDefault(false); // Initially not default

        ApprovalLineEntity updateData = new ApprovalLineEntity();
        updateData.setLineName("Updated Line");
        updateData.setDocumentType("PURCHASE_REQUEST");
        updateData.setApprovalSteps("Steps");
        updateData.setIsDefault(true);

        ApprovalLineEntity currentDefault = new ApprovalLineEntity();
        currentDefault.setApprovalLineId(2L);
        currentDefault.setIsDefault(true);

        when(approvalLineRepository.findById(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));
        when(approvalLineRepository.findDefaultByTenantIdAndDocumentType(
                testApprovalLine.getTenant().getTenantId(),
                testApprovalLine.getDocumentType()))
                .thenReturn(Optional.of(currentDefault));
        when(approvalLineRepository.save(any(ApprovalLineEntity.class)))
                .thenReturn(testApprovalLine);
        when(approvalLineRepository.findByIdWithAllRelations(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));

        // When
        ApprovalLineEntity result = approvalLineService.updateApprovalLine(approvalLineId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(approvalLineRepository, times(2)).save(any(ApprovalLineEntity.class)); // Old default + updated line
    }

    @Test
    @DisplayName("승인 라인 수정 - 실패 (없음)")
    void testUpdateApprovalLine_Fail_NotFound() {
        // Given
        Long approvalLineId = 999L;
        ApprovalLineEntity updateData = new ApprovalLineEntity();

        when(approvalLineRepository.findById(approvalLineId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> approvalLineService.updateApprovalLine(approvalLineId, updateData))
                .isInstanceOf(IllegalArgumentException.class);
        verify(approvalLineRepository, never()).save(any(ApprovalLineEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("승인 라인 삭제 - 성공")
    void testDeleteApprovalLine_Success() {
        // Given
        Long approvalLineId = 1L;

        when(approvalLineRepository.findById(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));
        doNothing().when(approvalLineRepository).delete(testApprovalLine);

        // When
        approvalLineService.deleteApprovalLine(approvalLineId);

        // Then
        verify(approvalLineRepository, times(1)).findById(approvalLineId);
        verify(approvalLineRepository, times(1)).delete(testApprovalLine);
    }

    @Test
    @DisplayName("승인 라인 삭제 - 실패 (없음)")
    void testDeleteApprovalLine_Fail_NotFound() {
        // Given
        Long approvalLineId = 999L;

        when(approvalLineRepository.findById(approvalLineId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> approvalLineService.deleteApprovalLine(approvalLineId))
                .isInstanceOf(IllegalArgumentException.class);
        verify(approvalLineRepository, never()).delete(any(ApprovalLineEntity.class));
    }

    // ================== 상태 전환 테스트 ==================

    @Test
    @DisplayName("활성 상태 토글 - 성공 (활성 → 비활성)")
    void testToggleActive_Success_ActiveToInactive() {
        // Given
        Long approvalLineId = 1L;
        testApprovalLine.setIsActive(true);

        when(approvalLineRepository.findById(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));
        when(approvalLineRepository.save(any(ApprovalLineEntity.class)))
                .thenReturn(testApprovalLine);
        when(approvalLineRepository.findByIdWithAllRelations(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));

        // When
        ApprovalLineEntity result = approvalLineService.toggleActive(approvalLineId);

        // Then
        assertThat(result).isNotNull();
        verify(approvalLineRepository, times(1)).save(any(ApprovalLineEntity.class));
        verify(approvalLineRepository, times(1)).findByIdWithAllRelations(approvalLineId);
    }

    @Test
    @DisplayName("활성 상태 토글 - 성공 (비활성 → 활성)")
    void testToggleActive_Success_InactiveToActive() {
        // Given
        Long approvalLineId = 1L;
        testApprovalLine.setIsActive(false);

        when(approvalLineRepository.findById(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));
        when(approvalLineRepository.save(any(ApprovalLineEntity.class)))
                .thenReturn(testApprovalLine);
        when(approvalLineRepository.findByIdWithAllRelations(approvalLineId))
                .thenReturn(Optional.of(testApprovalLine));

        // When
        ApprovalLineEntity result = approvalLineService.toggleActive(approvalLineId);

        // Then
        assertThat(result).isNotNull();
        verify(approvalLineRepository, times(1)).save(any(ApprovalLineEntity.class));
    }

    @Test
    @DisplayName("활성 상태 토글 - 실패 (없음)")
    void testToggleActive_Fail_NotFound() {
        // Given
        Long approvalLineId = 999L;

        when(approvalLineRepository.findById(approvalLineId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> approvalLineService.toggleActive(approvalLineId))
                .isInstanceOf(IllegalArgumentException.class);
        verify(approvalLineRepository, never()).save(any(ApprovalLineEntity.class));
    }
}
