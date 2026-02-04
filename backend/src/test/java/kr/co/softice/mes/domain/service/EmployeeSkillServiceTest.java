package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
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
 * Employee Skill Service Test
 * 사원 스킬 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("사원 스킬 서비스 테스트")
class EmployeeSkillServiceTest {

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SkillMatrixRepository skillMatrixRepository;

    @InjectMocks
    private EmployeeSkillService employeeSkillService;

    private TenantEntity testTenant;
    private EmployeeEntity testEmployee;
    private SkillMatrixEntity testSkill;
    private EmployeeSkillEntity testEmployeeSkill;
    private Long employeeSkillId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        employeeSkillId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testEmployee = new EmployeeEntity();
        testEmployee.setEmployeeId(1L);
        testEmployee.setEmployeeNo("EMP001");
        testEmployee.setFullName("John Doe");

        testSkill = new SkillMatrixEntity();
        testSkill.setSkillId(1L);
        testSkill.setSkillCode("SKILL001");
        testSkill.setSkillName("Injection Molding");
        testSkill.setValidityPeriodMonths(12);

        testEmployeeSkill = new EmployeeSkillEntity();
        testEmployeeSkill.setEmployeeSkillId(employeeSkillId);
        testEmployeeSkill.setTenant(testTenant);
        testEmployeeSkill.setEmployee(testEmployee);
        testEmployeeSkill.setSkill(testSkill);
        testEmployeeSkill.setSkillLevel("INTERMEDIATE");
        testEmployeeSkill.setSkillLevelNumeric(2);
        testEmployeeSkill.setAcquisitionDate(LocalDate.now().minusMonths(6));
        testEmployeeSkill.setIsActive(true);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("전체 사원 스킬 조회 - 성공")
    void testGetAllEmployeeSkills_Success() {
        List<EmployeeSkillEntity> skills = Arrays.asList(testEmployeeSkill);
        when(employeeSkillRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(skills);

        List<EmployeeSkillEntity> result = employeeSkillService.getAllEmployeeSkills(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkillLevel()).isEqualTo("INTERMEDIATE");
        verify(employeeSkillRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("사원 스킬 ID로 조회 - 성공")
    void testGetEmployeeSkillById_Success() {
        when(employeeSkillRepository.findByIdWithAllRelations(employeeSkillId))
                .thenReturn(Optional.of(testEmployeeSkill));

        EmployeeSkillEntity result = employeeSkillService.getEmployeeSkillById(employeeSkillId);

        assertThat(result).isNotNull();
        assertThat(result.getSkillLevel()).isEqualTo("INTERMEDIATE");
        verify(employeeSkillRepository).findByIdWithAllRelations(employeeSkillId);
    }

    @Test
    @DisplayName("사원 스킬 ID로 조회 - 실패 (존재하지 않음)")
    void testGetEmployeeSkillById_Fail_NotFound() {
        when(employeeSkillRepository.findByIdWithAllRelations(employeeSkillId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeSkillService.getEmployeeSkillById(employeeSkillId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPLOYEE_SKILL_NOT_FOUND);
    }

    @Test
    @DisplayName("사원별 스킬 조회 - 성공")
    void testGetSkillsByEmployee_Success() {
        List<EmployeeSkillEntity> skills = Arrays.asList(testEmployeeSkill);
        when(employeeSkillRepository.findByEmployeeId(testEmployee.getEmployeeId()))
                .thenReturn(skills);

        List<EmployeeSkillEntity> result = employeeSkillService.getSkillsByEmployee(testEmployee.getEmployeeId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(employeeSkillRepository).findByEmployeeId(testEmployee.getEmployeeId());
    }

    @Test
    @DisplayName("스킬별 사원 조회 - 성공")
    void testGetEmployeesBySkill_Success() {
        List<EmployeeSkillEntity> skills = Arrays.asList(testEmployeeSkill);
        when(employeeSkillRepository.findByTenantIdAndSkillId(tenantId, testSkill.getSkillId()))
                .thenReturn(skills);

        List<EmployeeSkillEntity> result = employeeSkillService.getEmployeesBySkill(tenantId, testSkill.getSkillId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(employeeSkillRepository).findByTenantIdAndSkillId(tenantId, testSkill.getSkillId());
    }

    @Test
    @DisplayName("스킬 및 레벨별 사원 조회 - 성공")
    void testGetEmployeesBySkillAndLevel_Success() {
        List<EmployeeSkillEntity> skills = Arrays.asList(testEmployeeSkill);
        when(employeeSkillRepository.findByTenantIdAndSkillIdAndMinLevel(tenantId, testSkill.getSkillId(), 2))
                .thenReturn(skills);

        List<EmployeeSkillEntity> result = employeeSkillService.getEmployeesBySkillAndLevel(tenantId, testSkill.getSkillId(), 2);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkillLevelNumeric()).isGreaterThanOrEqualTo(2);
        verify(employeeSkillRepository).findByTenantIdAndSkillIdAndMinLevel(tenantId, testSkill.getSkillId(), 2);
    }

    @Test
    @DisplayName("만료 예정 인증 조회 - 성공")
    void testGetExpiringCertifications_Success() {
        LocalDate expiryDate = LocalDate.now().plusMonths(1);
        testEmployeeSkill.setExpiryDate(expiryDate);

        List<EmployeeSkillEntity> skills = Arrays.asList(testEmployeeSkill);
        when(employeeSkillRepository.findExpiringCertifications(tenantId, expiryDate))
                .thenReturn(skills);

        List<EmployeeSkillEntity> result = employeeSkillService.getExpiringCertifications(tenantId, expiryDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(employeeSkillRepository).findExpiringCertifications(tenantId, expiryDate);
    }

    @Test
    @DisplayName("평가 예정 조회 - 성공")
    void testGetPendingAssessments_Success() {
        LocalDate assessmentDate = LocalDate.now().plusWeeks(2);
        testEmployeeSkill.setNextAssessmentDate(assessmentDate);

        List<EmployeeSkillEntity> skills = Arrays.asList(testEmployeeSkill);
        when(employeeSkillRepository.findPendingAssessments(tenantId, assessmentDate))
                .thenReturn(skills);

        List<EmployeeSkillEntity> result = employeeSkillService.getPendingAssessments(tenantId, assessmentDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(employeeSkillRepository).findPendingAssessments(tenantId, assessmentDate);
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("사원 스킬 생성 - 성공 (기본)")
    void testCreateEmployeeSkill_Success() {
        EmployeeSkillEntity newSkill = new EmployeeSkillEntity();
        newSkill.setEmployee(testEmployee);
        newSkill.setSkill(testSkill);
        newSkill.setSkillLevel("INTERMEDIATE");
        newSkill.setAcquisitionDate(LocalDate.now());

        when(employeeSkillRepository.existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(
                tenantId, testEmployee.getEmployeeId(), testSkill.getSkillId()))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(employeeRepository.findByIdWithAllRelations(testEmployee.getEmployeeId()))
                .thenReturn(Optional.of(testEmployee));
        when(skillMatrixRepository.findByIdWithAllRelations(testSkill.getSkillId()))
                .thenReturn(Optional.of(testSkill));
        when(employeeSkillRepository.save(any(EmployeeSkillEntity.class)))
                .thenAnswer(invocation -> {
                    EmployeeSkillEntity saved = invocation.getArgument(0);
                    assertThat(saved.getSkillLevelNumeric()).isEqualTo(2); // INTERMEDIATE = 2
                    assertThat(saved.getIsActive()).isTrue(); // Default
                    return saved;
                });

        EmployeeSkillEntity result = employeeSkillService.createEmployeeSkill(tenantId, newSkill);

        assertThat(result).isNotNull();
        verify(employeeSkillRepository).save(any(EmployeeSkillEntity.class));
    }

    @Test
    @DisplayName("사원 스킬 생성 - 성공 (자동 만료일 계산)")
    void testCreateEmployeeSkill_Success_AutoExpiryDate() {
        LocalDate acquisitionDate = LocalDate.now();
        LocalDate expectedExpiryDate = acquisitionDate.plusMonths(12); // testSkill has 12 months validity

        EmployeeSkillEntity newSkill = new EmployeeSkillEntity();
        newSkill.setEmployee(testEmployee);
        newSkill.setSkill(testSkill);
        newSkill.setSkillLevel("ADVANCED");
        newSkill.setAcquisitionDate(acquisitionDate);

        when(employeeSkillRepository.existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(
                tenantId, testEmployee.getEmployeeId(), testSkill.getSkillId()))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(employeeRepository.findByIdWithAllRelations(testEmployee.getEmployeeId()))
                .thenReturn(Optional.of(testEmployee));
        when(skillMatrixRepository.findByIdWithAllRelations(testSkill.getSkillId()))
                .thenReturn(Optional.of(testSkill));
        when(employeeSkillRepository.save(any(EmployeeSkillEntity.class)))
                .thenAnswer(invocation -> {
                    EmployeeSkillEntity saved = invocation.getArgument(0);
                    assertThat(saved.getExpiryDate()).isEqualTo(expectedExpiryDate);
                    assertThat(saved.getSkillLevelNumeric()).isEqualTo(3); // ADVANCED = 3
                    return saved;
                });

        EmployeeSkillEntity result = employeeSkillService.createEmployeeSkill(tenantId, newSkill);

        assertThat(result).isNotNull();
        verify(employeeSkillRepository).save(any(EmployeeSkillEntity.class));
    }

    @Test
    @DisplayName("사원 스킬 생성 - 스킬 레벨 변환 테스트 (모든 레벨)")
    void testCreateEmployeeSkill_SkillLevelConversion() {
        String[] levels = {"BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT", "MASTER"};
        Integer[] expectedNumerics = {1, 2, 3, 4, 5};

        for (int i = 0; i < levels.length; i++) {
            final String level = levels[i];
            final Integer expectedNumeric = expectedNumerics[i];

            EmployeeSkillEntity newSkill = new EmployeeSkillEntity();
            newSkill.setEmployee(testEmployee);
            newSkill.setSkill(testSkill);
            newSkill.setSkillLevel(level);

            when(employeeSkillRepository.existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(
                    anyString(), anyLong(), anyLong()))
                    .thenReturn(false);
            when(tenantRepository.findById(tenantId))
                    .thenReturn(Optional.of(testTenant));
            when(employeeRepository.findByIdWithAllRelations(testEmployee.getEmployeeId()))
                    .thenReturn(Optional.of(testEmployee));
            when(skillMatrixRepository.findByIdWithAllRelations(testSkill.getSkillId()))
                    .thenReturn(Optional.of(testSkill));
            when(employeeSkillRepository.save(any(EmployeeSkillEntity.class)))
                    .thenAnswer(invocation -> {
                        EmployeeSkillEntity saved = invocation.getArgument(0);
                        assertThat(saved.getSkillLevelNumeric()).isEqualTo(expectedNumeric);
                        return saved;
                    });

            employeeSkillService.createEmployeeSkill(tenantId, newSkill);
        }

        verify(employeeSkillRepository, times(5)).save(any(EmployeeSkillEntity.class));
    }

    @Test
    @DisplayName("사원 스킬 생성 - 실패 (중복)")
    void testCreateEmployeeSkill_Fail_Duplicate() {
        EmployeeSkillEntity newSkill = new EmployeeSkillEntity();
        newSkill.setEmployee(testEmployee);
        newSkill.setSkill(testSkill);

        when(employeeSkillRepository.existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(
                tenantId, testEmployee.getEmployeeId(), testSkill.getSkillId()))
                .thenReturn(true);

        assertThatThrownBy(() -> employeeSkillService.createEmployeeSkill(tenantId, newSkill))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPLOYEE_SKILL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("사원 스킬 생성 - 실패 (테넌트 없음)")
    void testCreateEmployeeSkill_Fail_TenantNotFound() {
        EmployeeSkillEntity newSkill = new EmployeeSkillEntity();
        newSkill.setEmployee(testEmployee);
        newSkill.setSkill(testSkill);

        when(employeeSkillRepository.existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(
                tenantId, testEmployee.getEmployeeId(), testSkill.getSkillId()))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeSkillService.createEmployeeSkill(tenantId, newSkill))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TENANT_NOT_FOUND);
    }

    @Test
    @DisplayName("사원 스킬 생성 - 실패 (사원 없음)")
    void testCreateEmployeeSkill_Fail_EmployeeNotFound() {
        EmployeeSkillEntity newSkill = new EmployeeSkillEntity();
        newSkill.setEmployee(testEmployee);
        newSkill.setSkill(testSkill);

        when(employeeSkillRepository.existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(
                tenantId, testEmployee.getEmployeeId(), testSkill.getSkillId()))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(employeeRepository.findByIdWithAllRelations(testEmployee.getEmployeeId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeSkillService.createEmployeeSkill(tenantId, newSkill))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPLOYEE_NOT_FOUND);
    }

    @Test
    @DisplayName("사원 스킬 생성 - 실패 (스킬 없음)")
    void testCreateEmployeeSkill_Fail_SkillNotFound() {
        EmployeeSkillEntity newSkill = new EmployeeSkillEntity();
        newSkill.setEmployee(testEmployee);
        newSkill.setSkill(testSkill);

        when(employeeSkillRepository.existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(
                tenantId, testEmployee.getEmployeeId(), testSkill.getSkillId()))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(employeeRepository.findByIdWithAllRelations(testEmployee.getEmployeeId()))
                .thenReturn(Optional.of(testEmployee));
        when(skillMatrixRepository.findByIdWithAllRelations(testSkill.getSkillId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeSkillService.createEmployeeSkill(tenantId, newSkill))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SKILL_NOT_FOUND);
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("사원 스킬 수정 - 성공 (스킬 레벨)")
    void testUpdateEmployeeSkill_Success_SkillLevel() {
        EmployeeSkillEntity updateData = new EmployeeSkillEntity();
        updateData.setSkillLevel("EXPERT");

        when(employeeSkillRepository.findByIdWithAllRelations(employeeSkillId))
                .thenReturn(Optional.of(testEmployeeSkill));
        when(employeeSkillRepository.save(any(EmployeeSkillEntity.class)))
                .thenAnswer(invocation -> {
                    EmployeeSkillEntity saved = invocation.getArgument(0);
                    assertThat(saved.getSkillLevel()).isEqualTo("EXPERT");
                    assertThat(saved.getSkillLevelNumeric()).isEqualTo(4); // EXPERT = 4
                    return saved;
                });

        EmployeeSkillEntity result = employeeSkillService.updateEmployeeSkill(employeeSkillId, updateData);

        assertThat(result).isNotNull();
        verify(employeeSkillRepository).save(testEmployeeSkill);
    }

    @Test
    @DisplayName("사원 스킬 수정 - 성공 (취득일자 변경 시 만료일 자동 계산)")
    void testUpdateEmployeeSkill_Success_RecalculateExpiryDate() {
        LocalDate newAcquisitionDate = LocalDate.now();
        LocalDate expectedExpiryDate = newAcquisitionDate.plusMonths(12);

        EmployeeSkillEntity updateData = new EmployeeSkillEntity();
        updateData.setAcquisitionDate(newAcquisitionDate);

        when(employeeSkillRepository.findByIdWithAllRelations(employeeSkillId))
                .thenReturn(Optional.of(testEmployeeSkill));
        when(employeeSkillRepository.save(any(EmployeeSkillEntity.class)))
                .thenAnswer(invocation -> {
                    EmployeeSkillEntity saved = invocation.getArgument(0);
                    assertThat(saved.getExpiryDate()).isEqualTo(expectedExpiryDate);
                    return saved;
                });

        EmployeeSkillEntity result = employeeSkillService.updateEmployeeSkill(employeeSkillId, updateData);

        assertThat(result).isNotNull();
        verify(employeeSkillRepository).save(testEmployeeSkill);
    }

    @Test
    @DisplayName("사원 스킬 수정 - 성공 (평가 정보)")
    void testUpdateEmployeeSkill_Success_AssessmentInfo() {
        EmployeeSkillEntity updateData = new EmployeeSkillEntity();
        updateData.setLastAssessmentDate(LocalDate.now());
        updateData.setNextAssessmentDate(LocalDate.now().plusMonths(6));
        updateData.setAssessorName("Senior Manager");
        updateData.setAssessmentScore(new BigDecimal("85"));
        updateData.setAssessmentResult("PASS");

        when(employeeSkillRepository.findByIdWithAllRelations(employeeSkillId))
                .thenReturn(Optional.of(testEmployeeSkill));
        when(employeeSkillRepository.save(any(EmployeeSkillEntity.class)))
                .thenReturn(testEmployeeSkill);

        EmployeeSkillEntity result = employeeSkillService.updateEmployeeSkill(employeeSkillId, updateData);

        assertThat(result).isNotNull();
        assertThat(testEmployeeSkill.getAssessorName()).isEqualTo("Senior Manager");
        assertThat(testEmployeeSkill.getAssessmentScore()).isEqualByComparingTo(new BigDecimal("85"));
        verify(employeeSkillRepository).save(testEmployeeSkill);
    }

    @Test
    @DisplayName("사원 스킬 수정 - 성공 (인증 정보)")
    void testUpdateEmployeeSkill_Success_CertificationInfo() {
        EmployeeSkillEntity updateData = new EmployeeSkillEntity();
        updateData.setCertificationNo("CERT-2024-001");
        updateData.setIssuingAuthority("National Certification Board");
        updateData.setExpiryDate(LocalDate.now().plusYears(3));

        when(employeeSkillRepository.findByIdWithAllRelations(employeeSkillId))
                .thenReturn(Optional.of(testEmployeeSkill));
        when(employeeSkillRepository.save(any(EmployeeSkillEntity.class)))
                .thenReturn(testEmployeeSkill);

        EmployeeSkillEntity result = employeeSkillService.updateEmployeeSkill(employeeSkillId, updateData);

        assertThat(result).isNotNull();
        assertThat(testEmployeeSkill.getCertificationNo()).isEqualTo("CERT-2024-001");
        assertThat(testEmployeeSkill.getIssuingAuthority()).isEqualTo("National Certification Board");
        verify(employeeSkillRepository).save(testEmployeeSkill);
    }

    @Test
    @DisplayName("사원 스킬 수정 - 실패 (존재하지 않음)")
    void testUpdateEmployeeSkill_Fail_NotFound() {
        EmployeeSkillEntity updateData = new EmployeeSkillEntity();
        updateData.setSkillLevel("EXPERT");

        when(employeeSkillRepository.findByIdWithAllRelations(employeeSkillId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeSkillService.updateEmployeeSkill(employeeSkillId, updateData))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPLOYEE_SKILL_NOT_FOUND);
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("사원 스킬 삭제 - 성공")
    void testDeleteEmployeeSkill_Success() {
        when(employeeSkillRepository.findById(employeeSkillId))
                .thenReturn(Optional.of(testEmployeeSkill));

        employeeSkillService.deleteEmployeeSkill(employeeSkillId);

        verify(employeeSkillRepository).delete(testEmployeeSkill);
    }

    @Test
    @DisplayName("사원 스킬 삭제 - 실패 (존재하지 않음)")
    void testDeleteEmployeeSkill_Fail_NotFound() {
        when(employeeSkillRepository.findById(employeeSkillId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeSkillService.deleteEmployeeSkill(employeeSkillId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPLOYEE_SKILL_NOT_FOUND);
    }

    // === 통계 테스트 ===

    @Test
    @DisplayName("사원별 스킬 수 조회 - 성공")
    void testCountSkillsByEmployee_Success() {
        when(employeeSkillRepository.countByEmployeeId(testEmployee.getEmployeeId()))
                .thenReturn(5L);

        Long result = employeeSkillService.countSkillsByEmployee(testEmployee.getEmployeeId());

        assertThat(result).isEqualTo(5L);
        verify(employeeSkillRepository).countByEmployeeId(testEmployee.getEmployeeId());
    }
}
