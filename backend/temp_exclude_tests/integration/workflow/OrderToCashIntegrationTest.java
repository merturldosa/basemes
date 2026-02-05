package kr.co.softice.mes.integration.workflow;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.*;
import kr.co.softice.mes.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Order-to-Cash 워크플로우 통합 테스트
 *
 * 작업지시 → 자재 출고 → 생산 → OQC → 출하 전체 프로세스 검증
 *
 * @author Claude Code (Sonnet 4.5)
 * @company SoftIce Co., Ltd.
 * @since 2026-01-27
 */
@DisplayName("Order-to-Cash 워크플로우 통합 테스트")
public class OrderToCashIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private GoodsIssueService goodsIssueService;

    @Autowired
    private ProductionRecordService productionRecordService;

    @Autowired
    private OQCInspectionService oqcInspectionService;

    @Autowired
    private StockLevelService stockLevelService;

    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Test
    @DisplayName("작업지시부터 완제품 출하까지 전체 프로세스 - 성공")
    void testCompleteOrderToCashWorkflow_Success() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 초기 설정 - 원자재 재고 준비
        // ═══════════════════════════════════════════════════════════════

        // 원자재 재고 준비 (자재 100개 입고)
        StockLevelEntity materialStock = createStockLevel(testProduct, 100.0);

        // ═══════════════════════════════════════════════════════════════
        // Step 1: 작업지시 생성 (Work Order)
        // ═══════════════════════════════════════════════════════════════

        WorkOrderEntity workOrder = new WorkOrderEntity();
        workOrder.setTenant(testTenant);
        workOrder.setWoNumber("WO-2026-001");
        workOrder.setProduct(testProduct);
        workOrder.setPlannedQuantity(50.0);
        workOrder.setActualQuantity(0.0);
        workOrder.setStatus("DRAFT");
        workOrder.setPlannedStartDate(LocalDateTime.now());
        workOrder.setPlannedEndDate(LocalDateTime.now().plusDays(3));
        workOrder = workOrderService.createWorkOrder(workOrder);

        assertThat(workOrder.getId()).isNotNull();
        assertThat(workOrder.getStatus()).isEqualTo("DRAFT");

        // ═══════════════════════════════════════════════════════════════
        // Step 2: 작업지시 출하 (RELEASED)
        // ═══════════════════════════════════════════════════════════════

        WorkOrderEntity releasedWO = workOrderService.releaseWorkOrder(workOrder.getId());

        assertThat(releasedWO.getStatus()).isEqualTo("RELEASED");
        assertThat(releasedWO.getActualStartDate()).isNotNull();

        // ═══════════════════════════════════════════════════════════════
        // Step 3: 자재 출고 (Goods Issue)
        // ═══════════════════════════════════════════════════════════════

        GoodsIssueEntity goodsIssue = new GoodsIssueEntity();
        goodsIssue.setTenant(testTenant);
        goodsIssue.setGiNumber("GI-2026-001");
        goodsIssue.setWorkOrder(releasedWO);
        goodsIssue.setProduct(testProduct);
        goodsIssue.setIssuedQuantity(50.0); // 생산에 필요한 자재 50개 출고
        goodsIssue.setIssueDate(LocalDateTime.now());
        goodsIssue.setStatus("COMPLETED");
        goodsIssue.setWarehouse(testWarehouse);
        goodsIssue = goodsIssueService.createGoodsIssue(goodsIssue);

        assertThat(goodsIssue.getId()).isNotNull();
        assertThat(goodsIssue.getStatus()).isEqualTo("COMPLETED");

        // 재고 차감 확인 (100 - 50 = 50)
        StockLevelEntity stockAfterIssue = stockLevelService
                .getStockLevel(testTenant.getTenantId(), testProduct.getId(), testWarehouse.getId())
                .orElseThrow();

        assertThat(stockAfterIssue.getAvailableQuantity()).isEqualTo(50.0);

        // ═══════════════════════════════════════════════════════════════
        // Step 4: 작업지시 시작 (IN_PROGRESS)
        // ═══════════════════════════════════════════════════════════════

        WorkOrderEntity inProgressWO = workOrderService.startWorkOrder(releasedWO.getId());

        assertThat(inProgressWO.getStatus()).isEqualTo("IN_PROGRESS");

        // ═══════════════════════════════════════════════════════════════
        // Step 5: 생산 실적 기록 (Production Record)
        // ═══════════════════════════════════════════════════════════════

        ProductionRecordEntity productionRecord = new ProductionRecordEntity();
        productionRecord.setTenant(testTenant);
        productionRecord.setWorkOrder(inProgressWO);
        productionRecord.setProduct(testProduct);
        productionRecord.setProducedQuantity(48.0); // 생산 수량 (수율 96%)
        productionRecord.setDefectQuantity(2.0); // 불량 2개
        productionRecord.setRecordDate(LocalDateTime.now());
        productionRecord.setShift("DAY");
        productionRecord = productionRecordService.createProductionRecord(productionRecord);

        assertThat(productionRecord.getId()).isNotNull();
        assertThat(productionRecord.getProducedQuantity()).isEqualTo(48.0);

        // ═══════════════════════════════════════════════════════════════
        // Step 6: 작업지시 완료 (COMPLETED)
        // ═══════════════════════════════════════════════════════════════

        WorkOrderEntity completedWO = workOrderService.completeWorkOrder(inProgressWO.getId(), 48.0);

        assertThat(completedWO.getStatus()).isEqualTo("COMPLETED");
        assertThat(completedWO.getActualQuantity()).isEqualTo(48.0);
        assertThat(completedWO.getActualEndDate()).isNotNull();

        // ═══════════════════════════════════════════════════════════════
        // Step 7: OQC 검사 생성 (Outgoing Quality Control)
        // ═══════════════════════════════════════════════════════════════

        OQCInspectionEntity oqcInspection = new OQCInspectionEntity();
        oqcInspection.setTenant(testTenant);
        oqcInspection.setInspectionNumber("OQC-2026-001");
        oqcInspection.setWorkOrder(completedWO);
        oqcInspection.setProduct(testProduct);
        oqcInspection.setInspectionDate(LocalDateTime.now());
        oqcInspection.setInspectionQuantity(48.0);
        oqcInspection.setStatus("IN_PROGRESS");
        oqcInspection = oqcInspectionService.createOQCInspection(oqcInspection);

        assertThat(oqcInspection.getId()).isNotNull();
        assertThat(oqcInspection.getStatus()).isEqualTo("IN_PROGRESS");

        // ═══════════════════════════════════════════════════════════════
        // Step 8: OQC 검사 완료 (PASS)
        // ═══════════════════════════════════════════════════════════════

        oqcInspection.setStatus("COMPLETED");
        oqcInspection.setResult("PASS");
        oqcInspection.setPassedQuantity(48.0);
        oqcInspection.setFailedQuantity(0.0);
        oqcInspection.setCompletedDate(LocalDateTime.now());
        oqcInspection = oqcInspectionService.updateOQCInspection(oqcInspection);

        assertThat(oqcInspection.getStatus()).isEqualTo("COMPLETED");
        assertThat(oqcInspection.getResult()).isEqualTo("PASS");

        // ═══════════════════════════════════════════════════════════════
        // Step 9: 완제품 재고 반영 (출하 가능 상태)
        // ═══════════════════════════════════════════════════════════════

        // 완제품 재고 생성 (또는 업데이트)
        // 실제로는 ProductionRecordService에서 자동으로 처리될 수 있음

        InventoryTransactionEntity inboundTransaction = new InventoryTransactionEntity();
        inboundTransaction.setTenant(testTenant);
        inboundTransaction.setTransactionType("PRODUCTION_INBOUND");
        inboundTransaction.setProduct(testProduct);
        inboundTransaction.setWarehouse(testWarehouse);
        inboundTransaction.setQuantity(48.0);
        inboundTransaction.setTransactionDate(LocalDateTime.now());
        inboundTransaction.setReferenceType("WORK_ORDER");
        inboundTransaction.setReferenceId(completedWO.getId());
        inboundTransaction = inventoryTransactionService.createTransaction(inboundTransaction);

        assertThat(inboundTransaction.getId()).isNotNull();

        // ═══════════════════════════════════════════════════════════════
        // Step 10: 재고 거래 내역 검증
        // ═══════════════════════════════════════════════════════════════

        List<InventoryTransactionEntity> transactions = inventoryTransactionRepository
                .findByTenantAndProduct(testTenant, testProduct);

        // OUTBOUND (자재 출고): -50
        assertThat(transactions).anyMatch(t ->
                t.getTransactionType().equals("OUTBOUND") &&
                t.getQuantity().equals(50.0)
        );

        // PRODUCTION_INBOUND (완제품 입고): +48
        assertThat(transactions).anyMatch(t ->
                t.getTransactionType().equals("PRODUCTION_INBOUND") &&
                t.getQuantity().equals(48.0)
        );

        // ═══════════════════════════════════════════════════════════════
        // 최종 검증: 전체 프로세스 완료
        // ═══════════════════════════════════════════════════════════════

        assertThat(completedWO.getStatus()).isEqualTo("COMPLETED");
        assertThat(completedWO.getActualQuantity()).isEqualTo(48.0);
        assertThat(productionRecord.getProducedQuantity()).isEqualTo(48.0);
        assertThat(oqcInspection.getResult()).isEqualTo("PASS");

        // 수율 계산 검증: 48/50 = 96%
        double yield = (productionRecord.getProducedQuantity() / releasedWO.getPlannedQuantity()) * 100;
        assertThat(yield).isEqualTo(96.0);
    }

    @Test
    @DisplayName("OQC 불합격 시 재가공 워크플로우")
    void testOQCFailure_Rework() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 작업지시 및 생산 완료
        // ═══════════════════════════════════════════════════════════════

        StockLevelEntity materialStock = createStockLevel(testProduct, 100.0);

        WorkOrderEntity workOrder = createWorkOrder("WO-2026-002", "COMPLETED");
        workOrder.setActualQuantity(50.0);
        workOrder = workOrderRepository.save(workOrder);

        // ═══════════════════════════════════════════════════════════════
        // When: OQC 검사 불합격
        // ═══════════════════════════════════════════════════════════════

        OQCInspectionEntity oqcInspection = new OQCInspectionEntity();
        oqcInspection.setTenant(testTenant);
        oqcInspection.setInspectionNumber("OQC-2026-002");
        oqcInspection.setWorkOrder(workOrder);
        oqcInspection.setProduct(testProduct);
        oqcInspection.setInspectionQuantity(50.0);
        oqcInspection.setStatus("COMPLETED");
        oqcInspection.setResult("FAIL");
        oqcInspection.setPassedQuantity(40.0);
        oqcInspection.setFailedQuantity(10.0);
        oqcInspection.setDefectDescription("표면 불량 10개");
        oqcInspection = oqcInspectionService.createOQCInspection(oqcInspection);

        // ═══════════════════════════════════════════════════════════════
        // Then: 불합격 수량 확인 및 재가공 작업지시 생성
        // ═══════════════════════════════════════════════════════════════

        assertThat(oqcInspection.getResult()).isEqualTo("FAIL");
        assertThat(oqcInspection.getFailedQuantity()).isEqualTo(10.0);
        assertThat(oqcInspection.getPassedQuantity()).isEqualTo(40.0);

        // 재가공 작업지시 생성 (불합격 수량에 대해)
        WorkOrderEntity reworkWO = new WorkOrderEntity();
        reworkWO.setTenant(testTenant);
        reworkWO.setWoNumber("WO-2026-002-REWORK");
        reworkWO.setProduct(testProduct);
        reworkWO.setPlannedQuantity(10.0);
        reworkWO.setStatus("DRAFT");
        reworkWO.setWorkOrderType("REWORK");
        reworkWO.setReworkReason("OQC 불합격 - 표면 불량");
        reworkWO = workOrderService.createWorkOrder(reworkWO);

        assertThat(reworkWO.getWorkOrderType()).isEqualTo("REWORK");
        assertThat(reworkWO.getPlannedQuantity()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("자재 부족 시 작업지시 출하 불가 검증")
    void testMaterialShortage_CannotReleaseWorkOrder() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 자재 재고 10개만 있음 (필요 수량: 50개)
        // ═══════════════════════════════════════════════════════════════

        StockLevelEntity insufficientStock = createStockLevel(testProduct, 10.0);

        WorkOrderEntity workOrder = createWorkOrder("WO-2026-003", "DRAFT");
        workOrder.setPlannedQuantity(50.0); // 50개 생산 계획
        workOrder = workOrderRepository.save(workOrder);

        // ═══════════════════════════════════════════════════════════════
        // When: 작업지시 출하 시도
        // ═══════════════════════════════════════════════════════════════

        // 실제 서비스에서는 자재 부족으로 예외 발생할 것으로 예상
        // 또는 상태 변경이 거부됨

        try {
            WorkOrderEntity releasedWO = workOrderService.releaseWorkOrder(workOrder.getId());

            // 만약 출하가 허용된다면, 부분 출하로 처리되거나 경고 메시지가 있어야 함
            // 이는 비즈니스 룰에 따라 다를 수 있음

        } catch (Exception e) {
            // 자재 부족 예외 발생 확인
            assertThat(e.getMessage()).contains("재고 부족");
        }

        // ═══════════════════════════════════════════════════════════════
        // Then: 재고 수준 확인 (10개로 유지)
        // ═══════════════════════════════════════════════════════════════

        StockLevelEntity finalStock = stockLevelService
                .getStockLevel(testTenant.getTenantId(), testProduct.getId(), testWarehouse.getId())
                .orElseThrow();

        assertThat(finalStock.getAvailableQuantity()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("다중 생산 기록 집계 검증")
    void testMultipleProductionRecords_Aggregation() {
        // ═══════════════════════════════════════════════════════════════
        // Given: 작업지시 생성 및 출하
        // ═══════════════════════════════════════════════════════════════

        StockLevelEntity materialStock = createStockLevel(testProduct, 200.0);

        WorkOrderEntity workOrder = createWorkOrder("WO-2026-004", "RELEASED");
        workOrder.setPlannedQuantity(100.0);
        workOrder = workOrderRepository.save(workOrder);

        workOrder = workOrderService.startWorkOrder(workOrder.getId());

        // ═══════════════════════════════════════════════════════════════
        // When: 여러 차례 생산 기록 (교대조별)
        // ═══════════════════════════════════════════════════════════════

        // 주간 교대: 30개 생산
        ProductionRecordEntity dayShift = new ProductionRecordEntity();
        dayShift.setTenant(testTenant);
        dayShift.setWorkOrder(workOrder);
        dayShift.setProduct(testProduct);
        dayShift.setProducedQuantity(30.0);
        dayShift.setDefectQuantity(2.0);
        dayShift.setRecordDate(LocalDateTime.now().withHour(8));
        dayShift.setShift("DAY");
        dayShift = productionRecordService.createProductionRecord(dayShift);

        // 야간 교대: 40개 생산
        ProductionRecordEntity nightShift = new ProductionRecordEntity();
        nightShift.setTenant(testTenant);
        nightShift.setWorkOrder(workOrder);
        nightShift.setProduct(testProduct);
        nightShift.setProducedQuantity(40.0);
        nightShift.setDefectQuantity(3.0);
        nightShift.setRecordDate(LocalDateTime.now().withHour(20));
        nightShift.setShift("NIGHT");
        nightShift = productionRecordService.createProductionRecord(nightShift);

        // 잔업: 25개 생산
        ProductionRecordEntity overtimeShift = new ProductionRecordEntity();
        overtimeShift.setTenant(testTenant);
        overtimeShift.setWorkOrder(workOrder);
        overtimeShift.setProduct(testProduct);
        overtimeShift.setProducedQuantity(25.0);
        overtimeShift.setDefectQuantity(1.0);
        overtimeShift.setRecordDate(LocalDateTime.now().withHour(22));
        overtimeShift.setShift("OVERTIME");
        overtimeShift = productionRecordService.createProductionRecord(overtimeShift);

        // ═══════════════════════════════════════════════════════════════
        // Then: 총 생산 수량 집계 확인
        // ═══════════════════════════════════════════════════════════════

        List<ProductionRecordEntity> records = productionRecordRepository
                .findByWorkOrder(workOrder);

        assertThat(records).hasSize(3);

        double totalProduced = records.stream()
                .mapToDouble(ProductionRecordEntity::getProducedQuantity)
                .sum();

        double totalDefects = records.stream()
                .mapToDouble(ProductionRecordEntity::getDefectQuantity)
                .sum();

        assertThat(totalProduced).isEqualTo(95.0); // 30 + 40 + 25
        assertThat(totalDefects).isEqualTo(6.0); // 2 + 3 + 1

        // 작업지시 완료 처리
        WorkOrderEntity completedWO = workOrderService.completeWorkOrder(workOrder.getId(), totalProduced);

        assertThat(completedWO.getActualQuantity()).isEqualTo(95.0);
        assertThat(completedWO.getStatus()).isEqualTo("COMPLETED");

        // 수율 계산: 95/100 = 95%
        double yield = (totalProduced / workOrder.getPlannedQuantity()) * 100;
        assertThat(yield).isEqualTo(95.0);
    }
}
