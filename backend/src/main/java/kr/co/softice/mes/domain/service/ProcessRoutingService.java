package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.ProcessRoutingEntity;
import kr.co.softice.mes.domain.entity.ProcessRoutingStepEntity;
import kr.co.softice.mes.domain.repository.ProcessRoutingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Process Routing Service
 * 공정 라우팅 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessRoutingService {

    private final ProcessRoutingRepository routingRepository;

    /**
     * 테넌트별 전체 라우팅 조회
     */
    public List<ProcessRoutingEntity> findByTenant(String tenantId) {
        return routingRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * 테넌트 및 제품별 라우팅 조회
     */
    public List<ProcessRoutingEntity> findByTenantAndProduct(String tenantId, Long productId) {
        return routingRepository.findByTenantIdAndProductIdWithAllRelations(tenantId, productId);
    }

    /**
     * 테넌트별 활성 라우팅 조회
     */
    public List<ProcessRoutingEntity> findActiveByTenant(String tenantId) {
        return routingRepository.findByTenantIdAndIsActiveWithAllRelations(tenantId, true);
    }

    /**
     * ID로 라우팅 조회
     */
    public Optional<ProcessRoutingEntity> findById(Long routingId) {
        return routingRepository.findByIdWithAllRelations(routingId);
    }

    /**
     * 라우팅 코드 및 버전으로 조회
     */
    public Optional<ProcessRoutingEntity> findByRoutingCodeAndVersion(
            String tenantId, String routingCode, String version) {
        return routingRepository.findByTenant_TenantIdAndRoutingCodeAndVersion(
            tenantId, routingCode, version);
    }

    /**
     * 라우팅 생성
     */
    @Transactional
    public ProcessRoutingEntity createRouting(ProcessRoutingEntity routing) {
        log.info("Creating routing: {} version: {} for product: {}",
            routing.getRoutingCode(), routing.getVersion(), routing.getProduct().getProductCode());

        // 중복 체크
        if (routingRepository.existsByTenantAndRoutingCodeAndVersion(
            routing.getTenant(), routing.getRoutingCode(), routing.getVersion())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }

        // Set sequence numbers for steps if not set
        int sequence = 1;
        for (ProcessRoutingStepEntity step : routing.getSteps()) {
            if (step.getSequenceOrder() == null) {
                step.setSequenceOrder(sequence++);
            }
            step.setRouting(routing);
        }

        ProcessRoutingEntity saved = routingRepository.save(routing);
        return routingRepository.findByIdWithAllRelations(saved.getRoutingId()).orElse(saved);
    }

    /**
     * 라우팅 수정
     */
    @Transactional
    public ProcessRoutingEntity updateRouting(ProcessRoutingEntity routing) {
        log.info("Updating routing: {}", routing.getRoutingId());

        // Set sequence numbers for new steps
        int sequence = 1;
        List<ProcessRoutingStepEntity> newSteps = new ArrayList<>(routing.getSteps());

        // Clear existing steps and add new ones
        routing.clearSteps();

        for (ProcessRoutingStepEntity step : newSteps) {
            if (step.getSequenceOrder() == null) {
                step.setSequenceOrder(sequence++);
            }
            routing.addStep(step);
        }

        ProcessRoutingEntity updated = routingRepository.save(routing);
        return routingRepository.findByIdWithAllRelations(updated.getRoutingId()).orElse(updated);
    }

    /**
     * 라우팅 삭제
     */
    @Transactional
    public void deleteRouting(Long routingId) {
        log.info("Deleting routing: {}", routingId);
        routingRepository.deleteById(routingId);
    }

    /**
     * 라우팅 활성화/비활성화 토글
     */
    @Transactional
    public ProcessRoutingEntity toggleActive(Long routingId) {
        ProcessRoutingEntity routing = routingRepository.findById(routingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        log.info("Toggling routing {} active status from {} to {}",
            routing.getRoutingCode(), routing.getIsActive(), !routing.getIsActive());

        routing.setIsActive(!routing.getIsActive());
        ProcessRoutingEntity updated = routingRepository.save(routing);
        return routingRepository.findByIdWithAllRelations(updated.getRoutingId()).orElse(updated);
    }

    /**
     * 라우팅을 새 버전으로 복사
     */
    @Transactional
    public ProcessRoutingEntity copyRouting(Long sourceRoutingId, String newVersion) {
        ProcessRoutingEntity sourceRouting = routingRepository.findByIdWithAllRelations(sourceRoutingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        log.info("Copying routing {} from version {} to version {}",
            sourceRouting.getRoutingCode(), sourceRouting.getVersion(), newVersion);

        // Check if target version already exists
        if (routingRepository.existsByTenantAndRoutingCodeAndVersion(
            sourceRouting.getTenant(), sourceRouting.getRoutingCode(), newVersion)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }

        // Create new routing with new version
        ProcessRoutingEntity newRouting = ProcessRoutingEntity.builder()
            .tenant(sourceRouting.getTenant())
            .product(sourceRouting.getProduct())
            .routingCode(sourceRouting.getRoutingCode())
            .routingName(sourceRouting.getRoutingName())
            .version(newVersion)
            .effectiveDate(sourceRouting.getEffectiveDate())
            .expiryDate(sourceRouting.getExpiryDate())
            .isActive(true)
            .remarks(sourceRouting.getRemarks())
            .build();

        // Copy steps
        for (ProcessRoutingStepEntity sourceStep : sourceRouting.getSteps()) {
            ProcessRoutingStepEntity newStep = ProcessRoutingStepEntity.builder()
                .sequenceOrder(sourceStep.getSequenceOrder())
                .process(sourceStep.getProcess())
                .standardTime(sourceStep.getStandardTime())
                .setupTime(sourceStep.getSetupTime())
                .waitTime(sourceStep.getWaitTime())
                .requiredWorkers(sourceStep.getRequiredWorkers())
                .equipment(sourceStep.getEquipment())
                .isParallel(sourceStep.getIsParallel())
                .parallelGroup(sourceStep.getParallelGroup())
                .isOptional(sourceStep.getIsOptional())
                .alternateProcess(sourceStep.getAlternateProcess())
                .qualityCheckRequired(sourceStep.getQualityCheckRequired())
                .qualityStandard(sourceStep.getQualityStandard())
                .remarks(sourceStep.getRemarks())
                .build();
            newRouting.addStep(newStep);
        }

        ProcessRoutingEntity saved = routingRepository.save(newRouting);
        return routingRepository.findByIdWithAllRelations(saved.getRoutingId()).orElse(saved);
    }
}
