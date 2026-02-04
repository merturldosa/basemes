package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.quality.QualityInspectionCreateRequest;
import kr.co.softice.mes.common.dto.quality.QualityInspectionResponse;
import kr.co.softice.mes.common.dto.quality.QualityInspectionUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.QualityInspectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quality Inspection Controller
 * 품질 검사 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/quality-inspections")
@RequiredArgsConstructor
@Tag(name = "Quality Inspection Management", description = "품질 검사 관리 API")
public class QualityInspectionController {

    private final QualityInspectionService qualityInspectionService;
    private final TenantRepository tenantRepository;
    private final QualityStandardRepository qualityStandardRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkResultRepository workResultRepository;

    /**
     * 품질 검사 목록 조회
     * GET /api/quality-inspections
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "품질 검사 목록 조회", description = "테넌트의 모든 품질 검사 조회")
    public ResponseEntity<ApiResponse<List<QualityInspectionResponse>>> getQualityInspections() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting quality inspections for tenant: {}", tenantId);

        List<QualityInspectionResponse> inspections = qualityInspectionService.findByTenant(tenantId).stream()
                .map(this::toQualityInspectionResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("품질 검사 목록 조회 성공", inspections));
    }

    /**
     * 품질 검사 상세 조회
     * GET /api/quality-inspections/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "품질 검사 상세 조회", description = "품질 검사 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<QualityInspectionResponse>> getQualityInspection(@PathVariable Long id) {
        log.info("Getting quality inspection: {}", id);

        QualityInspectionEntity inspection = qualityInspectionService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.QUALITY_INSPECTION_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("품질 검사 조회 성공", toQualityInspectionResponse(inspection)));
    }

    /**
     * 작업 지시별 품질 검사 조회
     * GET /api/quality-inspections/work-order/{workOrderId}
     */
    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "작업 지시별 품질 검사 조회", description = "특정 작업 지시의 품질 검사 목록 조회")
    public ResponseEntity<ApiResponse<List<QualityInspectionResponse>>> getQualityInspectionsByWorkOrder(
            @PathVariable Long workOrderId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting quality inspections for work order: {} in tenant: {}", workOrderId, tenantId);

        List<QualityInspectionResponse> inspections = qualityInspectionService.findByWorkOrderId(workOrderId).stream()
                .map(this::toQualityInspectionResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("작업 지시별 품질 검사 조회 성공", inspections));
    }

    /**
     * 검사 결과별 품질 검사 조회
     * GET /api/quality-inspections/result/{result}
     */
    @GetMapping("/result/{result}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "검사 결과별 품질 검사 조회", description = "검사 결과별 품질 검사 목록 조회 (PASS, FAIL, CONDITIONAL)")
    public ResponseEntity<ApiResponse<List<QualityInspectionResponse>>> getQualityInspectionsByResult(
            @PathVariable String result) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting quality inspections for result: {} in tenant: {}", result, tenantId);

        List<QualityInspectionResponse> inspections = qualityInspectionService.findByResult(tenantId, result).stream()
                .map(this::toQualityInspectionResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("검사 결과별 품질 검사 조회 성공", inspections));
    }

    /**
     * IQC 의뢰 리스트 조회
     * GET /api/quality-inspections/iqc-requests
     */
    @GetMapping("/iqc-requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "IQC 의뢰 리스트", description = "입고 품질 검사 의뢰 목록 조회")
    public ResponseEntity<ApiResponse<List<QualityInspectionResponse>>> getIQCRequests() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting IQC inspection requests for tenant: {}", tenantId);

        List<QualityInspectionResponse> inspections = qualityInspectionService.findByInspectionType(tenantId, "INCOMING").stream()
                .map(this::toQualityInspectionResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("IQC 의뢰 리스트 조회 성공", inspections));
    }

    /**
     * OQC 의뢰 리스트 조회
     * GET /api/quality-inspections/oqc-requests
     */
    @GetMapping("/oqc-requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "OQC 의뢰 리스트", description = "출하 품질 검사 의뢰 목록 조회")
    public ResponseEntity<ApiResponse<List<QualityInspectionResponse>>> getOQCRequests() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting OQC inspection requests for tenant: {}", tenantId);

        List<QualityInspectionResponse> inspections = qualityInspectionService.findByInspectionType(tenantId, "OUTGOING").stream()
                .map(this::toQualityInspectionResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("OQC 의뢰 리스트 조회 성공", inspections));
    }

    /**
     * 재시험 필요 검사 조회
     * GET /api/quality-inspections/retest-required
     */
    @GetMapping("/retest-required")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "재시험 필요 검사 조회", description = "실패한 검사 중 시정 조치가 정의되었으나 완료되지 않은 검사 목록")
    public ResponseEntity<ApiResponse<List<QualityInspectionResponse>>> getRetestRequired() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting retest required inspections for tenant: {}", tenantId);

        List<QualityInspectionResponse> inspections = qualityInspectionService.findRetestRequired(tenantId).stream()
                .map(this::toQualityInspectionResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("재시험 필요 검사 조회 성공", inspections));
    }

    /**
     * 실패 항목 조회 (반품 처리용)
     * GET /api/quality-inspections/failed-items
     */
    @GetMapping("/failed-items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "실패 항목 조회", description = "반품 처리를 위한 검사 실패 항목 목록")
    public ResponseEntity<ApiResponse<List<QualityInspectionResponse>>> getFailedItems() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting failed items for returns for tenant: {}", tenantId);

        List<QualityInspectionResponse> inspections = qualityInspectionService.findFailedItemsForReturns(tenantId).stream()
                .map(this::toQualityInspectionResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("실패 항목 조회 성공", inspections));
    }

    /**
     * 품질 검사 생성
     * POST /api/quality-inspections
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'QUALITY_INSPECTOR')")
    @Operation(summary = "품질 검사 생성", description = "신규 품질 검사 등록")
    public ResponseEntity<ApiResponse<QualityInspectionResponse>> createQualityInspection(
            @Valid @RequestBody QualityInspectionCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating quality inspection: {} for tenant: {}", request.getInspectionNo(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        QualityStandardEntity qualityStandard = qualityStandardRepository.findById(request.getQualityStandardId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.QUALITY_STANDARD_NOT_FOUND));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        UserEntity inspector = userRepository.findById(request.getInspectorUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        // Optional references
        WorkOrderEntity workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));
        }

        WorkResultEntity workResult = null;
        if (request.getWorkResultId() != null) {
            workResult = workResultRepository.findById(request.getWorkResultId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_RESULT_NOT_FOUND));
        }

        QualityInspectionEntity inspection = QualityInspectionEntity.builder()
                .tenant(tenant)
                .qualityStandard(qualityStandard)
                .workOrder(workOrder)
                .workResult(workResult)
                .product(product)
                .inspectionNo(request.getInspectionNo())
                .inspectionDate(request.getInspectionDate())
                .inspectionType(request.getInspectionType())
                .inspector(inspector)
                .inspectedQuantity(request.getInspectedQuantity())
                .passedQuantity(request.getPassedQuantity())
                .failedQuantity(request.getFailedQuantity())
                .measuredValue(request.getMeasuredValue())
                .measurementUnit(request.getMeasurementUnit())
                .inspectionResult(request.getInspectionResult())
                .defectType(request.getDefectType())
                .defectReason(request.getDefectReason())
                .defectLocation(request.getDefectLocation())
                .correctiveAction(request.getCorrectiveAction())
                .correctiveActionDate(request.getCorrectiveActionDate())
                .remarks(request.getRemarks())
                .build();

        QualityInspectionEntity createdInspection = qualityInspectionService.createQualityInspection(inspection);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("품질 검사 생성 성공", toQualityInspectionResponse(createdInspection)));
    }

    /**
     * 품질 검사 수정
     * PUT /api/quality-inspections/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'QUALITY_INSPECTOR')")
    @Operation(summary = "품질 검사 수정", description = "품질 검사 정보 수정")
    public ResponseEntity<ApiResponse<QualityInspectionResponse>> updateQualityInspection(
            @PathVariable Long id,
            @Valid @RequestBody QualityInspectionUpdateRequest request) {

        log.info("Updating quality inspection: {}", id);

        QualityInspectionEntity inspection = qualityInspectionService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.QUALITY_INSPECTION_NOT_FOUND));

        QualityStandardEntity qualityStandard = qualityStandardRepository.findById(request.getQualityStandardId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.QUALITY_STANDARD_NOT_FOUND));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        UserEntity inspector = userRepository.findById(request.getInspectorUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        // Optional references
        WorkOrderEntity workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));
        }

        WorkResultEntity workResult = null;
        if (request.getWorkResultId() != null) {
            workResult = workResultRepository.findById(request.getWorkResultId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_RESULT_NOT_FOUND));
        }

        inspection.setQualityStandard(qualityStandard);
        inspection.setWorkOrder(workOrder);
        inspection.setWorkResult(workResult);
        inspection.setProduct(product);
        inspection.setInspectionDate(request.getInspectionDate());
        inspection.setInspectionType(request.getInspectionType());
        inspection.setInspector(inspector);
        inspection.setInspectedQuantity(request.getInspectedQuantity());
        inspection.setPassedQuantity(request.getPassedQuantity());
        inspection.setFailedQuantity(request.getFailedQuantity());
        inspection.setMeasuredValue(request.getMeasuredValue());
        inspection.setMeasurementUnit(request.getMeasurementUnit());
        inspection.setInspectionResult(request.getInspectionResult());
        inspection.setDefectType(request.getDefectType());
        inspection.setDefectReason(request.getDefectReason());
        inspection.setDefectLocation(request.getDefectLocation());
        inspection.setCorrectiveAction(request.getCorrectiveAction());
        inspection.setCorrectiveActionDate(request.getCorrectiveActionDate());
        inspection.setRemarks(request.getRemarks());

        QualityInspectionEntity updatedInspection = qualityInspectionService.updateQualityInspection(inspection);

        return ResponseEntity.ok(ApiResponse.success("품질 검사 수정 성공", toQualityInspectionResponse(updatedInspection)));
    }

    /**
     * 품질 검사 삭제
     * DELETE /api/quality-inspections/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "품질 검사 삭제", description = "품질 검사 완전 삭제 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteQualityInspection(@PathVariable Long id) {
        log.info("Deleting quality inspection: {}", id);

        qualityInspectionService.deleteQualityInspection(id);

        return ResponseEntity.ok(ApiResponse.success("품질 검사 삭제 성공", null));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private QualityInspectionResponse toQualityInspectionResponse(QualityInspectionEntity inspection) {
        return QualityInspectionResponse.builder()
                .qualityInspectionId(inspection.getQualityInspectionId())
                .qualityStandardId(inspection.getQualityStandard().getQualityStandardId())
                .standardCode(inspection.getQualityStandard().getStandardCode())
                .standardName(inspection.getQualityStandard().getStandardName())
                .workOrderId(inspection.getWorkOrder() != null ? inspection.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(inspection.getWorkOrder() != null ? inspection.getWorkOrder().getWorkOrderNo() : null)
                .workResultId(inspection.getWorkResult() != null ? inspection.getWorkResult().getWorkResultId() : null)
                .productId(inspection.getProduct().getProductId())
                .productCode(inspection.getProduct().getProductCode())
                .productName(inspection.getProduct().getProductName())
                .inspectionNo(inspection.getInspectionNo())
                .inspectionDate(inspection.getInspectionDate())
                .inspectionType(inspection.getInspectionType())
                .inspectorUserId(inspection.getInspector().getUserId())
                .inspectorUsername(inspection.getInspector().getUsername())
                .inspectorName(inspection.getInspector().getFullName())
                .inspectedQuantity(inspection.getInspectedQuantity())
                .passedQuantity(inspection.getPassedQuantity())
                .failedQuantity(inspection.getFailedQuantity())
                .measuredValue(inspection.getMeasuredValue())
                .measurementUnit(inspection.getMeasurementUnit())
                .inspectionResult(inspection.getInspectionResult())
                .defectType(inspection.getDefectType())
                .defectReason(inspection.getDefectReason())
                .defectLocation(inspection.getDefectLocation())
                .correctiveAction(inspection.getCorrectiveAction())
                .correctiveActionDate(inspection.getCorrectiveActionDate())
                .remarks(inspection.getRemarks())
                .tenantId(inspection.getTenant().getTenantId())
                .tenantName(inspection.getTenant().getTenantName())
                .createdAt(inspection.getCreatedAt())
                .updatedAt(inspection.getUpdatedAt())
                .build();
    }
}
