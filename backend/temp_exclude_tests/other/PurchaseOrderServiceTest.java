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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
 * Purchase Order Service Test
 */
@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private TenantEntity testTenant;
    private UserEntity testBuyer;
    private SupplierEntity testSupplier;
    private MaterialEntity testMaterial;
    private PurchaseRequestEntity testPurchaseRequest;
    private PurchaseOrderEntity testPurchaseOrder;
    private PurchaseOrderItemEntity testOrderItem;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("Test Tenant");

        // 테스트 구매자
        testBuyer = new UserEntity();
        testBuyer.setUserId(1L);
        testBuyer.setUsername("buyer01");
        testBuyer.setFullName("구매 담당자");

        // 테스트 공급업체
        testSupplier = new SupplierEntity();
        testSupplier.setSupplierId(1L);
        testSupplier.setSupplierCode("SUP-001");
        testSupplier.setSupplierName("Test Supplier");

        // 테스트 자재
        testMaterial = new MaterialEntity();
        testMaterial.setMaterialId(1L);
        testMaterial.setMaterialCode("MAT-001");
        testMaterial.setMaterialName("Test Material");
        testMaterial.setStandardPrice(new BigDecimal("10000"));

        // 테스트 구매 요청
        testPurchaseRequest = new PurchaseRequestEntity();
        testPurchaseRequest.setPurchaseRequestId(1L);
        testPurchaseRequest.setRequestNo("PR-001");
        testPurchaseRequest.setStatus("PENDING");

        // 테스트 구매 주문 아이템
        testOrderItem = new PurchaseOrderItemEntity();
        testOrderItem.setPurchaseOrderItemId(1L);
        testOrderItem.setMaterial(testMaterial);
        testOrderItem.setOrderedQuantity(new BigDecimal("100"));
        testOrderItem.setUnitPrice(new BigDecimal("10000"));
        testOrderItem.setAmount(new BigDecimal("1000000"));
        testOrderItem.setReceivedQuantity(BigDecimal.ZERO);

        // 테스트 구매 주문
        testPurchaseOrder = new PurchaseOrderEntity();
        testPurchaseOrder.setPurchaseOrderId(1L);
        testPurchaseOrder.setOrderNo("PO-2026-001");
        testPurchaseOrder.setStatus("DRAFT");
        testPurchaseOrder.setOrderDate(LocalDateTime.now());
        testPurchaseOrder.setTotalAmount(new BigDecimal("1000000"));
        testPurchaseOrder.setTenant(testTenant);
        testPurchaseOrder.setBuyer(testBuyer);
        testPurchaseOrder.setSupplier(testSupplier);
        testPurchaseOrder.setItems(new ArrayList<>(Arrays.asList(testOrderItem)));
        testPurchaseOrder.setCurrency("KRW");
        testPurchaseOrder.setPaymentTerms("NET30");
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("구매 주문 조회 - 전체 조회 성공")
    void testGetAllPurchaseOrders_Success() {
        // Given
        String tenantId = "TEST001";
        List<PurchaseOrderEntity> expectedList = Arrays.asList(testPurchaseOrder);

        when(purchaseOrderRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<PurchaseOrderEntity> result = purchaseOrderService.getAllPurchaseOrders(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNo()).isEqualTo("PO-2026-001");
        verify(purchaseOrderRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("구매 주문 조회 - ID로 조회 성공")
    void testGetPurchaseOrderById_Success() {
        // Given
        Long purchaseOrderId = 1L;

        when(purchaseOrderRepository.findByIdWithAllRelations(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When
        PurchaseOrderEntity result = purchaseOrderService.getPurchaseOrderById(purchaseOrderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPurchaseOrderId()).isEqualTo(purchaseOrderId);
        verify(purchaseOrderRepository, times(1)).findByIdWithAllRelations(purchaseOrderId);
    }

    @Test
    @DisplayName("구매 주문 조회 - ID로 조회 실패 (없음)")
    void testGetPurchaseOrderById_Fail_NotFound() {
        // Given
        Long purchaseOrderId = 999L;

        when(purchaseOrderRepository.findByIdWithAllRelations(purchaseOrderId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.getPurchaseOrderById(purchaseOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase order not found");
        verify(purchaseOrderRepository, times(1)).findByIdWithAllRelations(purchaseOrderId);
    }

    @Test
    @DisplayName("구매 주문 조회 - 상태별 조회 성공")
    void testGetPurchaseOrdersByStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String status = "DRAFT";
        List<PurchaseOrderEntity> expectedList = Arrays.asList(testPurchaseOrder);

        when(purchaseOrderRepository.findByTenantIdAndStatus(tenantId, status))
                .thenReturn(expectedList);

        // When
        List<PurchaseOrderEntity> result = purchaseOrderService.getPurchaseOrdersByStatus(tenantId, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("DRAFT");
        verify(purchaseOrderRepository, times(1)).findByTenantIdAndStatus(tenantId, status);
    }

    @Test
    @DisplayName("구매 주문 조회 - 공급업체별 조회 성공")
    void testGetPurchaseOrdersBySupplier_Success() {
        // Given
        String tenantId = "TEST001";
        Long supplierId = 1L;
        List<PurchaseOrderEntity> expectedList = Arrays.asList(testPurchaseOrder);

        when(purchaseOrderRepository.findByTenantIdAndSupplierId(tenantId, supplierId))
                .thenReturn(expectedList);

        // When
        List<PurchaseOrderEntity> result = purchaseOrderService.getPurchaseOrdersBySupplier(tenantId, supplierId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(purchaseOrderRepository, times(1)).findByTenantIdAndSupplierId(tenantId, supplierId);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("구매 주문 생성 - 성공")
    void testCreatePurchaseOrder_Success() {
        // Given
        String tenantId = "TEST001";
        PurchaseOrderEntity newOrder = new PurchaseOrderEntity();
        newOrder.setOrderNo("PO-2026-002");
        newOrder.setItems(new ArrayList<>());

        PurchaseOrderItemEntity newItem = new PurchaseOrderItemEntity();
        newItem.setMaterial(testMaterial);
        newItem.setOrderedQuantity(new BigDecimal("50"));
        newItem.setUnitPrice(new BigDecimal("10000"));
        newOrder.getItems().add(newItem);

        UserEntity buyer = new UserEntity();
        buyer.setUserId(1L);
        newOrder.setBuyer(buyer);

        SupplierEntity supplier = new SupplierEntity();
        supplier.setSupplierId(1L);
        newOrder.setSupplier(supplier);

        when(purchaseOrderRepository.existsByTenant_TenantIdAndOrderNo(tenantId, "PO-2026-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testBuyer));
        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(testSupplier));
        when(materialRepository.findById(testMaterial.getMaterialId()))
                .thenReturn(Optional.of(testMaterial));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderEntity saved = invocation.getArgument(0);
                    saved.setPurchaseOrderId(2L);
                    assertThat(saved.getStatus()).isEqualTo("DRAFT");
                    assertThat(saved.getOrderDate()).isNotNull();
                    assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500000"));
                    return saved;
                });
        when(purchaseOrderRepository.findByIdWithAllRelations(2L))
                .thenReturn(Optional.of(newOrder));

        // When
        PurchaseOrderEntity result = purchaseOrderService.createPurchaseOrder(tenantId, newOrder);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrderEntity.class));
        verify(purchaseOrderRepository, times(1)).findByIdWithAllRelations(2L);
    }

    @Test
    @DisplayName("구매 주문 생성 - 실패 (주문 번호 중복)")
    void testCreatePurchaseOrder_Fail_DuplicateOrderNo() {
        // Given
        String tenantId = "TEST001";
        PurchaseOrderEntity newOrder = new PurchaseOrderEntity();
        newOrder.setOrderNo("PO-2026-001");

        when(purchaseOrderRepository.existsByTenant_TenantIdAndOrderNo(tenantId, "PO-2026-001"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(tenantId, newOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase order number already exists");
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 생성 - 구매 요청 연결 및 상태 업데이트")
    void testCreatePurchaseOrder_WithPurchaseRequest() {
        // Given
        String tenantId = "TEST001";
        PurchaseOrderEntity newOrder = new PurchaseOrderEntity();
        newOrder.setOrderNo("PO-2026-003");
        newOrder.setItems(new ArrayList<>());

        PurchaseOrderItemEntity newItem = new PurchaseOrderItemEntity();
        newItem.setMaterial(testMaterial);
        newItem.setOrderedQuantity(new BigDecimal("50"));
        newItem.setUnitPrice(new BigDecimal("10000"));
        newItem.setPurchaseRequest(testPurchaseRequest);
        newOrder.getItems().add(newItem);

        when(purchaseOrderRepository.existsByTenant_TenantIdAndOrderNo(tenantId, "PO-2026-003"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(materialRepository.findById(testMaterial.getMaterialId()))
                .thenReturn(Optional.of(testMaterial));
        when(purchaseRequestRepository.findById(testPurchaseRequest.getPurchaseRequestId()))
                .thenReturn(Optional.of(testPurchaseRequest));
        when(purchaseRequestRepository.save(any(PurchaseRequestEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseRequestEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("ORDERED");
                    return saved;
                });
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderEntity saved = invocation.getArgument(0);
                    saved.setPurchaseOrderId(3L);
                    return saved;
                });
        when(purchaseOrderRepository.findByIdWithAllRelations(3L))
                .thenReturn(Optional.of(newOrder));

        // When
        PurchaseOrderEntity result = purchaseOrderService.createPurchaseOrder(tenantId, newOrder);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseRequestRepository, times(1)).save(any(PurchaseRequestEntity.class));
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 생성 - 기본값 설정 확인")
    void testCreatePurchaseOrder_DefaultValues() {
        // Given
        String tenantId = "TEST001";
        PurchaseOrderEntity newOrder = new PurchaseOrderEntity();
        newOrder.setOrderNo("PO-2026-004");
        // status와 orderDate를 설정하지 않음

        when(purchaseOrderRepository.existsByTenant_TenantIdAndOrderNo(tenantId, "PO-2026-004"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderEntity saved = invocation.getArgument(0);
                    saved.setPurchaseOrderId(4L);
                    assertThat(saved.getStatus()).isEqualTo("DRAFT");
                    assertThat(saved.getOrderDate()).isNotNull();
                    return saved;
                });
        when(purchaseOrderRepository.findByIdWithAllRelations(4L))
                .thenReturn(Optional.of(newOrder));

        // When
        PurchaseOrderEntity result = purchaseOrderService.createPurchaseOrder(tenantId, newOrder);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrderEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("구매 주문 수정 - 성공 (DRAFT 상태)")
    void testUpdatePurchaseOrder_Success() {
        // Given
        Long purchaseOrderId = 1L;
        PurchaseOrderEntity updatedOrder = new PurchaseOrderEntity();
        updatedOrder.setPaymentTerms("NET60");
        updatedOrder.setCurrency("USD");
        updatedOrder.setRemarks("Updated remarks");

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseOrderRepository.findByIdWithAllRelations(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When
        PurchaseOrderEntity result = purchaseOrderService.updatePurchaseOrder(purchaseOrderId, updatedOrder);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 수정 - 실패 (없음)")
    void testUpdatePurchaseOrder_Fail_NotFound() {
        // Given
        Long purchaseOrderId = 999L;
        PurchaseOrderEntity updatedOrder = new PurchaseOrderEntity();

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.updatePurchaseOrder(purchaseOrderId, updatedOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase order not found");
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 수정 - 실패 (DRAFT 아님)")
    void testUpdatePurchaseOrder_Fail_NotDraft() {
        // Given
        Long purchaseOrderId = 1L;
        testPurchaseOrder.setStatus("CONFIRMED");
        PurchaseOrderEntity updatedOrder = new PurchaseOrderEntity();

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.updatePurchaseOrder(purchaseOrderId, updatedOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only draft purchase orders can be updated");
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 수정 - 아이템 업데이트 및 총액 재계산")
    void testUpdatePurchaseOrder_UpdateItems() {
        // Given
        Long purchaseOrderId = 1L;
        PurchaseOrderEntity updatedOrder = new PurchaseOrderEntity();
        updatedOrder.setItems(new ArrayList<>());

        PurchaseOrderItemEntity newItem = new PurchaseOrderItemEntity();
        newItem.setMaterial(testMaterial);
        newItem.setOrderedQuantity(new BigDecimal("200"));
        newItem.setUnitPrice(new BigDecimal("10000"));
        updatedOrder.getItems().add(newItem);

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));
        when(materialRepository.findById(testMaterial.getMaterialId()))
                .thenReturn(Optional.of(testMaterial));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderEntity saved = invocation.getArgument(0);
                    assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2000000"));
                    return saved;
                });
        when(purchaseOrderRepository.findByIdWithAllRelations(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When
        PurchaseOrderEntity result = purchaseOrderService.updatePurchaseOrder(purchaseOrderId, updatedOrder);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrderEntity.class));
    }

    // ================== 확정 테스트 ==================

    @Test
    @DisplayName("구매 주문 확정 - 성공")
    void testConfirmPurchaseOrder_Success() {
        // Given
        Long purchaseOrderId = 1L;

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("CONFIRMED");
                    return saved;
                });
        when(purchaseOrderRepository.findByIdWithAllRelations(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When
        PurchaseOrderEntity result = purchaseOrderService.confirmPurchaseOrder(purchaseOrderId);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 확정 - 실패 (없음)")
    void testConfirmPurchaseOrder_Fail_NotFound() {
        // Given
        Long purchaseOrderId = 999L;

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.confirmPurchaseOrder(purchaseOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase order not found");
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 확정 - 실패 (DRAFT 아님)")
    void testConfirmPurchaseOrder_Fail_NotDraft() {
        // Given
        Long purchaseOrderId = 1L;
        testPurchaseOrder.setStatus("CONFIRMED");

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.confirmPurchaseOrder(purchaseOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only draft purchase orders can be confirmed");
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrderEntity.class));
    }

    // ================== 취소 테스트 ==================

    @Test
    @DisplayName("구매 주문 취소 - 성공")
    void testCancelPurchaseOrder_Success() {
        // Given
        Long purchaseOrderId = 1L;
        testPurchaseOrder.setStatus("CONFIRMED");

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderEntity saved = invocation.getArgument(0);
                    assertThat(saved.getStatus()).isEqualTo("CANCELLED");
                    return saved;
                });
        when(purchaseOrderRepository.findByIdWithAllRelations(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When
        PurchaseOrderEntity result = purchaseOrderService.cancelPurchaseOrder(purchaseOrderId);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 취소 - 실패 (이미 취소됨)")
    void testCancelPurchaseOrder_Fail_AlreadyCancelled() {
        // Given
        Long purchaseOrderId = 1L;
        testPurchaseOrder.setStatus("CANCELLED");

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.cancelPurchaseOrder(purchaseOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase order is already cancelled");
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrderEntity.class));
    }

    @Test
    @DisplayName("구매 주문 취소 - 실패 (이미 수령됨)")
    void testCancelPurchaseOrder_Fail_AlreadyReceived() {
        // Given
        Long purchaseOrderId = 1L;
        testPurchaseOrder.setStatus("RECEIVED");

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.cancelPurchaseOrder(purchaseOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot cancel fully received purchase order");
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrderEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("구매 주문 삭제 - 성공 (DRAFT 상태)")
    void testDeletePurchaseOrder_Success_Draft() {
        // Given
        Long purchaseOrderId = 1L;

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));
        doNothing().when(purchaseOrderRepository).deleteById(purchaseOrderId);

        // When
        purchaseOrderService.deletePurchaseOrder(purchaseOrderId);

        // Then
        verify(purchaseOrderRepository, times(1)).findById(purchaseOrderId);
        verify(purchaseOrderRepository, times(1)).deleteById(purchaseOrderId);
    }

    @Test
    @DisplayName("구매 주문 삭제 - 성공 (CANCELLED 상태)")
    void testDeletePurchaseOrder_Success_Cancelled() {
        // Given
        Long purchaseOrderId = 1L;
        testPurchaseOrder.setStatus("CANCELLED");

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));
        doNothing().when(purchaseOrderRepository).deleteById(purchaseOrderId);

        // When
        purchaseOrderService.deletePurchaseOrder(purchaseOrderId);

        // Then
        verify(purchaseOrderRepository, times(1)).findById(purchaseOrderId);
        verify(purchaseOrderRepository, times(1)).deleteById(purchaseOrderId);
    }

    @Test
    @DisplayName("구매 주문 삭제 - 실패 (없음)")
    void testDeletePurchaseOrder_Fail_NotFound() {
        // Given
        Long purchaseOrderId = 999L;

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.deletePurchaseOrder(purchaseOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Purchase order not found");
        verify(purchaseOrderRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("구매 주문 삭제 - 실패 (CONFIRMED 상태)")
    void testDeletePurchaseOrder_Fail_Confirmed() {
        // Given
        Long purchaseOrderId = 1L;
        testPurchaseOrder.setStatus("CONFIRMED");

        when(purchaseOrderRepository.findById(purchaseOrderId))
                .thenReturn(Optional.of(testPurchaseOrder));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.deletePurchaseOrder(purchaseOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only draft or cancelled purchase orders can be deleted");
        verify(purchaseOrderRepository, never()).deleteById(anyLong());
    }
}
