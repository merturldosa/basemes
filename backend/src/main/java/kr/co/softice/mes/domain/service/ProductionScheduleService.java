package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.schedule.GanttChartData;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.ProductionScheduleEntity;
import kr.co.softice.mes.domain.entity.ProcessRoutingStepEntity;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.repository.ProductionScheduleRepository;
import kr.co.softice.mes.domain.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production Schedule Service
 * 생산 일정 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionScheduleService {

    private final ProductionScheduleRepository scheduleRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * 테넌트별 전체 일정 조회
     */
    public List<ProductionScheduleEntity> findByTenant(String tenantId) {
        return scheduleRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * 기간별 일정 조회 (Gantt Chart용)
     */
    public List<ProductionScheduleEntity> findByPeriod(String tenantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);
        return scheduleRepository.findByPeriod(tenantId, startTime, endTime);
    }

    /**
     * WorkOrder별 일정 조회
     */
    public List<ProductionScheduleEntity> findByWorkOrder(Long workOrderId) {
        return scheduleRepository.findByWorkOrderIdWithAllRelations(workOrderId);
    }

    /**
     * ID로 일정 조회
     */
    public Optional<ProductionScheduleEntity> findById(Long scheduleId) {
        return scheduleRepository.findByIdWithAllRelations(scheduleId);
    }

    /**
     * 지연 일정 조회
     */
    public List<ProductionScheduleEntity> findDelayedSchedules(String tenantId) {
        return scheduleRepository.findDelayedSchedules(tenantId);
    }

    /**
     * 상태별 일정 조회
     */
    public List<ProductionScheduleEntity> findByStatus(String tenantId, String status) {
        return scheduleRepository.findByTenantIdAndStatusWithAllRelations(tenantId, status);
    }

    /**
     * WorkOrder에서 자동으로 일정 생성
     * - WorkOrder의 routing을 기반으로
     * - 각 routing step마다 Schedule 생성
     * - 시간 계산: 이전 단계 종료 시간 + 현재 단계 소요 시간
     */
    @Transactional
    public List<ProductionScheduleEntity> generateSchedulesFromWorkOrder(Long workOrderId) {
        log.info("Generating schedules for WorkOrder: {}", workOrderId);

        WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        if (workOrder.getRouting() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 기존 일정이 있으면 삭제
        List<ProductionScheduleEntity> existingSchedules = scheduleRepository.findByWorkOrder_WorkOrderId(workOrderId);
        if (!existingSchedules.isEmpty()) {
            log.info("Deleting {} existing schedules for WorkOrder: {}", existingSchedules.size(), workOrderId);
            scheduleRepository.deleteAll(existingSchedules);
        }

        List<ProductionScheduleEntity> schedules = new ArrayList<>();
        LocalDateTime currentTime = workOrder.getPlannedStartDate();

        for (ProcessRoutingStepEntity step : workOrder.getRouting().getSteps()) {
            // 시간 계산 (분 단위)
            int standardTime = step.getStandardTime() != null ? step.getStandardTime() : 0;
            int setupTime = step.getSetupTime() != null ? step.getSetupTime() : 0;
            int waitTime = step.getWaitTime() != null ? step.getWaitTime() : 0;
            int totalMinutes = standardTime + setupTime + waitTime;

            LocalDateTime startTime = currentTime;
            LocalDateTime endTime = currentTime.plusMinutes(totalMinutes);

            // Schedule 생성
            ProductionScheduleEntity schedule = ProductionScheduleEntity.builder()
                .tenant(workOrder.getTenant())
                .workOrder(workOrder)
                .routingStep(step)
                .sequenceOrder(step.getSequenceOrder())
                .plannedStartTime(startTime)
                .plannedEndTime(endTime)
                .plannedDuration(totalMinutes)
                .assignedEquipment(step.getEquipment())
                .assignedWorkers(step.getRequiredWorkers() != null ? step.getRequiredWorkers() : 1)
                .status("SCHEDULED")
                .progressRate(BigDecimal.ZERO)
                .isDelayed(false)
                .delayMinutes(0)
                .build();

            schedules.add(schedule);

            // 다음 공정 시작 시간 설정
            currentTime = endTime;

            log.info("Created schedule for step {}: {} - {}",
                step.getSequenceOrder(), startTime, endTime);
        }

        List<ProductionScheduleEntity> savedSchedules = scheduleRepository.saveAll(schedules);
        log.info("Generated {} schedules for WorkOrder: {}", savedSchedules.size(), workOrderId);

        return savedSchedules;
    }

    /**
     * 일정 생성
     */
    @Transactional
    public ProductionScheduleEntity createSchedule(ProductionScheduleEntity schedule) {
        log.info("Creating schedule for WorkOrder: {}", schedule.getWorkOrder().getWorkOrderId());

        // 리소스 충돌 체크
        if (schedule.getAssignedEquipment() != null) {
            List<ProductionScheduleEntity> conflicts = scheduleRepository.findConflictingSchedulesByEquipment(
                schedule.getAssignedEquipment().getEquipmentId(),
                schedule.getPlannedStartTime(),
                schedule.getPlannedEndTime()
            );
            if (!conflicts.isEmpty()) {
                log.warn("Resource conflict detected for equipment: {}",
                    schedule.getAssignedEquipment().getEquipmentCode());
                // 경고만 하고 진행 (실제로는 예외를 던질 수도 있음)
            }
        }

        ProductionScheduleEntity saved = scheduleRepository.save(schedule);
        return scheduleRepository.findByIdWithAllRelations(saved.getScheduleId()).orElse(saved);
    }

    /**
     * 일정 수정
     */
    @Transactional
    public ProductionScheduleEntity updateSchedule(ProductionScheduleEntity schedule) {
        log.info("Updating schedule: {}", schedule.getScheduleId());

        // 리소스 충돌 체크 (시간이나 설비가 변경된 경우)
        if (schedule.getAssignedEquipment() != null) {
            List<ProductionScheduleEntity> conflicts = scheduleRepository.findConflictingSchedulesByEquipment(
                schedule.getAssignedEquipment().getEquipmentId(),
                schedule.getPlannedStartTime(),
                schedule.getPlannedEndTime()
            ).stream()
            .filter(s -> !s.getScheduleId().equals(schedule.getScheduleId())) // 자기 자신 제외
            .collect(Collectors.toList());

            if (!conflicts.isEmpty()) {
                log.warn("Resource conflict detected for equipment: {}",
                    schedule.getAssignedEquipment().getEquipmentCode());
            }
        }

        ProductionScheduleEntity updated = scheduleRepository.save(schedule);
        return scheduleRepository.findByIdWithAllRelations(updated.getScheduleId()).orElse(updated);
    }

    /**
     * 일정 삭제
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        log.info("Deleting schedule: {}", scheduleId);
        scheduleRepository.deleteById(scheduleId);
    }

    /**
     * 일정 상태 변경
     */
    @Transactional
    public ProductionScheduleEntity updateStatus(Long scheduleId, String status) {
        ProductionScheduleEntity schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        log.info("Updating schedule {} status from {} to {}",
            scheduleId, schedule.getStatus(), status);

        schedule.setStatus(status);

        // 상태별 자동 처리
        if ("IN_PROGRESS".equals(status) && schedule.getActualStartTime() == null) {
            schedule.setActualStartTime(LocalDateTime.now());
        } else if ("COMPLETED".equals(status) && schedule.getActualEndTime() == null) {
            schedule.setActualEndTime(LocalDateTime.now());
            schedule.setProgressRate(BigDecimal.valueOf(100));
        }

        ProductionScheduleEntity updated = scheduleRepository.save(schedule);
        return scheduleRepository.findByIdWithAllRelations(updated.getScheduleId()).orElse(updated);
    }

    /**
     * 리소스 충돌 체크
     */
    public List<ProductionScheduleEntity> checkResourceConflicts(Long scheduleId) {
        ProductionScheduleEntity schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (schedule.getAssignedEquipment() == null) {
            return new ArrayList<>();
        }

        return scheduleRepository.findConflictingSchedulesByEquipment(
            schedule.getAssignedEquipment().getEquipmentId(),
            schedule.getPlannedStartTime(),
            schedule.getPlannedEndTime()
        ).stream()
        .filter(s -> !s.getScheduleId().equals(scheduleId)) // 자기 자신 제외
        .collect(Collectors.toList());
    }

    /**
     * Gantt Chart 데이터 생성
     */
    public GanttChartData getGanttChartData(String tenantId, LocalDate startDate, LocalDate endDate) {
        List<ProductionScheduleEntity> schedules = findByPeriod(tenantId, startDate, endDate);

        List<GanttChartData.GanttTask> tasks = schedules.stream()
            .map(this::convertToGanttTask)
            .collect(Collectors.toList());

        return GanttChartData.builder()
            .startDate(LocalDateTime.of(startDate, LocalTime.MIN))
            .endDate(LocalDateTime.of(endDate, LocalTime.MAX))
            .tasks(tasks)
            .build();
    }

    /**
     * Schedule을 GanttTask로 변환
     */
    private GanttChartData.GanttTask convertToGanttTask(ProductionScheduleEntity schedule) {
        String taskId = "schedule-" + schedule.getScheduleId();
        String taskName = schedule.getWorkOrder().getWorkOrderNo() + " - "
                        + schedule.getRoutingStep().getProcess().getProcessName();
        String parentId = "wo-" + schedule.getWorkOrder().getWorkOrderId();

        // 상태별 색상
        String color = getStatusColor(schedule.getStatus());

        // 리소스 정보
        GanttChartData.ResourceInfo resourceInfo = GanttChartData.ResourceInfo.builder()
            .equipmentCode(schedule.getAssignedEquipment() != null ?
                schedule.getAssignedEquipment().getEquipmentCode() : null)
            .equipmentName(schedule.getAssignedEquipment() != null ?
                schedule.getAssignedEquipment().getEquipmentName() : null)
            .workers(schedule.getAssignedWorkers())
            .assignedUserName(schedule.getAssignedUser() != null ?
                schedule.getAssignedUser().getUsername() : null)
            .build();

        return GanttChartData.GanttTask.builder()
            .id(taskId)
            .name(taskName)
            .startTime(schedule.getPlannedStartTime())
            .endTime(schedule.getPlannedEndTime())
            .duration(schedule.getPlannedDuration())
            .progress(schedule.getProgressRate())
            .status(schedule.getStatus())
            .color(color)
            .parentId(parentId)
            .resource(resourceInfo)
            .build();
    }

    /**
     * 상태별 색상 반환
     */
    private String getStatusColor(String status) {
        switch (status) {
            case "SCHEDULED": return "#3498db";  // 파랑
            case "READY": return "#f39c12";      // 주황
            case "IN_PROGRESS": return "#2ecc71"; // 초록
            case "COMPLETED": return "#95a5a6";   // 회색
            case "DELAYED": return "#e74c3c";     // 빨강
            case "CANCELLED": return "#bdc3c7";   // 연회색
            default: return "#34495e";            // 검정
        }
    }

    /**
     * 통계 정보
     */
    public long countByStatus(String tenantId, String status) {
        return scheduleRepository.countByTenantAndStatus(tenantId, status);
    }

    public long countDelayed(String tenantId) {
        return scheduleRepository.countDelayedSchedules(tenantId);
    }
}
