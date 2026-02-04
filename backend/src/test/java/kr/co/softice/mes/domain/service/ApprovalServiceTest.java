package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Approval Service Test
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalLineTemplateRepository templateRepository;

    @Mock
    private ApprovalInstanceRepository instanceRepository;

    @Mock
    private ApprovalDelegationRepository delegationRepository;

    @InjectMocks
    private ApprovalService approvalService;

    private TenantEntity testTenant;
    private ApprovalLineTemplateEntity testTemplate;
    private ApprovalLineStepEntity testStep1;
    private ApprovalLineStepEntity testStep2;
    private ApprovalInstanceEntity testInstance;
    private ApprovalDelegationEntity testDelegation;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("Test Tenant");

        // 테스트 템플릿 단계
        testStep1 = new ApprovalLineStepEntity();
        testStep1.setStepId(1L);
        testStep1.setStepOrder(1);
        testStep1.setStepName("Team Leader Approval");
        testStep1.setStepType("SINGLE");

        testStep2 = new ApprovalLineStepEntity();
        testStep2.setStepId(2L);
        testStep2.setStepOrder(2);
        testStep2.setStepName("Manager Approval");
        testStep2.setStepType("SINGLE");

        // 테스트 템플릿
        testTemplate = new ApprovalLineTemplateEntity();
        testTemplate.setTemplateId(1L);
        testTemplate.setTemplateCode("TPL-001");
        testTemplate.setTemplateName("Standard Template");
        testTemplate.setDocumentType("PURCHASE_REQUEST");
        testTemplate.setApprovalType("SEQUENTIAL");
        testTemplate.setAutoApproveAmount(new BigDecimal("100000"));
        testTemplate.setIsDefault(true);
        testTemplate.setIsActive(true);
        testTemplate.setTenant(testTenant);
        testTemplate.setSteps(Arrays.asList(testStep1, testStep2));

        // 테스트 승인 인스턴스
        testInstance = new ApprovalInstanceEntity();
        testInstance.setInstanceId(1L);
        testInstance.setDocumentType("PURCHASE_REQUEST");
        testInstance.setDocumentId(100L);
        testInstance.setDocumentNo("PR-2026-001");
        testInstance.setDocumentTitle("Office Supplies Purchase");
        testInstance.setDocumentAmount(new BigDecimal("500000"));
        testInstance.setRequesterId(1L);
        testInstance.setRequesterName("John Doe");
        testInstance.setApprovalStatus("PENDING");
        testInstance.setTenant(testTenant);
        testInstance.setTemplate(testTemplate);
        testInstance.setStepInstances(new ArrayList<>());

        // 테스트 위임
        testDelegation = new ApprovalDelegationEntity();
        testDelegation.setDelegationId(1L);
        testDelegation.setDelegatorId(1L);
        testDelegation.setDelegatorName("Delegator User");
        testDelegation.setDelegateId(2L);
        testDelegation.setDelegateName("Delegate User");
        testDelegation.setStartDate(LocalDate.now());
        testDelegation.setEndDate(LocalDate.now().plusDays(7));
        testDelegation.setIsActive(true);
        testDelegation.setTenant(testTenant);
    }

    // ================== 템플릿 관리 테스트 ==================

    @Test
    @DisplayName("템플릿 조회 - 전체 조회 성공")
    void testFindAllTemplates_Success() {
        // Given
        String tenantId = "TEST001";
        List<ApprovalLineTemplateEntity> expectedList = Arrays.asList(testTemplate);

        when(templateRepository.findAllByTenantId(tenantId))
                .thenReturn(expectedList);

        // When
        List<ApprovalLineTemplateEntity> result = approvalService.findAllTemplates(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(templateRepository, times(1)).findAllByTenantId(tenantId);
    }

    @Test
    @DisplayName("템플릿 조회 - ID로 조회 성공")
    void testFindTemplateById_Success() {
        // Given
        Long templateId = 1L;

        when(templateRepository.findByIdWithSteps(templateId))
                .thenReturn(Optional.of(testTemplate));

        // When
        Optional<ApprovalLineTemplateEntity> result = approvalService.findTemplateById(templateId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTemplateId()).isEqualTo(templateId);
        verify(templateRepository, times(1)).findByIdWithSteps(templateId);
    }

    @Test
    @DisplayName("템플릿 조회 - 문서 타입별 조회 성공")
    void testFindTemplatesByDocumentType_Success() {
        // Given
        String tenantId = "TEST001";
        String documentType = "PURCHASE_REQUEST";
        List<ApprovalLineTemplateEntity> expectedList = Arrays.asList(testTemplate);

        when(templateRepository.findByTenantIdAndDocumentType(tenantId, documentType))
                .thenReturn(expectedList);

        // When
        List<ApprovalLineTemplateEntity> result = approvalService.findTemplatesByDocumentType(tenantId, documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(templateRepository, times(1)).findByTenantIdAndDocumentType(tenantId, documentType);
    }

    @Test
    @DisplayName("템플릿 조회 - 기본 템플릿 조회 성공")
    void testFindDefaultTemplate_Success() {
        // Given
        String tenantId = "TEST001";
        String documentType = "PURCHASE_REQUEST";

        when(templateRepository.findDefaultByTenantIdAndDocumentType(tenantId, documentType))
                .thenReturn(Optional.of(testTemplate));

        // When
        Optional<ApprovalLineTemplateEntity> result = approvalService.findDefaultTemplate(tenantId, documentType);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIsDefault()).isTrue();
        verify(templateRepository, times(1)).findDefaultByTenantIdAndDocumentType(tenantId, documentType);
    }

    @Test
    @DisplayName("템플릿 생성 - 성공")
    void testCreateTemplate_Success() {
        // Given
        ApprovalLineTemplateEntity newTemplate = new ApprovalLineTemplateEntity();
        newTemplate.setTemplateCode("TPL-002");
        newTemplate.setTemplateName("New Template");
        newTemplate.setDocumentType("PURCHASE_ORDER");
        newTemplate.setTenant(testTenant);

        when(templateRepository.existsByTenantIdAndTemplateCode("TEST001", "TPL-002"))
                .thenReturn(false);
        when(templateRepository.save(any(ApprovalLineTemplateEntity.class)))
                .thenReturn(newTemplate);

        // When
        ApprovalLineTemplateEntity result = approvalService.createTemplate(newTemplate);

        // Then
        assertThat(result).isNotNull();
        verify(templateRepository, times(1)).save(any(ApprovalLineTemplateEntity.class));
    }

    @Test
    @DisplayName("템플릿 생성 - 기본값으로 설정")
    void testCreateTemplate_Success_SetAsDefault() {
        // Given
        ApprovalLineTemplateEntity newTemplate = new ApprovalLineTemplateEntity();
        newTemplate.setTemplateCode("TPL-003");
        newTemplate.setDocumentType("PURCHASE_REQUEST");
        newTemplate.setIsDefault(true);
        newTemplate.setTenant(testTenant);

        when(templateRepository.existsByTenantIdAndTemplateCode("TEST001", "TPL-003"))
                .thenReturn(false);
        when(templateRepository.findByTenantIdAndDocumentType("TEST001", "PURCHASE_REQUEST"))
                .thenReturn(Arrays.asList(testTemplate));
        when(templateRepository.save(any(ApprovalLineTemplateEntity.class)))
                .thenReturn(newTemplate);

        // When
        ApprovalLineTemplateEntity result = approvalService.createTemplate(newTemplate);

        // Then
        assertThat(result).isNotNull();
        verify(templateRepository, times(2)).save(any(ApprovalLineTemplateEntity.class)); // Old default + new template
    }

    @Test
    @DisplayName("템플릿 생성 - 실패 (중복)")
    void testCreateTemplate_Fail_Duplicate() {
        // Given
        ApprovalLineTemplateEntity newTemplate = new ApprovalLineTemplateEntity();
        newTemplate.setTemplateCode("TPL-001");
        newTemplate.setTenant(testTenant);

        when(templateRepository.existsByTenantIdAndTemplateCode("TEST001", "TPL-001"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> approvalService.createTemplate(newTemplate))
                .isInstanceOf(BusinessException.class);
        verify(templateRepository, never()).save(any(ApprovalLineTemplateEntity.class));
    }

    @Test
    @DisplayName("템플릿 수정 - 성공")
    void testUpdateTemplate_Success() {
        // Given
        ApprovalLineTemplateEntity updateData = new ApprovalLineTemplateEntity();
        updateData.setTemplateId(1L);
        updateData.setTemplateName("Updated Template");
        updateData.setDescription("Updated description");
        updateData.setIsDefault(false);

        when(templateRepository.findById(1L))
                .thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(ApprovalLineTemplateEntity.class)))
                .thenReturn(testTemplate);

        // When
        ApprovalLineTemplateEntity result = approvalService.updateTemplate(updateData);

        // Then
        assertThat(result).isNotNull();
        verify(templateRepository, times(1)).save(any(ApprovalLineTemplateEntity.class));
    }

    @Test
    @DisplayName("템플릿 삭제 - 성공")
    void testDeleteTemplate_Success() {
        // Given
        Long templateId = 1L;

        when(templateRepository.findById(templateId))
                .thenReturn(Optional.of(testTemplate));
        doNothing().when(templateRepository).delete(testTemplate);

        // When
        approvalService.deleteTemplate(templateId);

        // Then
        verify(templateRepository, times(1)).findById(templateId);
        verify(templateRepository, times(1)).delete(testTemplate);
    }

    // ================== 승인 인스턴스 관리 테스트 ==================

    @Test
    @DisplayName("승인 인스턴스 생성 - 성공 (일반 승인)")
    void testCreateApprovalInstance_Success() {
        // Given
        String tenantId = "TEST001";
        String documentType = "PURCHASE_REQUEST";
        BigDecimal documentAmount = new BigDecimal("500000");

        when(instanceRepository.existsByDocument(tenantId, documentType, 100L))
                .thenReturn(false);
        when(templateRepository.findDefaultByTenantIdAndDocumentType(tenantId, documentType))
                .thenReturn(Optional.of(testTemplate));
        when(instanceRepository.save(any(ApprovalInstanceEntity.class)))
                .thenAnswer(invocation -> {
                    ApprovalInstanceEntity instance = invocation.getArgument(0);
                    instance.setInstanceId(1L);
                    return instance;
                });

        // When
        ApprovalInstanceEntity result = approvalService.createApprovalInstance(
                tenantId, documentType, 100L, "PR-2026-001", "Purchase Request",
                documentAmount, 1L, "John Doe", "Sales", "Urgent request"
        );

        // Then
        assertThat(result).isNotNull();
        verify(instanceRepository, times(2)).save(any(ApprovalInstanceEntity.class)); // Initial save + after startApproval
    }

    @Test
    @DisplayName("승인 인스턴스 생성 - 자동 승인 (금액 조건)")
    void testCreateApprovalInstance_AutoApprove() {
        // Given
        String tenantId = "TEST001";
        String documentType = "PURCHASE_REQUEST";
        BigDecimal documentAmount = new BigDecimal("50000"); // Below auto-approve threshold

        when(instanceRepository.existsByDocument(tenantId, documentType, 100L))
                .thenReturn(false);
        when(templateRepository.findDefaultByTenantIdAndDocumentType(tenantId, documentType))
                .thenReturn(Optional.of(testTemplate));
        when(instanceRepository.save(any(ApprovalInstanceEntity.class)))
                .thenAnswer(invocation -> {
                    ApprovalInstanceEntity instance = invocation.getArgument(0);
                    instance.setInstanceId(2L);
                    assertThat(instance.getApprovalStatus()).isEqualTo("APPROVED");
                    assertThat(instance.getFinalApproverId()).isEqualTo(0L);
                    return instance;
                });

        // When
        ApprovalInstanceEntity result = approvalService.createApprovalInstance(
                tenantId, documentType, 100L, "PR-2026-002", "Small Purchase",
                documentAmount, 1L, "John Doe", "Sales", "Auto-approve eligible"
        );

        // Then
        assertThat(result).isNotNull();
        verify(instanceRepository, times(1)).save(any(ApprovalInstanceEntity.class));
    }

    @Test
    @DisplayName("승인 인스턴스 생성 - 실패 (중복)")
    void testCreateApprovalInstance_Fail_Duplicate() {
        // Given
        String tenantId = "TEST001";
        String documentType = "PURCHASE_REQUEST";

        when(instanceRepository.existsByDocument(tenantId, documentType, 100L))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> approvalService.createApprovalInstance(
                tenantId, documentType, 100L, "PR-2026-001", "Purchase Request",
                new BigDecimal("500000"), 1L, "John Doe", "Sales", "Request"
        ))
                .isInstanceOf(BusinessException.class);
        verify(instanceRepository, never()).save(any(ApprovalInstanceEntity.class));
    }

    @Test
    @DisplayName("승인 인스턴스 생성 - 실패 (기본 템플릿 없음)")
    void testCreateApprovalInstance_Fail_NoTemplate() {
        // Given
        String tenantId = "TEST001";
        String documentType = "UNKNOWN_TYPE";

        when(instanceRepository.existsByDocument(tenantId, documentType, 100L))
                .thenReturn(false);
        when(templateRepository.findDefaultByTenantIdAndDocumentType(tenantId, documentType))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> approvalService.createApprovalInstance(
                tenantId, documentType, 100L, "DOC-001", "Document",
                new BigDecimal("500000"), 1L, "John Doe", "Sales", "Request"
        ))
                .isInstanceOf(BusinessException.class);
        verify(instanceRepository, never()).save(any(ApprovalInstanceEntity.class));
    }

    @Test
    @DisplayName("승인 단계 승인 - 성공")
    void testApproveStep_Success() {
        // Given
        Long instanceId = 1L;
        Long stepInstanceId = 1L;
        Long approverId = 1L;

        ApprovalStepInstanceEntity stepInstance = new ApprovalStepInstanceEntity();
        stepInstance.setStepInstanceId(stepInstanceId);
        stepInstance.setApproverId(approverId);
        stepInstance.setApproverName("Approver");
        stepInstance.setStepStatus("PENDING");

        testInstance.getStepInstances().add(stepInstance);

        when(instanceRepository.findByIdWithStepInstances(instanceId))
                .thenReturn(Optional.of(testInstance));
        when(instanceRepository.save(any(ApprovalInstanceEntity.class)))
                .thenReturn(testInstance);

        // When
        approvalService.approveStep(instanceId, stepInstanceId, approverId, "Approved");

        // Then
        verify(instanceRepository, times(1)).save(any(ApprovalInstanceEntity.class));
    }

    @Test
    @DisplayName("승인 단계 승인 - 실패 (권한 없음)")
    void testApproveStep_Fail_Unauthorized() {
        // Given
        Long instanceId = 1L;
        Long stepInstanceId = 1L;
        Long approverId = 999L; // Wrong approver

        ApprovalStepInstanceEntity stepInstance = new ApprovalStepInstanceEntity();
        stepInstance.setStepInstanceId(stepInstanceId);
        stepInstance.setApproverId(1L); // Expected approver
        stepInstance.setStepStatus("PENDING");

        testInstance.getStepInstances().add(stepInstance);

        when(instanceRepository.findByIdWithStepInstances(instanceId))
                .thenReturn(Optional.of(testInstance));

        // When & Then
        assertThatThrownBy(() -> approvalService.approveStep(instanceId, stepInstanceId, approverId, "Approved"))
                .isInstanceOf(BusinessException.class);
        verify(instanceRepository, never()).save(any(ApprovalInstanceEntity.class));
    }

    @Test
    @DisplayName("승인 단계 반려 - 성공")
    void testRejectStep_Success() {
        // Given
        Long instanceId = 1L;
        Long stepInstanceId = 1L;
        Long approverId = 1L;

        ApprovalStepInstanceEntity stepInstance = new ApprovalStepInstanceEntity();
        stepInstance.setStepInstanceId(stepInstanceId);
        stepInstance.setApproverId(approverId);
        stepInstance.setApproverName("Approver");
        stepInstance.setStepStatus("PENDING");

        testInstance.getStepInstances().add(stepInstance);

        when(instanceRepository.findByIdWithStepInstances(instanceId))
                .thenReturn(Optional.of(testInstance));
        when(instanceRepository.save(any(ApprovalInstanceEntity.class)))
                .thenReturn(testInstance);

        // When
        approvalService.rejectStep(instanceId, stepInstanceId, approverId, "Rejected due to budget");

        // Then
        verify(instanceRepository, times(1)).save(any(ApprovalInstanceEntity.class));
    }

    @Test
    @DisplayName("승인 인스턴스 조회 - 대기 중인 승인")
    void testFindPendingApprovalsForUser_Success() {
        // Given
        String tenantId = "TEST001";
        Long userId = 1L;
        List<ApprovalInstanceEntity> expectedList = Arrays.asList(testInstance);

        when(instanceRepository.findPendingByApprover(tenantId, userId))
                .thenReturn(expectedList);

        // When
        List<ApprovalInstanceEntity> result = approvalService.findPendingApprovalsForUser(tenantId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(instanceRepository, times(1)).findPendingByApprover(tenantId, userId);
    }

    @Test
    @DisplayName("승인 인스턴스 조회 - 요청자별 조회")
    void testFindInstancesByRequester_Success() {
        // Given
        String tenantId = "TEST001";
        Long requesterId = 1L;
        List<ApprovalInstanceEntity> expectedList = Arrays.asList(testInstance);

        when(instanceRepository.findByTenantIdAndRequesterId(tenantId, requesterId))
                .thenReturn(expectedList);

        // When
        List<ApprovalInstanceEntity> result = approvalService.findInstancesByRequester(tenantId, requesterId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(instanceRepository, times(1)).findByTenantIdAndRequesterId(tenantId, requesterId);
    }

    @Test
    @DisplayName("승인 인스턴스 취소 - 성공")
    void testCancelInstance_Success() {
        // Given
        Long instanceId = 1L;
        Long requesterId = 1L;

        when(instanceRepository.findById(instanceId))
                .thenReturn(Optional.of(testInstance));
        when(instanceRepository.save(any(ApprovalInstanceEntity.class)))
                .thenReturn(testInstance);

        // When
        approvalService.cancelInstance(instanceId, requesterId);

        // Then
        verify(instanceRepository, times(1)).save(any(ApprovalInstanceEntity.class));
    }

    @Test
    @DisplayName("승인 인스턴스 취소 - 실패 (권한 없음)")
    void testCancelInstance_Fail_Unauthorized() {
        // Given
        Long instanceId = 1L;
        Long requesterId = 999L; // Wrong requester

        when(instanceRepository.findById(instanceId))
                .thenReturn(Optional.of(testInstance));

        // When & Then
        assertThatThrownBy(() -> approvalService.cancelInstance(instanceId, requesterId))
                .isInstanceOf(BusinessException.class);
        verify(instanceRepository, never()).save(any(ApprovalInstanceEntity.class));
    }

    // ================== 위임 관리 테스트 ==================

    @Test
    @DisplayName("위임 생성 - 성공")
    void testCreateDelegation_Success() {
        // Given
        when(delegationRepository.findOverlappingDelegations(
                "TEST001", 1L, testDelegation.getStartDate(), testDelegation.getEndDate()))
                .thenReturn(Arrays.asList());
        when(delegationRepository.save(any(ApprovalDelegationEntity.class)))
                .thenReturn(testDelegation);

        // When
        ApprovalDelegationEntity result = approvalService.createDelegation(testDelegation);

        // Then
        assertThat(result).isNotNull();
        verify(delegationRepository, times(1)).save(any(ApprovalDelegationEntity.class));
    }

    @Test
    @DisplayName("위임 생성 - 실패 (중복 기간)")
    void testCreateDelegation_Fail_Overlapping() {
        // Given
        ApprovalDelegationEntity existingDelegation = new ApprovalDelegationEntity();

        when(delegationRepository.findOverlappingDelegations(
                "TEST001", 1L, testDelegation.getStartDate(), testDelegation.getEndDate()))
                .thenReturn(Arrays.asList(existingDelegation));

        // When & Then
        assertThatThrownBy(() -> approvalService.createDelegation(testDelegation))
                .isInstanceOf(BusinessException.class);
        verify(delegationRepository, never()).save(any(ApprovalDelegationEntity.class));
    }

    @Test
    @DisplayName("위임 조회 - 위임자별 조회")
    void testFindDelegationsByDelegator_Success() {
        // Given
        String tenantId = "TEST001";
        Long delegatorId = 1L;
        List<ApprovalDelegationEntity> expectedList = Arrays.asList(testDelegation);

        when(delegationRepository.findByTenantIdAndDelegatorId(tenantId, delegatorId))
                .thenReturn(expectedList);

        // When
        List<ApprovalDelegationEntity> result = approvalService.findDelegationsByDelegator(tenantId, delegatorId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(delegationRepository, times(1)).findByTenantIdAndDelegatorId(tenantId, delegatorId);
    }

    @Test
    @DisplayName("위임 조회 - 현재 유효한 위임 조회")
    void testFindCurrentDelegations_Success() {
        // Given
        String tenantId = "TEST001";
        List<ApprovalDelegationEntity> expectedList = Arrays.asList(testDelegation);

        when(delegationRepository.findCurrentEffectiveDelegations(tenantId))
                .thenReturn(expectedList);

        // When
        List<ApprovalDelegationEntity> result = approvalService.findCurrentDelegations(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(delegationRepository, times(1)).findCurrentEffectiveDelegations(tenantId);
    }

    @Test
    @DisplayName("위임 비활성화 - 성공")
    void testDeactivateDelegation_Success() {
        // Given
        Long delegationId = 1L;

        when(delegationRepository.findById(delegationId))
                .thenReturn(Optional.of(testDelegation));
        when(delegationRepository.save(any(ApprovalDelegationEntity.class)))
                .thenReturn(testDelegation);

        // When
        approvalService.deactivateDelegation(delegationId);

        // Then
        verify(delegationRepository, times(1)).save(any(ApprovalDelegationEntity.class));
    }

    // ================== 통계 테스트 ==================

    @Test
    @DisplayName("승인 통계 조회 - 성공")
    void testGetStatistics_Success() {
        // Given
        String tenantId = "TEST001";

        when(instanceRepository.countByTenantIdAndStatus(tenantId, "PENDING"))
                .thenReturn(5L);
        when(instanceRepository.countByTenantIdAndStatus(tenantId, "IN_PROGRESS"))
                .thenReturn(3L);
        when(instanceRepository.countByTenantIdAndStatus(tenantId, "APPROVED"))
                .thenReturn(20L);
        when(instanceRepository.countByTenantIdAndStatus(tenantId, "REJECTED"))
                .thenReturn(2L);

        // When
        ApprovalService.ApprovalStatistics result = approvalService.getStatistics(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPending()).isEqualTo(5L);
        assertThat(result.getInProgress()).isEqualTo(3L);
        assertThat(result.getApproved()).isEqualTo(20L);
        assertThat(result.getRejected()).isEqualTo(2L);
        assertThat(result.getTotal()).isEqualTo(30L);
        assertThat(result.getActive()).isEqualTo(8L);
        assertThat(result.getApprovalRate()).isEqualTo(90.909, org.assertj.core.data.Offset.offset(0.01));
    }
}
