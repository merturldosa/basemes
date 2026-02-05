package kr.co.softice.mes.integration;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test Data Factory
 * Helper class to create test entities for integration tests
 * @author Moon Myung-seop
 */
@Component
public class TestDataFactory {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final LotRepository lotRepository;

    public TestDataFactory(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            MaterialRepository materialRepository,
            WarehouseRepository warehouseRepository,
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            LotRepository lotRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.materialRepository = materialRepository;
        this.warehouseRepository = warehouseRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.lotRepository = lotRepository;
    }

    public TenantEntity createTenant(String tenantId) {
        return tenantRepository.save(TenantEntity.builder()
                .tenantId(tenantId)
                .tenantName("Test Tenant " + tenantId)
                .active(true)
                .build());
    }

    public UserEntity createUser(TenantEntity tenant, String username) {
        return userRepository.save(UserEntity.builder()
                .tenant(tenant)
                .username(username)
                .password("password")
                .email(username + "@test.com")
                .fullName("Test User " + username)
                .active(true)
                .build());
    }

    public ProductEntity createProduct(TenantEntity tenant, String productCode) {
        return productRepository.save(ProductEntity.builder()
                .tenant(tenant)
                .productCode(productCode)
                .productName("Test Product " + productCode)
                .productType("FINISHED")
                .category("TEST")
                .unitOfMeasure("EA")
                .standardPrice(new BigDecimal("1000.00"))
                .active(true)
                .build());
    }

    public MaterialEntity createMaterial(TenantEntity tenant, String materialCode) {
        return materialRepository.save(MaterialEntity.builder()
                .tenant(tenant)
                .materialCode(materialCode)
                .materialName("Test Material " + materialCode)
                .materialType("RAW")
                .category("TEST")
                .unitOfMeasure("KG")
                .standardPrice(new BigDecimal("500.00"))
                .active(true)
                .build());
    }

    public WarehouseEntity createWarehouse(TenantEntity tenant, String warehouseCode) {
        return warehouseRepository.save(WarehouseEntity.builder()
                .tenant(tenant)
                .warehouseCode(warehouseCode)
                .warehouseName("Test Warehouse " + warehouseCode)
                .warehouseType("GENERAL")
                .location("Test Location")
                .active(true)
                .build());
    }

    public CustomerEntity createCustomer(TenantEntity tenant, String customerCode) {
        return customerRepository.save(CustomerEntity.builder()
                .tenant(tenant)
                .customerCode(customerCode)
                .customerName("Test Customer " + customerCode)
                .customerType("DOMESTIC")
                .contactPerson("Test Contact")
                .phone("010-1234-5678")
                .email(customerCode + "@customer.com")
                .active(true)
                .build());
    }

    public SupplierEntity createSupplier(TenantEntity tenant, String supplierCode) {
        return supplierRepository.save(SupplierEntity.builder()
                .tenant(tenant)
                .supplierCode(supplierCode)
                .supplierName("Test Supplier " + supplierCode)
                .supplierType("DOMESTIC")
                .contactPerson("Test Contact")
                .phone("010-1234-5678")
                .email(supplierCode + "@supplier.com")
                .active(true)
                .build());
    }

    public LotEntity createLot(TenantEntity tenant, ProductEntity product, String lotNo) {
        return lotRepository.save(LotEntity.builder()
                .tenant(tenant)
                .product(product)
                .lotNo(lotNo)
                .manufactureDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1))
                .status("AVAILABLE")
                .quantity(new BigDecimal("1000"))
                .build());
    }

    public WorkOrderEntity createWorkOrder(
            TenantEntity tenant,
            ProductEntity product,
            String workOrderNo,
            BigDecimal targetQuantity) {
        WorkOrderEntity workOrder = new WorkOrderEntity();
        workOrder.setTenant(tenant);
        workOrder.setProduct(product);
        workOrder.setWorkOrderNo(workOrderNo);
        workOrder.setTargetQuantity(targetQuantity);
        workOrder.setStartDate(LocalDate.now());
        workOrder.setEndDate(LocalDate.now().plusDays(1));
        workOrder.setStatus("PENDING");
        workOrder.setPriority("NORMAL");
        return workOrder;
    }

    public SalesOrderEntity createSalesOrder(
            TenantEntity tenant,
            CustomerEntity customer,
            String orderNo) {
        SalesOrderEntity salesOrder = new SalesOrderEntity();
        salesOrder.setTenant(tenant);
        salesOrder.setCustomer(customer);
        salesOrder.setOrderNo(orderNo);
        salesOrder.setOrderDate(LocalDate.now());
        salesOrder.setDeliveryDate(LocalDate.now().plusDays(7));
        salesOrder.setStatus("DRAFT");
        salesOrder.setTotalAmount(BigDecimal.ZERO);
        return salesOrder;
    }

    public WeighingEntity createWeighing(
            TenantEntity tenant,
            ProductEntity product,
            UserEntity operator,
            String weighingNo,
            BigDecimal tareWeight,
            BigDecimal grossWeight) {
        WeighingEntity weighing = new WeighingEntity();
        weighing.setTenant(tenant);
        weighing.setProduct(product);
        weighing.setOperator(operator);
        weighing.setWeighingNo(weighingNo);
        weighing.setWeighingDate(LocalDateTime.now());
        weighing.setWeighingType("INCOMING");
        weighing.setTareWeight(tareWeight);
        weighing.setGrossWeight(grossWeight);
        weighing.setUnit("kg");
        weighing.setVerificationStatus("PENDING");
        weighing.performCalculations();
        return weighing;
    }
}
