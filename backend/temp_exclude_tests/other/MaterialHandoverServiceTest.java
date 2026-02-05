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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Material Handover Service Test
 * 자재 인수인계 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("자재 인수인계 서비스 테스트")
class MaterialHandoverServiceTest {

    @Mock
    private MaterialHandoverRepository materialHandoverRepository;

    @Mock
    private MaterialRequestRepository materialRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MaterialHandoverService materialHandoverService;

    private TenantEntity testTenant;
    private UserEntity testDeliverer;
    private UserEntity testReceiver;
    private MaterialRequestEntity testRequest;
    private MaterialHandoverEntity testHandover;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);

        testDeliverer = new UserEntity();
        testDeliverer.setUserId(1L);
        testDeliverer.setUsername("deliverer");

        testReceiver = new UserEntity();
        testReceiver.setUserId(2L);
        testReceiver.setUsername("receiver");

        testRequest = new MaterialRequestEntity();
        testRequest.setMaterialRequestId(1L);
        testRequest.setRequestNo("MR001");
        testRequest.setRequestStatus("ISSUED");
        testRequest.setTenant(testTenant);

        testHandover = new MaterialHandoverEntity();
        testHandover.setMaterialHandoverId(1L);
        testHandover.setHandoverNo("MH001");
        testHandover.setMaterialRequest(testRequest);
        testHandover.setIssuer(testDeliverer);
        testHandover.setReceiver(testReceiver);
        testHandover.setHandoverStatus("PENDING");
        testHandover.setTenant(testTenant);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("테넌트별 인수인계 조회 - 성공")
    void testFindByTenant_Success() {
        List<MaterialHandoverEntity> handovers = Arrays.asList(testHandover);
        when(materialHandoverRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(handovers);

        List<MaterialHandoverEntity> result = materialHandoverService.findByTenant(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHandoverNo()).isEqualTo("MH001");
    }

    @Test
    @DisplayName("ID로 인수인계 조회 - 성공")
    void testFindById_Success() {
        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testHandover));

        Optional<MaterialHandoverEntity> result = materialHandoverService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getHandoverNo()).isEqualTo("MH001");
    }

    @Test
    @DisplayName("불출 신청별 인수인계 조회 - 성공")
    void testFindByMaterialRequest_Success() {
        List<MaterialHandoverEntity> handovers = Arrays.asList(testHandover);
        when(materialHandoverRepository.findByMaterialRequestIdWithRelations(1L))
                .thenReturn(handovers);

        List<MaterialHandoverEntity> result = materialHandoverService.findByMaterialRequest(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("상태별 인수인계 조회 - 성공")
    void testFindByStatus_Success() {
        List<MaterialHandoverEntity> handovers = Arrays.asList(testHandover);
        when(materialHandoverRepository.findByTenantIdAndStatusWithRelations(tenantId, "PENDING"))
                .thenReturn(handovers);

        List<MaterialHandoverEntity> result = materialHandoverService.findByStatus(tenantId, "PENDING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHandoverStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("인수자별 대기 인수인계 조회 - 성공")
    void testFindPendingByReceiver_Success() {
        List<MaterialHandoverEntity> handovers = Arrays.asList(testHandover);
        when(materialHandoverRepository.findPendingHandoversByReceiver(2L))
                .thenReturn(handovers);

        List<MaterialHandoverEntity> result = materialHandoverService.findPendingByReceiver(2L);

        assertThat(result).hasSize(1);
    }

    // === 인수 확인 테스트 ===

    @Test
    @DisplayName("인수 확인 - 성공")
    void testConfirmHandover_Success() {
        MaterialHandoverEntity updatedHandover = new MaterialHandoverEntity();
        updatedHandover.setMaterialHandoverId(1L);
        updatedHandover.setHandoverStatus("CONFIRMED");
        updatedHandover.setMaterialRequest(testRequest);
        updatedHandover.setIssuer(testDeliverer);
        updatedHandover.setReceiver(testReceiver);

        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testHandover))
                .thenReturn(Optional.of(updatedHandover));
        when(materialHandoverRepository.save(any(MaterialHandoverEntity.class)))
                .thenReturn(updatedHandover);
        when(materialHandoverRepository.findByMaterialRequestIdWithRelations(1L))
                .thenReturn(Arrays.asList(testHandover));
        when(materialRequestRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testRequest));

        MaterialHandoverEntity result = materialHandoverService.confirmHandover(
                1L, 2L, "Confirmed");

        assertThat(result.getHandoverStatus()).isEqualTo("CONFIRMED");
        verify(materialHandoverRepository).save(any(MaterialHandoverEntity.class));
    }

    @Test
    @DisplayName("인수 확인 - 실패 (존재하지 않음)")
    void testConfirmHandover_Fail_NotFound() {
        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialHandoverService.confirmHandover(1L, 2L, "Confirmed"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("인수 확인 - 실패 (잘못된 상태)")
    void testConfirmHandover_Fail_InvalidStatus() {
        testHandover.setHandoverStatus("CONFIRMED");
        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testHandover));

        assertThatThrownBy(() -> materialHandoverService.confirmHandover(1L, 2L, "Confirmed"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot confirm");
    }

    @Test
    @DisplayName("인수 확인 - 실패 (권한 없음)")
    void testConfirmHandover_Fail_UnauthorizedReceiver() {
        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testHandover));

        assertThatThrownBy(() -> materialHandoverService.confirmHandover(1L, 999L, "Confirmed"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only assigned receiver");
    }

    @Test
    @DisplayName("인수 확인 - 모든 인수인계 완료 시 신청 자동 완료")
    void testConfirmHandover_AutoCompleteRequest() {
        MaterialHandoverEntity confirmedHandover = new MaterialHandoverEntity();
        confirmedHandover.setMaterialHandoverId(1L);
        confirmedHandover.setHandoverStatus("CONFIRMED");
        confirmedHandover.setMaterialRequest(testRequest);
        confirmedHandover.setIssuer(testDeliverer);
        confirmedHandover.setReceiver(testReceiver);

        // All handovers are confirmed
        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testHandover))
                .thenReturn(Optional.of(confirmedHandover));
        when(materialHandoverRepository.save(any(MaterialHandoverEntity.class)))
                .thenReturn(confirmedHandover);
        when(materialHandoverRepository.findByMaterialRequestIdWithRelations(1L))
                .thenReturn(Arrays.asList(confirmedHandover));
        when(materialRequestRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testRequest));
        when(materialRequestRepository.save(any(MaterialRequestEntity.class)))
                .thenReturn(testRequest);

        materialHandoverService.confirmHandover(1L, 2L, "Confirmed");

        verify(materialRequestRepository).save(argThat(request ->
                "COMPLETED".equals(request.getRequestStatus())));
    }

    // === 인수 거부 테스트 ===

    @Test
    @DisplayName("인수 거부 - 성공")
    void testRejectHandover_Success() {
        MaterialHandoverEntity rejectedHandover = new MaterialHandoverEntity();
        rejectedHandover.setMaterialHandoverId(1L);
        rejectedHandover.setHandoverStatus("REJECTED");
        rejectedHandover.setIssuer(testDeliverer);
        rejectedHandover.setReceiver(testReceiver);

        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testHandover))
                .thenReturn(Optional.of(rejectedHandover));
        when(materialHandoverRepository.save(any(MaterialHandoverEntity.class)))
                .thenReturn(rejectedHandover);

        MaterialHandoverEntity result = materialHandoverService.rejectHandover(
                1L, 2L, "Damaged");

        assertThat(result.getHandoverStatus()).isEqualTo("REJECTED");
        verify(materialHandoverRepository).save(any(MaterialHandoverEntity.class));
    }

    @Test
    @DisplayName("인수 거부 - 실패 (존재하지 않음)")
    void testRejectHandover_Fail_NotFound() {
        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialHandoverService.rejectHandover(1L, 2L, "Damaged"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("인수 거부 - 실패 (잘못된 상태)")
    void testRejectHandover_Fail_InvalidStatus() {
        testHandover.setHandoverStatus("CONFIRMED");
        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testHandover));

        assertThatThrownBy(() -> materialHandoverService.rejectHandover(1L, 2L, "Damaged"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot reject");
    }

    @Test
    @DisplayName("인수 거부 - 실패 (권한 없음)")
    void testRejectHandover_Fail_UnauthorizedReceiver() {
        when(materialHandoverRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testHandover));

        assertThatThrownBy(() -> materialHandoverService.rejectHandover(1L, 999L, "Damaged"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only assigned receiver");
    }
}
