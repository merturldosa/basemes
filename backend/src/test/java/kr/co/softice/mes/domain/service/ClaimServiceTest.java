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
 * Claim Service Test
 */
@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

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
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClaimService claimService;

    private TenantEntity testTenant;
    private CustomerEntity testCustomer;
    private ProductEntity testProduct;
    private SalesOrderEntity testSalesOrder;
    private ShippingEntity testShipping;
    private DepartmentEntity testDepartment;
    private UserEntity testResponsibleUser;
    private ClaimEntity testClaim;

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

        // 테스트 부서
        testDepartment = new DepartmentEntity();
        testDepartment.setDepartmentId(1L);
        testDepartment.setDepartmentCode("DEPT-001");
        testDepartment.setDepartmentName("Quality Department");

        // 테스트 담당자
        testResponsibleUser = new UserEntity();
        testResponsibleUser.setUserId(1L);
        testResponsibleUser.setUsername("responsible01");
        testResponsibleUser.setFullName("Responsible User");

        // 테스트 클레임
        testClaim = new ClaimEntity();
        testClaim.setClaimId(1L);
        testClaim.setClaimNo("CLM-2026-001");
        testClaim.setClaimType("QUALITY");
        testClaim.setClaimCategory("DEFECT");
        testClaim.setClaimDescription("Product defect found");
        testClaim.setClaimedQuantity(new BigDecimal("10"));
        testClaim.setClaimedAmount(new BigDecimal("100000"));
        testClaim.setStatus("RECEIVED");
        testClaim.setSeverity("HIGH");
        testClaim.setPriority("URGENT");
        testClaim.setTenant(testTenant);
        testClaim.setCustomer(testCustomer);
        testClaim.setCustomerCode("CUST-001");
        testClaim.setCustomerName("Test Customer");
        testClaim.setProduct(testProduct);
        testClaim.setProductCode("PROD-001");
        testClaim.setProductName("Test Product");
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("클레임 조회 - 전체 조회 성공")
    void testGetAllClaims_Success() {
        // Given
        String tenantId = "TEST001";
        List<ClaimEntity> expectedList = Arrays.asList(testClaim);

        when(claimRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedList);

        // When
        List<ClaimEntity> result = claimService.getAllClaims(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(claimRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("클레임 조회 - ID로 조회 성공")
    void testGetClaimById_Success() {
        // Given
        Long claimId = 1L;

        when(claimRepository.findByIdWithAllRelations(claimId))
                .thenReturn(Optional.of(testClaim));

        // When
        ClaimEntity result = claimService.getClaimById(claimId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getClaimId()).isEqualTo(claimId);
        verify(claimRepository, times(1)).findByIdWithAllRelations(claimId);
    }

    @Test
    @DisplayName("클레임 조회 - ID로 조회 실패 (없음)")
    void testGetClaimById_Fail_NotFound() {
        // Given
        Long claimId = 999L;

        when(claimRepository.findByIdWithAllRelations(claimId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> claimService.getClaimById(claimId))
                .isInstanceOf(BusinessException.class);
        verify(claimRepository, times(1)).findByIdWithAllRelations(claimId);
    }

    @Test
    @DisplayName("클레임 조회 - 상태별 조회 성공")
    void testGetClaimsByStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String status = "RECEIVED";
        List<ClaimEntity> expectedList = Arrays.asList(testClaim);

        when(claimRepository.findByTenantIdAndStatus(tenantId, status))
                .thenReturn(expectedList);

        // When
        List<ClaimEntity> result = claimService.getClaimsByStatus(tenantId, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(claimRepository, times(1)).findByTenantIdAndStatus(tenantId, status);
    }

    @Test
    @DisplayName("클레임 조회 - 클레임 유형별 조회 성공")
    void testGetClaimsByClaimType_Success() {
        // Given
        String tenantId = "TEST001";
        String claimType = "QUALITY";
        List<ClaimEntity> expectedList = Arrays.asList(testClaim);

        when(claimRepository.findByTenantIdAndClaimType(tenantId, claimType))
                .thenReturn(expectedList);

        // When
        List<ClaimEntity> result = claimService.getClaimsByClaimType(tenantId, claimType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(claimRepository, times(1)).findByTenantIdAndClaimType(tenantId, claimType);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("클레임 생성 - 성공 (전체 정보)")
    void testCreateClaim_Success_FullInfo() {
        // Given
        String tenantId = "TEST001";
        ClaimEntity newClaim = new ClaimEntity();
        newClaim.setClaimNo("CLM-2026-002");
        newClaim.setClaimType("QUALITY");
        newClaim.setClaimDescription("Product defect");

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(1L);
        newClaim.setCustomer(customer);

        ProductEntity product = new ProductEntity();
        product.setProductId(1L);
        newClaim.setProduct(product);

        SalesOrderEntity salesOrder = new SalesOrderEntity();
        salesOrder.setSalesOrderId(1L);
        newClaim.setSalesOrder(salesOrder);

        ShippingEntity shipping = new ShippingEntity();
        shipping.setShippingId(1L);
        newClaim.setShipping(shipping);

        DepartmentEntity department = new DepartmentEntity();
        department.setDepartmentId(1L);
        newClaim.setResponsibleDepartment(department);

        UserEntity responsibleUser = new UserEntity();
        responsibleUser.setUserId(1L);
        newClaim.setResponsibleUser(responsibleUser);

        when(claimRepository.existsByTenant_TenantIdAndClaimNo(tenantId, "CLM-2026-002"))
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
        when(departmentRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testDepartment));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testResponsibleUser));
        when(claimRepository.save(any(ClaimEntity.class)))
                .thenAnswer(invocation -> {
                    ClaimEntity saved = invocation.getArgument(0);
                    saved.setClaimId(2L);
                    assertThat(saved.getCustomerCode()).isEqualTo("CUST-001");
                    assertThat(saved.getProductCode()).isEqualTo("PROD-001");
                    assertThat(saved.getSalesOrderNo()).isEqualTo("SO-2026-001");
                    assertThat(saved.getAssignedDate()).isNotNull();
                    assertThat(saved.getStatus()).isEqualTo("RECEIVED");
                    return saved;
                });

        // When
        ClaimEntity result = claimService.createClaim(tenantId, newClaim);

        // Then
        assertThat(result).isNotNull();
        verify(claimRepository, times(1)).save(any(ClaimEntity.class));
    }

    @Test
    @DisplayName("클레임 생성 - 성공 (최소 정보)")
    void testCreateClaim_Success_MinimalInfo() {
        // Given
        String tenantId = "TEST001";
        ClaimEntity newClaim = new ClaimEntity();
        newClaim.setClaimNo("CLM-2026-003");

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(1L);
        newClaim.setCustomer(customer);

        ProductEntity product = new ProductEntity();
        product.setProductId(1L);
        newClaim.setProduct(product);

        when(claimRepository.existsByTenant_TenantIdAndClaimNo(tenantId, "CLM-2026-003"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(customerRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(claimRepository.save(any(ClaimEntity.class)))
                .thenAnswer(invocation -> {
                    ClaimEntity saved = invocation.getArgument(0);
                    saved.setClaimId(3L);
                    assertThat(saved.getClaimedQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getClaimedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getResolutionAmount()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(saved.getStatus()).isEqualTo("RECEIVED");
                    return saved;
                });

        // When
        ClaimEntity result = claimService.createClaim(tenantId, newClaim);

        // Then
        assertThat(result).isNotNull();
        verify(claimRepository, times(1)).save(any(ClaimEntity.class));
        verify(salesOrderRepository, never()).findByIdWithAllRelations(any());
        verify(shippingRepository, never()).findByIdWithAllRelations(any());
    }

    @Test
    @DisplayName("클레임 생성 - 실패 (중복)")
    void testCreateClaim_Fail_Duplicate() {
        // Given
        String tenantId = "TEST001";
        ClaimEntity newClaim = new ClaimEntity();
        newClaim.setClaimNo("CLM-2026-001");

        when(claimRepository.existsByTenant_TenantIdAndClaimNo(tenantId, "CLM-2026-001"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> claimService.createClaim(tenantId, newClaim))
                .isInstanceOf(BusinessException.class);
        verify(claimRepository, never()).save(any(ClaimEntity.class));
    }

    @Test
    @DisplayName("클레임 생성 - 실패 (테넌트 없음)")
    void testCreateClaim_Fail_TenantNotFound() {
        // Given
        String tenantId = "TEST999";
        ClaimEntity newClaim = new ClaimEntity();
        newClaim.setClaimNo("CLM-2026-002");

        when(claimRepository.existsByTenant_TenantIdAndClaimNo(tenantId, "CLM-2026-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> claimService.createClaim(tenantId, newClaim))
                .isInstanceOf(BusinessException.class);
        verify(claimRepository, never()).save(any(ClaimEntity.class));
    }

    @Test
    @DisplayName("클레임 생성 - 실패 (고객 없음)")
    void testCreateClaim_Fail_CustomerNotFound() {
        // Given
        String tenantId = "TEST001";
        ClaimEntity newClaim = new ClaimEntity();
        newClaim.setClaimNo("CLM-2026-002");

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(999L);
        newClaim.setCustomer(customer);

        when(claimRepository.existsByTenant_TenantIdAndClaimNo(tenantId, "CLM-2026-002"))
                .thenReturn(false);
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(testTenant));
        when(customerRepository.findByIdWithAllRelations(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> claimService.createClaim(tenantId, newClaim))
                .isInstanceOf(BusinessException.class);
        verify(claimRepository, never()).save(any(ClaimEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("클레임 수정 - 성공")
    void testUpdateClaim_Success() {
        // Given
        Long claimId = 1L;
        ClaimEntity updateData = new ClaimEntity();
        updateData.setClaimType("DELIVERY");
        updateData.setClaimCategory("DELAY");
        updateData.setClaimDescription("Late delivery");
        updateData.setSeverity("MEDIUM");
        updateData.setPriority("NORMAL");
        updateData.setInvestigationFindings("Investigation result");
        updateData.setRootCauseAnalysis("Root cause identified");
        updateData.setResolutionType("REPLACEMENT");
        updateData.setResolutionDescription("Product replaced");
        updateData.setResolutionAmount(new BigDecimal("50000"));

        when(claimRepository.findByIdWithAllRelations(claimId))
                .thenReturn(Optional.of(testClaim));
        when(claimRepository.save(any(ClaimEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ClaimEntity result = claimService.updateClaim(claimId, updateData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getClaimType()).isEqualTo("DELIVERY");
        assertThat(result.getClaimCategory()).isEqualTo("DELAY");
        assertThat(result.getClaimDescription()).isEqualTo("Late delivery");
        assertThat(result.getSeverity()).isEqualTo("MEDIUM");
        assertThat(result.getPriority()).isEqualTo("NORMAL");
        assertThat(result.getInvestigationFindings()).isEqualTo("Investigation result");
        assertThat(result.getRootCauseAnalysis()).isEqualTo("Root cause identified");
        assertThat(result.getResolutionType()).isEqualTo("REPLACEMENT");
        assertThat(result.getResolutionDescription()).isEqualTo("Product replaced");
        assertThat(result.getResolutionAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        verify(claimRepository, times(1)).save(any(ClaimEntity.class));
    }

    @Test
    @DisplayName("클레임 수정 - 실패 (없음)")
    void testUpdateClaim_Fail_NotFound() {
        // Given
        Long claimId = 999L;
        ClaimEntity updateData = new ClaimEntity();

        when(claimRepository.findByIdWithAllRelations(claimId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> claimService.updateClaim(claimId, updateData))
                .isInstanceOf(BusinessException.class);
        verify(claimRepository, never()).save(any(ClaimEntity.class));
    }

    // ================== 상태 전환 테스트 ==================

    @Test
    @DisplayName("조사 시작 - 성공")
    void testStartInvestigation_Success() {
        // Given
        Long claimId = 1L;

        when(claimRepository.findByIdWithAllRelations(claimId))
                .thenReturn(Optional.of(testClaim));
        when(claimRepository.save(any(ClaimEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ClaimEntity result = claimService.startInvestigation(claimId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("INVESTIGATING");
        verify(claimRepository, times(1)).save(any(ClaimEntity.class));
    }

    @Test
    @DisplayName("클레임 해결 - 성공")
    void testResolveClaim_Success() {
        // Given
        Long claimId = 1L;
        testClaim.setStatus("INVESTIGATING");

        when(claimRepository.findByIdWithAllRelations(claimId))
                .thenReturn(Optional.of(testClaim));
        when(claimRepository.save(any(ClaimEntity.class)))
                .thenAnswer(invocation -> {
                    ClaimEntity saved = invocation.getArgument(0);
                    assertThat(saved.getResolutionDate()).isNotNull();
                    return saved;
                });

        // When
        ClaimEntity result = claimService.resolveClaim(claimId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("RESOLVED");
        verify(claimRepository, times(1)).save(any(ClaimEntity.class));
    }

    @Test
    @DisplayName("클레임 종료 - 성공")
    void testCloseClaim_Success() {
        // Given
        Long claimId = 1L;
        testClaim.setStatus("RESOLVED");

        when(claimRepository.findByIdWithAllRelations(claimId))
                .thenReturn(Optional.of(testClaim));
        when(claimRepository.save(any(ClaimEntity.class)))
                .thenAnswer(invocation -> {
                    ClaimEntity saved = invocation.getArgument(0);
                    assertThat(saved.getActionCompletionDate()).isNotNull();
                    return saved;
                });

        // When
        ClaimEntity result = claimService.closeClaim(claimId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CLOSED");
        verify(claimRepository, times(1)).save(any(ClaimEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("클레임 삭제 - 성공")
    void testDeleteClaim_Success() {
        // Given
        Long claimId = 1L;

        when(claimRepository.findById(claimId))
                .thenReturn(Optional.of(testClaim));
        doNothing().when(claimRepository).delete(testClaim);

        // When
        claimService.deleteClaim(claimId);

        // Then
        verify(claimRepository, times(1)).findById(claimId);
        verify(claimRepository, times(1)).delete(testClaim);
    }

    @Test
    @DisplayName("클레임 삭제 - 실패 (없음)")
    void testDeleteClaim_Fail_NotFound() {
        // Given
        Long claimId = 999L;

        when(claimRepository.findById(claimId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> claimService.deleteClaim(claimId))
                .isInstanceOf(BusinessException.class);
        verify(claimRepository, never()).delete(any(ClaimEntity.class));
    }
}
