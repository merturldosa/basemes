package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.MaterialEntity;
import kr.co.softice.mes.domain.entity.SupplierEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.MaterialRepository;
import kr.co.softice.mes.domain.repository.SupplierRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Material Service Test
 * 자재 마스터 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("자재 서비스 테스트")
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private MaterialService materialService;

    private TenantEntity testTenant;
    private SupplierEntity testSupplier;
    private MaterialEntity testMaterial;
    private Long materialId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        materialId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testSupplier = new SupplierEntity();
        testSupplier.setSupplierId(1L);
        testSupplier.setSupplierCode("SUP001");
        testSupplier.setSupplierName("Test Supplier");

        testMaterial = new MaterialEntity();
        testMaterial.setMaterialId(materialId);
        testMaterial.setTenant(testTenant);
        testMaterial.setMaterialCode("MAT001");
        testMaterial.setMaterialName("Test Material");
        testMaterial.setMaterialType("RAW_MATERIAL");
        testMaterial.setSpecification("Test Spec");
        testMaterial.setUnit("EA");
        testMaterial.setStandardPrice(new BigDecimal("1000.00"));
        testMaterial.setCurrentPrice(new BigDecimal("1100.00"));
        testMaterial.setCurrency("KRW");
        testMaterial.setLeadTimeDays(7);
        testMaterial.setMinStockQuantity(new BigDecimal("100"));
        testMaterial.setMaxStockQuantity(new BigDecimal("1000"));
        testMaterial.setSafetyStockQuantity(new BigDecimal("150"));
        testMaterial.setReorderPoint(new BigDecimal("200"));
        testMaterial.setIsActive(true);
        testMaterial.setLotManaged(false);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("전체 자재 조회 - 성공")
    void testGetAllMaterials_Success() {
        List<MaterialEntity> materials = Arrays.asList(testMaterial);
        when(materialRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(materials);

        List<MaterialEntity> result = materialService.getAllMaterials(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaterialCode()).isEqualTo("MAT001");
        verify(materialRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("자재 ID로 조회 - 성공")
    void testGetMaterialById_Success() {
        when(materialRepository.findByIdWithAllRelations(materialId))
                .thenReturn(Optional.of(testMaterial));

        MaterialEntity result = materialService.getMaterialById(materialId);

        assertThat(result).isNotNull();
        assertThat(result.getMaterialCode()).isEqualTo("MAT001");
        verify(materialRepository).findByIdWithAllRelations(materialId);
    }

    @Test
    @DisplayName("자재 ID로 조회 - 실패 (존재하지 않음)")
    void testGetMaterialById_Fail_NotFound() {
        when(materialRepository.findByIdWithAllRelations(materialId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.getMaterialById(materialId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Material not found");
    }

    @Test
    @DisplayName("활성 자재 조회 - 성공")
    void testGetActiveMaterials_Success() {
        testMaterial.setIsActive(true);
        List<MaterialEntity> materials = Arrays.asList(testMaterial);
        when(materialRepository.findActiveByTenantId(tenantId))
                .thenReturn(materials);

        List<MaterialEntity> result = materialService.getActiveMaterials(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(materialRepository).findActiveByTenantId(tenantId);
    }

    @Test
    @DisplayName("자재 유형별 조회 - 성공")
    void testGetMaterialsByType_Success() {
        List<MaterialEntity> materials = Arrays.asList(testMaterial);
        when(materialRepository.findByTenantIdAndType(tenantId, "RAW_MATERIAL"))
                .thenReturn(materials);

        List<MaterialEntity> result = materialService.getMaterialsByType(tenantId, "RAW_MATERIAL");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaterialType()).isEqualTo("RAW_MATERIAL");
        verify(materialRepository).findByTenantIdAndType(tenantId, "RAW_MATERIAL");
    }

    @Test
    @DisplayName("공급업체별 자재 조회 - 성공")
    void testGetMaterialsBySupplier_Success() {
        testMaterial.setSupplier(testSupplier);
        List<MaterialEntity> materials = Arrays.asList(testMaterial);
        when(materialRepository.findByTenantIdAndSupplierId(tenantId, testSupplier.getSupplierId()))
                .thenReturn(materials);

        List<MaterialEntity> result = materialService.getMaterialsBySupplier(tenantId, testSupplier.getSupplierId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(materialRepository).findByTenantIdAndSupplierId(tenantId, testSupplier.getSupplierId());
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("자재 생성 - 성공 (기본)")
    void testCreateMaterial_Success() {
        MaterialEntity newMaterial = new MaterialEntity();
        newMaterial.setMaterialCode("MAT999");
        newMaterial.setMaterialName("New Material");
        newMaterial.setUnit("KG");

        when(materialRepository.existsByTenant_TenantIdAndMaterialCode(tenantId, "MAT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(materialRepository.save(any(MaterialEntity.class)))
                .thenAnswer(invocation -> {
                    MaterialEntity saved = invocation.getArgument(0);
                    saved.setMaterialId(99L);
                    assertThat(saved.getIsActive()).isTrue(); // Default
                    assertThat(saved.getLotManaged()).isFalse(); // Default
                    return saved;
                });
        when(materialRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newMaterial));

        MaterialEntity result = materialService.createMaterial(tenantId, newMaterial);

        assertThat(result).isNotNull();
        verify(materialRepository).save(any(MaterialEntity.class));
    }

    @Test
    @DisplayName("자재 생성 - 성공 (공급업체 포함)")
    void testCreateMaterial_Success_WithSupplier() {
        MaterialEntity newMaterial = new MaterialEntity();
        newMaterial.setMaterialCode("MAT999");
        newMaterial.setMaterialName("New Material");
        newMaterial.setUnit("KG");
        newMaterial.setSupplier(testSupplier);

        when(materialRepository.existsByTenant_TenantIdAndMaterialCode(tenantId, "MAT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(supplierRepository.findById(testSupplier.getSupplierId()))
                .thenReturn(Optional.of(testSupplier));
        when(materialRepository.save(any(MaterialEntity.class)))
                .thenAnswer(invocation -> {
                    MaterialEntity saved = invocation.getArgument(0);
                    saved.setMaterialId(99L);
                    return saved;
                });
        when(materialRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newMaterial));

        MaterialEntity result = materialService.createMaterial(tenantId, newMaterial);

        assertThat(result).isNotNull();
        verify(supplierRepository).findById(testSupplier.getSupplierId());
    }

    @Test
    @DisplayName("자재 생성 - 실패 (중복 코드)")
    void testCreateMaterial_Fail_DuplicateCode() {
        MaterialEntity newMaterial = new MaterialEntity();
        newMaterial.setMaterialCode("MAT001"); // Duplicate

        when(materialRepository.existsByTenant_TenantIdAndMaterialCode(tenantId, "MAT001"))
                .thenReturn(true);

        assertThatThrownBy(() -> materialService.createMaterial(tenantId, newMaterial))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Material code already exists");
    }

    @Test
    @DisplayName("자재 생성 - 실패 (테넌트 없음)")
    void testCreateMaterial_Fail_TenantNotFound() {
        MaterialEntity newMaterial = new MaterialEntity();
        newMaterial.setMaterialCode("MAT999");

        when(materialRepository.existsByTenant_TenantIdAndMaterialCode(tenantId, "MAT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.createMaterial(tenantId, newMaterial))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("자재 생성 - 실패 (공급업체 없음)")
    void testCreateMaterial_Fail_SupplierNotFound() {
        MaterialEntity newMaterial = new MaterialEntity();
        newMaterial.setMaterialCode("MAT999");
        newMaterial.setSupplier(testSupplier);

        when(materialRepository.existsByTenant_TenantIdAndMaterialCode(tenantId, "MAT999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(supplierRepository.findById(testSupplier.getSupplierId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.createMaterial(tenantId, newMaterial))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supplier not found");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("자재 수정 - 성공")
    void testUpdateMaterial_Success() {
        MaterialEntity updateData = new MaterialEntity();
        updateData.setMaterialName("Updated Material");
        updateData.setSpecification("Updated Spec");
        updateData.setStandardPrice(new BigDecimal("1200.00"));
        updateData.setCurrentPrice(new BigDecimal("1250.00"));
        updateData.setMinStockQuantity(new BigDecimal("120"));
        updateData.setIsActive(true);
        updateData.setLotManaged(true);

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(testMaterial));
        when(materialRepository.save(any(MaterialEntity.class)))
                .thenReturn(testMaterial);
        when(materialRepository.findByIdWithAllRelations(materialId))
                .thenReturn(Optional.of(testMaterial));

        MaterialEntity result = materialService.updateMaterial(materialId, updateData);

        assertThat(result).isNotNull();
        verify(materialRepository).save(testMaterial);
        assertThat(testMaterial.getMaterialName()).isEqualTo("Updated Material");
    }

    @Test
    @DisplayName("자재 수정 - 성공 (공급업체 변경)")
    void testUpdateMaterial_Success_ChangeSupplier() {
        SupplierEntity newSupplier = new SupplierEntity();
        newSupplier.setSupplierId(2L);
        newSupplier.setSupplierName("New Supplier");

        MaterialEntity updateData = new MaterialEntity();
        updateData.setMaterialName("Updated Material");
        updateData.setSpecification("Updated Spec");
        updateData.setSupplier(newSupplier);
        updateData.setIsActive(true);
        updateData.setLotManaged(false);

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(testMaterial));
        when(supplierRepository.findById(2L))
                .thenReturn(Optional.of(newSupplier));
        when(materialRepository.save(any(MaterialEntity.class)))
                .thenReturn(testMaterial);
        when(materialRepository.findByIdWithAllRelations(materialId))
                .thenReturn(Optional.of(testMaterial));

        MaterialEntity result = materialService.updateMaterial(materialId, updateData);

        assertThat(result).isNotNull();
        verify(supplierRepository).findById(2L);
    }

    @Test
    @DisplayName("자재 수정 - 성공 (공급업체 제거)")
    void testUpdateMaterial_Success_RemoveSupplier() {
        testMaterial.setSupplier(testSupplier);

        MaterialEntity updateData = new MaterialEntity();
        updateData.setMaterialName("Updated Material");
        updateData.setSpecification("Updated Spec");
        updateData.setSupplier(null); // Remove supplier
        updateData.setIsActive(true);
        updateData.setLotManaged(false);

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(testMaterial));
        when(materialRepository.save(any(MaterialEntity.class)))
                .thenReturn(testMaterial);
        when(materialRepository.findByIdWithAllRelations(materialId))
                .thenReturn(Optional.of(testMaterial));

        MaterialEntity result = materialService.updateMaterial(materialId, updateData);

        assertThat(result).isNotNull();
        assertThat(testMaterial.getSupplier()).isNull();
    }

    @Test
    @DisplayName("자재 수정 - 실패 (존재하지 않음)")
    void testUpdateMaterial_Fail_NotFound() {
        MaterialEntity updateData = new MaterialEntity();
        updateData.setMaterialName("Updated Material");
        updateData.setIsActive(true);
        updateData.setLotManaged(false);

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.updateMaterial(materialId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Material not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("자재 삭제 - 성공")
    void testDeleteMaterial_Success() {
        when(materialRepository.existsById(materialId))
                .thenReturn(true);

        materialService.deleteMaterial(materialId);

        verify(materialRepository).deleteById(materialId);
    }

    @Test
    @DisplayName("자재 삭제 - 실패 (존재하지 않음)")
    void testDeleteMaterial_Fail_NotFound() {
        when(materialRepository.existsById(materialId))
                .thenReturn(false);

        assertThatThrownBy(() -> materialService.deleteMaterial(materialId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Material not found");
    }

    // === 상태 관리 테스트 ===

    @Test
    @DisplayName("활성 상태 토글 - 성공 (활성 → 비활성)")
    void testToggleActive_Success_ActiveToInactive() {
        testMaterial.setIsActive(true);

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(testMaterial));
        when(materialRepository.save(any(MaterialEntity.class)))
                .thenReturn(testMaterial);
        when(materialRepository.findByIdWithAllRelations(materialId))
                .thenReturn(Optional.of(testMaterial));

        MaterialEntity result = materialService.toggleActive(materialId);

        assertThat(result).isNotNull();
        assertThat(testMaterial.getIsActive()).isFalse();
        verify(materialRepository).save(testMaterial);
    }

    @Test
    @DisplayName("활성 상태 토글 - 성공 (비활성 → 활성)")
    void testToggleActive_Success_InactiveToActive() {
        testMaterial.setIsActive(false);

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(testMaterial));
        when(materialRepository.save(any(MaterialEntity.class)))
                .thenReturn(testMaterial);
        when(materialRepository.findByIdWithAllRelations(materialId))
                .thenReturn(Optional.of(testMaterial));

        MaterialEntity result = materialService.toggleActive(materialId);

        assertThat(result).isNotNull();
        assertThat(testMaterial.getIsActive()).isTrue();
        verify(materialRepository).save(testMaterial);
    }

    @Test
    @DisplayName("활성 상태 토글 - 실패 (존재하지 않음)")
    void testToggleActive_Fail_NotFound() {
        when(materialRepository.findById(materialId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.toggleActive(materialId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Material not found");
    }
}
