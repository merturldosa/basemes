package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Employee Service Test
 * 사원 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("사원 서비스 테스트")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private TenantEntity testTenant;
    private UserEntity testUser;
    private SiteEntity testSite;
    private DepartmentEntity testDepartment;
    private EmployeeEntity testEmployee;
    private Long employeeId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        employeeId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testUser = new UserEntity();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");

        testSite = new SiteEntity();
        testSite.setSiteId(1L);
        testSite.setSiteName("Test Site");

        testDepartment = new DepartmentEntity();
        testDepartment.setDepartmentId(1L);
        testDepartment.setDepartmentName("Test Department");

        testEmployee = new EmployeeEntity();
        testEmployee.setEmployeeId(employeeId);
        testEmployee.setTenant(testTenant);
        testEmployee.setEmployeeNo("EMP001");
        testEmployee.setFullName("John Doe");
        testEmployee.setNameEnglish("John Doe");
        testEmployee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testEmployee.setGender("M");
        testEmployee.setPosition("Manager");
        testEmployee.setJobTitle("Production Manager");
        testEmployee.setHireDate(LocalDate.of(2020, 1, 1));
        testEmployee.setEmploymentType("FULL_TIME");
        testEmployee.setEmploymentStatus("ACTIVE");
        testEmployee.setPhone("02-1234-5678");
        testEmployee.setMobile("010-1234-5678");
        testEmployee.setEmail("john.doe@test.com");
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("사원 전체 조회 - 성공")
    void testGetAllEmployeesByTenant_Success() {
        List<EmployeeEntity> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(employees);

        List<EmployeeEntity> result = employeeService.getAllEmployeesByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeNo()).isEqualTo("EMP001");
        verify(employeeRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("사원 ID로 조회 - 성공")
    void testGetEmployeeById_Success() {
        when(employeeRepository.findByIdWithAllRelations(employeeId))
                .thenReturn(Optional.of(testEmployee));

        EmployeeEntity result = employeeService.getEmployeeById(employeeId);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeNo()).isEqualTo("EMP001");
        assertThat(result.getFullName()).isEqualTo("John Doe");
        verify(employeeRepository).findByIdWithAllRelations(employeeId);
    }

    @Test
    @DisplayName("사원 ID로 조회 - 실패 (존재하지 않음)")
    void testGetEmployeeById_Fail_NotFound() {
        when(employeeRepository.findByIdWithAllRelations(employeeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(employeeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    @DisplayName("활성 사원 조회 - 성공")
    void testGetActiveEmployees_Success() {
        testEmployee.setEmploymentStatus("ACTIVE");
        List<EmployeeEntity> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findActiveEmployeesByTenantId(tenantId))
                .thenReturn(employees);

        List<EmployeeEntity> result = employeeService.getActiveEmployees(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmploymentStatus()).isEqualTo("ACTIVE");
        verify(employeeRepository).findActiveEmployeesByTenantId(tenantId);
    }

    @Test
    @DisplayName("사업장별 사원 조회 - 성공")
    void testGetEmployeesBySite_Success() {
        testEmployee.setSite(testSite);
        List<EmployeeEntity> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByTenantIdAndSiteId(tenantId, testSite.getSiteId()))
                .thenReturn(employees);

        List<EmployeeEntity> result = employeeService.getEmployeesBySite(tenantId, testSite.getSiteId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(employeeRepository).findByTenantIdAndSiteId(tenantId, testSite.getSiteId());
    }

    @Test
    @DisplayName("부서별 사원 조회 - 성공")
    void testGetEmployeesByDepartment_Success() {
        testEmployee.setDepartment(testDepartment);
        List<EmployeeEntity> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByTenantIdAndDepartmentId(tenantId, testDepartment.getDepartmentId()))
                .thenReturn(employees);

        List<EmployeeEntity> result = employeeService.getEmployeesByDepartment(tenantId, testDepartment.getDepartmentId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(employeeRepository).findByTenantIdAndDepartmentId(tenantId, testDepartment.getDepartmentId());
    }

    @Test
    @DisplayName("재직 상태별 사원 조회 - 성공")
    void testGetEmployeesByStatus_Success() {
        testEmployee.setEmploymentStatus("ACTIVE");
        List<EmployeeEntity> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByTenantIdAndEmploymentStatus(tenantId, "ACTIVE"))
                .thenReturn(employees);

        List<EmployeeEntity> result = employeeService.getEmployeesByStatus(tenantId, "ACTIVE");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmploymentStatus()).isEqualTo("ACTIVE");
        verify(employeeRepository).findByTenantIdAndEmploymentStatus(tenantId, "ACTIVE");
    }

    @Test
    @DisplayName("사용자 ID로 사원 조회 - 성공")
    void testGetEmployeeByUser_Success() {
        testEmployee.setUser(testUser);
        when(employeeRepository.findByUserId(testUser.getUserId()))
                .thenReturn(Optional.of(testEmployee));

        EmployeeEntity result = employeeService.getEmployeeByUser(testUser.getUserId());

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeNo()).isEqualTo("EMP001");
        verify(employeeRepository).findByUserId(testUser.getUserId());
    }

    @Test
    @DisplayName("사용자 ID로 사원 조회 - 없음")
    void testGetEmployeeByUser_NotFound() {
        when(employeeRepository.findByUserId(testUser.getUserId()))
                .thenReturn(Optional.empty());

        EmployeeEntity result = employeeService.getEmployeeByUser(testUser.getUserId());

        assertThat(result).isNull();
        verify(employeeRepository).findByUserId(testUser.getUserId());
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("사원 생성 - 성공 (기본 정보만)")
    void testCreateEmployee_Success_MinimalInfo() {
        EmployeeEntity newEmployee = new EmployeeEntity();
        newEmployee.setTenant(testTenant);
        newEmployee.setEmployeeNo("EMP999");
        newEmployee.setFullName("Jane Smith");

        when(employeeRepository.existsByTenant_TenantIdAndEmployeeNo(tenantId, "EMP999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(employeeRepository.save(any(EmployeeEntity.class)))
                .thenAnswer(invocation -> {
                    EmployeeEntity saved = invocation.getArgument(0);
                    saved.setEmployeeId(99L);
                    assertThat(saved.getEmploymentStatus()).isEqualTo("ACTIVE"); // Default status
                    return saved;
                });
        when(employeeRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newEmployee));

        EmployeeEntity result = employeeService.createEmployee(newEmployee);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(any(EmployeeEntity.class));
    }

    @Test
    @DisplayName("사원 생성 - 성공 (완전한 정보)")
    void testCreateEmployee_Success_FullInfo() {
        EmployeeEntity newEmployee = new EmployeeEntity();
        newEmployee.setTenant(testTenant);
        newEmployee.setUser(testUser);
        newEmployee.setSite(testSite);
        newEmployee.setDepartment(testDepartment);
        newEmployee.setEmployeeNo("EMP999");
        newEmployee.setFullName("Jane Smith");
        newEmployee.setEmploymentStatus("ACTIVE");

        when(employeeRepository.existsByTenant_TenantIdAndEmployeeNo(tenantId, "EMP999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.findById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(siteRepository.findById(testSite.getSiteId()))
                .thenReturn(Optional.of(testSite));
        when(departmentRepository.findById(testDepartment.getDepartmentId()))
                .thenReturn(Optional.of(testDepartment));
        when(employeeRepository.save(any(EmployeeEntity.class)))
                .thenAnswer(invocation -> {
                    EmployeeEntity saved = invocation.getArgument(0);
                    saved.setEmployeeId(99L);
                    return saved;
                });
        when(employeeRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newEmployee));

        EmployeeEntity result = employeeService.createEmployee(newEmployee);

        assertThat(result).isNotNull();
        verify(userRepository).findById(testUser.getUserId());
        verify(siteRepository).findById(testSite.getSiteId());
        verify(departmentRepository).findById(testDepartment.getDepartmentId());
    }

    @Test
    @DisplayName("사원 생성 - 실패 (중복 사원번호)")
    void testCreateEmployee_Fail_DuplicateEmployeeNo() {
        EmployeeEntity newEmployee = new EmployeeEntity();
        newEmployee.setTenant(testTenant);
        newEmployee.setEmployeeNo("EMP001"); // Duplicate

        when(employeeRepository.existsByTenant_TenantIdAndEmployeeNo(tenantId, "EMP001"))
                .thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(newEmployee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee already exists");
    }

    @Test
    @DisplayName("사원 생성 - 실패 (테넌트 없음)")
    void testCreateEmployee_Fail_TenantNotFound() {
        EmployeeEntity newEmployee = new EmployeeEntity();
        newEmployee.setTenant(testTenant);
        newEmployee.setEmployeeNo("EMP999");

        when(employeeRepository.existsByTenant_TenantIdAndEmployeeNo(tenantId, "EMP999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(newEmployee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("사원 생성 - 실패 (사용자 없음)")
    void testCreateEmployee_Fail_UserNotFound() {
        EmployeeEntity newEmployee = new EmployeeEntity();
        newEmployee.setTenant(testTenant);
        newEmployee.setUser(testUser);
        newEmployee.setEmployeeNo("EMP999");

        when(employeeRepository.existsByTenant_TenantIdAndEmployeeNo(tenantId, "EMP999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.findById(testUser.getUserId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(newEmployee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("사원 수정 - 성공")
    void testUpdateEmployee_Success() {
        EmployeeEntity updateData = new EmployeeEntity();
        updateData.setFullName("Updated Name");
        updateData.setPosition("Senior Manager");
        updateData.setJobTitle("Operations Manager");
        updateData.setMobile("010-9999-9999");
        updateData.setEmail("updated@test.com");

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(EmployeeEntity.class)))
                .thenReturn(testEmployee);
        when(employeeRepository.findByIdWithAllRelations(employeeId))
                .thenReturn(Optional.of(testEmployee));

        EmployeeEntity result = employeeService.updateEmployee(employeeId, updateData);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(testEmployee);
        assertThat(testEmployee.getFullName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("사원 수정 - 성공 (부서 변경)")
    void testUpdateEmployee_Success_ChangeDepartment() {
        DepartmentEntity newDepartment = new DepartmentEntity();
        newDepartment.setDepartmentId(2L);
        newDepartment.setDepartmentName("New Department");

        EmployeeEntity updateData = new EmployeeEntity();
        updateData.setFullName("Updated Name");
        updateData.setPosition("Manager");
        updateData.setDepartment(newDepartment);

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(testEmployee));
        when(departmentRepository.findById(2L))
                .thenReturn(Optional.of(newDepartment));
        when(employeeRepository.save(any(EmployeeEntity.class)))
                .thenReturn(testEmployee);
        when(employeeRepository.findByIdWithAllRelations(employeeId))
                .thenReturn(Optional.of(testEmployee));

        EmployeeEntity result = employeeService.updateEmployee(employeeId, updateData);

        assertThat(result).isNotNull();
        verify(departmentRepository).findById(2L);
    }

    @Test
    @DisplayName("사원 수정 - 성공 (퇴사 처리)")
    void testUpdateEmployee_Success_Resignation() {
        EmployeeEntity updateData = new EmployeeEntity();
        updateData.setFullName("John Doe");
        updateData.setPosition("Manager");
        updateData.setEmploymentStatus("RESIGNED");
        updateData.setResignationDate(LocalDate.now());

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(EmployeeEntity.class)))
                .thenReturn(testEmployee);
        when(employeeRepository.findByIdWithAllRelations(employeeId))
                .thenReturn(Optional.of(testEmployee));

        EmployeeEntity result = employeeService.updateEmployee(employeeId, updateData);

        assertThat(result).isNotNull();
        assertThat(testEmployee.getEmploymentStatus()).isEqualTo("RESIGNED");
        assertThat(testEmployee.getResignationDate()).isNotNull();
    }

    @Test
    @DisplayName("사원 수정 - 실패 (존재하지 않음)")
    void testUpdateEmployee_Fail_NotFound() {
        EmployeeEntity updateData = new EmployeeEntity();
        updateData.setFullName("Updated Name");
        updateData.setPosition("Manager");

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("사원 삭제 - 성공")
    void testDeleteEmployee_Success() {
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(testEmployee));

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository).delete(testEmployee);
    }

    @Test
    @DisplayName("사원 삭제 - 실패 (존재하지 않음)")
    void testDeleteEmployee_Fail_NotFound() {
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(employeeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee not found");
    }
}
