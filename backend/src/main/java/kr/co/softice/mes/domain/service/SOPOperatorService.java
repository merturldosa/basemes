package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.sop.SOPSimplifiedResponse;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SOP Operator Service
 * 운영자용 간소화 SOP 서비스
 * Simplified SOP interface for field operators
 * @author Moon Myung-seop
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SOPOperatorService {

    private final SOPRepository sopRepository;
    private final SOPExecutionRepository sopExecutionRepository;
    private final SOPExecutionStepRepository sopExecutionStepRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    /**
     * Get SOPs for a work order
     * 작업지시에 연결된 SOP 목록 조회
     *
     * @param workOrderId Work order ID
     * @return List of simplified SOPs
     */
    @Transactional(readOnly = true)
    public List<SOPSimplifiedResponse> getWorkOrderSOPs(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        String tenantId = workOrder.getTenant().getTenantId();
        String processName = workOrder.getProcess().getProcessName();

        // Find active SOPs for the process
        List<SOPEntity> sops = sopRepository.findActiveByTenantId(tenantId)
            .stream()
            .filter(sop -> "PRODUCTION".equals(sop.getSopType()))
            .filter(sop -> sop.getTargetProcess() != null &&
                          sop.getTargetProcess().contains(processName))
            .collect(Collectors.toList());

        return sops.stream()
            .map(this::convertToSimplifiedResponse)
            .collect(Collectors.toList());
    }

    /**
     * Start SOP execution
     * SOP 실행 시작
     *
     * @param sopId SOP ID
     * @param workOrderId Work order ID (context)
     * @param operatorId Operator user ID
     * @return SOP execution response
     */
    public SOPSimplifiedResponse startSOPExecution(Long sopId, Long workOrderId, Long operatorId) {
        // 1. Get SOP with steps
        SOPEntity sop = sopRepository.findById(sopId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "SOP not found"));

        // 2. Get work order
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        // 3. Get operator
        UserEntity operator = userRepository.findById(operatorId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 4. Generate execution number
        String executionNo = generateExecutionNo(sop.getTenant().getTenantId());

        // 5. Create SOP execution
        SOPExecutionEntity execution = SOPExecutionEntity.builder()
            .tenant(sop.getTenant())
            .sop(sop)
            .executionNo(executionNo)
            .executionDate(LocalDateTime.now())
            .executor(operator)
            .executorName(operator.getUsername())
            .referenceType("WORK_ORDER")
            .referenceId(workOrderId)
            .referenceNo(workOrder.getWorkOrderNo())
            .executionStatus("IN_PROGRESS")
            .build();

        SOPExecutionEntity savedExecution = sopExecutionRepository.save(execution);

        // 6. Create execution steps from SOP steps
        List<SOPStepEntity> sopSteps = sop.getSteps();
        if (sopSteps != null && !sopSteps.isEmpty()) {
            for (SOPStepEntity sopStep : sopSteps) {
                SOPExecutionStepEntity execStep = SOPExecutionStepEntity.builder()
                    .execution(savedExecution)
                    .sopStep(sopStep)
                    .stepNumber(sopStep.getStepNumber())
                    .stepStatus("PENDING")
                    .build();
                sopExecutionStepRepository.save(execStep);
            }
        }

        log.info("Started SOP execution: {} for work order: {} by operator: {}",
            executionNo, workOrderId, operatorId);

        return convertToSimplifiedResponseWithExecution(sop, savedExecution);
    }

    /**
     * Complete a step
     * 단계 완료 처리
     *
     * @param executionStepId Execution step ID
     * @param passed Pass/Fail result
     * @param notes Optional notes
     */
    public void completeStep(Long executionStepId, Boolean passed, String notes) {
        // 1. Get execution step
        SOPExecutionStepEntity execStep = sopExecutionStepRepository.findById(executionStepId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Execution step not found"));

        // 2. Update step
        execStep.setStepStatus("COMPLETED");
        execStep.setCompletedAt(LocalDateTime.now());

        if (execStep.getStartedAt() != null) {
            long duration = Duration.between(execStep.getStartedAt(), execStep.getCompletedAt()).toMinutes();
            execStep.setDuration((int) duration);
        }

        execStep.setResultValue(passed ? "PASS" : "FAIL");

        if (notes != null) {
            execStep.setRemarks(notes);
        }

        sopExecutionStepRepository.save(execStep);

        log.info("Completed step {} with result: {}", executionStepId, passed ? "PASS" : "FAIL");
    }

    /**
     * Complete SOP execution
     * SOP 실행 완료
     *
     * @param executionId Execution ID
     * @param remarks Optional remarks
     * @return Completed SOP execution response
     */
    public SOPSimplifiedResponse completeSOPExecution(Long executionId, String remarks) {
        // 1. Get execution
        SOPExecutionEntity execution = sopExecutionRepository.findById(executionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "SOP execution not found"));

        // 2. Check if all required steps are completed
        List<SOPExecutionStepEntity> steps = sopExecutionStepRepository.findByExecution(execution);

        long pendingRequiredSteps = steps.stream()
            .filter(step -> step.getSopStep().getIsMandatory())
            .filter(step -> !"COMPLETED".equals(step.getStepStatus()))
            .count();

        if (pendingRequiredSteps > 0) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                "Cannot complete SOP execution: " + pendingRequiredSteps + " required steps are not completed");
        }

        // 3. Update execution
        execution.setExecutionStatus("COMPLETED");
        execution.setEndTime(LocalDateTime.now());

        if (execution.getStartTime() != null) {
            long duration = Duration.between(execution.getStartTime(), execution.getEndTime()).toMinutes();
            execution.setDuration((int) duration);
        }

        if (remarks != null) {
            execution.setRemarks(remarks);
        }

        SOPExecutionEntity saved = sopExecutionRepository.save(execution);

        log.info("Completed SOP execution: {}", execution.getExecutionNo());

        return convertToSimplifiedResponseWithExecution(execution.getSop(), saved);
    }

    /**
     * Get execution progress
     * 실행 진행률 조회
     *
     * @param executionId Execution ID
     * @return SOP execution response with progress
     */
    @Transactional(readOnly = true)
    public SOPSimplifiedResponse getExecutionProgress(Long executionId) {
        SOPExecutionEntity execution = sopExecutionRepository.findById(executionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "SOP execution not found"));

        return convertToSimplifiedResponseWithExecution(execution.getSop(), execution);
    }

    // Helper methods

    private SOPSimplifiedResponse convertToSimplifiedResponse(SOPEntity sop) {
        List<SOPSimplifiedResponse.SimplifiedStep> steps = new ArrayList<>();

        if (sop.getSteps() != null) {
            steps = sop.getSteps().stream()
                .map(step -> SOPSimplifiedResponse.SimplifiedStep.builder()
                    .stepId(step.getSopStepId())
                    .stepNumber(step.getStepNumber())
                    .stepTitle(step.getStepTitle())
                    .stepDescription(step.getStepDescription())
                    .isRequired(step.getIsMandatory())
                    .isCritical(step.getIsCritical())
                    .executionStatus("PENDING")
                    .build())
                .collect(Collectors.toList());
        }

        return SOPSimplifiedResponse.builder()
            .sopId(sop.getSopId())
            .sopCode(sop.getSopCode())
            .sopName(sop.getSopName())
            .description(sop.getDescription())
            .sopType(sop.getSopType())
            .version(sop.getVersion())
            .totalSteps(steps.size())
            .completedSteps(0)
            .completionRate(0.0)
            .steps(steps)
            .build();
    }

    private SOPSimplifiedResponse convertToSimplifiedResponseWithExecution(SOPEntity sop, SOPExecutionEntity execution) {
        // Get execution steps
        List<SOPExecutionStepEntity> execSteps = sopExecutionStepRepository.findByExecution(execution);

        List<SOPSimplifiedResponse.SimplifiedStep> steps = new ArrayList<>();

        if (sop.getSteps() != null) {
            for (SOPStepEntity sopStep : sop.getSteps()) {
                // Find matching execution step
                SOPExecutionStepEntity execStep = execSteps.stream()
                    .filter(es -> es.getSopStep().getSopStepId().equals(sopStep.getSopStepId()))
                    .findFirst()
                    .orElse(null);

                SOPSimplifiedResponse.SimplifiedStep.SimplifiedStepBuilder stepBuilder =
                    SOPSimplifiedResponse.SimplifiedStep.builder()
                        .stepId(sopStep.getSopStepId())
                        .stepNumber(sopStep.getStepNumber())
                        .stepTitle(sopStep.getStepTitle())
                        .stepDescription(sopStep.getStepDescription())
                        .isRequired(sopStep.getIsMandatory())
                        .isCritical(sopStep.getIsCritical());

                if (execStep != null) {
                    stepBuilder
                        .executionStepId(execStep.getExecutionStepId())
                        .executionStatus(execStep.getStepStatus())
                        .checkResult("PASS".equals(execStep.getResultValue()))
                        .notes(execStep.getRemarks());
                }

                steps.add(stepBuilder.build());
            }
        }

        // Calculate progress
        long completedCount = steps.stream()
            .filter(step -> "COMPLETED".equals(step.getExecutionStatus()))
            .count();

        double completionRate = steps.isEmpty() ? 0.0 :
            ((double) completedCount / steps.size()) * 100.0;

        return SOPSimplifiedResponse.builder()
            .sopId(sop.getSopId())
            .sopCode(sop.getSopCode())
            .sopName(sop.getSopName())
            .description(sop.getDescription())
            .sopType(sop.getSopType())
            .version(sop.getVersion())
            .executionId(execution.getExecutionId())
            .executionNo(execution.getExecutionNo())
            .executionStatus(execution.getExecutionStatus())
            .totalSteps(steps.size())
            .completedSteps((int) completedCount)
            .completionRate(completionRate)
            .steps(steps)
            .build();
    }

    private String generateExecutionNo(String tenantId) {
        String prefix = "SOPE-" + LocalDateTime.now().toLocalDate().toString().replace("-", "") + "-";
        long count = sopExecutionRepository.countByTenant_TenantIdAndExecutionNoStartingWith(tenantId, prefix);
        return prefix + String.format("%04d", count + 1);
    }
}
