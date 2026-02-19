package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.process.ProcessCreateRequest;
import kr.co.softice.mes.common.dto.process.ProcessResponse;
import kr.co.softice.mes.common.dto.process.ProcessUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.ProcessEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.ProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Process Controller
 * 공정 마스터 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/processes")
@RequiredArgsConstructor
@Tag(name = "Process Management", description = "공정 마스터 관리 API")
public class ProcessController {

    private final ProcessService processService;
    private final TenantRepository tenantRepository;

    /**
     * 공정 목록 조회 (순서대로)
     * GET /api/processes
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "공정 목록 조회", description = "테넌트의 모든 공정을 순서대로 조회")
    public ResponseEntity<ApiResponse<List<ProcessResponse>>> getProcesses() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting processes for tenant: {}", tenantId);

        List<ProcessResponse> processes = processService.findByTenant(tenantId).stream()
                .map(this::toProcessResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("공정 목록 조회 성공", processes));
    }

    /**
     * 활성 공정 목록 조회
     * GET /api/processes/active
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 공정 목록 조회", description = "활성 상태의 공정만 조회")
    public ResponseEntity<ApiResponse<List<ProcessResponse>>> getActiveProcesses() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active processes for tenant: {}", tenantId);

        List<ProcessResponse> processes = processService.findActiveByTenant(tenantId).stream()
                .map(this::toProcessResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("활성 공정 목록 조회 성공", processes));
    }

    /**
     * 공정 상세 조회
     * GET /api/processes/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "공정 상세 조회", description = "공정 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<ProcessResponse>> getProcess(@PathVariable Long id) {
        log.info("Getting process: {}", id);

        ProcessEntity process = processService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROCESS_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("공정 조회 성공", toProcessResponse(process)));
    }

    /**
     * 공정 코드로 조회
     * GET /api/processes/code/{processCode}
     */
    @GetMapping("/code/{processCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "공정 코드로 조회", description = "공정 코드로 공정 정보 조회")
    public ResponseEntity<ApiResponse<ProcessResponse>> getProcessByCode(@PathVariable String processCode) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting process by code: {} for tenant: {}", processCode, tenantId);

        ProcessEntity process = processService.findByProcessCode(tenantId, processCode)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROCESS_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("공정 조회 성공", toProcessResponse(process)));
    }

    /**
     * 공정 생성
     * POST /api/processes
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "공정 생성", description = "신규 공정 등록")
    public ResponseEntity<ApiResponse<ProcessResponse>> createProcess(
            @Valid @RequestBody ProcessCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating process: {} for tenant: {}", request.getProcessCode(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        ProcessEntity process = ProcessEntity.builder()
                .tenant(tenant)
                .processCode(request.getProcessCode())
                .processName(request.getProcessName())
                .processType(request.getProcessType())
                .sequenceOrder(request.getSequenceOrder())
                .isActive(true)
                .description(request.getRemarks())
                .build();

        ProcessEntity createdProcess = processService.createProcess(process);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("공정 생성 성공", toProcessResponse(createdProcess)));
    }

    /**
     * 공정 수정
     * PUT /api/processes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "공정 수정", description = "공정 정보 수정")
    public ResponseEntity<ApiResponse<ProcessResponse>> updateProcess(
            @PathVariable Long id,
            @Valid @RequestBody ProcessUpdateRequest request) {

        log.info("Updating process: {}", id);

        ProcessEntity process = processService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROCESS_NOT_FOUND));

        if (request.getProcessName() != null) {
            process.setProcessName(request.getProcessName());
        }
        if (request.getProcessType() != null) {
            process.setProcessType(request.getProcessType());
        }
        if (request.getSequenceOrder() != null) {
            process.setSequenceOrder(request.getSequenceOrder());
        }
        if (request.getRemarks() != null) {
            process.setDescription(request.getRemarks());
        }

        ProcessEntity updatedProcess = processService.updateProcess(process);

        return ResponseEntity.ok(ApiResponse.success("공정 수정 성공", toProcessResponse(updatedProcess)));
    }

    /**
     * 공정 삭제
     * DELETE /api/processes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공정 삭제", description = "공정 완전 삭제 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteProcess(@PathVariable Long id) {
        log.info("Deleting process: {}", id);

        processService.deleteProcess(id);

        return ResponseEntity.ok(ApiResponse.success("공정 삭제 성공", null));
    }

    /**
     * 공정 활성화
     * POST /api/processes/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "공정 활성화", description = "공정을 활성 상태로 변경")
    public ResponseEntity<ApiResponse<ProcessResponse>> activateProcess(@PathVariable Long id) {
        log.info("Activating process: {}", id);

        ProcessEntity process = processService.activateProcess(id);

        return ResponseEntity.ok(ApiResponse.success("공정 활성화 성공", toProcessResponse(process)));
    }

    /**
     * 공정 비활성화
     * POST /api/processes/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "공정 비활성화", description = "공정을 비활성 상태로 변경")
    public ResponseEntity<ApiResponse<ProcessResponse>> deactivateProcess(@PathVariable Long id) {
        log.info("Deactivating process: {}", id);

        ProcessEntity process = processService.deactivateProcess(id);

        return ResponseEntity.ok(ApiResponse.success("공정 비활성화 성공", toProcessResponse(process)));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private ProcessResponse toProcessResponse(ProcessEntity process) {
        return ProcessResponse.builder()
                .processId(process.getProcessId())
                .processCode(process.getProcessCode())
                .processName(process.getProcessName())
                .processType(process.getProcessType())
                .sequenceOrder(process.getSequenceOrder())
                .isActive(process.getIsActive())
                .tenantId(process.getTenant().getTenantId())
                .tenantName(process.getTenant().getTenantName())
                .remarks(process.getDescription())
                .createdAt(process.getCreatedAt())
                .updatedAt(process.getUpdatedAt())
                .build();
    }
}
