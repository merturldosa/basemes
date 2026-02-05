package kr.co.softice.mes.integration;

import kr.co.softice.mes.common.dto.pop.*;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.POPService;
import kr.co.softice.mes.domain.service.SOPOperatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * POP Integration Test
 * Complete workflow integration tests for POP system
 * @author Moon Myung-seop
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class POPIntegrationTest {

    @Autowired
    private POPService popService;

    @Autowired
    private SOPOperatorService sopOperatorService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private WorkProgressRepository workProgressRepository;

    @Autowired
    private PauseResumeRepository pauseResumeRepository;

    @Autowired
    private WorkResultRepository workResultRepository;

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private SOPRepository sopRepository;

    @Autowired
    private SOPStepRepository sopStepRepository;

    @Autowired
    private SOPExecutionRepository sopExecutionRepository;

    @Autowired
    private SOPExecutionStepRepository sopExecutionStepRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private TenantEntity testTenant;
    private UserEntity testOperator;
    private ProductEntity testProduct;
    private WorkOrderEntity testWorkOrder;

    @BeforeEach
    void setUp() {
        // Create test tenant
        testTenant = TenantEntity.builder()
                .tenantId("test-tenant")
                .tenantName("Test Tenant")
                .subscriptionStatus("ACTIVE")
                .build();
        testTenant = tenantRepository.save(testTenant);

        // Create test operator
        testOperator = UserEntity.builder()
                .tenant(testTenant)
                .username("operator1")
                .password("password")
                .email("operator1@test.com")
                .fullName("Test Operator")
                .userStatus("ACTIVE")
                .build();
        testOperator = userRepository.save(testOperator);

        // Create test product
        testProduct = ProductEntity.builder()
                .tenant(testTenant)
                .productCode("TEST-PROD-001")
                .productName("Test Product")
                .productType("FG")
                .unitOfMeasure("EA")
                .status("ACTIVE")
                .build();
        testProduct = productRepository.save(testProduct);

        // Create test work order
        testWorkOrder = WorkOrderEntity.builder()
                .tenant(testTenant)
                .workOrderNo("WO-TEST-001")
                .product(testProduct)
                .plannedQuantity(100)
                .status("READY")
                .plannedStartDate(LocalDateTime.now())
                .plannedEndDate(LocalDateTime.now().plusHours(8))
                .assignedUser(testOperator)
                .build();
        testWorkOrder = workOrderRepository.save(testWorkOrder);
    }

    /**
     * Test 1: Complete work order workflow
     * Start → Record Progress (3 times) → Record Defect → Complete
     */
    @Test
    void testCompleteWorkOrderWorkflow() {
        // 1. Start work order
        WorkProgressResponse progress = popService.startWorkOrder(
                testTenant.getTenantId(),
                testWorkOrder.getWorkOrderId(),
                testOperator.getUserId()
        );

        assertThat(progress).isNotNull();
        assertThat(progress.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(progress.getWorkOrderNo()).isEqualTo("WO-TEST-001");

        Long progressId = progress.getProgressId();

        // Verify work order status changed
        WorkOrderEntity updatedWorkOrder = workOrderRepository.findById(testWorkOrder.getWorkOrderId()).orElseThrow();
        assertThat(updatedWorkOrder.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(updatedWorkOrder.getActualStartDate()).isNotNull();

        // 2. Record progress (3 times)
        WorkProgressRecordRequest request1 = WorkProgressRecordRequest.builder()
                .producedQuantity(30)
                .goodQuantity(30)
                .build();
        progress = popService.recordProgress(testTenant.getTenantId(), progressId, request1);
        assertThat(progress.getProducedQuantity()).isEqualTo(30);
        assertThat(progress.getGoodQuantity()).isEqualTo(30);

        WorkProgressRecordRequest request2 = WorkProgressRecordRequest.builder()
                .producedQuantity(25)
                .goodQuantity(25)
                .build();
        progress = popService.recordProgress(testTenant.getTenantId(), progressId, request2);
        assertThat(progress.getProducedQuantity()).isEqualTo(55);
        assertThat(progress.getGoodQuantity()).isEqualTo(55);

        WorkProgressRecordRequest request3 = WorkProgressRecordRequest.builder()
                .producedQuantity(20)
                .goodQuantity(20)
                .build();
        progress = popService.recordProgress(testTenant.getTenantId(), progressId, request3);
        assertThat(progress.getProducedQuantity()).isEqualTo(75);
        assertThat(progress.getGoodQuantity()).isEqualTo(75);

        // 3. Record defect
        DefectRecordRequest defectRequest = DefectRecordRequest.builder()
                .defectQuantity(5)
                .defectType("외관 불량")
                .defectReason("스크래치 발견")
                .severity("MINOR")
                .build();

        popService.recordDefect(testTenant.getTenantId(), progressId, defectRequest);

        // Verify defect was created
        List<DefectEntity> defects = defectRepository.findByTenant_TenantIdAndReferenceTypeAndReferenceId(
                testTenant.getTenantId(),
                "WORK_ORDER",
                testWorkOrder.getWorkOrderId()
        );
        assertThat(defects).hasSize(1);
        assertThat(defects.get(0).getDefectType()).isEqualTo("외관 불량");
        assertThat(defects.get(0).getDefectQuantity()).isEqualTo(5);

        // Verify work progress updated
        progress = popService.getWorkProgress(testTenant.getTenantId(), testWorkOrder.getWorkOrderId());
        assertThat(progress.getDefectQuantity()).isEqualTo(5);
        assertThat(progress.getProducedQuantity()).isEqualTo(80); // 75 + 5

        // 4. Record more progress to meet target
        WorkProgressRecordRequest request4 = WorkProgressRecordRequest.builder()
                .producedQuantity(20)
                .goodQuantity(20)
                .build();
        popService.recordProgress(testTenant.getTenantId(), progressId, request4);

        // 5. Complete work order
        popService.completeWorkOrder(testTenant.getTenantId(), testWorkOrder.getWorkOrderId(), "정상 완료");

        // Verify work order status
        updatedWorkOrder = workOrderRepository.findById(testWorkOrder.getWorkOrderId()).orElseThrow();
        assertThat(updatedWorkOrder.getStatus()).isEqualTo("COMPLETED");
        assertThat(updatedWorkOrder.getActualEndDate()).isNotNull();
        assertThat(updatedWorkOrder.getActualQuantity()).isEqualTo(100);
        assertThat(updatedWorkOrder.getGoodQuantity()).isEqualTo(95);
        assertThat(updatedWorkOrder.getDefectQuantity()).isEqualTo(5);

        // Verify work result was created
        List<WorkResultEntity> results = workResultRepository.findByTenant_TenantIdAndWorkOrder_WorkOrderId(
                testTenant.getTenantId(),
                testWorkOrder.getWorkOrderId()
        );
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTotalQuantity()).isEqualTo(100);
        assertThat(results.get(0).getGoodQuantity()).isEqualTo(95);
        assertThat(results.get(0).getDefectQuantity()).isEqualTo(5);

        // Verify work progress is completed
        WorkProgressEntity progressEntity = workProgressRepository.findById(progressId).orElseThrow();
        assertThat(progressEntity.getStatus()).isEqualTo("COMPLETED");
        assertThat(progressEntity.getEndTime()).isNotNull();
    }

    /**
     * Test 2: Pause and resume workflow
     * Start → Pause → Resume → Pause → Resume → Complete
     */
    @Test
    void testPauseResumeWorkflow() throws InterruptedException {
        // 1. Start work order
        WorkProgressResponse progress = popService.startWorkOrder(
                testTenant.getTenantId(),
                testWorkOrder.getWorkOrderId(),
                testOperator.getUserId()
        );

        assertThat(progress.getStatus()).isEqualTo("IN_PROGRESS");

        // 2. First pause
        PauseWorkRequest pauseRequest1 = PauseWorkRequest.builder()
                .pauseReason("휴식 시간")
                .pauseType("BREAK")
                .build();

        popService.pauseWork(testTenant.getTenantId(), testWorkOrder.getWorkOrderId(), pauseRequest1);

        // Verify pause
        progress = popService.getWorkProgress(testTenant.getTenantId(), testWorkOrder.getWorkOrderId());
        assertThat(progress.getStatus()).isEqualTo("PAUSED");
        assertThat(progress.getPauseCount()).isEqualTo(1);

        // Wait a bit to simulate pause duration
        Thread.sleep(100);

        // 3. Resume
        popService.resumeWork(testTenant.getTenantId(), testWorkOrder.getWorkOrderId());

        progress = popService.getWorkProgress(testTenant.getTenantId(), testWorkOrder.getWorkOrderId());
        assertThat(progress.getStatus()).isEqualTo("IN_PROGRESS");

        // 4. Second pause
        PauseWorkRequest pauseRequest2 = PauseWorkRequest.builder()
                .pauseReason("설비 점검")
                .pauseType("MAINTENANCE")
                .build();

        popService.pauseWork(testTenant.getTenantId(), testWorkOrder.getWorkOrderId(), pauseRequest2);

        progress = popService.getWorkProgress(testTenant.getTenantId(), testWorkOrder.getWorkOrderId());
        assertThat(progress.getStatus()).isEqualTo("PAUSED");
        assertThat(progress.getPauseCount()).isEqualTo(2);

        // Wait a bit
        Thread.sleep(100);

        // 5. Resume again
        popService.resumeWork(testTenant.getTenantId(), testWorkOrder.getWorkOrderId());

        // 6. Record some progress
        WorkProgressRecordRequest recordRequest = WorkProgressRecordRequest.builder()
                .producedQuantity(100)
                .goodQuantity(100)
                .build();
        popService.recordProgress(testTenant.getTenantId(), progress.getProgressId(), recordRequest);

        // 7. Complete
        popService.completeWorkOrder(testTenant.getTenantId(), testWorkOrder.getWorkOrderId(), "완료");

        // Verify pause/resume history
        List<PauseResumeEntity> pauseHistory = pauseResumeRepository.findByProgress_ProgressId(progress.getProgressId());
        assertThat(pauseHistory).hasSize(2);

        // All pauses should be resumed
        for (PauseResumeEntity pause : pauseHistory) {
            assertThat(pause.getResumeTime()).isNotNull();
            assertThat(pause.getDurationMinutes()).isGreaterThan(0);
        }

        // Verify total pause duration
        WorkProgressEntity finalProgress = workProgressRepository.findById(progress.getProgressId()).orElseThrow();
        assertThat(finalProgress.getTotalPauseDuration()).isGreaterThan(0);
    }

    /**
     * Test 3: Multiple defect records
     * Start → Defect 1 → Defect 2 → Complete
     */
    @Test
    void testMultipleDefectRecords() {
        // 1. Start work order
        WorkProgressResponse progress = popService.startWorkOrder(
                testTenant.getTenantId(),
                testWorkOrder.getWorkOrderId(),
                testOperator.getUserId()
        );

        Long progressId = progress.getProgressId();

        // 2. Record first defect
        DefectRecordRequest defectRequest1 = DefectRecordRequest.builder()
                .defectQuantity(5)
                .defectType("외관 불량")
                .defectReason("표면 스크래치")
                .defectLocation("상단 모서리")
                .severity("MINOR")
                .notes("재작업 가능")
                .build();

        popService.recordDefect(testTenant.getTenantId(), progressId, defectRequest1);

        // 3. Record second defect
        DefectRecordRequest defectRequest2 = DefectRecordRequest.builder()
                .defectQuantity(3)
                .defectType("치수 불량")
                .defectReason("공차 초과")
                .defectLocation("중앙부")
                .severity("MAJOR")
                .notes("폐기 필요")
                .build();

        popService.recordDefect(testTenant.getTenantId(), progressId, defectRequest2);

        // 4. Record good quantity
        WorkProgressRecordRequest progressRequest = WorkProgressRecordRequest.builder()
                .producedQuantity(92)
                .goodQuantity(92)
                .build();
        popService.recordProgress(testTenant.getTenantId(), progressId, progressRequest);

        // 5. Complete work order
        popService.completeWorkOrder(testTenant.getTenantId(), testWorkOrder.getWorkOrderId(), "완료");

        // Verify defects
        List<DefectEntity> defects = defectRepository.findByTenant_TenantIdAndReferenceTypeAndReferenceId(
                testTenant.getTenantId(),
                "WORK_ORDER",
                testWorkOrder.getWorkOrderId()
        );

        assertThat(defects).hasSize(2);

        // Check defect details
        DefectEntity defect1 = defects.stream()
                .filter(d -> d.getDefectType().equals("외관 불량"))
                .findFirst()
                .orElseThrow();
        assertThat(defect1.getDefectQuantity()).isEqualTo(5);
        assertThat(defect1.getSeverity()).isEqualTo("MINOR");

        DefectEntity defect2 = defects.stream()
                .filter(d -> d.getDefectType().equals("치수 불량"))
                .findFirst()
                .orElseThrow();
        assertThat(defect2.getDefectQuantity()).isEqualTo(3);
        assertThat(defect2.getSeverity()).isEqualTo("MAJOR");

        // Verify work order aggregation
        WorkOrderEntity completedOrder = workOrderRepository.findById(testWorkOrder.getWorkOrderId()).orElseThrow();
        assertThat(completedOrder.getActualQuantity()).isEqualTo(100);
        assertThat(completedOrder.getGoodQuantity()).isEqualTo(92);
        assertThat(completedOrder.getDefectQuantity()).isEqualTo(8); // 5 + 3
    }

    /**
     * Test 4: SOP execution workflow
     * Start Work → Start SOP → Complete Steps (Pass/Fail) → Complete SOP → Complete Work
     */
    @Test
    void testSOPExecutionWorkflow() {
        // 1. Create SOP with steps
        SOPEntity sop = SOPEntity.builder()
                .tenant(testTenant)
                .sopNo("SOP-TEST-001")
                .sopName("Test SOP")
                .sopType("PRODUCTION")
                .version("1.0")
                .status("ACTIVE")
                .build();
        sop = sopRepository.save(sop);

        // Create SOP steps
        SOPStepEntity step1 = SOPStepEntity.builder()
                .tenant(testTenant)
                .sop(sop)
                .stepNumber(1)
                .stepTitle("원자재 준비")
                .stepDescription("필요한 원자재를 준비합니다")
                .isMandatory(true)
                .isCritical(true)
                .build();
        sopStepRepository.save(step1);

        SOPStepEntity step2 = SOPStepEntity.builder()
                .tenant(testTenant)
                .sop(sop)
                .stepNumber(2)
                .stepTitle("설비 점검")
                .stepDescription("설비 상태를 점검합니다")
                .isMandatory(true)
                .isCritical(false)
                .build();
        sopStepRepository.save(step2);

        SOPStepEntity step3 = SOPStepEntity.builder()
                .tenant(testTenant)
                .sop(sop)
                .stepNumber(3)
                .stepTitle("작업 시작")
                .stepDescription("작업을 시작합니다")
                .isMandatory(true)
                .isCritical(true)
                .build();
        sopStepRepository.save(step3);

        // 2. Start work order
        WorkProgressResponse progress = popService.startWorkOrder(
                testTenant.getTenantId(),
                testWorkOrder.getWorkOrderId(),
                testOperator.getUserId()
        );

        // 3. Start SOP execution
        var sopResponse = sopOperatorService.startSOPExecution(
                testTenant.getTenantId(),
                sop.getSopId(),
                testWorkOrder.getWorkOrderId(),
                testOperator.getUserId()
        );

        assertThat(sopResponse.getExecutionNo()).isNotNull();
        assertThat(sopResponse.getSteps()).hasSize(3);

        Long executionId = sopResponse.getExecutionId();

        // 4. Complete step 1 (Pass)
        var execSteps = sopExecutionStepRepository.findByExecution_ExecutionId(executionId);
        Long step1ExecId = execSteps.stream()
                .filter(s -> s.getSopStep().getStepNumber() == 1)
                .findFirst()
                .orElseThrow()
                .getExecutionStepId();

        sopOperatorService.completeStep(
                testTenant.getTenantId(),
                executionId,
                step1ExecId,
                true,
                null
        );

        // 5. Complete step 2 (Pass)
        Long step2ExecId = execSteps.stream()
                .filter(s -> s.getSopStep().getStepNumber() == 2)
                .findFirst()
                .orElseThrow()
                .getExecutionStepId();

        sopOperatorService.completeStep(
                testTenant.getTenantId(),
                executionId,
                step2ExecId,
                true,
                "설비 정상"
        );

        // 6. Complete step 3 (Fail)
        Long step3ExecId = execSteps.stream()
                .filter(s -> s.getSopStep().getStepNumber() == 3)
                .findFirst()
                .orElseThrow()
                .getExecutionStepId();

        sopOperatorService.completeStep(
                testTenant.getTenantId(),
                executionId,
                step3ExecId,
                false,
                "재작업 필요"
        );

        // 7. Complete SOP execution
        sopOperatorService.completeSOPExecution(
                testTenant.getTenantId(),
                executionId,
                "일부 단계 실패, 재작업 완료"
        );

        // Verify SOP execution
        SOPExecutionEntity execution = sopExecutionRepository.findById(executionId).orElseThrow();
        assertThat(execution.getExecutionStatus()).isEqualTo("COMPLETED");
        assertThat(execution.getEndTime()).isNotNull();

        // Verify execution steps
        List<SOPExecutionStepEntity> completedSteps = sopExecutionStepRepository.findByExecution_ExecutionId(executionId);
        assertThat(completedSteps).hasSize(3);

        // Check individual step results
        SOPExecutionStepEntity completedStep1 = completedSteps.stream()
                .filter(s -> s.getSopStep().getStepNumber() == 1)
                .findFirst()
                .orElseThrow();
        assertThat(completedStep1.getStepStatus()).isEqualTo("COMPLETED");
        assertThat(completedStep1.getCheckResult()).isTrue();

        SOPExecutionStepEntity completedStep3 = completedSteps.stream()
                .filter(s -> s.getSopStep().getStepNumber() == 3)
                .findFirst()
                .orElseThrow();
        assertThat(completedStep3.getStepStatus()).isEqualTo("FAILED");
        assertThat(completedStep3.getCheckResult()).isFalse();
        assertThat(completedStep3.getCheckNotes()).isEqualTo("재작업 필요");

        // 8. Complete work order
        WorkProgressRecordRequest recordRequest = WorkProgressRecordRequest.builder()
                .producedQuantity(100)
                .goodQuantity(100)
                .build();
        popService.recordProgress(testTenant.getTenantId(), progress.getProgressId(), recordRequest);
        popService.completeWorkOrder(testTenant.getTenantId(), testWorkOrder.getWorkOrderId(), "SOP 완료 및 작업 완료");
    }

    /**
     * Test 5: Today's statistics calculation
     * Multiple work orders, operators, verify statistics accuracy
     */
    @Test
    void testTodayStatisticsCalculation() {
        // Create second operator
        UserEntity operator2 = UserEntity.builder()
                .tenant(testTenant)
                .username("operator2")
                .password("password")
                .email("operator2@test.com")
                .fullName("Test Operator 2")
                .userStatus("ACTIVE")
                .build();
        operator2 = userRepository.save(operator2);

        // Create second work order
        WorkOrderEntity workOrder2 = WorkOrderEntity.builder()
                .tenant(testTenant)
                .workOrderNo("WO-TEST-002")
                .product(testProduct)
                .plannedQuantity(50)
                .status("READY")
                .plannedStartDate(LocalDateTime.now())
                .plannedEndDate(LocalDateTime.now().plusHours(4))
                .assignedUser(operator2)
                .build();
        workOrder2 = workOrderRepository.save(workOrder2);

        // 1. Operator 1 work
        WorkProgressResponse progress1 = popService.startWorkOrder(
                testTenant.getTenantId(),
                testWorkOrder.getWorkOrderId(),
                testOperator.getUserId()
        );

        WorkProgressRecordRequest record1 = WorkProgressRecordRequest.builder()
                .producedQuantity(80)
                .goodQuantity(75)
                .build();
        popService.recordProgress(testTenant.getTenantId(), progress1.getProgressId(), record1);

        DefectRecordRequest defect1 = DefectRecordRequest.builder()
                .defectQuantity(5)
                .defectType("외관 불량")
                .severity("MINOR")
                .build();
        popService.recordDefect(testTenant.getTenantId(), progress1.getProgressId(), defect1);

        // 2. Operator 2 work
        WorkProgressResponse progress2 = popService.startWorkOrder(
                testTenant.getTenantId(),
                workOrder2.getWorkOrderId(),
                operator2.getUserId()
        );

        WorkProgressRecordRequest record2 = WorkProgressRecordRequest.builder()
                .producedQuantity(50)
                .goodQuantity(48)
                .build();
        popService.recordProgress(testTenant.getTenantId(), progress2.getProgressId(), record2);

        DefectRecordRequest defect2 = DefectRecordRequest.builder()
                .defectQuantity(2)
                .defectType("치수 불량")
                .severity("MAJOR")
                .build();
        popService.recordDefect(testTenant.getTenantId(), progress2.getProgressId(), defect2);

        // 3. Get overall statistics
        ProductionStatisticsResponse overallStats = popService.getTodayStatistics(
                testTenant.getTenantId(),
                null // All operators
        );

        assertThat(overallStats).isNotNull();
        assertThat(overallStats.getTotalProduced()).isEqualTo(130); // 80 + 50
        assertThat(overallStats.getTotalGood()).isEqualTo(123); // 75 + 48
        assertThat(overallStats.getTotalDefect()).isEqualTo(7); // 5 + 2
        assertThat(overallStats.getDefectRate()).isGreaterThan(0.0);

        // 4. Get operator 1 statistics
        ProductionStatisticsResponse operator1Stats = popService.getTodayStatistics(
                testTenant.getTenantId(),
                testOperator.getUserId()
        );

        assertThat(operator1Stats.getTotalProduced()).isEqualTo(80);
        assertThat(operator1Stats.getTotalGood()).isEqualTo(75);
        assertThat(operator1Stats.getTotalDefect()).isEqualTo(5);

        // 5. Get operator 2 statistics
        ProductionStatisticsResponse operator2Stats = popService.getTodayStatistics(
                testTenant.getTenantId(),
                operator2.getUserId()
        );

        assertThat(operator2Stats.getTotalProduced()).isEqualTo(50);
        assertThat(operator2Stats.getTotalGood()).isEqualTo(48);
        assertThat(operator2Stats.getTotalDefect()).isEqualTo(2);
    }
}
