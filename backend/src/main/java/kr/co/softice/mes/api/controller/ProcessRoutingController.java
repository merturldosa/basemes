package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.routing.*;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.ProcessRepository;
import kr.co.softice.mes.domain.repository.EquipmentRepository;
import kr.co.softice.mes.domain.service.ProcessRoutingService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Process Routing Controller
 * 공정 라우팅 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/routings")
@RequiredArgsConstructor
@Tag(name = "Process Routing", description = "공정 라우팅 관리 API")
public class ProcessRoutingController {

    private final ProcessRoutingService routingService;
    private final ProductRepository productRepository;
    private final ProcessRepository processRepository;
    private final EquipmentRepository equipmentRepository;
    private final kr.co.softice.mes.domain.repository.TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER', 'USER')")
    public ResponseEntity<List<RoutingResponse>> getAllRoutings() {
        String tenantId = TenantContext.getCurrentTenant();
        List<ProcessRoutingEntity> routings = routingService.findByTenant(tenantId);
        return ResponseEntity.ok(routings.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER', 'USER')")
    public ResponseEntity<List<RoutingResponse>> getActiveRoutings() {
        String tenantId = TenantContext.getCurrentTenant();
        List<ProcessRoutingEntity> routings = routingService.findActiveByTenant(tenantId);
        return ResponseEntity.ok(routings.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER', 'USER')")
    public ResponseEntity<List<RoutingResponse>> getRoutingsByProduct(@PathVariable Long productId) {
        String tenantId = TenantContext.getCurrentTenant();
        List<ProcessRoutingEntity> routings = routingService.findByTenantAndProduct(tenantId, productId);
        return ResponseEntity.ok(routings.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{routingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER', 'USER')")
    public ResponseEntity<RoutingResponse> getRoutingById(@PathVariable Long routingId) {
        return routingService.findById(routingId)
            .map(routing -> ResponseEntity.ok(toResponse(routing)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER')")
    public ResponseEntity<RoutingResponse> createRouting(@Valid @RequestBody RoutingCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        ProductEntity product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProcessRoutingEntity routing = ProcessRoutingEntity.builder()
            .tenant(tenant)
            .product(product)
            .routingCode(request.getRoutingCode())
            .routingName(request.getRoutingName())
            .version(request.getVersion())
            .effectiveDate(request.getEffectiveDate())
            .expiryDate(request.getExpiryDate())
            .isActive(request.getIsActive())
            .remarks(request.getRemarks())
            .steps(new ArrayList<>())
            .build();

        // Add steps
        for (RoutingStepRequest stepReq : request.getSteps()) {
            ProcessEntity process = processRepository.findById(stepReq.getProcessId())
                .orElseThrow(() -> new IllegalArgumentException("Process not found"));

            EquipmentEntity equipment = null;
            if (stepReq.getEquipmentId() != null) {
                equipment = equipmentRepository.findById(stepReq.getEquipmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));
            }

            ProcessEntity alternateProcess = null;
            if (stepReq.getAlternateProcessId() != null) {
                alternateProcess = processRepository.findById(stepReq.getAlternateProcessId())
                    .orElseThrow(() -> new IllegalArgumentException("Alternate process not found"));
            }

            ProcessRoutingStepEntity step = ProcessRoutingStepEntity.builder()
                .sequenceOrder(stepReq.getSequenceOrder())
                .process(process)
                .standardTime(stepReq.getStandardTime())
                .setupTime(stepReq.getSetupTime())
                .waitTime(stepReq.getWaitTime())
                .requiredWorkers(stepReq.getRequiredWorkers())
                .equipment(equipment)
                .isParallel(stepReq.getIsParallel())
                .parallelGroup(stepReq.getParallelGroup())
                .isOptional(stepReq.getIsOptional())
                .alternateProcess(alternateProcess)
                .qualityCheckRequired(stepReq.getQualityCheckRequired())
                .qualityStandard(stepReq.getQualityStandard())
                .remarks(stepReq.getRemarks())
                .build();

            routing.addStep(step);
        }

        ProcessRoutingEntity created = routingService.createRouting(routing);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{routingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER')")
    public ResponseEntity<RoutingResponse> updateRouting(
        @PathVariable Long routingId,
        @Valid @RequestBody RoutingUpdateRequest request) {

        return routingService.findById(routingId)
            .map(routing -> {
                routing.setRoutingName(request.getRoutingName());
                routing.setEffectiveDate(request.getEffectiveDate());
                routing.setExpiryDate(request.getExpiryDate());
                routing.setIsActive(request.getIsActive());
                routing.setRemarks(request.getRemarks());

                // Clear and rebuild steps
                routing.clearSteps();
                for (RoutingStepRequest stepReq : request.getSteps()) {
                    ProcessEntity process = processRepository.findById(stepReq.getProcessId())
                        .orElseThrow(() -> new IllegalArgumentException("Process not found"));

                    EquipmentEntity equipment = null;
                    if (stepReq.getEquipmentId() != null) {
                        equipment = equipmentRepository.findById(stepReq.getEquipmentId())
                            .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));
                    }

                    ProcessEntity alternateProcess = null;
                    if (stepReq.getAlternateProcessId() != null) {
                        alternateProcess = processRepository.findById(stepReq.getAlternateProcessId())
                            .orElseThrow(() -> new IllegalArgumentException("Alternate process not found"));
                    }

                    ProcessRoutingStepEntity step = ProcessRoutingStepEntity.builder()
                        .sequenceOrder(stepReq.getSequenceOrder())
                        .process(process)
                        .standardTime(stepReq.getStandardTime())
                        .setupTime(stepReq.getSetupTime())
                        .waitTime(stepReq.getWaitTime())
                        .requiredWorkers(stepReq.getRequiredWorkers())
                        .equipment(equipment)
                        .isParallel(stepReq.getIsParallel())
                        .parallelGroup(stepReq.getParallelGroup())
                        .isOptional(stepReq.getIsOptional())
                        .alternateProcess(alternateProcess)
                        .qualityCheckRequired(stepReq.getQualityCheckRequired())
                        .qualityStandard(stepReq.getQualityStandard())
                        .remarks(stepReq.getRemarks())
                        .build();

                    routing.addStep(step);
                }

                ProcessRoutingEntity updated = routingService.updateRouting(routing);
                return ResponseEntity.ok(toResponse(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{routingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRouting(@PathVariable Long routingId) {
        routingService.deleteRouting(routingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{routingId}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER')")
    public ResponseEntity<RoutingResponse> toggleActive(@PathVariable Long routingId) {
        ProcessRoutingEntity routing = routingService.toggleActive(routingId);
        return ResponseEntity.ok(toResponse(routing));
    }

    @PostMapping("/{routingId}/copy")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER')")
    public ResponseEntity<RoutingResponse> copyRouting(
        @PathVariable Long routingId,
        @RequestParam String newVersion) {
        ProcessRoutingEntity copied = routingService.copyRouting(routingId, newVersion);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(copied));
    }

    private RoutingResponse toResponse(ProcessRoutingEntity routing) {
        List<RoutingStepResponse> stepResponses = routing.getSteps().stream()
            .map(this::toStepResponse)
            .collect(Collectors.toList());

        return RoutingResponse.builder()
            .routingId(routing.getRoutingId())
            .tenantId(routing.getTenant().getTenantId())
            .tenantName(routing.getTenant().getTenantName())
            .productId(routing.getProduct().getProductId())
            .productCode(routing.getProduct().getProductCode())
            .productName(routing.getProduct().getProductName())
            .routingCode(routing.getRoutingCode())
            .routingName(routing.getRoutingName())
            .version(routing.getVersion())
            .effectiveDate(routing.getEffectiveDate())
            .expiryDate(routing.getExpiryDate())
            .isActive(routing.getIsActive())
            .totalStandardTime(routing.getTotalStandardTime())
            .remarks(routing.getRemarks())
            .steps(stepResponses)
            .createdAt(routing.getCreatedAt())
            .updatedAt(routing.getUpdatedAt())
            .build();
    }

    private RoutingStepResponse toStepResponse(ProcessRoutingStepEntity step) {
        return RoutingStepResponse.builder()
            .routingStepId(step.getRoutingStepId())
            .routingId(step.getRouting().getRoutingId())
            .sequenceOrder(step.getSequenceOrder())
            .processId(step.getProcess().getProcessId())
            .processCode(step.getProcess().getProcessCode())
            .processName(step.getProcess().getProcessName())
            .standardTime(step.getStandardTime())
            .setupTime(step.getSetupTime())
            .waitTime(step.getWaitTime())
            .requiredWorkers(step.getRequiredWorkers())
            .equipmentId(step.getEquipment() != null ? step.getEquipment().getEquipmentId() : null)
            .equipmentCode(step.getEquipment() != null ? step.getEquipment().getEquipmentCode() : null)
            .equipmentName(step.getEquipment() != null ? step.getEquipment().getEquipmentName() : null)
            .isParallel(step.getIsParallel())
            .parallelGroup(step.getParallelGroup())
            .isOptional(step.getIsOptional())
            .alternateProcessId(step.getAlternateProcess() != null ? step.getAlternateProcess().getProcessId() : null)
            .alternateProcessCode(step.getAlternateProcess() != null ? step.getAlternateProcess().getProcessCode() : null)
            .alternateProcessName(step.getAlternateProcess() != null ? step.getAlternateProcess().getProcessName() : null)
            .qualityCheckRequired(step.getQualityCheckRequired())
            .qualityStandard(step.getQualityStandard())
            .remarks(step.getRemarks())
            .createdAt(step.getCreatedAt())
            .updatedAt(step.getUpdatedAt())
            .build();
    }
}
