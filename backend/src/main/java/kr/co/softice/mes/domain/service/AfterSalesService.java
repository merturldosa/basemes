package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * After Sales Service
 * A/S 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AfterSalesService {

    private final AfterSalesRepository afterSalesRepository;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ShippingRepository shippingRepository;
    private final UserRepository userRepository;

    /**
     * Get all after sales for tenant
     */
    public List<AfterSalesEntity> getAllAfterSales(String tenantId) {
        log.info("Getting all after sales for tenant: {}", tenantId);
        return afterSalesRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get after sales by ID
     */
    public AfterSalesEntity getAfterSalesById(Long afterSalesId) {
        log.info("Getting after sales by ID: {}", afterSalesId);
        return afterSalesRepository.findByIdWithAllRelations(afterSalesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AFTER_SALES_NOT_FOUND));
    }

    /**
     * Get after sales by service status
     */
    public List<AfterSalesEntity> getAfterSalesByServiceStatus(String tenantId, String serviceStatus) {
        log.info("Getting after sales for tenant: {} with service status: {}", tenantId, serviceStatus);
        return afterSalesRepository.findByTenantIdAndServiceStatus(tenantId, serviceStatus);
    }

    /**
     * Get after sales by priority
     */
    public List<AfterSalesEntity> getAfterSalesByPriority(String tenantId, String priority) {
        log.info("Getting after sales for tenant: {} with priority: {}", tenantId, priority);
        return afterSalesRepository.findByTenantIdAndPriority(tenantId, priority);
    }

    /**
     * Create after sales
     */
    @Transactional
    public AfterSalesEntity createAfterSales(String tenantId, AfterSalesEntity afterSales) {
        log.info("Creating after sales: {} for tenant: {}", afterSales.getAsNo(), tenantId);

        // Check duplicate
        if (afterSalesRepository.existsByTenant_TenantIdAndAsNo(tenantId, afterSales.getAsNo())) {
            throw new BusinessException(ErrorCode.AFTER_SALES_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        afterSales.setTenant(tenant);

        // Get customer
        CustomerEntity customer = customerRepository.findByIdWithAllRelations(afterSales.getCustomer().getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        afterSales.setCustomer(customer);
        afterSales.setCustomerCode(customer.getCustomerCode());
        afterSales.setCustomerName(customer.getCustomerName());

        // Get product
        ProductEntity product = productRepository.findById(afterSales.getProduct().getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        afterSales.setProduct(product);
        afterSales.setProductCode(product.getProductCode());
        afterSales.setProductName(product.getProductName());

        // Set optional relations
        if (afterSales.getSalesOrder() != null && afterSales.getSalesOrder().getSalesOrderId() != null) {
            SalesOrderEntity salesOrder = salesOrderRepository.findByIdWithAllRelations(afterSales.getSalesOrder().getSalesOrderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ORDER_NOT_FOUND));
            afterSales.setSalesOrder(salesOrder);
            afterSales.setSalesOrderNo(salesOrder.getOrderNo());
        }

        if (afterSales.getShipping() != null && afterSales.getShipping().getShippingId() != null) {
            ShippingEntity shipping = shippingRepository.findByIdWithAllRelations(afterSales.getShipping().getShippingId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPING_NOT_FOUND));
            afterSales.setShipping(shipping);
        }

        if (afterSales.getAssignedEngineer() != null && afterSales.getAssignedEngineer().getUserId() != null) {
            UserEntity engineer = userRepository.findById(afterSales.getAssignedEngineer().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            afterSales.setAssignedEngineer(engineer);
            afterSales.setAssignedEngineerName(engineer.getFullName());
            if (afterSales.getAssignedDate() == null) {
                afterSales.setAssignedDate(LocalDateTime.now());
            }
        }

        // Set defaults
        if (afterSales.getServiceCost() == null) {
            afterSales.setServiceCost(BigDecimal.ZERO);
        }
        if (afterSales.getPartsCost() == null) {
            afterSales.setPartsCost(BigDecimal.ZERO);
        }
        if (afterSales.getTotalCost() == null) {
            afterSales.setTotalCost(BigDecimal.ZERO);
        }
        if (afterSales.getChargeToCustomer() == null) {
            afterSales.setChargeToCustomer(BigDecimal.ZERO);
        }
        if (afterSales.getIsActive() == null) {
            afterSales.setIsActive(true);
        }
        if (afterSales.getServiceStatus() == null) {
            afterSales.setServiceStatus("RECEIVED");
        }

        AfterSalesEntity saved = afterSalesRepository.save(afterSales);
        log.info("After sales created successfully: {}", saved.getAsNo());
        return saved;
    }

    /**
     * Update after sales
     */
    @Transactional
    public AfterSalesEntity updateAfterSales(Long afterSalesId, AfterSalesEntity updateData) {
        log.info("Updating after sales ID: {}", afterSalesId);

        AfterSalesEntity existing = afterSalesRepository.findByIdWithAllRelations(afterSalesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AFTER_SALES_NOT_FOUND));

        // Update fields
        if (updateData.getIssueCategory() != null) {
            existing.setIssueCategory(updateData.getIssueCategory());
        }
        if (updateData.getIssueDescription() != null) {
            existing.setIssueDescription(updateData.getIssueDescription());
        }
        if (updateData.getSymptom() != null) {
            existing.setSymptom(updateData.getSymptom());
        }
        if (updateData.getServiceType() != null) {
            existing.setServiceType(updateData.getServiceType());
        }
        if (updateData.getServiceStatus() != null) {
            existing.setServiceStatus(updateData.getServiceStatus());
        }
        if (updateData.getPriority() != null) {
            existing.setPriority(updateData.getPriority());
        }
        if (updateData.getDiagnosis() != null) {
            existing.setDiagnosis(updateData.getDiagnosis());
        }
        if (updateData.getServiceAction() != null) {
            existing.setServiceAction(updateData.getServiceAction());
        }
        if (updateData.getPartsReplaced() != null) {
            existing.setPartsReplaced(updateData.getPartsReplaced());
        }
        if (updateData.getServiceCost() != null) {
            existing.setServiceCost(updateData.getServiceCost());
        }
        if (updateData.getPartsCost() != null) {
            existing.setPartsCost(updateData.getPartsCost());
        }
        if (updateData.getChargeToCustomer() != null) {
            existing.setChargeToCustomer(updateData.getChargeToCustomer());
        }
        if (updateData.getResolutionDescription() != null) {
            existing.setResolutionDescription(updateData.getResolutionDescription());
        }
        if (updateData.getCustomerSatisfaction() != null) {
            existing.setCustomerSatisfaction(updateData.getCustomerSatisfaction());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Auto-calculate total cost
        BigDecimal serviceCost = existing.getServiceCost() != null ? existing.getServiceCost() : BigDecimal.ZERO;
        BigDecimal partsCost = existing.getPartsCost() != null ? existing.getPartsCost() : BigDecimal.ZERO;
        existing.setTotalCost(serviceCost.add(partsCost));

        AfterSalesEntity updated = afterSalesRepository.save(existing);
        log.info("After sales updated successfully: {}", updated.getAsNo());
        return updated;
    }

    /**
     * Start service
     */
    @Transactional
    public AfterSalesEntity startService(Long afterSalesId) {
        log.info("Starting service for after sales ID: {}", afterSalesId);

        AfterSalesEntity afterSales = afterSalesRepository.findByIdWithAllRelations(afterSalesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AFTER_SALES_NOT_FOUND));

        afterSales.setServiceStatus("IN_PROGRESS");
        if (afterSales.getServiceStartDate() == null) {
            afterSales.setServiceStartDate(LocalDateTime.now());
        }

        AfterSalesEntity updated = afterSalesRepository.save(afterSales);
        log.info("Service started successfully: {}", updated.getAsNo());
        return updated;
    }

    /**
     * Complete service
     */
    @Transactional
    public AfterSalesEntity completeService(Long afterSalesId) {
        log.info("Completing service for after sales ID: {}", afterSalesId);

        AfterSalesEntity afterSales = afterSalesRepository.findByIdWithAllRelations(afterSalesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AFTER_SALES_NOT_FOUND));

        afterSales.setServiceStatus("COMPLETED");
        if (afterSales.getServiceEndDate() == null) {
            afterSales.setServiceEndDate(LocalDateTime.now());
        }

        AfterSalesEntity updated = afterSalesRepository.save(afterSales);
        log.info("Service completed successfully: {}", updated.getAsNo());
        return updated;
    }

    /**
     * Close after sales
     */
    @Transactional
    public AfterSalesEntity closeAfterSales(Long afterSalesId) {
        log.info("Closing after sales ID: {}", afterSalesId);

        AfterSalesEntity afterSales = afterSalesRepository.findByIdWithAllRelations(afterSalesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AFTER_SALES_NOT_FOUND));

        afterSales.setServiceStatus("CLOSED");

        AfterSalesEntity updated = afterSalesRepository.save(afterSales);
        log.info("After sales closed successfully: {}", updated.getAsNo());
        return updated;
    }

    /**
     * Delete after sales
     */
    @Transactional
    public void deleteAfterSales(Long afterSalesId) {
        log.info("Deleting after sales ID: {}", afterSalesId);

        AfterSalesEntity afterSales = afterSalesRepository.findById(afterSalesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AFTER_SALES_NOT_FOUND));

        afterSalesRepository.delete(afterSales);
        log.info("After sales deleted successfully: {}", afterSales.getAsNo());
    }
}
