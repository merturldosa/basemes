package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * After Sales Service Test
 */
@ExtendWith(MockitoExtension.class)
class AfterSalesServiceTest {

    @Mock
    private AfterSalesRepository afterSalesRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AfterSalesService afterSalesService;

    private TenantEntity testTenant;
    private CustomerEntity testCustomer;
    private ProductEntity testProduct;
    private SalesOrderEntity testSalesOrder;
    private ShippingEntity testShipping;
    private UserEntity testEngineer;
    private AfterSalesEntity testAfterSales;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("Test Tenant");

        // 테스트 고객
        testCustomer = new CustomerEntity();
        testCustomer.setCustomerId(1L);
        testCustomer.setCustomerCode("CUST-001");
        testCustomer.setCustomerName("Test Customer");

        // 테스트 제품
        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("PROD-001");
        testProduct.setProductName("Test Product");

        // 테스트 판매 주문
        testSalesOrder = new SalesOrderEntity();
        testSalesOrder.setSalesOrderId(1L);
        testSalesOrder.setOrderNo("SO-2026-001");

        // 테스트 출하
        testShipping = new ShippingEntity();
        testShipping.setShippingId(1L);

        // 테스트 엔지니어
        testEngineer = new UserEntity();
        testEngineer.setUserId(1L);
        testEngineer.setUsername("engineer01");
        testEngineer.setFullName("Test Engineer");

