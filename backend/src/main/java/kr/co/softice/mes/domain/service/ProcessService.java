package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.ProcessEntity;
import kr.co.softice.mes.domain.repository.ProcessRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Process Service
 * 공정 마스터 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessService {

    private final ProcessRepository processRepository;
    private final TenantRepository tenantRepository;

    /**
     * Find all processes by tenant ID
     */
    public List<ProcessEntity> findByTenant(String tenantId) {
        return processRepository.findByTenantIdWithTenantOrderBySequence(tenantId);
    }

    /**
     * Find active processes by tenant ID
     */
    public List<ProcessEntity> findActiveByTenant(String tenantId) {
        return processRepository.findByTenantIdAndIsActiveWithTenant(tenantId, true);
    }

    /**
     * Find process by ID
     */
    public Optional<ProcessEntity> findById(Long processId) {
        return processRepository.findById(processId);
    }

    /**
     * Find process by process code
     */
    public Optional<ProcessEntity> findByProcessCode(String tenantId, String processCode) {
        return processRepository.findByTenant_TenantIdAndProcessCode(tenantId, processCode);
    }

    /**
     * Create new process
     */
    @Transactional
    public ProcessEntity createProcess(ProcessEntity process) {
        log.info("Creating process: {} for tenant: {}",
            process.getProcessCode(), process.getTenant().getTenantId());

        // Check duplicate
        if (processRepository.existsByTenantAndProcessCode(process.getTenant(), process.getProcessCode())) {
            throw new BusinessException(ErrorCode.PROCESS_ALREADY_EXISTS);
        }

        return processRepository.save(process);
    }

    /**
     * Update process
     */
    @Transactional
    public ProcessEntity updateProcess(ProcessEntity process) {
        log.info("Updating process: {}", process.getProcessId());

        if (!processRepository.existsById(process.getProcessId())) {
            throw new BusinessException(ErrorCode.PROCESS_NOT_FOUND);
        }

        return processRepository.save(process);
    }

    /**
     * Delete process
     */
    @Transactional
    public void deleteProcess(Long processId) {
        log.info("Deleting process: {}", processId);
        processRepository.deleteById(processId);
    }

    /**
     * Activate process
     */
    @Transactional
    public ProcessEntity activateProcess(Long processId) {
        ProcessEntity process = processRepository.findById(processId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROCESS_NOT_FOUND));

        process.setIsActive(true);
        return processRepository.save(process);
    }

    /**
     * Deactivate process
     */
    @Transactional
    public ProcessEntity deactivateProcess(Long processId) {
        ProcessEntity process = processRepository.findById(processId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROCESS_NOT_FOUND));

        process.setIsActive(false);
        return processRepository.save(process);
    }
}
