package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.SiteEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.SiteRepository;
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
 * Site Service Test
 * 사업장 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("사업장 서비스 테스트")
class SiteServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private SiteService siteService;

    private TenantEntity testTenant;
    private SiteEntity testSite;
    private Long siteId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        siteId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testSite = new SiteEntity();
        testSite.setSiteId(siteId);
        testSite.setTenant(testTenant);
        testSite.setSiteCode("SITE001");
        testSite.setSiteName("Test Site");
        testSite.setSiteType("FACTORY");
        testSite.setAddress("Seoul, Korea");
        testSite.setCountry("Korea");
        testSite.setIsActive(true);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("테넌트별 사업장 조회 - 성공")
    void testGetAllSitesByTenant_Success() {
        List<SiteEntity> sites = Arrays.asList(testSite);
        when(siteRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(sites);

        List<SiteEntity> result = siteService.getAllSitesByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSiteCode()).isEqualTo("SITE001");
        verify(siteRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("사업장 ID로 조회 - 성공")
    void testGetSiteById_Success() {
        when(siteRepository.findByIdWithAllRelations(siteId))
                .thenReturn(Optional.of(testSite));

        SiteEntity result = siteService.getSiteById(siteId);

        assertThat(result).isNotNull();
        assertThat(result.getSiteCode()).isEqualTo("SITE001");
        verify(siteRepository).findByIdWithAllRelations(siteId);
    }

    @Test
    @DisplayName("사업장 ID로 조회 - 실패 (존재하지 않음)")
    void testGetSiteById_Fail_NotFound() {
        when(siteRepository.findByIdWithAllRelations(siteId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.getSiteById(siteId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Site not found");
    }

    @Test
    @DisplayName("활성 사업장 조회 - 성공")
    void testGetActiveSites_Success() {
        testSite.setIsActive(true);
        List<SiteEntity> sites = Arrays.asList(testSite);
        when(siteRepository.findActiveSitesByTenantId(tenantId))
                .thenReturn(sites);

        List<SiteEntity> result = siteService.getActiveSites(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(siteRepository).findActiveSitesByTenantId(tenantId);
    }

    @Test
    @DisplayName("유형별 사업장 조회 - 성공")
    void testGetSitesByType_Success() {
        List<SiteEntity> sites = Arrays.asList(testSite);
        when(siteRepository.findByTenantIdAndSiteType(tenantId, "FACTORY"))
                .thenReturn(sites);

        List<SiteEntity> result = siteService.getSitesByType(tenantId, "FACTORY");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSiteType()).isEqualTo("FACTORY");
        verify(siteRepository).findByTenantIdAndSiteType(tenantId, "FACTORY");
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("사업장 생성 - 성공")
    void testCreateSite_Success() {
        SiteEntity newSite = new SiteEntity();
        newSite.setTenant(testTenant);
        newSite.setSiteCode("SITE999");
        newSite.setSiteName("New Site");

        when(siteRepository.existsByTenant_TenantIdAndSiteCode(tenantId, "SITE999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(siteRepository.save(any(SiteEntity.class)))
                .thenAnswer(invocation -> {
                    SiteEntity saved = invocation.getArgument(0);
                    saved.setSiteId(99L);
                    assertThat(saved.getIsActive()).isTrue(); // Default value
                    return saved;
                });
        when(siteRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newSite));

        SiteEntity result = siteService.createSite(newSite);

        assertThat(result).isNotNull();
        verify(siteRepository).save(any(SiteEntity.class));
    }

    @Test
    @DisplayName("사업장 생성 - 실패 (중복 코드)")
    void testCreateSite_Fail_DuplicateCode() {
        SiteEntity newSite = new SiteEntity();
        newSite.setTenant(testTenant);
        newSite.setSiteCode("SITE001");

        when(siteRepository.existsByTenant_TenantIdAndSiteCode(tenantId, "SITE001"))
                .thenReturn(true);

        assertThatThrownBy(() -> siteService.createSite(newSite))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Site already exists");
    }

    @Test
    @DisplayName("사업장 생성 - 실패 (테넌트 없음)")
    void testCreateSite_Fail_TenantNotFound() {
        SiteEntity newSite = new SiteEntity();
        newSite.setTenant(testTenant);
        newSite.setSiteCode("SITE999");

        when(siteRepository.existsByTenant_TenantIdAndSiteCode(tenantId, "SITE999"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.createSite(newSite))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("사업장 수정 - 성공")
    void testUpdateSite_Success() {
        SiteEntity updatedSite = new SiteEntity();
        updatedSite.setSiteName("Updated Site");
        updatedSite.setAddress("Busan, Korea");
        updatedSite.setPostalCode("12345");
        updatedSite.setCountry("Korea");
        updatedSite.setRegion("Busan");
        updatedSite.setPhone("051-1234-5678");
        updatedSite.setFax("051-1234-5679");
        updatedSite.setEmail("site@test.com");
        updatedSite.setManagerName("Kim Manager");
        updatedSite.setManagerPhone("010-1234-5678");
        updatedSite.setManagerEmail("kim@test.com");
        updatedSite.setSiteType("WAREHOUSE");
        updatedSite.setRemarks("Test remarks");

        when(siteRepository.findById(siteId))
                .thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(SiteEntity.class)))
                .thenReturn(testSite);
        when(siteRepository.findByIdWithAllRelations(siteId))
                .thenReturn(Optional.of(testSite));

        SiteEntity result = siteService.updateSite(siteId, updatedSite);

        assertThat(result).isNotNull();
        verify(siteRepository).save(testSite);
        verify(siteRepository).findByIdWithAllRelations(siteId);
    }

    @Test
    @DisplayName("사업장 수정 - 실패 (존재하지 않음)")
    void testUpdateSite_Fail_NotFound() {
        SiteEntity updatedSite = new SiteEntity();
        updatedSite.setSiteName("Updated Site");

        when(siteRepository.findById(siteId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.updateSite(siteId, updatedSite))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Site not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("사업장 삭제 - 성공")
    void testDeleteSite_Success() {
        when(siteRepository.findById(siteId))
                .thenReturn(Optional.of(testSite));

        siteService.deleteSite(siteId);

        verify(siteRepository).delete(testSite);
    }

    @Test
    @DisplayName("사업장 삭제 - 실패 (존재하지 않음)")
    void testDeleteSite_Fail_NotFound() {
        when(siteRepository.findById(siteId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.deleteSite(siteId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Site not found");
    }

    // === 상태 토글 테스트 ===

    @Test
    @DisplayName("활성 상태 토글 - 성공 (활성 → 비활성)")
    void testToggleActive_Success_ActiveToInactive() {
        testSite.setIsActive(true);

        when(siteRepository.findById(siteId))
                .thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(SiteEntity.class)))
                .thenAnswer(invocation -> {
                    SiteEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });
        when(siteRepository.findByIdWithAllRelations(siteId))
                .thenReturn(Optional.of(testSite));

        SiteEntity result = siteService.toggleActive(siteId);

        assertThat(result).isNotNull();
        verify(siteRepository).save(testSite);
    }

    @Test
    @DisplayName("활성 상태 토글 - 성공 (비활성 → 활성)")
    void testToggleActive_Success_InactiveToActive() {
        testSite.setIsActive(false);

        when(siteRepository.findById(siteId))
                .thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(SiteEntity.class)))
                .thenAnswer(invocation -> {
                    SiteEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });
        when(siteRepository.findByIdWithAllRelations(siteId))
                .thenReturn(Optional.of(testSite));

        SiteEntity result = siteService.toggleActive(siteId);

        assertThat(result).isNotNull();
        verify(siteRepository).save(testSite);
    }

    @Test
    @DisplayName("활성 상태 토글 - 실패 (존재하지 않음)")
    void testToggleActive_Fail_NotFound() {
        when(siteRepository.findById(siteId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.toggleActive(siteId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Site not found");
    }
}
