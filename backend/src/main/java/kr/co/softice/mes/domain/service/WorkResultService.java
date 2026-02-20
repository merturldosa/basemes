package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.entity.WorkResultEntity;
import kr.co.softice.mes.domain.repository.WorkOrderRepository;
import kr.co.softice.mes.domain.repository.WorkResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Work Result Service
 * 작업 실적 비즈니스 로직
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkResultService {

    private final WorkResultRepository workResultRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * 테넌트별 작업 실적 목록 조회
     */
    public List<WorkResultEntity> findByTenant(String tenantId) {
        return workResultRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * 작업 지시별 작업 실적 목록 조회
     */
    public List<WorkResultEntity> findByWorkOrder(Long workOrderId) {
        return workResultRepository.findByWorkOrderIdWithAllRelations(workOrderId);
    }

    /**
     * 작업 실적 ID로 조회
     */
    public Optional<WorkResultEntity> findById(Long workResultId) {
        return workResultRepository.findById(workResultId);
    }

    /**
     * 기간별 작업 실적 조회
     */
    public List<WorkResultEntity> findByDateRange(String tenantId,
                                                   LocalDateTime startDate,
                                                   LocalDateTime endDate) {
        return workResultRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * 작업자별 작업 실적 조회
     */
    public List<WorkResultEntity> findByWorker(Long userId) {
        return workResultRepository.findByWorker_UserId(userId);
    }

    /**
     * 작업 실적 생성 및 작업 지시 수량 업데이트
     */
    @Transactional
    public WorkResultEntity createWorkResult(WorkResultEntity workResult) {
        log.info("Creating work result for work order: {}",
            workResult.getWorkOrder().getWorkOrderId());

        // 작업 실적 저장
        WorkResultEntity saved = workResultRepository.save(workResult);

        // 작업 지시의 실적 수량 업데이트
        updateWorkOrderQuantities(workResult.getWorkOrder().getWorkOrderId());

        return saved;
    }

    /**
     * 작업 실적 수정
     */
    @Transactional
    public WorkResultEntity updateWorkResult(WorkResultEntity workResult) {
        log.info("Updating work result: {}", workResult.getWorkResultId());

        if (!workResultRepository.existsById(workResult.getWorkResultId())) {
            throw new BusinessException(ErrorCode.WORK_RESULT_NOT_FOUND);
        }

        WorkResultEntity updated = workResultRepository.save(workResult);

        // 작업 지시의 실적 수량 재계산
        updateWorkOrderQuantities(workResult.getWorkOrder().getWorkOrderId());

        return updated;
    }

    /**
     * 작업 실적 삭제
     */
    @Transactional
    public void deleteWorkResult(Long workResultId) {
        log.info("Deleting work result: {}", workResultId);

        WorkResultEntity workResult = workResultRepository.findById(workResultId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_RESULT_NOT_FOUND));

        Long workOrderId = workResult.getWorkOrder().getWorkOrderId();

        workResultRepository.deleteById(workResultId);

        // 작업 지시의 실적 수량 재계산
        updateWorkOrderQuantities(workOrderId);
    }

    /**
     * 작업 지시의 실적 수량 업데이트
     * 해당 작업 지시의 모든 실적을 집계하여 작업 지시의 실적 필드 업데이트
     */
    @Transactional
    public void updateWorkOrderQuantities(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        List<WorkResultEntity> results = workResultRepository.findByWorkOrder_WorkOrderId(workOrderId);

        // 실적 합계 계산
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalGoodQuantity = BigDecimal.ZERO;
        BigDecimal totalDefectQuantity = BigDecimal.ZERO;

        for (WorkResultEntity result : results) {
            totalQuantity = totalQuantity.add(result.getQuantity());
            totalGoodQuantity = totalGoodQuantity.add(result.getGoodQuantity());
            totalDefectQuantity = totalDefectQuantity.add(result.getDefectQuantity());
        }

        // 작업 지시 업데이트
        workOrder.setActualQuantity(totalQuantity);
        workOrder.setGoodQuantity(totalGoodQuantity);
        workOrder.setDefectQuantity(totalDefectQuantity);

        workOrderRepository.save(workOrder);

        log.info("Updated work order {} quantities - Total: {}, Good: {}, Defect: {}",
            workOrderId, totalQuantity, totalGoodQuantity, totalDefectQuantity);
    }

    /**
     * 작업 지시의 총 실적 수 조회
     */
    public long countByWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        return workResultRepository.countByWorkOrder(workOrder);
    }
}
