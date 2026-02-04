package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.MaterialEntity;
import kr.co.softice.mes.domain.entity.PurchaseRequestEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.MaterialRepository;
import kr.co.softice.mes.domain.repository.PurchaseRequestRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Purchase Request Service Test
 */
@ExtendWith(MockitoExtension.class)
class PurchaseRequestServiceTest {

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private PurchaseRequestService purchaseRequestService;

    private TenantEntity testTenant;
    private UserEntity testRequester;
    private UserEntity testApprover;
    private MaterialEntity testMaterial;
    private PurchaseRequestEntity testPurchaseRequest;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("Test Tenant");

        // 테스트 요청자
        testRequester = new UserEntity();
        testRequester.setUserId(1L);
        testRequester.setUsername("requester01");
        testRequester.setFullName("요청 담당자");

        // 테스트 승인자
        testApprover = new UserEntity();
        testApprover.setUserId(2L);
        testApprover.setUsername("approver01");
        testApprover.setFullName("승인 담당자");

        // 테스트 자재
        testMaterial = new MaterialEntity();
        testMaterial.setMaterialId(1L);
        testMaterial.setMaterialCode("MAT-001");
        testMaterial.setMaterialName("Test Material");

        // 테스트 구매 요청
        testPurchaseRequest = new PurchaseRequestEntity();
        testPurchaseRequest.setPurchaseRequestId(1L);
        testPurchaseRequest.setRequestNo("PR-2026-001");
        testPurchaseRequest.setStatus("PENDING");
        testPurchaseRequest.setRequestDate(LocalDateTime.now());
        testPurchaseRequest.setRequestedQuantity(new BigDecimal("100"));
        testPurchaseRequest.setRequiredDate(LocalDateTime.now().plusDays(7));
        testPurchaseRequest.setTenant(testTenant);
        testPurchaseRequest.setRequester(testRequester);
        testPurchaseRequest.setMaterial(testMaterial);
        testPurchaseRequest.setPurpose("생산용");
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("구매 요청 조회 - 전체 조회 성공")
    void testGetAllPurchaseRequests_Success() {
        // Given
        String tenantId = "TEST001";
        List<PurchaseRequestEntity> expectedList = Arrays.asList(testPurchaseRequest);

        when(purchaseRequestRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<PurchaseRequestEntity> result = purchaseRequestService.getAllPurchaseRequests(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRequestNo()).isEqualTo("PR-2026-001");
        verify(purchaseRequestRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("구매 요청 조회 - ID로 조회 성공")
    void testGetPurchaseRequestById_Success() {
        // Given
        Long purchaseRequestId = 1L;

        when(purchaseRequestRepository.findByIdWithAllRelations(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));

        // When
        PurchaseRequestEntity result = purchaseRequestService.getPurchaseRequestById(purchaseRequestId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPurchaseRequestId()).isEqualTo(purchaseRequestId);
        verify(purchaseRequestRepository, times(1)).findByIdWithAllRelations(purchaseRequestId);
    }

    @Test
    @DisplayName("구매 요청 조회 - ID로 조회 실패 (없음)")
    void testGetPurchaseRequestById_Fail_NotFound() {
        // Given
        Long purchaseRequestId = 999L;

        when(purchaseRequestRepository.findByIdWithAllRelations(purchaseRequestId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseRequestService.getPurchaseRequestById(purchaseRequestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase request not found");
        verify(purchaseRequestRepository, times(1)).findByIdWithAllRelations(purchaseRequestId);
    }

    @Test
    @DisplayName("구매 요청 조회 - 상태별 조회 성공")
    void testGetPurchaseRequestsByStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String status = "PENDING";
        List<PurchaseRequestEntity> expectedList = Arrays.asList(testPurchaseRequest);

        when(purchaseRequestRepository.findByTenantIdAndStatus(tenantId, status))
                .thenReturn(expectedList);

        // When
        List<PurchaseRequestEntity> result = purchaseRequestService.getPurchaseRequestsByStatus(tenantId, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        verify(purchaseRequestRepository, times(1)).findByTenantIdAndStatus(tenantId, status);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("구매 요청 생성 - 성공")
    void testCreatePurchaseRequest_Success() {
        // Given
        String tenantId = "TEST001";
        PurchaseRequestEntity newRequest = new PurchaseRequestEntity();
        newRequest.setRequestNo("PR-2026-002");
        newRequest.setRequestedQuantity(new BigDecimal("50"));

        UserEntity requester = new UserEntity();
        requester.setUserId(1L);
        newRequest.setRequester(requester);

        MaterialEntity material = new MaterialEntity();
        material.setMaterialId(1L);
        newRequest.setMaterial(material);

        when(purchaseRequestRepository.existsByTenant_TenantIdAndRequestNo(tenantId, "PR-2026-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testRequester));
        when(materialRepository.findById(1L))
                .thenReturn(Optional.of(testMaterial));
        when(purchaseRequestRepository.save(any(PurchaseRequestEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseRequestEntity saved = invocation.getArgument(0);
                    saved.setPurchaseRequestId(2L);
                    assertThat(saved.getStatus()).isEqualTo("PENDING");
                    assertThat(saved.getRequestDate()).isNotNull();
                    return saved;
                });
        when(purchaseRequestRepository.findByIdWithAllRelations(2L))
                .thenReturn(Optional.of(newRequest));

        // When
        PurchaseRequestEntity result = purchaseRequestService.createPurchaseRequest(tenantId, newRequest);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseRequestRepository, times(1)).save(any(PurchaseRequestEntity.class));
        verify(purchaseRequestRepository, times(1)).findByIdWithAllRelations(2L);
    }

    @Test
    @DisplayName("구매 요청 생성 - 실패 (요청 번호 중복)")
    void testCreatePurchaseRequest_Fail_DuplicateRequestNo() {
        // Given
        String tenantId = "TEST001";
        PurchaseRequestEntity newRequest = new PurchaseRequestEntity();
        newRequest.setRequestNo("PR-2026-001");

        when(purchaseRequestRepository.existsByTenant_TenantIdAndRequestNo(tenantId, "PR-2026-001"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> purchaseRequestService.createPurchaseRequest(tenantId, newRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase request number already exists");
        verify(purchaseRequestRepository, never()).save(any(PurchaseRequestEntity.class));
    }

    @Test
    @DisplayName("구매 요청 생성 - 기본값 설정 확인")
    void testCreatePurchaseRequest_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        PurchaseRequestEntity newRequest = new PurchaseRequestEntity();
        newRequest.setRequestNo("PR-2026-003");
        // status와 requestDate를 설정하지 않음

        when(purchaseRequestRepository.existsByTenant_TenantIdAndRequestNo(tenantId, "PR-2026-003"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(purchaseRequestRepository.save(any(PurchaseRequestEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseRequestEntity saved = invocation.getArgument(0);
                    saved.setPurchaseRequestId(3L);
                    assertThat(saved.getStatus()).isEqualTo("PENDING");
                    assertThat(saved.getRequestDate()).isNotNull();
                    return saved;
                });
        when(purchaseRequestRepository.findByIdWithAllRelations(3L))
                .thenReturn(Optional.of(newRequest));

        // When
        PurchaseRequestEntity result = purchaseRequestService.createPurchaseRequest(tenantId, newRequest);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseRequestRepository, times(1)).save(any(PurchaseRequestEntity.class));
    }

    // ================== 승인 테스트 ==================

    @Test
    @DisplayName("구매 요청 승인 - 성공")
    void testApprovePurchaseRequest_Success() {
        // Given
        Long purchaseRequestId = 1L;
        Long approverUserId = 2L;
        String approvalComment = "승인합니다";

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));
        when(userRepository.findById(approverUserId))
                .thenReturn(Optional.of(testApprover));
        when(purchaseRequestRepository.save(any(PurchaseRequestEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseRequestEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("APPROVED");
                    assertThat(saved.getApprovalDate()).isNotNull();
                    assertThat(saved.getApprovalComment()).isEqualTo(approvalComment);
                    return saved;
                });
        when(purchaseRequestRepository.findByIdWithAllRelations(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));

        // When
        PurchaseRequestEntity result = purchaseRequestService.approvePurchaseRequest(purchaseRequestId, approverUserId, approvalComment);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseRequestRepository, times(1)).save(any(PurchaseRequestEntity.class));
    }

    @Test
    @DisplayName("구매 요청 승인 - 실패 (없음)")
    void testApprovePurchaseRequest_Fail_NotFound() {
        // Given
        Long purchaseRequestId = 999L;
        Long approverUserId = 2L;
        String approvalComment = "승인합니다";

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseRequestService.approvePurchaseRequest(purchaseRequestId, approverUserId, approvalComment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase request not found");
        verify(purchaseRequestRepository, never()).save(any(PurchaseRequestEntity.class));
    }

    @Test
    @DisplayName("구매 요청 승인 - 실패 (PENDING 아님)")
    void testApprovePurchaseRequest_Fail_NotPending() {
        // Given
        Long purchaseRequestId = 1L;
        Long approverUserId = 2L;
        String approvalComment = "승인합니다";
        testPurchaseRequest.setStatus("APPROVED");

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));

        // When & Then
        assertThatThrownBy(() -> purchaseRequestService.approvePurchaseRequest(purchaseRequestId, approverUserId, approvalComment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only pending purchase requests can be approved");
        verify(purchaseRequestRepository, never()).save(any(PurchaseRequestEntity.class));
    }

    // ================== 거절 테스트 ==================

    @Test
    @DisplayName("구매 요청 거절 - 성공")
    void testRejectPurchaseRequest_Success() {
        // Given
        Long purchaseRequestId = 1L;
        Long approverUserId = 2L;
        String approvalComment = "거절합니다";

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));
        when(userRepository.findById(approverUserId))
                .thenReturn(Optional.of(testApprover));
        when(purchaseRequestRepository.save(any(PurchaseRequestEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseRequestEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("REJECTED");
                    assertThat(saved.getApprovalDate()).isNotNull();
                    assertThat(saved.getApprovalComment()).isEqualTo(approvalComment);
                    return saved;
                });
        when(purchaseRequestRepository.findByIdWithAllRelations(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));

        // When
        PurchaseRequestEntity result = purchaseRequestService.rejectPurchaseRequest(purchaseRequestId, approverUserId, approvalComment);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseRequestRepository, times(1)).save(any(PurchaseRequestEntity.class));
    }

    @Test
    @DisplayName("구매 요청 거절 - 실패 (없음)")
    void testRejectPurchaseRequest_Fail_NotFound() {
        // Given
        Long purchaseRequestId = 999L;
        Long approverUserId = 2L;
        String approvalComment = "거절합니다";

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseRequestService.rejectPurchaseRequest(purchaseRequestId, approverUserId, approvalComment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase request not found");
        verify(purchaseRequestRepository, never()).save(any(PurchaseRequestEntity.class));
    }

    @Test
    @DisplayName("구매 요청 거절 - 실패 (PENDING 아님)")
    void testRejectPurchaseRequest_Fail_NotPending() {
        // Given
        Long purchaseRequestId = 1L;
        Long approverUserId = 2L;
        String approvalComment = "거절합니다";
        testPurchaseRequest.setStatus("APPROVED");

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));

        // When & Then
        assertThatThrownBy(() -> purchaseRequestService.rejectPurchaseRequest(purchaseRequestId, approverUserId, approvalComment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only pending purchase requests can be rejected");
        verify(purchaseRequestRepository, never()).save(any(PurchaseRequestEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("구매 요청 삭제 - 성공")
    void testDeletePurchaseRequest_Success() {
        // Given
        Long purchaseRequestId = 1L;

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));
        doNothing().when(purchaseRequestRepository).deleteById(purchaseRequestId);

        // When
        purchaseRequestService.deletePurchaseRequest(purchaseRequestId);

        // Then
        verify(purchaseRequestRepository, times(1)).findById(purchaseRequestId);
        verify(purchaseRequestRepository, times(1)).deleteById(purchaseRequestId);
    }

    @Test
    @DisplayName("구매 요청 삭제 - 실패 (없음)")
    void testDeletePurchaseRequest_Fail_NotFound() {
        // Given
        Long purchaseRequestId = 999L;

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseRequestService.deletePurchaseRequest(purchaseRequestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase request not found");
        verify(purchaseRequestRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("구매 요청 삭제 - 실패 (이미 주문됨)")
    void testDeletePurchaseRequest_Fail_AlreadyOrdered() {
        // Given
        Long purchaseRequestId = 1L;
        testPurchaseRequest.setStatus("ORDERED");

        when(purchaseRequestRepository.findById(purchaseRequestId))
                .thenReturn(Optional.of(testPurchaseRequest));

        // When & Then
        assertThatThrownBy(() -> purchaseRequestService.deletePurchaseRequest(purchaseRequestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete purchase request that has been ordered");
        verify(purchaseRequestRepository, never()).deleteById(anyLong());
    }
}