        // 테스트 A/S
        testAfterSales = new AfterSalesEntity();
        testAfterSales.setAfterSalesId(1L);
        testAfterSales.setAsNo("AS-2026-001");
        testAfterSales.setIssueCategory("MALFUNCTION");
        testAfterSales.setIssueDescription("Product not working");
        testAfterSales.setServiceType("REPAIR");
        testAfterSales.setServiceStatus("RECEIVED");
        testAfterSales.setPriority("HIGH");
        testAfterSales.setServiceCost(new BigDecimal("50000"));
        testAfterSales.setPartsCost(new BigDecimal("30000"));
        testAfterSales.setTotalCost(new BigDecimal("80000"));
        testAfterSales.setChargeToCustomer(new BigDecimal("80000"));
        testAfterSales.setTenant(testTenant);
        testAfterSales.setCustomer(testCustomer);
        testAfterSales.setCustomerCode("CUST-001");
        testAfterSales.setCustomerName("Test Customer");
        testAfterSales.setProduct(testProduct);
        testAfterSales.setProductCode("PROD-001");
        testAfterSales.setProductName("Test Product");
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("A/S 조회 - 전체 조회 성공")
    void testGetAllAfterSales_Success() {
        // Given
        String tenantId = "TEST001";
        List<AfterSalesEntity> expectedList = Arrays.asList(testAfterSales);

        when(afterSalesRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<AfterSalesEntity> result = afterSalesService.getAllAfterSales(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(afterSalesRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("A/S 조회 - ID로 조회 성공")
    void testGetAfterSalesById_Success() {
        // Given
        Long afterSalesId = 1L;

        when(afterSalesRepository.findByIdWithAllRelations(afterSalesId))
                .thenReturn(Optional.of(testAfterSales));

        // When
        AfterSalesEntity result = afterSalesService.getAfterSalesById(afterSalesId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAfterSalesId()).isEqualTo(afterSalesId);
        verify(afterSalesRepository, times(1)).findByIdWithAllRelations(afterSalesId);
    }

    @Test
    @DisplayName("A/S 조회 - ID로 조회 실패 (없음)")
    void testGetAfterSalesById_Fail_NotFound() {
        // Given
        Long afterSalesId = 999L;

        when(afterSalesRepository.findByIdWithAllRelations(afterSalesId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> afterSalesService.getAfterSalesById(afterSalesId))
                .isInstanceOf(BusinessException.class);
        verify(afterSalesRepository, times(1)).findByIdWithAllRelations(afterSalesId);
    }

    @Test
    @DisplayName("A/S 조회 - 서비스 상태별 조회 성공")
    void testGetAfterSalesByServiceStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String serviceStatus = "RECEIVED";
        List<AfterSalesEntity> expectedList = Arrays.asList(testAfterSales);

        when(afterSalesRepository.findByTenantIdAndServiceStatus(tenantId, serviceStatus))
                .thenReturn(expectedList);

        // When
        List<AfterSalesEntity> result = afterSalesService.getAfterSalesByServiceStatus(tenantId, serviceStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(afterSalesRepository, times(1)).findByTenantIdAndServiceStatus(tenantId, serviceStatus);
    }

    @Test
    @DisplayName("A/S 조회 - 우선순위별 조회 성공")
    void testGetAfterSalesByPriority_Success() {
        // Given
        String tenantId = "TEST001";
        String priority = "HIGH";
        List<AfterSalesEntity> expectedList = Arrays.asList(testAfterSales);

        when(afterSalesRepository.findByTenantIdAndPriority(tenantId, priority))
                .thenReturn(expectedList);

        // When
        List<AfterSalesEntity> result = afterSalesService.getAfterSalesByPriority(tenantId, priority);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(afterSalesRepository, times(1)).findByTenantIdAndPriority(tenantId, priority);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("A/S 생성 - 성공 (전체 정보)")
    void testCreateAfterSales_Success_FullInfo() {
        // Given
        String tenantId = "TEST001";
        AfterSalesEntity newAfterSales = new AfterSalesEntity();
        newAfterSales.setAsNo("AS-2026-002");
        newAfterSales.setIssueCategory("MALFUNCTION");
        newAfterSales.setIssueDescription("Product issue");

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(1L);
        newAfterSales.setCustomer(customer);

        ProductEntity product = new ProductEntity();
        product.setProductId(1L);
        newAfterSales.setProduct(product);

        SalesOrderEntity salesOrder = new SalesOrderEntity();
        salesOrder.setSalesOrderId(1L);
        newAfterSales.setSalesOrder(salesOrder);

        ShippingEntity shipping = new ShippingEntity();
        shipping.setShippingId(1L);
        newAfterSales.setShipping(shipping);

        UserEntity engineer = new UserEntity();
        engineer.setUserId(1L);
        newAfterSales.setAssignedEngineer(engineer);

        when(afterSalesRepository.existsByTenant_TenantIdAndAsNo(tenantId, "AS-2026-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(customerRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(salesOrderRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testSalesOrder));
        when(shippingRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testShipping));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testEngineer));
        when(afterSalesRepository.save(any(AfterSalesEntity.class)))
                .thenAnswer(invocation -> {
                    AfterSalesEntity saved = invocation.getArgument(0);
                    saved.setAfterSalesId(2L);
                    assertThat(saved.getCustomerCode()).isEqualTo("CUST-001");
                    assertThat(saved.getProductCode()).isEqualTo("PROD-001");
                    assertThat(saved.getSalesOrderNo()).isEqualTo("SO-2026-001");
                    assertThat(saved.getAssignedEngineerName()).isEqualTo("Test Engineer");
                    assertThat(saved.getAssignedDate()).isNotNull();
                    assertThat(saved.getServiceStatus()).isEqualTo("RECEIVED");
                    return saved;
                });

        // When
        AfterSalesEntity result = afterSalesService.createAfterSales(tenantId, newAfterSales);

        // Then
        assertThat(result).isNotNull();
        verify(afterSalesRepository, times(1)).save(any(AfterSalesEntity.class));
    }

    @Test
    @DisplayName("A/S 생성 - 성공 (최소 정보)")
    void testCreateAfterSales_Success_MinimalInfo() {
        // Given
        String tenantId = "TEST001";
        AfterSalesEntity newAfterSales = new AfterSalesEntity();
        newAfterSales.setAsNo("AS-2026-003");

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(1L);
        newAfterSales.setCustomer(customer);

        ProductEntity product = new ProductEntity();
        product.setProductId(1L);
        newAfterSales.setProduct(product);

        when(afterSalesRepository.existsByTenant_TenantIdAndAsNo(tenantId, "AS-2026-003"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(customerRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(afterSalesRepository.save(any(AfterSalesEntity.class)))
                .thenAnswer(invocation -> {
                    AfterSalesEntity saved = invocation.getArgument(0);
                    saved.setAfterSalesId(3L);
                    assertThat(saved.getServiceCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getPartsCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getTotalCost()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getServiceStatus()).isEqualTo("RECEIVED");
                    return saved;
                });

        // When
        AfterSalesEntity result = afterSalesService.createAfterSales(tenantId, newAfterSales);

        // Then
        assertThat(result).isNotNull();
        verify(afterSalesRepository, times(1)).save(any(AfterSalesEntity.class));
        verify(salesOrderRepository, never()).findByIdWithAllRelations(any());
        verify(shippingRepository, never()).findByIdWithAllRelations(any());
    }

    @Test
    @DisplayName("A/S 생성 - 실패 (중복)")
    void testCreateAfterSales_Fail_Duplicate() {
        // Given
        String tenantId = "TEST001";
        AfterSalesEntity newAfterSales = new AfterSalesEntity();
        newAfterSales.setAsNo("AS-2026-001");

        when(afterSalesRepository.existsByTenant_TenantIdAndAsNo(tenantId, "AS-2026-001"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> afterSalesService.createAfterSales(tenantId, newAfterSales))
                .isInstanceOf(BusinessException.class);
        verify(afterSalesRepository, never()).save(any(AfterSalesEntity.class));
    }

    @Test
    @DisplayName("A/S 생성 - 실패 (테넌트 없음)")
    void testCreateAfterSales_Fail_TenantNotFound() {
        // Given
        String tenantId = "TEST999";
        AfterSalesEntity newAfterSales = new AfterSalesEntity();
        newAfterSales.setAsNo("AS-2026-002");

        when(afterSalesRepository.existsByTenant_TenantIdAndAsNo(tenantId, "AS-2026-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> afterSalesService.createAfterSales(tenantId, newAfterSales))
                .isInstanceOf(BusinessException.class);
        verify(afterSalesRepository, never()).save(any(AfterSalesEntity.class));
    }

    @Test
    @DisplayName("A/S 생성 - 실패 (제품 없음)")
    void testCreateAfterSales_Fail_ProductNotFound() {
        // Given
        String tenantId = "TEST001";
        AfterSalesEntity newAfterSales = new AfterSalesEntity();
        newAfterSales.setAsNo("AS-2026-002");

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(1L);
        newAfterSales.setCustomer(customer);

        ProductEntity product = new ProductEntity();
        product.setProductId(999L);
        newAfterSales.setProduct(product);

        when(afterSalesRepository.existsByTenant_TenantIdAndAsNo(tenantId, "AS-2026-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(customerRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> afterSalesService.createAfterSales(tenantId, newAfterSales))
                .isInstanceOf(BusinessException.class);
        verify(afterSalesRepository, never()).save(any(AfterSalesEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("A/S 수정 - 성공 (비용 자동 계산)")
    void testUpdateAfterSales_Success_AutoCalculateTotalCost() {
        // Given
        Long afterSalesId = 1L;
        AfterSalesEntity updateData = new AfterSalesEntity();
        updateData.setServiceCost(new BigDecimal("60000"));
        updateData.setPartsCost(new BigDecimal("40000"));
        updateData.setDiagnosis("Hardware failure");
        updateData.setServiceAction("Replaced component");
        updateData.setPartsReplaced("Motor, Circuit board");

        when(afterSalesRepository.findByIdWithAllRelations(afterSalesId))
                .thenReturn(Optional.of(testAfterSales));
        when(afterSalesRepository.save(any(AfterSalesEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AfterSalesEntity result = afterSalesService.updateAfterSales(afterSalesId, updateData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getServiceCost()).isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(result.getPartsCost()).isEqualByComparingTo(new BigDecimal("40000"));
        assertThat(result.getTotalCost()).isEqualByComparingTo(new BigDecimal("100000")); // Auto-calculated
        assertThat(result.getDiagnosis()).isEqualTo("Hardware failure");
        assertThat(result.getServiceAction()).isEqualTo("Replaced component");
        assertThat(result.getPartsReplaced()).isEqualTo("Motor, Circuit board");
        verify(afterSalesRepository, times(1)).save(any(AfterSalesEntity.class));
    }

    @Test
    @DisplayName("A/S 수정 - 실패 (없음)")
    void testUpdateAfterSales_Fail_NotFound() {
        // Given
        Long afterSalesId = 999L;
        AfterSalesEntity updateData = new AfterSalesEntity();

        when(afterSalesRepository.findByIdWithAllRelations(afterSalesId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> afterSalesService.updateAfterSales(afterSalesId, updateData))
                .isInstanceOf(BusinessException.class);
        verify(afterSalesRepository, never()).save(any(AfterSalesEntity.class));
    }

    // ================== 상태 전환 테스트 ==================

    @Test
    @DisplayName("서비스 시작 - 성공")
    void testStartService_Success() {
        // Given
        Long afterSalesId = 1L;

        when(afterSalesRepository.findByIdWithAllRelations(afterSalesId))
                .thenReturn(Optional.of(testAfterSales));
        when(afterSalesRepository.save(any(AfterSalesEntity.class)))
                .thenAnswer(invocation -> {
                    AfterSalesEntity saved = invocation.getArgument(0);
                    assertThat(saved.getServiceStartDate()).isNotNull();
                    return saved;
                });

        // When
        AfterSalesEntity result = afterSalesService.startService(afterSalesId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getServiceStatus()).isEqualTo("IN_PROGRESS");
        verify(afterSalesRepository, times(1)).save(any(AfterSalesEntity.class));
    }

    @Test
    @DisplayName("서비스 완료 - 성공")
    void testCompleteService_Success() {
        // Given
        Long afterSalesId = 1L;
        testAfterSales.setServiceStatus("IN_PROGRESS");

        when(afterSalesRepository.findByIdWithAllRelations(afterSalesId))
                .thenReturn(Optional.of(testAfterSales));
        when(afterSalesRepository.save(any(AfterSalesEntity.class)))
                .thenAnswer(invocation -> {
                    AfterSalesEntity saved = invocation.getArgument(0);
                    assertThat(saved.getServiceEndDate()).isNotNull();
                    return saved;
                });

        // When
        AfterSalesEntity result = afterSalesService.completeService(afterSalesId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getServiceStatus()).isEqualTo("COMPLETED");
        verify(afterSalesRepository, times(1)).save(any(AfterSalesEntity.class));
    }

    @Test
    @DisplayName("A/S 종료 - 성공")
    void testCloseAfterSales_Success() {
        // Given
        Long afterSalesId = 1L;
        testAfterSales.setServiceStatus("COMPLETED");

        when(afterSalesRepository.findByIdWithAllRelations(afterSalesId))
                .thenReturn(Optional.of(testAfterSales));
        when(afterSalesRepository.save(any(AfterSalesEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AfterSalesEntity result = afterSalesService.closeAfterSales(afterSalesId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getServiceStatus()).isEqualTo("CLOSED");
        verify(afterSalesRepository, times(1)).save(any(AfterSalesEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("A/S 삭제 - 성공")
    void testDeleteAfterSales_Success() {
        // Given
        Long afterSalesId = 1L;

        when(afterSalesRepository.findById(afterSalesId))
                .thenReturn(Optional.of(testAfterSales));
        doNothing().when(afterSalesRepository).delete(testAfterSales);

        // When
        afterSalesService.deleteAfterSales(afterSalesId);

        // Then
        verify(afterSalesRepository, times(1)).findById(afterSalesId);
        verify(afterSalesRepository, times(1)).delete(testAfterSales);
    }

    @Test
    @DisplayName("A/S 삭제 - 실패 (없음)")
    void testDeleteAfterSales_Fail_NotFound() {
        // Given
        Long afterSalesId = 999L;

        when(afterSalesRepository.findById(afterSalesId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> afterSalesService.deleteAfterSales(afterSalesId))
                .isInstanceOf(BusinessException.class);
        verify(afterSalesRepository, never()).delete(any(AfterSalesEntity.class));
    }
}
