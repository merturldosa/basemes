package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.DepartmentEntity;
import kr.co.softice.mes.domain.entity.SiteEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.DepartmentRepository;
import kr.co.softice.mes.domain.repository.SiteRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Department Service Test
 * 부서 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("부서 서비스 테스트")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private TenantEntity testTenant;
    private SiteEntity testSite;
    private UserEntity testUser;
    private DepartmentEntity testDepartment;
    private Long departmentId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        departmentId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testSite = new SiteEntity();
        testSite.setSiteId(1L);
        testSite.setSiteName("Test Site");

        testUser = new UserEntity();
        testUser.setUserId(1L);
        testUser.setUsername("manager");

        testDepartment = new DepartmentEntity();
        testDepartment.setDepartmentId(departmentId);
        testDepartment.setTenant(testTenant);
        testDepartment.setDepartmentCode("DEPT001");
        testDepartment.setDepartmentName("Test Department");
        testDepartment.setDepartmentType("PRODUCTION");
        testDepartment.setDepthLevel(0);
        testDepartment.setIsActive(true);
        testDepartment.setSortOrder(1);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("부서 전체 조회 - 성공")
    void testGetAllDepartmentsByTenant_Success() {
        List<DepartmentEntity> departments = Arrays.asList(testDepartment);
        when(departmentRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(departments);

        List<DepartmentEntity> result = departmentService.getAllDepartmentsByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartmentCode()).isEqualTo("DEPT001");
        verify(departmentRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("부서 ID로 조회 - 성공")
    void testGetDepartmentById_Success() {
        when(departmentRepository.findByIdWithAllRelations(departmentId))
                .thenReturn(Optional.of(testDepartment));

        DepartmentEntity result = departmentService.getDepartmentById(departmentId);

        assertThat(result).isNotNull();
        assertThat(result.getDepartmentCode()).isEqualTo("DEPT001");
        verify(departmentRepository).findByIdWithAllRelations(departmentId);
    }

    @Test
    @DisplayName("부서 ID로 조회 - 실패 (존재하지 않음)")
    void testGetDepartmentById_Fail_NotFound() {
        when(departmentRepository.findByIdWithAllRelations(departmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(departmentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    @DisplayName("활성 부서 조회 - 성공")
    void testGetActiveDepartments_Success() {
        List<DepartmentEntity> departments = Arrays.asList(testDepartment);
        when(departmentRepository.findActiveDepartmentsByTenantId(tenantId))
                .thenReturn(departments);

        List<DepartmentEntity> result = departmentService.getActiveDepartments(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(departmentRepository).findActiveDepartmentsByTenantId(tenantId);
    }

    @Test
    @DisplayName("사업장별 부서 조회 - 성공")
    void testGetDepartmentsBySite_Success() {
        testDepartment.setSite(testSite);
        List<DepartmentEntity> departments = Arrays.asList(testDepartment);
        when(departmentRepository.findByTenantIdAndSiteId(tenantId, testSite.getSiteId()))
                .thenReturn(departments);

        List<DepartmentEntity> result = departmentService.getDepartmentsBySite(tenantId, testSite.getSiteId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(departmentRepository).findByTenantIdAndSiteId(tenantId, testSite.getSiteId());
    }

    @Test
    @DisplayName("최상위 부서 조회 - 성공")
    void testGetTopLevelDepartments_Success() {
        List<DepartmentEntity> departments = Arrays.asList(testDepartment);
        when(departmentRepository.findTopLevelDepartmentsByTenantId(tenantId))
                .thenReturn(departments);

        List<DepartmentEntity> result = departmentService.getTopLevelDepartments(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepthLevel()).isEqualTo(0);
        verify(departmentRepository).findTopLevelDepartmentsByTenantId(tenantId);
    }

    @Test
    @DisplayName("하위 부서 조회 - 성공")
    void testGetChildDepartments_Success() {
        DepartmentEntity childDept = new DepartmentEntity();
        childDept.setDepartmentId(2L);
        childDept.setDepartmentCode("DEPT002");
        childDept.setParentDepartment(testDepartment);
        childDept.setDepthLevel(1);

        List<DepartmentEntity> children = Arrays.asList(childDept);
        when(departmentRepository.findByTenantIdAndParentDepartmentId(tenantId, departmentId))
                .thenReturn(children);

        List<DepartmentEntity> result = departmentService.getChildDepartments(tenantId, departmentId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepthLevel()).isEqualTo(1);
        verify(departmentRepository).findByTenantIdAndParentDepartmentId(tenantId, departmentId);
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("부서 생성 - 성공 (최상위 부서)")
    void testCreateDepartment_Success_TopLevel() {
        DepartmentEntity newDepartment = new DepartmentEntity();
        newDepartment.setTenant(testTenant);
        newDepartment.setDepartmentCode("DEPT999");
        newDepartment.setDepartmentName("New Department");
        newDepartment.setDepartmentType("PRODUCTION");

        when(departmentRepository.existsByTenant_TenantIdAndDepartmentCode(tenantId, "DEPT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(departmentRepository.save(any(DepartmentEntity.class)))
                .thenAnswer(invocation -> {
                    DepartmentEntity saved = invocation.getArgument(0);
                    saved.setDepartmentId(99L);
                    assertThat(saved.getDepthLevel()).isEqualTo(0);
                    assertThat(saved.getIsActive()).isTrue();
                    assertThat(saved.getSortOrder()).isEqualTo(0);
                    return saved;
                });
        when(departmentRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newDepartment));

        DepartmentEntity result = departmentService.createDepartment(newDepartment);

        assertThat(result).isNotNull();
        verify(departmentRepository).save(any(DepartmentEntity.class));
    }

    @Test
    @DisplayName("부서 생성 - 성공 (하위 부서)")
    void testCreateDepartment_Success_WithParent() {
        DepartmentEntity parentDept = new DepartmentEntity();
        parentDept.setDepartmentId(1L);
        parentDept.setDepthLevel(0);

        DepartmentEntity newDepartment = new DepartmentEntity();
        newDepartment.setTenant(testTenant);
        newDepartment.setDepartmentCode("DEPT999");
        newDepartment.setDepartmentName("Child Department");
        newDepartment.setParentDepartment(parentDept);

        when(departmentRepository.existsByTenant_TenantIdAndDepartmentCode(tenantId, "DEPT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(parentDept));
        when(departmentRepository.save(any(DepartmentEntity.class)))
                .thenAnswer(invocation -> {
                    DepartmentEntity saved = invocation.getArgument(0);
                    saved.setDepartmentId(99L);
                    assertThat(saved.getDepthLevel()).isEqualTo(1); // Parent level + 1
                    return saved;
                });
        when(departmentRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newDepartment));

        DepartmentEntity result = departmentService.createDepartment(newDepartment);

        assertThat(result).isNotNull();
        verify(departmentRepository).findById(1L);
    }

    @Test
    @DisplayName("부서 생성 - 성공 (사업장 및 관리자 설정)")
    void testCreateDepartment_Success_WithSiteAndManager() {
        DepartmentEntity newDepartment = new DepartmentEntity();
        newDepartment.setTenant(testTenant);
        newDepartment.setDepartmentCode("DEPT999");
        newDepartment.setDepartmentName("New Department");
        newDepartment.setSite(testSite);
        newDepartment.setManager(testUser);

        when(departmentRepository.existsByTenant_TenantIdAndDepartmentCode(tenantId, "DEPT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(siteRepository.findById(testSite.getSiteId()))
                .thenReturn(Optional.of(testSite));
        when(userRepository.findById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(departmentRepository.save(any(DepartmentEntity.class)))
                .thenAnswer(invocation -> {
                    DepartmentEntity saved = invocation.getArgument(0);
                    saved.setDepartmentId(99L);
                    return saved;
                });
        when(departmentRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newDepartment));

        DepartmentEntity result = departmentService.createDepartment(newDepartment);

        assertThat(result).isNotNull();
        verify(siteRepository).findById(testSite.getSiteId());
        verify(userRepository).findById(testUser.getUserId());
    }

    @Test
    @DisplayName("부서 생성 - 실패 (중복 코드)")
    void testCreateDepartment_Fail_DuplicateCode() {
        DepartmentEntity newDepartment = new DepartmentEntity();
        newDepartment.setTenant(testTenant);
        newDepartment.setDepartmentCode("DEPT001");

        when(departmentRepository.existsByTenant_TenantIdAndDepartmentCode(tenantId, "DEPT001"))
                .thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(newDepartment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department already exists");
    }

    @Test
    @DisplayName("부서 생성 - 실패 (테넌트 없음)")
    void testCreateDepartment_Fail_TenantNotFound() {
        DepartmentEntity newDepartment = new DepartmentEntity();
        newDepartment.setTenant(testTenant);
        newDepartment.setDepartmentCode("DEPT999");

        when(departmentRepository.existsByTenant_TenantIdAndDepartmentCode(tenantId, "DEPT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.createDepartment(newDepartment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("부서 생성 - 실패 (상위 부서 없음)")
    void testCreateDepartment_Fail_ParentNotFound() {
        DepartmentEntity parentDept = new DepartmentEntity();
        parentDept.setDepartmentId(999L);

        DepartmentEntity newDepartment = new DepartmentEntity();
        newDepartment.setTenant(testTenant);
        newDepartment.setDepartmentCode("DEPT999");
        newDepartment.setParentDepartment(parentDept);

        when(departmentRepository.existsByTenant_TenantIdAndDepartmentCode(tenantId, "DEPT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(departmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.createDepartment(newDepartment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parent department not found");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("부서 수정 - 성공")
    void testUpdateDepartment_Success() {
        DepartmentEntity updateData = new DepartmentEntity();
        updateData.setDepartmentName("Updated Name");
        updateData.setDepartmentType("OFFICE");
        updateData.setSortOrder(5);
        updateData.setRemarks("Updated remarks");

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(DepartmentEntity.class)))
                .thenReturn(testDepartment);
        when(departmentRepository.findByIdWithAllRelations(departmentId))
                .thenReturn(Optional.of(testDepartment));

        DepartmentEntity result = departmentService.updateDepartment(departmentId, updateData);

        assertThat(result).isNotNull();
        verify(departmentRepository).save(testDepartment);
        assertThat(testDepartment.getDepartmentName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("부서 수정 - 성공 (상위 부서 변경)")
    void testUpdateDepartment_Success_ChangeParent() {
        DepartmentEntity newParent = new DepartmentEntity();
        newParent.setDepartmentId(2L);
        newParent.setDepthLevel(0);

        DepartmentEntity updateData = new DepartmentEntity();
        updateData.setDepartmentName("Updated Name");
        updateData.setDepartmentType("OFFICE");
        updateData.setParentDepartment(newParent);

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(testDepartment));
        when(departmentRepository.findById(2L))
                .thenReturn(Optional.of(newParent));
        when(departmentRepository.save(any(DepartmentEntity.class)))
                .thenReturn(testDepartment);
        when(departmentRepository.findByIdWithAllRelations(departmentId))
                .thenReturn(Optional.of(testDepartment));

        DepartmentEntity result = departmentService.updateDepartment(departmentId, updateData);

        assertThat(result).isNotNull();
        assertThat(testDepartment.getDepthLevel()).isEqualTo(1); // Parent level + 1
    }

    @Test
    @DisplayName("부서 수정 - 실패 (자기 자신을 상위 부서로 설정)")
    void testUpdateDepartment_Fail_CircularReference() {
        DepartmentEntity updateData = new DepartmentEntity();
        updateData.setDepartmentName("Updated Name");
        updateData.setDepartmentType("OFFICE");

        DepartmentEntity selfParent = new DepartmentEntity();
        selfParent.setDepartmentId(departmentId); // Same as current department
        updateData.setParentDepartment(selfParent);

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(testDepartment));

        assertThatThrownBy(() -> departmentService.updateDepartment(departmentId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be its own parent");
    }

    @Test
    @DisplayName("부서 수정 - 실패 (존재하지 않음)")
    void testUpdateDepartment_Fail_NotFound() {
        DepartmentEntity updateData = new DepartmentEntity();
        updateData.setDepartmentName("Updated Name");

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(departmentId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("부서 삭제 - 성공")
    void testDeleteDepartment_Success() {
        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(testDepartment));
        when(departmentRepository.findByTenantIdAndParentDepartmentId(tenantId, departmentId))
                .thenReturn(Collections.emptyList());

        departmentService.deleteDepartment(departmentId);

        verify(departmentRepository).delete(testDepartment);
    }

    @Test
    @DisplayName("부서 삭제 - 실패 (하위 부서 존재)")
    void testDeleteDepartment_Fail_HasChildren() {
        DepartmentEntity childDept = new DepartmentEntity();
        childDept.setDepartmentId(2L);
        childDept.setParentDepartment(testDepartment);

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(testDepartment));
        when(departmentRepository.findByTenantIdAndParentDepartmentId(tenantId, departmentId))
                .thenReturn(Arrays.asList(childDept));

        assertThatThrownBy(() -> departmentService.deleteDepartment(departmentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete department with child departments");
    }

    @Test
    @DisplayName("부서 삭제 - 실패 (존재하지 않음)")
    void testDeleteDepartment_Fail_NotFound() {
        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deleteDepartment(departmentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department not found");
    }

    // === 상태 관리 테스트 ===

    @Test
    @DisplayName("활성 상태 토글 - 성공 (활성 → 비활성)")
    void testToggleActive_Success_ActiveToInactive() {
        testDepartment.setIsActive(true);

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(DepartmentEntity.class)))
                .thenReturn(testDepartment);
        when(departmentRepository.findByIdWithAllRelations(departmentId))
                .thenReturn(Optional.of(testDepartment));

        DepartmentEntity result = departmentService.toggleActive(departmentId);

        assertThat(result).isNotNull();
        assertThat(testDepartment.getIsActive()).isFalse();
        verify(departmentRepository).save(testDepartment);
    }

    @Test
    @DisplayName("활성 상태 토글 - 성공 (비활성 → 활성)")
    void testToggleActive_Success_InactiveToActive() {
        testDepartment.setIsActive(false);

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(DepartmentEntity.class)))
                .thenReturn(testDepartment);
        when(departmentRepository.findByIdWithAllRelations(departmentId))
                .thenReturn(Optional.of(testDepartment));

        DepartmentEntity result = departmentService.toggleActive(departmentId);

        assertThat(result).isNotNull();
        assertThat(testDepartment.getIsActive()).isTrue();
        verify(departmentRepository).save(testDepartment);
    }

    @Test
    @DisplayName("활성 상태 토글 - 실패 (존재하지 않음)")
    void testToggleActive_Fail_NotFound() {
        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.toggleActive(departmentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department not found");
    }
}
