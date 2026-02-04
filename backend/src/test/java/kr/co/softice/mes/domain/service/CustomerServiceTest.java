package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.CustomerEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.CustomerRepository;
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
 * Customer Service Test
 * 고객 관리 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("고객 서비스 테스트")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private TenantEntity testTenant;
    private CustomerEntity testCustomer;
    private Long customerId;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TEST001";
        customerId = 1L;

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);
        testTenant.setTenantName("Test Tenant");

        testCustomer = new CustomerEntity();
        testCustomer.setCustomerId(customerId);
        testCustomer.setTenant(testTenant);
        testCustomer.setCustomerCode("CUST001");
        testCustomer.setCustomerName("Test Customer");
        testCustomer.setCustomerType("DOMESTIC");
        testCustomer.setIsActive(true);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("테넌트별 고객 조회 - 성공")
    void testFindByTenant_Success() {
        List<CustomerEntity> customers = Arrays.asList(testCustomer);
        when(customerRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(customers);

        List<CustomerEntity> result = customerService.findByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerCode()).isEqualTo("CUST001");
        verify(customerRepository).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("활성 고객 조회 - 성공")
    void testFindActiveByTenant_Success() {
        testCustomer.setIsActive(true);
        List<CustomerEntity> customers = Arrays.asList(testCustomer);
        when(customerRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true))
                .thenReturn(customers);

        List<CustomerEntity> result = customerService.findActiveByTenant(tenantId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(customerRepository).findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    @Test
    @DisplayName("유형별 고객 조회 - 성공")
    void testFindByTenantAndType_Success() {
        List<CustomerEntity> customers = Arrays.asList(testCustomer);
        when(customerRepository.findByTenantIdAndCustomerTypeWithAllRelations(tenantId, "DOMESTIC"))
                .thenReturn(customers);

        List<CustomerEntity> result = customerService.findByTenantAndType(tenantId, "DOMESTIC");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerType()).isEqualTo("DOMESTIC");
        verify(customerRepository).findByTenantIdAndCustomerTypeWithAllRelations(tenantId, "DOMESTIC");
    }

    @Test
    @DisplayName("고객 ID로 조회 - 성공")
    void testFindById_Success() {
        when(customerRepository.findByIdWithAllRelations(customerId))
                .thenReturn(Optional.of(testCustomer));

        Optional<CustomerEntity> result = customerService.findById(customerId);

        assertThat(result).isPresent();
        assertThat(result.get().getCustomerCode()).isEqualTo("CUST001");
        verify(customerRepository).findByIdWithAllRelations(customerId);
    }

    @Test
    @DisplayName("고객 코드로 조회 - 성공")
    void testFindByCustomerCode_Success() {
        when(customerRepository.findByTenant_TenantIdAndCustomerCode(tenantId, "CUST001"))
                .thenReturn(Optional.of(testCustomer));

        Optional<CustomerEntity> result = customerService.findByCustomerCode(tenantId, "CUST001");

        assertThat(result).isPresent();
        assertThat(result.get().getCustomerCode()).isEqualTo("CUST001");
        verify(customerRepository).findByTenant_TenantIdAndCustomerCode(tenantId, "CUST001");
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("고객 생성 - 성공")
    void testCreateCustomer_Success() {
        CustomerEntity newCustomer = new CustomerEntity();
        newCustomer.setTenant(testTenant);
        newCustomer.setCustomerCode("CUST999");
        newCustomer.setCustomerName("New Customer");

        when(customerRepository.existsByTenantAndCustomerCode(testTenant, "CUST999"))
                .thenReturn(false);
        when(customerRepository.save(any(CustomerEntity.class)))
                .thenAnswer(invocation -> {
                    CustomerEntity saved = invocation.getArgument(0);
                    saved.setCustomerId(99L);
                    return saved;
                });
        when(customerRepository.findByIdWithAllRelations(99L))
                .thenReturn(Optional.of(newCustomer));

        CustomerEntity result = customerService.createCustomer(newCustomer);

        assertThat(result).isNotNull();
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    @DisplayName("고객 생성 - 실패 (중복 코드)")
    void testCreateCustomer_Fail_DuplicateCode() {
        CustomerEntity newCustomer = new CustomerEntity();
        newCustomer.setTenant(testTenant);
        newCustomer.setCustomerCode("CUST001");

        when(customerRepository.existsByTenantAndCustomerCode(testTenant, "CUST001"))
                .thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(newCustomer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer code already exists");
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("고객 수정 - 성공")
    void testUpdateCustomer_Success() {
        testCustomer.setCustomerName("Updated Customer");

        when(customerRepository.save(any(CustomerEntity.class)))
                .thenReturn(testCustomer);
        when(customerRepository.findByIdWithAllRelations(customerId))
                .thenReturn(Optional.of(testCustomer));

        CustomerEntity result = customerService.updateCustomer(testCustomer);

        assertThat(result).isNotNull();
        verify(customerRepository).save(testCustomer);
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("고객 삭제 - 성공")
    void testDeleteCustomer_Success() {
        customerService.deleteCustomer(customerId);

        verify(customerRepository).deleteById(customerId);
    }

    // === 상태 토글 테스트 ===

    @Test
    @DisplayName("활성 상태 토글 - 성공 (활성 → 비활성)")
    void testToggleActive_Success_ActiveToInactive() {
        testCustomer.setIsActive(true);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(CustomerEntity.class)))
                .thenAnswer(invocation -> {
                    CustomerEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });
        when(customerRepository.findByIdWithAllRelations(customerId))
                .thenReturn(Optional.of(testCustomer));

        CustomerEntity result = customerService.toggleActive(customerId);

        assertThat(result).isNotNull();
        verify(customerRepository).save(testCustomer);
    }

    @Test
    @DisplayName("활성 상태 토글 - 성공 (비활성 → 활성)")
    void testToggleActive_Success_InactiveToActive() {
        testCustomer.setIsActive(false);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(CustomerEntity.class)))
                .thenAnswer(invocation -> {
                    CustomerEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });
        when(customerRepository.findByIdWithAllRelations(customerId))
                .thenReturn(Optional.of(testCustomer));

        CustomerEntity result = customerService.toggleActive(customerId);

        assertThat(result).isNotNull();
        verify(customerRepository).save(testCustomer);
    }

    @Test
    @DisplayName("활성 상태 토글 - 실패 (존재하지 않음)")
    void testToggleActive_Fail_NotFound() {
        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.toggleActive(customerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer not found");
    }
}
