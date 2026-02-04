package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * WorkOrder Service
 * 작업 지시 비즈니스 로직
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;

    /**
     * 테넌트별 작업 지시 목록 조회
     */
    public List<WorkOrderEntity> findByTenant(String tenantId) {
        return workOrderRepository.findByTenant_TenantId(tenantId);
    }

    /**
     * 작업 지시 ID로 조회
     */
    public Optional<WorkOrderEntity> findById(Long workOrderId) {
        return workOrderRepository.findById(workOrderId);
    }

    /**
     * 작업 지시 생성
     */
    @Transactional
    public WorkOrderEntity createWorkOrder(WorkOrderEntity workOrder) {
        return workOrderRepository.save(workOrder);
    }

    /**
     * 작업 지시 수정
     */
    @Transactional
    public WorkOrderEntity updateWorkOrder(WorkOrderEntity workOrder) {
        return workOrderRepository.save(workOrder);
    }

    /**
     * 작업 지시 삭제
     */
    @Transactional
    public void deleteWorkOrder(Long workOrderId) {
        workOrderRepository.deleteById(workOrderId);
    }

    /**
     * 상태별 작업 지시 조회
     */
    public List<WorkOrderEntity> findByStatus(String tenantId, String status) {
        return workOrderRepository.findByTenant_TenantIdAndStatus(tenantId, status);
    }

    /**
     * 기간별 작업 지시 조회
     */
    public List<WorkOrderEntity> findByDateRange(String tenantId,
                                                  java.time.LocalDateTime startDate,
                                                  java.time.LocalDateTime endDate) {
        return workOrderRepository.findByTenantIdAndDateRange(
            tenantId, startDate, endDate);
    }

    /**
     * 작업 시작
     */
    @Transactional
    public WorkOrderEntity startWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("작업 지시를 찾을 수 없습니다: " + workOrderId));

        if (!"READY".equals(workOrder.getStatus()) && !"PENDING".equals(workOrder.getStatus())) {
            throw new IllegalStateException("작업을 시작할 수 없는 상태입니다: " + workOrder.getStatus());
        }

        workOrder.setStatus("IN_PROGRESS");
        workOrder.setActualStartDate(java.time.LocalDateTime.now());

        return workOrderRepository.save(workOrder);
    }

    /**
     * 작업 완료
     */
    @Transactional
    public WorkOrderEntity completeWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("작업 지시를 찾을 수 없습니다: " + workOrderId));

        if (!"IN_PROGRESS".equals(workOrder.getStatus())) {
            throw new IllegalStateException("진행 중인 작업만 완료할 수 있습니다");
        }

        workOrder.setStatus("COMPLETED");
        workOrder.setActualEndDate(java.time.LocalDateTime.now());

        return workOrderRepository.save(workOrder);
    }

    /**
     * 작업 취소
     */
    @Transactional
    public WorkOrderEntity cancelWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("작업 지시를 찾을 수 없습니다: " + workOrderId));

        if ("COMPLETED".equals(workOrder.getStatus())) {
            throw new IllegalStateException("완료된 작업은 취소할 수 없습니다");
        }

        workOrder.setStatus("CANCELLED");

        return workOrderRepository.save(workOrder);
    }

    /**
     * 작업 준비 완료
     */
    @Transactional
    public WorkOrderEntity readyWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("작업 지시를 찾을 수 없습니다: " + workOrderId));

        if (!"PENDING".equals(workOrder.getStatus())) {
            throw new IllegalStateException("대기 중인 작업만 준비 완료로 변경할 수 있습니다");
        }

        workOrder.setStatus("READY");

        return workOrderRepository.save(workOrder);
    }
}
