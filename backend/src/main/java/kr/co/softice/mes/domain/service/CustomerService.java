package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.CustomerEntity;
import kr.co.softice.mes.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Customer Service
 * 고객 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerEntity> findByTenant(String tenantId) {
        return customerRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public List<CustomerEntity> findActiveByTenant(String tenantId) {
        return customerRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    public List<CustomerEntity> findByTenantAndType(String tenantId, String customerType) {
        return customerRepository.findByTenantIdAndCustomerTypeWithAllRelations(tenantId, customerType);
    }

    public Optional<CustomerEntity> findById(Long customerId) {
        return customerRepository.findByIdWithAllRelations(customerId);
    }

    public Optional<CustomerEntity> findByCustomerCode(String tenantId, String customerCode) {
        return customerRepository.findByTenant_TenantIdAndCustomerCode(tenantId, customerCode);
    }

    @Transactional
    public CustomerEntity createCustomer(CustomerEntity customer) {
        log.info("Creating customer: {} for tenant: {}",
            customer.getCustomerCode(), customer.getTenant().getTenantId());

        if (customerRepository.existsByTenantAndCustomerCode(
            customer.getTenant(), customer.getCustomerCode())) {
            throw new BusinessException(ErrorCode.CUSTOMER_ALREADY_EXISTS);
        }

        CustomerEntity saved = customerRepository.save(customer);
        return customerRepository.findByIdWithAllRelations(saved.getCustomerId()).orElse(saved);
    }

    @Transactional
    public CustomerEntity updateCustomer(CustomerEntity customer) {
        log.info("Updating customer: {}", customer.getCustomerId());
        CustomerEntity updated = customerRepository.save(customer);
        return customerRepository.findByIdWithAllRelations(updated.getCustomerId()).orElse(updated);
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        log.info("Deleting customer: {}", customerId);
        customerRepository.deleteById(customerId);
    }

    @Transactional
    public CustomerEntity toggleActive(Long customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        log.info("Toggling customer {} active status from {} to {}",
            customer.getCustomerCode(), customer.getIsActive(), !customer.getIsActive());

        customer.setIsActive(!customer.getIsActive());
        CustomerEntity updated = customerRepository.save(customer);
        return customerRepository.findByIdWithAllRelations(updated.getCustomerId()).orElse(updated);
    }
}
