package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.weighing.*;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.WeighingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Weighing Controller
 * 칭량 관리 API
 *
 * 엔드포인트:
 * - GET /api/weighings - 칭량 목록
 * - GET /api/weighings/{id} - 칭량 상세
 * - GET /api/weighings/tolerance-exceeded - 허용 오차 초과 목록
 * - GET /api/weighings/pending-verification - 검증 대기 목록
 * - GET /api/weighings/reference/{type}/{id} - 참조 문서별 조회
 * - POST /api/weighings - 칭량 생성
 * - PUT /api/weighings/{id} - 칭량 수정
 * - POST /api/weighings/{id}/verify - 칭량 검증 (GMP 이중 검증)
 * - DELETE /api/weighings/{id} - 칭량 삭제
 *
 * 워크플로우: PENDING → VERIFIED or REJECTED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/weighings")
@RequiredArgsConstructor
@Tag(name = "Weighing Management", description = "칭량 관리 API (GMP 준수)")
public class WeighingController {

    private final WeighingService weighingService;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;

    /**
     * 칭량 목록 조회
     * GET /api/weighings
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 목록 조회", description = "테넌트의 모든 칭량 기록 조회")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getWeighings(
            @RequestParam(required = false) String weighingType,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) Long productId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting weighings for tenant: {}, type: {}, status: {}, product: {}",
            tenantId, weighingType, verificationStatus, productId);

        List<WeighingEntity> weighings;

        if (weighingType != null) {
            weighings = weighingService.findByType(tenantId, weighingType);
        } else if (verificationStatus != null) {
            weighings = weighingService.findByVerificationStatus(tenantId, verificationStatus);
        } else {
            weighings = weighingService.findByTenant(tenantId);
        }

        // Additional filtering by product if needed
        if (productId != null) {
            weighings = weighings.stream()
                .filter(w -> w.getProduct().getProductId().equals(productId))
                .collect(Collectors.toList());
        }

        List<WeighingResponse> responses = weighings.stream()
                .map(this::toWeighingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("칭량 목록 조회 성공", responses));
    }

    /**
     * 칭량 상세 조회
     * GET /api/weighings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 상세 조회", description = "칭량 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<WeighingResponse>> getWeighing(@PathVariable Long id) {
        log.info("Getting weighing: {}", id);

        WeighingEntity weighing = weighingService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WEIGHING_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("칭량 조회 성공", toWeighingResponse(weighing)));
    }

    /**
     * 허용 오차 초과 칭량 조회
     * GET /api/weighings/tolerance-exceeded
     */
    @GetMapping("/tolerance-exceeded")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "허용 오차 초과 조회", description = "허용 오차를 초과한 칭량 기록 조회")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getToleranceExceeded() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting tolerance exceeded weighings for tenant: {}", tenantId);

        List<WeighingEntity> weighings = weighingService.findToleranceExceeded(tenantId);
        List<WeighingResponse> responses = weighings.stream()
                .map(this::toWeighingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("허용 오차 초과 칭량 조회 성공", responses));
    }

    /**
     * 검증 대기 칭량 조회
     * GET /api/weighings/pending-verification
     */
    @GetMapping("/pending-verification")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "검증 대기 조회", description = "검증 대기 중인 칭량 기록 조회")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getPendingVerification() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting pending verification weighings for tenant: {}", tenantId);

        List<WeighingEntity> weighings = weighingService.findPendingVerification(tenantId);
        List<WeighingResponse> responses = weighings.stream()
                .map(this::toWeighingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("검증 대기 칭량 조회 성공", responses));
    }

    /**
     * 미검증 허용 오차 초과 칭량 조회 (긴급 주의 필요)
     * GET /api/weighings/unverified-tolerance-exceeded
     */
    @GetMapping("/unverified-tolerance-exceeded")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "미검증 허용 오차 초과 조회", description = "검증되지 않은 허용 오차 초과 칭량 (긴급 주의)")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getUnverifiedToleranceExceeded() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting unverified tolerance exceeded weighings for tenant: {}", tenantId);

        List<WeighingEntity> weighings = weighingService.findUnverifiedToleranceExceeded(tenantId);
        List<WeighingResponse> responses = weighings.stream()
                .map(this::toWeighingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("미검증 허용 오차 초과 칭량 조회 성공", responses));
    }

    /**
     * 참조 문서별 칭량 조회
     * GET /api/weighings/reference/{type}/{id}
     */
    @GetMapping("/reference/{type}/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "참조 문서별 조회", description = "참조 문서(불출, 작업지시 등)의 칭량 기록 조회")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getWeighingsByReference(
            @PathVariable String type,
            @PathVariable Long id) {
        log.info("Getting weighings for reference: type={}, id={}", type, id);

        List<WeighingEntity> weighings = weighingService.findByReference(type, id);
        List<WeighingResponse> responses = weighings.stream()
                .map(this::toWeighingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("참조 문서 칭량 조회 성공", responses));
    }

    /**
     * 칭량 생성
     * POST /api/weighings
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 생성", description = "새로운 칭량 기록 생성 (자동 계산)")
    public ResponseEntity<ApiResponse<WeighingResponse>> createWeighing(
            @Valid @RequestBody WeighingCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating weighing for tenant: {}, type: {}, product: {}",
            tenantId, request.getWeighingType(), request.getProductId());

        // Resolve entities
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        UserEntity operator = userRepository.findById(request.getOperatorUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        LotEntity lot = null;
        if (request.getLotId() != null) {
            lot = lotRepository.findById(request.getLotId())
                    .orElse(null);
        }

        // Build weighing entity
        WeighingEntity weighing = WeighingEntity.builder()
                .tenant(tenant)
                .weighingNo(request.getWeighingNo())
                .weighingDate(request.getWeighingDate())
                .weighingType(request.getWeighingType())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .product(product)
                .lot(lot)
                .tareWeight(request.getTareWeight())
                .grossWeight(request.getGrossWeight())
                .expectedWeight(request.getExpectedWeight())
                .unit(request.getUnit())
                .scaleId(request.getScaleId())
                .scaleName(request.getScaleName())
                .operator(operator)
                .tolerancePercentage(request.getTolerancePercentage())
                .remarks(request.getRemarks())
                .attachments(request.getAttachments())
                .temperature(request.getTemperature())
                .humidity(request.getHumidity())
                .build();

        // Create weighing (with auto-calculation)
        WeighingEntity created = weighingService.createWeighing(weighing);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("칭량 생성 성공", toWeighingResponse(created)));
    }

    /**
     * 칭량 수정
     * PUT /api/weighings/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 수정", description = "칭량 기록 수정 (PENDING 상태만 가능)")
    public ResponseEntity<ApiResponse<WeighingResponse>> updateWeighing(
            @PathVariable Long id,
            @Valid @RequestBody WeighingUpdateRequest request) {

        log.info("Updating weighing: {}", id);

        // Build updates
        WeighingEntity updates = WeighingEntity.builder()
                .weighingDate(request.getWeighingDate())
                .weighingType(request.getWeighingType())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .tareWeight(request.getTareWeight())
                .grossWeight(request.getGrossWeight())
                .expectedWeight(request.getExpectedWeight())
                .unit(request.getUnit())
                .scaleId(request.getScaleId())
                .scaleName(request.getScaleName())
                .tolerancePercentage(request.getTolerancePercentage())
                .remarks(request.getRemarks())
                .attachments(request.getAttachments())
                .temperature(request.getTemperature())
                .humidity(request.getHumidity())
                .build();

        // Resolve optional entities
        if (request.getProductId() != null) {
            ProductEntity product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
            updates.setProduct(product);
        }

        if (request.getLotId() != null) {
            LotEntity lot = lotRepository.findById(request.getLotId())
                    .orElse(null);
            updates.setLot(lot);
        }

        WeighingEntity updated = weighingService.updateWeighing(id, updates);

        return ResponseEntity.ok(ApiResponse.success("칭량 수정 성공", toWeighingResponse(updated)));
    }

    /**
     * 칭량 검증 또는 거부
     * POST /api/weighings/{id}/verify
     */
    @PostMapping("/{id}/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 검증/거부", description = "칭량 검증 또는 거부 (GMP 이중 검증)")
    public ResponseEntity<ApiResponse<WeighingResponse>> verifyWeighing(
            @PathVariable Long id,
            @Valid @RequestBody WeighingVerificationRequest request) {

        log.info("Verifying weighing: {} with action: {}", id, request.getAction());

        WeighingEntity result;

        if ("VERIFY".equals(request.getAction())) {
            result = weighingService.verifyWeighing(id, request.getVerifierUserId(), request.getRemarks());
        } else if ("REJECT".equals(request.getAction())) {
            result = weighingService.rejectWeighing(id, request.getVerifierUserId(), request.getRemarks());
        } else {
            throw new IllegalArgumentException("Invalid action: " + request.getAction());
        }

        String message = "VERIFY".equals(request.getAction()) ? "칭량 검증 성공" : "칭량 거부 성공";
        return ResponseEntity.ok(ApiResponse.success(message, toWeighingResponse(result)));
    }

    /**
     * 칭량 삭제
     * DELETE /api/weighings/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "칭량 삭제", description = "칭량 삭제 (PENDING 또는 REJECTED만 가능, 관리자 권한)")
    public ResponseEntity<ApiResponse<Void>> deleteWeighing(@PathVariable Long id) {
        log.info("Deleting weighing: {}", id);

        weighingService.deleteWeighing(id);

        return ResponseEntity.ok(ApiResponse.success("칭량 삭제 성공", null));
    }

    // === Helper Methods ===

    /**
     * Convert WeighingEntity to WeighingResponse
     */
    private WeighingResponse toWeighingResponse(WeighingEntity weighing) {
        return WeighingResponse.builder()
                .weighingId(weighing.getWeighingId())
                .tenantId(weighing.getTenant().getTenantId())
                .tenantName(weighing.getTenant().getTenantName())
                .weighingNo(weighing.getWeighingNo())
                .weighingDate(weighing.getWeighingDate())
                .weighingType(weighing.getWeighingType())
                .referenceType(weighing.getReferenceType())
                .referenceId(weighing.getReferenceId())
                .productId(weighing.getProduct().getProductId())
                .productCode(weighing.getProduct().getProductCode())
                .productName(weighing.getProduct().getProductName())
                .lotId(weighing.getLot() != null ? weighing.getLot().getLotId() : null)
                .lotNo(weighing.getLot() != null ? weighing.getLot().getLotNo() : null)
                .tareWeight(weighing.getTareWeight())
                .grossWeight(weighing.getGrossWeight())
                .netWeight(weighing.getNetWeight())
                .expectedWeight(weighing.getExpectedWeight())
                .variance(weighing.getVariance())
                .variancePercentage(weighing.getVariancePercentage())
                .unit(weighing.getUnit())
                .scaleId(weighing.getScaleId())
                .scaleName(weighing.getScaleName())
                .operatorUserId(weighing.getOperator().getUserId())
                .operatorUserName(weighing.getOperator().getUsername())
                .operatorName(weighing.getOperator().getUsername())
                .verifierUserId(weighing.getVerifier() != null ? weighing.getVerifier().getUserId() : null)
                .verifierUserName(weighing.getVerifier() != null ? weighing.getVerifier().getUsername() : null)
                .verifierName(weighing.getVerifier() != null ? weighing.getVerifier().getUsername() : null)
                .verificationDate(weighing.getVerificationDate())
                .verificationStatus(weighing.getVerificationStatus())
                .toleranceExceeded(weighing.getToleranceExceeded())
                .tolerancePercentage(weighing.getTolerancePercentage())
                .remarks(weighing.getRemarks())
                .attachments(weighing.getAttachments())
                .temperature(weighing.getTemperature())
                .humidity(weighing.getHumidity())
                .createdAt(weighing.getCreatedAt())
                .createdBy(null)
                .updatedAt(weighing.getUpdatedAt())
                .updatedBy(null)
                .build();
    }
}
