package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.TenantEntity;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tenant Service Test
 * 테넌트 관리 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("테넌트 서비스 테스트")
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private TenantEntity testTenant;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");
        testTenant.setIndustryType("MANUFACTURING");
        testTenant.setStatus("active");
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("테넌트 ID로 조회 - 성공")
    void testFindById_Success() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));

        Optional<TenantEntity> result = tenantService.findById(tenantId);

        assertThat(result).isPresent();
        assertThat(result.get().getTenantId()).isEqualTo(tenantId);
        verify(tenantRepository).findById(tenantId);
    }

    @Test
    @DisplayName("테넌트 이름으로 조회 - 성공")
    void testFindByTenantName_Success() {
        when(tenantRepository.findByTenantName("Test Tenant"))
                .thenReturn(Optional.of(testTenant));

        Optional<TenantEntity> result = tenantService.findByTenantName("Test Tenant");

        assertThat(result).isPresent();
        assertThat(result.get().getTenantName()).isEqualTo("Test Tenant");
        verify(tenantRepository).findByTenantName("Test Tenant");
    }

    @Test
    @DisplayName("모든 테넌트 조회 - 성공")
    void testFindAll_Success() {
        List<TenantEntity> tenants = Arrays.asList(testTenant);
        when(tenantRepository.findAll())
                .thenReturn(tenants);

        List<TenantEntity> result = tenantService.findAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(tenantRepository).findAll();
    }

    @Test
    @DisplayName("산업 타입별 테넌트 조회 - 성공")
    void testFindByIndustryType_Success() {
        List<TenantEntity> tenants = Arrays.asList(testTenant);
        when(tenantRepository.findByIndustryType("MANUFACTURING"))
                .thenReturn(tenants);

        List<TenantEntity> result = tenantService.findByIndustryType("MANUFACTURING");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIndustryType()).isEqualTo("MANUFACTURING");
        verify(tenantRepository).findByIndustryType("MANUFACTURING");
    }

    @Test
    @DisplayName("활성 테넌트 조회 - 성공")
    void testFindActiveTenants_Success() {
        List<TenantEntity> tenants = Arrays.asList(testTenant);
        when(tenantRepository.findByStatus("active"))
                .thenReturn(tenants);

        List<TenantEntity> result = tenantService.findActiveTenants();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("active");
        verify(tenantRepository).findByStatus("active");
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("테넌트 생성 - 성공")
    void testCreateTenant_Success() {
        TenantEntity newTenant = new TenantEntity();
        newTenant.setTenantId("TENANT999");
        newTenant.setTenantName("New Tenant");

        when(tenantRepository.existsById("TENANT999"))
                .thenReturn(false);
        when(tenantRepository.save(any(TenantEntity.class)))
                .thenReturn(newTenant);

        TenantEntity result = tenantService.createTenant(newTenant);

        assertThat(result).isNotNull();
        verify(tenantRepository).save(newTenant);
    }

    @Test
    @DisplayName("테넌트 생성 - 실패 (중복 ID)")
    void testCreateTenant_Fail_DuplicateId() {
        TenantEntity newTenant = new TenantEntity();
        newTenant.setTenantId(tenantId);

        when(tenantRepository.existsById(tenantId))
                .thenReturn(true);

        assertThatThrownBy(() -> tenantService.createTenant(newTenant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID already exists");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("테넌트 수정 - 성공")
    void testUpdateTenant_Success() {
        testTenant.setTenantName("Updated Tenant");

        when(tenantRepository.existsById(tenantId))
                .thenReturn(true);
        when(tenantRepository.save(any(TenantEntity.class)))
                .thenReturn(testTenant);

        TenantEntity result = tenantService.updateTenant(testTenant);

        assertThat(result).isNotNull();
        verify(tenantRepository).save(testTenant);
    }

    @Test
    @DisplayName("테넌트 수정 - 실패 (존재하지 않음)")
    void testUpdateTenant_Fail_NotFound() {
        when(tenantRepository.existsById(tenantId))
                .thenReturn(false);

        assertThatThrownBy(() -> tenantService.updateTenant(testTenant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("테넌트 삭제 - 성공")
    void testDeleteTenant_Success() {
        tenantService.deleteTenant(tenantId);

        verify(tenantRepository).deleteById(tenantId);
    }

    // === 활성화/비활성화 테스트 ===

    @Test
    @DisplayName("테넌트 활성화 - 성공")
    void testActivateTenant_Success() {
        testTenant.setStatus("inactive");

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(TenantEntity.class)))
                .thenAnswer(invocation -> {
                    TenantEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("active");
                    return saved;
                });

        TenantEntity result = tenantService.activateTenant(tenantId);

        assertThat(result).isNotNull();
        verify(tenantRepository).save(testTenant);
    }

    @Test
    @DisplayName("테넌트 활성화 - 실패 (존재하지 않음)")
    void testActivateTenant_Fail_NotFound() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.activateTenant(tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("테넌트 비활성화 - 성공")
    void testDeactivateTenant_Success() {
        testTenant.setStatus("active");

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(TenantEntity.class)))
                .thenAnswer(invocation -> {
                    TenantEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("inactive");
                    return saved;
                });

        TenantEntity result = tenantService.deactivateTenant(tenantId);

        assertThat(result).isNotNull();
        verify(tenantRepository).save(testTenant);
    }

    @Test
    @DisplayName("테넌트 비활성화 - 실패 (존재하지 않음)")
    void testDeactivateTenant_Fail_NotFound() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.deactivateTenant(tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }
}
