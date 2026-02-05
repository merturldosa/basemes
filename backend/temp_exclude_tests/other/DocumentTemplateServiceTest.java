package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.domain.entity.DocumentTemplateEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.DocumentTemplateRepository;
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
 * Document Template Service Test
 * 문서 템플릿 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("문서 템플릿 서비스 테스트")
class DocumentTemplateServiceTest {

    @Mock
    private DocumentTemplateRepository documentTemplateRepository;

    @InjectMocks
    private DocumentTemplateService documentTemplateService;

    private TenantEntity testTenant;
    private DocumentTemplateEntity testTemplate;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);

        testTemplate = DocumentTemplateEntity.builder()
                .templateId(1L)
                .tenant(testTenant)
                .templateCode("TPL001")
                .templateName("Test Template")
                .description("Test Description")
                .templateType("REPORT")
                .category("PRODUCTION")
                .fileName("template.docx")
                .filePath("/templates/template.docx")
                .fileType("DOCX")
                .fileSize(1024L)
                .templateContent("Template content")
                .version("1.0")
                .isLatest(true)
                .displayOrder(1)
                .isActive(true)
                .build();
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("전체 템플릿 조회 - 성공")
    void testFindAllTemplates_Success() {
        List<DocumentTemplateEntity> templates = Arrays.asList(testTemplate);
        when(documentTemplateRepository.findAllByTenantId(tenantId))
                .thenReturn(templates);

        List<DocumentTemplateEntity> result = documentTemplateService.findAllTemplates(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTemplateCode()).isEqualTo("TPL001");
    }

    @Test
    @DisplayName("활성 템플릿 조회 - 성공")
    void testFindActiveTemplates_Success() {
        List<DocumentTemplateEntity> templates = Arrays.asList(testTemplate);
        when(documentTemplateRepository.findActiveByTenantId(tenantId))
                .thenReturn(templates);

        List<DocumentTemplateEntity> result = documentTemplateService.findActiveTemplates(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("타입별 템플릿 조회 - 성공")
    void testFindTemplatesByType_Success() {
        List<DocumentTemplateEntity> templates = Arrays.asList(testTemplate);
        when(documentTemplateRepository.findByTenantIdAndTemplateType(tenantId, "REPORT"))
                .thenReturn(templates);

        List<DocumentTemplateEntity> result = documentTemplateService.findTemplatesByType(tenantId, "REPORT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTemplateType()).isEqualTo("REPORT");
    }

    @Test
    @DisplayName("카테고리별 템플릿 조회 - 성공")
    void testFindTemplatesByCategory_Success() {
        List<DocumentTemplateEntity> templates = Arrays.asList(testTemplate);
        when(documentTemplateRepository.findByTenantIdAndCategory(tenantId, "PRODUCTION"))
                .thenReturn(templates);

        List<DocumentTemplateEntity> result = documentTemplateService.findTemplatesByCategory(tenantId, "PRODUCTION");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("PRODUCTION");
    }

    @Test
    @DisplayName("ID로 템플릿 조회 - 성공")
    void testFindTemplateById_Success() {
        when(documentTemplateRepository.findById(1L))
                .thenReturn(Optional.of(testTemplate));

        Optional<DocumentTemplateEntity> result = documentTemplateService.findTemplateById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTemplateId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ID로 템플릿 조회 - 없음")
    void testFindTemplateById_NotFound() {
        when(documentTemplateRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<DocumentTemplateEntity> result = documentTemplateService.findTemplateById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("코드로 최신 템플릿 조회 - 성공")
    void testFindLatestTemplateByCode_Success() {
        when(documentTemplateRepository.findLatestByTenantIdAndTemplateCode(tenantId, "TPL001"))
                .thenReturn(Optional.of(testTemplate));

        Optional<DocumentTemplateEntity> result = documentTemplateService.findLatestTemplateByCode(tenantId, "TPL001");

        assertThat(result).isPresent();
        assertThat(result.get().getIsLatest()).isTrue();
    }

    @Test
    @DisplayName("코드와 버전으로 템플릿 조회 - 성공")
    void testFindTemplateByCodeAndVersion_Success() {
        when(documentTemplateRepository.findByTenantIdAndTemplateCodeAndVersion(tenantId, "TPL001", "1.0"))
                .thenReturn(Optional.of(testTemplate));

        Optional<DocumentTemplateEntity> result = documentTemplateService.findTemplateByCodeAndVersion(
                tenantId, "TPL001", "1.0");

        assertThat(result).isPresent();
        assertThat(result.get().getVersion()).isEqualTo("1.0");
    }

    @Test
    @DisplayName("모든 버전 조회 - 성공")
    void testFindAllVersions_Success() {
        DocumentTemplateEntity version2 = DocumentTemplateEntity.builder()
                .templateId(2L)
                .tenant(testTenant)
                .templateCode("TPL001")
                .version("2.0")
                .isLatest(false)
                .build();

        List<DocumentTemplateEntity> templates = Arrays.asList(testTemplate, version2);
        when(documentTemplateRepository.findAllVersionsByTenantIdAndTemplateCode(tenantId, "TPL001"))
                .thenReturn(templates);

        List<DocumentTemplateEntity> result = documentTemplateService.findAllVersions(tenantId, "TPL001");

        assertThat(result).hasSize(2);
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("템플릿 생성 - 성공")
    void testCreateTemplate_Success() {
        when(documentTemplateRepository.existsByTenantIdAndTemplateCode(tenantId, "TPL001"))
                .thenReturn(false);
        when(documentTemplateRepository.save(testTemplate))
                .thenReturn(testTemplate);

        DocumentTemplateEntity result = documentTemplateService.createTemplate(testTemplate);

        assertThat(result).isNotNull();
        assertThat(result.getIsLatest()).isTrue();
        verify(documentTemplateRepository).save(testTemplate);
    }

    @Test
    @DisplayName("템플릿 생성 - 실패 (중복 코드)")
    void testCreateTemplate_Fail_DuplicateCode() {
        when(documentTemplateRepository.existsByTenantIdAndTemplateCode(tenantId, "TPL001"))
                .thenReturn(true);

        assertThatThrownBy(() -> documentTemplateService.createTemplate(testTemplate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Template code already exists");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("템플릿 수정 - 성공")
    void testUpdateTemplate_Success() {
        DocumentTemplateEntity updatedTemplate = DocumentTemplateEntity.builder()
                .templateId(1L)
                .templateName("Updated Template")
                .description("Updated Description")
                .build();

        when(documentTemplateRepository.findById(1L))
                .thenReturn(Optional.of(testTemplate));
        when(documentTemplateRepository.save(any(DocumentTemplateEntity.class)))
                .thenReturn(testTemplate);

        DocumentTemplateEntity result = documentTemplateService.updateTemplate(updatedTemplate);

        assertThat(result).isNotNull();
        verify(documentTemplateRepository).save(any(DocumentTemplateEntity.class));
    }

    @Test
    @DisplayName("템플릿 수정 - 실패 (템플릿 없음)")
    void testUpdateTemplate_Fail_NotFound() {
        DocumentTemplateEntity updatedTemplate = DocumentTemplateEntity.builder()
                .templateId(999L)
                .build();

        when(documentTemplateRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentTemplateService.updateTemplate(updatedTemplate))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 버전 생성 테스트 ===

    @Test
    @DisplayName("새 버전 생성 - 성공")
    void testCreateNewVersion_Success() {
        when(documentTemplateRepository.findLatestByTenantIdAndTemplateCode(tenantId, "TPL001"))
                .thenReturn(Optional.of(testTemplate));
        when(documentTemplateRepository.save(any(DocumentTemplateEntity.class)))
                .thenReturn(testTemplate);

        DocumentTemplateEntity result = documentTemplateService.createNewVersion(tenantId, "TPL001", "2.0");

        assertThat(result).isNotNull();
        verify(documentTemplateRepository, times(2)).save(any(DocumentTemplateEntity.class));
    }

    @Test
    @DisplayName("새 버전 생성 - 실패 (템플릿 없음)")
    void testCreateNewVersion_Fail_NotFound() {
        when(documentTemplateRepository.findLatestByTenantIdAndTemplateCode(tenantId, "TPL999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentTemplateService.createNewVersion(tenantId, "TPL999", "2.0"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Template not found");
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("템플릿 삭제 - 성공 (소프트 삭제)")
    void testDeleteTemplate_Success() {
        when(documentTemplateRepository.findById(1L))
                .thenReturn(Optional.of(testTemplate));
        when(documentTemplateRepository.save(any(DocumentTemplateEntity.class)))
                .thenReturn(testTemplate);

        documentTemplateService.deleteTemplate(1L);

        verify(documentTemplateRepository).save(argThat(template ->
                template.getIsActive() == false));
    }

    @Test
    @DisplayName("템플릿 삭제 - 실패 (템플릿 없음)")
    void testDeleteTemplate_Fail_NotFound() {
        when(documentTemplateRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentTemplateService.deleteTemplate(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 활성화/비활성화 테스트 ===

    @Test
    @DisplayName("템플릿 활성화 - 성공")
    void testActivateTemplate_Success() {
        testTemplate.setIsActive(false);
        when(documentTemplateRepository.findById(1L))
                .thenReturn(Optional.of(testTemplate));
        when(documentTemplateRepository.save(any(DocumentTemplateEntity.class)))
                .thenReturn(testTemplate);

        DocumentTemplateEntity result = documentTemplateService.activateTemplate(1L);

        assertThat(result).isNotNull();
        verify(documentTemplateRepository).save(argThat(template ->
                template.getIsActive() == true));
    }

    @Test
    @DisplayName("템플릿 활성화 - 실패 (템플릿 없음)")
    void testActivateTemplate_Fail_NotFound() {
        when(documentTemplateRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentTemplateService.activateTemplate(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("템플릿 비활성화 - 성공")
    void testDeactivateTemplate_Success() {
        when(documentTemplateRepository.findById(1L))
                .thenReturn(Optional.of(testTemplate));
        when(documentTemplateRepository.save(any(DocumentTemplateEntity.class)))
                .thenReturn(testTemplate);

        DocumentTemplateEntity result = documentTemplateService.deactivateTemplate(1L);

        assertThat(result).isNotNull();
        verify(documentTemplateRepository).save(argThat(template ->
                template.getIsActive() == false));
    }

    @Test
    @DisplayName("템플릿 비활성화 - 실패 (템플릿 없음)")
    void testDeactivateTemplate_Fail_NotFound() {
        when(documentTemplateRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentTemplateService.deactivateTemplate(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
