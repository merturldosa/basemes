package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.quality.QualityStandardCreateRequest;
import kr.co.softice.mes.common.dto.quality.QualityStandardResponse;
import kr.co.softice.mes.common.dto.quality.QualityStandardUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.QualityStandardEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.QualityStandardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quality Standard Controller
 * 품질 기준 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/quality-standards")
@RequiredArgsConstructor
@Tag(name = "Quality Standard Management", description = "품질 기준 관리 API")
public class QualityStandardController {

    private final QualityStandardService qualityStandardService;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;

    /**
     * 품질 기준 목록 조회
     * GET /api/quality-standards
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "품질 기준 목록 조회", description = "테넌트의 모든 품질 기준 조회")
    public ResponseEntity<ApiResponse<List<QualityStandardResponse>>> getQualityStandards() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting quality standards for tenant: {}", tenantId);

        List<QualityStandardResponse> standards = qualityStandardService.findByTenant(tenantId).stream()
                .map(this::toQualityStandardResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("품질 기준 목록 조회 성공", standards));
    }

    /**
     * 활성 품질 기준 목록 조회
     * GET /api/quality-standards/active
     */
    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 품질 기준 목록 조회", description = "활성 상태의 품질 기준만 조회")
    public ResponseEntity<ApiResponse<List<QualityStandardResponse>>> getActiveQualityStandards() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active quality standards for tenant: {}", tenantId);

