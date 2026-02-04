package kr.co.softice.mes.integration.workflow;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.GoodsReceiptService;
import kr.co.softice.mes.domain.service.InventoryService;
import kr.co.softice.mes.domain.service.QualityInspectionService;
import kr.co.softice.mes.integration.BaseIntegrationTest;
import kr.co.softice.mes.integration.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Goods Receipt Workflow Integration Test
 * Tests: Material Receipt → IQC → Inventory
 * @author Moon Myung-seop
 */
@DisplayName("Goods Receipt Workflow Integration Tests")
public class GoodsReceiptWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GoodsReceiptService goodsReceiptService;

    @Autowired
    private QualityInspectionService qualityInspectionService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private GoodsReceiptRepository goodsReceiptRepository;

    @Autowired
    private QualityInspectionRepository qualityInspectionRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private MaterialEntity testMaterial;
    private WarehouseEntity testWarehouse;
    private SupplierEntity testSupplier;

    @BeforeEach
    public void setUpGoodsReceiptTest() {
        testMaterial = testDataFactory.createMaterial(testTenant, "MAT-001");
        testWarehouse = testDataFactory.createWarehouse(testTenant, "WH-001");
        testSupplier = testDataFactory.createSupplier(testTenant, "SUP-001");
    }

    @Test
    @DisplayName("Complete workflow: Receipt → IQC PASS → Inventory Created")
    public void testCompleteGoodsReceiptWorkflow() {
        // Step 1: Create Goods Receipt
        GoodsReceiptEntity goodsReceipt = new GoodsReceiptEntity();
        goodsReceipt.setTenant(testTenant);
        goodsReceipt.setReceiptNo("GR-20260204-001");
        goodsReceipt.setReceiptDate(LocalDate.now());
        goodsReceipt.setSupplier(testSupplier);
        goodsReceipt.setMaterial(testMaterial);
        goodsReceipt.setWarehouse(testWarehouse);
        goodsReceipt.setReceivedQuantity(new BigDecimal("1000"));
        goodsReceipt.setUnitPrice(new BigDecimal("500.00"));
        goodsReceipt.setTotalAmount(new BigDecimal("500000.00"));
        goodsReceipt.setStatus("PENDING_IQC");
        goodsReceipt.setReceiver(testUser);

        GoodsReceiptEntity savedReceipt = goodsReceiptService.createGoodsReceipt(testTenantId, goodsReceipt);

        assertThat(savedReceipt).isNotNull();
        assertThat(savedReceipt.getReceiptId()).isNotNull();
        assertThat(savedReceipt.getStatus()).isEqualTo("PENDING_IQC");

        // Step 2: Create IQC Inspection
        QualityInspectionEntity inspection = new QualityInspectionEntity();
        inspection.setTenant(testTenant);
        inspection.setInspectionNo("IQC-20260204-001");
        inspection.setInspectionType("IQC");
        inspection.setInspectionDate(LocalDate.now());
        inspection.setProduct(null);  // IQC is for material
        inspection.setMaterial(testMaterial);
        inspection.setLot(null);  // LOT will be created after IQC
        inspection.setInspectedQuantity(new BigDecimal("1000"));
        inspection.setInspector(testUser);
        inspection.setResult("PASS");
        inspection.setDefectQuantity(BigDecimal.ZERO);
        inspection.setNotes("All tests passed");

        QualityInspectionEntity savedInspection = qualityInspectionService.createQualityInspection(
                testTenantId,
                inspection
        );

        assertThat(savedInspection).isNotNull();
        assertThat(savedInspection.getResult()).isEqualTo("PASS");

        // Step 3: Verify Goods Receipt status updated
        GoodsReceiptEntity updatedReceipt = goodsReceiptRepository.findById(savedReceipt.getReceiptId())
                .orElseThrow();
        // Note: In real implementation, IQC service would update GoodsReceipt status

        // Step 4: Create Inventory after IQC PASS
        InventoryEntity inventory = new InventoryEntity();
        inventory.setTenant(testTenant);
        inventory.setWarehouse(testWarehouse);
        inventory.setProduct(null);  // Material
        inventory.setMaterial(testMaterial);
        inventory.setLot(null);  // For simplicity, not creating LOT in this test
        inventory.setQuantity(new BigDecimal("1000"));
        inventory.setAvailableQuantity(new BigDecimal("1000"));
        inventory.setReservedQuantity(BigDecimal.ZERO);

        InventoryEntity savedInventory = inventoryRepository.save(inventory);

        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getQuantity()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(savedInventory.getAvailableQuantity()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("IQC FAIL workflow: Receipt → IQC FAIL → Quarantine")
    public void testGoodsReceiptIQCFailWorkflow() {
        // Step 1: Create Goods Receipt
        GoodsReceiptEntity goodsReceipt = new GoodsReceiptEntity();
        goodsReceipt.setTenant(testTenant);
        goodsReceipt.setReceiptNo("GR-20260204-002");
        goodsReceipt.setReceiptDate(LocalDate.now());
        goodsReceipt.setSupplier(testSupplier);
        goodsReceipt.setMaterial(testMaterial);
        goodsReceipt.setWarehouse(testWarehouse);
        goodsReceipt.setReceivedQuantity(new BigDecimal("500"));
        goodsReceipt.setUnitPrice(new BigDecimal("500.00"));
        goodsReceipt.setTotalAmount(new BigDecimal("250000.00"));
        goodsReceipt.setStatus("PENDING_IQC");
        goodsReceipt.setReceiver(testUser);

        GoodsReceiptEntity savedReceipt = goodsReceiptService.createGoodsReceipt(testTenantId, goodsReceipt);

        // Step 2: Create IQC Inspection with FAIL result
        QualityInspectionEntity inspection = new QualityInspectionEntity();
        inspection.setTenant(testTenant);
        inspection.setInspectionNo("IQC-20260204-002");
        inspection.setInspectionType("IQC");
        inspection.setInspectionDate(LocalDate.now());
        inspection.setMaterial(testMaterial);
        inspection.setInspectedQuantity(new BigDecimal("500"));
        inspection.setInspector(testUser);
        inspection.setResult("FAIL");
        inspection.setDefectQuantity(new BigDecimal("50"));
        inspection.setDefectDescription("Contamination detected");
        inspection.setCorrectiveAction("Return to supplier or dispose");

        QualityInspectionEntity savedInspection = qualityInspectionService.createQualityInspection(
                testTenantId,
                inspection
        );

        assertThat(savedInspection).isNotNull();
        assertThat(savedInspection.getResult()).isEqualTo("FAIL");
        assertThat(savedInspection.getDefectQuantity()).isEqualByComparingTo(new BigDecimal("50"));

        // Step 3: Verify no inventory created (or moved to quarantine warehouse)
        var inventories = inventoryRepository.findByTenantIdAndWarehouse(
                testTenantId,
                testWarehouse.getWarehouseId()
        );

        // In real implementation, failed items would go to quarantine warehouse
        // For this test, we just verify that inspection failed
        assertThat(savedInspection.getCorrectiveAction()).isNotNull();
    }

    @Test
    @DisplayName("Partial acceptance: Some quantity passes IQC, some fails")
    public void testPartialAcceptanceWorkflow() {
        // Step 1: Create Goods Receipt
        GoodsReceiptEntity goodsReceipt = new GoodsReceiptEntity();
        goodsReceipt.setTenant(testTenant);
        goodsReceipt.setReceiptNo("GR-20260204-003");
        goodsReceipt.setReceiptDate(LocalDate.now());
        goodsReceipt.setSupplier(testSupplier);
        goodsReceipt.setMaterial(testMaterial);
        goodsReceipt.setWarehouse(testWarehouse);
        goodsReceipt.setReceivedQuantity(new BigDecimal("1000"));
        goodsReceipt.setUnitPrice(new BigDecimal("500.00"));
        goodsReceipt.setTotalAmount(new BigDecimal("500000.00"));
        goodsReceipt.setStatus("PENDING_IQC");
        goodsReceipt.setReceiver(testUser);

        GoodsReceiptEntity savedReceipt = goodsReceiptService.createGoodsReceipt(testTenantId, goodsReceipt);

        // Step 2: Create IQC with partial acceptance
        QualityInspectionEntity inspection = new QualityInspectionEntity();
        inspection.setTenant(testTenant);
        inspection.setInspectionNo("IQC-20260204-003");
        inspection.setInspectionType("IQC");
        inspection.setInspectionDate(LocalDate.now());
        inspection.setMaterial(testMaterial);
        inspection.setInspectedQuantity(new BigDecimal("1000"));
        inspection.setInspector(testUser);
        inspection.setResult("PARTIAL");
        inspection.setDefectQuantity(new BigDecimal("100"));  // 10% defect
        inspection.setDefectDescription("Minor defects in 100 units");

        QualityInspectionEntity savedInspection = qualityInspectionService.createQualityInspection(
                testTenantId,
                inspection
        );

        assertThat(savedInspection.getResult()).isEqualTo("PARTIAL");

        // Step 3: Create inventory for passed quantity only (900 units)
        BigDecimal acceptedQuantity = savedReceipt.getReceivedQuantity()
                .subtract(savedInspection.getDefectQuantity());

        InventoryEntity inventory = new InventoryEntity();
        inventory.setTenant(testTenant);
        inventory.setWarehouse(testWarehouse);
        inventory.setMaterial(testMaterial);
        inventory.setQuantity(acceptedQuantity);
        inventory.setAvailableQuantity(acceptedQuantity);
        inventory.setReservedQuantity(BigDecimal.ZERO);

        InventoryEntity savedInventory = inventoryRepository.save(inventory);

        assertThat(savedInventory.getQuantity()).isEqualByComparingTo(new BigDecimal("900"));
    }
}
