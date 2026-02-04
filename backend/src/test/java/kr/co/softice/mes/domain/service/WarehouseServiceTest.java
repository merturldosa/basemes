package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.WarehouseEntity;
import kr.co.softice.mes.domain.repository.WarehouseRepository;
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
 * Warehouse Service Test
 * 창고 마스터 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("창고 서비스 테스트")
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private TenantEntity testTenant;
    private WarehouseEntity testWarehouse;
    private Long warehouseId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        warehouseId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testWarehouse = new WarehouseEntity();
        testWarehouse.setWarehouseId(warehouseId);
        testWarehouse.setTenant(testTenant);
        testWarehouse.setWarehouseCode("WH001");
        testWarehouse.setWarehouseName("Test Warehouse");
        testWarehouse.setWarehouseType("MAIN");
        testWarehouse.setIsActive(true);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("테넌트별 창고 조회 - 성공")
    void testFindByTenant_Success() {
        List<WarehouseEntity> warehouses = Arrays.asList(testWarehouse);
        when(warehouseRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(warehouses);

        List<WarehouseEntity> result = warehouseService.findByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWarehouseCode()).isEqualTo("WH001");
        verify(warehouseRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("활성 창고 조회 - 성공")
    void testFindActiveByTenant_Success() {
        testWarehouse.setIsActive(true);
        List<WarehouseEntity> warehouses = Arrays.asList(testWarehouse);
        when(warehouseRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true))
                .thenReturn(warehouses);

        List<WarehouseEntity> result = warehouseService.findActiveByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(warehouseRepository).findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    @Test
    @DisplayName("창고 ID로 조회 - 성공")
    void testFindById_Success() {
        when(warehouseRepository.findByIdWithAllRelations(warehouseId))
                .thenReturn(Optional.of(testWarehouse));

        Optional<WarehouseEntity> result = warehouseService.findById(warehouseId);

        assertThat(result).isPresent();
        assertThat(result.get().getWarehouseCode()).isEqualTo("WH001");
        verify(warehouseRepository).findByIdWithAllRelations(warehouseId);
    }

    @Test
    @DisplayName("창고 ID로 조회 - 빈 결과")
    void testFindById_Empty() {
        when(warehouseRepository.findByIdWithAllRelations(warehouseId))
                .thenReturn(Optional.empty());

        Optional<WarehouseEntity> result = warehouseService.findById(warehouseId);

        assertThat(result).isEmpty();
        verify(warehouseRepository).findByIdWithAllRelations(warehouseId);
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("창고 생성 - 성공")
    void testCreateWarehouse_Success() {
        WarehouseEntity newWarehouse = new WarehouseEntity();
        newWarehouse.setTenant(testTenant);
        newWarehouse.setWarehouseCode("WH999");
        newWarehouse.setWarehouseName("New Warehouse");

        when(warehouseRepository.existsByTenantAndWarehouseCode(testTenant, "WH999"))
                .thenReturn(false);
        when(warehouseRepository.save(any(WarehouseEntity.class)))
                .thenAnswer(invocation -> {
                    WarehouseEntity saved = invocation.getArgument(0);
                    saved.setWarehouseId(99L);
                    return saved;
                });
        when(warehouseRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newWarehouse));

        WarehouseEntity result = warehouseService.createWarehouse(newWarehouse);

        assertThat(result).isNotNull();
        verify(warehouseRepository).save(any(WarehouseEntity.class));
    }

    @Test
    @DisplayName("창고 생성 - 실패 (중복 코드)")
    void testCreateWarehouse_Fail_DuplicateCode() {
        WarehouseEntity newWarehouse = new WarehouseEntity();
        newWarehouse.setTenant(testTenant);
        newWarehouse.setWarehouseCode("WH001");

        when(warehouseRepository.existsByTenantAndWarehouseCode(testTenant, "WH001"))
                .thenReturn(true);

        assertThatThrownBy(() -> warehouseService.createWarehouse(newWarehouse))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Warehouse code already exists");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("창고 수정 - 성공")
    void testUpdateWarehouse_Success() {
        testWarehouse.setWarehouseName("Updated Warehouse");

        when(warehouseRepository.save(any(WarehouseEntity.class)))
                .thenReturn(testWarehouse);
        when(warehouseRepository.findByIdWithAllRelations(warehouseId))
                .thenReturn(Optional.of(testWarehouse));

        WarehouseEntity result = warehouseService.updateWarehouse(testWarehouse);

        assertThat(result).isNotNull();
        verify(warehouseRepository).save(testWarehouse);
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("창고 삭제 - 성공")
    void testDeleteWarehouse_Success() {
        warehouseService.deleteWarehouse(warehouseId);

        verify(warehouseRepository).deleteById(warehouseId);
    }

    // === 상태 토글 테스트 ===

    @Test
    @DisplayName("활성 상태 토글 - 성공 (활성 → 비활성)")
    void testToggleActive_Success_ActiveToInactive() {
        testWarehouse.setIsActive(true);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.save(any(WarehouseEntity.class)))
                .thenAnswer(invocation -> {
                    WarehouseEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });
        when(warehouseRepository.findByIdWithAllRelations(warehouseId))
                .thenReturn(Optional.of(testWarehouse));

        WarehouseEntity result = warehouseService.toggleActive(warehouseId);

        assertThat(result).isNotNull();
        verify(warehouseRepository).save(testWarehouse);
    }

    @Test
    @DisplayName("활성 상태 토글 - 성공 (비활성 → 활성)")
    void testToggleActive_Success_InactiveToActive() {
        testWarehouse.setIsActive(false);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.save(any(WarehouseEntity.class)))
                .thenAnswer(invocation -> {
                    WarehouseEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });
        when(warehouseRepository.findByIdWithAllRelations(warehouseId))
                .thenReturn(Optional.of(testWarehouse));

        WarehouseEntity result = warehouseService.toggleActive(warehouseId);

        assertThat(result).isNotNull();
        verify(warehouseRepository).save(testWarehouse);
    }

    @Test
    @DisplayName("활성 상태 토글 - 실패 (존재하지 않음)")
    void testToggleActive_Fail_NotFound() {
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.toggleActive(warehouseId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Warehouse not found");
    }
}