        List<QualityStandardResponse> standards = qualityStandardService.findActiveByTenant(tenantId).stream()
                .map(this::toQualityStandardResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("활성 품질 기준 목록 조회 성공", standards));
    }

    /**
     * 품질 기준 상세 조회
     * GET /api/quality-standards/{id}
     */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "품질 기준 상세 조회", description = "품질 기준 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<QualityStandardResponse>> getQualityStandard(@PathVariable Long id) {
        log.info("Getting quality standard: {}", id);

        QualityStandardEntity standard = qualityStandardService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.QUALITY_STANDARD_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("품질 기준 조회 성공", toQualityStandardResponse(standard)));
    }

    /**
     * 제품별 품질 기준 조회
     * GET /api/quality-standards/product/{productId}
     */
    @Transactional(readOnly = true)
    @GetMapping("/product/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "제품별 품질 기준 조회", description = "특정 제품의 품질 기준 목록 조회")
    public ResponseEntity<ApiResponse<List<QualityStandardResponse>>> getQualityStandardsByProduct(
            @PathVariable Long productId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting quality standards for product: {} in tenant: {}", productId, tenantId);

        List<QualityStandardResponse> standards = qualityStandardService.findByProductId(productId).stream()
                .map(this::toQualityStandardResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("제품별 품질 기준 조회 성공", standards));
    }

    /**
     * 품질 기준 생성
     * POST /api/quality-standards
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "품질 기준 생성", description = "신규 품질 기준 등록")
    public ResponseEntity<ApiResponse<QualityStandardResponse>> createQualityStandard(
            @Valid @RequestBody QualityStandardCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating quality standard: {} for tenant: {}", request.getStandardCode(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        QualityStandardEntity standard = QualityStandardEntity.builder()
                .tenant(tenant)
                .product(product)
                .standardCode(request.getStandardCode())
                .standardName(request.getStandardName())
                .standardVersion(request.getStandardVersion())
                .inspectionType(request.getInspectionType())
                .inspectionMethod(request.getInspectionMethod())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .targetValue(request.getTargetValue())
                .toleranceValue(request.getToleranceValue())
                .unit(request.getUnit())
                .measurementItem(request.getMeasurementItem())
                .measurementEquipment(request.getMeasurementEquipment())
                .samplingMethod(request.getSamplingMethod())
                .sampleSize(request.getSampleSize())
                .isActive(request.getIsActive())
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .remarks(request.getRemarks())
                .build();

        QualityStandardEntity createdStandard = qualityStandardService.createQualityStandard(standard);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("품질 기준 생성 성공", toQualityStandardResponse(createdStandard)));
    }

    /**
     * 품질 기준 수정
     * PUT /api/quality-standards/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "품질 기준 수정", description = "품질 기준 정보 수정")
    public ResponseEntity<ApiResponse<QualityStandardResponse>> updateQualityStandard(
            @PathVariable Long id,
            @Valid @RequestBody QualityStandardUpdateRequest request) {

        log.info("Updating quality standard: {}", id);

        QualityStandardEntity standard = qualityStandardService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.QUALITY_STANDARD_NOT_FOUND));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        standard.setProduct(product);
        standard.setStandardName(request.getStandardName());
        standard.setInspectionType(request.getInspectionType());
        standard.setInspectionMethod(request.getInspectionMethod());
        standard.setMinValue(request.getMinValue());
        standard.setMaxValue(request.getMaxValue());
        standard.setTargetValue(request.getTargetValue());
        standard.setToleranceValue(request.getToleranceValue());
        standard.setUnit(request.getUnit());
        standard.setMeasurementItem(request.getMeasurementItem());
        standard.setMeasurementEquipment(request.getMeasurementEquipment());
        standard.setSamplingMethod(request.getSamplingMethod());
        standard.setSampleSize(request.getSampleSize());
        standard.setIsActive(request.getIsActive());
        standard.setEffectiveDate(request.getEffectiveDate());
        standard.setExpiryDate(request.getExpiryDate());
        standard.setRemarks(request.getRemarks());

        QualityStandardEntity updatedStandard = qualityStandardService.updateQualityStandard(standard);

        return ResponseEntity.ok(ApiResponse.success("품질 기준 수정 성공", toQualityStandardResponse(updatedStandard)));
    }

    /**
     * 품질 기준 삭제
     * DELETE /api/quality-standards/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "품질 기준 삭제", description = "품질 기준 완전 삭제 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteQualityStandard(@PathVariable Long id) {
        log.info("Deleting quality standard: {}", id);

        qualityStandardService.deleteQualityStandard(id);

        return ResponseEntity.ok(ApiResponse.success("품질 기준 삭제 성공", null));
    }

    /**
     * 품질 기준 활성화
     * PUT /api/quality-standards/{id}/activate
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "품질 기준 활성화", description = "품질 기준을 활성 상태로 변경")
    public ResponseEntity<ApiResponse<QualityStandardResponse>> activateQualityStandard(@PathVariable Long id) {
        log.info("Activating quality standard: {}", id);

        QualityStandardEntity standard = qualityStandardService.activateQualityStandard(id);

        return ResponseEntity.ok(ApiResponse.success("품질 기준 활성화 성공", toQualityStandardResponse(standard)));
    }

    /**
     * 품질 기준 비활성화
     * PUT /api/quality-standards/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "품질 기준 비활성화", description = "품질 기준을 비활성 상태로 변경")
    public ResponseEntity<ApiResponse<QualityStandardResponse>> deactivateQualityStandard(@PathVariable Long id) {
        log.info("Deactivating quality standard: {}", id);

        QualityStandardEntity standard = qualityStandardService.deactivateQualityStandard(id);

        return ResponseEntity.ok(ApiResponse.success("품질 기준 비활성화 성공", toQualityStandardResponse(standard)));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private QualityStandardResponse toQualityStandardResponse(QualityStandardEntity standard) {
        return QualityStandardResponse.builder()
                .qualityStandardId(standard.getQualityStandardId())
                .productId(standard.getProduct().getProductId())
                .productCode(standard.getProduct().getProductCode())
                .productName(standard.getProduct().getProductName())
                .standardCode(standard.getStandardCode())
                .standardName(standard.getStandardName())
                .standardVersion(standard.getStandardVersion())
                .inspectionType(standard.getInspectionType())
                .inspectionMethod(standard.getInspectionMethod())
                .minValue(standard.getMinValue())
                .maxValue(standard.getMaxValue())
                .targetValue(standard.getTargetValue())
                .toleranceValue(standard.getToleranceValue())
                .unit(standard.getUnit())
                .measurementItem(standard.getMeasurementItem())
                .measurementEquipment(standard.getMeasurementEquipment())
                .samplingMethod(standard.getSamplingMethod())
                .sampleSize(standard.getSampleSize())
                .isActive(standard.getIsActive())
                .effectiveDate(standard.getEffectiveDate())
                .expiryDate(standard.getExpiryDate())
                .remarks(standard.getRemarks())
                .tenantId(standard.getTenant().getTenantId())
                .tenantName(standard.getTenant().getTenantName())
                .createdAt(standard.getCreatedAt())
                .updatedAt(standard.getUpdatedAt())
                .build();
    }
}
