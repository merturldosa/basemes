package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Mold Production History Service
 * 금형 생산 이력 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MoldProductionHistoryService {

    private final MoldProductionHistoryRepository historyRepository;
    private final TenantRepository tenantRepository;
    private final MoldRepository moldRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkResultRepository workResultRepository;
    private final UserRepository userRepository;

    public List<MoldProductionHistoryEntity> getAllHistories(String tenantId) {
        log.info("Getting all mold production histories for tenant: {}", tenantId);
        return historyRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public MoldProductionHistoryEntity getHistoryById(Long historyId) {
        log.info("Getting mold production history by ID: {}", historyId);
        return historyRepository.findByIdWithAllRelations(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_PRODUCTION_HISTORY_NOT_FOUND));
    }

    public List<MoldProductionHistoryEntity> getHistoriesByMold(Long moldId) {
        log.info("Getting production histories for mold ID: {}", moldId);
        return historyRepository.findByMoldId(moldId);
    }

    public List<MoldProductionHistoryEntity> getHistoriesByDateRange(String tenantId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting production histories for tenant: {} from {} to {}", tenantId, startDate, endDate);
        return historyRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    public List<MoldProductionHistoryEntity> getHistoriesByWorkOrder(Long workOrderId) {
        log.info("Getting production histories for work order ID: {}", workOrderId);
        return historyRepository.findByWorkOrderId(workOrderId);
    }

    @Transactional
    public MoldProductionHistoryEntity createHistory(String tenantId, MoldProductionHistoryEntity history) {
        log.info("Creating mold production history for tenant: {}", tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        history.setTenant(tenant);

        MoldEntity mold = moldRepository.findByIdWithAllRelations(history.getMold().getMoldId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_NOT_FOUND));
        history.setMold(mold);

        if (history.getWorkOrder() != null && history.getWorkOrder().getWorkOrderId() != null) {
            WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(history.getWorkOrder().getWorkOrderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));
            history.setWorkOrder(workOrder);
        }

        if (history.getWorkResult() != null && history.getWorkResult().getWorkResultId() != null) {
            WorkResultEntity workResult = workResultRepository.findById(history.getWorkResult().getWorkResultId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_RESULT_NOT_FOUND));
            history.setWorkResult(workResult);
        }

        if (history.getOperatorUser() != null && history.getOperatorUser().getUserId() != null) {
            UserEntity operator = userRepository.findById(history.getOperatorUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            history.setOperatorUser(operator);
            history.setOperatorName(operator.getFullName());
        }

        // Shot count will be auto-updated by database trigger
        MoldProductionHistoryEntity saved = historyRepository.save(history);

        log.info("Mold production history created successfully. Shot count added: {}", history.getShotCount());
        return saved;
    }

    @Transactional
    public MoldProductionHistoryEntity updateHistory(Long historyId, MoldProductionHistoryEntity updateData) {
        log.info("Updating mold production history ID: {}", historyId);

        MoldProductionHistoryEntity existing = historyRepository.findByIdWithAllRelations(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_PRODUCTION_HISTORY_NOT_FOUND));

        if (updateData.getProductionQuantity() != null) {
            existing.setProductionQuantity(updateData.getProductionQuantity());
        }
        if (updateData.getGoodQuantity() != null) {
            existing.setGoodQuantity(updateData.getGoodQuantity());
        }
        if (updateData.getDefectQuantity() != null) {
            existing.setDefectQuantity(updateData.getDefectQuantity());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        MoldProductionHistoryEntity updated = historyRepository.save(existing);
        log.info("Mold production history updated successfully");
        return updated;
    }

    @Transactional
    public void deleteHistory(Long historyId) {
        log.info("Deleting mold production history ID: {}", historyId);

        MoldProductionHistoryEntity history = historyRepository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOLD_PRODUCTION_HISTORY_NOT_FOUND));

        historyRepository.delete(history);
        log.info("Mold production history deleted successfully");
    }
}
