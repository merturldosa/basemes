package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.SupplierEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.SupplierRepository;
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
 * Supplier Service Test
 * 공급업체 관리 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("공급업체 서비스 테스트")
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private TenantEntity testTenant;
    private SupplierEntity testSupplier;
    private Long supplierId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        supplierId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testSupplier = new SupplierEntity();
        testSupplier.setSupplierId(supplierId);
        testSupplier.setTenant(testTenant);
        testSupplier.setSupplierCode("SUP001");
        testSupplier.setSupplierName("Test Supplier");
        testSupplier.setSupplierType("RAW_MATERIAL");
        testSupplier.setRating("A");
        testSupplier.setIsActive(true);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("테넌트별 공급업체 조회 - 성공")
    void testFindByTenant_Success() {
        List<SupplierEntity> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(suppliers);

        List<SupplierEntity> result = supplierService.findByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplierCode()).isEqualTo("SUP001");
        verify(supplierRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("활성 공급업체 조회 - 성공")
    void testFindActiveByTenant_Success() {
        testSupplier.setIsActive(true);
        List<SupplierEntity> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true))
                .thenReturn(suppliers);

        List<SupplierEntity> result = supplierService.findActiveByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(supplierRepository).findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    @Test
    @DisplayName("유형별 공급업체 조회 - 성공")
    void testFindByTenantAndType_Success() {
        List<SupplierEntity> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByTenantIdAndSupplierTypeWithAllRelations(tenantId, "RAW_MATERIAL"))
                .thenReturn(suppliers);

        List<SupplierEntity> result = supplierService.findByTenantAndType(tenantId, "RAW_MATERIAL");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplierType()).isEqualTo("RAW_MATERIAL");
        verify(supplierRepository).findByTenantIdAndSupplierTypeWithAllRelations(tenantId, "RAW_MATERIAL");
    }

    @Test
    @DisplayName("등급별 공급업체 조회 - 성공")
    void testFindByTenantAndRating_Success() {
        List<SupplierEntity> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByTenantIdAndRatingWithAllRelations(tenantId, "A"))
                .thenReturn(suppliers);

        List<SupplierEntity> result = supplierService.findByTenantAndRating(tenantId, "A");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isEqualTo("A");
        verify(supplierRepository).findByTenantIdAndRatingWithAllRelations(tenantId, "A");
    }

    @Test
    @DisplayName("공급업체 ID로 조회 - 성공")
    void testFindById_Success() {
        when(supplierRepository.findByIdWithAllRelations(supplierId))
                .thenReturn(Optional.of(testSupplier));

        Optional<SupplierEntity> result = supplierService.findById(supplierId);

        assertThat(result).isPresent();
        assertThat(result.get().getSupplierCode()).isEqualTo("SUP001");
        verify(supplierRepository).findByIdWithAllRelations(supplierId);
    }

    @Test
    @DisplayName("공급업체 코드로 조회 - 성공")
    void testFindBySupplierCode_Success() {
        when(supplierRepository.findByTenant_TenantIdAndSupplierCode(tenantId, "SUP001"))
                .thenReturn(Optional.of(testSupplier));

        Optional<SupplierEntity> result = supplierService.findBySupplierCode(tenantId, "SUP001");

        assertThat(result).isPresent();
        assertThat(result.get().getSupplierCode()).isEqualTo("SUP001");
        verify(supplierRepository).findByTenant_TenantIdAndSupplierCode(tenantId, "SUP001");
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("공급업체 생성 - 성공")
    void testCreateSupplier_Success() {
        SupplierEntity newSupplier = new SupplierEntity();
        newSupplier.setTenant(testTenant);
        newSupplier.setSupplierCode("SUP999");
        newSupplier.setSupplierName("New Supplier");

        when(supplierRepository.existsByTenantAndSupplierCode(testTenant, "SUP999"))
                .thenReturn(false);
        when(supplierRepository.save(any(SupplierEntity.class)))
                .thenAnswer(invocation -> {
                    SupplierEntity saved = invocation.getArgument(0);
                    saved.setSupplierId(99L);
                    return saved;
                });
        when(supplierRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newSupplier));

        SupplierEntity result = supplierService.createSupplier(newSupplier);

        assertThat(result).isNotNull();
        verify(supplierRepository).save(any(SupplierEntity.class));
    }

    @Test
    @DisplayName("공급업체 생성 - 실패 (중복 코드)")
    void testCreateSupplier_Fail_DuplicateCode() {
        SupplierEntity newSupplier = new SupplierEntity();
        newSupplier.setTenant(testTenant);
        newSupplier.setSupplierCode("SUP001");

        when(supplierRepository.existsByTenantAndSupplierCode(testTenant, "SUP001"))
                .thenReturn(true);

        assertThatThrownBy(() -> supplierService.createSupplier(newSupplier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supplier code already exists");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("공급업체 수정 - 성공")
    void testUpdateSupplier_Success() {
        testSupplier.setSupplierName("Updated Supplier");

        when(supplierRepository.save(any(SupplierEntity.class)))
                .thenReturn(testSupplier);
        when(supplierRepository.findByIdWithAllRelations(supplierId))
                .thenReturn(Optional.of(testSupplier));

        SupplierEntity result = supplierService.updateSupplier(testSupplier);

        assertThat(result).isNotNull();
        verify(supplierRepository).save(testSupplier);
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("공급업체 삭제 - 성공")
    void testDeleteSupplier_Success() {
        supplierService.deleteSupplier(supplierId);

        verify(supplierRepository).deleteById(supplierId);
    }

    // === 상태 토글 테스트 ===

    @Test
    @DisplayName("활성 상태 토글 - 성공 (활성 → 비활성)")
    void testToggleActive_Success_ActiveToInactive() {
        testSupplier.setIsActive(true);

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(any(SupplierEntity.class)))
                .thenAnswer(invocation -> {
                    SupplierEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });
        when(supplierRepository.findByIdWithAllRelations(supplierId))
                .thenReturn(Optional.of(testSupplier));

        SupplierEntity result = supplierService.toggleActive(supplierId);

        assertThat(result).isNotNull();
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    @DisplayName("활성 상태 토글 - 성공 (비활성 → 활성)")
    void testToggleActive_Success_InactiveToActive() {
        testSupplier.setIsActive(false);

        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(any(SupplierEntity.class)))
                .thenAnswer(invocation -> {
                    SupplierEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });
        when(supplierRepository.findByIdWithAllRelations(supplierId))
                .thenReturn(Optional.of(testSupplier));

        SupplierEntity result = supplierService.toggleActive(supplierId);

        assertThat(result).isNotNull();
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    @DisplayName("활성 상태 토글 - 실패 (존재하지 않음)")
    void testToggleActive_Fail_NotFound() {
        when(supplierRepository.findById(supplierId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.toggleActive(supplierId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supplier not found");
    }
}
